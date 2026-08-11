<?php
if (!defined('IN_DISCUZ')) exit('Access Denied');
function stk_project_json(int $code, string $message, $data = null, int $status = 200): void {
    $requestId = bin2hex(random_bytes(8));
    $payload = ['code'=>$code,'message'=>$message,'data'=>$data,'request_id'=>$requestId,'server_time'=>gmdate('c')];
    $body = json_encode($payload, JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES);
    if (function_exists('stk_project_idempotency_store')) stk_project_idempotency_store((string) $body, $status);
    http_response_code($status); header('Content-Type: application/json; charset=utf-8'); header('Cache-Control: no-store');
    echo $body; exit;
}
