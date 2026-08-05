<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }

final class StkPasswordPolicy {
    public static function validate(string $password): bool {
        $length = strlen($password);
        if ($length < 8 || $length > 32) { return false; }
        $categories = 0;
        foreach ([ '/[0-9]/', '/[a-z]/', '/[A-Z]/', '/[^A-Za-z0-9]/' ] as $pattern) {
            if (preg_match($pattern, $password)) { $categories++; }
        }
        return $categories >= 2;
    }
}
