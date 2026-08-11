<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
$query = trim((string) ($_GET['mobile'] ?? ''));
$params = ['common_member', 'stk_auth_mobile'];
$where = '';
if ($query !== '') { $where = ' WHERE a.mobile=%s'; $params[] = $query; }
$params[] = 200;
$rows = DB::fetch_all('SELECT m.uid,m.username,m.email,a.mobile,a.verified,a.created_at FROM %t m INNER JOIN %t a ON a.uid=m.uid' . $where . ' ORDER BY a.created_at DESC LIMIT %d', $params);
showformheader('plugins&operation=config&do=' . $pluginid . '&identifier=stk_auth&pmod=users', 'get');
showtableheader('注册用户查询'); showsetting('完整手机号精确查询', 'mobile', $query, 'text'); showsubmit('', '查询'); showtablefooter(); showformfooter();
showtableheader('注册用户'); showtablerow('', [], ['UID', '用户名', '手机号', '短信已验证', '注册时间']);
foreach ($rows as $row) showtablerow('', [], [(int) $row['uid'], dhtmlspecialchars($row['username']), dhtmlspecialchars(stk_auth_mask_mobile($row['mobile'])), (int) $row['verified'] === 1 ? '是' : '否', dgmdate((int) $row['created_at'])]);
showtablefooter();

