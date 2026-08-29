const clients = new Map();

function register(phoneId, ws) {
    clients.set(phoneId, ws);
    ws.on('close', () => {
        if (clients.get(phoneId) === ws) clients.delete(phoneId);
    });
}

function notifyApproval(approval) {
    const message = JSON.stringify({
        type: 'APPROVAL_REQUEST',
        requestId: approval.id,
        command: approval.command,
        riskScore: approval.risk_score,
        level: approval.level,
        context: approval.context || {},
        reason: approval.reason || ''
    });

    for (const [phoneId, ws] of clients.entries()) {
        if (ws.readyState === 1) {
            ws.send(message);
        } else {
            clients.delete(phoneId);
        }
    }
}

module.exports = { register, notifyApproval };
