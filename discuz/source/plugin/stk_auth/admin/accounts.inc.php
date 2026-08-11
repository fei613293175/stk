<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';
require_once dirname(__DIR__) . '/lib/auth_service.php';
if (submitcheck('accountsubmit')) {
    $uid = max(1, (int) ($_POST['uid'] ?? 0));
    $member = C::t('common_member')->fetch($uid);
    if (!$member) cpmsg('用户 UID 不存在', '', 'error');
    $mobile = trim((string) ($_POST['mobile'] ?? ''));
    if ($mobile !== '' && !preg_match('/^1[3-9]\d{9}$/', $mobile)) cpmsg('手机号格式错误', '', 'error');
    if ($mobile !== '') {
        $boundUid = (int) DB::result_first('SELECT uid FROM %t WHERE mobile=%s', ['stk_auth_mobile', $mobile]);
        if ($boundUid > 0 && $boundUid !== $uid) cpmsg('该手机号已经绑定其他用户', '', 'error');
        DB::query('DELETE FROM %t WHERE uid=%d', ['stk_auth_mobile', $uid]);
        DB::insert('stk_auth_mobile', ['uid'=>$uid,'mobile'=>$mobile,'verified'=>1,'created_at'=>TIMESTAMP,'updated_at'=>TIMESTAMP], false, true);
    }
    $memberStatus = in_array($_POST['member_status'] ?? 'inactive', ['inactive','active','expired','disabled'], true) ? (string) $_POST['member_status'] : 'inactive';
    DB::query('INSERT INTO %t (uid,status,member_label,level,expires_at,updated_at) VALUES (%d,%s,%s,%s,%d,%d) ON DUPLICATE KEY UPDATE status=VALUES(status),member_label=VALUES(member_label),level=VALUES(level),expires_at=VALUES(expires_at),updated_at=VALUES(updated_at)', ['stk_member_status',$uid,$memberStatus,trim((string) ($_POST['member_label'] ?? '普通用户')),trim((string) ($_POST['level'] ?? 'L1')),strtotime((string) ($_POST['expires_at'] ?? '')) ?: 0,TIMESTAMP]);
    DB::query('INSERT INTO %t (uid,commission_amount,task_points,updated_at) VALUES (%d,%s,%d,%d) ON DUPLICATE KEY UPDATE commission_amount=VALUES(commission_amount),task_points=VALUES(task_points),updated_at=VALUES(updated_at)', ['stk_wallet_account',$uid,(string) ($_POST['commission_amount'] ?? '0'),max(0,(int) ($_POST['task_points'] ?? 0)),TIMESTAMP]);
    stk_auth_security_event((int) ($_G['uid'] ?? 0), 'admin_account', 'success', ['target_uid' => $uid]);
    cpmsg('账户设置已保存','action=plugins&operation=config&do='.$pluginid.'&identifier=stk_auth&pmod=accounts','succeed');
}
showformheader('plugins&operation=config&do='.$pluginid.'&identifier=stk_auth&pmod=accounts');
showtableheader('账户与会员运营');
showsetting('用户 UID','uid','1','text'); showsetting('绑定手机号','mobile','','text', '', 0, '用于 App 密码登录；留空时不修改现有绑定。'); showsetting('会员状态','member_status','inactive','select','','','','',['inactive'=>'未开通','active'=>'有效','expired'=>'已过期','disabled'=>'已关闭']); showsetting('会员标签','member_label','普通用户','text'); showsetting('会员等级','level','L1','text'); showsetting('到期日期','expires_at','','text'); showsetting('佣金余额','commission_amount','0','text'); showsetting('任务积分','task_points','0','text');
showsubmit('accountsubmit','保存'); showtablefooter(); showformfooter();
$rows=DB::fetch_all('SELECT m.uid,m.username,a.mobile,s.status,s.member_label,s.level,b.commission_amount,b.task_points FROM %t m LEFT JOIN %t a ON a.uid=m.uid LEFT JOIN %t s ON s.uid=m.uid LEFT JOIN %t b ON b.uid=m.uid ORDER BY m.uid DESC LIMIT 100',['common_member','stk_auth_mobile','stk_member_status','stk_wallet_account']);
showtableheader('最近账户'); showtablerow('',[],['UID','用户名','手机号','状态','会员','等级','佣金','积分']);
foreach($rows as $row) showtablerow('',[],[$row['uid'],dhtmlspecialchars($row['username']),dhtmlspecialchars($row['mobile'] ?: '-'),dhtmlspecialchars($row['status'] ?: 'inactive'),dhtmlspecialchars($row['member_label'] ?: '普通用户'),dhtmlspecialchars($row['level'] ?: 'L1'),(string)($row['commission_amount'] ?? '0.00'),(string)($row['task_points'] ?? 0)]);
showtablefooter();
