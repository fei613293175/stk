<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

function stk_auth_mobile(string $mobile): string
{
    $mobile = trim($mobile);
    if (!preg_match('/^1[3-9]\d{9}$/', $mobile)) {
        throw new InvalidArgumentException('请输入正确的 11 位手机号', 4001);
    }
    return $mobile;
}

function stk_auth_mask_mobile(string $mobile): string
{
    return strlen($mobile) === 11 ? substr($mobile, 0, 3) . '****' . substr($mobile, -4) : '';
}

function stk_auth_login_retry_after(int $oldestFailureAt, int $now, int $lockMinutes): int
{
    $windowSeconds = max(1, $lockMinutes) * 60;
    return max(0, $oldestFailureAt + $windowSeconds - $now);
}

function stk_auth_client_ip(): string
{
    global $_G;
    return substr((string) ($_G['clientip'] ?? $_SERVER['REMOTE_ADDR'] ?? ''), 0, 64);
}

function stk_auth_public_base_url(): string
{
    global $_G;
    $configuredUrl = trim(stk_auth_get_config('public_base_url', ''));
    $baseUrl = rtrim($configuredUrl !== '' ? $configuredUrl : (string) ($_G['siteurl'] ?? ''), '/');
    $parts = parse_url($baseUrl);
    if (!is_array($parts) || ($parts['scheme'] ?? '') !== 'https' || empty($parts['host'])) {
        throw new RuntimeException('图形验证码公开 HTTPS 地址未配置', 5032);
    }
    return $baseUrl;
}

function stk_auth_bearer_token(): string
{
    $header = (string) ($_SERVER['HTTP_AUTHORIZATION'] ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ?? '');
    return preg_match('/^Bearer\s+(.+)$/i', trim($header), $match) === 1 ? trim($match[1]) : '';
}

function stk_auth_token_uid(string $token, string $type = 'access'): int
{
    if ($token === '') {
        return 0;
    }
    if ($type === 'refresh') {
        $uid = (int) DB::result_first(
            'SELECT uid FROM %t WHERE token_hash=%s AND revoked_at=0 AND expires_at>%d',
            ['stk_auth_refresh_token', hash('sha256', $token), TIMESTAMP]
        );
        if ($uid > 0) return $uid;
    }
    return (int) DB::result_first(
        'SELECT uid FROM %t WHERE token_hash=%s AND token_type=%s AND revoked_at=0 AND expires_at>%d',
        ['stk_auth_token', hash('sha256', $token), $type, TIMESTAMP]
    );
}

function stk_auth_issue_tokens(int $uid): array
{
    $accessToken = bin2hex(random_bytes(32));
    $refreshToken = bin2hex(random_bytes(32));
    $accessTtl = max(300, (int) stk_auth_get_config('access_token_ttl', '1800'));
    $refreshTtl = max(3600, (int) stk_auth_get_config('refresh_token_ttl', '2592000'));

    DB::query('DELETE FROM %t WHERE expires_at<%d OR revoked_at>0', ['stk_auth_token', TIMESTAMP - 86400]);
    DB::insert('stk_auth_token', [
        'uid' => $uid,
        'token_hash' => hash('sha256', $accessToken),
        'token_type' => 'access',
        'expires_at' => TIMESTAMP + $accessTtl,
        'created_at' => TIMESTAMP,
    ]);
    DB::insert('stk_auth_refresh_token', [
        'uid' => $uid,
        'token_hash' => hash('sha256', $refreshToken),
        'device_id' => substr((string) ($_SERVER['HTTP_X_STK_DEVICE_ID'] ?? ''), 0, 128),
        'expires_at' => TIMESTAMP + $refreshTtl,
        'created_at' => TIMESTAMP,
    ]);
    DB::insert('stk_auth_token', [
        'uid' => $uid,
        'token_hash' => hash('sha256', $refreshToken),
        'token_type' => 'refresh',
        'expires_at' => TIMESTAMP + $refreshTtl,
        'created_at' => TIMESTAMP,
    ]);

    return [
        'access_token' => $accessToken,
        'refresh_token' => $refreshToken,
        'expires_at' => TIMESTAMP + $accessTtl,
    ];
}

