<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
$rows=DB::fetch_all('SELECT audit_id,operator_uid,section,action,payload_json,ip,created_at FROM %t ORDER BY audit_id DESC LIMIT 300',['stk_admin_audit']);showtableheader('生产安全与配置审计');showtablerow('',[],['ID','操作人','区域','动作','内容','IP','时间']);foreach($rows as $r)showtablerow('',[],[(int)$r['audit_id'],(int)$r['operator_uid'],dhtmlspecialchars($r['section']),dhtmlspecialchars($r['action']),dhtmlspecialchars(substr($r['payload_json'],0,300)),dhtmlspecialchars($r['ip']),dgmdate((int)$r['created_at'])]);showtablefooter();

