<?php

define('IN_DISCUZ', true);
$root = dirname(__DIR__);

function fail_contract(string $message): void
{
    fwrite(STDERR, 'FAIL: ' . $message . PHP_EOL);
    exit(1);
}

function read_contract_file(string $path): string
{
    $contents = file_get_contents($path);
    if ($contents === false) fail_contract('missing source ' . $path);
    return $contents;
}

function require_markers(string $source, array $markers, string $label): void
{
    foreach ($markers as $contract => $marker) {
        if (strpos($source, $marker) === false) {
            fail_contract($label . ' missing ' . $contract . ' marker ' . $marker);
        }
    }
}

$authRoot = $root . '/source/plugin/stk_auth';
$projectRoot = $root . '/source/plugin/stk_project';
$authInstall = read_contract_file($authRoot . '/install.php');
$authUpgrade = read_contract_file($authRoot . '/upgrade.php');
$authApi = read_contract_file($authRoot . '/api.inc.php');
$authService = read_contract_file($authRoot . '/lib/auth_service.php');
$authSms = read_contract_file($authRoot . '/lib/sms_service.php');
$authResponse = read_contract_file($authRoot . '/lib/response.php');
$projectInstall = read_contract_file($projectRoot . '/install.php');
$projectUpgrade = read_contract_file($projectRoot . '/upgrade.php');
$projectApi = read_contract_file($projectRoot . '/api.inc.php');
$projectUpload = read_contract_file($projectRoot . '/lib/upload.php');
$projectConfig = read_contract_file($projectRoot . '/lib/config.php');
$projectResponse = read_contract_file($projectRoot . '/lib/response.php');
$allSource = $authInstall . $authUpgrade . $authApi . $authService . $authSms . $authResponse
    . $projectInstall . $projectUpgrade . $projectApi . $projectUpload . $projectConfig . $projectResponse;

$entities = [
    'Discuz native user tables' => "common_member",
    'pre_stk_auth_mobile' => 'pre_stk_auth_mobile',
    'pre_stk_auth_sms_code' => 'pre_stk_auth_sms_code',
    'pre_stk_auth_refresh_token' => 'pre_stk_auth_refresh_token',
    'pre_stk_auth_login_log' => 'pre_stk_auth_login_log',
    'pre_stk_security_event' => 'pre_stk_security_event',
    'pre_stk_app_config' => 'pre_stk_app_config',
    'pre_stk_project_category' => 'pre_stk_project_category',
    'pre_stk_project' => 'pre_stk_project',
    'pre_stk_project_image' => 'pre_stk_project_image',
    'pre_stk_project_view_daily' => 'pre_stk_project_view_daily',
    'pre_stk_project_audit_log' => 'pre_stk_project_audit_log',
    'pre_stk_member_status' => 'pre_stk_member_status',
    'pre_stk_wallet_account' => 'pre_stk_wallet_account',
    'pre_stk_prop_catalog' => 'pre_stk_prop_catalog',
    'pre_stk_release' => 'pre_stk_release',
];
if (count($entities) !== 16) fail_contract('canonical entity catalog count changed');
require_markers($allSource, $entities, 'database catalog');

