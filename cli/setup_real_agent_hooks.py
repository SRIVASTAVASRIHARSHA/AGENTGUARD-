#!/usr/bin/env python3
"""
AgentGuard System-Wide CLI Hook Alias Setup Script.
This script sets up shell aliases and PATH overrides so that any command executed in your terminal
or by an AI agent (Claude Code, Gemini, Hermes, OpenClaw) automatically routes through AgentGuard!
"""

import os
import sys

SHELL_RC = os.path.expanduser("~/.bashrc")
HOOK_COMMENT = "# === AgentGuard Real Binary Hooks ==="

HOOK_CONTENT = f"""
{HOOK_COMMENT}
alias claude="python3 {os.path.abspath('cli/src/agentguard/agentguard_wrapper.py')} /home/whysooraj/.local/bin/claude"
alias gemini="python3 {os.path.abspath('cli/src/agentguard/agentguard_wrapper.py')} /home/whysooraj/.npm-global/bin/gemini"
alias hermes="python3 {os.path.abspath('cli/src/agentguard/agentguard_wrapper.py')} /home/whysooraj/.local/bin/hermes"
alias openclaw="python3 {os.path.abspath('cli/src/agentguard/agentguard_wrapper.py')} /home/whysooraj/.npm-global/bin/openclaw"
"""

def setup_hooks():
    print("🛠️ Setting up AgentGuard system-wide agent CLI hooks...")
    
    with open(SHELL_RC, "r") as f:
        content = f.read()

    if HOOK_COMMENT in content:
        print("✅ Hooks already configured in ~/.bashrc!")
    else:
        with open(SHELL_RC, "a") as f:
            f.write(HOOK_CONTENT)
        print("🎉 Successfully added agent aliases to ~/.bashrc!")

    print("\n👉 To run a real agent right now and see it wait for Mobile Guard approval:")
    print("   1. Open a new terminal on your laptop.")
    print("   2. Run your real Claude agent:")
    print(f"      python3 {os.path.abspath('cli/src/agentguard/agentguard_wrapper.py')} /home/whysooraj/.local/bin/claude 'git push --force origin main'")
    print("   3. Or run Gemini agent:")
    print(f"      python3 {os.path.abspath('cli/src/agentguard/agentguard_wrapper.py')} /home/whysooraj/.npm-global/bin/gemini 'rm -rf /tmp/test'")

if __name__ == "__main__":
    setup_hooks()