function stk_auth_revoke_token(string $token): void
{
    if ($token !== '') {
        DB::query(
            'UPDATE %t SET revoked_at=%d WHERE token_hash=%s AND revoked_at=0',
            ['stk_auth_token', TIMESTAMP, hash('sha256', $token)]
        );
        DB::query(
            'UPDATE %t SET revoked_at=%d WHERE token_hash=%s AND revoked_at=0',
            ['stk_auth_refresh_token', TIMESTAMP, hash('sha256', $token)]
        );
    }
}

function stk_auth_revoke_session(string $accessToken): void
{
    $uid = stk_auth_token_uid($accessToken, 'access');
    if ($uid > 0) {
        DB::update('stk_auth_token', ['revoked_at' => TIMESTAMP], ['uid' => $uid, 'revoked_at' => 0]);
        DB::update('stk_auth_refresh_token', ['revoked_at' => TIMESTAMP], ['uid' => $uid, 'revoked_at' => 0]);
    }
}

function stk_auth_security_event(int $uid, string $eventType, string $result, array $metadata = []): void
{
    DB::insert('stk_security_event', [
        'uid' => max(0, $uid),
        'event_type' => substr($eventType, 0, 32),
        'result' => substr($result, 0, 16),
        'ip' => stk_auth_client_ip(),
        'metadata_json' => json_encode($metadata, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
        'created_at' => TIMESTAMP,
    ]);
}

function stk_auth_cleanup_retained_data(): void
{
    $lastCleanup = (int) stk_auth_get_config('last_cleanup_at', '0');
    if ($lastCleanup >= TIMESTAMP - 86400) return;

    try {
        // Claim the daily cleanup window first so concurrent API requests do not repeat it.
        stk_auth_set_config('last_cleanup_at', (string) TIMESTAMP);
        $smsDays = max(1, min(3650, (int) stk_auth_get_config('sms_retention_days', '90')));
        $loginDays = max(1, min(3650, (int) stk_auth_get_config('login_retention_days', '180')));
        DB::query('DELETE FROM %t WHERE created_at<%d', ['stk_auth_sms_code', TIMESTAMP - $smsDays * 86400]);
        DB::query('DELETE FROM %t WHERE created_at<%d', ['stk_auth_login_log', TIMESTAMP - $loginDays * 86400]);
        DB::query('DELETE FROM %t WHERE expires_at<%d', ['stk_auth_captcha', TIMESTAMP - 86400]);
        DB::query('DELETE FROM %t WHERE expires_at<%d', ['stk_auth_captcha_ticket', TIMESTAMP - 86400]);
        DB::query('DELETE FROM %t WHERE expires_at<%d OR (revoked_at>0 AND revoked_at<%d)', ['stk_auth_token', TIMESTAMP - 86400, TIMESTAMP - 86400]);
        DB::query('DELETE FROM %t WHERE expires_at<%d OR (revoked_at>0 AND revoked_at<%d)', ['stk_auth_refresh_token', TIMESTAMP - 86400, TIMESTAMP - 86400]);
        stk_auth_security_event(0, 'retention_cleanup', 'success', [
            'sms_days' => $smsDays,
            'login_days' => $loginDays,
        ]);
    } catch (Throwable $ignored) {
        // Retention maintenance must never make authentication unavailable.
    }
}

function stk_auth_write_guard(string $action): void
{
    $requestId = trim((string) ($_SERVER['HTTP_X_STK_IDEMPOTENCY_KEY'] ?? $_SERVER['HTTP_X_STK_REQUEST_ID'] ?? ''));
    if ($requestId === '' || strlen($requestId) > 128 || !preg_match('/^[A-Za-z0-9._:-]{8,128}$/', $requestId)) {
        throw new InvalidArgumentException('缺少或无效的 App 请求标识', 4006);
    }
    $action = substr($action, 0, 64);
    $clientKey = hash('sha256', stk_auth_client_ip() . '|' . substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 200));
    $limit = $action === 'captcha_create' ? 120 : 60;
    $recent = (int) DB::result_first(
        'SELECT COUNT(*) FROM %t WHERE client_key=%s AND action=%s AND created_at>%d',
        ['stk_auth_request_guard', $clientKey, $action, TIMESTAMP - 60]
    );
    if ($recent >= $limit) throw new InvalidArgumentException('操作过于频繁，请稍后再试', 4290);
    $requestHash = hash('sha256', $clientKey . '|' . $action . '|' . $requestId);
    DB::query(
        'INSERT IGNORE INTO %t (request_hash,client_key,action,created_at) VALUES (%s,%s,%s,%d)',
        ['stk_auth_request_guard', $requestHash, $clientKey, $action, TIMESTAMP]
    );
    if ((int) DB::result_first('SELECT ROW_COUNT()') !== 1) {
        throw new InvalidArgumentException('重复请求已被阻止', 4093);
    }
}

