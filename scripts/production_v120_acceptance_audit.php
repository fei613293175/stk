<?php

declare(strict_types=1);

// Run from the Discuz root. The default mode is read-only; --cleanup is guarded by
// the exact owner, project IDs, and acceptance marker used by this QA run.
define('CURSCRIPT', 'forum');
$_SERVER['REMOTE_ADDR'] = '127.0.0.1';
$_SERVER['HTTP_HOST'] = 'stk.zz-yihao.com';
$_SERVER['SERVER_NAME'] = 'stk.zz-yihao.com';
define('STK_V120_CLEANUP_REQUESTED', getenv('STK_V120_CLEANUP') === '1');

require rtrim((string) getcwd(), '/\\') . '/source/class/class_core.php';
$discuz = C::app();
$discuz->init();

$ownerUid = 9;
$projectIds = [11, 12, 13, 14];
$marker = 'STK-V120-ACCEPTANCE-20260811';
$temporaryAssetId = 14;
$temporaryStorageKey = 'data/attachment/stk_project/2026/08/20260811110057-8a0cca57fd6168ef8c3813f4.jpg';
$cleanup = STK_V120_CLEANUP_REQUESTED;

function rows(string $sql, array $params): array
{
    $result = DB::fetch_all($sql, $params);
    return is_array($result) ? $result : [];
}

function tableExists(string $table): bool
{
    return (bool) DB::fetch_first('SHOW TABLES LIKE %s', [DB::table($table)]);
}

function emit(string $label, $value): void
{
    echo $label . PHP_EOL;
    echo json_encode($value, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES) . PHP_EOL;
}

$projects = rows(
    'SELECT project_id,publisher_uid,title,summary,status,rejection_reason,row_version,cover_url,published_at,deleted_at,created_at,updated_at FROM %t WHERE project_id IN (%n) ORDER BY project_id',
    ['stk_project', $projectIds]
);

$images = rows(
    'SELECT image_id,project_id,asset_id,image_url,sort_order,mime_type,size_bytes,storage_key FROM %t WHERE project_id IN (%n) ORDER BY project_id,sort_order,image_id',
    ['stk_project_image', $projectIds]
);

$assetIds = [];
foreach ($images as $image) {
    if ((int) ($image['asset_id'] ?? 0) > 0) $assetIds[] = (int) $image['asset_id'];
}
$assetIds = array_values(array_unique($assetIds));

$uploads = rows(
    'SELECT asset_id,owner_uid,project_id,status,mime_type,size_bytes,storage_key,created_at,expires_at FROM %t WHERE owner_uid=%d ORDER BY asset_id',
    ['stk_project_upload', $ownerUid]
);

$auditLogs = tableExists('stk_project_audit_log')
    ? rows('SELECT * FROM %t WHERE project_id IN (%n) ORDER BY audit_id', ['stk_project_audit_log', $projectIds])
    : [];
$legacyAuditLogs = tableExists('stk_project_audit')
    ? rows('SELECT * FROM %t WHERE project_id IN (%n)', ['stk_project_audit', $projectIds])
    : [];

$fileChecks = [];
foreach ($uploads as $upload) {
    $storageKey = ltrim(str_replace('\\', '/', (string) ($upload['storage_key'] ?? '')), '/');
    $fileChecks[] = [
        'asset_id' => (int) ($upload['asset_id'] ?? 0),
        'project_id' => (int) ($upload['project_id'] ?? 0),
        'status' => (string) ($upload['status'] ?? ''),
        'storage_key' => $storageKey,
        'bound_to_acceptance_project' => in_array((int) ($upload['asset_id'] ?? 0), $assetIds, true),
        'file_exists' => $storageKey !== '' && is_file(DISCUZ_ROOT . $storageKey),
    ];
}

emit('MODE', $cleanup ? 'cleanup' : 'inspect');
emit('PROJECTS_BEFORE', $projects);
emit('PROJECT_IMAGES', $images);
emit('OWNER_UPLOADS', $uploads);
emit('UPLOAD_FILE_CHECKS', $fileChecks);
emit('AUDIT_LOGS_BEFORE', $auditLogs);
emit('LEGACY_AUDIT_LOGS_BEFORE', $legacyAuditLogs);

if (!$cleanup) exit(0);

if (count($projects) !== count($projectIds)) {
    fwrite(STDERR, 'Guard failed: expected exactly four acceptance projects.' . PHP_EOL);
    exit(20);
}

foreach ($projects as $project) {
    $id = (int) ($project['project_id'] ?? 0);
    $uid = (int) ($project['publisher_uid'] ?? 0);
    $haystack = (string) ($project['title'] ?? '') . "\n" . (string) ($project['summary'] ?? '');
    if (!in_array($id, $projectIds, true) || $uid !== $ownerUid || strpos($haystack, $marker) === false) {
        fwrite(STDERR, 'Guard failed for project ' . $id . ': owner or marker mismatch.' . PHP_EOL);
        exit(21);
    }
}

