<?php

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    fwrite(STDERR, 'CLI only.' . PHP_EOL);
    exit(2);
}
if (getenv('STK_V140_APPLY') !== '1') {
    fwrite(STDERR, 'Set STK_V140_APPLY=1 to run the migration.' . PHP_EOL);
    exit(3);
}

$stkV140DiscuzRoot = rtrim((string) getenv('STK_DISCUZ_ROOT'), '/\\');
if ($stkV140DiscuzRoot !== '/www/wwwroot/stk_zz_yihao_com' || !is_file($stkV140DiscuzRoot . '/source/class/class_core.php')) {
    fwrite(STDERR, 'STK_DISCUZ_ROOT is not the guarded production root.' . PHP_EOL);
    exit(6);
}

define('CURSCRIPT', 'forum');
$_SERVER['REMOTE_ADDR'] = '127.0.0.1';
$_SERVER['HTTP_HOST'] = 'stk.zz-yihao.com';
$_SERVER['SERVER_NAME'] = 'stk.zz-yihao.com';
chdir($stkV140DiscuzRoot);
require $stkV140DiscuzRoot . '/source/class/class_core.php';
$discuz = C::app();
$discuz->init();
require_once DISCUZ_ROOT . 'source/function/function_plugin.php';

// Discuz clears non-whitelisted globals during bootstrap; initialize deployment inputs afterward.
$versionName = '1.4.0-beta.1';
$versionCode = 10401;
$minimumName = '1.3.0-beta.1';
$minimumCode = 10301;
$apkUrl = trim((string) getenv('STK_V140_APK_URL'));
$apkSha256 = strtolower(trim((string) getenv('STK_V140_APK_SHA256')));
$apkSizeBytes = (int) getenv('STK_V140_APK_SIZE_BYTES');
$downloadHost = strtolower((string) parse_url($apkUrl, PHP_URL_HOST));
if (strtolower((string) parse_url($apkUrl, PHP_URL_SCHEME)) !== 'https' || !in_array($downloadHost, ['stk-download.zz-yihao.com','stk.zz-yihao.com'], true)) {
    fwrite(STDERR, 'STK_V140_APK_URL must use an approved HTTPS download host.' . PHP_EOL);
    exit(4);
}
if (!preg_match('/^[a-f0-9]{64}$/', $apkSha256) || $apkSizeBytes < 1 || $apkSizeBytes > 200 * 1024 * 1024) {
    fwrite(STDERR, 'STK_V140_APK_SHA256 or STK_V140_APK_SIZE_BYTES is invalid.' . PHP_EOL);
    exit(5);
}

$finish = false;
require DISCUZ_ROOT . 'source/plugin/stk_project/upgrade.php';
if ($finish !== true) {
    fwrite(STDERR, 'Plugin migration did not report completion.' . PHP_EOL);
    exit(10);
}
require_once DISCUZ_ROOT . 'source/plugin/stk_project/lib/config.php';

try {
    DB::query('START TRANSACTION');
    DB::query('UPDATE %t SET enabled=0,status=%s,updated_at=%d WHERE enabled=1 AND version_code<>%d', ['stk_release','retired',TIMESTAMP,$versionCode]);
    $notes = ['完成可选与强制更新弹层','完成 APK 安全下载、SHA-256 校验与系统安装器回退','完成维护模式、App Links 与生产诊断'];
    DB::query(
        'INSERT INTO %t (version_name,version_code,minimum_version_name,minimum_version_code,apk_url,apk_sha256,apk_size_bytes,mandatory,notes,status,enabled,published_at,created_at,updated_at) VALUES (%s,%d,%s,%d,%s,%s,%d,0,%s,%s,1,%d,%d,%d) ON DUPLICATE KEY UPDATE version_name=VALUES(version_name),minimum_version_name=VALUES(minimum_version_name),minimum_version_code=VALUES(minimum_version_code),apk_url=VALUES(apk_url),apk_sha256=VALUES(apk_sha256),apk_size_bytes=VALUES(apk_size_bytes),mandatory=0,notes=VALUES(notes),status=VALUES(status),enabled=1,published_at=VALUES(published_at),updated_at=VALUES(updated_at)',
        ['stk_release',$versionName,$versionCode,$minimumName,$minimumCode,$apkUrl,$apkSha256,$apkSizeBytes,json_encode($notes, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),'published',TIMESTAMP,TIMESTAMP,TIMESTAMP]
    );
    foreach ([
        'release.current_version'=>$versionName,
        'release.minimum_version'=>$minimumName,
        'release.force_update'=>'0',
        'release.download_url'=>$apkUrl,
        'release.apk_sha256'=>$apkSha256,
        'release.notes'=>implode("\n", $notes),
        'maintenance.enabled'=>'0',
        'maintenance.message'=>'系统维护中，请稍后再试。',
        'maintenance.expected_end'=>'',
        'app_links.host'=>'stk.zz-yihao.com',
    ] as $key=>$value) stk_project_set_contract_config($key, $value);
    stk_project_admin_audit('release', 'deployment_publish', [
        'version_name'=>$versionName,
        'version_code'=>$versionCode,
        'minimum_version_code'=>$minimumCode,
        'download_host'=>$downloadHost,
        'apk_size_bytes'=>$apkSizeBytes,
    ]);
    DB::query('COMMIT');
} catch (Throwable $error) {
    DB::query('ROLLBACK');
    fwrite(STDERR, 'Migration failed: ' . $error->getMessage() . PHP_EOL);
    exit(11);
}

foreach ([
    'minimum_version_name','apk_size_bytes','status','published_at',
] as $column) {
    if (!DB::fetch_first('SHOW COLUMNS FROM %t LIKE %s', ['stk_release',$column])) {
        fwrite(STDERR, 'Required release column is missing: ' . $column . PHP_EOL);
        exit(12);
    }
}
$contractConfigCount = 0;
foreach ([
    'release.current_version','release.minimum_version','release.force_update','release.download_url','release.apk_sha256','release.notes',
    'maintenance.enabled','maintenance.message','maintenance.expected_end','app_links.host',
] as $requiredConfigKey) {
    $contractConfigCount += (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE config_key=%s', ['stk_app_config',$requiredConfigKey]);
}
$result = [
    'schema_version'=>stk_project_config('schema_version', ''),
    'release'=>DB::fetch_first('SELECT version_name,version_code,minimum_version_code,apk_url,apk_sha256,apk_size_bytes,mandatory,status,enabled FROM %t WHERE version_code=%d', ['stk_release',$versionCode]),
    'contract_config_count'=>$contractConfigCount,
];
if (
    $result['schema_version'] !== '14001' ||
    $result['contract_config_count'] !== 10 ||
    ($result['release']['version_name'] ?? '') !== $versionName ||
    (int) ($result['release']['enabled'] ?? 0) !== 1 ||
    ($result['release']['status'] ?? '') !== 'published' ||
    !hash_equals($apkSha256, strtolower((string) ($result['release']['apk_sha256'] ?? '')))
) {
    fwrite(STDERR, 'V1.4.0 migration verification failed.' . PHP_EOL);
    exit(13);
}
echo json_encode($result, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES) . PHP_EOL;
