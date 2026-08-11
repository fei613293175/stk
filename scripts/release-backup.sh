#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STAMP="$(date +%Y%m%d%H%M%S)"
DEST="$ROOT/release-backups/backup-$STAMP"
mkdir -p "$DEST"
cp -a "$ROOT/release/." "$DEST/"
printf 'BACKUP_CREATED %s\n' "$DEST"
