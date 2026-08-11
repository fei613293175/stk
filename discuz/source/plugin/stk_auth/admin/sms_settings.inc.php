<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';
require_once dirname(__DIR__) . '/lib/auth_service.php';
require_once dirname(__DIR__) . '/lib/sms_service.php';

if (submitcheck('smssettingssubmit')) {
    $enabled = !empty($_POST['sms_enabled']);
    $accessKeyId = trim((string) ($_POST['sms_access_key_id'] ?? ''));
    $newSecret = trim((string) ($_POST['sms_access_key_secret'] ?? ''));
    $signName = trim((string) ($_POST['sms_sign_name'] ?? ''));
    $templateCode = trim((string) ($_POST['sms_template_code'] ?? ''));
    if ($enabled && ($accessKeyId === '' || ($newSecret === '' && stk_auth_get_config('sms_access_key_secret', '') === '') || $signName === '' || $templateCode === '')) {
        cpmsg('启用短信前必须完整配置 AccessKey、签名和模板', '', 'error');
    }
    stk_auth_set_config('sms_enabled', $enabled ? '1' : '0');
    stk_auth_set_config('sms_access_key_id', $accessKeyId);
    if ($newSecret !== '') stk_auth_set_config('sms_access_key_secret', stk_auth_encrypt_secret($newSecret));
    stk_auth_set_config('sms_sign_name', $signName);
    stk_auth_set_config('sms_template_code', $templateCode);
    foreach (['code_length' => [4, 8, 6], 'expire_minutes' => [1, 30, 5], 'resend_seconds' => [30, 600, 60], 'phone_hour_limit' => [1, 100, 5], 'phone_day_limit' => [1, 500, 10], 'ip_hour_limit' => [1, 1000, 20]] as $key => $rule) {
        stk_auth_set_config('sms_' . $key, (string) max($rule[0], min($rule[1], (int) ($_POST['sms_' . $key] ?? $rule[2]))));
    }
    stk_auth_security_event((int) ($_G['uid'] ?? 0), 'admin_config', 'success', ['section' => 'sms', 'enabled' => $enabled]);
    cpmsg('短信设置已保存', 'action=plugins&operation=config&do=' . $pluginid . '&identifier=stk_auth&pmod=sms_settings', 'succeed');
}

showformheader('plugins&operation=config&do=' . $pluginid . '&identifier=stk_auth&pmod=sms_settings');
showtableheader('阿里云短信设置');
showsetting('启用生产短信', 'sms_enabled', stk_auth_get_config('sms_enabled', '0'), 'radio', '', 0, '启用前请确认阿里云签名和模板均已审核通过。');
showsetting('AccessKey ID', 'sms_access_key_id', stk_auth_get_config('sms_access_key_id', ''), 'text');
showsetting('AccessKey Secret', 'sms_access_key_secret', '', 'password', '', 0, stk_auth_get_config('sms_access_key_secret', '') !== '' ? '已加密保存；留空保持不变。' : '尚未配置。');
showsetting('短信签名', 'sms_sign_name', stk_auth_get_config('sms_sign_name', ''), 'text');
showsetting('登录模板 Code', 'sms_template_code', stk_auth_get_config('sms_template_code', ''), 'text');
showsetting('验证码位数', 'sms_code_length', stk_auth_get_config('sms_code_length', '6'), 'text');
showsetting('有效期（分钟）', 'sms_expire_minutes', stk_auth_get_config('sms_expire_minutes', '5'), 'text');
showsetting('重发间隔（秒）', 'sms_resend_seconds', stk_auth_get_config('sms_resend_seconds', '60'), 'text');
showsetting('手机号每小时上限', 'sms_phone_hour_limit', stk_auth_get_config('sms_phone_hour_limit', '5'), 'text');
showsetting('手机号每日上限', 'sms_phone_day_limit', stk_auth_get_config('sms_phone_day_limit', '10'), 'text');
showsetting('IP 每小时上限', 'sms_ip_hour_limit', stk_auth_get_config('sms_ip_hour_limit', '20'), 'text');
showsubmit('smssettingssubmit', '保存'); showtablefooter(); showformfooter();

