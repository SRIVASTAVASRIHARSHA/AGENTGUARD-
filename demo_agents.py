"""
AgentGuard AI Agent Integration Suite
Supports: Gemini, Claude Code, Hermes, OpenClaw, AutoGPT, & custom subagents.
"""

import sys
import os
import subprocess
import json
import time
import requests

RELAY_URL = os.getenv("AGENTGUARD_RELAY", "http://192.168.0.140:3000")
DEVICE_ID = os.getenv("AGENTGUARD_DEVICE_ID", "laptop-agent-runner")

class AgentGuardInterceptor:
    def __init__(self, agent_name="AI-Agent"):
        self.agent_name = agent_name
        self.relay_url = RELAY_URL

    def check_command(self, command, env="prod", reason=None):
        """
        Calculates risk score, posts request to AgentGuard Relay, and long-polls for approval.
        """
        print(f"\n🤖 [{self.agent_name}] Agent requesting command execution:")
        print(f"   💻 Command: '{command}'")
        
        payload = {
            "device_id": DEVICE_ID,
            "command": command,
            "environment": env,
            "agent_name": self.agent_name,
            "reason": reason or f"Triggered by {self.agent_name} agent loop"
        }

        try:
            # 1. Post request to AgentGuard Cloud Relay
            res = requests.post(f"{self.relay_url}/approvals/request", json=payload, timeout=5)
            if res.status_code != 200 and res.status_code != 202:
                print(f"⚠️ [AgentGuard] Relay response: {res.status_code}. Proceeding with caution.")
                return True, "BYPASS"

            req_data = res.json()
            req_id = req_data.get("id") or req_data.get("request_id")
            score = req_data.get("risk_score", 0)
            level = req_data.get("level", "LOW")

            print(f"🛡️ [AgentGuard] Command Risk Score: {score}/100 ({level})")

            # 2. Auto-approve low risk commands
            if score <= 30:
                print("⚡ [AgentGuard] Auto-approved low risk command.")
                return True, "AUTO_APPROVED"

            # 3. High / Critical risk requires Mobile Biometric Approval
            print(f"🔒 [AgentGuard] Mobile Biometric Approval Required! ID: {req_id}")
            print(f"📱 Please open Mobile Guard on your phone to approve/deny...")

            # 4. Long-poll wait for user decision (30s timeout)
            start_time = time.time()
            while time.time() - start_time < 30:
                poll_res = requests.get(f"{self.relay_url}/approvals/wait/{req_id}", timeout=10)
                if poll_res.status_code == 200:
                    status_data = poll_res.json()
                    status = status_data.get("status")
                    if status == "APPROVED":
                        print("✅ [AgentGuard] Mobile Biometric Verification SUCCESSFUL! Command approved.")
                        return True, "APPROVED"
                    elif status == "DENIED":
                        print("❌ [AgentGuard] Command DENIED by mobile user!")
                        return False, "DENIED"
                time.sleep(1)

            print("⏰ [AgentGuard] Approval TIMEOUT (30s). Blocking execution for safety.")
            return False, "TIMEOUT"

        except Exception as e:
            print(f"⚠️ [AgentGuard] Communication error: {e}")
            return False, "ERROR"

    def execute_guarded(self, command, env="prod", reason=None):
        allowed, status = self.check_command(command, env=env, reason=reason)
        if not allowed:
            print(f"🚫 [{self.agent_name}] Execution aborted due to AgentGuard policy ({status}).")
            return None
        
        print(f"🚀 [{self.agent_name}] Executing command...")
        result = subprocess.run(command, shell=True, capture_output=True, text=True)
        print(f"📋 Exit Code: {result.returncode}")
        return result


# --- AGENT WRAPPERS ---

def demo_gemini_agent():
    print("\n=======================================================")
    print("🤖 DEMO: Google Gemini Code Agent Integration")
    print("=======================================================")
    guard = AgentGuardInterceptor(agent_name="Gemini-Agent")
    # Low Risk Demo
    guard.execute_guarded("git status", env="dev")
    # Critical Risk Demo
    guard.execute_guarded("git push --force origin main", env="prod", reason="Gemini refactor force push")

def demo_claude_agent():
    print("\n=======================================================")
    print("🤖 DEMO: Claude Code Agent Integration")
    print("=======================================================")
    guard = AgentGuardInterceptor(agent_name="Claude-Code")
    guard.execute_guarded("rm -rf /tmp/scratch_data", env="prod", reason="Claude cleanup step")

def demo_hermes_agent():
    print("\n=======================================================")
    print("🤖 DEMO: Hermes Autonomous Agent Integration")
    print("=======================================================")
    guard = AgentGuardInterceptor(agent_name="Hermes-Agent")
    guard.execute_guarded("chmod 777 /etc/config", env="prod", reason="Hermes permission update")

def demo_openclaw_agent():
    print("\n=======================================================")
    print("🤖 DEMO: OpenClaw Agent Integration")
    print("=======================================================")
    guard = AgentGuardInterceptor(agent_name="OpenClaw-Agent")
    guard.execute_guarded("terraform destroy --auto-approve", env="prod", reason="OpenClaw teardown task")

if __name__ == "__main__":
    agent_type = sys.argv[1] if len(sys.argv) > 1 else "all"
    
    if agent_type == "gemini":
        demo_gemini_agent()
    elif agent_type == "claude":
        demo_claude_agent()
    elif agent_type == "hermes":
        demo_hermes_agent()
    elif agent_type == "openclaw":
        demo_openclaw_agent()
    else:
        demo_gemini_agent()
        demo_claude_agent()
        demo_hermes_agent()
        demo_openclaw_agent()
