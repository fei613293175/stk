<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }

final class StkProjectService {
    private static function settingInt(string $key, int $default, int $min, int $max): int {
        global $_G;
        $settings = $_G['cache']['plugin']['stk_project'] ?? [];
        $value = is_array($settings) ? (string)($settings[$key] ?? $default) : (string)$default;
        return preg_match('/^[0-9]+$/', $value) ? max($min, min($max, (int)$value)) : $default;
    }
    private static function settingString(string $key, string $default): string {
        global $_G;
        $settings = $_G['cache']['plugin']['stk_project'] ?? [];
        return is_array($settings) && isset($settings[$key]) ? trim((string)$settings[$key]) : $default;
    }

    public static function categories(bool $includeAll): array {
        if (!class_exists('DB')) { throw new StkApiException(503, 'PROJECT_CATEGORY_UNAVAILABLE', '项目分类暂不可用'); }
        $where = $includeAll ? 'deleted_at IS NULL' : 'enabled=1 AND deleted_at IS NULL';
        $rows = DB::fetch_all('SELECT category_id,name,icon_asset,sort_order FROM ' . DB::table('stk_project_category') . ' WHERE ' . $where . ' ORDER BY sort_order ASC,category_id ASC');
        $items = [];
        foreach ($rows as $row) { $items[] = ['id' => (int)$row['category_id'], 'name' => (string)$row['name'], 'icon_url' => (string)($row['icon_asset'] ?? ''), 'sort_order' => (int)$row['sort_order']]; }
        return ['categories' => $items];
    }

    public static function list(array $query): array {
        if (!class_exists('DB')) { throw new StkApiException(503, 'PROJECT_SERVICE_UNAVAILABLE', '项目服务暂不可用'); }
        $limit = max(1, min(30, (int)($query['limit'] ?? self::settingInt('home_page_size', 10, 5, 30))));
        $cursor = max(0, (int)($query['cursor'] ?? 0));
        $categoryId = max(0, (int)($query['category_id'] ?? 0));
        $keyword = trim((string)($query['keyword'] ?? ''));
        $sort = (string)($query['sort'] ?? self::settingString('home_default_sort', 'latest'));
        if (!in_array($sort, ['latest', 'recommended'], true)) { throw new StkApiException(422, 'PROJECT_QUERY_INVALID', '项目查询参数错误'); }

        $where = ['p.status=%s', 'p.deleted_at IS NULL'];
        $args = ['published'];
        if ($cursor > 0) { $where[] = 'p.project_id<%d'; $args[] = $cursor; }
        if ($categoryId > 0) { $where[] = 'p.category_id=%d'; $args[] = $categoryId; }
        if ($keyword !== '') { $where[] = '(p.title LIKE %s OR p.summary LIKE %s)'; $like = '%' . $keyword . '%'; $args[] = $like; $args[] = $like; }
        $order = $sort === 'latest' ? 'p.published_at DESC,p.project_id DESC' : 'p.recommended DESC,p.recommendation_weight DESC,p.published_at DESC,p.project_id DESC';
        $sql = 'SELECT p.project_id,p.category_id,p.title,p.summary,p.recommended,p.view_count,p.cover_asset_id,p.published_at,c.name AS category_name FROM ' . DB::table('stk_project') . ' p LEFT JOIN ' . DB::table('stk_project_category') . ' c ON c.category_id=p.category_id WHERE ' . implode(' AND ', $where) . ' ORDER BY ' . $order . ' LIMIT ' . ($limit + 1);
        $rows = DB::fetch_all($sql, $args);
        $hasMore = count($rows) > $limit;
        if ($hasMore) { array_pop($rows); }
        $items = [];
        foreach ($rows as $row) { $items[] = self::summary($row); }
        $last = end($rows);
        return ['items' => $items, 'next_cursor' => $hasMore && $last ? (string)$last['project_id'] : null, 'has_more' => $hasMore, 'applied_filters' => ['keyword' => $keyword, 'category_id' => $categoryId ?: null, 'sort' => $sort]];
    }

    public static function detail(int $projectId, int $viewerUid): array {
        if ($projectId <= 0 || !class_exists('DB')) { throw new StkApiException(404, 'PROJECT_NOT_FOUND', '项目不存在'); }
        $row = DB::fetch_first('SELECT p.*,c.name AS category_name,m.username AS publisher_name FROM ' . DB::table('stk_project') . ' p LEFT JOIN ' . DB::table('stk_project_category') . ' c ON c.category_id=p.category_id LEFT JOIN ' . DB::table('common_member') . ' m ON m.uid=p.uid WHERE p.project_id=%d AND p.deleted_at IS NULL LIMIT 1', [$projectId]);
        if (!$row) { throw new StkApiException(404, 'PROJECT_NOT_FOUND', '项目不存在'); }
        if ((string)$row['status'] !== 'published' && (int)$row['uid'] !== $viewerUid) { throw new StkApiException(403, 'PROJECT_NOT_VISIBLE', '项目当前不可查看'); }
        return array_merge(self::summary($row), [
            'content' => (string)$row['summary'],
            'images' => [],
            'publisher' => ['uid' => (int)$row['uid'], 'display_name' => (string)($row['publisher_name'] ?? '商推客用户'), 'avatar_url' => ''],
            'contact_capability' => ['available' => false, 'type' => (string)$row['contact_type'], 'display_value' => null],
            'owner_actions' => (int)$row['uid'] === $viewerUid ? ['can_edit' => false, 'can_manage' => false] : ['can_edit' => false, 'can_manage' => false],
        ]);
    }

    public static function recordView(int $projectId, int $viewerUid, string $sessionId): array {
        self::detail($projectId, $viewerUid);
        if ($sessionId === '') { throw new StkApiException(422, 'SYS_REQUEST_INVALID', '请求参数错误'); }
        $window = self::settingInt('detail_view_dedupe_seconds', 1800, 60, 86400);
        $bucket = (string)floor(time() / $window) . '-' . substr(hash('sha256', $sessionId), 0, 12);
        $now = gmdate('Y-m-d H:i:s');
        $counted = false;
        try {
            DB::insert('stk_project_view', ['project_id' => $projectId, 'viewer_uid' => $viewerUid, 'bucket_key' => $bucket, 'created_at' => $now]);
            $counted = true;
            DB::query('UPDATE ' . DB::table('stk_project') . ' SET view_count=view_count+1 WHERE project_id=%d', [$projectId]);
            DB::query('INSERT INTO ' . DB::table('stk_project_view_daily') . ' (project_id,date_key,view_count) VALUES (%d,%s,1) ON DUPLICATE KEY UPDATE view_count=view_count+1', [$projectId, gmdate('Y-m-d')]);
        } catch (Throwable $error) {
            $counted = false;
        }
        $row = DB::fetch_first('SELECT view_count FROM ' . DB::table('stk_project') . ' WHERE project_id=%d LIMIT 1', [$projectId]);
        return ['counted' => $counted, 'view_count' => (int)($row['view_count'] ?? 0)];
    }

    private static function summary(array $row): array {
        return ['project_id' => (int)$row['project_id'], 'title' => (string)$row['title'], 'summary' => (string)$row['summary'], 'category' => ['id' => (int)($row['category_id'] ?? 0), 'name' => (string)($row['category_name'] ?? '')], 'recommended' => (bool)$row['recommended'], 'view_count' => (int)$row['view_count'], 'cover_url' => '', 'published_at' => (string)($row['published_at'] ?? '')];
    }
}
