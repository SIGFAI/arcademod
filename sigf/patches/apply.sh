#!/usr/bin/env bash
# Apply the SIGF patch set to a clean Bay4lly/ArcadeMod checkout at the pinned commit.
#   apply.sh <upstream_checkout> <generated_assets_dir>
# <generated_assets_dir> is the output of library/arcademod/assets/gen_assets.py (a src/main/resources overlay).
set -euo pipefail
UP="$1"; GEN="$2"; HERE="$(cd "$(dirname "$0")" && pwd)"
PIN=eb09571cf6883651f7516da85682b5e9c9c6b166
[ "$(git -C "$UP" rev-parse HEAD)" = "$PIN" ] || { echo "upstream is not at $PIN" >&2; exit 1; }
git -C "$UP" apply --whitespace=nowarn "$HERE/0001-sigf-original-names-and-license.patch"
grep -v '^#' "$HERE/delete.txt" | sed '/^$/d' | while read -r f; do rm -f "$UP/$f"; done
cp -R "$GEN/." "$UP/src/main/resources/"
echo "patched $UP"
