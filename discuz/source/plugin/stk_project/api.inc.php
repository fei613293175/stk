<?php

if (!defined('IN_DISCUZ')) exit('Access Denied');
require_once __DIR__ . '/lib/response.php';
require_once __DIR__ . '/lib/config.php';
require_once __DIR__ . '/lib/upload.php';
require_once __DIR__ . '/lib/routes.php';

$resource = trim((string) ($_GET['resource'] ?? 'projects'), '/');
$method = strtoupper((string) ($_SERVER['REQUEST_METHOD'] ?? 'GET'));
$raw = file_get_contents('php://input');
$input = [];
if ($raw !== false && trim($raw) !== '' && stripos((string) ($_SERVER['CONTENT_TYPE'] ?? ''), 'application/json') !== false) {
    $input = json_decode($raw, true);
    if (!is_array($input)) stk_project_json(4001, '请求 JSON 格式错误', null, 400);
}

$route = stk_project_normalize_route($resource, $method);
$resource = $route['resource'];
$method = $route['method'];
if ($route['action'] !== '') $_GET['action'] = $route['action'];

function stk_project_require_method(string $actual, string $expected): void
{
    if ($actual !== $expected) stk_project_json(4050, '请求方法不允许', null, 405);
}

function stk_project_contacts(array $input): array
{
    $items = $input['contacts'] ?? [];
    if (!is_array($items)) return [];
    $configured = array_filter(array_map('trim', explode(',', stk_project_config('contact_types', 'phone,wechat,qq,url'))));
    $out = [];
    foreach ($items as $item) {
        if (!is_array($item)) continue;
        $type = trim((string) ($item['type'] ?? ''));
        $value = trim((string) ($item['value'] ?? ''));
        $label = trim((string) ($item['label'] ?? $type));
        if (!in_array($type, $configured, true) || $value === '') continue;
        if ($type === 'url') {
            $scheme = strtolower((string) parse_url($value, PHP_URL_SCHEME));
            $host = strtolower((string) parse_url($value, PHP_URL_HOST));
            $hosts = array_map('strtolower', array_filter(array_map('trim', preg_split('/[\s,]+/', stk_project_config('external_url_allowlist', 'stk.zz-yihao.com')))));
            if ($scheme !== 'https' || !in_array($host, $hosts, true)) stk_project_json(4001, '项目网址必须使用 HTTPS 且域名位于白名单', null, 400);
        }
        $out[] = ['type' => $type, 'value' => substr($value, 0, 500), 'label' => substr($label, 0, 32)];
    }
    return $out;
}

function stk_project_validate_images(array $input, int $uid): array
{
    $images = $input['images'] ?? [];
    if (!is_array($images)) stk_project_json(4001, '图片字段格式错误', null, 400);
    $max = max(1, (int) stk_project_config('max_images', '9'));
    if (count($images) < 1 || count($images) > $max) stk_project_json(4001, '图片数量不符合限制', null, 400);
    $types = array_filter(array_map('trim', explode(',', stk_project_config('allowed_image_types', 'image/jpeg,image/png,image/webp'))));
    $out = [];
    $coverIndex = null;
    foreach (array_values($images) as $index => $image) {
        if (!is_array($image)) stk_project_json(4001, '图片字段格式错误', null, 400);
        $resolved = stk_project_resolve_image((string) ($image['storage_key'] ?? ''), $uid);
        if (!$resolved || !in_array($resolved['mime_type'], $types, true) || $resolved['size_bytes'] < 1 || $resolved['size_bytes'] > (int) stk_project_config('max_image_bytes', '10485760')) {
            stk_project_json(4001, '图片资源不存在、不属于当前用户或不符合限制', null, 400);
        }
        $requestedAssetId = (int) ($image['asset_id'] ?? 0);
        if ($requestedAssetId > 0 && $requestedAssetId !== (int) $resolved['asset_id']) {
            stk_project_json(4001, '图片资源标识与当前上传资产不一致', null, 400);
        }
        if (!empty($image['is_cover'])) {
            if ($coverIndex !== null) stk_project_json(4001, '只能设置一张封面图片', null, 400);
            $coverIndex = $index;
        }
        $out[] = [
            'asset_id' => (int) $resolved['asset_id'],
            'url' => $resolved['url'],
            'alt' => substr(trim((string) ($image['alt_text'] ?? '项目图片')), 0, 120),
            'sort_order' => $index,
            'mime_type' => $resolved['mime_type'],
            'size_bytes' => $resolved['size_bytes'],
            'storage_key' => $resolved['storage_key'],
        ];
    }
    if ($coverIndex !== null && $coverIndex > 0) {
        $cover = $out[$coverIndex];
        array_splice($out, $coverIndex, 1);
        array_unshift($out, $cover);
    }
    foreach ($out as $index => &$image) $image['sort_order'] = $index;
    unset($image);
    return $out;
}

