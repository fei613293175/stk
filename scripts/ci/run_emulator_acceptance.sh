#!/usr/bin/env bash
set -euo pipefail
release_id="${STK_RELEASE_ID:?STK_RELEASE_ID is required}"
mkdir -p artifacts/emulator/screenshots artifacts/emulator/logs
adb wait-for-device
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell wm size 1170x2532
adb shell wm density 480
adb shell settings put system font_scale 1.0

current_apk="${STK_CURRENT_APK:-}"
if [[ -z "$current_apk" ]]; then
  current_apk=$(find android-app/app/build/outputs/apk/release -maxdepth 1 -type f -name '*.apk' | head -n1)
fi
[[ -f "$current_apk" ]] || { echo "Current release APK not found" >&2; exit 1; }
adb install -r "$current_apk"

test_apk="${STK_TEST_APK:-}"
if [[ -z "$test_apk" ]]; then
  test_apk=$(find android-app/app/build/outputs/apk/androidTest/release artifacts/exact -type f -name '*androidTest*.apk' 2>/dev/null | head -n1 || true)
fi
[[ -f "$test_apk" ]] || { echo "Release instrumentation APK not found" >&2; exit 1; }
adb install -r "$test_apk"
runner=$(adb shell pm list instrumentation | tr -d '\r' | grep 'target=com.zzyihao.stk' | head -n1 | sed -E 's/^instrumentation:([^ ]+).*/\1/')
[[ -n "$runner" ]] || { echo "Instrumentation runner for com.zzyihao.stk not found" >&2; exit 1; }
set +e
adb shell am instrument -w -r -e stkReleaseId "$release_id" "$runner" | tee artifacts/emulator/logs/instrumentation.txt
status=${PIPESTATUS[0]}
set -e
adb logcat -d > artifacts/emulator/logs/logcat.txt || true
adb shell dumpsys package com.zzyihao.stk > artifacts/emulator/logs/package.txt || true
adb pull /sdcard/Android/data/com.zzyihao.stk/files/stk-screenshots artifacts/emulator/ >/dev/null 2>&1 || true
adb pull /sdcard/Android/data/com.zzyihao.stk/files/interaction-results.json artifacts/emulator/interaction-results.json >/dev/null 2>&1 || true
if [[ -d artifacts/emulator/stk-screenshots ]]; then
  find artifacts/emulator/stk-screenshots -type f -name '*.png' -exec cp {} artifacts/emulator/screenshots/ \;
fi
[[ $status -eq 0 ]] || exit $status
[[ -f artifacts/emulator/interaction-results.json ]] || { echo "Instrumentation did not produce interaction-results.json" >&2; exit 1; }
python scripts/verify_interaction_evidence.py --release "$release_id" --evidence artifacts/emulator/interaction-results.json
