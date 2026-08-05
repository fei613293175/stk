#!/usr/bin/env bash
set -euo pipefail
release_id="${STK_RELEASE_ID:?}"
current_apk=$(find artifacts/exact -type f -name '商推客-*-release.apk' | head -n1)
test_apk=$(find artifacts/exact -type f -name '*androidTest*.apk' | head -n1)
[[ -f "$current_apk" && -f "$test_apk" ]] || { echo "Exact release or test APK missing" >&2; exit 1; }
mkdir -p artifacts/emulator/logs
adb wait-for-device
adb uninstall com.zzyihao.stk >/dev/null 2>&1 || true

install_with_retry() {
  apk=$1
  shift
  for attempt in 1 2 3; do
    if adb shell service check package 2>/dev/null | grep -q 'found' && adb install --no-streaming -r "$@" "$apk"; then
      return 0
    fi
    echo "Release acceptance install attempt $attempt failed; reconnecting ADB" >&2
    adb reconnect >/dev/null 2>&1 || true
    adb wait-for-device
    sleep $((attempt * 2))
  done
  return 1
}

if [[ "$release_id" != "V1.0.0" ]]; then
  previous_apk=$(find artifacts/previous -type f -name '*.apk' | head -n1)
  [[ -f "$previous_apk" ]] || { echo "Previous accepted APK missing" >&2; exit 1; }
  install_with_retry "$previous_apk"
  install_with_retry "$test_apk" -t
  runner=$(adb shell pm list instrumentation | tr -d '\r' | grep 'target=com.zzyihao.stk' | head -n1 | sed -E 's/^instrumentation:([^ ]+).*/\1/')
  adb shell am instrument -w -r -e class com.zzyihao.stk.release.UpgradeSeedTest -e stkReleaseId "$release_id" "$runner" | tee artifacts/emulator/logs/upgrade-seed.txt
  install_with_retry "$current_apk" | tee artifacts/emulator/logs/upgrade-install.txt
else
  install_with_retry "$current_apk" | tee artifacts/emulator/logs/fresh-install.txt
fi
export STK_CURRENT_APK="$current_apk"
export STK_TEST_APK="$test_apk"
bash scripts/ci/run_emulator_acceptance.sh
