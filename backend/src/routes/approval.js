const express = require('express');
const store = require('../models/store');
const { verifySignature } = require('../auth/crypto');
const router = express.Router();

router.post('/', (req, res) => {
    const approval = store.createApproval(req.body);
    res.status(201).json(approval);
});

router.post('/:id/respond', (req, res) => {
    const { id } = req.params;
    const { status, pubKey, signature } = req.body;
    
    const device = store.getDevice(pubKey);
    if (!device) {
        return res.status(401).json({ error: 'Unknown device' });
    }

    const payload = { id, status };
    if (!verifySignature(payload, signature, pubKey)) {
        return res.status(401).json({ error: 'Invalid signature' });
    }

    const approval = store.updateApprovalStatus(id, status);
    if (!approval) {
        return res.status(404).json({ error: 'Approval not found' });
    }

    res.json(approval);
});

module.exports = router;
