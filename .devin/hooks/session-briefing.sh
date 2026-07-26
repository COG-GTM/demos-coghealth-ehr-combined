#!/bin/bash
# SessionStart: hand the agent a one-line situational briefing so it never
# starts blind on branch state or leftover changes.
cat > /dev/null  # consume stdin

cd "${DEVIN_PROJECT_DIR:-$PWD}" || exit 0
branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
dirty=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')
last=$(git log -1 --format='%h %s' 2>/dev/null || echo "none")
api_up="down"; curl -sf -o /dev/null -m 1 "http://localhost:8080/api/actuator/health" 2>/dev/null && api_up="up"

python3 - "$branch" "$dirty" "$last" "$api_up" <<'PY'
import json, sys
branch, dirty, last, api_up = sys.argv[1:5]
print(json.dumps({
    "hookSpecificOutput": {
        "hookEventName": "SessionStart",
        "additionalContext": (
            f"[session-briefing hook] branch={branch}, uncommitted files={dirty}, "
            f"last commit=\"{last}\", backend API is {api_up}. "
            "Before committing significant work, run /run-verify."
        ),
    }
}))
PY
