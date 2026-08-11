<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

function stk_auth_get_config(string $key, string $default = ''): string
{
    try {
        $row = DB::fetch_first(
            'SELECT config_value FROM %t WHERE config_key=%s',
            ['stk_auth_config', $key]
        );
        if (isset($row['config_value'])) return (string) $row['config_value'];
        $shared = DB::fetch_first('SELECT config_value FROM %t WHERE config_key=%s', ['stk_app_config', $key]);
        return isset($shared['config_value']) ? (string) $shared['config_value'] : $default;
    } catch (Throwable $error) {
        return $default;
    }
}

function stk_auth_set_config(string $key, string $value): void
{
    DB::query(
        'INSERT INTO %t (config_key, config_value, updated_at) VALUES (%s, %s, %d) '
        . 'ON DUPLICATE KEY UPDATE config_value=VALUES(config_value), updated_at=VALUES(updated_at)',
        ['stk_auth_config', $key, $value, TIMESTAMP]
    );
    DB::query(
        'INSERT INTO %t (config_key, config_value, updated_at) VALUES (%s, %s, %d) '
        . 'ON DUPLICATE KEY UPDATE config_value=VALUES(config_value), updated_at=VALUES(updated_at)',
        ['stk_app_config', $key, $value, TIMESTAMP]
    );
}
