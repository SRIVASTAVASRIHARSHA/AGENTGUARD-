const express = require('express');
const http = require('http');
const https = require('https');
const fs = require('fs');
const WebSocket = require('ws');
const devicesRouter = require('./routes/devices');
const approvalRouter = require('./routes/approval');
const auditRouter = require('./routes/audit');
const store = require('./models/store');
const { verifySignature } = require('./auth/crypto');
const phoneHub = require('./ws/phoneHub');

const app = express();
app.use(express.json());
const TOKEN = process.env.AGENTGUARD_DEVICE_TOKEN || 'demo-token-change-me';

function authorized(req) {
    return req.headers.authorization === `Bearer ${TOKEN}` || req.headers['x-agentguard-token'] === TOKEN;
}

app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization, X-AgentGuard-Token, X-Device-Id');
    res.header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    if (req.method === 'OPTIONS') return res.sendStatus(200);
    if (req.path === '/health') return next();
    if (!authorized(req)) return res.status(401).json({ error: 'Unauthorized' });
    next();
});

app.use('/devices', devicesRouter);
app.use('/api/v1/devices', devicesRouter);
app.use('/approvals', approvalRouter);
app.use('/api/v1/approval', approvalRouter);
app.use('/audit', auditRouter);
app.use('/api/v1/audit', auditRouter);
app.get('/health', (req, res) => res.json({ status: 'ok', secure: process.env.AGENTGUARD_TLS === '1' }));

const useTls = process.env.AGENTGUARD_TLS === '1';
let server;
if (useTls) {
    const keyPath = process.env.AGENTGUARD_TLS_KEY || './certs/server.key';
    const certPath = process.env.AGENTGUARD_TLS_CERT || './certs/server.crt';
    if (!fs.existsSync(keyPath) || !fs.existsSync(certPath)) throw new Error(`TLS enabled but certificate files are missing: ${keyPath}, ${certPath}`);
    server = https.createServer({ key: fs.readFileSync(keyPath), cert: fs.readFileSync(certPath) }, app);
} else {
    server = http.createServer(app);
}

const wss = new WebSocket.Server({
    server,
    verifyClient: ({ req }, done) => done(req.headers.authorization === `Bearer ${TOKEN}` || req.headers['x-agentguard-token'] === TOKEN)
});

wss.on('connection', (ws, req) => {
    const base = useTls ? 'https://agentguard.local' : 'http://agentguard.local';
    const path = new URL(req.url, base).pathname.split('/');
    const phoneId = decodeURIComponent(path[path.length - 1] || 'phone');
    phoneHub.register(phoneId, ws);

    ws.on('message', (message) => {
        try {
            const body = JSON.parse(message.toString());
            if (body.type === 'REGISTER_DEVICE') {
                store.registerDevice({ id: phoneId, pubKey: body.public_key, type: 'phone' });
                ws.send(JSON.stringify({ type: 'REGISTERED', deviceId: phoneId }));
                return;
            }
            if (body.type === 'APPROVAL_RESPONSE') {
                const approval = store.getApproval(body.request_id);
                if (!approval) return ws.send(JSON.stringify({ type: 'ERROR', error: 'Approval not found' }));
                const device = store.getDevice(phoneId);
                const signed = { request_id: body.request_id, status: body.status, timestamp: body.timestamp };
                const fresh = Number.isFinite(body.timestamp) && Math.abs(Date.now() - body.timestamp) <= 60_000;
                if (!device || !fresh || !verifySignature(signed, body.signature, device.pubKey)) {
                    return ws.send(JSON.stringify({ type: 'ERROR', error: 'Invalid approval signature or expired timestamp' }));
                }
                store.updateApprovalStatus(body.request_id, body.status);
            }
        } catch (_) {
            ws.send(JSON.stringify({ type: 'ERROR', error: 'Invalid message' }));
        }
    });
});

if (require.main === module) {
    server.listen(3000, '0.0.0.0', () => console.log(`${useTls ? 'HTTPS/WSS' : 'HTTP/WS'} AgentGuard relay started on port 3000`));
}

module.exports = { app, server, TOKEN };
