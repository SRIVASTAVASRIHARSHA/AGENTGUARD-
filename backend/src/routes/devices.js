const express = require('express');
const store = require('../models/store');
const { verifySignature } = require('../auth/crypto');
const router = express.Router();

router.post('/register', (req, res) => {
    const device_id = req.body.device_id || (req.body.device && req.body.device.id);
    const pubKey = req.body.public_key || req.body.pubKey || (req.body.device && req.body.device.pubKey);
    const type = req.body.type || 'device';

    store.registerDevice({ id: device_id, pubKey, type });
    res.status(201).json({ success: true, device_id });
});

module.exports = router;
