<?php

declare(strict_types=1);

// Run from the production Discuz root. Inspection is the default; cleanup is
// allowed only for the exact withdrawn 10204 acceptance project and asset.
define('CURSCRIPT', 'forum');
$_SERVER['REMOTE_ADDR'] = '127.0.0.1';
$_SERVER['HTTP_HOST'] = 'stk.zz-yihao.com';
$_SERVER['SERVER_NAME'] = 'stk.zz-yihao.com';
define('STK_10204_CLEANUP_REQUESTED', getenv('STK_10204_CLEANUP') === '1');

require rtrim((string) getcwd(), '/\\') . '/source/class/class_core.php';
$discuz = C::app();
$discuz->init();

$ownerUid = 9;
$title = 'ADB输入法 验证中文空格 10204';
$cleanup = STK_10204_CLEANUP_REQUESTED;

function emit10204(string $label, $value): void
{
    echo $label . PHP_EOL;
    echo json_encode($value, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES) . PHP_EOL;
}

$projects = DB::fetch_all(
    'SELECT project_id,publisher_uid,title,summary,status,row_version,cover_url,published_at,deleted_at,created_at,updated_at FROM %t WHERE publisher_uid=%d AND BINARY title=BINARY %s ORDER BY project_id',
    ['stk_project', $ownerUid, $title]
);
$projects = is_array($projects) ? $projects : [];
$projectId = count($projects) === 1 ? (int) $projects[0]['project_id'] : 0;
$images = $projectId > 0
    ? DB::fetch_all(
        'SELECT image_id,project_id,asset_id,image_url,sort_order,mime_type,size_bytes,storage_key FROM %t WHERE project_id=%d ORDER BY sort_order,image_id',
        ['stk_project_image', $projectId]
    )
    : [];
$images = is_array($images) ? $images : [];
$uploads = $projectId > 0
    ? DB::fetch_all(
        'SELECT asset_id,owner_uid,project_id,status,mime_type,size_bytes,storage_key,created_at,updated_at,expires_at FROM %t WHERE owner_uid=%d AND project_id=%d ORDER BY asset_id',
        ['stk_project_upload', $ownerUid, $projectId]
    )
    : [];
$uploads = is_array($uploads) ? $uploads : [];
$auditLogs = $projectId > 0
    ? DB::fetch_all(
        'SELECT audit_id,project_id,operator_uid,action,from_status,to_status,reason,payload_json,created_at FROM %t WHERE project_id=%d ORDER BY audit_id',
        ['stk_project_audit_log', $projectId]
    )
    : [];
$auditLogs = is_array($auditLogs) ? $auditLogs : [];

$fileChecks = [];
foreach ($uploads as $upload) {
    $storageKey = ltrim(str_replace('\\', '/', (string) ($upload['storage_key'] ?? '')), '/');
    $fileChecks[] = [
        'asset_id' => (int) ($upload['asset_id'] ?? 0),
        'storage_key' => $storageKey,
        'file_exists' => $storageKey !== '' && is_file(DISCUZ_ROOT . $storageKey),
    ];
}

emit10204('MODE', $cleanup ? 'cleanup' : 'inspect');
emit10204('PROJECTS', $projects);
emit10204('PROJECT_IMAGES', $images);
emit10204('PROJECT_UPLOADS', $uploads);
emit10204('UPLOAD_FILE_CHECKS', $fileChecks);
emit10204('AUDIT_LOGS', $auditLogs);

if (!$cleanup) exit(0);

if (count($projects) !== 1) {
    fwrite(STDERR, 'Guard failed: expected exactly one matching acceptance project.' . PHP_EOL);
    exit(20);
}
$project = $projects[0];
if (
    $projectId <= 15
    || (int) ($project['publisher_uid'] ?? 0) !== $ownerUid
    || (string) ($project['title'] ?? '') !== $title
    || (string) ($project['status'] ?? '') !== 'deleted'
    || (int) ($project['deleted_at'] ?? 0) <= 0
) {
    fwrite(STDERR, 'Guard failed: project identity or withdrawn state mismatch.' . PHP_EOL);
    exit(21);
}
if (count($images) !== 1 || count($uploads) !== 1) {
    fwrite(STDERR, 'Guard failed: expected exactly one project image and one bound upload.' . PHP_EOL);
    exit(22);
}

