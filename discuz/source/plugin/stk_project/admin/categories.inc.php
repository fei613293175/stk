<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__).'/lib/config.php';

if (submitcheck('categorysubmit')) {
    $categoryId = strtolower(trim((string) ($_POST['category_id'] ?? '')));
    $name = trim((string) ($_POST['name'] ?? ''));
    if (!preg_match('/^[a-z0-9_]{2,32}$/', $categoryId) || $name === '') cpmsg('分类标识或名称不符合要求', '', 'error');
    DB::query(
        'INSERT INTO %t (category_id,name,sort_order,enabled) VALUES (%s,%s,%d,%d) ON DUPLICATE KEY UPDATE name=VALUES(name),sort_order=VALUES(sort_order),enabled=VALUES(enabled)',
        ['stk_project_category', $categoryId, $name, (int) ($_POST['sort_order'] ?? 0), !empty($_POST['enabled']) ? 1 : 0]
    );
    stk_project_admin_audit('categories','upsert',['category_id'=>$categoryId]);
    cpmsg('分类已保存', 'action=plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=categories', 'succeed');
}

showformheader('plugins&operation=config&do='.$pluginid.'&identifier=stk_project&pmod=categories');
showtableheader('新增或修改分类');
showsetting('分类标识', 'category_id', '', 'text', '', 0, '仅使用小写字母、数字和下划线；已有标识将被更新。');
showsetting('分类名称', 'name', '', 'text');
showsetting('排序', 'sort_order', '0', 'text');
showsetting('启用', 'enabled', '1', 'radio');
showsubmit('categorysubmit', '保存');
showtablefooter();
showformfooter();

$rows=DB::fetch_all('SELECT category_id,name,sort_order,enabled FROM %t ORDER BY sort_order,category_id',['stk_project_category']);
showtableheader('项目分类');
showtablerow('',[],['标识','名称','排序','状态']);
foreach($rows as $row) showtablerow('',[],[$row['category_id'],dhtmlspecialchars($row['name']),$row['sort_order'],$row['enabled']?'启用':'停用']);
showtablefooter();
