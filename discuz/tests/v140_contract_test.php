<?php

define('IN_DISCUZ', true);
$root = dirname(__DIR__) . '/source/plugin/stk_project';
$api = file_get_contents($root . '/api.inc.php');
$routes = file_get_contents($root . '/lib/routes.php');
$config = file_get_contents($root . '/lib/config.php');
$install = file_get_contents($root . '/install.php');
$upgrade = file_get_contents($root . '/upgrade.php');
$releaseAdmin = file_get_contents($root . '/admin/release_settings.inc.php');
$maintenanceAdmin = file_get_contents($root . '/admin/maintenance_settings.inc.php');
$deploymentAdmin = file_get_contents($root . '/admin/deployment_diagnostics.inc.php');
$securityAdmin = file_get_contents($root . '/admin/security_audit.inc.php');

function v140_fail(string $message): void
{
    fwrite(STDERR, 'FAIL: ' . $message . PHP_EOL);
    exit(1);
}

function v140_require(string $source, array $markers, string $label): void
{
    foreach ($markers as $marker) {
        if (strpos($source, $marker) === false) v140_fail($label . ' missing ' . $marker);
    }
}

v140_require($routes, ["'releases/current'", "'release/current'", "'api/v1/'"], 'release route');
v140_require($api, [
    "status=%s",
    "'published'",
    "'apk_size_bytes'",
    "'update_available'",
    "'force_for_client'",
    "stk_project_contract_config('maintenance.enabled'",
    "stk_project_contract_config('app_links.host'",
], 'release API');

$contractKeys = [
    'release.current_version','release.minimum_version','release.force_update',
    'release.download_url','release.apk_sha256','release.notes',
    'maintenance.enabled','maintenance.message','maintenance.expected_end','app_links.host',
];
foreach ($contractKeys as $key) {
    if (strpos($install . $upgrade . $releaseAdmin . $maintenanceAdmin, $key) === false) {
        v140_fail('missing contract config key ' . $key);
    }
}

v140_require($install . $upgrade, [
    'minimum_version_name','apk_size_bytes','published_at','idx_status_version',
    "'schema_version','14001'",
], 'V1.4 migration');
if (strpos($upgrade, 'published_at=IF') !== false) v140_fail('upgrade contains a query rejected by Discuz safecheck');
v140_require($releaseAdmin, [
    '(?:-(?:alpha|beta|rc)\\.\\d+)?',
    'stk_project_release_download_hosts()',
    'version_compare($minimumName, $versionName',
    "['draft', 'published', 'retired']",
    "stk_project_admin_audit('release'",
], 'release admin');
v140_require($maintenanceAdmin, [
    "stk_project_set_contract_config('maintenance.enabled'",
    "stk_project_set_contract_config('app_links.host'",
    "stk_project_admin_audit('maintenance'",
], 'maintenance admin');
v140_require($deploymentAdmin, ['verify_peer_name','assetlinks.json','health/ready','stk-download.zz-yihao.com'], 'deployment diagnostics');
v140_require($securityAdmin, ['dev_fake_enabled','display_errors','schema_version','APK SHA-256','部署备份审计'], 'security audit');

require_once $root . '/lib/routes.php';
$route = stk_project_normalize_route('api/v1/releases/current', 'GET');
if ($route['resource'] !== 'release/current' || $route['method'] !== 'GET') v140_fail('canonical release route is invalid');

echo "PASS: v140_contract_test" . PHP_EOL;
