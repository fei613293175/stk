<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
require_once dirname(__DIR__) . '/../stk_auth/service/ApiResponse.php';
require_once dirname(__DIR__) . '/../stk_auth/service/TokenService.php';
require_once dirname(__DIR__) . '/../stk_auth/service/Input.php';
require_once dirname(__DIR__) . '/service/ProjectService.php';

final class StkProjectApi {
    public static function dispatch(string $operation, int $projectId = 0): void {
        try {
            $uid = StkTokenService::authenticateAccess(StkTokenService::bearerToken());
            $method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
            if ($method === 'GET' && $operation === 'categories') { StkApiResponse::send(200, 'OK', 'ok', StkProjectService::categories(($_GET['include_all'] ?? '') === 'true')); }
            if ($method === 'GET' && $operation === 'list') { StkApiResponse::send(200, 'OK', 'ok', StkProjectService::list($_GET)); }
            if ($method === 'GET' && $operation === 'detail') { StkApiResponse::send(200, 'OK', 'ok', StkProjectService::detail($projectId, $uid)); }
            if ($method === 'POST' && $operation === 'record_view') { $input = StkInput::json(); StkApiResponse::send(200, 'OK', 'ok', StkProjectService::recordView($projectId, $uid, StkInput::string($input, 'view_session_id', 128))); }
            StkApiResponse::send(404, 'SYS_REQUEST_INVALID', '请求路径不存在');
        } catch (StkApiException $error) { StkApiResponse::fail($error); }
          catch (RuntimeException $error) { StkApiResponse::send(503, 'PROJECT_SERVICE_UNAVAILABLE', '项目服务暂不可用'); }
    }
}
