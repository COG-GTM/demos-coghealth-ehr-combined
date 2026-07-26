#!/bin/bash
# =============================================================================
# session-briefing.sh — situational briefing at session start
# =============================================================================
#
# Event:   SessionStart (registered in .devin/hooks.v1.json with an empty
#          matcher — runs once whenever a new agent session begins)
#
# What it does:
#   1. Truncates .devin/hook-firings.log and writes its own firing as the
#      first entry, so the log only ever contains the current session's
#      hook activity; watch live with `tail -f`.
#   2. Gathers a one-line snapshot of the repo and environment:
#        - current git branch
#        - number of uncommitted files
#        - last commit (short hash + subject)
#        - whether the backend API is up (health check on localhost:8080)
#   3. Prints a SessionStart additionalContext JSON payload so the briefing
#      is injected into the agent's context — the agent never starts blind
#      on branch state or leftover changes, and is reminded to run
#      /run-verify before committing significant work.
#
# Input:   JSON hook payload on stdin (consumed and ignored).
# Output:  hookSpecificOutput JSON with the briefing text.
# Exit:    0 always; exits early if not inside a project directory.
# =============================================================================
cat > /dev/null  # consume stdin

# New session: start the firing log fresh (truncate, don't append)
echo "$(date '+%Y-%m-%d %H:%M:%S') [SessionStart] session-briefing fired" > "${DEVIN_PROJECT_DIR:-$PWD}/.devin/hook-firings.log"

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
