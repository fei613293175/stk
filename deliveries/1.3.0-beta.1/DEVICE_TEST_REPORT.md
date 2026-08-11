# V1.3.0 Final Device Test Report

- Device: Redmi `24094RAD4C`
- Serial: `CMZDEENVAESS89UC`
- Android: 15 / API 35
- APK: `STK-1.3.0-beta.1.apk`
- SHA-256: `a3cd2fffc6e3f626b1787ba2b4f1b2afca75ea43f9bfdb0c89700a15757a0d08`
- Cover install: PASS (`adb install -r -g` returned `Success`)
- Installed version: `10301 / 1.3.0-beta.1`
- Login state preserved across install: yes
- Crash/ANR log matches: 0

Exercised paths:

- Home to My; profile; member and member-stage notice; wallets and account-stage notice.
- Props and prop-stage notice; My Published; history, favorites, and real-name placeholders.
- Support display, copy success, external confirmation, allowlisted HTTPS browser launch.
- Settings, cache clear, user agreement, privacy, about, logout confirmation cancel.
- Real taps, scrolling, back navigation, system browser handoff, and App relaunch.

Device release:

- Original/final IME: `com.sohu.inputmethod.sogou/.SogouIME`.
- Phone returned to HOME.
- Final recorded state before release: `device`.
- FIFO ticket renamed `.ticket.done`; active lock moved to `active.lock.released-stk-v130-final-20260812-002017`.
- No ADB command was sent after release.
