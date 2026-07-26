#!/bin/bash
# =============================================================================
# lint-feedback.sh — instant eslint feedback after frontend edits
# =============================================================================
#
# Event:   PostToolUse (registered in .devin/hooks.v1.json with matcher
#          ^(edit|write)$ — runs after every successful file edit or write)
#
# What it does:
#   1. Logs the firing (timestamp + edited file) to .devin/hook-firings.log
#      so hook activity is visible; watch live with `tail -f`.
#   2. Exits silently unless the edited file is a .ts/.tsx file inside
#      demos-coghealth-ehr-web and the tool call succeeded.
#   3. Runs the project's local eslint (node_modules/.bin/eslint) on just
#      that one file, with a 20s timeout.
#   4. If eslint reports problems, prints a PostToolUse additionalContext
#      JSON payload so the lint errors are injected straight into the
#      agent's context — the agent fixes them immediately instead of
#      discovering them at the final `npm run build`.
#
# Input:   JSON hook payload on stdin (tool_name, tool_input, tool_response).
# Output:  Nothing on success/skip; hookSpecificOutput JSON when lint fails.
# Exit:    Always 0 (never blocks the edit).
# =============================================================================
export HOOK_PAYLOAD="$(cat)"

_edited_file=$(python3 -c 'import json,os; print(json.loads(os.environ.get("HOOK_PAYLOAD","{}")).get("tool_input",{}).get("file_path",""))' 2>/dev/null)
echo "$(date '+%Y-%m-%d %H:%M:%S') [PostToolUse] lint-feedback fired (file: ${_edited_file:-unknown})" >> "${DEVIN_PROJECT_DIR:-$PWD}/.devin/hook-firings.log"

python3 - <<'PY'
import json, os, re, subprocess, sys

data = json.loads(os.environ.get("HOOK_PAYLOAD", "{}"))
path = data.get("tool_input", {}).get("file_path", "")

if not data.get("tool_response", {}).get("success", True):
    sys.exit(0)
if "demos-coghealth-ehr-web" not in path or not re.search(r"\.tsx?$", path):
    sys.exit(0)

web_root = os.path.join(os.environ.get("DEVIN_PROJECT_DIR", "."), "demos-coghealth-ehr-web")
eslint = os.path.join(web_root, "node_modules", ".bin", "eslint")
if not os.path.exists(eslint):
    sys.exit(0)

try:
    result = subprocess.run(
        [eslint, "--no-color", path],
        cwd=web_root, capture_output=True, text=True, timeout=20,
    )
except subprocess.TimeoutExpired:
    sys.exit(0)

output = (result.stdout or "").strip()
if result.returncode != 0 and output:
    print(json.dumps({
        "hookSpecificOutput": {
            "hookEventName": "PostToolUse",
            "additionalContext": (
                f"[lint-feedback hook] eslint found problems in the file you just edited "
                f"— fix them now rather than at build time:\n{output[:2000]}"
            ),
        }
    }))
PY
