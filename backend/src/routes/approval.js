const express = require('express');
const store = require('../models/store');
const phoneHub = require('../ws/phoneHub');
const router = express.Router();

function createAndNotify(req, res, code) {
    const approval = store.createApproval(req.body);
    phoneHub.notifyApproval(approval);
    res.status(code).json(approval);
}

router.post('/request', (req, res) => createAndNotify(req, res, 202));
router.post('/', (req, res) => createAndNotify(req, res, 201));

router.get('/pending', (req, res) => {
    res.json(Array.from(store.approvals.values()).filter(a => a.status === 'PENDING'));
});

router.get('/active-agents', (req, res) => {
    const agentsMap = new Map();
    for (const a of store.approvals.values()) {
        const agentName = a.agent_name || a.device_id || 'CLI Agent';
        agentsMap.set(agentName, {
            agent: agentName,
            status: a.status === 'PENDING' ? 'RUNNING (INTERCEPTED)' : 'COMPLETED / IDLE',
            current_command: a.command,
            risk_score: a.risk_score,
            last_active: a.createdAt
        });
    }
    res.json(Array.from(agentsMap.values()));
});

router.get('/wait/:id', (req, res) => {
    const approval = store.getApproval(req.params.id);
    if (!approval) return res.status(404).json({ error: 'Not found' });
    res.json(approval);
});

router.post('/response', (req, res) => {
    const approval = store.updateApprovalStatus(req.body.request_id, req.body.status);
    res.json(approval || { status: 'ok' });
});

router.post('/:id/respond', (req, res) => {
    const approval = store.updateApprovalStatus(req.params.id, req.body.status);
    res.json(approval || { status: 'ok' });
});

module.exports = router;
