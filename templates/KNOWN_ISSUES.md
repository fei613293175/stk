# V1.4.0-beta.1 Known Issues

- `stk-api` and `stk-admin` resolve but still have self-signed TLS certificates.
- `stk-static` and `stk-download` are not yet present in DNS; the valid main domain serves the Beta APK and App Links.
- The shared repository Beta signing key is not a long-term production signing decision.
- No API 36 real phone is connected; API 37/emulator coverage is not a release gate.
- Production previously contained a newer V1.5.4 record. Existing V1.5 installs are not automatically downgraded.

See the full delivery `KNOWN_ISSUES.md`, `DNS_ACTION_REQUIRED.md`, and device report for evidence limits.
