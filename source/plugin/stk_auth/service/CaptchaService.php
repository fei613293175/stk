<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }

final class StkCaptchaService {
    private static function dateTime(int $timestamp): string { return gmdate('Y-m-d H:i:s', $timestamp); }

    public static function challenge(string $action, string $deviceId): array {
        $allowed = ['password_login', 'sms_send', 'sms_login', 'register', 'password_reset'];
        if (!in_array($action, $allowed, true) || $deviceId === '') {
            throw new StkApiException(422, 'SYS_REQUEST_INVALID', '请求参数错误');
        }
        $answer = strtoupper(substr(bin2hex(random_bytes(3)), 0, 4));
        $challenge = bin2hex(random_bytes(16));
        $expires = time() + 120;
        if (class_exists('DB')) {
            DB::insert('stk_auth_captcha_challenge', [
                'challenge_id' => $challenge,
                'action' => $action,
                'answer_hash' => hash('sha256', $answer),
                'session_hash' => hash('sha256', session_id()),
                'device_hash' => hash('sha256', $deviceId),
                'attempts' => 0,
                'max_attempts' => 5,
                'expires_at' => self::dateTime($expires),
                'created_at' => self::dateTime(time()),
            ]);
        }
        return ['challenge_id' => $challenge, 'image_base64_or_url' => null, 'expires_in' => 120, 'debug_answer' => null];
    }

    public static function verify(string $challengeId, string $answer, string $action, string $deviceId): string {
        if ($challengeId === '' || $answer === '' || $action === '' || $deviceId === '') {
            throw new StkApiException(422, 'CAPTCHA_INVALID', '安全验证码错误');
        }
        if (!class_exists('DB')) { throw new StkApiException(503, 'CAPTCHA_UNAVAILABLE', '安全验证码暂不可用'); }
        $row = DB::fetch_first(
            'SELECT * FROM ' . DB::table('stk_auth_captcha_challenge') . ' WHERE challenge_id=%s LIMIT 1',
            [$challengeId]
        );
        if (!$row || strtotime((string)$row['expires_at']) < time() || $row['used_at'] !== null) {
            throw new StkApiException(410, 'CAPTCHA_EXPIRED', '安全验证码已过期');
        }
        if ((string)$row['action'] !== $action || !hash_equals((string)$row['device_hash'], hash('sha256', $deviceId))) {
            throw new StkApiException(422, 'CAPTCHA_INVALID', '安全验证码错误');
        }
        if ((int)$row['attempts'] >= (int)$row['max_attempts']) {
            throw new StkApiException(429, 'CAPTCHA_ATTEMPTS_EXCEEDED', '安全验证码尝试次数过多');
        }
        if (!hash_equals((string)$row['answer_hash'], hash('sha256', strtoupper($answer)))) {
            DB::query(
                'UPDATE ' . DB::table('stk_auth_captcha_challenge') . ' SET attempts=attempts+1 WHERE challenge_id=%s',
                [$challengeId]
            );
            throw new StkApiException(422, 'CAPTCHA_INVALID', '安全验证码错误');
        }
        $ticket = bin2hex(random_bytes(24));
        DB::update('stk_auth_captcha_challenge', [
            'ticket_hash' => hash('sha256', $ticket),
            'ticket_expires_at' => self::dateTime(time() + 90),
            'verified_at' => self::dateTime(time()),
        ], ['challenge_id' => $challengeId]);
        return $ticket;
    }

    public static function consumeTicket(string $ticket, string $action, string $deviceId): void {
        if ($ticket === '' || $action === '' || $deviceId === '' || !class_exists('DB')) {
            throw new StkApiException(422, 'CAPTCHA_TICKET_INVALID', '安全验证已失效，请重试');
        }
        $row = DB::fetch_first(
            'SELECT * FROM ' . DB::table('stk_auth_captcha_challenge') . ' WHERE ticket_hash=%s LIMIT 1',
            [hash('sha256', $ticket)]
        );
        if (!$row || $row['used_at'] !== null || strtotime((string)$row['ticket_expires_at']) < time()
            || (string)$row['action'] !== $action
            || !hash_equals((string)$row['device_hash'], hash('sha256', $deviceId))) {
            throw new StkApiException(422, 'CAPTCHA_TICKET_INVALID', '安全验证已失效，请重试');
        }
        DB::update('stk_auth_captcha_challenge', ['used_at' => self::dateTime(time())], ['id' => (int)$row['id']]);
    }
}
