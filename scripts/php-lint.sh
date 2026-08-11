#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if ! command -v php >/dev/null 2>&1; then
    echo "SKIP: php 未安装；GitHub Fast CI 会执行 PHP 检查。"
    exit 0
fi
while IFS= read -r -d '' file; do
    php -l "$file" >/dev/null
    echo "PHP OK: ${file#$ROOT/}"
done < <(find "$ROOT/discuz" -type f -name '*.php' -print0)
php "$ROOT/discuz/tests/dev_auth_service_test.php"
