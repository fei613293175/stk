# V1.3.0 Known Issues And Evidence Limits

- The sole GitHub Fast CI run (`31514148073`) failed before compilation because the existing validator requires an untracked legacy `offline-spec/STK_FULL_SPEC_OFFLINE.zip`. No rerun was made for V1.3. Server fast-check, PHP/contracts, Android unit tests, assembleDebug, signing inspection, and final-device tests passed.
- On the tested Redmi, the system browser did not exit within three Back presses after opening support. Returning through HOME and tapping 商推客 reopened the App at the Home tab rather than restoring the Support subpage. External launch, App availability, and normal startup passed; subpage restoration is not claimed.
- The live production support URL was available, so the “external application unavailable” fallback was verified by code/contract inspection rather than uninstalling or disabling a system browser.
- Active and expired membership render branches are implemented and backend-controlled, but the final production retest retained the owner's real inactive status instead of mutating production solely for screenshots.
- Short-lived loading, cache-clearing, and forced failure states are audited in code/tests; not every one has a direct final-device screenshot.
- The targeted cleanup command `rm -f /sdcard/stk-v130-final-*` returned success. Its follow-up wildcard listing was malformed by ADB shell argument splitting and is not valid zero-residue evidence; the phone was released immediately afterward and no post-release ADB command was sent.
- Status/navigation bars, device aspect ratio, real username/UID/phone, and production content differ from fixed visual fixtures.
- The APK is a debug Beta signed by the existing shared STK Beta certificate. It is suitable for upgrade/acceptance testing, not a production signing-key decision.
