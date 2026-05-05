#!/usr/bin/env bash
# Repo-root shortcut for tools/sl-cli/bin/sl.js — works from this directory
# regardless of cwd. Forwards every argument and exit code untouched.
#
# Usage from repo root:
#   ./sl              # interactive REPL
#   ./sl start        # boot Node API + ngrok, write URL to gradle.properties
#   ./sl publish      # publish library + JS bundle + iOS XCFramework
#   ./sl status
#   ./sl stop
#
# For a real global `sl` command (like gemini / claude), see
# tools/sl-cli/README.md → "Install globally".

set -euo pipefail

# Resolve the directory this script lives in, even via symlink.
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
    DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
    SOURCE="$(readlink "$SOURCE")"
    [[ "$SOURCE" != /* ]] && SOURCE="$DIR/$SOURCE"
done
REPO_ROOT="$(cd -P "$(dirname "$SOURCE")" && pwd)"

CLI_ENTRY="$REPO_ROOT/tools/sl-cli/bin/sl.js"

if [ ! -d "$REPO_ROOT/tools/sl-cli/node_modules" ]; then
    echo "▶ sl-cli deps not installed yet. Running npm install once..." >&2
    (cd "$REPO_ROOT/tools/sl-cli" && npm install --silent)
fi

exec node "$CLI_ENTRY" "$@"
