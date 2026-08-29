const express = require('express');
const store = require('../models/store');
const router = express.Router();

router.get('/', (req, res) => {
    res.json(store.getAuditLogs());
});

module.exports = router;
