<?php

if (!defined('IN_ADMINCP')) {
    exit('Access Denied');
}

require_once dirname(__DIR__) . '/lib/config.php';
require_once dirname(__DIR__) . '/lib/auth_service.php';

if (submitcheck('settingssubmit')) {
    stk_auth_set_config('api_enabled', !empty($_POST['api_enabled']) ? '1' : '0');
    stk_auth_set_config('allow_password_login', !empty($_POST['allow_password_login']) ? '1' : '0');
    stk_auth_set_config('allow_sms_login', !empty($_POST['allow_sms_login']) ? '1' : '0');
    stk_auth_set_config('allow_register', !empty($_POST['allow_register']) ? '1' : '0');
    stk_auth_set_config('default_login_tab', ($_POST['default_login_tab'] ?? 'password') === 'sms' ? 'sms' : 'password');
    stk_auth_set_config('dev_fake_enabled', !empty($_POST['dev_fake_enabled']) ? '1' : '0');
    stk_auth_set_config('access_token_ttl', (string) max(300, min(86400, (int) ($_POST['access_token_ttl'] ?? 1800))));
    stk_auth_set_config('refresh_token_ttl', (string) max(3600, min(7776000, (int) ($_POST['refresh_token_ttl'] ?? 2592000))));
    stk_auth_set_config('password_min_length', (string) max(8, min(32, (int) ($_POST['password_min_length'] ?? 8))));
    stk_auth_set_config('password_max_length', (string) max(32, min(64, (int) ($_POST['password_max_length'] ?? 64))));
    stk_auth_set_config('login_fail_limit', (string) max(3, min(20, (int) ($_POST['login_fail_limit'] ?? 5))));
    stk_auth_set_config('login_lock_minutes', (string) max(1, min(1440, (int) ($_POST['login_lock_minutes'] ?? 15))));
    stk_auth_set_config('captcha_expire_seconds', (string) max(60, min(600, (int) ($_POST['captcha_expire_seconds'] ?? 300))));
    stk_auth_set_config('captcha_max_attempts', (string) max(3, min(10, (int) ($_POST['captcha_max_attempts'] ?? 5))));
    foreach (['login_success_route', 'register_success_route'] as $routeKey) {
        $successRoute = trim((string) ($_POST[$routeKey] ?? 'stk://home'));
        if (!preg_match('#^stk://(home|me|publish)$#', $successRoute)) cpmsg('成功路由只能使用 stk://home、stk://me 或 stk://publish', '', 'error');
        stk_auth_set_config($routeKey, $successRoute);
    }
    foreach (['legal_user_agreement_url', 'legal_privacy_url'] as $legalKey) {
        $legalUrl = trim((string) ($_POST[$legalKey] ?? ''));
        $isLocal = strpos($legalUrl, '/') === 0 && strpos($legalUrl, '//') !== 0;
        $isHttps = filter_var($legalUrl, FILTER_VALIDATE_URL) && strtolower((string) parse_url($legalUrl, PHP_URL_SCHEME)) === 'https';
        if (!$isLocal && !$isHttps) cpmsg('协议入口必须是站内绝对路径或 HTTPS URL', '', 'error');
        stk_auth_set_config($legalKey, $legalUrl);
    }
    stk_auth_set_config('sms_retention_days', (string) max(1, min(3650, (int) ($_POST['sms_retention_days'] ?? 90))));
    stk_auth_set_config('login_retention_days', (string) max(1, min(3650, (int) ($_POST['login_retention_days'] ?? 180))));
    $publicBaseUrl = rtrim(trim((string) ($_POST['public_base_url'] ?? '')), '/');
    if ($publicBaseUrl !== '' && (!filter_var($publicBaseUrl, FILTER_VALIDATE_URL) || parse_url($publicBaseUrl, PHP_URL_SCHEME) !== 'https')) {
        cpmsg('图形验证码公开地址必须是 HTTPS URL', '', 'error');
    }
    stk_auth_set_config('public_base_url', $publicBaseUrl);
    foreach (['user_agreement', 'privacy_policy'] as $documentId) {
        stk_auth_set_config('legal_' . $documentId . '_title', trim((string) ($_POST['legal_' . $documentId . '_title'] ?? '')));
        stk_auth_set_config('legal_' . $documentId . '_version', trim((string) ($_POST['legal_' . $documentId . '_version'] ?? '')));
        stk_auth_set_config('legal_' . $documentId . '_content', trim((string) ($_POST['legal_' . $documentId . '_content'] ?? '')));
    }
    stk_auth_security_event((int) ($_G['uid'] ?? 0), 'admin_config', 'success', ['section' => 'auth']);
    cpmsg('设置已保存', 'action=plugins&operation=config&do=' . $pluginid . '&identifier=stk_auth&pmod=settings', 'succeed');
}

