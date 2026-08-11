<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';

$release = DB::fetch_first('SELECT version_name,apk_url,apk_sha256,status,enabled FROM %t WHERE enabled=1 AND status=%s ORDER BY version_code DESC LIMIT 1', ['stk_release','published']);
$releaseHost = strtolower((string) parse_url((string) ($release['apk_url'] ?? ''), PHP_URL_HOST));
$fakeEnabled = false;
try {
    $fakeEnabled = DB::result_first('SELECT config_value FROM %t WHERE config_key=%s', ['stk_auth_config','dev_fake_enabled']) === '1';
} catch (Throwable $error) {
    $fakeEnabled = true;
}
$globalConfig = rtrim(DISCUZ_ROOT, '/\\').'/config/config_global.php';
$globalPermissions = is_file($globalConfig) ? (fileperms($globalConfig) & 0777) : 0;
$latestBackupAudit = (int) DB::result_first('SELECT created_at FROM %t WHERE (section=%s AND action=%s) OR action=%s ORDER BY audit_id DESC LIMIT 1', ['stk_admin_audit','deployment','backup_created','deployment_config_update']);
$checks = [
    ['开发假数据开关', !$fakeEnabled, $fakeEnabled ? '必须关闭 dev_fake_enabled' : '已关闭'],
    ['PHP 错误回显', strtolower((string) ini_get('display_errors')) !== '1' && strtolower((string) ini_get('display_errors')) !== 'on', ini_get('display_errors') ? '当前值 '.ini_get('display_errors') : '已关闭'],
    ['数据库版本', stk_project_config('schema_version', '') === '14001', '当前 '.stk_project_config('schema_version', '未设置')],
    ['发布记录状态', ($release['status'] ?? '') === 'published' && !empty($release['enabled']), ($release['version_name'] ?? '无发布记录').' / '.($release['status'] ?? 'none')],
    ['APK HTTPS 与白名单', strpos((string) ($release['apk_url'] ?? ''), 'https://') === 0 && in_array($releaseHost, stk_project_release_download_hosts(), true), $releaseHost ?: '未配置'],
    ['APK SHA-256', preg_match('/^[a-f0-9]{64}$/', strtolower((string) ($release['apk_sha256'] ?? ''))) === 1, '仅显示校验结果，不回显完整值'],
    ['App Links 主机', stk_project_contract_config('app_links.host', '') === 'stk.zz-yihao.com', stk_project_contract_config('app_links.host', '未配置')],
    ['全局配置文件权限', $globalPermissions > 0 && ($globalPermissions & 0004) === 0, $globalPermissions > 0 ? decoct($globalPermissions) : '文件不存在'],
    ['部署备份审计', $latestBackupAudit > 0, $latestBackupAudit > 0 ? date('Y-m-d H:i:s', $latestBackupAudit) : '未找到备份审计'],
];

showtableheader('生产安全检查');
showtablerow('', [], ['检查项','结论','证据 / 建议']);
foreach ($checks as [$label, $ok, $detail]) {
    showtablerow('', [], [dhtmlspecialchars($label), $ok ? '通过' : '待处理', dhtmlspecialchars((string) $detail)]);
}
showtablefooter();

$rows = DB::fetch_all('SELECT audit_id,operator_uid,section,action,payload_json,ip,created_at FROM %t ORDER BY audit_id DESC LIMIT 300', ['stk_admin_audit']);
showtableheader('管理操作审计日志');
showtablerow('', [], ['ID','操作人','区域','动作','内容','IP','时间']);
foreach ($rows as $row) {
    showtablerow('', [], [
        (int) $row['audit_id'],
        (int) $row['operator_uid'],
        dhtmlspecialchars($row['section']),
        dhtmlspecialchars($row['action']),
        dhtmlspecialchars(substr($row['payload_json'], 0, 300)),
        dhtmlspecialchars($row['ip']),
        dgmdate((int) $row['created_at']),
    ]);
}
showtablefooter();
