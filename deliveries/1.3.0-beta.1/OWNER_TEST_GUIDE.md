# V1.3.0 Owner Test Guide

1. Install `STK-1.3.0-beta.1.apk` over the prior Beta without uninstalling it.
2. Confirm the package reports `1.3.0-beta.1 (10301)` and the existing login remains.
3. Open My and verify the real profile, commission/task balances, member card, and common entries.
4. Confirm member chips show `消费 5 折` and `消费返佣 40%`; tap the card and both open buttons to read the stage notice.
5. Open both accounts and verify balances are read-only; tap an account explanation and confirm no withdrawal/transfer success is shown.
6. Open Props, tap a prop, and confirm there is no price, inventory, purchase, or use operation.
7. Open history, favorites, and real-name verification; confirm each is a complete placeholder with working back navigation.
8. Open Support, copy the configured value, then open it only after the confirmation dialog.
9. Open Settings; clear cache, scroll the agreement/privacy pages, open About, then cancel logout.
10. In Discuz admin, change one non-sensitive display setting and verify the API/App output changes and an audit record is created.

Before sideloading, verify SHA-256 equals `a3cd2fffc6e3f626b1787ba2b4f1b2afca75ea43f9bfdb0c89700a15757a0d08`.
