const request = require('supertest');
const { app, server } = require('../src/server');
const nacl = require('tweetnacl');
const util = require('tweetnacl-util');
const { canonicalize } = require('../src/auth/crypto');



describe('Relay Server API', () => {
    let keyPair;
    let pubKeyBase64;

    beforeAll(() => {
        keyPair = nacl.sign.keyPair();
        pubKeyBase64 = util.encodeBase64(keyPair.publicKey);
    });

    test('Register device', async () => {
        const device = { name: 'My Device', pubKey: pubKeyBase64 };
        const msg = util.decodeUTF8(canonicalize(device));
        const sig = nacl.sign.detached(msg, keyPair.secretKey);
        const signature = util.encodeBase64(sig);

        const res = await request(app)
            .post('/devices/register')
            .send({ device, signature });
        
        expect(res.status).toBe(201);
    });

    let approvalId;

    test('Create approval', async () => {
        const res = await request(app)
            .post('/approvals')
            .send({ action: 'DROP_TABLE' });
        
        expect(res.status).toBe(201);
        expect(res.body.id).toBeDefined();
        approvalId = res.body.id;
    });

    test('Respond to approval', async () => {
        const payload = { id: approvalId, status: 'APPROVED' };
        const msg = util.decodeUTF8(canonicalize(payload));
        const sig = nacl.sign.detached(msg, keyPair.secretKey);
        const signature = util.encodeBase64(sig);

        const res = await request(app)
            .post(`/approvals/${approvalId}/respond`)
            .send({ status: 'APPROVED', pubKey: pubKeyBase64, signature });
        
        expect(res.status).toBe(200);
        expect(res.body.status).toBe('APPROVED');
    });

    test('Audit log', async () => {
        const res = await request(app).get('/audit');
        expect(res.status).toBe(200);
        expect(res.body.length).toBeGreaterThan(0);
        expect(res.body[0].hash).toBeDefined();
    });
});
