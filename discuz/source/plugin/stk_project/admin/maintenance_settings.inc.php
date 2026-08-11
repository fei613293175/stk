<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';

if (submitcheck('maintenancesubmit')) {
    $enabled = !empty($_POST['maintenance_enabled']) ? '1' : '0';
    $message = trim((string) ($_POST['maintenance_message'] ?? ''));
    $expectedEnd = trim((string) ($_POST['maintenance_expected_end'] ?? ''));
    $appLinksHost = strtolower(trim((string) ($_POST['app_links_host'] ?? '')));
    if ($message === '' || mb_strlen($message) > 500) cpmsg('维护文案不能为空且不能超过 500 字', '', 'error');
    if ($expectedEnd !== '' && strtotime($expectedEnd) === false) cpmsg('预计恢复时间格式错误', '', 'error');
    if ($appLinksHost !== 'stk.zz-yihao.com') cpmsg('App Links 主机必须与 Android Manifest 的 stk.zz-yihao.com 保持一致', '', 'error');
    stk_project_set_contract_config('maintenance.enabled', $enabled);
    stk_project_set_contract_config('maintenance.message', $message);
    stk_project_set_contract_config('maintenance.expected_end', $expectedEnd);
    stk_project_set_contract_config('app_links.host', $appLinksHost);
    stk_project_admin_audit('maintenance', 'config_update', [
        'enabled'=>$enabled === '1',
        'expected_end'=>$expectedEnd,
        'app_links_host'=>$appLinksHost,
    ]);
    cpmsg('维护设置已保存', 'action=plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=maintenance_settings', 'succeed');
}

showformheader('plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=maintenance_settings');
showtableheader('维护与运行配置');
showsetting('维护模式', 'maintenance_enabled', stk_project_contract_config('maintenance.enabled', '0'), 'radio');
showsetting('维护文案', 'maintenance_message', stk_project_contract_config('maintenance.message', '系统维护中，请稍后再试。'), 'textarea');
showsetting('预计恢复时间', 'maintenance_expected_end', stk_project_contract_config('maintenance.expected_end', ''), 'text');
showsetting('App Links 主机', 'app_links_host', stk_project_contract_config('app_links.host', 'stk.zz-yihao.com'), 'text');
showsubmit('maintenancesubmit', '保存');
showtablefooter();
showformfooter();
