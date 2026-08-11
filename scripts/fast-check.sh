#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
python3 "$ROOT/scripts/validate-kit.py"
"$ROOT/scripts/php-lint.sh"
php "$ROOT/discuz/tests/project_contract_test.php"
for script in "$ROOT"/scripts/*.sh "$ROOT/gradlew"; do
    bash -n "$script"
done
php "$ROOT/discuz/tests/v130_contract_test.php"
php "$ROOT/discuz/tests/v140_contract_test.php"
echo "PASS: STK V1.4.0 fast static checks"
