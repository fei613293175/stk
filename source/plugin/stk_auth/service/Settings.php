<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }

final class StkSettings {
    private static function all(): array {
        global $_G;
        $settings = $_G['cache']['plugin']['stk_auth'] ?? [];
        return is_array($settings) ? $settings : [];
    }

    public static function string(string $key, string $default = ''): string {
        $settings = self::all();
        return array_key_exists($key, $settings) ? trim((string)$settings[$key]) : $default;
    }

    public static function int(string $key, int $default, int $min, int $max): int {
        $value = self::string($key, (string)$default);
        if (!preg_match('/^-?[0-9]+$/', $value)) return $default;
        return max($min, min($max, (int)$value));
    }

    public static function bool(string $key, bool $default): bool {
        $value = self::string($key, $default ? '1' : '0');
        return $value === '1' || ($value === '' && $default);
    }

    public static function csv(string $key, array $default): array {
        $value = self::string($key, implode(',', $default));
        $items = array_values(array_filter(array_map('trim', explode(',', $value)), static fn(string $item): bool => $item !== ''));
        return $items ?: $default;
    }
}
