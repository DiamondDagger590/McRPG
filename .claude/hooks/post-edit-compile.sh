#!/usr/bin/env bash
# PostToolUse hook — incremental compile check after Java source file edits.
#
# Claude Code calls this script after every Edit or Write tool use.
# It receives a JSON payload via stdin with shape:
#   { "tool_name": "Edit", "tool_input": { "file_path": "...", ... }, ... }
#
# If the edited file is a .java source file the hook runs:
#   ./gradlew compileJava --quiet
# A non-zero exit feeds the compiler errors back to Claude in the same turn
# (exit code 2), so it can fix compilation failures immediately.

set -uo pipefail

input=$(cat)

# Extract tool_input.file_path from the JSON payload.
file_path=$(python3 - <<<"$input" 2>/dev/null <<'PYEOF'
import sys, json
try:
    data = json.loads(sys.stdin.read())
    tip = data.get("tool_input", {})
    if isinstance(tip, str):
        tip = json.loads(tip)
    print(tip.get("file_path", ""))
except Exception:
    print("")
PYEOF
) || file_path=""

# Only run for Java source files.
if [[ "$file_path" != *.java ]]; then
    exit 0
fi

compile_output=$(./gradlew compileJava --quiet 2>&1)
exit_code=$?

if [ $exit_code -ne 0 ]; then
    echo "compileJava failed after editing $(basename "$file_path"):"
    echo ""
    echo "$compile_output"
    exit 2  # exit 2 signals Claude Code to surface this output in the current turn
fi
