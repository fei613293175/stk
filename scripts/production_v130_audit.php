<?php

declare(strict_types=1);

// Read-only V1.3.0 production audit. Run from the Discuz root.
define('CURSCRIPT', 'forum');
$_SERVER['REMOTE_ADDR'] = '127.0.0.1';
$_SERVER['HTTP_HOST'] = 'stk.zz-yihao.com';
$_SERVER['SERVER_NAME'] = 'stk.zz-yihao.com';

require rtrim((string) getcwd(), '/\\\\') . '/source/class/class_core.php';
$discuz = C::app();
$discuz->init();

$ownerUid = 9;
$v130ConfigKeys = [
    'schema_version',
    'show_wallets',
    'commission_label',
    'task_label',
    'show_member_card',
    'member_title',
    'member_benefit_discount',
    'member_benefit_rebate',
    'member_open_button_text',
    'show_props_center',
    'profile_bio',
    'placeholder_message',
    'support_type',
    'support_label',
    'support_value',
    'support_hours',
    'support_copy_enabled',
    'support_url_allowlist',
];

function emitV130(string $label, $value): void
{
    echo $label . PHP_EOL;
    echo json_encode($value, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES) . PHP_EOL;
}

function tableExistsV130(string $table): bool
{
    return (bool) DB::fetch_first('SHOW TABLES LIKE %s', [DB::table($table)]);
}

function tableDefinitionV130(string $table): array
{
    if (!tableExistsV130($table)) {
        return [
            'exists' => false,
            'columns' => [],
            'indexes' => [],
            'row_count' => null,
        ];
    }

    return [
        'exists' => true,
        'columns' => DB::fetch_all('SHOW COLUMNS FROM %t', [$table]),
        'indexes' => DB::fetch_all('SHOW INDEX FROM %t', [$table]),
        'row_count' => (int) DB::result_first('SELECT COUNT(*) FROM %t', [$table]),
    ];
}

function configRowsV130(string $table, array $keys): array
{
    if (!tableExistsV130($table)) return [];

    $rows = DB::fetch_all(
        'SELECT config_key,config_value,updated_at FROM %t WHERE config_key IN (%n) ORDER BY config_key',
        [$table, $keys]
    );
    return is_array($rows) ? $rows : [];
}

$memberExists = tableExistsV130('stk_member_status');
$walletExists = tableExistsV130('stk_wallet_account');
$propsExist = tableExistsV130('stk_prop_catalog');
$auditExists = tableExistsV130('stk_admin_audit');

emitV130('MODE', 'read_only');
emitV130('DISCuz_CONTEXT', [
    'root' => defined('DISCUZ_ROOT') ? DISCUZ_ROOT : getcwd(),
    'table_prefix' => DB::table(''),
    'owner_uid' => $ownerUid,
    'owner_exists' => (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE uid=%d', ['common_member', $ownerUid]) === 1,
]);
emitV130('TABLES', [
    'stk_member_status' => tableDefinitionV130('stk_member_status'),
    'stk_wallet_account' => tableDefinitionV130('stk_wallet_account'),
    'stk_prop_catalog' => tableDefinitionV130('stk_prop_catalog'),
    'stk_project_config' => tableDefinitionV130('stk_project_config'),
    'stk_app_config' => tableDefinitionV130('stk_app_config'),
    'stk_admin_audit' => tableDefinitionV130('stk_admin_audit'),
]);
emitV130('PROJECT_CONFIG', configRowsV130('stk_project_config', $v130ConfigKeys));
emitV130('APP_CONFIG', configRowsV130('stk_app_config', $v130ConfigKeys));
emitV130(
    'OWNER_MEMBER_STATUS',
    $memberExists
        ? DB::fetch_all('SELECT * FROM %t WHERE uid=%d', ['stk_member_status', $ownerUid])
        : []
);
emitV130(
    'OWNER_WALLET_ACCOUNT',
    $walletExists
        ? DB::fetch_all('SELECT * FROM %t WHERE uid=%d', ['stk_wallet_account', $ownerUid])
        : []
);
emitV130(
    'PROP_CATALOG',
    $propsExist
        ? DB::fetch_all('SELECT * FROM %t ORDER BY sort_order,prop_id', ['stk_prop_catalog'])
        : []
);
emitV130(
    'RECENT_V130_ADMIN_AUDIT',
    $auditExists
        ? DB::fetch_all(
            'SELECT audit_id,operator_uid,section,action,payload_json,ip,created_at FROM %t WHERE section IN (%n) ORDER BY audit_id DESC LIMIT 20',
            ['stk_admin_audit', ['me', 'membership', 'wallets', 'props', 'support']]
        )
        : []
);
