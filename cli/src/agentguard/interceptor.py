#!/usr/bin/env python3
import os
import sys
import subprocess
import requests
import time

RELAY_URL = os.getenv("AGENTGUARD_RELAY_URL", "https://127.0.0.1:3000")
TOKEN = os.getenv("AGENTGUARD_DEVICE_TOKEN", "demo-token-change-me")
CA_CERT = os.getenv("AGENTGUARD_CA_CERT", os.path.join(os.path.dirname(__file__), "..", "..", "..", "backend", "certs", "ca.crt"))


def get_risk_score(cmd_str):
    lowered = cmd_str.lower()
    if any(k in lowered for k in ["rm -rf", "git push --force", "drop table", "terraform destroy"]):
        return 96, "CRITICAL"
    if any(k in lowered for k in ["chmod", "chown", "kill -9", "sudo"]):
        return 65, "HIGH"
    return 15, "LOW"


def request_kwargs():
    headers = {"Authorization": f"Bearer {TOKEN}"}
    verify = CA_CERT if RELAY_URL.startswith("https://") else True
    if RELAY_URL.startswith("https://") and not os.path.exists(CA_CERT):
        raise RuntimeError(f"TLS CA certificate not found: {CA_CERT}. Run backend/scripts/generate_dev_tls.ps1 first.")
    return {"headers": headers, "verify": verify, "timeout": 10}


def main():
    if len(sys.argv) < 2:
        print("Usage: python interceptor.py <binary> [args...]")
        sys.exit(1)

    cmd_args = sys.argv[1:]
    cmd_str = " ".join(cmd_args)
    score, level = get_risk_score(cmd_str)

    print(f"\n🛡️ [AgentGuard Interceptor] Intercepted Real CLI Call: '{cmd_str}'", flush=True)
    print(f"📊 Risk Score: {score}/100 ({level})", flush=True)

    if score < 50:
        print("⚡ [AgentGuard] Low risk command auto-approved. Executing real binary...", flush=True)
        sys.exit(subprocess.run(cmd_args).returncode)

    print("🔒 [AgentGuard] High risk command detected! Sent to Mobile Guard for approval...", flush=True)
    try:
        kwargs = request_kwargs()
        req_res = requests.post(f"{RELAY_URL}/api/v1/approval/request", json={
            "device_id": "real-agent-cli",
            "agent_name": "Real-Terminal-Agent",
            "command": cmd_str,
            "risk_score": score,
            "reason": f"Intercepted live real terminal execution of '{cmd_str}'"
        }, **kwargs)
        req_res.raise_for_status()
        req_id = req_res.json()["id"]
        print(f"📱 Waiting for Mobile Guard approval ID: {req_id}", flush=True)

        started = time.time()
        while time.time() - started < 30:
            status_res = requests.get(f"{RELAY_URL}/api/v1/approval/wait/{req_id}", **kwargs)
            if status_res.ok:
                status = status_res.json().get("status")
                if status == "APPROVED":
                    print("✅ [AgentGuard] Biometric approval granted. Executing real binary...", flush=True)
                    sys.exit(subprocess.run(cmd_args).returncode)
                if status == "DENIED":
                    print("🚫 [AgentGuard] Command execution DENIED by user on phone!", flush=True)
                    sys.exit(1)
            time.sleep(1)

        print("⏰ [AgentGuard] Approval TIMEOUT (30s). Execution aborted.", flush=True)
        sys.exit(1)
    except Exception as e:
        print(f"❌ Interceptor error: {e}", flush=True)
        sys.exit(1)


if __name__ == "__main__":
    main()