require_once $authRoot . '/lib/routes.php';
require_once $projectRoot . '/lib/routes.php';
$authRoutes = [
    'healthLive' => ['health/live', 'GET', 'health_live'],
    'healthReady' => ['health/ready', 'GET', 'health_ready'],
    'getBootstrap' => ['api/v1/bootstrap', 'GET', 'bootstrap'],
    'getCaptchaChallenge' => ['api/v1/security/captcha', 'GET', 'captcha_create'],
    'verifyCaptcha' => ['api/v1/security/captcha/verify', 'POST', 'captcha_verify'],
    'sendSmsCode' => ['api/v1/auth/sms/send', 'POST', 'sms_send'],
    'loginWithPassword' => ['api/v1/auth/login/password', 'POST', 'password_login'],
    'loginWithSms' => ['api/v1/auth/login/sms', 'POST', 'sms_login'],
    'registerUser' => ['api/v1/auth/register', 'POST', 'register'],
    'refreshToken' => ['api/v1/auth/token/refresh', 'POST', 'refresh'],
    'logout' => ['api/v1/auth/logout', 'POST', 'logout'],
    'getMySummary' => ['api/v1/me/summary', 'GET', 'me_summary'],
    'getLegalDocument' => ['api/v1/legal/user-agreement', 'GET', 'legal_document'],
];
foreach ($authRoutes as $operation => [$path, $method, $action]) {
    $route = stk_auth_normalize_route('', $path, $method);
    if ($route['action'] !== $action || $route['expected_method'] !== $method) {
        fail_contract('API operation ' . $operation . ' is not normalized correctly');
    }
}
$projectRoutes = [
    'listProjectCategories' => ['api/v1/project/categories', 'GET', 'categories', ''],
    'listProjects' => ['api/v1/projects', 'GET', 'projects', ''],
    'getProjectDetail' => ['api/v1/projects/42', 'GET', 'projects/42', ''],
    'recordProjectView' => ['api/v1/projects/42/view', 'POST', 'projects/42/view', ''],
    'uploadProjectImage' => ['api/v1/media/project-images', 'POST', 'upload', ''],
    'deleteUploadedImage' => ['api/v1/media/project-images/7', 'DELETE', 'media/project-images/7', ''],
    'createProject' => ['api/v1/projects', 'POST', 'create', ''],
    'updateProject' => ['api/v1/projects/42', 'PUT', 'update/42', ''],
    'listMyProjects' => ['api/v1/me/projects', 'GET', 'mine', ''],
    'offlineProject' => ['api/v1/projects/42/offline', 'POST', 'mine/42', 'unpublish'],
    'resubmitProject' => ['api/v1/projects/42/resubmit', 'POST', 'resubmit/42', ''],
    'deleteProject' => ['api/v1/projects/42', 'DELETE', 'mine/42', 'delete'],
    'getMyProfile' => ['api/v1/me/profile', 'GET', 'me/profile', ''],
    'getMyMember' => ['api/v1/me/member', 'GET', 'me/member', ''],
    'getMyWallets' => ['api/v1/me/wallets', 'GET', 'me/wallets', ''],
    'getPropDisplay' => ['api/v1/props/display', 'GET', 'props/display', ''],
    'getSupportConfig' => ['api/v1/support', 'GET', 'support', ''],
    'getCurrentRelease' => ['api/v1/releases/current', 'GET', 'release/current', ''],
];
foreach ($projectRoutes as $operation => [$path, $method, $resource, $action]) {
    $route = stk_project_normalize_route($path, $method);
    if ($route['resource'] !== $resource || $route['action'] !== $action) {
        fail_contract('API operation ' . $operation . ' is not normalized correctly');
    }
}
if (count($authRoutes) + count($projectRoutes) !== 31) fail_contract('API catalog count changed');

$adminModules = [
    'stk_auth' => ['overview','settings','sms_settings','captcha_records','users','security_logs','diagnostics'],
    'stk_project' => ['overview','projects','categories','home_settings','contact_policy','review','publish_settings','image_settings','audit_logs','user_projects','me_settings','memberships','wallets','props_settings','support_settings','release_settings','maintenance_settings','deployment_diagnostics','security_audit'],
];
$adminCount = 0;
foreach ($adminModules as $plugin => $modules) {
    $pluginRoot = $root . '/source/plugin/' . $plugin;
    $manifest = json_decode(read_contract_file($pluginRoot . '/discuz_plugin_' . $plugin . '.json'), true);
    $declared = [];
    foreach (($manifest['Data']['plugin']['__modules'] ?? []) as $module) {
        if (($module['type'] ?? '') === '3') $declared[] = $module['name'];
    }
    foreach ($modules as $module) {
        $adminCount++;
        if (!in_array($module, $declared, true)) fail_contract($plugin . ' manifest missing admin module ' . $module);
        if (!is_file($pluginRoot . '/admin/' . $module . '.inc.php')) fail_contract($plugin . ' missing admin implementation ' . $module);
        if (!is_file($pluginRoot . '/' . $module . '.inc.php')) fail_contract($plugin . ' missing Discuz admin wrapper ' . $module);
    }
}
if ($adminCount !== 26) fail_contract('admin catalog count changed');

