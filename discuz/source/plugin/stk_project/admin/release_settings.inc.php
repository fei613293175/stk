<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';

if (submitcheck('releasesubmit')) {
    $versionName = trim((string) ($_POST['version_name'] ?? ''));
    $versionCode = (int) ($_POST['version_code'] ?? 0);
    $minimumName = trim((string) ($_POST['minimum_version_name'] ?? ''));
    $minimumCode = (int) ($_POST['minimum_version_code'] ?? 0);
    $apkUrl = trim((string) ($_POST['apk_url'] ?? ''));
    $apkSizeBytes = max(0, (int) ($_POST['apk_size_bytes'] ?? 0));
    $sha = strtolower(trim((string) ($_POST['apk_sha256'] ?? '')));
    $mandatory = !empty($_POST['mandatory']) ? 1 : 0;
    $status = trim((string) ($_POST['status'] ?? 'draft'));
    $notes = array_values(array_filter(array_map('trim', preg_split('/\r?\n/', (string) ($_POST['notes'] ?? '')))));
    $versionPattern = '/^\d+\.\d+\.\d+(?:-(?:alpha|beta|rc)\.\d+)?$/i';

    if (!preg_match($versionPattern, $versionName) || !preg_match($versionPattern, $minimumName)) {
        cpmsg('当前版本和最低版本必须是兼容 Beta 的语义版本号', '', 'error');
    }
    if ($versionCode < 1 || $minimumCode < 1 || $minimumCode > $versionCode || version_compare($minimumName, $versionName, '>')) {
        cpmsg('最低版本不能高于当前发布版本，版本代码必须为正整数', '', 'error');
    }
    if (!filter_var($apkUrl, FILTER_VALIDATE_URL) || strtolower((string) parse_url($apkUrl, PHP_URL_SCHEME)) !== 'https') {
        cpmsg('APK 地址必须是有效 HTTPS URL', '', 'error');
    }
    $downloadHost = strtolower((string) parse_url($apkUrl, PHP_URL_HOST));
    if (!in_array($downloadHost, stk_project_release_download_hosts(), true)) {
        cpmsg('APK 下载域名不在发布白名单', '', 'error');
    }
    if (!preg_match('/^[a-f0-9]{64}$/', $sha)) cpmsg('APK SHA-256 格式错误', '', 'error');
    if ($apkSizeBytes > 200 * 1024 * 1024) cpmsg('APK 大小超过 200 MB 安全上限', '', 'error');
    if (!in_array($status, ['draft', 'published', 'retired'], true)) cpmsg('发布状态无效', '', 'error');
    if ($status === 'published' && $apkSizeBytes < 1) cpmsg('已发布版本必须填写 APK 大小', '', 'error');

    if ($status === 'published') {
        DB::query('UPDATE %t SET enabled=0,status=%s,updated_at=%d WHERE enabled=1 AND status=%s AND version_code<>%d', ['stk_release','retired',TIMESTAMP,'published',$versionCode]);
    }
    $enabled = $status === 'published' ? 1 : 0;
    $publishedAt = $status === 'published' ? TIMESTAMP : 0;
    DB::query(
        'INSERT INTO %t (version_name,version_code,minimum_version_name,minimum_version_code,apk_url,apk_sha256,apk_size_bytes,mandatory,notes,status,enabled,published_at,created_at,updated_at) VALUES (%s,%d,%s,%d,%s,%s,%d,%d,%s,%s,%d,%d,%d,%d) ON DUPLICATE KEY UPDATE version_name=VALUES(version_name),minimum_version_name=VALUES(minimum_version_name),minimum_version_code=VALUES(minimum_version_code),apk_url=VALUES(apk_url),apk_sha256=VALUES(apk_sha256),apk_size_bytes=VALUES(apk_size_bytes),mandatory=VALUES(mandatory),notes=VALUES(notes),status=VALUES(status),enabled=VALUES(enabled),published_at=VALUES(published_at),updated_at=VALUES(updated_at)',
        ['stk_release',$versionName,$versionCode,$minimumName,$minimumCode,$apkUrl,$sha,$apkSizeBytes,$mandatory,json_encode($notes, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),$status,$enabled,$publishedAt,TIMESTAMP,TIMESTAMP]
    );
    if ($status === 'published') {
        foreach ([
            'release.current_version'=>$versionName,
            'release.minimum_version'=>$minimumName,
            'release.force_update'=>(string) $mandatory,
            'release.download_url'=>$apkUrl,
            'release.apk_sha256'=>$sha,
            'release.notes'=>implode("\n", $notes),
        ] as $key=>$value) stk_project_set_contract_config($key, $value);
    }
    stk_project_admin_audit('release', 'publish', [
        'version_name'=>$versionName,
        'version_code'=>$versionCode,
        'minimum_version_code'=>$minimumCode,
        'download_host'=>$downloadHost,
        'mandatory'=>(bool) $mandatory,
        'status'=>$status,
    ]);
    cpmsg('Android 版本记录已保存', 'action=plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=release_settings', 'succeed');
}

$latest = DB::fetch_first('SELECT version_name,version_code,minimum_version_name,minimum_version_code,apk_url,apk_size_bytes,mandatory,status FROM %t ORDER BY enabled DESC,version_code DESC LIMIT 1', ['stk_release']);
showformheader('plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=release_settings');
showtableheader('Android 版本发布');
showsetting('当前版本名称', 'version_name', $latest['version_name'] ?? '1.4.0-beta.1', 'text');
showsetting('当前版本代码', 'version_code', (string) ($latest['version_code'] ?? '10401'), 'text');
showsetting('最低版本名称', 'minimum_version_name', $latest['minimum_version_name'] ?? '1.3.0-beta.1', 'text');
showsetting('最低版本代码', 'minimum_version_code', (string) ($latest['minimum_version_code'] ?? '10301'), 'text');
showsetting('APK HTTPS 地址', 'apk_url', $latest['apk_url'] ?? 'https://stk-download.zz-yihao.com/app/STK-1.4.0-beta.1.apk', 'text');
showsetting('APK 大小（字节，可选）', 'apk_size_bytes', (string) ($latest['apk_size_bytes'] ?? '0'), 'text');
showsetting('APK SHA-256', 'apk_sha256', '', 'text');
showsetting('强制更新', 'mandatory', (string) ($latest['mandatory'] ?? '0'), 'radio');
showsetting('发布状态', 'status', $latest['status'] ?? 'draft', 'select', 0, 0, '', '', ['draft'=>'草稿','published'=>'已发布','retired'=>'已停用']);
showsetting('更新说明（每行一条）', 'notes', '', 'textarea');
showsubmit('releasesubmit', '保存');
showtablefooter();
showformfooter();

$rows = DB::fetch_all('SELECT version_name,version_code,minimum_version_code,apk_url,apk_sha256,apk_size_bytes,mandatory,status,enabled,updated_at FROM %t ORDER BY version_code DESC LIMIT 50', ['stk_release']);
showtableheader('版本记录');
showtablerow('', [], ['版本','代码','最低代码','APK','大小','SHA-256','强制','状态','更新']);
foreach ($rows as $row) {
    showtablerow('', [], [
        dhtmlspecialchars($row['version_name']),
        (int) $row['version_code'],
        (int) $row['minimum_version_code'],
        dhtmlspecialchars($row['apk_url']),
        (int) $row['apk_size_bytes'],
        dhtmlspecialchars(substr($row['apk_sha256'], 0, 16).'...'),
        $row['mandatory'] ? '是' : '否',
        dhtmlspecialchars($row['status']),
        dgmdate((int) $row['updated_at']),
    ]);
}
showtablefooter();