function stk_project_summary_row(array $row): array
{
    $row['published_at'] = ((int) ($row['published_at'] ?? 0) > 0) ? date('Y-m-d', (int) $row['published_at']) : null;
    $row['updated_at'] = ((int) ($row['updated_at'] ?? 0) > 0) ? date('Y-m-d', (int) $row['updated_at']) : null;
    if (isset($row['row_version'])) $row['row_version'] = (int) $row['row_version'];
    return $row;
}

function stk_project_publisher_avatar_url(int $uid): string
{
    if ($uid <= 0 || stk_project_config('show_publisher_avatar', '1') !== '1') return '';
    return function_exists('avatar') ? (string) avatar($uid, 'small', true) : '';
}

function stk_project_owner_actions(array $row): array
{
    $status = (string) ($row['status'] ?? '');
    if ((int) ($row['deleted_at'] ?? 0) > 0 || $status === 'deleted') return [];
    $actions = ['view'];
    if ($status === 'pending' || $status === 'published' || $status === 'rejected' || $status === 'offline' || $status === 'unpublished') $actions[] = 'edit';
    if ($status === 'published' && stk_project_config('user_can_offline', '1') === '1') $actions[] = 'offline';
    if ($status === 'rejected') $actions[] = 'resubmit';
    if ($status !== 'published' && stk_project_config('user_can_delete', '1') === '1') $actions[] = 'delete';
    return array_values(array_unique($actions));
}

function stk_project_owned_row(int $id, int $uid): ?array
{
    $row = DB::fetch_first(
        'SELECT p.project_id AS id,p.title,p.summary,p.category_id,c.name AS category_name,p.status,p.rejection_reason,p.row_version,p.contact_name,p.contacts_json,p.published_at,p.updated_at,p.publisher_uid FROM %t p LEFT JOIN %t c ON c.category_id=p.category_id WHERE p.project_id=%d AND p.publisher_uid=%d AND p.deleted_at=0',
        ['stk_project', 'stk_project_category', $id, $uid]
    );
    return $row ?: null;
}

function stk_project_save_images(int $projectId, int $uid, array $images): void
{
    DB::delete('stk_project_image', ['project_id' => $projectId]);
    foreach ($images as $image) {
        DB::insert('stk_project_image', [
            'project_id' => $projectId,
            'asset_id' => $image['asset_id'],
            'image_url' => $image['url'],
            'alt_text' => $image['alt'],
            'sort_order' => $image['sort_order'],
            'mime_type' => $image['mime_type'],
            'size_bytes' => $image['size_bytes'],
            'storage_key' => $image['storage_key'],
        ]);
    }
    stk_project_bind_uploads($images, $uid, $projectId);
}

function stk_project_current_release(): array
{
    $release = DB::fetch_first('SELECT version_name,version_code,minimum_version_name,minimum_version_code,apk_url,apk_sha256 AS sha256,apk_size_bytes,mandatory,notes,status,published_at,updated_at FROM %t WHERE enabled=1 AND status=%s ORDER BY version_code DESC LIMIT 1', ['stk_release','published']);
    if (!$release) stk_project_json(4040, '暂无可用版本', null, 404);
    $release['version_code'] = (int) $release['version_code'];
    $release['minimum_version_code'] = (int) $release['minimum_version_code'];
    $release['apk_size_bytes'] = (int) $release['apk_size_bytes'];
    $release['mandatory'] = (bool) $release['mandatory'];
    $release['force_update'] = $release['mandatory'];
    $release['current_version'] = $release['version_name'];
    $release['minimum_version'] = $release['minimum_version_name'];
    $release['published_at'] = (int) $release['published_at'] > 0 ? date('c',(int)$release['published_at']) : null;
    $release['updated_at'] = (int) $release['updated_at'] > 0 ? date('c',(int)$release['updated_at']) : null;
    $notes = json_decode((string) $release['notes'], true);
    $release['notes'] = is_array($notes) ? $notes : [];
    $release['maintenance'] = [
        'enabled' => stk_project_contract_config('maintenance.enabled', '0') === '1',
        'message' => stk_project_contract_config('maintenance.message', '系统维护中，请稍后再试。'),
        'resume_at' => stk_project_contract_config('maintenance.expected_end', '') ?: null,
    ];
    $release['app_links_host'] = stk_project_contract_config('app_links.host','stk.zz-yihao.com');
    $clientVersion=(int)($_GET['version_code']??0);
    $release['update_available']=$clientVersion>0&&$clientVersion<$release['version_code'];
    $release['force_for_client']=$clientVersion>0&&($clientVersion<$release['minimum_version_code']||($release['mandatory']&&$clientVersion<$release['version_code']));
    return $release;
}

