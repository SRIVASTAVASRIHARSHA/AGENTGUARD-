import re

class RiskScore:
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"

def get_risk_level(score):
    if score <= 30:
        return RiskScore.LOW
    elif score <= 60:
        return RiskScore.MEDIUM
    elif score <= 90:
        return RiskScore.HIGH
    else:
        return RiskScore.CRITICAL

DANGEROUS_PATTERNS = {
    r'\brm\s+-rf\b': 90,
    r'\brm\b': 40,
    r'\bmv\s+.*?\s+/dev/null\b': 70,
    r'>\s*/dev/sda': 100,
    r'\bmkfs\b': 100,
    r'\bchmod\s+777\b': 60,
    r'\bchown\s+root\b': 60,
    r'\bpasswd\b': 80,
    r'\bwget\b': 30,
    r'\bcurl\b': 30,
    r'\bnc\b': 70,
    r'\bnetcat\b': 70,
    r'\btelnet\b': 50,
    r'\bssh\b': 40,
    r'\bscp\b': 40,
    r'\bftp\b': 40,
    r'\bsudo\b': 50,
    r'\bsu\b': 60,
    r'\bdd\b': 80,
    r'\bkill\s+-9\b': 50,
    r'\bkillall\b': 50,
    r'\biptables\b': 80,
    r'\bhistory\s+-c\b': 70,
}

ENV_MULTIPLIERS = {
    "dev": 0.5,
    "staging": 1.0,
    "prod": 2.0
}

def analyze_command(command, environment="dev"):
    base_score = 0
    matched_patterns = []
    
    for pattern, score in DANGEROUS_PATTERNS.items():
        if re.search(pattern, command):
            base_score = max(base_score, score)
            matched_patterns.append(pattern)
            
    multiplier = ENV_MULTIPLIERS.get(environment, 1.0)
    final_score = min(100, int(base_score * multiplier))
    
    return {
        "score": final_score,
        "level": get_risk_level(final_score),
        "matched": matched_patterns
    }
