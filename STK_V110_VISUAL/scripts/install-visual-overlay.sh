#!/usr/bin/env bash
set -euo pipefail

REPO="${1:-.}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACK_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO="$(cd "$REPO" && pwd)"
TARGET="$REPO/ui-reference/detailed/V1.1.0"
CONTRACT_TARGET="$REPO/ui-reference/detailed/contracts/V1.1.0"

mkdir -p "$TARGET" "$CONTRACT_TARGET"
cp "$PACK_ROOT"/visual/*.png "$TARGET"/
cp "$PACK_ROOT"/contracts/*.csv "$CONTRACT_TARGET"/
cp "$PACK_ROOT/VISUAL_BATCHES_V1.1.0.csv" "$REPO/VISUAL_BATCHES_V1.1.0.csv"
cp "$PACK_ROOT/VISUAL_IMPLEMENTATION_STATUS_V1.1.0.csv" "$REPO/VISUAL_IMPLEMENTATION_STATUS_V1.1.0.csv"
cp "$PACK_ROOT/CURRENT_VISUAL_TASK_V1.1.0.md" "$REPO/CURRENT_VISUAL_TASK_V1.1.0.md"
cp "$PACK_ROOT/PAGE_VISUAL_CHECKLIST_V1.1.0.md" "$REPO/PAGE_VISUAL_CHECKLIST_V1.1.0.md"

COUNT="$(find "$TARGET" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')"
if [[ "$COUNT" != "45" ]]; then
  echo "Expected 45 PNG files, found $COUNT" >&2
  exit 1
fi

echo "Installed 45 V1.1.0 detailed visual references into $TARGET"
echo "Version-specific task files were installed without overwriting V1.0.0 records."
echo "No business code, AGENTS.md, CI workflow or build configuration was overwritten."