showformheader('plugins&operation=config&do=' . $pluginid . '&identifier=stk_auth&pmod=settings');
showtableheader('商推客账户与认证 - 基础设置');
showsetting('API 开关', 'api_enabled', stk_auth_get_config('api_enabled', '1'), 'radio', '', 0, '关闭后客户端接口返回 503。');
showsetting('允许密码登录', 'allow_password_login', stk_auth_get_config('allow_password_login', '1'), 'radio');
showsetting('允许短信登录', 'allow_sms_login', stk_auth_get_config('allow_sms_login', '1'), 'radio', '', 0, '此开关控制客户端是否显示和调用短信登录；阿里云短信页的开关控制供应商发送能力。');
showsetting('允许新用户注册', 'allow_register', stk_auth_get_config('allow_register', '1'), 'radio');
showsetting('默认登录方式', 'default_login_tab', stk_auth_get_config('default_login_tab', 'password'), 'select', '', 0, '', '', ['password' => '密码登录', 'sms' => '短信登录']);
showsetting('开发 Fake 开关', 'dev_fake_enabled', stk_auth_get_config('dev_fake_enabled', '0'), 'radio', '', 0, '仅受控测试环境开启；生产环境必须关闭。');
showsetting('访问令牌有效期（秒）', 'access_token_ttl', stk_auth_get_config('access_token_ttl', '1800'), 'text');
showsetting('刷新令牌有效期（秒）', 'refresh_token_ttl', stk_auth_get_config('refresh_token_ttl', '2592000'), 'text');
showsetting('密码最小长度', 'password_min_length', stk_auth_get_config('password_min_length', '8'), 'text');
showsetting('密码最大长度', 'password_max_length', stk_auth_get_config('password_max_length', '64'), 'text');
showsetting('失败锁定阈值', 'login_fail_limit', stk_auth_get_config('login_fail_limit', '5'), 'text');
showsetting('锁定窗口（分钟）', 'login_lock_minutes', stk_auth_get_config('login_lock_minutes', '15'), 'text');
showsetting('安全验证码有效期（秒）', 'captcha_expire_seconds', stk_auth_get_config('captcha_expire_seconds', '300'), 'text');
showsetting('安全验证码最大错误次数', 'captcha_max_attempts', stk_auth_get_config('captcha_max_attempts', '5'), 'text');
showsetting('登录成功路由', 'login_success_route', stk_auth_get_config('login_success_route', 'stk://home'), 'text');
showsetting('注册成功路由', 'register_success_route', stk_auth_get_config('register_success_route', 'stk://home'), 'text');
showsetting('短信记录保留天数', 'sms_retention_days', stk_auth_get_config('sms_retention_days', '90'), 'text');
showsetting('登录日志保留天数', 'login_retention_days', stk_auth_get_config('login_retention_days', '180'), 'text');
showsetting('图形验证码公开 HTTPS 地址', 'public_base_url', stk_auth_get_config('public_base_url', ''), 'text', '', 0, '留空时使用 Discuz siteurl；若 siteurl 不是完整 HTTPS 地址，必须在此配置，例如 https://stk.zz-yihao.com。');
showtablerow('', [], ['短信登录', stk_auth_get_config('sms_enabled', '0') === '1' ? '已开启；配置在“阿里云短信”页面' : '已关闭；配置在“阿里云短信”页面']);
showtableheader('协议与隐私（版本化配置）');
showsetting('用户协议入口', 'legal_user_agreement_url', stk_auth_get_config('legal_user_agreement_url', '/legal/user-agreement'), 'text');
showsetting('隐私政策入口', 'legal_privacy_url', stk_auth_get_config('legal_privacy_url', '/legal/privacy'), 'text');
showsetting('用户协议标题', 'legal_user_agreement_title', stk_auth_get_config('legal_user_agreement_title', '用户协议'), 'text');
showsetting('用户协议版本', 'legal_user_agreement_version', stk_auth_get_config('legal_user_agreement_version', '1.4.0'), 'text');
showsetting('用户协议内容', 'legal_user_agreement_content', stk_auth_get_config('legal_user_agreement_content', ''), 'textarea');
showsetting('隐私政策标题', 'legal_privacy_policy_title', stk_auth_get_config('legal_privacy_policy_title', '隐私政策'), 'text');
showsetting('隐私政策版本', 'legal_privacy_policy_version', stk_auth_get_config('legal_privacy_policy_version', '1.4.0'), 'text');
showsetting('隐私政策内容', 'legal_privacy_policy_content', stk_auth_get_config('legal_privacy_policy_content', ''), 'textarea');
showsubmit('settingssubmit', '保存');
showtablefooter();
showformfooter();
