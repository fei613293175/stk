<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
$loginRows = DB::fetch_all('SELECT uid,mobile_masked,login_type,result,error_code,ip,created_at FROM %t ORDER BY id DESC LIMIT 200', ['stk_auth_login_log']);
$eventRows = DB::fetch_all('SELECT uid,event_type,result,ip,metadata_json,created_at FROM %t ORDER BY event_id DESC LIMIT 200', ['stk_security_event']);
showtableheader('登录安全日志'); showtablerow('', [], ['UID', '手机号', '类型', '结果', '错误码', 'IP', '时间']);
foreach ($loginRows as $row) showtablerow('', [], [(int) $row['uid'], dhtmlspecialchars($row['mobile_masked']), dhtmlspecialchars($row['login_type']), dhtmlspecialchars($row['result']), (int) $row['error_code'], dhtmlspecialchars($row['ip']), dgmdate((int) $row['created_at'])]);
showtablefooter();
showtableheader('安全与管理审计'); showtablerow('', [], ['UID', '事件', '结果', 'IP', '元数据', '时间']);
foreach ($eventRows as $row) showtablerow('', [], [(int) $row['uid'], dhtmlspecialchars($row['event_type']), dhtmlspecialchars($row['result']), dhtmlspecialchars($row['ip']), dhtmlspecialchars(substr($row['metadata_json'], 0, 300)), dgmdate((int) $row['created_at'])]);
showtablefooter();

