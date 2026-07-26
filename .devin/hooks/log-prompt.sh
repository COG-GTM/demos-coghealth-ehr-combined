#!/bin/bash
# =============================================================================
# log-prompt.sh — record each user prompt in the hook firing log
# =============================================================================
#
# Event:   UserPromptSubmit (registered in .devin/hooks.v1.json with an empty
#          matcher — runs every time the user submits a message)
#
# What it does:
#   1. Reads the hook payload from stdin and extracts the `prompt` field.
#   2. Appends a timestamped entry with the prompt text (truncated to 500
#      chars) to .devin/hook-firings.log, so the session log shows what
#      was asked alongside which hooks fired; watch live with `tail -f`.
#
#   Note: the SessionStart event has no access to the prompt (its payload
#   only contains `source`), which is why prompt logging lives here.
#
# Input:   JSON hook payload on stdin ({"prompt": "..."}).
# Output:  Nothing (logging only; never injects context).
# Exit:    Always 0 (never blocks the prompt).
# =============================================================================
export HOOK_PAYLOAD="$(cat)"

_prompt=$(python3 -c 'import json,os; p=json.loads(os.environ.get("HOOK_PAYLOAD","{}")).get("prompt",""); print(" ".join(p.split())[:500])' 2>/dev/null)
echo "$(date '+%Y-%m-%d %H:%M:%S') [UserPromptSubmit] prompt: ${_prompt:-<empty>}" >> "${DEVIN_PROJECT_DIR:-$PWD}/.devin/hook-firings.log"
exit 0
