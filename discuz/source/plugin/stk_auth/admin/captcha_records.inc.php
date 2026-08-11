<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';
require_once dirname(__DIR__) . '/lib/auth_service.php';
require_once dirname(__DIR__) . '/lib/sms_service.php';
$revealedSms = null;
if (submitcheck('smsrevealsubmit')) {
    $smsId = max(1, (int) ($_POST['sms_id'] ?? 0));
    if (trim((string) ($_POST['reveal_confirmation'] ?? '')) !== '查看短信验证码') cpmsg('请输入确认短语“查看短信验证码”', '', 'error');
    $record = DB::fetch_first('SELECT id,mobile,code_audit_ciphertext,expires_at,use_status FROM %t WHERE id=%d', ['stk_auth_sms_code', $smsId]);
    if (!$record) cpmsg('短信记录不存在', '', 'error');
    $code = stk_auth_decrypt_secret((string) $record['code_audit_ciphertext']);
    if ($code === '') cpmsg('该记录没有可解密的审计值', '', 'error');
    stk_auth_security_event((int) ($_G['uid'] ?? 0), 'admin_sms_reveal', 'success', ['sms_id' => $smsId, 'mobile' => stk_auth_mask_mobile((string) $record['mobile'])]);
    $revealedSms = ['id' => $smsId, 'mobile' => stk_auth_mask_mobile((string) $record['mobile']), 'code' => $code, 'expires_at' => (int) $record['expires_at'], 'use_status' => (string) $record['use_status']];
}
$captchaRows = DB::fetch_all('SELECT challenge_id,scene,client_ip,attempt_count,status,expires_at,used_at,created_at FROM %t ORDER BY created_at DESC LIMIT 100', ['stk_auth_captcha']);
$smsRows = DB::fetch_all('SELECT id,mobile,scene,send_status,use_status,attempt_count,provider_code,created_at,expires_at FROM %t ORDER BY id DESC LIMIT 100', ['stk_auth_sms_code']);
showtableheader('图形验证码记录（不保存明文答案）'); showtablerow('', [], ['Challenge', '场景', 'IP', '尝试', '状态', '创建', '过期']);
foreach ($captchaRows as $row) showtablerow('', [], [dhtmlspecialchars(substr($row['challenge_id'], 0, 12) . '...'), dhtmlspecialchars($row['scene']), dhtmlspecialchars($row['client_ip']), (int) $row['attempt_count'], dhtmlspecialchars($row['status']), dgmdate((int) $row['created_at']), dgmdate((int) $row['expires_at'])]);
showtablefooter();
showformheader('plugins&operation=config&do='.$pluginid.'&identifier=stk_auth&pmod=captcha_records');
showtableheader('授权查看短信验证码');
showsetting('短信记录 ID', 'sms_id', '', 'text');
showsetting('二次确认短语', 'reveal_confirmation', '', 'text', '', 0, '请输入：查看短信验证码。查看动作会写入安全日志。');
showsubmit('smsrevealsubmit', '确认查看');
showtablefooter(); showformfooter();
if ($revealedSms) {
    showtableheader('本次授权查看结果（离开页面后不再显示）');
    showtablerow('', [], ['记录 ID', '手机号', '验证码', '使用状态', '过期时间']);
    showtablerow('', [], [(int) $revealedSms['id'], dhtmlspecialchars($revealedSms['mobile']), dhtmlspecialchars($revealedSms['code']), dhtmlspecialchars($revealedSms['use_status']), dgmdate($revealedSms['expires_at'])]);
    showtablefooter();
}
showtableheader('短信验证码记录（仅脱敏手机号和状态）'); showtablerow('', [], ['ID', '手机号', '场景', '发送', '使用', '尝试', '供应商状态', '创建']);
foreach ($smsRows as $row) showtablerow('', [], [(int) $row['id'], dhtmlspecialchars(stk_auth_mask_mobile($row['mobile'])), dhtmlspecialchars($row['scene']), dhtmlspecialchars($row['send_status']), dhtmlspecialchars($row['use_status']), (int) $row['attempt_count'], dhtmlspecialchars($row['provider_code']), dgmdate((int) $row['created_at'])]);
showtablefooter();
