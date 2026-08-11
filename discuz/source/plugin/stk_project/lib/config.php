<?php
if (!defined('IN_DISCUZ')) exit('Access Denied');
function stk_project_config(string $key, string $default=''): string { try { $row=DB::fetch_first('SELECT config_value FROM %t WHERE config_key=%s',['stk_project_config',$key]); if(isset($row['config_value']))return (string)$row['config_value'];$shared=DB::fetch_first('SELECT config_value FROM %t WHERE config_key=%s',['stk_app_config',$key]);return isset($shared['config_value'])?(string)$shared['config_value']:$default; } catch(Throwable $e){ return $default; } }
function stk_project_set_config(string $key,string $value): void { DB::query('INSERT INTO %t (config_key,config_value,updated_at) VALUES (%s,%s,%d) ON DUPLICATE KEY UPDATE config_value=VALUES(config_value),updated_at=VALUES(updated_at)',['stk_project_config',$key,$value,TIMESTAMP]);DB::query('INSERT INTO %t (config_key,config_value,updated_at) VALUES (%s,%s,%d) ON DUPLICATE KEY UPDATE config_value=VALUES(config_value),updated_at=VALUES(updated_at)',['stk_app_config',$key,$value,TIMESTAMP]); }
function stk_project_contract_config_alias(string $key): string {
    $aliases=[
        'release.current_version'=>'release_current_version',
        'release.minimum_version'=>'release_minimum_version',
        'release.force_update'=>'release_force_update',
        'release.download_url'=>'release_download_url',
        'release.apk_sha256'=>'release_apk_sha256',
        'release.notes'=>'release_notes',
        'maintenance.enabled'=>'maintenance_enabled',
        'maintenance.message'=>'maintenance_message',
        'maintenance.expected_end'=>'maintenance_resume_at',
        'app_links.host'=>'app_links_host',
    ];
    return $aliases[$key]??$key;
}
function stk_project_contract_config(string $key,string $default=''): string {
    $value=stk_project_config($key,'');
    return $value!==''?$value:stk_project_config(stk_project_contract_config_alias($key),$default);
}
function stk_project_set_contract_config(string $key,string $value): void {
    stk_project_set_config($key,$value);
    $alias=stk_project_contract_config_alias($key);
    if($alias!==$key)stk_project_set_config($alias,$value);
}
function stk_project_release_download_hosts(): array {
    $configured=stk_project_config('release.download_hosts','stk-download.zz-yihao.com,stk.zz-yihao.com');
    $hosts=[];
    foreach(preg_split('/[\s,]+/',strtolower($configured)) as $host){
        $host=trim($host);
        if($host!==''&&preg_match('/^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,63}$/',$host))$hosts[]=$host;
    }
    return array_values(array_unique($hosts));
}
function stk_project_admin_audit(string $section,string $action,array $payload=[]): void {
    global $_G;
    DB::insert('stk_admin_audit',['operator_uid'=>(int)($_G['uid']??0),'section'=>substr($section,0,32),'action'=>substr($action,0,32),'payload_json'=>json_encode($payload,JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES),'ip'=>substr((string)($_G['clientip']??''),0,64),'created_at'=>TIMESTAMP]);
}
function stk_project_bearer_token(): string {
    $header=(string)($_SERVER['HTTP_AUTHORIZATION']??$_SERVER['REDIRECT_HTTP_AUTHORIZATION']??'');
    return preg_match('/^Bearer\s+(.+)$/i',trim($header),$match)===1?trim($match[1]):'';
}
function stk_project_require_auth(): void {
    $token=stk_project_bearer_token();
    if($token==='') stk_project_json(4010,'缺少访问令牌',null,401);
    try {
        $fakeEnabled=DB::result_first('SELECT config_value FROM %t WHERE config_key=%s',['stk_auth_config','dev_fake_enabled'])==='1';
        if($fakeEnabled && strpos($token,'dev-access-')===0) return;
        $row=DB::fetch_first('SELECT id FROM %t WHERE token_hash=%s AND token_type=%s AND revoked_at=0 AND expires_at>%d',['stk_auth_token',hash('sha256',$token),'access',TIMESTAMP]);
        if($row) return;
    } catch(Throwable $e) {
        // Authentication storage may not be installed yet; fail closed.
    }
    stk_project_json(4011,'访问令牌无效或已过期',null,401);
}

