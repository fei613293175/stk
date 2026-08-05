<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
require_once __DIR__ . '/service/ApiResponse.php';

$path = '/' . ltrim((string)($_GET['path'] ?? ''), '/');
if ($path === '/' && isset($_SERVER['REQUEST_URI'])) {
    $path = (string)(parse_url((string)$_SERVER['REQUEST_URI'], PHP_URL_PATH) ?: '/');
}

if ($path === '/healthz') {
    StkApiResponse::send(200, 'OK', 'ok', ['status' => 'live', 'service' => 'stk-api', 'version' => '1.0.0']);
}
if ($path === '/readyz') {
    try {
        if (!class_exists('DB')) { throw new RuntimeException('database unavailable'); }
        DB::fetch_first('SELECT 1 AS ready');
        StkApiResponse::send(200, 'OK', 'ok', ['status' => 'ready', 'service' => 'stk-api', 'version' => '1.0.0']);
    } catch (Throwable $error) {
        StkApiResponse::send(503, 'SYS_UNAVAILABLE', '服务暂不可用', ['status' => 'not_ready']);
    }
}

$authRoutes = [
    '/v1/bootstrap' => 'bootstrap',
    '/v1/auth/captcha/challenges' => 'captcha_challenge',
    '/v1/auth/captcha/verify' => 'captcha_verify',
    '/v1/auth/sms/send' => 'sms_send',
    '/v1/auth/login/password' => 'login_password',
    '/v1/auth/login/sms' => 'login_sms',
    '/v1/auth/register' => 'register',
    '/v1/auth/password/reset' => 'password_reset',
    '/v1/auth/token/refresh' => 'token_refresh',
    '/v1/auth/logout' => 'logout',
    '/v1/me/summary' => 'me_summary',
];
if (isset($authRoutes[$path])) {
    require_once __DIR__ . '/api/router.php';
    StkAuthRouter::dispatch($authRoutes[$path]);
}
if (preg_match('#^/v1/legal/(user_agreement|privacy_policy)$#', $path, $match)) {
    require_once __DIR__ . '/api/router.php';
    StkAuthRouter::dispatch('legal:' . $match[1]);
}

if ($path === '/v1/project/categories' || $path === '/v1/projects') {
    require_once dirname(__DIR__) . '/stk_project/api/v1.php';
    StkProjectApi::dispatch($path === '/v1/project/categories' ? 'categories' : 'list');
}
if (preg_match('#^/v1/projects/([1-9][0-9]*)(/views)?$#', $path, $match)) {
    require_once dirname(__DIR__) . '/stk_project/api/v1.php';
    StkProjectApi::dispatch(($match[2] ?? '') === '/views' ? 'record_view' : 'detail', (int)$match[1]);
}

StkApiResponse::send(404, 'SYS_REQUEST_INVALID', '请求路径不存在');
