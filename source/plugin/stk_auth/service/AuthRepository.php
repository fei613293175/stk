<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
final class AuthRepository {
    public static function install(): void {
        if (!class_exists('DB')) { return; }
        $sql = file_get_contents(dirname(__DIR__, 4) . '/backend/sql/V1.0.0__auth_project_foundation.sql');
        foreach (preg_split('/;\s*(?:\r?\n|$)/', (string)$sql) as $statement) {
            $statement = trim($statement);
            if ($statement !== '' && stripos($statement, 'CREATE TABLE') !== false) { DB::query($statement); }
        }
    }
    public static function hash(string $value): string { return hash('sha256', $value); }
}
