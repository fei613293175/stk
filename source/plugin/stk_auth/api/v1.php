<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
require_once __DIR__ . '/router.php';

final class StkAuthApi {
    public static function respond(int $status, string $code, string $message, array $data = []): void {
        StkApiResponse::send($status, $code, $message, $data);
    }
    public static function requestId(): string { return StkApiResponse::requestId(); }
    public static function dispatch(string $operation): void {
        if ($_SERVER['REQUEST_METHOD'] === 'GET' && $operation === 'health') { self::respond(200,'OK','ok',['status'=>'ok','service'=>'stk_auth','build'=>'1.0.0']); }
        StkAuthRouter::dispatch($operation === 'captcha' ? 'captcha_challenge' : $operation);
    }
}
