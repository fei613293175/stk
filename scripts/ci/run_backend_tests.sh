#!/usr/bin/env bash
set -euo pipefail
for plugin in source/plugin/stk_auth source/plugin/stk_project; do
  [[ -d "$plugin" ]] || { echo "Missing Discuz plugin source: $plugin" >&2; exit 1; }
done
find source/plugin/stk_auth source/plugin/stk_project -type f -name '*.php' -print0 | xargs -0 -r -n1 php -l
if [[ -f composer.json ]]; then
  composer install --no-interaction --prefer-dist
  if [[ -x vendor/bin/phpunit ]]; then vendor/bin/phpunit; fi
fi
python scripts/verify_database_contract.py
php backend/tests/captcha_service_test.php
php backend/tests/auth_repository_test.php
php backend/tests/idempotency_contract_test.php
python backend/tests/admin_contract_test.py
if [[ -x backend/tests/run.sh ]]; then backend/tests/run.sh; fi