function stk_project_auth_uid(): int {
    $token=stk_project_bearer_token();
    try {
        $fakeEnabled=DB::result_first('SELECT config_value FROM %t WHERE config_key=%s',['stk_auth_config','dev_fake_enabled'])==='1';
        if($fakeEnabled && strpos($token,'dev-access-')===0) return 10001;
        return (int)DB::result_first('SELECT uid FROM %t WHERE token_hash=%s AND token_type=%s AND revoked_at=0 AND expires_at>%d',['stk_auth_token',hash('sha256',$token),'access',TIMESTAMP]);
    } catch(Throwable $e) { return 0; }
}
function stk_project_audit(int $projectId,int $operatorUid,string $action,string $fromStatus,string $toStatus,string $reason='',array $payload=[]): void {
    DB::query('INSERT INTO %t (project_id,operator_uid,action,from_status,to_status,reason,payload_json,created_at) VALUES (%d,%d,%s,%s,%s,%s,%s,%d)',['stk_project_audit_log',$projectId,$operatorUid,$action,$fromStatus,$toStatus,$reason,json_encode($payload,JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES),TIMESTAMP]);
}
function stk_project_write_guard(int $uid,string $action): void {
    global $stkProjectIdempotencyHash, $stkProjectIdempotencyUid;
    $requestId=trim((string)($_SERVER['HTTP_X_STK_IDEMPOTENCY_KEY']??$_SERVER['HTTP_X_STK_REQUEST_ID']??''));
    if($requestId===''||strlen($requestId)>128||!preg_match('/^[A-Za-z0-9._:-]{8,128}$/',$requestId))stk_project_json(4006,'缺少或无效的 App 请求标识',null,400);
    $action=substr($action,0,128);
    $limit=strpos($action,'/view')!==false?120:60;
    $recent=(int)DB::result_first('SELECT COUNT(*) FROM %t WHERE uid=%d AND action=%s AND created_at>%d',['stk_project_request_guard',$uid,$action,TIMESTAMP-60]);
    if($recent>=$limit)stk_project_json(4290,'操作过于频繁，请稍后再试',null,429);
    $hash=hash('sha256',$uid.'|'.$action.'|'.$requestId);
    $existing=DB::fetch_first('SELECT response_json,response_status FROM %t WHERE request_hash=%s AND uid=%d AND action=%s',['stk_project_request_guard',$hash,$uid,$action]);
    if ($existing && trim((string)($existing['response_json'] ?? '')) !== '') {
        http_response_code((int)($existing['response_status'] ?? 200));
        header('Content-Type: application/json; charset=utf-8'); header('Cache-Control: no-store');
        echo (string)$existing['response_json']; exit;
    }
    DB::query('INSERT IGNORE INTO %t (request_hash,uid,action,created_at) VALUES (%s,%d,%s,%d)',['stk_project_request_guard',$hash,$uid,$action,TIMESTAMP]);
    if((int)DB::result_first('SELECT ROW_COUNT()')!==1)stk_project_json(4093,'请求正在处理中，请稍后重试',null,409);
    $stkProjectIdempotencyHash = $hash;
    $stkProjectIdempotencyUid = $uid;
    if(random_int(1,100)===1)DB::query('DELETE FROM %t WHERE created_at<%d',['stk_project_request_guard',TIMESTAMP-86400]);
}
function stk_project_idempotency_store(string $response, int $status): void {
    global $stkProjectIdempotencyHash, $stkProjectIdempotencyUid;
    if (empty($stkProjectIdempotencyHash) || empty($stkProjectIdempotencyUid) || defined('STK_PROJECT_REPLAYING')) return;
    DB::query('UPDATE %t SET response_json=%s,response_status=%d WHERE request_hash=%s AND uid=%d',['stk_project_request_guard',$response,$status,$stkProjectIdempotencyHash,$stkProjectIdempotencyUid]);
}
