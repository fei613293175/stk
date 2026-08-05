<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }

/** @return array{success:bool,code?:string,message?:string,biz_id?:string} */
function stk_aliyun_sms_send(string $mobile, string $scene, string $code, array $settings): array {
    $accessKey = trim((string)($settings['sms_access_key_id'] ?? ''));
    $accessSecret = trim((string)($settings['sms_access_key_secret'] ?? ''));
    $signName = trim((string)($settings['sms_sign_name'] ?? ''));
    $template = trim((string)($settings['sms_template_' . $scene] ?? ''));
    if ($accessKey === '' || $accessSecret === '' || $signName === '' || $template === '' || !function_exists('curl_init')) {
        return ['success' => false, 'code' => 'CONFIG_MISSING', 'message' => '短信配置不完整'];
    }

    $parameters = [
        'AccessKeyId' => $accessKey,
        'Action' => 'SendSms',
        'Format' => 'JSON',
        'PhoneNumbers' => $mobile,
        'RegionId' => (string)($settings['sms_region_id'] ?? 'cn-hangzhou'),
        'SignName' => $signName,
        'SignatureMethod' => 'HMAC-SHA1',
        'SignatureNonce' => bin2hex(random_bytes(16)),
        'SignatureVersion' => '1.0',
        'SignatureType' => 'HMAC-SHA1',
        'TemplateCode' => $template,
        'TemplateParam' => json_encode(['code' => $code], JSON_UNESCAPED_UNICODE),
        'Timestamp' => gmdate('Y-m-d\TH:i:s\Z'),
        'Version' => '2017-05-25',
    ];
    ksort($parameters);
    $canonical = [];
    foreach ($parameters as $key => $value) { $canonical[] = _stk_aliyun_encode($key) . '=' . _stk_aliyun_encode((string)$value); }
    $canonicalized = implode('&', $canonical);
    $stringToSign = 'POST&%2F&' . _stk_aliyun_encode($canonicalized);
    $parameters['Signature'] = base64_encode(hash_hmac('sha1', $stringToSign, $accessSecret . '&', true));
    $ch = curl_init('https://dysmsapi.aliyuncs.com/');
    curl_setopt_array($ch, [CURLOPT_POST => true, CURLOPT_POSTFIELDS => http_build_query($parameters, '', '&'), CURLOPT_RETURNTRANSFER => true, CURLOPT_CONNECTTIMEOUT => 8, CURLOPT_TIMEOUT => 15, CURLOPT_HTTPHEADER => ['Accept: application/json']]);
    $raw = curl_exec($ch);
    $curlError = curl_error($ch);
    $status = (int)curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    if ($raw === false || $curlError !== '' || $status < 200 || $status >= 300) { return ['success' => false, 'code' => 'NETWORK_ERROR', 'message' => '短信服务网络错误']; }
    $response = json_decode((string)$raw, true);
    if (!is_array($response) || ($response['Code'] ?? '') !== 'OK') { return ['success' => false, 'code' => (string)($response['Code'] ?? 'PROVIDER_ERROR'), 'message' => (string)($response['Message'] ?? '短信服务拒绝请求')]; }
    return ['success' => true, 'code' => 'OK', 'message' => 'OK', 'biz_id' => (string)($response['BizId'] ?? '')];
}

function _stk_aliyun_encode(string $value): string { return str_replace(['+', '*', '%7E'], ['%20', '%2A', '~'], urlencode($value)); }
