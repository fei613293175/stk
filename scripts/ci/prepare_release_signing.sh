#!/usr/bin/env bash
set -euo pipefail
: "${STK_RELEASE_KEYSTORE_BASE64:?GitHub Secret STK_RELEASE_KEYSTORE_BASE64 is required}"
: "${STK_RELEASE_STORE_PASSWORD:?GitHub Secret STK_RELEASE_STORE_PASSWORD is required}"
: "${STK_RELEASE_KEY_ALIAS:?GitHub Secret STK_RELEASE_KEY_ALIAS is required}"
: "${STK_RELEASE_KEY_PASSWORD:?GitHub Secret STK_RELEASE_KEY_PASSWORD is required}"
mkdir -p android-app/.ci
printf '%s' "$STK_RELEASE_KEYSTORE_BASE64" | base64 --decode > android-app/.ci/stk-release.jks
chmod 600 android-app/.ci/stk-release.jks
if [[ -n "${GITHUB_ENV:-}" ]]; then
  {
    echo "STK_RELEASE_KEYSTORE_PATH=${GITHUB_WORKSPACE}/android-app/.ci/stk-release.jks"
    echo "STK_RELEASE_STORE_PASSWORD=$STK_RELEASE_STORE_PASSWORD"
    echo "STK_RELEASE_KEY_ALIAS=$STK_RELEASE_KEY_ALIAS"
    echo "STK_RELEASE_KEY_PASSWORD=$STK_RELEASE_KEY_PASSWORD"
  } >> "$GITHUB_ENV"
fi