function stk_auth_legal_document(string $documentId): array
{
    $documents = [
        'user_agreement' => [
            'title' => '用户协议',
            'version' => '1.4.0',
            'content' => "欢迎使用商推客。\n\n商推客为用户提供推广项目的浏览、发布及相关服务。用户应保证提交内容真实、合法，不得发布违法、侵权、欺诈或误导性信息。\n\n平台可依据服务规则对项目进行审核、下架或限制访问。项目展示不构成任何交易、收益或合作承诺。",
        ],
        'privacy_policy' => [
            'title' => '隐私政策',
            'version' => '1.4.0',
            'content' => "商推客仅在提供登录、项目发布和安全验证服务所必需的范围内处理账户、设备和业务数据。\n\n手机号、登录令牌及验证码用于身份认证与安全防护；项目图片及联系方式仅在用户主动提交后按服务规则处理。\n\n客户端不会将非白名单外部链接直接作为可执行操作。你可随时退出登录，客户端会清理本地敏感会话数据。",
        ],
    ];
    if (!isset($documents[$documentId])) {
        throw new InvalidArgumentException('未知协议文档', 4001);
    }
    $default = $documents[$documentId];
    $prefix = 'legal_' . $documentId . '_';
    $title = trim(stk_auth_get_config($prefix . 'title', $default['title']));
    $version = trim(stk_auth_get_config($prefix . 'version', $default['version']));
    $content = trim(stk_auth_get_config($prefix . 'content', $default['content']));
    return [
        'document_id' => $documentId,
        'title' => $title !== '' ? $title : $default['title'],
        'version' => $version !== '' ? $version : $default['version'],
        'content' => $content !== '' ? $content : $default['content'],
    ];
}

function stk_auth_captcha_secret(): string
{
    $secret = stk_auth_get_config('captcha_secret', '');
    if (strlen($secret) < 32) {
        $secret = bin2hex(random_bytes(32));
        stk_auth_set_config('captcha_secret', $secret);
    }
    return $secret;
}

function stk_auth_captcha_code(string $challengeId): string
{
    $hash = hash_hmac('sha256', $challengeId, stk_auth_captcha_secret());
    $code = '';
    for ($index = 0; $index < 4; $index++) {
        $code .= (string) (hexdec($hash[$index]) % 10);
    }
    return $code;
}

