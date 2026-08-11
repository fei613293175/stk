<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';

if (submitcheck('supportsubmit')) {
    $type = in_array($_POST['support_type'] ?? 'wechat', ['wechat','qq','phone','email','url'], true)
        ? (string) $_POST['support_type'] : 'wechat';
    $value = trim((string) ($_POST['support_value'] ?? ''));
    $allowlist = strtolower(trim((string) ($_POST['support_url_allowlist'] ?? 'stk.zz-yihao.com')));
    $hosts = array_values(array_unique(array_filter(array_map('trim', preg_split('/[\s,]+/', $allowlist)))));
    foreach ($hosts as $host) {
        if (!preg_match('/^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)(?:\.(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?))+$/', $host)) cpmsg('客服网址白名单包含无效域名', '', 'error');
    }
    if ($type === 'url' && $value !== '') {
        $scheme = strtolower((string) parse_url($value, PHP_URL_SCHEME));
        $host = strtolower((string) parse_url($value, PHP_URL_HOST));
        if ($scheme !== 'https' || !in_array($host, $hosts, true)) cpmsg('客服网址必须使用 HTTPS 且域名位于白名单', '', 'error');
    }
    foreach (['support_label' => trim((string) ($_POST['support_label'] ?? '')), 'support_value' => $value, 'support_hours' => trim((string) ($_POST['support_hours'] ?? '')), 'support_type' => $type, 'support_url_allowlist' => implode(',', $hosts)] as $key => $configValue) {
        stk_project_set_config($key, $configValue);
    }
    stk_project_set_config('support_copy_enabled', !empty($_POST['support_copy_enabled']) ? '1' : '0');
    stk_project_admin_audit('support', 'config_update', ['type' => $type, 'url_hosts' => $hosts]);
    cpmsg('客服设置已保存', 'action=plugins&operation=config&do=' . $pluginid . '&identifier=stk_project&pmod=support_settings', 'succeed');
}

showformheader('plugins&operation=config&do=' . $pluginid . '&identifier=stk_project&pmod=support_settings');
showtableheader('客服与协议入口');
showsetting('客服类型', 'support_type', stk_project_config('support_type', 'wechat'), 'select', 0, 0, '', '', ['wechat'=>'微信','qq'=>'QQ','phone'=>'电话','email'=>'邮箱','url'=>'HTTPS 网址']);
showsetting('客服名称', 'support_label', stk_project_config('support_label', '在线客服'), 'text');
showsetting('客服值', 'support_value', stk_project_config('support_value', ''), 'text');
showsetting('服务时间', 'support_hours', stk_project_config('support_hours', '工作日 09:00-18:00'), 'text');
showsetting('允许复制', 'support_copy_enabled', stk_project_config('support_copy_enabled', '1'), 'radio');
showsetting('客服网址白名单', 'support_url_allowlist', stk_project_config('support_url_allowlist', 'stk.zz-yihao.com'), 'text', '', 0, '仅支持逗号分隔的域名；网址类型必须使用 HTTPS。');
showsubmit('supportsubmit', '保存');
showtablefooter();
showformfooter();
