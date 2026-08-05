<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
require_once __DIR__ . '/Settings.php';
require_once __DIR__ . '/SecurityService.php';

final class StkSmsService {
    private static function dateTime(int $timestamp): string { return gmdate('Y-m-d H:i:s', $timestamp); }
    private static function utcTimestamp(string $value): int {
        $parsed = strtotime($value . ' UTC');
        return $parsed === false ? 0 : $parsed;
    }

    public static function send(string $mobile, string $scene, string $deviceId): array {
        global $_G;
        if (!in_array($scene, ['login', 'password_reset'], true)) {
            throw new StkApiException(422, 'SMS_SCENE_DISABLED', '当前短信场景不可用');
        }
        $settings = $_G['cache']['plugin']['stk_auth'] ?? [];
        if (($settings['sms_enabled'] ?? '0') !== '1') {
            throw new StkApiException(503, 'SMS_SCENE_DISABLED', '短信服务尚未配置');
        }
        if (!class_exists('DB')) { throw new StkApiException(503, 'SMS_PROVIDER_FAILED', '短信发送失败'); }

        StkSecurityService::assertSmsAllowed($mobile, $deviceId, $scene);
        $digits = StkSettings::int('sms_code_digits', 6, 4, 8);
        $code = str_pad((string)random_int(0, (10 ** $digits) - 1), $digits, '0', STR_PAD_LEFT);
        $requestId = StkApiResponse::requestId();
        $now = time();
        DB::insert('stk_auth_sms_code', [
            'scene' => $scene,
            'mobile_hash' => StkMobileCrypto::hash($mobile),
            'mobile_ciphertext' => StkMobileCrypto::encrypt($mobile),
            'code_hash' => hash('sha256', $code),
            'code_ciphertext' => null,
            'send_status' => 'pending',
            'use_status' => 'unused',
            'request_id' => $requestId,
            'request_ip_hash' => StkSecurityService::requestIpHash(),
            'device_hash' => hash('sha256', $deviceId),
            'failed_attempts' => 0,
            'expires_at' => self::dateTime($now + StkSettings::int('sms_code_ttl_seconds', 300, 60, 900)),
            'created_at' => self::dateTime($now),
        ]);
        $id = (int)DB::insert_id();

        $adapter = dirname(__DIR__) . '/integration/aliyun_sms.php';
        if (!is_file($adapter)) {
            DB::update('stk_auth_sms_code', ['send_status' => 'failed', 'provider_message' => 'adapter_missing'], ['id' => $id]);
            throw new StkApiException(503, 'SMS_PROVIDER_FAILED', '短信发送失败');
        }
        require_once $adapter;
        if (!function_exists('stk_aliyun_sms_send')) {
            DB::update('stk_auth_sms_code', ['send_status' => 'failed', 'provider_message' => 'adapter_invalid'], ['id' => $id]);
            throw new StkApiException(503, 'SMS_PROVIDER_FAILED', '短信发送失败');
        }
        $result = stk_aliyun_sms_send($mobile, $scene, $code, $settings);
        if (!is_array($result) || empty($result['success'])) {
            DB::update('stk_auth_sms_code', [
                'send_status' => 'failed',
                'provider_code' => substr((string)($result['code'] ?? ''), 0, 64),
                'provider_message' => substr((string)($result['message'] ?? ''), 0, 255),
            ], ['id' => $id]);
            throw new StkApiException(503, 'SMS_PROVIDER_FAILED', '短信发送失败');
        }
        DB::update('stk_auth_sms_code', [
            'send_status' => 'sent',
            'provider_code' => substr((string)($result['code'] ?? ''), 0, 64),
            'provider_message' => substr((string)($result['message'] ?? ''), 0, 255),
            'biz_id' => substr((string)($result['biz_id'] ?? ''), 0, 128),
            'sent_at' => self::dateTime(time()),
        ], ['id' => $id]);
        return ['request_id' => $requestId, 'resend_after' => StkSettings::int('sms_resend_interval_seconds', 60, 30, 300), 'expires_in' => StkSettings::int('sms_code_ttl_seconds', 300, 60, 900), 'masked_mobile' => StkMobileCrypto::mask($mobile)];
    }

    public static function consume(string $mobile, string $scene, string $code, string $deviceId): void {
        if (!class_exists('DB')) { throw new StkApiException(503, 'SYS_UNAVAILABLE', '服务暂不可用'); }
        $row = DB::fetch_first(
            'SELECT * FROM ' . DB::table('stk_auth_sms_code') . ' WHERE mobile_hash=%s AND scene=%s AND device_hash=%s AND send_status=%s AND use_status=%s ORDER BY id DESC LIMIT 1',
            [StkMobileCrypto::hash($mobile), $scene, hash('sha256', $deviceId), 'sent', 'unused']
        );
        if (!$row || self::utcTimestamp((string)$row['expires_at']) < time()) {
            throw new StkApiException(422, 'SMS_CODE_EXPIRED', '短信验证码已过期');
        }
        if (!hash_equals((string)$row['code_hash'], hash('sha256', $code))) {
            DB::query('UPDATE ' . DB::table('stk_auth_sms_code') . ' SET failed_attempts=failed_attempts+1 WHERE id=%d', [(int)$row['id']]);
            throw new StkApiException(422, 'SMS_CODE_INVALID', '短信验证码错误');
        }
        DB::update('stk_auth_sms_code', ['use_status' => 'used', 'used_at' => self::dateTime(time())], ['id' => (int)$row['id']]);
    }
}
