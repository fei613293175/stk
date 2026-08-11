# V1.3.0 Automated Test Report

All builds and non-device tests ran on existing server `obx-test`; no local build/test environment, simulator, or virtual device was used.

Server workspace: `/opt/stk-build/stk-fast-v130-beta1-20260811`

Results:

- `scripts/fast-check.sh`: PASS.
- Visual/package validator: PASS, zero errors and warnings.
- PHP syntax: PASS for 75 plugin/test PHP files.
- Existing project contract test: PASS.
- V1.3 contract test: PASS.
- Android `testDebugUnitTest`: PASS.
- Android `assembleDebug`: PASS.
- Final Gradle result: `BUILD SUCCESSFUL in 1m`, 52 tasks.

The first non-interactive Gradle invocation selected server Java 8 and stopped before compilation because AGP requires Java 11+. The only correction was selecting the server's already-installed Java 17; no environment was installed or upgraded.

Package verification:

- APK SHA-256: `a3cd2fffc6e3f626b1787ba2b4f1b2afca75ea43f9bfdb0c89700a15757a0d08`
- APK Signature Scheme v2: verified.
- Certificate SHA-256: `8d82da03e9133c1d60087e735b2abc9d9d2ecbb0f1815164297079d8eba537ee`
- Package/version: `com.zzyihao.stk`, `10301`, `1.3.0-beta.1`.

## GitHub Fast CI

The single V1.3 Fast CI run was created for the same commit and failed before compilation in `Fast static checks`:

- Run: `31514148073`
- Commit: `8269015fc576a9b7a132af62836e0130100b043b`
- URL: `https://github.com/fei613293175/stk/actions/runs/31514148073`
- Cause: existing `validate-kit.py` requires untracked legacy file `offline-spec/STK_FULL_SPEC_OFFLINE.zip`.

No second V1.3 CI run was started. The failure does not supersede the passing server PHP, contract, Android unit-test, build, package, and real-device evidence, but GitHub CI itself is not reported as passed.