$temporaryUpload = null;
foreach ($uploads as $upload) {
    if ((int) ($upload['asset_id'] ?? 0) === $temporaryAssetId) $temporaryUpload = $upload;
}
if (
    !$temporaryUpload
    || (int) ($temporaryUpload['owner_uid'] ?? 0) !== $ownerUid
    || (int) ($temporaryUpload['project_id'] ?? -1) !== 0
    || (string) ($temporaryUpload['status'] ?? '') !== 'temporary'
    || (string) ($temporaryUpload['storage_key'] ?? '') !== $temporaryStorageKey
) {
    fwrite(STDERR, 'Guard failed: temporary acceptance upload does not match asset 14.' . PHP_EOL);
    exit(23);
}

$temporaryPath = DISCUZ_ROOT . $temporaryStorageKey;
if (!is_file($temporaryPath)) {
    fwrite(STDERR, 'Guard failed: temporary acceptance upload file is missing.' . PHP_EOL);
    exit(24);
}
$quarantineDirectory = '/opt/stk-build/stk-v120-cleanup-quarantine';
if (!is_dir($quarantineDirectory) && !mkdir($quarantineDirectory, 0700, true) && !is_dir($quarantineDirectory)) {
    fwrite(STDERR, 'Could not create cleanup quarantine.' . PHP_EOL);
    exit(25);
}
$quarantinePath = $quarantineDirectory . '/asset-14-' . basename($temporaryStorageKey);
if (!rename($temporaryPath, $quarantinePath)) {
    fwrite(STDERR, 'Could not quarantine temporary acceptance upload.' . PHP_EOL);
    exit(26);
}

$now = TIMESTAMP;
DB::query('START TRANSACTION');
try {
    foreach ($projects as $project) {
        $id = (int) $project['project_id'];
        $from = ((int) $project['deleted_at'] > 0 || (string) $project['status'] === 'deleted')
            ? 'deleted'
            : (string) $project['status'];
        if ($from !== 'deleted') {
            DB::query(
                'UPDATE %t SET status=%s,published_at=0,deleted_at=%d,updated_at=%d,row_version=row_version+1 WHERE project_id=%d AND publisher_uid=%d AND deleted_at=0',
                ['stk_project', 'deleted', $now, $now, $id, $ownerUid]
            );
            if ((int) DB::affected_rows() !== 1) {
                throw new RuntimeException('Cleanup update lost for project ' . $id);
            }
            DB::insert('stk_project_audit_log', [
                'project_id' => $id,
                'operator_uid' => $ownerUid,
                'action' => 'cleanup_acceptance',
                'from_status' => $from,
                'to_status' => 'deleted',
                'reason' => 'STK V1.2.0 production acceptance cleanup',
                'payload_json' => json_encode(['marker' => $marker, 'mode' => 'soft_delete'], JSON_UNESCAPED_SLASHES),
                'created_at' => $now,
            ]);
        }
    }
    DB::delete('stk_project_upload', ['asset_id' => $temporaryAssetId, 'owner_uid' => $ownerUid]);
    if ((int) DB::affected_rows() !== 1) {
        throw new RuntimeException('Temporary acceptance upload row was not deleted.');
    }
    DB::query('COMMIT');
} catch (Throwable $error) {
    DB::query('ROLLBACK');
    if (is_file($quarantinePath) && !is_file($temporaryPath)) rename($quarantinePath, $temporaryPath);
    fwrite(STDERR, $error->getMessage() . PHP_EOL);
    exit(22);
}

$projectsAfter = rows(
    'SELECT project_id,publisher_uid,title,status,row_version,published_at,deleted_at,updated_at FROM %t WHERE project_id IN (%n) ORDER BY project_id',
    ['stk_project', $projectIds]
);
$auditAfter = rows(
    'SELECT * FROM %t WHERE project_id IN (%n) ORDER BY audit_id',
    ['stk_project_audit_log', $projectIds]
);

emit('PROJECTS_AFTER', $projectsAfter);
emit('AUDIT_LOGS_AFTER', $auditAfter);
emit('PUBLIC_HOME_MATCHES_AFTER', (int) DB::result_first(
    'SELECT COUNT(*) FROM %t WHERE project_id IN (%n) AND status=%s AND deleted_at=0',
    ['stk_project', $projectIds, 'published']
));
emit('TEMPORARY_UPLOAD_AFTER', [
    'asset_id' => $temporaryAssetId,
    'database_rows' => (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE asset_id=%d', ['stk_project_upload', $temporaryAssetId]),
    'production_file_exists' => is_file($temporaryPath),
    'quarantine_path' => $quarantinePath,
    'quarantine_file_exists' => is_file($quarantinePath),
]);
