# V1.4.0-beta.1 Owner Test Guide

1. Verify package `com.zzyihao.stk`, version `10401 / 1.4.0-beta.1`, SHA-256 and signing certificate.
2. Cover-install over V1.3.0 without clearing app data.
3. Test up-to-date, optional update, forced update and maintenance startup gates.
4. Test download byte progress, cancellation, network failure, retry, SHA failure and ready-to-install states.
5. Verify unknown-source authorization and Android system installer handoff; no silent installation is allowed.
6. Check About-page update states and `/project/{id}` plus `/update` App Links.
7. Inspect crash, ANR and abnormal logs and compare representative device screenshots with V1.4 references.
8. Return the phone to HOME, restore changed settings/input method and release the FIFO ticket/lock.
