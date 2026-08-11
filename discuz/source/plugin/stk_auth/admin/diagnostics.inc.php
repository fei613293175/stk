<?php
if (!defined('IN_ADMINCP')) exit('Access Denied');
require_once dirname(__DIR__) . '/lib/config.php';
$checks = [
    ['PHP', PHP_VERSION, version_compare(PHP_VERSION, '7.4.0', '>=')],
    ['数据库', (string) DB::result_first('SELECT DATABASE()'), true],
    ['GD 图形验证码', function_exists('imagecreatetruecolor') ? '可用' : '缺失', function_exists('imagecreatetruecolor')],
    ['OpenSSL 密钥保护', function_exists('openssl_encrypt') ? '可用' : '缺失', function_exists('openssl_encrypt')],
    ['cURL 短信连接', function_exists('curl_init') ? '可用' : '使用 PHP HTTPS Stream', true],
    ['公开 HTTPS 地址', stk_auth_get_config('public_base_url', ''), strpos(stk_auth_get_config('public_base_url', ''), 'https://') === 0],
    ['开发 Fake', stk_auth_get_config('dev_fake_enabled', '0') === '1' ? '已开启' : '已关闭', stk_auth_get_config('dev_fake_enabled', '0') !== '1'],
    ['生产短信', stk_auth_get_config('sms_enabled', '0') === '1' ? '已开启' : '未开启', true],
];
showtableheader('认证系统检测'); showtablerow('', [], ['检测项', '结果', '状态']);
foreach ($checks as $check) showtablerow('', [], [dhtmlspecialchars($check[0]), dhtmlspecialchars($check[1] !== '' ? $check[1] : '未配置'), $check[2] ? '通过' : '需处理']);
showtablefooter();

