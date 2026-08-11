<?php

declare(strict_types=1);

// Read-only V1.4.0 production audit. Run with the Discuz root as cwd.
define('CURSCRIPT', 'forum');
$_SERVER['REMOTE_ADDR'] = '127.0.0.1';
$_SERVER['HTTP_HOST'] = 'stk.zz-yihao.com';
$_SERVER['SERVER_NAME'] = 'stk.zz-yihao.com';

$root = rtrim((string) (getenv('STK_DISCUZ_ROOT') ?: getcwd()), '/\\');
if ($root !== '/www/wwwroot/stk_zz_yihao_com' || !is_file($root . '/source/class/class_core.php')) {
    fwrite(STDERR, 'STK_DISCUZ_ROOT is not the guarded production root.' . PHP_EOL);
    exit(2);
}

chdir($root);
require $root . '/source/class/class_core.php';
$discuz = C::app();
$discuz->init();

$configKeys = [
    'release.current_version',
    'release.minimum_version',
    'release.force_update',
    'release.download_url',
    'release.apk_sha256',
    'release.notes',
    'maintenance.enabled',
    'maintenance.message',
    'maintenance.expected_end',
    'app_links.host',
];

function emitV140(string $label, $value): void
{
    echo $label . PHP_EOL;
    echo json_encode($value, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES) . PHP_EOL;
}

function tableExistsV140(string $table): bool
{
    return (bool) DB::fetch_first('SHOW TABLES LIKE %s', [DB::table($table)]);
}

function tableDefinitionV140(string $table): array
{
    if (!tableExistsV140($table)) {
        return ['exists' => false, 'columns' => [], 'indexes' => [], 'row_count' => null];
    }
    return [
        'exists' => true,
        'columns' => DB::fetch_all('SHOW COLUMNS FROM %t', [$table]),
        'indexes' => DB::fetch_all('SHOW INDEX FROM %t', [$table]),
        'row_count' => (int) DB::result_first('SELECT COUNT(*) FROM %t', [$table]),
    ];
}

$configRows = tableExistsV140('stk_app_config')
    ? DB::fetch_all(
        'SELECT config_key,config_value,updated_at FROM %t WHERE config_key IN (%n) ORDER BY config_key',
        ['stk_app_config', $configKeys]
    )
    : [];

emitV140('MODE', 'read_only');
emitV140('DISCUZ_CONTEXT', [
    'root' => defined('DISCUZ_ROOT') ? DISCUZ_ROOT : $root,
    'table_prefix' => DB::table(''),
]);
emitV140('TABLES', [
    'stk_release' => tableDefinitionV140('stk_release'),
    'stk_app_config' => tableDefinitionV140('stk_app_config'),
    'stk_admin_audit' => tableDefinitionV140('stk_admin_audit'),
]);
emitV140('CONTRACT_CONFIG', [
    'expected_count' => count($configKeys),
    'actual_count' => count($configRows),
    'rows' => $configRows,
]);
emitV140(
    'RELEASE_10401',
    tableExistsV140('stk_release')
        ? DB::fetch_all(
            'SELECT version_name,version_code,minimum_version_name,minimum_version_code,apk_url,apk_sha256,apk_size_bytes,mandatory,status,enabled,published_at,updated_at FROM %t WHERE version_code=%d',
            ['stk_release', 10401]
        )
        : []
);
emitV140(
    'RECENT_V140_ADMIN_AUDIT',
    tableExistsV140('stk_admin_audit')
        ? DB::fetch_all(
            'SELECT audit_id,operator_uid,section,action,payload_json,ip,created_at FROM %t WHERE section IN (%n) ORDER BY audit_id DESC LIMIT 20',
            ['stk_admin_audit', ['release', 'maintenance', 'deployment', 'security']]
        )
        : []
);
