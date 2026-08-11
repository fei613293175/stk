<?php
if (!defined('IN_ADMINCP')) exit('Access Denied'); require_once dirname(__DIR__).'/lib/config.php';
if (submitcheck('publishsettingssubmit')) {
    stk_project_set_config('publish_enabled',!empty($_POST['publish_enabled'])?'1':'0');
    stk_project_set_config('review_mode',in_array($_POST['review_mode']??'manual',['manual','direct'],true)?(string)$_POST['review_mode']:'manual');
    foreach (['title_min'=>[1,20,4],'title_max'=>[20,120,60],'summary_min'=>[1,100,10],'summary_max'=>[100,2000,1000],'daily_limit'=>[1,100,10],'pending_limit'=>[1,50,5]] as $key=>$rule) stk_project_set_config($key,(string)max($rule[0],min($rule[1],(int)($_POST[$key]??$rule[2]))));
    foreach (['edit_requires_review','user_can_offline','user_can_delete'] as $key) stk_project_set_config($key,!empty($_POST[$key])?'1':'0');
    stk_project_admin_audit('publish','config_update'); cpmsg('发布规则已保存','action=plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=publish_settings','succeed');
}
showformheader('plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=publish_settings'); showtableheader('发布规则');
showsetting('用户发布开关','publish_enabled',stk_project_config('publish_enabled','1'),'radio'); showsetting('审核模式','review_mode',stk_project_config('review_mode','manual'),'select',0,0,'','',['manual'=>'人工审核','direct'=>'直接发布']); showsetting('标题最少字数','title_min',stk_project_config('title_min','4'),'text'); showsetting('标题最多字数','title_max',stk_project_config('title_max','60'),'text'); showsetting('简介最少字数','summary_min',stk_project_config('summary_min','10'),'text'); showsetting('简介最多字数','summary_max',stk_project_config('summary_max','1000'),'text'); showsetting('单用户每日发布上限','daily_limit',stk_project_config('daily_limit','10'),'text'); showsetting('待审核项目上限','pending_limit',stk_project_config('pending_limit','5'),'text'); showsetting('已发布编辑后重审','edit_requires_review',stk_project_config('edit_requires_review','1'),'radio'); showsetting('允许用户下架','user_can_offline',stk_project_config('user_can_offline','1'),'radio'); showsetting('允许用户软删除','user_can_delete',stk_project_config('user_can_delete','1'),'radio'); showsubmit('publishsettingssubmit','保存'); showtablefooter(); showformfooter();

