<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';

$uid = max(0, (int) ($_GET['uid'] ?? 0));
showformheader('plugins&operation=config&do=' . $pluginid . '&identifier=stk_project&pmod=wallets', 'get');
showtableheader('账户展示查询（只读）');
showsetting('用户 UID', 'uid', $uid > 0 ? (string) $uid : '', 'text', '', 0, '本模块不提供资金增减、转账、提现或流水操作。');
showsubmit('', '查询');
showtablefooter();
showformfooter();

if ($uid > 0) {
    DB::query('INSERT IGNORE INTO %t (uid,commission_amount,task_amount,task_points,updated_at) SELECT uid,0,0,0,%d FROM %t WHERE uid=%d', ['stk_wallet_account',TIMESTAMP,'common_member',$uid]);
}
$where = $uid > 0 ? 'WHERE w.uid=' . $uid : '';
$rows = DB::fetch_all(
    'SELECT w.uid,m.username,w.commission_amount,w.task_amount,w.updated_at FROM %t w LEFT JOIN %t m ON m.uid=w.uid ' . $where . ' ORDER BY w.updated_at DESC,w.uid DESC LIMIT 200',
    ['stk_wallet_account','common_member']
);
showtableheader('账户列表');
showtablerow('', [], ['UID','用户名',stk_project_config('commission_label', '佣金账户'),stk_project_config('task_label', '任务账户'),'更新']);
foreach ($rows as $row) {
    showtablerow('', [], [(int) $row['uid'],dhtmlspecialchars($row['username'] ?: '-'),number_format((float) $row['commission_amount'], 2),number_format((float) $row['task_amount'], 2),dgmdate((int) $row['updated_at'])]);
}
showtablefooter();
