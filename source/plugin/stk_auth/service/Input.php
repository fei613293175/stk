<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }

final class StkInput {
    public static function json(): array {
        $raw = file_get_contents('php://input');
        $value = json_decode((string)$raw, true);
        return is_array($value) ? $value : [];
    }

    public static function string(array $input, string $key, int $max = 255): string {
        $value = trim((string)($input[$key] ?? ''));
        return function_exists('mb_substr') ? mb_substr($value, 0, $max, 'UTF-8') : substr($value, 0, $max);
    }

    public static function mobile(array $input): string {
        $value = self::string($input, 'mobile', 32);
        return preg_match('/^1[3-9][0-9]{9}$/', $value) ? $value : '';
    }
}
