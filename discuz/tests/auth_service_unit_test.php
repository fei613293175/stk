<?php

define('IN_DISCUZ', true);

$config = [
    'captcha_secret' => str_repeat('a', 64),
    'public_base_url' => 'https://stk.zz-yihao.com',
];

function stk_auth_get_config(string $key, string $default = ''): string
{
    global $config;
    return (string) ($config[$key] ?? $default);
}

function stk_auth_set_config(string $key, string $value): void
{
    global $config;
    $config[$key] = $value;
}

function assert_auth_true(bool $condition, string $message): void
{
    if (!$condition) {
        fwrite(STDERR, "FAIL: {$message}\n");
        exit(1);
    }
}

require_once __DIR__ . '/../source/plugin/stk_auth/lib/auth_service.php';
require_once __DIR__ . '/../source/plugin/stk_auth/lib/sms_service.php';

$_G = ['siteurl' => 'http://insecure.example'];
assert_auth_true(stk_auth_public_base_url() === 'https://stk.zz-yihao.com', 'configured HTTPS captcha URL');

$config['public_base_url'] = '';
try {
    stk_auth_public_base_url();
    assert_auth_true(false, 'insecure siteurl must be rejected');
} catch (RuntimeException $error) {
    assert_auth_true($error->getCode() === 5032, 'insecure siteurl error code');
}

$config['public_base_url'] = 'https://stk.zz-yihao.com';
$document = stk_auth_legal_document('user_agreement');
assert_auth_true(strpos($document['content'], "\n\n") !== false, 'legal fallback has real newlines');
assert_auth_true(strpos($document['content'], '\\n') === false, 'legal fallback has no escaped newline text');

$code = stk_auth_captcha_code(str_repeat('b', 32));
assert_auth_true((bool) preg_match('/^\d{4}$/', $code), 'captcha code has four digits');
assert_auth_true($code === stk_auth_captcha_code(str_repeat('b', 32)), 'captcha code is stable for challenge');

assert_auth_true(stk_auth_sms_retry_after(1000, 1030, 60) === 30, 'SMS resend window returns remaining seconds');
assert_auth_true(stk_auth_sms_retry_after(1000, 1060, 60) === 0, 'SMS resend window expires exactly on time');
assert_auth_true(stk_auth_login_retry_after(1000, 1030, 15) === 870, 'login lock returns exact remaining seconds');
assert_auth_true(stk_auth_login_retry_after(1000, 1900, 15) === 0, 'login lock expires exactly on time');

echo "PASS: auth_service_unit_test\n";