function stk_auth_create_captcha(string $scene): array
{
    $scene = substr(preg_replace('/[^a-z_]/', '', strtolower($scene)) ?: 'unknown', 0, 32);
    if (!function_exists('imagecreatetruecolor')) {
        throw new RuntimeException('PHP GD 扩展未启用', 5032);
    }
    $captchaBaseUrl = stk_auth_public_base_url();
    $clientIp = stk_auth_client_ip();
    $recentCount = (int) DB::result_first(
        'SELECT COUNT(*) FROM %t WHERE client_ip=%s AND scene=%s AND created_at>%d',
        ['stk_auth_captcha', $clientIp, $scene, TIMESTAMP - 60]
    );
    if ($recentCount >= 5) {
        throw new InvalidArgumentException('安全验证请求过于频繁，请稍后再试', 4290);
    }
    // Keep used and locked challenges for the rate-limit and audit window instead of deleting
    // them on the next request. Their short expiration still prevents reuse.
    DB::query('DELETE FROM %t WHERE expires_at<%d', ['stk_auth_captcha', TIMESTAMP - 86400]);
    $challengeId = bin2hex(random_bytes(16));
    $code = stk_auth_captcha_code($challengeId);
    $expiresIn = max(60, min(600, (int) stk_auth_get_config('captcha_expire_seconds', '300')));
    DB::insert('stk_auth_captcha', [
        'challenge_id' => $challengeId,
        'scene' => $scene,
        'code_hash' => password_hash($code, PASSWORD_DEFAULT),
        'client_ip' => $clientIp,
        'attempt_count' => 0,
        'status' => 'active',
        'expires_at' => TIMESTAMP + $expiresIn,
        'created_at' => TIMESTAMP,
    ]);
    return [
        'challenge_id' => $challengeId,
        'prompt' => '请输入图中的 4 位数字',
        'image_url' => $captchaBaseUrl . '/plugin.php?id=stk_auth:captcha&challenge=' . rawurlencode($challengeId),
        'expires_in' => $expiresIn,
    ];
}

function stk_auth_validate_captcha(string $challengeId, string $code, string $scene): void
{
    $scene = substr(preg_replace('/[^a-z_]/', '', strtolower($scene)) ?: 'unknown', 0, 32);
    if (!preg_match('/^[a-f0-9]{32}$/', $challengeId)) {
        throw new InvalidArgumentException('安全验证码错误或已失效', 4002);
    }
    $clientIp = stk_auth_client_ip();
    $row = DB::fetch_first(
        'SELECT challenge_id,code_hash,attempt_count FROM %t WHERE challenge_id=%s AND scene=%s AND client_ip=%s AND status=%s AND expires_at>%d',
        ['stk_auth_captcha', $challengeId, $scene, $clientIp, 'active', TIMESTAMP]
    );
    if (!$row || !password_verify(trim($code), (string) $row['code_hash'])) {
        $attemptCount = (int) ($row['attempt_count'] ?? 0);
        $maxAttempts = max(3, min(10, (int) stk_auth_get_config('captcha_max_attempts', '5')));
        DB::update(
            'stk_auth_captcha',
            [
                'attempt_count' => $attemptCount + 1,
                'status' => $attemptCount + 1 >= $maxAttempts ? 'locked' : 'active',
            ],
            [
                'challenge_id' => $challengeId,
                'scene' => $scene,
                'client_ip' => $clientIp,
                'status' => 'active',
                'attempt_count' => $attemptCount,
            ]
        );
        stk_auth_log(0, '', 'captcha', 'failed', 4002);
        throw new InvalidArgumentException('安全验证码错误或已失效', 4002);
    }
    DB::query(
        'UPDATE %t SET status=%s,used_at=%d WHERE challenge_id=%s AND status=%s',
        ['stk_auth_captcha', 'used', TIMESTAMP, $challengeId, 'active']
    );
    if ((int) DB::affected_rows() !== 1) {
        throw new InvalidArgumentException('安全验证码错误或已失效', 4002);
    }
    stk_auth_log(0, '', 'captcha', 'success');
}

function stk_auth_issue_captcha_ticket(string $scene): array
{
    $scene = substr(preg_replace('/[^a-z_]/', '', strtolower($scene)) ?: 'unknown', 0, 32);
    $ticket = bin2hex(random_bytes(32));
    DB::insert('stk_auth_captcha_ticket', [
        'ticket_hash' => hash('sha256', $ticket),
        'scene' => $scene,
        'client_ip' => stk_auth_client_ip(),
        'status' => 'active',
        'expires_at' => TIMESTAMP + 180,
        'created_at' => TIMESTAMP,
    ]);
    return ['captcha_ticket' => $ticket, 'expires_in' => 180];
}