$transactionStarted = false;
try {
    if ($resource === 'health/live') {
        stk_project_require_method($method, 'GET');
        stk_project_json(0, 'ok', ['plugin' => 'stk_project', 'status' => 'live', 'version' => '1.4.0']);
    }
    if ($resource === 'health/ready') {
        stk_project_require_method($method, 'GET');
        $ready = (int) DB::result_first('SELECT COUNT(*) FROM %t', ['stk_project_config']) > 0;
        stk_project_json($ready ? 0 : 5031, $ready ? 'ok' : '项目插件尚未完成数据库迁移', ['ready' => $ready], $ready ? 200 : 503);
    }

    if ($resource === 'release/current') {
        stk_project_require_method($method, 'GET');
        stk_project_json(0, 'ok', stk_project_current_release());
    }

    if (stk_project_config('browse_enabled', '1') !== '1') stk_project_json(5030, '项目服务已关闭', null, 503);
    stk_project_require_auth();
    $uid = stk_project_auth_uid();
    if ($method !== 'GET') stk_project_write_guard($uid, $resource . ':' . (string) ($_GET['action'] ?? ''));

    if ($resource === 'publishing/config') {
        stk_project_require_method($method, 'GET');
        $labels = ['phone' => '手机', 'wechat' => '微信', 'qq' => 'QQ', 'url' => '网址'];
        $contactTypes = [];
        foreach (array_filter(array_map('trim', explode(',', stk_project_config('contact_types', 'phone,wechat,qq,url')))) as $type) {
            if (isset($labels[$type])) $contactTypes[] = ['wire' => $type, 'label' => $labels[$type]];
        }
        $mimeTypes = array_values(array_intersect(array_filter(array_map('trim', explode(',', stk_project_config('allowed_image_types', 'image/jpeg,image/png,image/webp')))), ['image/jpeg','image/png','image/webp']));
        stk_project_json(0, 'ok', [
            'title_min' => max(1, (int) stk_project_config('title_min', '4')),
            'title_max' => max(4, (int) stk_project_config('title_max', '60')),
            'summary_min' => max(1, (int) stk_project_config('summary_min', '10')),
            'summary_max' => max(10, (int) stk_project_config('summary_max', '1000')),
            'max_images' => max(1, min(12, (int) stk_project_config('max_images', '9'))),
            'max_image_bytes' => max(1048576, (int) stk_project_config('max_image_bytes', '10485760')),
            'image_long_edge' => max(640, min(4096, (int) stk_project_config('image_long_edge', '1920'))),
            'jpeg_quality' => max(50, min(95, (int) stk_project_config('jpeg_quality', '82'))),
            'allowed_image_types' => $mimeTypes,
            'edit_requires_review' => stk_project_config('edit_requires_review', '1') === '1',
            'contact_types' => $contactTypes,
        ]);
    }

    if ($resource === 'upload') {
        stk_project_require_method($method, 'POST');
        if (stk_project_config('publish_enabled', '1') !== '1') stk_project_json(5030, '发布功能已关闭', null, 503);
        if (empty($_FILES['image']) || !is_array($_FILES['image'])) stk_project_json(4001, '缺少图片文件', null, 400);
        stk_project_json(0, 'ok', stk_project_store_upload($_FILES['image'], $uid));
    }

    if (preg_match('#^media/project-images/(\d+)$#', $resource, $imageMatch) && $method === 'DELETE') {
        $asset = DB::fetch_first('SELECT asset_id,storage_key FROM %t WHERE asset_id=%d AND owner_uid=%d AND status=%s', ['stk_project_upload', (int) $imageMatch[1], $uid, 'temporary']);
        if (!$asset) stk_project_json(4040, '临时图片不存在或已绑定', null, 404);
        $path = rtrim(DISCUZ_ROOT, '/\\') . '/' . str_replace('\\', '/', (string) $asset['storage_key']);
        if (is_file($path)) @unlink($path);
        DB::delete('stk_project_upload', ['asset_id' => (int) $asset['asset_id']]);
        stk_project_json(0, 'ok', ['deleted' => true]);
    }

    if ($resource === 'upload/delete') {
        stk_project_require_method($method, 'POST');
        $storageKey = str_replace('\\', '/', trim((string) ($input['storage_key'] ?? '')));
        $asset = DB::fetch_first('SELECT asset_id FROM %t WHERE storage_key=%s AND owner_uid=%d AND status=%s', ['stk_project_upload', $storageKey, $uid, 'temporary']);
        if (!$asset) stk_project_json(4040, '临时图片不存在', null, 404);
        $path = rtrim(DISCUZ_ROOT, '/\\') . '/' . $storageKey;
        if (is_file($path)) @unlink($path);
        DB::delete('stk_project_upload', ['asset_id' => (int) $asset['asset_id']]);
        stk_project_json(0, 'ok', ['deleted' => true]);
    }

    if ($resource === 'categories') {
        stk_project_require_method($method, 'GET');
        if (stk_project_config('categories_enabled', '1') !== '1') stk_project_json(0, 'ok', ['items' => []]);
        $rows = DB::fetch_all('SELECT category_id AS id,name,sort_order AS sort FROM %t WHERE enabled=1 ORDER BY sort_order,category_id', ['stk_project_category']);
        stk_project_json(0, 'ok', ['items' => $rows]);
    }

    if ($resource === 'projects') {
        stk_project_require_method($method, 'GET');
        $q = substr(trim((string) ($_GET['q'] ?? '')), 0, 100);
        $category = trim((string) ($_GET['category_id'] ?? ''));
        $cursorRaw = trim((string) ($_GET['cursor'] ?? ''));
        $size = max(1, min(50, (int) stk_project_config('page_size', '20')));
        $sortMode = stk_project_config('default_sort', 'latest') === 'popular' ? 'popular' : 'latest';
        $where = ['p.status=%s', 'p.deleted_at=0'];
        $params = ['stk_project', 'stk_project_category', 'published'];
        if ($q !== '') {
            $where[] = '(p.title LIKE %s OR p.summary LIKE %s)';
            $like = '%' . $q . '%';
            $params[] = $like;
            $params[] = $like;
        }
        if ($category !== '') { $where[] = 'p.category_id=%s'; $params[] = $category; }
        if ($sortMode === 'popular' && preg_match('/^(\d+):(\d+)$/', $cursorRaw, $cursorParts)) {
            $where[] = '(p.view_count<%d OR (p.view_count=%d AND p.project_id<%d))';
            $params[] = (int) $cursorParts[1]; $params[] = (int) $cursorParts[1]; $params[] = (int) $cursorParts[2];
        } elseif ($sortMode === 'latest' && (int) $cursorRaw > 0) {
            $where[] = 'p.project_id<%d'; $params[] = (int) $cursorRaw;
        }
        $params[] = $size + 1;
        $orderBy = $sortMode === 'popular' ? 'p.view_count DESC,p.project_id DESC' : 'p.project_id DESC';
        $rows = DB::fetch_all('SELECT p.project_id AS id,p.title,p.summary,p.category_id,c.name AS category_name,p.cover_url,p.publisher_name,p.publisher_uid,p.member_label,p.published_at,p.updated_at,p.view_count FROM %t p LEFT JOIN %t c ON c.category_id=p.category_id WHERE ' . implode(' AND ', $where) . ' ORDER BY ' . $orderBy . ' LIMIT %d', $params);
        $more = count($rows) > $size;
        if ($more) array_pop($rows);
        $last = $more ? end($rows) : null;
        $next = $last ? ($sortMode === 'popular' ? ((int) $last['view_count']) . ':' . $last['id'] : (string) $last['id']) : null;
        foreach ($rows as &$row) {
            $row['publisher_avatar_url'] = stk_project_publisher_avatar_url((int) $row['publisher_uid']);
            unset($row['publisher_uid']);
            if ($row['cover_url'] === '') $row['cover_url'] = stk_project_config('default_cover_url', '');
            if (stk_project_config('show_member_badge', '1') !== '1') $row['member_label'] = '';
            if (stk_project_config('show_view_count', '1') !== '1') $row['view_count'] = 0;
            $row = stk_project_summary_row($row);
            if (stk_project_config('show_publish_time', '1') !== '1') $row['published_at'] = null;
        }
        unset($row);
        stk_project_json(0, 'ok', ['items' => $rows, 'next_cursor' => $next, 'empty_text' => stk_project_config('empty_text', '暂无项目'), 'sort' => $sortMode]);
    }

    if (preg_match('#^projects/(\d+)(/view)?$#', $resource, $match)) {
        $projectId = (int) $match[1];
        if (!empty($match[2])) {
            stk_project_require_method($method, 'POST');
            $exists = DB::result_first('SELECT project_id FROM %t WHERE project_id=%d AND status=%s AND deleted_at=0', ['stk_project', $projectId, 'published']);
            if (!$exists) stk_project_json(4040, '项目不存在或已下架', null, 404);
            $date = date('Y-m-d');
            $ip = (string) ($_G['clientip'] ?? $_SERVER['REMOTE_ADDR'] ?? '');
            $agent = substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 500);
            $viewerHash = hash('sha256', $projectId . '|' . $date . '|' . $ip . '|' . $agent . '|' . stk_project_bearer_token());
            DB::query('INSERT IGNORE INTO %t (project_id,view_date,viewer_hash,created_at) VALUES (%d,%s,%s,%d)', ['stk_project_view_daily', $projectId, $date, $viewerHash, TIMESTAMP]);
            $recorded = (bool) DB::result_first('SELECT ROW_COUNT()');
            if ($recorded) DB::query('UPDATE %t SET view_count=view_count+1 WHERE project_id=%d', ['stk_project', $projectId]);
            stk_project_json(0, 'ok', ['recorded' => $recorded]);
        }
        stk_project_require_method($method, 'GET');
        $row = DB::fetch_first('SELECT p.project_id AS id,p.title,p.summary,p.category_id,c.name AS category_name,p.cover_url,p.publisher_name,p.member_label,p.published_at,p.updated_at,p.view_count,p.publisher_uid,p.contacts_json,p.status,p.rejection_reason,p.row_version FROM %t p LEFT JOIN %t c ON c.category_id=p.category_id WHERE p.project_id=%d AND (p.status=%s OR p.publisher_uid=%d) AND p.deleted_at=0', ['stk_project', 'stk_project_category', $projectId, 'published', $uid]);
        if (!$row) stk_project_json(4040, '项目不存在或已下架', null, 404);
        $images = DB::fetch_all('SELECT image_id AS id,asset_id,image_url AS url,alt_text AS alt,sort_order,mime_type,size_bytes,storage_key FROM %t WHERE project_id=%d ORDER BY sort_order,image_id', ['stk_project_image', $projectId]);
        $contacts = json_decode((string) ($row['contacts_json'] ?? '[]'), true);
        unset($row['contacts_json']);
        $publisherUid = (int) $row['publisher_uid'];
        unset($row['publisher_uid']);
        $row['publisher_avatar_url'] = stk_project_publisher_avatar_url($publisherUid);
        if ($row['cover_url'] === '') $row['cover_url'] = stk_project_config('default_cover_url', '');
        if (stk_project_config('show_member_badge', '1') !== '1') $row['member_label'] = '';
        if (stk_project_config('show_view_count', '1') !== '1') $row['view_count'] = 0;
        $row = stk_project_summary_row($row);
        if (stk_project_config('show_publish_time', '1') !== '1') $row['published_at'] = null;
        $allowedExternalHosts = array_values(array_unique(array_filter(array_map('strtolower', array_map('trim', preg_split('/[\s,]+/', stk_project_config('external_url_allowlist', 'stk.zz-yihao.com')))))));
        stk_project_json(0, 'ok', [
            'summary' => $row,
            'images' => $images,
            'contacts' => is_array($contacts) ? $contacts : [],
            'publisher_uid' => $publisherUid,
            'allowed_external_hosts' => $allowedExternalHosts,
            'owner_actions' => $publisherUid === $uid ? stk_project_owner_actions($row) : [],
        ]);
    }

    if ($resource === 'mine') {
        stk_project_require_method($method, 'GET');
        $status = trim((string) ($_GET['status'] ?? ''));
        $cursor = max(0, (int) ($_GET['cursor'] ?? 0));
        $where = ['p.publisher_uid=%d'];
        $params = ['stk_project', 'stk_project_category', $uid];
        if ($status === 'deleted') $where[] = 'p.deleted_at>0'; else $where[] = 'p.deleted_at=0';
        if ($status === 'offline') {
            $where[] = '(p.status=%s OR p.status=%s)';
            $params[] = 'offline';
            $params[] = 'unpublished';
        } elseif ($status !== '') { $where[] = 'p.status=%s'; $params[] = $status; }
        if ($cursor > 0) { $where[] = 'p.project_id<%d'; $params[] = $cursor; }
        $params[] = 11;
        $rows = DB::fetch_all('SELECT p.project_id AS id,p.title,p.summary,p.category_id,c.name AS category_name,p.cover_url,p.status,p.rejection_reason,p.row_version,p.published_at,p.updated_at FROM %t p LEFT JOIN %t c ON c.category_id=p.category_id WHERE ' . implode(' AND ', $where) . ' ORDER BY p.project_id DESC LIMIT %d', $params);
        $more = count($rows) > 10;
        if ($more) array_pop($rows);
        foreach ($rows as &$row) { $row = stk_project_summary_row($row); $row['owner_actions'] = stk_project_owner_actions($row); }
        unset($row);
        $next = $more ? (string) end($rows)['id'] : null;
        $counts = [];
        foreach (['pending','published','rejected','offline'] as $countStatus) {
            $countWhere = 'publisher_uid=%d AND deleted_at=0'; $countParams = [$uid];
            if ($countStatus === 'offline') { $countWhere .= ' AND (status=%s OR status=%s)'; $countParams[] = 'offline'; $countParams[] = 'unpublished'; }
            else { $countWhere .= ' AND status=%s'; $countParams[] = $countStatus; }
            $counts[$countStatus] = (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE ' . $countWhere, array_merge(['stk_project'], $countParams));
        }
        $counts[''] = array_sum($counts);
        stk_project_json(0, 'ok', ['items' => $rows, 'next_cursor' => $next, 'has_more' => $more, 'status_counts' => $counts]);
    }

    if (in_array($resource, ['me/profile', 'me/member', 'me/wallets', 'props/display', 'support'], true)) {
        stk_project_require_method($method, 'GET');
        $profile = DB::fetch_first('SELECT uid,username,status FROM %t WHERE uid=%d', ['common_member', $uid]);
        if (!$profile) stk_project_json(4011, '用户不存在或已注销', null, 401);
        if ((int) ($profile['status'] ?? 0) < 0) stk_project_json(4030, '账户已被禁用', null, 403);

        // V1.3 migration compatibility: old users are completed lazily and idempotently.
        DB::query("INSERT IGNORE INTO %t (uid,status,member_label,level,starts_at,expires_at,updated_at) VALUES (%d,'inactive','普通用户','L1',0,0,%d)", ['stk_member_status', $uid, TIMESTAMP]);
        DB::query('INSERT IGNORE INTO %t (uid,commission_amount,task_amount,task_points,updated_at) VALUES (%d,0,0,0,%d)', ['stk_wallet_account', $uid, TIMESTAMP]);
        $mobile = DB::fetch_first('SELECT mobile FROM %t WHERE uid=%d', ['stk_auth_mobile', $uid]) ?: [];
        $membership = DB::fetch_first('SELECT status,member_label,level,starts_at,expires_at FROM %t WHERE uid=%d', ['stk_member_status', $uid]);
        $balance = DB::fetch_first('SELECT commission_amount,task_amount FROM %t WHERE uid=%d', ['stk_wallet_account', $uid]);
        $items = DB::fetch_all('SELECT prop_id,name AS title,description,icon_url,sort_order FROM %t WHERE enabled=1 ORDER BY sort_order,prop_id', ['stk_prop_catalog']);
        $memberExpiresAt = (int) ($membership['expires_at'] ?? 0);
        $memberStatus = (string) ($membership['status'] ?? 'inactive');
        if ($memberStatus === 'active' && $memberExpiresAt > 0 && $memberExpiresAt < TIMESTAMP) {
            $memberStatus = 'expired';
            DB::query('UPDATE %t SET status=%s,updated_at=%d WHERE uid=%d AND status=%s', ['stk_member_status', 'expired', TIMESTAMP, $uid, 'active']);
        }
        $benefitsEnabled = $memberStatus === 'active';
        $allowedSupportHosts = array_values(array_unique(array_filter(array_map('strtolower', array_map('trim', preg_split('/[\s,]+/', stk_project_config('support_url_allowlist', stk_project_config('external_url_allowlist', 'stk.zz-yihao.com'))))))));
        if ($resource === 'me/profile') {
            $data = [
                'uid' => (int) $uid,
                'username' => (string) $profile['username'],
                'mobile_masked' => preg_replace('/^(\d{3})\d{4}(\d{4})$/', '$1****$2', (string) ($mobile['mobile'] ?? '')),
                'member_label' => (string) ($membership['member_label'] ?? '普通用户'),
                'bio' => stk_project_config('profile_bio', ''),
                'avatar_url' => stk_project_publisher_avatar_url($uid),
                'placeholder_message' => stk_project_config('placeholder_message', '功能筹备中'),
            ];
        } elseif ($resource === 'me/member') {
            $startsAt = (int) ($membership['starts_at'] ?? 0);
            $data = [
                'label' => (string) ($membership['member_label'] ?? '普通用户'),
                'level' => (string) ($membership['level'] ?? 'L1'),
                'starts_at' => $startsAt > 0 ? date('Y-m-d', $startsAt) : null,
                'expires_at' => $memberExpiresAt > 0 ? date('Y-m-d', $memberExpiresAt) : null,
                'status' => $memberStatus,
                'title' => stk_project_config('member_title', '商推客会员'),
                'open_button_text' => stk_project_config('member_open_button_text', '立即开通'),
                'show_card' => stk_project_config('show_member_card', '1') === '1',
                'benefits' => [
                    ['title' => '消费权益', 'description' => stk_project_config('member_benefit_discount', '消费 5 折'), 'enabled' => $benefitsEnabled],
                    ['title' => '推广权益', 'description' => stk_project_config('member_benefit_rebate', '消费返佣 40%'), 'enabled' => $benefitsEnabled],
                ],
            ];
        } elseif ($resource === 'me/wallets') {
            $data = [
                'show_wallets' => stk_project_config('show_wallets', '1') === '1',
                'commission' => [
                    'title' => stk_project_config('commission_label', '佣金账户'),
                    'amount' => number_format((float) ($balance['commission_amount'] ?? 0), 2, '.', ''),
                    'description' => '仅展示账户余额',
                ],
                'tasks' => [
                    'title' => stk_project_config('task_label', '任务账户'),
                    'amount' => number_format((float) ($balance['task_amount'] ?? 0), 2, '.', ''),
                    'description' => '仅展示账户余额',
                ],
            ];
        } elseif ($resource === 'props/display') {
            foreach ($items as &$item) $item['id'] = (string) $item['prop_id'];
            unset($item);
            $data = ['show_center' => stk_project_config('show_props_center', '1') === '1', 'items' => $items];
        } else {
            $data = [
                'type' => stk_project_config('support_type', 'wechat'),
                'label' => stk_project_config('support_label', '在线客服'),
                'value' => stk_project_config('support_value', ''),
                'service_hours' => stk_project_config('support_hours', '工作日 09:00-18:00'),
                'copy_enabled' => stk_project_config('support_copy_enabled', '1') === '1',
                'allowed_url_hosts' => $allowedSupportHosts,
            ];
        }
        stk_project_json(0, 'ok', $data);
    }

    if (preg_match('#^mine/(\d+)$#', $resource, $match)) {
        $projectId = (int) $match[1];
        $row = stk_project_owned_row($projectId, $uid);
        if (!$row) stk_project_json(4040, '项目不存在', null, 404);
        if ($method === 'GET') {
            $images = DB::fetch_all('SELECT image_id AS id,asset_id,image_url AS url,alt_text AS alt,sort_order,mime_type,size_bytes,storage_key FROM %t WHERE project_id=%d ORDER BY sort_order,image_id', ['stk_project_image', $projectId]);
            $contacts = json_decode((string) ($row['contacts_json'] ?? '[]'), true);
            unset($row['publisher_uid'], $row['contacts_json']);
            $row['owner_actions'] = stk_project_owner_actions($row);
            stk_project_json(0, 'ok', ['project' => stk_project_summary_row($row), 'contacts' => is_array($contacts) ? $contacts : [], 'images' => $images]);
        }
        if ($method === 'POST') {
            $action = trim((string) ($_GET['action'] ?? ''));
            if (!in_array($action, ['unpublish', 'delete'], true)) stk_project_json(4050, '请求方法不允许', null, 405);
            if ($action === 'unpublish' && stk_project_config('user_can_offline', '1') !== '1') stk_project_json(4030, '用户下架功能已关闭', null, 403);
            if ($action === 'delete' && stk_project_config('user_can_delete', '1') !== '1') stk_project_json(4030, '用户删除功能已关闭', null, 403);
            $from = (string) $row['status'];
            if ($action === 'delete' && $from === 'published') stk_project_json(4092, '已发布项目请先下架后再删除', null, 409);
            if ($action === 'unpublish' && $from !== 'published') stk_project_json(4091, '只有已发布项目可以下架', null, 409);
            $expectedVersion = (int) ($input['version'] ?? $input['row_version'] ?? 0);
            if ($expectedVersion <= 0 || $expectedVersion !== (int) $row['row_version']) stk_project_json(4091, '项目已被修改，请刷新后重试', null, 409);
            $to = $action === 'unpublish' ? 'offline' : 'deleted';
            DB::query('START TRANSACTION');
            $transactionStarted = true;
            DB::query('UPDATE %t SET status=%s,published_at=0,deleted_at=%d,updated_at=%d,row_version=row_version+1 WHERE project_id=%d AND publisher_uid=%d AND status=%s AND row_version=%d', ['stk_project', $to, $to === 'deleted' ? TIMESTAMP : 0, TIMESTAMP, $projectId, $uid, $from, $expectedVersion]);
            if ((int) DB::affected_rows() !== 1) {
                DB::query('ROLLBACK');
                $transactionStarted = false;
                stk_project_json(4091, '项目状态已变化，请刷新后重试', null, 409);
            }
            stk_project_audit($projectId, $uid, $action, $from, $to);
            DB::query('COMMIT');
            $transactionStarted = false;
            if ($to === 'deleted') stk_project_json(0, 'ok', ['project_id' => $projectId, 'status' => $to]);
            $updated = stk_project_owned_row($projectId, $uid);
            unset($updated['publisher_uid']);
            stk_project_json(0, 'ok', ['project' => stk_project_summary_row($updated)]);
        }
        stk_project_json(4050, '请求方法不允许', null, 405);
    }

    $writeAction = $resource;
    $routeId = 0;
    if (preg_match('#^(update|resubmit)/(\d+)$#', $resource, $routeMatch)) {
        $writeAction = $routeMatch[1];
        $routeId = (int) $routeMatch[2];
    }
    if (in_array($writeAction, ['create', 'update', 'resubmit'], true)) {
        stk_project_require_method($method, 'POST');
        if (stk_project_config('publish_enabled', '1') !== '1') stk_project_json(5030, '发布功能已关闭', null, 503);
        $title = trim((string) ($input['title'] ?? ''));
        $summary = trim((string) ($input['summary'] ?? ''));
        $category = trim((string) ($input['category_id'] ?? ''));
        $titleMin = max(1, (int) stk_project_config('title_min', '4'));
        $titleMax = max($titleMin, (int) stk_project_config('title_max', '60'));
        $summaryMin = max(1, (int) stk_project_config('summary_min', '10'));
        $summaryMax = max($summaryMin, (int) stk_project_config('summary_max', '1000'));
        if (mb_strlen($title) < $titleMin || mb_strlen($title) > $titleMax || mb_strlen($summary) < $summaryMin || mb_strlen($summary) > $summaryMax || $category === '') {
            stk_project_json(4001, '标题、简介或分类不符合发布规则', null, 400);
        }
        if (!DB::result_first('SELECT category_id FROM %t WHERE category_id=%s AND enabled=1', ['stk_project_category', $category])) stk_project_json(4001, '分类不存在', null, 400);
        $images = stk_project_validate_images($input, $uid);
        $contacts = stk_project_contacts($input);
        $contactName = substr(trim((string) ($input['contact_name'] ?? '')), 0, 64);
        if ($contactName === '' || !$contacts) stk_project_json(4001, '请填写联系人和至少一种有效联系方式', null, 400);
        $coverUrl = $images[0]['url'];
        $now = TIMESTAMP;
        $projectId = $routeId > 0 ? $routeId : (int) ($input['id'] ?? 0);
        $targetStatus = stk_project_config('review_mode', 'manual') === 'direct' ? 'published' : 'pending';
        $publishedAt = $targetStatus === 'published' ? $now : 0;
        if ($writeAction === 'create') {
            $dailyCount = (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE publisher_uid=%d AND created_at>%d', ['stk_project', $uid, strtotime('today')]);
            $pendingCount = (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE publisher_uid=%d AND status=%s AND deleted_at=0', ['stk_project', $uid, 'pending']);
            if ($dailyCount >= max(1, (int) stk_project_config('daily_limit', '10'))) stk_project_json(4291, '已达到今日发布上限', null, 429);
            if ($pendingCount >= max(1, (int) stk_project_config('pending_limit', '5'))) stk_project_json(4092, '待审核项目数量已达上限', null, 409);
            DB::query('START TRANSACTION');
            $transactionStarted = true;
            $publisher = DB::fetch_first('SELECT username FROM %t WHERE uid=%d', ['common_member', $uid]) ?: [];
            $membership = DB::fetch_first('SELECT member_label FROM %t WHERE uid=%d', ['stk_member_status', $uid]) ?: [];
            $publisherName = trim((string) ($publisher['username'] ?? ''));
            $memberLabel = trim((string) ($membership['member_label'] ?? ''));
            $projectId = (int) DB::insert('stk_project', [
                'publisher_uid' => $uid, 'title' => $title, 'summary' => $summary, 'category_id' => $category,
                'cover_url' => $coverUrl, 'publisher_name' => $publisherName !== '' ? $publisherName : '用户' . $uid,
                'member_label' => $memberLabel !== '' ? $memberLabel : '普通用户',
                'contact_name' => $contactName,
                'contacts_json' => json_encode($contacts, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
                'status' => $targetStatus, 'row_version' => 1, 'published_at' => $publishedAt, 'created_at' => $now, 'updated_at' => $now,
            ], true);
            $from = '';
        } else {
            $row = stk_project_owned_row($projectId, $uid);
            if (!$row) stk_project_json(4040, '项目不存在', null, 404);
            $expectedVersion = (int) ($input['row_version'] ?? 0);
            if ($expectedVersion <= 0 || $expectedVersion !== (int) $row['row_version']) stk_project_json(4091, '项目已被修改，请刷新后重试', null, 409);
            if ($writeAction === 'resubmit' && $row['status'] !== 'rejected') stk_project_json(4090, '只有已驳回项目可以重提', null, 409);
            $from = (string) $row['status'];
            if ($writeAction === 'update' && $from === 'published' && stk_project_config('edit_requires_review', '1') !== '1') {
                $targetStatus = 'published';
                $publishedAt = $now;
            } else {
                $targetStatus = 'pending';
                $publishedAt = 0;
            }
            DB::query('START TRANSACTION');
            $transactionStarted = true;
            DB::query(
                'UPDATE %t SET title=%s,summary=%s,category_id=%s,cover_url=%s,contact_name=%s,contacts_json=%s,status=%s,rejection_reason=%s,published_at=%d,updated_at=%d,deleted_at=0,row_version=row_version+1 WHERE project_id=%d AND publisher_uid=%d AND row_version=%d',
                ['stk_project', $title, $summary, $category, $coverUrl, $contactName, json_encode($contacts, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES), $targetStatus, '', $publishedAt, $now, $projectId, $uid, $expectedVersion]
            );
            if ((int) DB::affected_rows() !== 1) {
                DB::query('ROLLBACK');
                $transactionStarted = false;
                stk_project_json(4091, '项目已被修改，请刷新后重试', null, 409);
            }
        }
        stk_project_save_images($projectId, $uid, $images);
        stk_project_audit($projectId, $uid, $writeAction, $from, $targetStatus, '', ['image_count' => count($images)]);
        DB::query('COMMIT');
        $transactionStarted = false;
        $updated = stk_project_owned_row($projectId, $uid);
        unset($updated['publisher_uid']);
        stk_project_json(0, 'ok', ['project' => stk_project_summary_row($updated)]);
    }

    stk_project_json(4040, '未知资源', null, 404);
} catch (Throwable $error) {
    if ($transactionStarted) {
        try { DB::query('ROLLBACK'); } catch (Throwable $ignored) {}
    }
    stk_project_json($error->getCode() > 0 ? (int) $error->getCode() : 5000, '服务器内部错误', null, 500);
}
