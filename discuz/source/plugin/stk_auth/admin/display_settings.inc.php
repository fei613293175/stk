<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php'; require_once dirname(__DIR__) . '/lib/auth_service.php';
if (submitcheck('displaysettingssubmit')) {
    foreach (['member_title','commission_label','task_label','support_type','support_label','support_value','support_hours','placeholder_message'] as $key) stk_auth_set_config($key, trim((string) ($_POST[$key] ?? '')));
    stk_auth_set_config('support_copy_enabled', !empty($_POST['support_copy_enabled']) ? '1' : '0');
    stk_auth_security_event((int) ($_G['uid'] ?? 0), 'admin_config', 'success', ['section' => 'display']);
    cpmsg('运营展示设置已保存', 'action=plugins&operation=config&do=' . $pluginid . '&identifier=stk_auth&pmod=display_settings', 'succeed');
}
showformheader('plugins&operation=config&do=' . $pluginid . '&identifier=stk_auth&pmod=display_settings'); showtableheader('我的与运营展示设置');
showsetting('会员卡标题','member_title',stk_auth_get_config('member_title','商推客会员'),'text'); showsetting('佣金账户名称','commission_label',stk_auth_get_config('commission_label','佣金账户'),'text'); showsetting('任务账户名称','task_label',stk_auth_get_config('task_label','任务账户'),'text'); showsetting('客服类型','support_type',stk_auth_get_config('support_type','wechat'),'text'); showsetting('客服名称','support_label',stk_auth_get_config('support_label','在线客服'),'text'); showsetting('客服值','support_value',stk_auth_get_config('support_value',''),'text'); showsetting('服务时间','support_hours',stk_auth_get_config('support_hours','工作日 09:00-18:00'),'text'); showsetting('允许复制客服信息','support_copy_enabled',stk_auth_get_config('support_copy_enabled','1'),'radio'); showsetting('占位页文案','placeholder_message',stk_auth_get_config('placeholder_message','功能筹备中'),'text');
showsubmit('displaysettingssubmit','保存'); showtablefooter(); showformfooter();

