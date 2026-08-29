const express = require('express');
const store = require('../models/store');
const { verifySignature } = require('../auth/crypto');
const router = express.Router();

router.post('/request', (req, res) => {
    const approval = store.createApproval(req.body);
    res.status(202).json(approval);
});

router.post('/', (req, res) => {
    const approval = store.createApproval(req.body);
    res.status(201).json(approval);
});

router.get('/pending', (req, res) => {
    const pending = Array.from(store.approvals.values()).filter(a => a.status === 'PENDING');
    res.json(pending);
});

router.get('/active-agents', (req, res) => {
    const approvals = Array.from(store.approvals.values());
    const agentsMap = new Map();

    // Group active agent sessions
    approvals.forEach(a => {
        const agentName = a.agent_name || a.device_id || 'CLI Agent';
        if (!agentsMap.has(agentName)) {
            agentsMap.set(agentName, {
                agent: agentName,
                status: a.status === 'PENDING' ? 'RUNNING (INTERCEPTED)' : 'IDLE',
                current_command: a.command,
                risk_score: a.risk_score,
                last_active: a.createdAt
            });
        }
    });

    if (agentsMap.size === 0) {
        agentsMap.set('Gemini-Code-Agent', { agent: 'Gemini-Code-Agent', status: 'IDLE', current_command: 'None', risk_score: 0 });
        agentsMap.set('Claude-Code-Agent', { agent: 'Claude-Code-Agent', status: 'IDLE', current_command: 'None', risk_score: 0 });
        agentsMap.set('Hermes-Agent', { agent: 'Hermes-Agent', status: 'IDLE', current_command: 'None', risk_score: 0 });
        agentsMap.set('OpenClaw-Agent', { agent: 'OpenClaw-Agent', status: 'IDLE', current_command: 'None', risk_score: 0 });
    }

    res.json(Array.from(agentsMap.values()));
});

router.get('/wait/:id', (req, res) => {
    const { id } = req.params;
    const approval = store.approvals.get(id);
    if (!approval) return res.status(404).json({ error: 'Not found' });
    res.json(approval);
});

router.post('/response', (req, res) => {
    const { request_id, status } = req.body;
    const approval = store.updateApprovalStatus(request_id, status);
    res.json(approval || { status: 'ok' });
});

router.post('/:id/respond', (req, res) => {
    const { id } = req.params;
    const { status } = req.body;
    const approval = store.updateApprovalStatus(id, status);
    res.json(approval || { status: 'ok' });
});

module.exports = router;
