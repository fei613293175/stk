<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';

if (submitcheck('mesettingssubmit')) {
    foreach (['member_title','member_benefit_discount','member_benefit_rebate','member_open_button_text','profile_bio','commission_label','task_label','placeholder_message'] as $key) {
        stk_project_set_config($key, trim((string) ($_POST[$key] ?? '')));
    }
    foreach (['show_wallets','show_member_card','show_props_center'] as $key) {
        stk_project_set_config($key, !empty($_POST[$key]) ? '1' : '0');
    }
    stk_project_admin_audit('me', 'config_update');
    cpmsg('我的页面设置已保存', 'action=plugins&operation=config&do=' . $pluginid . '&identifier=stk_project&pmod=me_settings', 'succeed');
}

showformheader('plugins&operation=config&do=' . $pluginid . '&identifier=stk_project&pmod=me_settings');
showtableheader('我的页面配置');
showsetting('会员卡标题', 'member_title', stk_project_config('member_title', '商推客会员'), 'text');
showsetting('会员折扣展示文案', 'member_benefit_discount', stk_project_config('member_benefit_discount', '消费 5 折'), 'text');
showsetting('会员返佣展示文案', 'member_benefit_rebate', stk_project_config('member_benefit_rebate', '消费返佣 40%'), 'text');
showsetting('未开通按钮文案', 'member_open_button_text', stk_project_config('member_open_button_text', '立即开通'), 'text');
showsetting('资料简介', 'profile_bio', stk_project_config('profile_bio', ''), 'textarea');
showsetting('显示双账户', 'show_wallets', stk_project_config('show_wallets', '1'), 'radio');
showsetting('显示会员卡', 'show_member_card', stk_project_config('show_member_card', '1'), 'radio');
showsetting('显示道具中心', 'show_props_center', stk_project_config('show_props_center', '1'), 'radio');
showsetting('佣金账户名称', 'commission_label', stk_project_config('commission_label', '佣金账户'), 'text');
showsetting('任务账户名称', 'task_label', stk_project_config('task_label', '任务账户'), 'text');
showsetting('占位页统一文案', 'placeholder_message', stk_project_config('placeholder_message', '功能筹备中'), 'text');
showsubmit('mesettingssubmit', '保存');
showtablefooter();
showformfooter();
