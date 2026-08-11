<?php

declare(strict_types=1);

// Guarded V1.3.0 migration runner. Run from the production Discuz root only
// after a verified database and plugin backup has been created.
if (PHP_SAPI !== 'cli') {
    fwrite(STDERR, 'CLI only.' . PHP_EOL);
    exit(2);
}
if (getenv('STK_V130_APPLY') !== '1') {
    fwrite(STDERR, 'Set STK_V130_APPLY=1 to run the migration.' . PHP_EOL);
    exit(3);
}

$supportUrl = trim((string) getenv('STK_V130_SUPPORT_URL'));
$supportScheme = strtolower((string) parse_url($supportUrl, PHP_URL_SCHEME));
$supportHost = strtolower((string) parse_url($supportUrl, PHP_URL_HOST));
if ($supportScheme !== 'https' || $supportHost !== 'stk.zz-yihao.com') {
    fwrite(STDERR, 'STK_V130_SUPPORT_URL must be an HTTPS URL on stk.zz-yihao.com.' . PHP_EOL);
    exit(4);
}
define('STK_V130_MIGRATION_SUPPORT_URL', $supportUrl);
define('STK_V130_MIGRATION_SUPPORT_HOST', $supportHost);

define('CURSCRIPT', 'forum');
$_SERVER['REMOTE_ADDR'] = '127.0.0.1';
$_SERVER['HTTP_HOST'] = 'stk.zz-yihao.com';
$_SERVER['SERVER_NAME'] = 'stk.zz-yihao.com';

require rtrim((string) getcwd(), '/\\') . '/source/class/class_core.php';
$discuz = C::app();
$discuz->init();
$supportUrl = STK_V130_MIGRATION_SUPPORT_URL;
$supportHost = STK_V130_MIGRATION_SUPPORT_HOST;

require_once DISCUZ_ROOT . 'source/function/function_plugin.php';
$finish = false;
require DISCUZ_ROOT . 'source/plugin/stk_project/upgrade.php';
if ($finish !== true) {
    fwrite(STDERR, 'Plugin migration did not report completion.' . PHP_EOL);
    exit(10);
}

require_once DISCUZ_ROOT . 'source/plugin/stk_project/lib/config.php';
foreach ([
    'support_type' => 'url',
    'support_label' => '在线客服',
    'support_value' => $supportUrl,
    'support_hours' => '工作日 09:00-18:00',
    'support_copy_enabled' => '1',
    'support_url_allowlist' => $supportHost,
] as $key => $value) {
    stk_project_set_config($key, $value);
}
stk_project_admin_audit('support', 'deployment_config_update', [
    'type' => 'url',
    'url_hosts' => [$supportHost],
    'release' => '1.3.0-beta.1',
]);

$requiredColumns = [
    'stk_member_status' => 'starts_at',
    'stk_wallet_account' => 'task_amount',
];
foreach ($requiredColumns as $table => $column) {
    if (!DB::fetch_first('SHOW COLUMNS FROM %t LIKE %s', [$table, $column])) {
        fwrite(STDERR, 'Required column is missing after migration: ' . $table . '.' . $column . PHP_EOL);
        exit(11);
    }
}

$result = [
    'schema_version' => stk_project_config('schema_version', ''),
    'support_type' => stk_project_config('support_type', ''),
    'support_value' => stk_project_config('support_value', ''),
    'support_url_allowlist' => stk_project_config('support_url_allowlist', ''),
    'member_rows' => (int) DB::result_first('SELECT COUNT(*) FROM %t', ['stk_member_status']),
    'wallet_rows' => (int) DB::result_first('SELECT COUNT(*) FROM %t', ['stk_wallet_account']),
    'prop_rows' => (int) DB::result_first('SELECT COUNT(*) FROM %t', ['stk_prop_catalog']),
];
if ($result['schema_version'] !== '13001') {
    fwrite(STDERR, 'Unexpected schema version after migration.' . PHP_EOL);
    exit(12);
}

echo json_encode($result, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES) . PHP_EOL;
