#!/usr/bin/env bash
set -euo pipefail
mkdir -p android-app/.ci
if [[ "${STK_SIGNING_MODE:-official}" == "ci-test" ]]; then
  test_store_password='ci-test-store-password'
  test_key_password="$test_store_password"
  test_key_alias='stk-ci-test'
  keytool -genkeypair -noprompt -keystore android-app/.ci/stk-release.jks \
    -storepass "$test_store_password" -keypass "$test_key_password" \
    -alias "$test_key_alias" -keyalg RSA -keysize 2048 -validity 2 \
    -dname 'CN=STK CI Test,O=STK,C=CN'
  STK_RELEASE_STORE_PASSWORD="$test_store_password"
  STK_RELEASE_KEY_ALIAS="$test_key_alias"
  STK_RELEASE_KEY_PASSWORD="$test_key_password"
else
  : "${STK_RELEASE_KEYSTORE_BASE64:?GitHub Secret STK_RELEASE_KEYSTORE_BASE64 is required}"
  : "${STK_RELEASE_STORE_PASSWORD:?GitHub Secret STK_RELEASE_STORE_PASSWORD is required}"
  : "${STK_RELEASE_KEY_ALIAS:?GitHub Secret STK_RELEASE_KEY_ALIAS is required}"
  : "${STK_RELEASE_KEY_PASSWORD:?GitHub Secret STK_RELEASE_KEY_PASSWORD is required}"
  printf '%s' "$STK_RELEASE_KEYSTORE_BASE64" | base64 --decode > android-app/.ci/stk-release.jks
fi
chmod 600 android-app/.ci/stk-release.jks
if [[ -n "${GITHUB_ENV:-}" ]]; then
  {
    echo "STK_RELEASE_KEYSTORE_PATH=${GITHUB_WORKSPACE}/android-app/.ci/stk-release.jks"
    echo "STK_RELEASE_STORE_PASSWORD=$STK_RELEASE_STORE_PASSWORD"
    echo "STK_RELEASE_KEY_ALIAS=$STK_RELEASE_KEY_ALIAS"
    echo "STK_RELEASE_KEY_PASSWORD=$STK_RELEASE_KEY_PASSWORD"
  } >> "$GITHUB_ENV"
fi
