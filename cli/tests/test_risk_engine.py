import pytest
from agentguard.risk_engine import analyze_command, RiskScore

def test_risk_engine_low():
    res = analyze_command("ls -la", "dev")
    assert res["score"] == 0
    assert res["level"] == RiskScore.LOW

def test_risk_engine_prod_multiplier():
    res_dev = analyze_command("rm file.txt", "dev")
    res_prod = analyze_command("rm file.txt", "prod")
    
    assert res_prod["score"] == res_dev["score"] * 4

def test_risk_engine_critical():
    res = analyze_command("sudo rm -rf /", "prod")
    assert res["level"] == RiskScore.CRITICAL
    assert res["score"] == 100
