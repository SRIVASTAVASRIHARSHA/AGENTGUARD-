import json
import nacl.signing
import nacl.encoding
import base64

def generate_keypair():
    """Generates an Ed25519 keypair and returns base64 encoded private and public keys."""
    signing_key = nacl.signing.SigningKey.generate()
    verify_key = signing_key.verify_key
    
    sk_b64 = base64.b64encode(signing_key.encode(encoder=nacl.encoding.RawEncoder)).decode('utf-8')
    vk_b64 = base64.b64encode(verify_key.encode(encoder=nacl.encoding.RawEncoder)).decode('utf-8')
    
    return sk_b64, vk_b64

def _canonicalize(payload):
    """Sort keys and remove whitespace."""
    if isinstance(payload, str):
        payload = json.loads(payload)
    return json.dumps(payload, separators=(',', ':'), sort_keys=True).encode('utf-8')

def sign_payload(payload, private_key_b64):
    """Signs a JSON payload using canonical JSON serialization."""
    canonical_data = _canonicalize(payload)
    sk_bytes = base64.b64decode(private_key_b64)
    signing_key = nacl.signing.SigningKey(sk_bytes, encoder=nacl.encoding.RawEncoder)
    
    signed = signing_key.sign(canonical_data)
    signature_b64 = base64.b64encode(signed.signature).decode('utf-8')
    
    return signature_b64

def verify_signature(payload, signature_b64, public_key_b64):
    """Verifies a signature."""
    canonical_data = _canonicalize(payload)
    vk_bytes = base64.b64decode(public_key_b64)
    verify_key = nacl.signing.VerifyKey(vk_bytes, encoder=nacl.encoding.RawEncoder)
    
    sig_bytes = base64.b64decode(signature_b64)
    
    try:
        verify_key.verify(canonical_data, sig_bytes)
        return True
    except nacl.exceptions.BadSignatureError:
        return False