$configMarkers = [
    'auth.plugin_enabled'=>'api_enabled','auth.allow_password_login'=>'allow_password_login','auth.allow_sms_login'=>'allow_sms_login','auth.allow_register'=>'allow_register','auth.default_login_tab'=>'default_login_tab','auth.password_min_length'=>'password_min_length','auth.password_max_length'=>'password_max_length','auth.login_fail_limit'=>'login_fail_limit','auth.lock_minutes'=>'login_lock_minutes','auth.access_token_minutes'=>'access_token_ttl','auth.refresh_token_days'=>'refresh_token_ttl','captcha.type'=>'imagecreatetruecolor','captcha.expire_seconds'=>'captcha_expire_seconds','captcha.max_attempts'=>'captcha_max_attempts','sms.enabled'=>'sms_enabled','sms.aliyun_access_key_id'=>'sms_access_key_id','sms.aliyun_access_key_secret'=>'sms_access_key_secret','sms.sign_name'=>'sms_sign_name','sms.login_template_code'=>'sms_template_code','sms.code_length'=>'sms_code_length','sms.expire_minutes'=>'sms_expire_minutes','sms.resend_seconds'=>'sms_resend_seconds','sms.phone_hour_limit'=>'sms_phone_hour_limit','sms.phone_day_limit'=>'sms_phone_day_limit','sms.ip_hour_limit'=>'sms_ip_hour_limit','legal.user_agreement_url'=>'legal_user_agreement_url','legal.privacy_url'=>'legal_privacy_url','auth.login_success_route'=>'login_success_route','auth.register_success_route'=>'register_success_route','log.sms_retention_days'=>'sms_retention_days','log.login_retention_days'=>'login_retention_days',
    'project.browse_enabled'=>'browse_enabled','project.page_size'=>'page_size','project.default_sort'=>'default_sort','project.categories_enabled'=>'categories_enabled','project.show_publisher_avatar'=>'show_publisher_avatar','project.show_member_badge'=>'show_member_badge','project.show_view_count'=>'show_view_count','project.show_publish_time'=>'show_publish_time','project.contact_types'=>'contact_types','project.external_url_allowlist'=>'external_url_allowlist','project.empty_text'=>'empty_text','project.default_cover_url'=>'default_cover_url',
    'publish.enabled'=>'publish_enabled','publish.review_mode'=>'review_mode','publish.title_min'=>'title_min','publish.title_max'=>'title_max','publish.summary_min'=>'summary_min','publish.summary_max'=>'summary_max','publish.max_images'=>'max_images','publish.image_max_mb'=>'max_image_bytes','publish.image_formats'=>'allowed_image_types','publish.image_long_edge'=>'image_long_edge','publish.jpeg_quality'=>'jpeg_quality','publish.daily_limit'=>'daily_limit','publish.pending_limit'=>'pending_limit','publish.edit_requires_review'=>'edit_requires_review','publish.user_can_offline'=>'user_can_offline','publish.user_can_delete'=>'user_can_delete','storage.static_base_url'=>'static_base_url',
    'me.show_wallets'=>'show_wallets','me.commission_label'=>'commission_label','me.task_label'=>'task_label','member.show_card'=>'show_member_card','member.title'=>'member_title','member.benefit_discount'=>'member_benefit_discount','member.benefit_rebate'=>'member_benefit_rebate','member.open_button_text'=>'member_open_button_text','props.show_center'=>'show_props_center','props.items'=>'stk_prop_catalog','support.type'=>'support_type','support.value'=>'support_value','support.copy_enabled'=>'support_copy_enabled','placeholder.message'=>'placeholder_message',
    'release.current_version'=>'version_name','release.minimum_version'=>'minimum_version_code','release.force_update'=>'mandatory','release.download_url'=>'apk_url','release.apk_sha256'=>'apk_sha256','release.notes'=>"'notes'",'maintenance.enabled'=>'maintenance_enabled','maintenance.message'=>'maintenance_message','maintenance.expected_end'=>'maintenance_resume_at','app_links.host'=>'app_links_host',
];
if (count($configMarkers) !== 84) fail_contract('configuration catalog count changed');
require_markers($allSource, $configMarkers, 'configuration catalog');

require_markers($allSource, [
    'response request id' => 'request_id',
    'write request id' => 'HTTP_X_STK_REQUEST_ID',
    'write rate limit' => '4290',
    'duplicate write guard' => '4093',
    'token ownership' => 'publisher_uid=%d',
    'temporary upload ownership' => 'owner_uid=%d',
    'soft delete' => 'deleted_at',
    'offline migration' => "'offline', 'unpublished'",
    'auth retention' => 'retention_cleanup',
    'temporary upload cleanup' => 'stk_project_cleanup_uploads',
    'admin audit' => 'stk_project_admin_audit',
], 'security/lifecycle catalog');

if (strpos($authInstall, "('sms_enabled','0'") === false) {
    fail_contract('production install must keep SMS disabled until credentials are configured');
}
if (strpos($projectInstall, "('static_base_url',''") === false) {
    fail_contract('install must retain the main-site static fallback when CDN DNS is absent');
}

echo "PASS: full_spec_contract_test (16 entities, 31 APIs, 26 admin modules, 84 configs)" . PHP_EOL;
