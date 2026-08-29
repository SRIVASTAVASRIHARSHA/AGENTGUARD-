const crypto = require('crypto');

class Store {
    constructor() {
        this.devices = new Map(); // pubKey -> { id, pubKey, name }
        this.approvals = new Map(); // id -> { id, payload, status, createdAt }
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
        const approval = { ...request, id, status: 'PENDING', createdAt: Date.now() };
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