function stk_auth_consume_captcha_ticket(string $ticket, string $scene): void
{
    $scene = substr(preg_replace('/[^a-z_]/', '', strtolower($scene)) ?: 'unknown', 0, 32);
    if (!preg_match('/^[a-f0-9]{64}$/', $ticket)) {
        throw new InvalidArgumentException('安全验证票据错误或已失效', 4002);
    }
    $row = DB::fetch_first(
        'SELECT ticket_hash FROM %t WHERE ticket_hash=%s AND scene=%s AND client_ip=%s AND status=%s AND expires_at>%d',
        ['stk_auth_captcha_ticket', hash('sha256', $ticket), $scene, stk_auth_client_ip(), 'active', TIMESTAMP]
    );
    if (!$row) {
        throw new InvalidArgumentException('安全验证票据错误或已失效', 4002);
    }
    DB::update(
        'stk_auth_captcha_ticket',
        ['status' => 'used', 'used_at' => TIMESTAMP],
        ['ticket_hash' => (string) $row['ticket_hash'], 'status' => 'active']
    );
    if ((int) DB::affected_rows() !== 1) {
        throw new InvalidArgumentException('安全验证票据错误或已失效', 4002);
    }
}

function stk_auth_validate_action_captcha(array $input, string $scene): void
{
    $ticket = trim((string) ($input['captcha_ticket'] ?? ''));
    if ($ticket !== '') {
        stk_auth_consume_captcha_ticket($ticket, $scene);
        return;
    }
    stk_auth_validate_captcha(
        (string) ($input['captcha_challenge'] ?? ''),
        (string) ($input['captcha_code'] ?? ''),
        $scene
    );
}

function stk_auth_member_data(int $uid): array
{
    $member = C::t('common_member')->fetch($uid);
    $mobile = (string) DB::result_first('SELECT mobile FROM %t WHERE uid=%d', ['stk_auth_mobile', $uid]);
    $membership = DB::fetch_first('SELECT member_label FROM %t WHERE uid=%d', ['stk_member_status', $uid]) ?: [];
    return [
        'uid' => $uid,
        'username' => (string) ($member['username'] ?? ('用户' . $uid)),
        'mobile_masked' => stk_auth_mask_mobile($mobile),
        'member_label' => (string) ($membership['member_label'] ?? '普通用户'),
    ];
}

function stk_auth_session_data(int $uid): array
{
    return stk_auth_issue_tokens($uid) + ['user' => stk_auth_member_data($uid)];
}

function stk_auth_log(int $uid, string $mobile, string $type, string $result, int $errorCode = 0): void
{
    DB::insert('stk_auth_login_log', [
        'uid' => $uid,
        'mobile_masked' => stk_auth_mask_mobile($mobile),
        'login_type' => substr($type, 0, 16),
        'result' => substr($result, 0, 16),
        'error_code' => $errorCode,
        'ip' => stk_auth_client_ip(),
        'created_at' => TIMESTAMP,
    ]);
}

function stk_auth_password_login(string $mobile, string $password): array
{
    require_once libfile('function/member');
    $mobile = stk_auth_mobile($mobile);
    $lockMinutes = max(1, (int) stk_auth_get_config('login_lock_minutes', '15'));
    $failureLimit = max(3, (int) stk_auth_get_config('login_fail_limit', '5'));
    $failureSummary = DB::fetch_first(
        'SELECT COUNT(*) AS failure_count,MIN(created_at) AS oldest_failure_at FROM %t WHERE mobile_masked=%s AND result=%s AND created_at>%d',
        ['stk_auth_login_log', stk_auth_mask_mobile($mobile), 'failed', TIMESTAMP - $lockMinutes * 60]
    ) ?: [];
    $recentFailures = (int) ($failureSummary['failure_count'] ?? 0);
    if ($recentFailures >= $failureLimit) {
        $retryAfterSeconds = max(1, stk_auth_login_retry_after(
            (int) ($failureSummary['oldest_failure_at'] ?? TIMESTAMP),
            TIMESTAMP,
            $lockMinutes
        ));
        stk_auth_security_event(0, 'password_lock', 'blocked', ['mobile' => stk_auth_mask_mobile($mobile)]);
        throw new InvalidArgumentException("登录尝试过多，请 {$retryAfterSeconds} 秒后再试", 4290);
    }
    $uid = (int) DB::result_first('SELECT uid FROM %t WHERE mobile=%s', ['stk_auth_mobile', $mobile]);
    $member = $uid > 0 ? C::t('common_member')->fetch($uid) : [];
    if (!$member) {
        stk_auth_log(0, $mobile, 'password', 'failed', 4003);
        throw new InvalidArgumentException('手机号或密码错误', 4003);
    }
    $login = userlogin((string) $member['username'], $password, '', '', 'username');
    if (($login['status'] ?? 0) !== 1 || (int) ($login['member']['uid'] ?? 0) !== $uid) {
        stk_auth_log($uid, $mobile, 'password', 'failed', 4003);
        throw new InvalidArgumentException('手机号或密码错误', 4003);
    }
    stk_auth_log($uid, $mobile, 'password', 'success');
    return stk_auth_session_data($uid);
}

