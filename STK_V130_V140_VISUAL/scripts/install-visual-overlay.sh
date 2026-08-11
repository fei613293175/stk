#!/usr/bin/env bash
set -euo pipefail
REPO_PATH="${1:-.}"
PACKAGE_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MAP="$PACKAGE_ROOT/contracts/INSTALL_FILE_MAP.csv"
mkdir -p "$REPO_PATH/ui-reference/detailed/V1.3.0" "$REPO_PATH/ui-reference/detailed/V1.4.0"
# Remove only prior PNGs in these two visual-overlay directories, not application code.
find "$REPO_PATH/ui-reference/detailed/V1.3.0" -maxdepth 1 -type f -name '*.png' -delete
find "$REPO_PATH/ui-reference/detailed/V1.4.0" -maxdepth 1 -type f -name '*.png' -delete
while IFS=',' read -r state_id version file_name; do
  state_id="${state_id//$'\r'/}"
  version="${version//$'\r'/}"
  file_name="${file_name//$'\r'/}"
  [[ "$state_id" == "state_id" || -z "$state_id" ]] && continue
  src="$PACKAGE_ROOT/visual/$file_name"
  dst="$REPO_PATH/ui-reference/detailed/$version/$file_name"
  [[ -f "$src" ]] || { echo "Missing visual file: $src" >&2; exit 1; }
  cp -f "$src" "$dst"
done < <(sed '1s/^\xEF\xBB\xBF//' "$MAP")
v13=$(find "$REPO_PATH/ui-reference/detailed/V1.3.0" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')
v14=$(find "$REPO_PATH/ui-reference/detailed/V1.4.0" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')
[[ "$v13" == "37" && "$v14" == "23" ]] || { echo "Unexpected visual counts: V1.3.0=$v13, V1.4.0=$v14" >&2; exit 1; }
echo "Visual overlay installed: V1.3.0=$v13, V1.4.0=$v14"
