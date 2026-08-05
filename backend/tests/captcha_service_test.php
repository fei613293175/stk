<?php
declare(strict_types=1);

define('IN_DISCUZ', true);

require_once __DIR__ . '/../../source/plugin/stk_auth/service/ApiResponse.php';

final class DB {
    public static array $challenges = [];
    private static int $nextId = 1;

    public static function table(string $name): string { return 'pre_' . $name; }

    public static function insert(string $table, array $row): int {
        if ($table !== 'stk_auth_captcha_challenge') {
            throw new RuntimeException('Unexpected table: ' . $table);
        }
        $row += [
            'id' => self::$nextId++,
            'ticket_hash' => null,
            'ticket_expires_at' => null,
            'verified_at' => null,
            'used_at' => null,
        ];
        self::$challenges[$row['challenge_id']] = $row;
        return (int)$row['id'];
    }

    public static function fetch_first(string $sql, array $arguments = []): ?array {
        $value = (string)($arguments[0] ?? '');
        if (str_contains($sql, 'challenge_id=')) {
            return self::$challenges[$value] ?? null;
        }
        if (str_contains($sql, 'ticket_hash=')) {
            foreach (self::$challenges as $row) {
                if (($row['ticket_hash'] ?? null) === $value) return $row;
            }
        }
        return null;
    }

    public static function update(string $table, array $values, array $where): void {
        foreach (self::$challenges as $challengeId => $row) {
            $matches = isset($where['challenge_id'])
                ? $challengeId === (string)$where['challenge_id']
                : (int)$row['id'] === (int)($where['id'] ?? 0);
            if ($matches) self::$challenges[$challengeId] = array_merge($row, $values);
        }
    }

    public static function query(string $sql, array $arguments = []): void {
        $challengeId = (string)($arguments[0] ?? '');
        if (!isset(self::$challenges[$challengeId]) || !str_contains($sql, 'attempts=attempts+1')) {
            throw new RuntimeException('Unexpected captcha query');
        }
        self::$challenges[$challengeId]['attempts']++;
    }
}

require_once __DIR__ . '/../../source/plugin/stk_auth/service/CaptchaService.php';

function assertTrue(bool $condition, string $message): void {
    if (!$condition) throw new RuntimeException($message);
}

function expectApiCode(string $code, callable $operation): void {
    try {
        $operation();
    } catch (StkApiException $error) {
        assertTrue($error->getMessage() === $code, "Expected $code, received {$error->getMessage()}");
        return;
    }
    throw new RuntimeException("Expected StkApiException $code");
}

function answerForHash(string $hash): string {
    for ($candidate = 0; $candidate <= 0xFFFF; $candidate++) {
        $answer = sprintf('%04X', $candidate);
        if (hash_equals($hash, hash('sha256', $answer))) return $answer;
    }
    throw new RuntimeException('Captcha answer hash is outside the contracted four-hex-character space');
}

$challenge = StkCaptchaService::challenge('password_login', 'test-device');
assertTrue(isset($challenge['challenge_id'], $challenge['image_base64_or_url'], $challenge['expires_in']), 'Challenge response fields are incomplete');
assertTrue(!array_key_exists('debug_answer', $challenge), 'Challenge response must not expose a debug answer');
assertTrue(str_starts_with($challenge['image_base64_or_url'], 'data:image/png;base64,'), 'Challenge image must be an inline PNG');
$png = base64_decode(substr($challenge['image_base64_or_url'], strlen('data:image/png;base64,')), true);
assertTrue(is_string($png) && str_starts_with($png, "\x89PNG\r\n\x1a\n"), 'Challenge image is not a valid PNG payload');

$challengeId = $challenge['challenge_id'];
$stored = DB::$challenges[$challengeId];
$answer = answerForHash($stored['answer_hash']);
expectApiCode('CAPTCHA_INVALID', fn() => StkCaptchaService::verify($challengeId, 'ZZZZ', 'password_login', 'test-device'));
assertTrue(DB::$challenges[$challengeId]['attempts'] === 1, 'Invalid answer must increment attempts');

$ticket = StkCaptchaService::verify($challengeId, $answer, 'password_login', 'test-device');
assertTrue(strlen($ticket) === 48, 'Captcha ticket must contain 24 random bytes');
expectApiCode('CAPTCHA_EXPIRED', fn() => StkCaptchaService::verify($challengeId, $answer, 'password_login', 'test-device'));
StkCaptchaService::consumeTicket($ticket, 'password_login', 'test-device');
expectApiCode('CAPTCHA_TICKET_INVALID', fn() => StkCaptchaService::consumeTicket($ticket, 'password_login', 'test-device'));

$expired = StkCaptchaService::challenge('register', 'test-device');
DB::$challenges[$expired['challenge_id']]['expires_at'] = '2000-01-01 00:00:00';
expectApiCode('CAPTCHA_EXPIRED', fn() => StkCaptchaService::verify($expired['challenge_id'], '0000', 'register', 'test-device'));
expectApiCode('SYS_REQUEST_INVALID', fn() => StkCaptchaService::challenge('unsupported', 'test-device'));

echo "captcha_service_test: PASS\n";
