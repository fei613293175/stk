#!/usr/bin/env bash
set -euo pipefail
mkdir -p artifacts/previous
release_id="${STK_RELEASE_ID:?}"
tag="${PREVIOUS_TAG:-${STK_PREVIOUS_TAG_DEFAULT:-}}"
if [[ "$release_id" == "V1.0.0" || -z "$tag" ]]; then
  echo "Fresh-install release: no previous APK required"
  exit 0
fi
command -v gh >/dev/null || { echo "GitHub CLI gh is required" >&2; exit 1; }
gh release download "$tag" --repo "${GITHUB_REPOSITORY:-fei613293175/stk}" --pattern '*.apk' --dir artifacts/previous --clobber
count=$(find artifacts/previous -maxdepth 1 -type f -name '*.apk' | wc -l | tr -d ' ')
[[ "$count" == "1" ]] || { echo "Expected exactly one previous accepted APK, got $count" >&2; exit 1; }
