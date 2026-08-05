<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
final class AuthRepository {
    public static function install(): void {
        $path = dirname(__DIR__) . '/migrations/V1.0.0__auth_project_foundation.sql';
        $sql = file_get_contents($path);
        if (!is_string($sql) || trim($sql) === '') {
            throw new RuntimeException('V1.0.0 plugin migration is missing');
        }
        if (function_exists('runquery')) {
            runquery($sql);
            return;
        }
        if (!class_exists('DB')) {
            throw new RuntimeException('Discuz database runtime is unavailable');
        }

        $sql = preg_replace_callback(
            '/\bpre_([A-Za-z0-9_]+)/',
            static fn(array $match): string => DB::table($match[1]),
            $sql
        );
        $sql = preg_replace('/^\s*--.*$/m', '', (string)$sql);
        foreach (preg_split('/;\s*(?:\r?\n|$)/', (string)$sql) as $statement) {
            $statement = trim($statement);
            if ($statement !== '' && stripos($statement, 'CREATE TABLE') === 0) { DB::query($statement); }
        }
    }
    public static function hash(string $value): string { return hash('sha256', $value); }
}
