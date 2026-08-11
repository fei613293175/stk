<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__).'/lib/config.php';
$status=trim((string)($_GET['status']??'pending'));
if(submitcheck('reviewsubmit')){
    $projectId=(int)($_POST['project_id']??0);$action=trim((string)($_POST['review_action']??''));$reason=trim((string)($_POST['reason']??''));
    $row=DB::fetch_first('SELECT project_id,status FROM %t WHERE project_id=%d AND deleted_at=0',['stk_project',$projectId]);
    if(!$row) cpmsg('项目不存在','', 'error');
    if((string)$row['status']!=='pending') cpmsg('只有待审核项目可以执行审核','', 'error');
    if(!in_array($action,['approve','reject'],true)) cpmsg('审核动作无效','', 'error');
    if($action==='reject' && $reason==='') cpmsg('驳回必须填写原因','', 'error');
    $to=$action==='approve'?'published':'rejected';
    DB::query('START TRANSACTION');
    try {
        DB::query('UPDATE %t SET status=%s,rejection_reason=%s,published_at=%d,updated_at=%d,row_version=row_version+1 WHERE project_id=%d AND status=%s',['stk_project',$to,$action==='reject'?substr($reason,0,500):'', $action==='approve'?TIMESTAMP:0,TIMESTAMP,$projectId,$row['status']]);
        if((int)DB::affected_rows()!==1){DB::query('ROLLBACK');cpmsg('项目状态已变化，请刷新后重试','', 'error');}
        stk_project_audit($projectId,(int)($_G['uid']??0),'review',$row['status'],$to,$reason);
        stk_project_admin_audit('review',$action,['project_id'=>$projectId,'reason'=>$reason]);
        DB::query('COMMIT');
    } catch(Throwable $error) {
        try{DB::query('ROLLBACK');}catch(Throwable $ignored){}
        cpmsg('审核保存失败，请稍后重试','', 'error');
    }
    cpmsg('审核结果已保存','action=plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=review','succeed');
}
$rows=DB::fetch_all('SELECT project_id,title,publisher_uid,publisher_name,status,rejection_reason,updated_at FROM %t WHERE status=%s AND deleted_at=0 ORDER BY project_id DESC LIMIT 100',['stk_project',$status]);
showtableheader('项目审核'); showtablerow('',[],['ID','标题','发布者','状态','原因','操作']);
foreach($rows as $row){
    $form=' <form method="post"><input type="hidden" name="formhash" value="'.FORMHASH.'"><input type="hidden" name="project_id" value="'.(int)$row['project_id'].'"><input type="text" name="reason" value=""><select name="review_action"><option value="approve">通过</option><option value="reject">驳回</option></select><button name="reviewsubmit" value="true">提交</button></form>';
    showtablerow('',[],[$row['project_id'],dhtmlspecialchars($row['title']),dhtmlspecialchars($row['publisher_name']),$row['status'],dhtmlspecialchars($row['rejection_reason']),$form]);
}
showtablefooter();
