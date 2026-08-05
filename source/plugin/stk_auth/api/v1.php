<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
final class StkAuthApi {
    public static function respond(int $status, string $code, string $message, array $data = []): void {
        http_response_code($status); header('Content-Type: application/json; charset=utf-8');
        echo json_encode(['code'=>$code,'message'=>$message,'data'=>$data,'request_id'=>self::requestId(),'server_time'=>gmdate('c')], JSON_UNESCAPED_UNICODE); exit;
    }
    public static function requestId(): string { return $_SERVER['HTTP_X_REQUEST_ID'] ?? bin2hex(random_bytes(16)); }
    public static function dispatch(string $operation): void {
        if ($_SERVER['REQUEST_METHOD'] === 'GET' && $operation === 'health') { self::respond(200,'OK','ok',['status'=>'ok','service'=>'stk_auth','build'=>'1.0.0']); }
        if ($operation === 'bootstrap') { self::respond(200,'OK','ok',['maintenance'=>false,'auth_state'=>'anonymous','feature_flags'=>['project_read'=>true],'legal_versions'=>['user_agreement'=>'1.0','privacy_policy'=>'1.0'],'release_policy'=>['version_name'=>'1.0.0','version_code'=>10000]]); }
        if ($operation === 'captcha') { self::respond(200,'OK','ok',['challenge_id'=>bin2hex(random_bytes(16)),'image_base64_or_url'=>null,'expires_in'=>120]); }
        self::respond(404,'SYS_REQUEST_INVALID','请求路径不存在');
    }
}
