<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }

final class StkMobileCrypto {
    private static function key(): string {
        global $_G;
        $authKey = (string)($_G['config']['security']['authkey'] ?? '');
        if ($authKey === '') { throw new StkApiException(503, 'SYS_UNAVAILABLE', '服务暂不可用'); }
        return hash('sha256', 'stk-mobile-v1|' . $authKey, true);
    }

    public static function encrypt(string $mobile): string {
        $iv = random_bytes(12);
        $tag = '';
        $ciphertext = openssl_encrypt($mobile, 'aes-256-gcm', self::key(), OPENSSL_RAW_DATA, $iv, $tag);
        if ($ciphertext === false) { throw new StkApiException(503, 'SYS_UNAVAILABLE', '服务暂不可用'); }
        return base64_encode($iv . $tag . $ciphertext);
    }

    public static function decrypt(string $payload): string {
        $raw = base64_decode($payload, true);
        if ($raw === false || strlen($raw) < 29) { return ''; }
        $value = openssl_decrypt(substr($raw, 28), 'aes-256-gcm', self::key(), OPENSSL_RAW_DATA, substr($raw, 0, 12), substr($raw, 12, 16));
        return $value === false ? '' : $value;
    }

    public static function hash(string $mobile): string { return hash('sha256', $mobile); }
    public static function mask(string $mobile): string { return strlen($mobile) === 11 ? substr($mobile, 0, 3) . '****' . substr($mobile, -4) : '***'; }
}
