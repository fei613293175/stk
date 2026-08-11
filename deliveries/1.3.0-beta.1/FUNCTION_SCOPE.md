# V1.3.0 Function Scope

Version: `1.3.0-beta.1` (`10301`)

Implemented scope:

- B30: My page, profile summary, dual-account summary, member card, common entries, loading/error/offline states.
- B31: Read-only profile, inactive/active/expired member states, member dates, commission/task balances, stage notices.
- B32: Backend-driven prop catalog and explicit placeholders for history, favorites, and real-name verification.
- B33: Support display/copy/safe external action, settings, cache clear, agreements, privacy, about, and logout confirmation.
- Backend: five authenticated V1.3 APIs, idempotent member/wallet defaults, prop display catalog, and support policy.
- Discuz: My settings, membership management, read-only wallet management, prop settings, and support settings with audit logging.

Explicitly excluded:

- Member payment, orders, auto-renewal, real discount settlement, or commission settlement.
- Withdrawals, transfers, balance mutations, or transaction history.
- Prop purchase, inventory, use, or ranking effects.
- Actual history, favorites, or real-name verification business flows.
- Arbitrary backend-supplied URI execution.
