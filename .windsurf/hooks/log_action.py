#!/usr/bin/env python3
"""
Cascade Hook: Unified Action Logger
====================================
Demonstrates: LOGGING + FULL COVERAGE

Logs every Cascade action (file reads, writes, commands, MCP calls,
user prompts, and Cascade responses) to a single audit log file.
Each entry captures the event type, timestamp, and relevant details.
"""

import sys
import json
import os
from datetime import datetime

LOG_DIR = os.path.join(os.path.dirname(__file__), "logs")
LOG_FILE = os.path.join(LOG_DIR, "cascade_audit.log")


def format_event(data: dict) -> str:
    """Format a hook event into a human-readable log entry."""
    action = data.get("agent_action_name", "unknown")
    timestamp = data.get("timestamp", datetime.now().isoformat())
    
    lines = [
        f"[{timestamp}] {action}",
    ]

    lines.append(f"  Full data: {json.dumps(data, indent=2)}")

    return "\n".join(lines)


def main():
    input_data = sys.stdin.read()

    try:
        data = json.loads(input_data)
    except json.JSONDecodeError as e:
        print(f"Error parsing JSON: {e}", file=sys.stderr)
        sys.exit(1)

    entry = format_event(data)

    # Ensure log directory exists
    os.makedirs(LOG_DIR, exist_ok=True)

    # Append to audit log
    with open(LOG_FILE, "a") as f:
        f.write(entry + "\n" + "-" * 60 + "\n")

    # Print summary to Cascade UI (visible when show_output is true)
    action = data.get("agent_action_name", "unknown")
    print(f"[LOG] {action} recorded")


if __name__ == "__main__":
    main()
