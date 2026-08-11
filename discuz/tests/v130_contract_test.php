<?php

define('IN_DISCUZ', true);
$root = dirname(__DIR__) . '/source/plugin';
$api = file_get_contents($root . '/stk_project/api.inc.php');
$install = file_get_contents($root . '/stk_project/install.php');
$upgrade = file_get_contents($root . '/stk_project/upgrade.php');
$meAdmin = file_get_contents($root . '/stk_project/admin/me_settings.inc.php');
$memberAdmin = file_get_contents($root . '/stk_project/admin/memberships.inc.php');
$walletAdmin = file_get_contents($root . '/stk_project/admin/wallets.inc.php');
$propsAdmin = file_get_contents($root . '/stk_project/admin/props_settings.inc.php');
$supportAdmin = file_get_contents($root . '/stk_project/admin/support_settings.inc.php');
$authAccounts = file_get_contents($root . '/stk_auth/admin/accounts.inc.php');

function v130_fail(string $message): void
{
    fwrite(STDERR, 'FAIL: ' . $message . PHP_EOL);
    exit(1);
}

function v130_require(string $source, array $markers, string $label): void
{
    foreach ($markers as $marker) {
        if (strpos($source, $marker) === false) v130_fail($label . ' missing ' . $marker);
    }
}

v130_require($api, [
    "['me/profile', 'me/member', 'me/wallets', 'props/display', 'support']",
    'stk_project_require_auth()',
    "INSERT IGNORE INTO %t (uid,status,member_label,level,starts_at,expires_at,updated_at)",
    "INSERT IGNORE INTO %t (uid,commission_amount,task_amount,task_points,updated_at)",
    "'placeholder_message' => stk_project_config",
    "'allowed_url_hosts' =>",
    "'show_wallets' =>",
    "'show_center' =>",
], 'V1.3 API');

v130_require($install . $upgrade, [
    'pre_stk_member_status', 'pre_stk_wallet_account', 'pre_stk_prop_catalog',
    "'schema_version','13001'", 'SELECT uid,0,0,0', 'starts_at', 'task_amount',
], 'V1.3 migration');

foreach ([$meAdmin, $memberAdmin, $propsAdmin, $supportAdmin] as $adminSource) {
    if (strpos($adminSource, 'stk_project_admin_audit') === false) v130_fail('mutating V1.3 admin page must write audit log');
}
if (strpos($walletAdmin, 'submitcheck') !== false || strpos($walletAdmin, 'ON DUPLICATE KEY UPDATE commission_amount') !== false) {
    v130_fail('wallet admin must remain read-only');
}
if (strpos($authAccounts, "ON DUPLICATE KEY UPDATE commission_amount") !== false || strpos($authAccounts, "showsetting('佣金余额'") !== false) {
    v130_fail('legacy account admin must not change balances');
}
if (strpos($api, 'stk_account_prop') !== false || strpos($api, 'quantity') !== false) {
    v130_fail('V1.3 project API must not expose user prop inventory');
}
if (strpos($supportAdmin, "\$scheme !== 'https'") === false || strpos($supportAdmin, 'support_url_allowlist') === false) {
    v130_fail('support external URL policy is incomplete');
}

require_once $root . '/stk_project/lib/routes.php';
foreach (['api/v1/me/profile','api/v1/me/member','api/v1/me/wallets','api/v1/props/display','api/v1/support'] as $path) {
    $route = stk_project_normalize_route($path, 'GET');
    if ($route['resource'] === '' || $route['method'] !== 'GET') v130_fail('invalid route ' . $path);
}

echo "PASS: v130_contract_test" . PHP_EOL;
