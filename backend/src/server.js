const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const devicesRouter = require('./routes/devices');
const approvalRouter = require('./routes/approval');
const auditRouter = require('./routes/audit');

const app = express();
app.use(express.json());

app.use('/devices', devicesRouter);
app.use('/approvals', approvalRouter);
app.use('/audit', auditRouter);

const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

wss.on('connection', (ws) => {
    ws.on('message', (message) => {
        // Basic echo or ws handling for now
    });
});

if (require.main === module) {
    server.listen(3000, () => {
        console.log('Server started on port 3000');
    });
}

module.exports = { app, server };
