<?php

define('IN_DISCUZ', true);
require_once __DIR__ . '/../source/plugin/stk_auth/lib/dev_auth_service.php';

function assert_true(bool $condition, string $message): void
{
    if (!$condition) {
        fwrite(STDERR, "FAIL: {$message}\n");
        exit(1);
    }
}

StkAuthDevService::validateMobile('13800138000');
StkAuthDevService::validateCaptcha('dev-login', '2468');
$data = StkAuthDevService::authData('13800138000');
assert_true($data['user']['mobile_masked'] === '138****8000', 'mobile masking');
assert_true($data['user']['username'] === '商推客用户8000', 'username generation');

echo "PASS: dev_auth_service_test\n";
