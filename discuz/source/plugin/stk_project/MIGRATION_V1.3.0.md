# V1.3.0 database migration and rollback

The `install.php` and `upgrade.php` scripts apply schema version `13001` idempotently.

Migration behavior:

- Creates `pre_stk_member_status`, `pre_stk_wallet_account`, and `pre_stk_prop_catalog` when missing.
- Adds `starts_at` and `task_amount` when upgrading an existing installation.
- Inserts missing membership and wallet rows for existing `pre_common_member` users without overwriting existing values.
- Seeds the four display-only prop catalog records and V1.3 configuration defaults.

Rollback procedure:

1. Take a database backup before running the Discuz plugin upgrade.
2. Disable the `stk_project` plugin if the upgrade cannot complete.
3. Restore the database backup and the previous plugin directory together.
4. Do not drop the three V1.3 tables during an application rollback. They are additive and retaining them avoids loss of manually maintained membership state.

No migration performs a destructive data rewrite. Runtime reads also use `INSERT IGNORE` to complete missing historical-user rows safely.
