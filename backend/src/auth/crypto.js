const nacl = require('tweetnacl');
const util = require('tweetnacl-util');

function canonicalize(obj) {
    if (obj === null || typeof obj !== 'object') {
        return JSON.stringify(obj);
    }
    if (Array.isArray(obj)) {
        return '[' + obj.map(canonicalize).join(',') + ']';
    }
    const keys = Object.keys(obj).sort();
    let result = '{';
    for (let i = 0; i < keys.length; i++) {
        const key = keys[i];
        if (obj[key] !== undefined) {
            result += JSON.stringify(key) + ':' + canonicalize(obj[key]);
            if (i < keys.length - 1) {
                result += ',';
            }
        }
    }
    result += '}';
    return result.replace(/,}/g, '}'); // Clean up any trailing commas
}

function verifySignature(payload, signatureBase64, publicKeyBase64) {
    try {
        const message = util.decodeUTF8(canonicalize(payload));
        const signature = util.decodeBase64(signatureBase64);
        const publicKey = util.decodeBase64(publicKeyBase64);
        return nacl.sign.detached.verify(message, signature, publicKey);
    } catch (e) {
        return false;
    }
}

module.exports = {
    canonicalize,
    verifySignature
};
