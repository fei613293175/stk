<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
require_once __DIR__ . '/Settings.php';

final class StkCaptchaService {
    private static function dateTime(int $timestamp): string { return gmdate('Y-m-d H:i:s', $timestamp); }
    private static function utcTimestamp(string $value): int {
        $parsed = strtotime($value . ' UTC');
        return $parsed === false ? 0 : $parsed;
    }

    private static function renderPngDataUri(string $answer): string {
        if (!function_exists('imagecreatetruecolor') || !function_exists('imagepng')) {
            throw new StkApiException(503, 'CAPTCHA_UNAVAILABLE', '安全验证码暂不可用');
        }

        $width = 278;
        $height = 96;
        $image = imagecreatetruecolor($width, $height);
        if ($image === false) {
            throw new StkApiException(503, 'CAPTCHA_UNAVAILABLE', '安全验证码暂不可用');
        }

        try {
            $background = imagecolorallocate($image, 245, 247, 250);
            $foreground = imagecolorallocate($image, 23, 32, 51);
            $line = imagecolorallocate($image, 152, 162, 179);
            $accent = imagecolorallocate($image, 36, 107, 253);
            if ($background === false || $foreground === false || $line === false || $accent === false) {
                throw new RuntimeException('captcha color allocation failed');
            }

            imagefilledrectangle($image, 0, 0, $width - 1, $height - 1, $background);
            for ($index = 0; $index < 6; $index++) {
                imageline(
                    $image,
                    random_int(0, $width - 1),
                    random_int(0, $height - 1),
                    random_int(0, $width - 1),
                    random_int(0, $height - 1),
                    $index % 2 === 0 ? $line : $accent
                );
            }
            for ($index = 0; $index < 90; $index++) {
                imagesetpixel($image, random_int(0, $width - 1), random_int(0, $height - 1), $line);
            }

            $font = 5;
            $characterWidth = imagefontwidth($font);
            $characterHeight = imagefontheight($font);
            $spacing = 22;
            $textWidth = strlen($answer) * $characterWidth + (strlen($answer) - 1) * $spacing;
            $startX = (int)(($width - $textWidth) / 2);
            $baseY = (int)(($height - $characterHeight) / 2);
            foreach (str_split($answer) as $index => $character) {
                imagestring(
                    $image,
                    $font,
                    $startX + $index * ($characterWidth + $spacing),
                    $baseY + random_int(-8, 8),
                    $character,
                    $index % 2 === 0 ? $foreground : $accent
                );
            }

            ob_start();
            if (!imagepng($image, null, 6)) {
                ob_end_clean();
                throw new RuntimeException('captcha png encoding failed');
            }
            $png = ob_get_clean();
            if (!is_string($png) || $png === '') {
                throw new RuntimeException('captcha png output missing');
            }
            return 'data:image/png;base64,' . base64_encode($png);
        } catch (Throwable $error) {
            if ($error instanceof StkApiException) {
                throw $error;
            }
            throw new StkApiException(503, 'CAPTCHA_UNAVAILABLE', '安全验证码暂不可用');
        } finally {
            imagedestroy($image);
        }
    }

    public static function challenge(string $action, string $deviceId): array {
        $allowed = ['password_login', 'sms_send', 'sms_login', 'register', 'password_reset'];
        if (!in_array($action, $allowed, true) || !in_array($action, StkSettings::csv('captcha_required_actions', $allowed), true) || $deviceId === '') {
            throw new StkApiException(422, 'SYS_REQUEST_INVALID', '请求参数错误');
        }
        if (!StkSettings::bool('captcha_enabled', true)) throw new StkApiException(503, 'CAPTCHA_UNAVAILABLE', '安全验证码暂不可用');
        $length = StkSettings::int('captcha_length', 4, 4, 6);
        $answer = strtoupper(substr(bin2hex(random_bytes((int)ceil($length / 2))), 0, $length));
        $challenge = bin2hex(random_bytes(16));
        $expires = time() + StkSettings::int('captcha_challenge_ttl_seconds', 120, 60, 300);
        $imageDataUri = self::renderPngDataUri($answer);
        if (class_exists('DB')) {
            DB::insert('stk_auth_captcha_challenge', [
                'challenge_id' => $challenge,
                'action' => $action,
                'answer_hash' => hash('sha256', $answer),
                'session_hash' => hash('sha256', session_id()),
                'device_hash' => hash('sha256', $deviceId),
                'attempts' => 0,
                'max_attempts' => StkSettings::int('captcha_max_attempts', 5, 2, 10),
                'expires_at' => self::dateTime($expires),
                'created_at' => self::dateTime(time()),
            ]);
        }
        return ['challenge_id' => $challenge, 'image_base64_or_url' => $imageDataUri, 'expires_in' => StkSettings::int('captcha_challenge_ttl_seconds', 120, 60, 300)];
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
        if (!$row || self::utcTimestamp((string)$row['expires_at']) < time() || $row['verified_at'] !== null || $row['used_at'] !== null) {
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
            'ticket_expires_at' => self::dateTime(time() + StkSettings::int('captcha_ticket_ttl_seconds', 90, 30, 180)),
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
        if (!$row || $row['used_at'] !== null || self::utcTimestamp((string)$row['ticket_expires_at']) < time()
            || (string)$row['action'] !== $action
            || !hash_equals((string)$row['device_hash'], hash('sha256', $deviceId))) {
            throw new StkApiException(422, 'CAPTCHA_TICKET_INVALID', '安全验证已失效，请重试');
        }
        DB::update('stk_auth_captcha_challenge', ['used_at' => self::dateTime(time())], ['id' => (int)$row['id']]);
    }
}
