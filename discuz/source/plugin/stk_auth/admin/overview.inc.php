<?php

if (!defined('IN_ADMINCP')) {
    exit('Access Denied');
}

require_once dirname(__DIR__) . '/lib/config.php';

$boundMobiles = (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE verified=1', ['stk_auth_mobile']);
$activeTokens = (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE token_type=%s AND revoked_at=0 AND expires_at>%d', ['stk_auth_token', 'access', TIMESTAMP]);
$todayLogins = (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE result=%s AND created_at>=%d', ['stk_auth_login_log', 'success', strtotime('today')]);
showtableheader('商推客账户与认证 - 概览');
showtablerow('', ['width="220"', ''], ['插件版本', '1.3.0']);
showtablerow('', [], ['API 状态', stk_auth_get_config('api_enabled', '1') === '1' ? '已开启' : '已关闭']);
showtablerow('', [], ['开发 Fake', stk_auth_get_config('dev_fake_enabled', '0') === '1' ? '已开启（仅测试）' : '已关闭']);
showtablerow('', [], ['密码认证', '已接入 Discuz 用户体系']);
showtablerow('', [], ['图形验证码', function_exists('imagecreatetruecolor') ? 'GD 已启用' : '不可用：服务器需启用 PHP GD 扩展']);
showtablerow('', [], ['验证码地址', stk_auth_get_config('public_base_url', '') !== '' ? '使用后台 HTTPS 配置' : '使用 Discuz siteurl（若非 HTTPS 请在认证设置配置）']);
showtablerow('', [], ['短信认证', stk_auth_get_config('sms_enabled', '0') === '1' ? '已配置' : '未配置（客户端应使用密码登录）']);
showtablerow('', [], ['已绑定手机号', (string) $boundMobiles]);
showtablerow('', [], ['有效访问令牌', (string) $activeTokens]);
showtablerow('', [], ['今日成功登录', (string) $todayLogins]);
showtablefooter();
