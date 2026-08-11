<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__).'/lib/config.php';
$categories=DB::fetch_all('SELECT category_id,name FROM %t WHERE enabled=1 ORDER BY sort_order,category_id',['stk_project_category']);
if(submitcheck('projectsubmit')){
    $title=trim((string)($_POST['title']??''));
    $summary=trim((string)($_POST['summary']??''));
    $categoryId=trim((string)($_POST['category_id']??''));
    $publisherUid=max(1,(int)($_POST['publisher_uid']??1));
    $publisherName=trim((string)($_POST['publisher_name']??''));
    $coverUrl=trim((string)($_POST['cover_url']??''));
    $phone=trim((string)($_POST['phone']??''));
    $website=trim((string)($_POST['website']??''));
    $categoryExists=DB::result_first('SELECT category_id FROM %t WHERE category_id=%s AND enabled=1',['stk_project_category',$categoryId]);
    if($title==='' || $summary==='' || $publisherName==='' || !$categoryExists) cpmsg('请完整填写标题、简介、分类和发布者','', 'error');
    if($coverUrl!=='' && strtolower((string)parse_url($coverUrl,PHP_URL_SCHEME))!=='https') cpmsg('封面 URL 必须使用 HTTPS','', 'error');
    $contacts=[];
    if($phone!=='') $contacts[]=['type'=>'phone','value'=>$phone,'label'=>'电话'];
    if($website!=='') {
        $host=(string)parse_url($website,PHP_URL_HOST);
        $allowed=array_filter(array_map('trim',preg_split('/[\s,]+/',stk_project_config('external_url_allowlist','stk.zz-yihao.com'))));
        if(strtolower((string)parse_url($website,PHP_URL_SCHEME))!=='https' || !in_array(strtolower($host),array_map('strtolower',$allowed),true)) cpmsg('项目网址必须使用 HTTPS 且域名位于白名单','', 'error');
        $contacts[]=['type'=>'url','value'=>$website,'label'=>'项目网址'];
    }
    $now=TIMESTAMP;
    $projectId=DB::insert('stk_project',[
        'publisher_uid'=>$publisherUid,'title'=>$title,'summary'=>$summary,'category_id'=>$categoryId,
        'cover_url'=>$coverUrl,'publisher_name'=>$publisherName,'member_label'=>'测试项目',
        'contacts_json'=>json_encode($contacts,JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES),
        'status'=>'published','view_count'=>0,'published_at'=>$now,'created_at'=>$now,'updated_at'=>$now,
    ],true);
    if($coverUrl!=='') DB::insert('stk_project_image',['project_id'=>$projectId,'image_url'=>$coverUrl,'alt_text'=>$title,'sort_order'=>0]);
    stk_project_admin_audit('projects','create',['project_id'=>$projectId]);
    cpmsg('测试项目已创建','action=plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=projects','succeed');
}
showformheader('plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=projects');
showtableheader('创建 published 测试项目');
showsetting('标题','title','','text');
showsetting('简介','summary','','textarea');
$categoryOptions=[]; foreach($categories as $category) $categoryOptions[$category['category_id']]=$category['name'];
$defaultCategory=$categories?(string)$categories[0]['category_id']:'';
showsetting('分类','category_id',$defaultCategory,'select',0,0,'','',$categoryOptions);
showsetting('发布者 UID','publisher_uid','1','text');
showsetting('发布者名称','publisher_name','测试发布者','text');
showsetting('封面 URL','cover_url','','text');
showsetting('联系电话','phone','','text');
showsetting('项目网址','website','','text');
showsubmit('projectsubmit','创建测试项目'); showtablefooter(); showformfooter();
$rows=DB::fetch_all('SELECT project_id,title,status,publisher_name,view_count FROM %t ORDER BY project_id DESC LIMIT 100',['stk_project']);
showtableheader('项目管理');
showtablerow('',[],['ID','标题','状态','发布者','浏览']);
foreach($rows as $row) showtablerow('',[],[$row['project_id'],dhtmlspecialchars($row['title']),$row['status'],dhtmlspecialchars($row['publisher_name']),$row['view_count']]);
showtablefooter();
