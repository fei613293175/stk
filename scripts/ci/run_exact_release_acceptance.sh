#!/usr/bin/env bash
set -euo pipefail
release_id="${STK_RELEASE_ID:?}"
current_apk=$(find artifacts/exact -type f -name '商推客-*-release.apk' | head -n1)
test_apk=$(find artifacts/exact -type f -name '*androidTest*.apk' | head -n1)
[[ -f "$current_apk" && -f "$test_apk" ]] || { echo "Exact release or test APK missing" >&2; exit 1; }
mkdir -p artifacts/emulator/logs
adb wait-for-device
adb uninstall com.zzyihao.stk >/dev/null 2>&1 || true

if [[ "$release_id" != "V1.0.0" ]]; then
  previous_apk=$(find artifacts/previous -type f -name '*.apk' | head -n1)
  [[ -f "$previous_apk" ]] || { echo "Previous accepted APK missing" >&2; exit 1; }
  adb install "$previous_apk"
  adb install -r "$test_apk"
  runner=$(adb shell pm list instrumentation | tr -d '\r' | grep 'target=com.zzyihao.stk' | head -n1 | sed -E 's/^instrumentation:([^ ]+).*/\1/')
  adb shell am instrument -w -r -e class com.zzyihao.stk.release.UpgradeSeedTest -e stkReleaseId "$release_id" "$runner" | tee artifacts/emulator/logs/upgrade-seed.txt
  adb install -r "$current_apk" | tee artifacts/emulator/logs/upgrade-install.txt
else
  adb install "$current_apk" | tee artifacts/emulator/logs/fresh-install.txt
fi
export STK_CURRENT_APK="$current_apk"
export STK_TEST_APK="$test_apk"
bash scripts/ci/run_emulator_acceptance.sh
