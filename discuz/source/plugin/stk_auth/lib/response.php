<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

function stk_auth_request_id(): string
{
    $header = $_SERVER['HTTP_X_STK_REQUEST_ID'] ?? '';
    if (is_string($header) && preg_match('/^[A-Za-z0-9\-]{8,64}$/', $header)) {
        return $header;
    }
    return bin2hex(random_bytes(8));
}

function stk_auth_json(int $code, string $message, $data = null, int $httpStatus = 200): void
{
    http_response_code($httpStatus);
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-store');
    echo json_encode([
        'code' => $code,
        'message' => $message,
        'data' => $data,
        'request_id' => stk_auth_request_id(),
    ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

function stk_auth_json_input(): array
{
    $raw = file_get_contents('php://input');
    if ($raw === false || trim($raw) === '') {
        return [];
    }
    $decoded = json_decode($raw, true);
    if (!is_array($decoded)) {
        stk_auth_json(4001, '请求 JSON 格式错误', null, 400);
    }
    return $decoded;
}
