#!/usr/bin/env python3
"""
AgentGuard Real Command Line Wrapper.
This script wraps REAL local binaries (claude, gemini, hermes, openclaw, git, etc.),
intercepts their shell commands, scores the risk, and holds execution until you approve on your mobile phone!
"""

import sys
import os
import subprocess
import requests
import time

RELAY_URL = "http://127.0.0.1:3000"

def calculate_risk(cmd_str):
    cmd_lower = cmd_str.lower()
    if any(k in cmd_lower for k in ["rm -rf", "git push --force", "drop table", "terraform destroy", "sudo", "eval"]):
        return 96, "CRITICAL"
    elif any(k in cmd_lower for k in ["chmod", "chown", "kill -9", "pip install", "npm install -g"]):
        return 65, "HIGH"
    else:
        return 15, "LOW"

def run_agentguard_wrapper():
    if len(sys.argv) < 2:
        print("Usage: agentguard-run <agent_binary_or_command> [args...]")
        print("Example: agentguard-run claude 'git push --force origin main'")
        sys.exit(1)

    agent_binary = sys.argv[1]
    cmd_args = sys.argv[1:]
    cmd_str = " ".join(cmd_args)
    score, level = calculate_risk(cmd_str)

    print(f"\n=======================================================")
    print(f"🛡️ [AgentGuard Real Interceptor] Intercepted Execution")
    print(f"=======================================================")
    print(f"🤖 Agent/Tool: '{agent_binary}'")
    print(f"💻 Command:    '{cmd_str}'")
    print(f"📊 Risk Score: {score}/100 ({level})")

    if score < 50:
        print("⚡ [AgentGuard] Low risk command auto-approved. Executing binary now...")
        res = subprocess.run(cmd_args)
        sys.exit(res.returncode)

    # Send REAL request to backend relay
    print("🔒 [AgentGuard] High risk command detected! Sent to Mobile Guard for approval...")
    try:
        req_res = requests.post(f"{RELAY_URL}/api/v1/approval/request", json={
            "device_id": "real-agent-cli",
            "agent_name": f"Real-{agent_binary.capitalize()}-Agent",
            "command": cmd_str,
            "risk_score": score,
            "reason": f"Intercepted live real binary execution: '{cmd_str}'"
        })
        approval_data = req_res.json()
        req_id = approval_data["id"]

        print(f"📱 Please open Mobile Guard on your phone to approve/deny ID: {req_id}")
        print("⏳ Waiting for mobile biometric approval (30s timeout)...")

        start_time = time.time()
        while time.time() - start_time < 30:
            status_res = requests.get(f"{RELAY_URL}/api/v1/approval/wait/{req_id}")
            if status_res.ok:
                st = status_res.json().get("status")
                if st == "APPROVED":
                    print("\n✅ [AgentGuard] Biometric approval granted on phone! Executing real binary now...\n")
                    res = subprocess.run(cmd_args)
                    sys.exit(res.returncode)
                elif st == "DENIED":
                    print("\n🚫 [AgentGuard] Command execution DENIED by user on phone!")
                    sys.exit(1)
            time.sleep(1)

        print("\n⏰ [AgentGuard] Approval TIMEOUT (30s). Execution aborted.")
        sys.exit(1)
    except Exception as e:
        print(f"❌ Interceptor error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    run_agentguard_wrapper()
