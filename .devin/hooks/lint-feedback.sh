#!/bin/bash
# PostToolUse (edit|write): instant feedback loop. After the agent edits a
# frontend TypeScript file, run eslint on just that file and feed any problems
# straight back into the agent's context — it fixes them immediately instead
# of discovering them at the final `npm run build`.
export HOOK_PAYLOAD="$(cat)"

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
