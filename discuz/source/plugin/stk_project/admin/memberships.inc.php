<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';

if (submitcheck('membershipsubmit')) {
    $uid = max(1, (int) ($_POST['uid'] ?? 0));
    if (!C::t('common_member')->fetch($uid)) cpmsg('用户不存在', '', 'error');
    $status = in_array($_POST['member_status'] ?? 'inactive', ['inactive','active','expired','disabled'], true)
        ? (string) $_POST['member_status'] : 'inactive';
    $startsAt = strtotime(trim((string) ($_POST['starts_at'] ?? ''))) ?: 0;
    $expiresAt = strtotime(trim((string) ($_POST['expires_at'] ?? ''))) ?: 0;
    if ($status === 'active' && $expiresAt > 0 && $expiresAt <= $startsAt) cpmsg('到期时间必须晚于开通时间', '', 'error');
    DB::query(
        'INSERT INTO %t (uid,status,member_label,level,starts_at,expires_at,updated_at) VALUES (%d,%s,%s,%s,%d,%d,%d) '
        . 'ON DUPLICATE KEY UPDATE status=VALUES(status),member_label=VALUES(member_label),level=VALUES(level),starts_at=VALUES(starts_at),expires_at=VALUES(expires_at),updated_at=VALUES(updated_at)',
        ['stk_member_status',$uid,$status,trim((string) ($_POST['member_label'] ?? '普通用户')),trim((string) ($_POST['level'] ?? 'L1')),$startsAt,$expiresAt,TIMESTAMP]
    );
    stk_project_admin_audit('membership', 'status_update', ['uid' => $uid, 'status' => $status, 'starts_at' => $startsAt, 'expires_at' => $expiresAt]);
    cpmsg('会员状态已保存', 'action=plugins&operation=config&do=' . $pluginid . '&identifier=stk_project&pmod=memberships', 'succeed');
}

showformheader('plugins&operation=config&do=' . $pluginid . '&identifier=stk_project&pmod=memberships');
showtableheader('会员状态管理');
showsetting('用户 UID', 'uid', '1', 'text');
showsetting('会员状态', 'member_status', 'inactive', 'select', '', '', '', '', ['inactive'=>'未开通','active'=>'有效','expired'=>'已过期','disabled'=>'已关闭']);
showsetting('会员标签', 'member_label', '普通用户', 'text');
showsetting('会员等级', 'level', 'L1', 'text');
showsetting('开通时间', 'starts_at', '', 'text', '', 0, '格式：YYYY-MM-DD HH:MM:SS；未开通可留空。');
showsetting('到期时间', 'expires_at', '', 'text', '', 0, '人工续期时填写新的到期时间。');
showsubmit('membershipsubmit', '保存');
showtablefooter();
showformfooter();

$rows = DB::fetch_all('SELECT uid,status,member_label,level,starts_at,expires_at,updated_at FROM %t ORDER BY updated_at DESC LIMIT 200', ['stk_member_status']);
showtableheader('会员列表');
showtablerow('', [], ['UID','状态','标签','等级','开通','到期','更新']);
foreach ($rows as $row) {
    showtablerow('', [], [
        (int) $row['uid'], dhtmlspecialchars($row['status']), dhtmlspecialchars($row['member_label']), dhtmlspecialchars($row['level']),
        $row['starts_at'] ? dgmdate((int) $row['starts_at'], 'Y-m-d') : '-',
        $row['expires_at'] ? dgmdate((int) $row['expires_at'], 'Y-m-d') : '-', dgmdate((int) $row['updated_at']),
    ]);
}
showtablefooter();
