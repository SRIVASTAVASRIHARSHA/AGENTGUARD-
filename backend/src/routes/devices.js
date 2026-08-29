const express = require('express');
const store = require('../models/store');
const { verifySignature } = require('../auth/crypto');
const router = express.Router();

router.post('/register', (req, res) => {
    const { device, signature } = req.body;
    if (!device || !device.pubKey || !signature) {
        return res.status(400).json({ error: 'Missing parameters' });
    }
    
    if (!verifySignature(device, signature, device.pubKey)) {
        return res.status(401).json({ error: 'Invalid signature' });
    }

    store.registerDevice(device);
    res.status(201).json({ success: true });
});

module.exports = router;
