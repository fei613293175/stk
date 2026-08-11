#!/usr/bin/env bash
set -euo pipefail
REPO="${1:-.}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACK_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO="$(cd "$REPO" && pwd)"
TARGET="$REPO/ui-reference/detailed/V1.0.0"
CONTRACT_TARGET="$REPO/ui-reference/detailed/contracts"
mkdir -p "$TARGET" "$CONTRACT_TARGET"
cp "$PACK_ROOT"/visual/*.png "$TARGET"/
cp "$PACK_ROOT"/contracts/*.csv "$CONTRACT_TARGET"/
cp "$PACK_ROOT/VISUAL_BATCHES.csv" "$REPO/VISUAL_BATCHES.csv"
cp "$PACK_ROOT/VISUAL_IMPLEMENTATION_STATUS.csv" "$REPO/VISUAL_IMPLEMENTATION_STATUS.csv"
cp "$PACK_ROOT/CURRENT_VISUAL_TASK.md" "$REPO/CURRENT_VISUAL_TASK.md"
cp "$PACK_ROOT/PAGE_VISUAL_CHECKLIST.md" "$REPO/PAGE_VISUAL_CHECKLIST.md"
COUNT="$(find "$TARGET" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')"
if [[ "$COUNT" != "67" ]]; then
  echo "Expected 67 PNG files, found $COUNT" >&2
  exit 1
fi
echo "Installed 67 V1.0.0 detailed visual references into $TARGET"
echo "No business code, AGENTS.md, CI workflow or build configuration was overwritten."
