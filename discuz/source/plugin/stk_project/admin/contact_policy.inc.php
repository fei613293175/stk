<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__).'/lib/config.php';
if(submitcheck('settingssubmit')){
    $types=array_values(array_unique(array_intersect(array_filter(array_map('trim',preg_split('/[\s,]+/',(string)($_POST['contact_types']??'')))),['phone','wechat','qq','url'])));
    $hosts=array_values(array_unique(array_filter(array_map('strtolower',array_map('trim',preg_split('/[\s,]+/',(string)($_POST['external_url_allowlist']??'')))),static function($host){return preg_match('/^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,63}$/',$host)===1;})));
    if(in_array('url',$types,true) && !$hosts) cpmsg('启用网址联系方式时至少配置一个有效域名','','error');
    stk_project_set_config('contact_types',implode(',',$types));
    stk_project_set_config('external_url_allowlist',implode(',',$hosts));
    stk_project_admin_audit('contact_policy','config_update');
    cpmsg('设置已保存','action=plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=contact_policy','succeed');
}
showformheader('plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=contact_policy');
showtableheader('联系方式与外链策略');
showsetting('允许联系方式','contact_types',stk_project_config('contact_types','phone,wechat,qq,url'),'text');
showsetting('网址白名单','external_url_allowlist',stk_project_config('external_url_allowlist','stk.zz-yihao.com'),'textarea');
showsubmit('settingssubmit','保存'); showtablefooter(); showformfooter();
