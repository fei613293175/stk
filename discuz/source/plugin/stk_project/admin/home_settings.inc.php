<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__).'/lib/config.php';
if(submitcheck('settingssubmit')){
    stk_project_set_config('browse_enabled',!empty($_POST['browse_enabled'])?'1':'0');
    stk_project_set_config('page_size',(string)max(1,min(50,(int)($_POST['page_size']??20))));
    stk_project_set_config('default_sort',in_array($_POST['default_sort']??'latest',['latest','popular'],true)?(string)$_POST['default_sort']:'latest');
    foreach(['categories_enabled','show_publisher_avatar','show_member_badge','show_view_count','show_publish_time'] as $key) stk_project_set_config($key,!empty($_POST[$key])?'1':'0');
    stk_project_set_config('empty_text',substr(trim((string)($_POST['empty_text']??'暂无项目')),0,120));
    $defaultCover=trim((string)($_POST['default_cover_url']??'')); if($defaultCover!=='' && (strtolower((string)parse_url($defaultCover,PHP_URL_SCHEME))!=='https' || !filter_var($defaultCover,FILTER_VALIDATE_URL))) cpmsg('默认封面必须是有效 HTTPS URL','','error'); stk_project_set_config('default_cover_url',$defaultCover);
    stk_project_set_config('publish_enabled',!empty($_POST['publish_enabled'])?'1':'0');
    stk_project_set_config('max_images',(string)max(1,min(12,(int)($_POST['max_images']??9))));
    stk_project_set_config('max_image_bytes',(string)max(1024,min(52428800,(int)($_POST['max_image_bytes']??10485760))));
    stk_project_admin_audit('home','config_update');
    cpmsg('设置已保存','action=plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=home_settings','succeed');
}
showformheader('plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=home_settings');
showtableheader('首页展示设置');
showsetting('项目浏览开关','browse_enabled',stk_project_config('browse_enabled','1'),'radio');
showsetting('每页数量','page_size',stk_project_config('page_size','20'),'text');
showsetting('默认排序','default_sort',stk_project_config('default_sort','latest'),'select',0,0,'','',['latest'=>'最新发布','popular'=>'浏览量优先']);
showsetting('显示分类','categories_enabled',stk_project_config('categories_enabled','1'),'radio');
showsetting('显示发布者头像','show_publisher_avatar',stk_project_config('show_publisher_avatar','1'),'radio');
showsetting('显示会员标识','show_member_badge',stk_project_config('show_member_badge','1'),'radio');
showsetting('显示浏览量','show_view_count',stk_project_config('show_view_count','1'),'radio');
showsetting('显示发布时间','show_publish_time',stk_project_config('show_publish_time','1'),'radio');
showsetting('空状态文案','empty_text',stk_project_config('empty_text','暂无项目'),'text');
showsetting('默认封面 HTTPS URL','default_cover_url',stk_project_config('default_cover_url',''),'text');
showsetting('发布开关','publish_enabled',stk_project_config('publish_enabled','1'),'radio');
showsetting('最大图片数','max_images',stk_project_config('max_images','9'),'text');
showsetting('单图最大字节','max_image_bytes',stk_project_config('max_image_bytes','10485760'),'text');
showsubmit('settingssubmit','保存'); showtablefooter(); showformfooter();
