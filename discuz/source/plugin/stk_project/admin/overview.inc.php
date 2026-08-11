<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
$total=DB::result_first('SELECT COUNT(*) FROM %t',['stk_project']);
$published=DB::result_first('SELECT COUNT(*) FROM %t WHERE status=%s',['stk_project','published']);
$views=DB::result_first('SELECT COALESCE(SUM(view_count),0) FROM %t',['stk_project']);
$pending=DB::result_first('SELECT COUNT(*) FROM %t WHERE status=%s AND deleted_at=0',['stk_project','pending']);
showtableheader('商推客项目 - V1.3.0 概览');
showtablerow('',[],['项目总数',(string)$total]);
showtablerow('',[],['公开项目',(string)$published]);
showtablerow('',[],['累计浏览',(string)$views]);
showtablerow('',[],['待审核',(string)$pending]);
showtablefooter();
