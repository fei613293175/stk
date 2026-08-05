<?php
declare(strict_types=1);

define('IN_DISCUZ', true);

final class DB {
    public static array $rows = [];
    public static int $nextId = 1;
    public static function table(string $name): string { return 'tenant42_' . $name; }
    public static function fetch_first(string $sql, array $arguments = []): ?array {
        $operation = (string)($arguments[0] ?? '');
        $hash = (string)($arguments[1] ?? '');
        foreach (self::$rows as $row) {
            if ($row['operation_id'] === $operation && $row['idempotency_key_hash'] === $hash) return $row;
        }
        return null;
    }
    public static function insert(string $table, array $values): void {
        $values['id'] = self::$nextId++;
        self::$rows[] = $values;
    }
    public static function insert_id(): int { return self::$nextId - 1; }
    public static function update(string $table, array $values, array $where): void {
        foreach (self::$rows as &$row) {
            if ((int)$row['id'] === (int)($where['id'] ?? 0)) $row = array_merge($row, $values);
        }
        unset($row);
    }
}

require_once __DIR__ . '/../../source/plugin/stk_auth/service/IdempotencyService.php';

$_SERVER['HTTP_IDEMPOTENCY_KEY'] = 'test-key-12345678';
$calls = 0;
$first = StkIdempotencyService::run('registerUser', 'mobile:test', ['mobile' => '13800138000'], static function () use (&$calls): array {
    $calls++;
    return ['uid' => 42];
});
if ($first['uid'] !== 42 || $calls !== 1) throw new RuntimeException('Initial idempotent operation failed');
$replay = StkIdempotencyService::run('registerUser', 'mobile:test', ['mobile' => '13800138000'], static function () use (&$calls): array {
    $calls++;
    return ['uid' => 99];
});
if ($replay['uid'] !== 42 || $calls !== 1) throw new RuntimeException('Completed idempotent operation did not replay');

try {
    StkIdempotencyService::run('registerUser', 'mobile:test', ['mobile' => '13900139000'], static fn(): array => ['uid' => 1]);
    throw new RuntimeException('Expected IDEMPOTENCY_KEY_REUSED');
} catch (StkApiException $error) {
    if ($error->getMessage() !== 'IDEMPOTENCY_KEY_REUSED' || $error->httpStatus() !== 409) throw $error;
}

echo "idempotency_contract_test: PASS\n";
