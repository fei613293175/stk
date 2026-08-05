#!/usr/bin/env bash
set -euo pipefail
release_id="${STK_RELEASE_ID:?STK_RELEASE_ID is required}"
mkdir -p artifacts/emulator/screenshots artifacts/emulator/logs

capture_failure_diagnostics() {
  adb logcat -d > artifacts/emulator/logs/logcat-install-failure.txt 2>/dev/null || true
  adb shell dumpsys package > artifacts/emulator/logs/package-manager-install-failure.txt 2>/dev/null || true
}
on_exit() {
  status=$?
  trap - EXIT
  if [[ $status -ne 0 ]]; then capture_failure_diagnostics; fi
  exit "$status"
}
trap on_exit EXIT

adb wait-for-device
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell wm size 1170x2532
adb shell wm density 480
adb shell settings put system font_scale 1.0
adb shell df -h /data | tee artifacts/emulator/logs/data-partition-before-install.txt

wait_for_package_manager() {
  for attempt in $(seq 1 30); do
    if adb shell service check package 2>/dev/null | grep -q 'found' \
      && adb shell pm path android >/dev/null 2>&1 \
      && adb shell sm list-volumes all 2>/dev/null | grep -q 'mounted'; then
      return 0
    fi
    sleep 2
  done
  echo "Android package manager did not become ready" >&2
  return 1
}

install_apk() {
  apk=$1
  shift
  for attempt in 1 2 3; do
    wait_for_package_manager
    if adb install --no-streaming -r "$@" "$apk"; then
      return 0
    fi
    echo "APK install attempt $attempt failed; reconnecting ADB before retry" >&2
    adb reconnect >/dev/null 2>&1 || true
    adb wait-for-device
    sleep $((attempt * 2))
  done
  echo "APK install failed after three attempts: $apk" >&2
  return 1
}

current_apk="${STK_CURRENT_APK:-}"
if [[ -z "$current_apk" ]]; then
  current_apk=$(find android-app/app/build/outputs/apk/release artifacts/exact -type f -name '*.apk' ! -name '*androidTest*' 2>/dev/null | head -n1 || true)
fi
[[ -f "$current_apk" ]] || { echo "Current release APK not found" >&2; exit 1; }
install_apk "$current_apk"

test_apk="${STK_TEST_APK:-}"
if [[ -z "$test_apk" ]]; then
  test_apk=$(find android-app/app/build/outputs/apk/androidTest/release artifacts/exact -type f -name '*androidTest*.apk' 2>/dev/null | head -n1 || true)
fi
[[ -f "$test_apk" ]] || { echo "Release instrumentation APK not found" >&2; exit 1; }
install_apk "$test_apk" -t
runner=$(adb shell pm list instrumentation | tr -d '\r' | grep 'target=com.zzyihao.stk' | head -n1 | sed -E 's/^instrumentation:([^ ]+).*/\1/')
[[ -n "$runner" ]] || { echo "Instrumentation runner for com.zzyihao.stk not found" >&2; exit 1; }
adb logcat -c
set +e
adb shell am instrument -w -r -e stkReleaseId "$release_id" "$runner" | tee artifacts/emulator/logs/instrumentation.txt
status=${PIPESTATUS[0]}
set -e
adb logcat -d > artifacts/emulator/logs/logcat.txt || true
adb shell dumpsys package com.zzyihao.stk > artifacts/emulator/logs/package.txt || true
adb pull /sdcard/Android/data/com.zzyihao.stk/files/stk-screenshots artifacts/emulator/ >/dev/null 2>&1 || true
adb pull /sdcard/Android/data/com.zzyihao.stk/files/interaction-results.json artifacts/emulator/interaction-results.json >/dev/null 2>&1 || true
if [[ ! -s artifacts/emulator/interaction-results.json ]]; then
  adb logcat -d -s STK_INTERACTION_RESULTS:I '*:S' \
    | sed -n 's/^.*STK_INTERACTION_RESULTS: //p' \
    | tail -n1 > artifacts/emulator/interaction-results.json
fi
if [[ -d artifacts/emulator/stk-screenshots ]]; then
  find artifacts/emulator/stk-screenshots -type f -name '*.png' -exec cp {} artifacts/emulator/screenshots/ \;
fi
[[ $status -eq 0 ]] || exit $status
[[ -f artifacts/emulator/interaction-results.json ]] || { echo "Instrumentation did not produce interaction-results.json" >&2; exit 1; }
python scripts/verify_interaction_evidence.py --release "$release_id" --evidence artifacts/emulator/interaction-results.json
