# V1.3.0 Admin And Configuration Completion

Discuz admin modules completed:

- My page settings.
- Membership status management: open, renew, set expiry, expire, and disable.
- Wallet query/display management; no balance mutation endpoint.
- Prop catalog visibility, copy, order, and icon settings.
- Support, agreement, privacy, copy, and safe URL allowlist settings.

The 14 required contract settings are implemented and connected to API output. Additional safety/display fields include support label, hours, URL allowlist, profile bio, and schema version.

Production support configuration:

- Type: `url`
- Label: `在线客服`
- Value: `https://stk.zz-yihao.com/`
- URL allowlist: `stk.zz-yihao.com`
- Copy enabled: yes

Admin mutations call the audit logger. Production audit entry `audit_id=12`, section `support`, action `deployment_config_update` confirms the deployment-time configuration write.
