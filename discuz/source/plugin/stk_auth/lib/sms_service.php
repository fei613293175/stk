<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

function stk_auth_secret_key(): string
{
    global $_G;
    $authKey = (string) ($_G['config']['security']['authkey'] ?? '');
    if ($authKey === '') {
        throw new RuntimeException('Discuz authkey 未配置，无法安全保存短信密钥', 5033);
    }
    return hash('sha256', $authKey . '|stk_auth_secrets', true);
}

function stk_auth_encrypt_secret(string $plainText): string
{
    if ($plainText === '') {
        return '';
    }
    if (!function_exists('openssl_encrypt')) {
        throw new RuntimeException('服务器缺少 OpenSSL，无法安全保存短信密钥', 5033);
    }
    $iv = random_bytes(12);
    $tag = '';
    $cipherText = openssl_encrypt($plainText, 'aes-256-gcm', stk_auth_secret_key(), OPENSSL_RAW_DATA, $iv, $tag);
    if ($cipherText === false) {
        throw new RuntimeException('短信密钥加密失败', 5033);
    }
    return 'v1:' . base64_encode($iv . $tag . $cipherText);
}

function stk_auth_decrypt_secret(string $encrypted): string
{
    if ($encrypted === '') {
        return '';
    }
    if (strpos($encrypted, 'v1:') !== 0 || !function_exists('openssl_decrypt')) {
        return '';
    }
    $payload = base64_decode(substr($encrypted, 3), true);
    if ($payload === false || strlen($payload) < 29) {
        return '';
    }
    $iv = substr($payload, 0, 12);
    $tag = substr($payload, 12, 16);
    $cipherText = substr($payload, 28);
    $plainText = openssl_decrypt($cipherText, 'aes-256-gcm', stk_auth_secret_key(), OPENSSL_RAW_DATA, $iv, $tag);
    return $plainText === false ? '' : $plainText;
}

function stk_auth_aliyun_encode(string $value): string
{
    return str_replace(['+', '*', '%7E'], ['%20', '%2A', '~'], rawurlencode($value));
}

function stk_auth_aliyun_send(string $mobile, string $code): array
{
    $accessKeyId = trim(stk_auth_get_config('sms_access_key_id', ''));
    $accessKeySecret = stk_auth_decrypt_secret(stk_auth_get_config('sms_access_key_secret', ''));
    $signName = trim(stk_auth_get_config('sms_sign_name', ''));
    $templateCode = trim(stk_auth_get_config('sms_template_code', ''));
    if ($accessKeyId === '' || $accessKeySecret === '' || $signName === '' || $templateCode === '') {
        throw new RuntimeException('短信服务配置不完整', 5031);
    }

    $parameters = [
        'AccessKeyId' => $accessKeyId,
        'Action' => 'SendSms',
        'Format' => 'JSON',
        'PhoneNumbers' => $mobile,
        'RegionId' => 'cn-hangzhou',
        'SignName' => $signName,
        'SignatureMethod' => 'HMAC-SHA1',
        'SignatureNonce' => bin2hex(random_bytes(16)),
        'SignatureVersion' => '1.0',
        'TemplateCode' => $templateCode,
        'TemplateParam' => json_encode(['code' => $code], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES),
        'Timestamp' => gmdate('Y-m-d\TH:i:s\Z'),
        'Version' => '2017-05-25',
    ];
    ksort($parameters);
    $canonical = [];
    foreach ($parameters as $key => $value) {
        $canonical[] = stk_auth_aliyun_encode((string) $key) . '=' . stk_auth_aliyun_encode((string) $value);
    }
    $canonicalQuery = implode('&', $canonical);
    $stringToSign = 'POST&%2F&' . stk_auth_aliyun_encode($canonicalQuery);
    $parameters['Signature'] = base64_encode(hash_hmac('sha1', $stringToSign, $accessKeySecret . '&', true));
    $body = http_build_query($parameters, '', '&', PHP_QUERY_RFC3986);
    $endpoint = 'https://dysmsapi.aliyuncs.com/';

    if (function_exists('curl_init')) {
        $curl = curl_init($endpoint);
        curl_setopt_array($curl, [
            CURLOPT_POST => true,
            CURLOPT_POSTFIELDS => $body,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_CONNECTTIMEOUT => 5,
            CURLOPT_TIMEOUT => 10,
            CURLOPT_HTTPHEADER => ['Content-Type: application/x-www-form-urlencoded'],
            CURLOPT_SSL_VERIFYPEER => true,
            CURLOPT_SSL_VERIFYHOST => 2,
        ]);
        $responseBody = curl_exec($curl);
        $httpStatus = (int) curl_getinfo($curl, CURLINFO_HTTP_CODE);
        $curlError = curl_error($curl);
        curl_close($curl);
        if ($responseBody === false) {
            throw new RuntimeException('短信供应商连接失败：' . $curlError, 5031);
        }
    } else {
        $context = stream_context_create(['http' => [
            'method' => 'POST',
            'header' => "Content-Type: application/x-www-form-urlencoded\r\n",
            'content' => $body,
            'timeout' => 10,
            'ignore_errors' => true,
        ]]);
        $responseBody = file_get_contents($endpoint, false, $context);
        $httpStatus = 200;
        if ($responseBody === false) {
            throw new RuntimeException('短信供应商连接失败', 5031);
        }
    }

    $response = json_decode((string) $responseBody, true);
    if ($httpStatus < 200 || $httpStatus >= 300 || !is_array($response) || ($response['Code'] ?? '') !== 'OK') {
        $providerMessage = is_array($response) ? (string) ($response['Message'] ?? $response['Code'] ?? 'unknown') : 'invalid response';
        throw new RuntimeException('短信发送失败：' . substr($providerMessage, 0, 160), 5031);
    }
    return [
        'request_id' => substr((string) ($response['RequestId'] ?? ''), 0, 64),
        'provider_code' => substr((string) ($response['Code'] ?? 'OK'), 0, 32),
    ];
}

