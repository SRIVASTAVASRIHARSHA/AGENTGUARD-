import sqlite3
import hashlib
import json

class AuditDB:
    def __init__(self, db_path="audit.db"):
        self.db_path = db_path
        self.conn = sqlite3.connect(self.db_path)
        self._init_db()
        
    def _init_db(self):
        self.conn.execute('''
            CREATE TABLE IF NOT EXISTS audit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                payload TEXT,
                hash TEXT,
                prev_hash TEXT
            )
        ''')
        self.conn.commit()
            
    def _get_last_hash(self):
        cursor = self.conn.cursor()
        cursor.execute("SELECT hash FROM audit_logs ORDER BY id DESC LIMIT 1")
        row = cursor.fetchone()
        return row[0] if row else "0" * 64

    def log_action(self, payload):
        payload_str = json.dumps(payload, sort_keys=True)
        prev_hash = self._get_last_hash()
        
        # Hash chain: H(payload + prev_hash)
        hash_input = (payload_str + prev_hash).encode('utf-8')
        current_hash = hashlib.sha256(hash_input).hexdigest()
        
        self.conn.execute(
            "INSERT INTO audit_logs (payload, hash, prev_hash) VALUES (?, ?, ?)",
            (payload_str, current_hash, prev_hash)
        )
        self.conn.commit()
        return current_hash
        
    def verify_chain(self):
        cursor = self.conn.cursor()
        cursor.execute("SELECT id, payload, hash, prev_hash FROM audit_logs ORDER BY id ASC")
        
        expected_prev_hash = "0" * 64
        for row in cursor.fetchall():
            log_id, payload, current_hash, prev_hash = row
            
            if prev_hash != expected_prev_hash:
                return False, f"Broken chain at id {log_id}: prev_hash mismatch"
                
            hash_input = (payload + prev_hash).encode('utf-8')
            calculated_hash = hashlib.sha256(hash_input).hexdigest()
            
            if calculated_hash != current_hash:
                return False, f"Broken chain at id {log_id}: hash mismatch"
                
            expected_prev_hash = current_hash
            
        return True, "Chain is valid"
