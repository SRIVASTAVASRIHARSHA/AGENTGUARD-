import pytest
from agentguard.crypto import generate_keypair, sign_payload, verify_signature

def test_crypto_cycle():
    sk, vk = generate_keypair()
    payload = {"command": "ls -l", "env": "prod"}
    
    signature = sign_payload(payload, sk)
    assert verify_signature(payload, signature, vk) == True

    # modify payload
    bad_payload = {"command": "rm -rf /", "env": "prod"}
    assert verify_signature(bad_payload, signature, vk) == False
