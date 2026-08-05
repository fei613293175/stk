<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }

final class StkSecretConfig {
    private static function key(): string {
        global $_G;
        $authKey = (string)($_G['config']['security']['authkey'] ?? '');
        if ($authKey === '') throw new RuntimeException('Server encryption key is unavailable');
        return hash('sha256', 'stk-secret-config-v1|' . $authKey, true);
    }

    public static function encrypt(string $plaintext): string {
        $iv = random_bytes(12);
        $tag = '';
        $ciphertext = openssl_encrypt($plaintext, 'aes-256-gcm', self::key(), OPENSSL_RAW_DATA, $iv, $tag);
        if ($ciphertext === false) throw new RuntimeException('Secret encryption failed');
        return base64_encode($iv . $tag . $ciphertext);
    }

    public static function decrypt(string $payload): string {
        $raw = base64_decode($payload, true);
        if ($raw === false || strlen($raw) < 29) return '';
        $plaintext = openssl_decrypt(substr($raw, 28), 'aes-256-gcm', self::key(), OPENSSL_RAW_DATA, substr($raw, 0, 12), substr($raw, 12, 16));
        return $plaintext === false ? '' : $plaintext;
    }

    public static function get(string $key): string {
        if (!class_exists('DB')) return '';
        $row = DB::fetch_first('SELECT ciphertext FROM ' . DB::table('stk_secret_config') . ' WHERE config_key=%s LIMIT 1', [$key]);
        return $row ? self::decrypt((string)$row['ciphertext']) : '';
    }
}
