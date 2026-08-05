<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
final class StkProjectApi {
    public static function respond(int $status, string $code, string $message, array $data = []): void {
        http_response_code($status); header('Content-Type: application/json; charset=utf-8');
        echo json_encode(['code'=>$code,'message'=>$message,'data'=>$data,'request_id'=>bin2hex(random_bytes(16)),'server_time'=>gmdate('c')], JSON_UNESCAPED_UNICODE); exit;
    }
    public static function listProjects(): void { self::respond(200,'OK','ok',['items'=>[],'next_cursor'=>null,'has_more'=>false,'applied_filters'=>[]]); }
    public static function categories(): void { self::respond(200,'OK','ok',['categories'=>[]]); }
}