function stk_auth_sms_retry_after(int $lastCreatedAt, int $now, int $resendSeconds): int
{
    $remaining = $lastCreatedAt + max(1, $resendSeconds) - $now;
    return max(0, $remaining);
}

function stk_auth_send_sms_code(string $mobile): array
{
    $mobile = stk_auth_mobile($mobile);
    if (stk_auth_get_config('sms_enabled', '0') !== '1') {
        throw new RuntimeException('短信服务尚未启用，请使用密码登录', 5031);
    }
    $phoneHourLimit = max(1, (int) stk_auth_get_config('sms_phone_hour_limit', '5'));
    $phoneDayLimit = max($phoneHourLimit, (int) stk_auth_get_config('sms_phone_day_limit', '10'));
    $ipHourLimit = max(1, (int) stk_auth_get_config('sms_ip_hour_limit', '20'));
    $resendSeconds = max(1, min(3600, (int) stk_auth_get_config('sms_resend_seconds', '60')));
    $clientIp = stk_auth_client_ip();
    $lastCreatedAt = (int) DB::result_first(
        'SELECT created_at FROM %t WHERE mobile=%s AND scene=%s ORDER BY id DESC LIMIT 1',
        ['stk_auth_sms_code', $mobile, 'sms_login']
    );
    $retryAfter = stk_auth_sms_retry_after($lastCreatedAt, TIMESTAMP, $resendSeconds);
    if ($retryAfter > 0) {
        throw new InvalidArgumentException("请 {$retryAfter} 秒后再试", 4290);
    }
    $phoneHourCount = (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE mobile=%s AND created_at>%d', ['stk_auth_sms_code', $mobile, TIMESTAMP - 3600]);
    $phoneDayCount = (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE mobile=%s AND created_at>%d', ['stk_auth_sms_code', $mobile, TIMESTAMP - 86400]);
    $ipHourCount = (int) DB::result_first('SELECT COUNT(*) FROM %t WHERE client_ip=%s AND created_at>%d', ['stk_auth_sms_code', $clientIp, TIMESTAMP - 3600]);
    if ($phoneHourCount >= $phoneHourLimit || $phoneDayCount >= $phoneDayLimit || $ipHourCount >= $ipHourLimit) {
        stk_auth_security_event(0, 'sms_rate_limit', 'blocked', ['mobile' => stk_auth_mask_mobile($mobile)]);
        throw new InvalidArgumentException('短信发送过于频繁，请稍后再试', 4290);
    }

    $codeLength = max(4, min(8, (int) stk_auth_get_config('sms_code_length', '6')));
    $minimum = (int) pow(10, $codeLength - 1);
    $maximum = (int) pow(10, $codeLength) - 1;
    $code = (string) random_int($minimum, $maximum);
    $expiresMinutes = max(1, min(30, (int) stk_auth_get_config('sms_expire_minutes', '5')));
    $smsId = (int) DB::insert('stk_auth_sms_code', [
        'mobile' => $mobile,
        'scene' => 'sms_login',
        'code_hash' => password_hash($code, PASSWORD_DEFAULT),
        'code_audit_ciphertext' => stk_auth_encrypt_secret($code),
        'send_status' => 'pending',
        'use_status' => 'unused',
        'attempt_count' => 0,
        'client_ip' => $clientIp,
        'expires_at' => TIMESTAMP + $expiresMinutes * 60,
        'created_at' => TIMESTAMP,
    ], true);
    try {
        $provider = stk_auth_aliyun_send($mobile, $code);
        DB::update('stk_auth_sms_code', [
            'send_status' => 'sent',
            'provider_request_id' => $provider['request_id'],
            'provider_code' => $provider['provider_code'],
        ], ['id' => $smsId]);
        stk_auth_security_event(0, 'sms_send', 'success', ['mobile' => stk_auth_mask_mobile($mobile), 'sms_id' => $smsId]);
    } catch (Throwable $error) {
        DB::update('stk_auth_sms_code', [
            'send_status' => 'failed',
            'provider_code' => (string) $error->getCode(),
            'last_error' => substr($error->getMessage(), 0, 255),
        ], ['id' => $smsId]);
        stk_auth_security_event(0, 'sms_send', 'failed', ['mobile' => stk_auth_mask_mobile($mobile), 'sms_id' => $smsId]);
        throw $error;
    }
    return ['retry_after_seconds' => $resendSeconds];
}

function stk_auth_sms_login(string $mobile, string $code): array
{
    $mobile = stk_auth_mobile($mobile);
    $row = DB::fetch_first(
        'SELECT id,code_hash,attempt_count FROM %t WHERE mobile=%s AND scene=%s AND send_status=%s AND use_status=%s AND expires_at>%d ORDER BY id DESC LIMIT 1',
        ['stk_auth_sms_code', $mobile, 'sms_login', 'sent', 'unused', TIMESTAMP]
    );
    if (!$row || (int) $row['attempt_count'] >= 5 || !password_verify(trim($code), (string) $row['code_hash'])) {
        if ($row) {
            $attemptCount = (int) $row['attempt_count'] + 1;
            DB::update('stk_auth_sms_code', [
                'attempt_count' => $attemptCount,
                'use_status' => $attemptCount >= 5 ? 'locked' : 'unused',
            ], ['id' => (int) $row['id'], 'attempt_count' => (int) $row['attempt_count']]);
        }
        stk_auth_log(0, $mobile, 'sms', 'failed', 4004);
        throw new InvalidArgumentException('短信验证码错误或已失效', 4004);
    }
    DB::update('stk_auth_sms_code', ['use_status' => 'used', 'used_at' => TIMESTAMP], ['id' => (int) $row['id'], 'use_status' => 'unused']);
    $uid = (int) DB::result_first('SELECT uid FROM %t WHERE mobile=%s', ['stk_auth_mobile', $mobile]);
    if ($uid <= 0) {
        stk_auth_log(0, $mobile, 'sms', 'failed', 4003);
        throw new InvalidArgumentException('手机号或验证码错误', 4003);
    }
    DB::update('stk_auth_mobile', ['verified' => 1, 'updated_at' => TIMESTAMP], ['uid' => $uid]);
    stk_auth_log($uid, $mobile, 'sms', 'success');
    return stk_auth_session_data($uid);
}
