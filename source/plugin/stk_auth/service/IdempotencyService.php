<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
require_once __DIR__ . '/ApiResponse.php';

final class StkIdempotencyService {
    private static function dateTime(int $timestamp): string { return gmdate('Y-m-d H:i:s', $timestamp); }

    public static function run(string $operation, string $subject, array $input, callable $callback): array {
        if (!class_exists('DB')) throw new StkApiException(503, 'SYS_UNAVAILABLE', '服务暂不可用');
        $key = trim((string)($_SERVER['HTTP_IDEMPOTENCY_KEY'] ?? ''));
        if (!preg_match('/^[A-Za-z0-9._:-]{16,128}$/', $key)) {
            throw new StkApiException(428, 'IDEMPOTENCY_KEY_REQUIRED', '请求缺少幂等键');
        }
        $keyHash = hash('sha256', $key);
        $requestDigest = hash('sha256', json_encode($input, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES));
        $now = time();
        $existing = DB::fetch_first(
            'SELECT * FROM ' . DB::table('stk_idempotency_key') . ' WHERE operation_id=%s AND idempotency_key_hash=%s LIMIT 1',
            [$operation, $keyHash]
        );
        if ($existing && strtotime((string)$existing['expires_at'] . ' UTC') !== false && strtotime((string)$existing['expires_at'] . ' UTC') >= $now) {
            if (!hash_equals((string)$existing['request_digest'], $requestDigest)) {
                throw new StkApiException(409, 'IDEMPOTENCY_KEY_REUSED', '幂等键已用于其他请求');
            }
            if ((string)$existing['status'] === 'completed') {
                $payload = json_decode((string)($existing['response_payload'] ?? ''), true);
                if (is_array($payload)) return $payload;
                throw new StkApiException(409, 'IDEMPOTENCY_REPLAY_UNAVAILABLE', '幂等结果暂不可重放');
            }
            if ((string)$existing['status'] === 'pending') {
                throw new StkApiException(409, 'IDEMPOTENCY_IN_PROGRESS', '相同请求正在处理中');
            }
        }

        if ($existing) {
            DB::update('stk_idempotency_key', [
                'uid_or_subject' => substr($subject, 0, 128),
                'request_digest' => $requestDigest,
                'response_digest' => null,
                'response_payload' => null,
                'response_status' => null,
                'status' => 'pending',
                'expires_at' => self::dateTime($now + 86400),
                'updated_at' => self::dateTime($now),
            ], ['id' => (int)$existing['id']]);
            $id = (int)$existing['id'];
        } else {
            DB::insert('stk_idempotency_key', [
                'idempotency_key_hash' => $keyHash,
                'uid_or_subject' => substr($subject, 0, 128),
                'operation_id' => $operation,
                'request_digest' => $requestDigest,
                'status' => 'pending',
                'expires_at' => self::dateTime($now + 86400),
                'created_at' => self::dateTime($now),
                'updated_at' => self::dateTime($now),
            ]);
            $id = (int)DB::insert_id();
        }

        try {
            $payload = $callback();
            if (!is_array($payload)) throw new RuntimeException('Idempotent operation must return an array');
            $encoded = json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
            DB::update('stk_idempotency_key', [
                'response_digest' => hash('sha256', (string)$encoded),
                'response_payload' => $encoded,
                'response_status' => 200,
                'status' => 'completed',
                'updated_at' => self::dateTime(time()),
            ], ['id' => $id]);
            return $payload;
        } catch (Throwable $error) {
            DB::update('stk_idempotency_key', ['status' => 'failed', 'updated_at' => self::dateTime(time())], ['id' => $id]);
            throw $error;
        }
    }
}
