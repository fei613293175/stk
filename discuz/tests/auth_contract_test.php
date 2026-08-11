<?php

$root = dirname(__DIR__);
$service = file_get_contents($root . '/source/plugin/stk_auth/lib/auth_service.php');
$routes = file_get_contents($root . '/source/plugin/stk_auth/lib/routes.php');
$api = file_get_contents($root . '/source/plugin/stk_auth/api.inc.php');
$captcha = file_get_contents($root . '/source/plugin/stk_auth/captcha.inc.php');
$install = file_get_contents($root . '/source/plugin/stk_auth/install.php');
$upgrade = file_get_contents($root . '/source/plugin/stk_auth/upgrade.php');
$sms = file_get_contents($root . '/source/plugin/stk_auth/lib/sms_service.php');
$manifest = file_get_contents($root . '/source/plugin/stk_auth/discuz_plugin_stk_auth.json');
$captchaAdmin = file_get_contents($root . '/source/plugin/stk_auth/admin/captcha_records.inc.php');

foreach ([$service, $routes, $api, $captcha, $install, $upgrade, $sms, $manifest, $captchaAdmin] as $file) {
    if ($file === false) {
        fwrite(STDERR, "FAIL: missing auth source\n");
        exit(1);
    }
}
foreach (['code_audit_ciphertext', 'stk_auth_encrypt_secret($code)', 'smsrevealsubmit', 'reveal_confirmation', 'admin_sms_reveal'] as $needle) {
    if (strpos($sms . $install . $upgrade . $captchaAdmin, $needle) === false) {
        fwrite(STDERR, "FAIL: missing encrypted SMS audit contract {$needle}\n");
        exit(1);
    }
}

foreach (['health/live', 'health/ready', 'api/v1/bootstrap', 'captcha_verify', 'stk_auth_issue_captcha_ticket', 'refresh', 'account_overview'] as $needle) {
    if (strpos($routes . $api . $service, $needle) === false) {
        fwrite(STDERR, "FAIL: missing canonical auth contract {$needle}\n");
        exit(1);
    }
}
foreach (['openssl_encrypt', 'aes-256-gcm', 'dysmsapi.aliyuncs.com', 'attempt_count', 'phone_hour_limit', 'sms_resend_seconds', 'stk_auth_sms_retry_after', 'provider_request_id'] as $needle) {
    if (strpos($sms . $install . $upgrade, $needle) === false) {
        fwrite(STDERR, "FAIL: missing production SMS contract {$needle}\n");
        exit(1);
    }
}
foreach (['sms_settings', 'captcha_records', 'security_logs', 'diagnostics', 'accounts', 'display_settings'] as $needle) {
    if (strpos($manifest, $needle) === false) {
        fwrite(STDERR, "FAIL: missing auth admin module {$needle}\n");
        exit(1);
    }
}

foreach (['stk_auth_captcha', 'random_bytes(16)', 'imagecreatetruecolor', 'attempt_count', 'DB::update(', 'substr(preg_replace', 'client_ip', 'configuredUrl', 'stk_auth_public_base_url', 'global $_G'] as $needle) {
    if (strpos($service, $needle) === false) {
        fwrite(STDERR, "FAIL: missing captcha security contract {$needle}\n");
        exit(1);
    }
}
if (strpos($service, 'expires_at<%d OR status IN') !== false) {
    fwrite(STDERR, "FAIL: captcha rate-limit history must not be deleted on use or lock\n");
    exit(1);
}
if (strpos($service, "DB::insert('stk_auth_sms_code'") !== false) {
    fwrite(STDERR, "FAIL: captcha challenges must not be written to SMS records\n");
    exit(1);
}
foreach (['stk_auth_captcha', 'imagecreatetruecolor', 'Cache-Control: no-store'] as $needle) {
    if (strpos($captcha, $needle) === false) {
        fwrite(STDERR, "FAIL: missing captcha image contract {$needle}\n");
        exit(1);
    }
}
if (strpos($service, "'prompt' => '请输入图中的 4 位数字'") === false) {
    fwrite(STDERR, "FAIL: captcha prompt must state the 4-digit requirement\n");
    exit(1);
}
foreach (['stk_auth_captcha', 'public_base_url'] as $needle) {
    if (strpos($install . $upgrade, $needle) === false) {
        fwrite(STDERR, "FAIL: missing captcha migration {$needle}\n");
        exit(1);
    }
}
if (strpos($api, "'answer'") !== false) {
    fwrite(STDERR, "FAIL: production captcha answer must not be serialized\n");
    exit(1);
}
if (strpos($service, "'content' => '欢迎使用商推客。\\n") !== false) {
    fwrite(STDERR, "FAIL: legal fallback must use actual newlines\n");
    exit(1);
}
if (strpos($service, "'stk' . \$mobile") !== false) {
    fwrite(STDERR, "FAIL: public username must not contain the mobile number\n");
    exit(1);
}
foreach (["'stk_' . bin2hex(random_bytes(6))", "'stk_auth_mobile', 'stk_member_status', 'stk_wallet_account'", "'stk_account_membership', 'stk_account_balance'", "C::t('common_member')->delete"] as $needle) {
    if (strpos($service, $needle) === false) {
        fwrite(STDERR, "FAIL: missing registration privacy/rollback contract {$needle}\n");
        exit(1);
    }
}
foreach (['pre_stk_app_config', 'pre_stk_auth_refresh_token', 'pre_stk_member_status', 'pre_stk_wallet_account', 'pre_stk_auth_request_guard'] as $needle) {
    if (strpos($install . $upgrade, $needle) === false) {
        fwrite(STDERR, "FAIL: missing canonical auth entity {$needle}\n");
        exit(1);
    }
}
foreach (['allow_sms_login', 'legal_user_agreement_url', 'legal_privacy_url', 'sms_retention_days', 'login_retention_days'] as $needle) {
    if (strpos($api . $install . $upgrade, $needle) === false) {
        fwrite(STDERR, "FAIL: missing auth configuration behavior {$needle}\n");
        exit(1);
    }
}
foreach (['stk_auth_cleanup_retained_data', 'stk_auth_write_guard', 'HTTP_X_STK_REQUEST_ID', 'retention_cleanup'] as $needle) {
    if (strpos($api . $service, $needle) === false) {
        fwrite(STDERR, "FAIL: missing auth lifecycle/security behavior {$needle}\n");
        exit(1);
    }
}

echo "PASS: auth_contract_test\n";
