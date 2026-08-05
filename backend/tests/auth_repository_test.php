<?php
declare(strict_types=1);

define('IN_DISCUZ', true);

final class DB {
    public static array $queries = [];
    public static function table(string $name): string { return 'tenant42_' . $name; }
    public static function query(string $sql): void { self::$queries[] = $sql; }
}

require_once __DIR__ . '/../../source/plugin/stk_auth/service/AuthRepository.php';

AuthRepository::install();
if (count(DB::$queries) !== 15) {
    throw new RuntimeException('Expected 15 V1.0.0 CREATE TABLE statements, received ' . count(DB::$queries));
}
foreach (DB::$queries as $query) {
    if (str_contains($query, 'pre_') || !str_contains($query, 'CREATE TABLE IF NOT EXISTS tenant42_')) {
        throw new RuntimeException('Migration did not apply the active Discuz table prefix');
    }
}

echo "auth_repository_test: PASS\n";
