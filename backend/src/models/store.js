const crypto = require('crypto');

class Store {
    constructor() {
        this.devices = new Map(); // pubKey -> { id, pubKey, name }
        this.approvals = new Map(); // id -> { id, payload, status, createdAt }
        this.auditLog = [];
        this.lastHash = crypto.createHash('sha256').update('genesis').digest('hex');
    }

    reset() {
        this.devices.clear();
        this.approvals.clear();
        this.auditLog = [];
        this.lastHash = crypto.createHash('sha256').update('genesis').digest('hex');
    }

    registerDevice(device) {
        this.devices.set(device.pubKey, device);
        this.appendAuditLog('DEVICE_REGISTERED', device);
    }

    getDevice(pubKey) {
        return this.devices.get(pubKey);
    }

    createApproval(request) {
        const id = crypto.randomUUID();
        let score = request.risk_score || 0;
        let level = 'LOW';
        
        const cmd = request.command || '';
        if (score === 0) {
            if (/rm\s+-rf|terraform\s+destroy|git\s+push\s+--force|DROP\s+TABLE/i.test(cmd)) {
                score = 96;
                level = 'CRITICAL';
            } else if (/chmod|chown|kill\s+-9|sudo/i.test(cmd)) {
                score = 65;
                level = 'HIGH';
            } else {
                score = 15;
                level = 'LOW';
            }
        }
        
        const approval = { ...request, id, risk_score: score, level, status: 'PENDING', createdAt: Date.now() };
        this.approvals.set(id, approval);
        this.appendAuditLog('APPROVAL_CREATED', approval);
        return approval;
    }

    getApproval(id) {
        return this.approvals.get(id);
    }

    updateApprovalStatus(id, status) {
        const approval = this.approvals.get(id);
        if (approval) {
            approval.status = status;
            this.appendAuditLog('APPROVAL_STATUS_UPDATED', { id, status });
        }
        return approval;
    }

    appendAuditLog(action, data) {
        const entry = {
            timestamp: Date.now(),
            action,
            data,
            prevHash: this.lastHash
        };
        const entryString = JSON.stringify(entry);
        this.lastHash = crypto.createHash('sha256').update(entryString).digest('hex');
        entry.hash = this.lastHash;
        this.auditLog.push(entry);
        return entry;
    }
    
    getAuditLogs() {
        return this.auditLog;
    }
}

module.exports = new Store();