$image = $images[0];
$upload = $uploads[0];
$assetId = (int) ($upload['asset_id'] ?? 0);
$storageKey = ltrim(str_replace('\\', '/', (string) ($upload['storage_key'] ?? '')), '/');
if (
    $assetId <= 16
    || (int) ($image['asset_id'] ?? 0) !== $assetId
    || (int) ($image['project_id'] ?? 0) !== $projectId
    || (int) ($upload['owner_uid'] ?? 0) !== $ownerUid
    || (int) ($upload['project_id'] ?? 0) !== $projectId
    || (string) ($upload['status'] ?? '') !== 'bound'
    || (string) ($image['storage_key'] ?? '') !== (string) ($upload['storage_key'] ?? '')
    || strpos($storageKey, 'data/attachment/stk_project/2026/08/20260811') !== 0
) {
    fwrite(STDERR, 'Guard failed: asset identity, binding, or storage path mismatch.' . PHP_EOL);
    exit(23);
}

$productionPath = DISCUZ_ROOT . $storageKey;
if (!is_file($productionPath)) {
    fwrite(STDERR, 'Guard failed: bound acceptance asset file is missing.' . PHP_EOL);
    exit(24);
}
$quarantineDirectory = '/opt/stk-build/stk-v120-cleanup-quarantine';
if (!is_dir($quarantineDirectory) && !mkdir($quarantineDirectory, 0700, true) && !is_dir($quarantineDirectory)) {
    fwrite(STDERR, 'Could not create cleanup quarantine.' . PHP_EOL);
    exit(25);
}
$quarantinePath = $quarantineDirectory . '/asset-' . $assetId . '-' . basename($storageKey);
if (is_file($quarantinePath) || !rename($productionPath, $quarantinePath)) {
    fwrite(STDERR, 'Could not quarantine the acceptance asset.' . PHP_EOL);
    exit(26);
}

DB::query('START TRANSACTION');
try {
    DB::delete('stk_project_image', ['project_id' => $projectId, 'asset_id' => $assetId]);
    if ((int) DB::affected_rows() !== 1) throw new RuntimeException('Project image cleanup lost.');
    DB::delete('stk_project_upload', [
        'asset_id' => $assetId,
        'owner_uid' => $ownerUid,
        'project_id' => $projectId,
        'status' => 'bound',
    ]);
    if ((int) DB::affected_rows() !== 1) throw new RuntimeException('Bound upload cleanup lost.');
    DB::update('stk_project', ['cover_url' => '', 'updated_at' => TIMESTAMP], [
        'project_id' => $projectId,
        'publisher_uid' => $ownerUid,
        'status' => 'deleted',
    ]);
    if ((int) DB::affected_rows() !== 1) throw new RuntimeException('Project cover cleanup lost.');
    DB::insert('stk_project_audit_log', [
        'project_id' => $projectId,
        'operator_uid' => $ownerUid,
        'action' => 'cleanup_acceptance_assets',
        'from_status' => 'deleted',
        'to_status' => 'deleted',
        'reason' => 'STK V1.2.0 10204 real-device acceptance asset cleanup',
        'payload_json' => json_encode([
            'asset_id' => $assetId,
            'mode' => 'quarantine_asset_keep_project_audit',
        ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
        'created_at' => TIMESTAMP,
    ]);
    DB::query('COMMIT');
} catch (Throwable $error) {
    DB::query('ROLLBACK');
    if (is_file($quarantinePath) && !is_file($productionPath)) rename($quarantinePath, $productionPath);
    fwrite(STDERR, $error->getMessage() . PHP_EOL);
    exit(27);
}

emit10204('CLEANUP_RESULT', [
    'project_id' => $projectId,
    'asset_id' => $assetId,
    'project_images_remaining' => (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE project_id=%d', ['stk_project_image', $projectId]),
    'project_uploads_remaining' => (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE asset_id=%d', ['stk_project_upload', $assetId]),
    'project_cover_url' => (string) DB::result_first('SELECT cover_url FROM %t WHERE project_id=%d', ['stk_project', $projectId]),
    'production_file_exists' => is_file($productionPath),
    'quarantine_path' => $quarantinePath,
    'quarantine_file_exists' => is_file($quarantinePath),
]);
