const express = require('express');
const store = require('../models/store');
const router = express.Router();

router.get('/', (req, res) => {
    res.json(store.getAuditLogs());
});

router.post('/clear', (req, res) => {
    store.reset();
    res.json({ status: 'cleared' });
});

module.exports = router;
