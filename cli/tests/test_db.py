import pytest
import os
from agentguard.db import AuditDB

@pytest.fixture
def test_db():
    db_path = "test_audit.db"
    db = AuditDB(db_path)
    yield db
    if os.path.exists(db_path):
        os.remove(db_path)

def test_db_chain(test_db):
    test_db.log_action({"cmd": "ls"})
    test_db.log_action({"cmd": "pwd"})
    
    valid, msg = test_db.verify_chain()
    assert valid == True
