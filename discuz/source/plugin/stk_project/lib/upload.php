<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

function stk_project_upload_root(): string
{
    return rtrim(DISCUZ_ROOT, '/\\') . '/data/attachment/stk_project';
}

function stk_project_upload_url(string $storageKey): string
{
    global $_G;
    $configured = rtrim(trim(stk_project_config('static_base_url', '')), '/');
    $baseUrl = $configured !== '' ? $configured : rtrim((string) $_G['siteurl'], '/');
    return $baseUrl . '/' . ltrim($storageKey, '/');
}

function stk_project_detect_mime(string $path): string
{
    if (function_exists('finfo_open')) {
        $finfo = finfo_open(FILEINFO_MIME_TYPE);
        $mime = $finfo ? strtolower((string) finfo_file($finfo, $path)) : '';
        if ($finfo) {
            finfo_close($finfo);
        }
        if ($mime !== '') {
            return $mime;
        }
    }
    $imageInfo = @getimagesize($path);
    return is_array($imageInfo) ? strtolower((string) ($imageInfo['mime'] ?? '')) : '';
}

function stk_project_cleanup_uploads(): void
{
    $expired = DB::fetch_all('SELECT storage_key FROM %t WHERE status=%s AND expires_at<%d LIMIT 100', ['stk_project_upload', 'temporary', TIMESTAMP]);
    foreach ($expired as $row) {
        $storageKey = str_replace('\\', '/', (string) $row['storage_key']);
        if (preg_match('#^data/attachment/stk_project/[0-9]{4}/[0-9]{2}/[A-Za-z0-9._-]+$#', $storageKey)) {
            $path = rtrim(DISCUZ_ROOT, '/\\') . '/' . $storageKey;
            if (is_file($path)) @unlink($path);
        }
        DB::delete('stk_project_upload', ['storage_key' => $storageKey, 'status' => 'temporary']);
    }
}

function stk_project_store_upload(array $file, int $ownerUid): array
{
    if ($ownerUid <= 0) stk_project_json(4011, '访问令牌无效或已过期', null, 401);
    stk_project_cleanup_uploads();
    if (($file['error'] ?? UPLOAD_ERR_NO_FILE) !== UPLOAD_ERR_OK || empty($file['tmp_name']) || !is_uploaded_file($file['tmp_name'])) {
        stk_project_json(4001, '图片上传失败', null, 400);
    }
    $size = (int) ($file['size'] ?? 0);
    $maxBytes = max(1024, (int) stk_project_config('max_image_bytes', '10485760'));
    if ($size < 1 || $size > $maxBytes) {
        stk_project_json(4001, '图片大小不符合限制', null, 400);
    }

    $mime = stk_project_detect_mime($file['tmp_name']);
    $allowed = array_filter(array_map('trim', explode(',', stk_project_config('allowed_image_types', 'image/jpeg,image/png,image/webp'))));
    $extensions = ['image/jpeg' => 'jpg', 'image/png' => 'png', 'image/webp' => 'webp'];
    $dimensions = @getimagesize($file['tmp_name']);
    $longEdge = max(640, (int) stk_project_config('image_long_edge', '1920'));
    if (!in_array($mime, $allowed, true) || !isset($extensions[$mime]) || $dimensions === false || max((int) $dimensions[0], (int) $dimensions[1]) > $longEdge * 4) {
        stk_project_json(4001, '图片格式不支持', null, 400);
    }

    $relativeDirectory = 'data/attachment/stk_project/' . date('Y/m');
    $directory = rtrim(DISCUZ_ROOT, '/\\') . '/' . $relativeDirectory;
    if (!is_dir($directory) && !mkdir($directory, 0755, true) && !is_dir($directory)) {
        stk_project_json(5003, '图片目录不可写', null, 500);
    }
    $filename = date('YmdHis') . '-' . bin2hex(random_bytes(12)) . '.' . $extensions[$mime];
    $storageKey = $relativeDirectory . '/' . $filename;
    $destination = rtrim(DISCUZ_ROOT, '/\\') . '/' . $storageKey;
    if (!move_uploaded_file($file['tmp_name'], $destination)) {
        stk_project_json(5003, '图片保存失败', null, 500);
    }
    @chmod($destination, 0644);

    $assetId = (int) DB::insert('stk_project_upload', [
        'owner_uid' => $ownerUid,
        'project_id' => 0,
        'storage_key' => $storageKey,
        'mime_type' => $mime,
        'size_bytes' => filesize($destination) ?: $size,
        'status' => 'temporary',
        'expires_at' => TIMESTAMP + 86400,
        'created_at' => TIMESTAMP,
        'updated_at' => TIMESTAMP,
    ], true);

    return [
        'asset_id' => $assetId,
        'storage_key' => $storageKey,
        'url' => stk_project_upload_url($storageKey),
        'mime_type' => $mime,
        'size_bytes' => filesize($destination) ?: $size,
    ];
}

function stk_project_resolve_image(string $storageKey, int $ownerUid): ?array
{
    $storageKey = str_replace('\\', '/', trim($storageKey));
    if (!preg_match('#^data/attachment/stk_project/[0-9]{4}/[0-9]{2}/[A-Za-z0-9._-]+$#', $storageKey)) {
        return null;
    }
    $path = rtrim(DISCUZ_ROOT, '/\\') . '/' . $storageKey;
    $root = realpath(stk_project_upload_root());
    $real = realpath($path);
    if ($root === false || $real === false || strpos(str_replace('\\', '/', $real), str_replace('\\', '/', $root) . '/') !== 0) {
        return null;
    }
    $asset = DB::fetch_first(
        'SELECT asset_id,status,expires_at FROM %t WHERE storage_key=%s AND owner_uid=%d AND (status=%s OR status=%s)',
        ['stk_project_upload', $storageKey, $ownerUid, 'temporary', 'bound']
    );
    if (!$asset || ($asset['status'] === 'temporary' && (int) $asset['expires_at'] < TIMESTAMP)) return null;
    $mime = stk_project_detect_mime($real);
    $dimensions = @getimagesize($real) ?: [0, 0];
    return [
        'asset_id' => (int) $asset['asset_id'],
        'storage_key' => $storageKey,
        'url' => stk_project_upload_url($storageKey),
        'mime_type' => $mime,
        'size_bytes' => filesize($real) ?: 0,
        'width' => (int) $dimensions[0],
        'height' => (int) $dimensions[1],
    ];
}

function stk_project_bind_uploads(array $images, int $ownerUid, int $projectId): void
{
    foreach ($images as $image) {
        DB::update('stk_project_upload', [
            'project_id' => $projectId,
            'status' => 'bound',
            'expires_at' => 0,
            'updated_at' => TIMESTAMP,
        ], ['storage_key' => $image['storage_key'], 'owner_uid' => $ownerUid]);
    }
}
