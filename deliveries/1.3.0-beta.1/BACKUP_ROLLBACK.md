# V1.3.0 Backup And Rollback

Production backup: `/opt/stk-build/backups/stk-v130-predeploy-20260811224158`

- Database dump size: 1,610,214 bytes
- Database dump SHA-256: `7333c204b987b586d43b4ad60b4647b0824f8aff85e9b20625af6cb007b62fb4`
- Both production plugin directories are included.
- Server backup retention remains exactly five directories.

Rollback order:

1. Put the site in a controlled maintenance window.
2. Restore both plugin directories from the backup while preserving production `www:www`, directory `700`, and file `600` ownership/modes.
3. Restore the database dump from the same backup point.
4. Run PHP syntax checks with `/www/server/php/80/bin/php`, then check health and authenticated V1.2 routes.
5. Remove maintenance mode only after API and Android smoke checks succeed.

Do not copy source directories over production without restoring destination ownership and modes; an earlier `cp -a` changed them to `root:root` and blocked Web PHP until corrected.