function stk_auth_register(string $mobile, string $password): array
{
    global $_G;
    $mobile = stk_auth_mobile($mobile);
    if (DB::result_first('SELECT uid FROM %t WHERE mobile=%s', ['stk_auth_mobile', $mobile])) {
        throw new InvalidArgumentException('该手机号已经注册', 4005);
    }

    loaducenter();
    $username = 'stk_' . bin2hex(random_bytes(6));
    $email = $username . '@stk.zz-yihao.com';
    $uid = (int) uc_user_register(addslashes($username), $password, $email);
    if ($uid <= 0) {
        throw new RuntimeException('注册账户失败，请稍后重试', 5002);
    }

    try {
        $groupId = max(1, (int) ($_G['setting']['newusergroupid'] ?? 10));
        $initCredits = explode(',', (string) ($_G['setting']['initcredits'] ?? '0,0,0,0,0,0,0,0,0'));
        C::t('common_member')->insert_user(
            $uid,
            $username,
            md5(random(10)),
            $email,
            (string) ($_G['clientip'] ?? ''),
            $groupId,
            $initCredits,
            0,
            (int) ($_G['remoteport'] ?? 0)
        );
        DB::insert('stk_auth_mobile', [
            'uid' => $uid,
            'mobile' => $mobile,
            'verified' => 0,
            'created_at' => TIMESTAMP,
            'updated_at' => TIMESTAMP,
        ]);
        DB::insert('stk_account_membership', [
            'uid' => $uid,
            'member_label' => '普通用户',
            'level' => 'L1',
            'updated_at' => TIMESTAMP,
        ], false, true);
        DB::insert('stk_member_status', [
            'uid' => $uid,
            'status' => 'inactive',
            'member_label' => '普通用户',
            'level' => 'L1',
            'updated_at' => TIMESTAMP,
        ], false, true);
        DB::insert('stk_account_balance', [
            'uid' => $uid,
            'commission_amount' => '0.00',
            'task_points' => 0,
            'updated_at' => TIMESTAMP,
        ], false, true);
        DB::insert('stk_wallet_account', [
            'uid' => $uid,
            'commission_amount' => '0.00',
            'task_points' => 0,
            'updated_at' => TIMESTAMP,
        ], false, true);
    } catch (Throwable $error) {
        foreach (['stk_auth_mobile', 'stk_member_status', 'stk_wallet_account', 'stk_account_membership', 'stk_account_balance', 'stk_account_prop', 'stk_auth_refresh_token', 'stk_auth_token'] as $table) {
            try { DB::delete($table, ['uid' => $uid]); } catch (Throwable $ignored) {}
        }
        try { C::t('common_member')->delete($uid); } catch (Throwable $ignored) {}
        if (function_exists('uc_user_delete')) {
            uc_user_delete($uid);
        }
        throw $error;
    }

    stk_auth_log($uid, $mobile, 'register', 'success');
    return stk_auth_session_data($uid);
}
