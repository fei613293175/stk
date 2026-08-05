<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
require_once __DIR__ . '/Settings.php';

final class StkPasswordPolicy {
    public static function validate(string $password): bool {
        $length = strlen($password);
        $minimum = StkSettings::int('auth_password_min_length', 8, 8, 32);
        $maximum = StkSettings::int('auth_password_max_length', 32, 16, 64);
        $categoriesRequired = StkSettings::int('auth_password_category_min', 2, 1, 4);
        if ($length < $minimum || $length > $maximum) { return false; }
        $categories = 0;
        foreach ([ '/[0-9]/', '/[a-z]/', '/[A-Z]/', '/[^A-Za-z0-9]/' ] as $pattern) {
            if (preg_match($pattern, $password)) { $categories++; }
        }
        return $categories >= $categoriesRequired;
    }
}
