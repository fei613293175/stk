<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
require_once __DIR__ . '/ApiResponse.php';
require_once __DIR__ . '/Settings.php';

final class StkSecurityService {
    private static function dateTime(int $timestamp): string { return gmdate('Y-m-d H:i:s', $timestamp); }
    private static function hashSubject(string $value): string { return hash('sha256', $value); }
    public static function requestIpHash(): string {
        global $_G;
        $ip = (string)($_SERVER['REMOTE_ADDR'] ?? '');
        $key = (string)($_G['config']['security']['authkey'] ?? 'stk-security');
        return hash_hmac('sha256', $ip, $key);
    }
    private static function deviceHash(string $deviceId): string { return self::hashSubject($deviceId); }

    public static function recordLogin(string $mobile, ?int $uid, string $loginType, string $result, ?string $errorCode = null, string $deviceId = ''): void {
        if (!class_exists('DB')) return;
        try {
            DB::insert('stk_auth_login_log', [
                'uid' => $uid,
                'mobile_hash' => $mobile === '' ? null : self::hashSubject($mobile),
                'login_type' => substr($loginType, 0, 24),
                'result' => substr($result, 0, 16),
                'error_code' => $errorCode === null ? null : substr($errorCode, 0, 64),
                'request_id' => StkApiResponse::requestId(),
                'ip_hash' => self::requestIpHash(),
                'device_hash' => self::deviceHash($deviceId),
                'user_agent_digest' => hash('sha256', (string)($_SERVER['HTTP_USER_AGENT'] ?? '')),
                'created_at' => self::dateTime(time()),
            ]);
        } catch (Throwable $ignored) {
            // Authentication outcome must not become a database error if audit storage is unavailable.
        }
    }

    public static function recordRisk(string $scene, string $subjectType, string $subject, string $ruleCode, string $action, ?int $unlockAt = null, array $metadata = []): void {
        if (!class_exists('DB')) return;
        try {
            DB::insert('stk_auth_risk_event', [
                'scene' => substr($scene, 0, 32),
                'subject_type' => substr($subjectType, 0, 24),
                'subject_hash' => self::hashSubject($subject),
                'rule_code' => substr($ruleCode, 0, 64),
                'action' => substr($action, 0, 32),
                'metadata_json' => $metadata ? json_encode($metadata, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES) : null,
                'unlock_at' => $unlockAt === null ? null : self::dateTime($unlockAt),
                'created_at' => self::dateTime(time()),
            ]);
        } catch (Throwable $ignored) {
        }
    }

    public static function assertLoginAllowed(string $mobile, string $deviceId): void {
        if (!class_exists('DB')) return;
        $now = self::dateTime(time());
        foreach ([['mobile', $mobile], ['device', $deviceId]] as [$type, $subject]) {
            $row = DB::fetch_first(
                'SELECT unlock_at FROM ' . DB::table('stk_auth_risk_event') . ' WHERE scene=%s AND subject_type=%s AND subject_hash=%s AND action=%s AND unlock_at>%s ORDER BY id DESC LIMIT 1',
                ['login', $type, self::hashSubject($subject), 'lock', $now]
            );
            if ($row) throw new StkApiException(429, 'AUTH_LOGIN_LOCKED', '登录暂时受限，请稍后重试');
        }
    }

    public static function recordLoginFailure(string $mobile, string $deviceId, string $loginType, string $errorCode): void {
        $limit = StkSettings::int('auth_login_failure_limit', 5, 3, 10);
        $window = StkSettings::int('auth_login_lock_seconds', 900, 60, 86400);
        if (!class_exists('DB')) return;
        try {
            $since = self::dateTime(time() - $window);
            $row = DB::fetch_first(
                'SELECT COUNT(*) AS total FROM ' . DB::table('stk_auth_login_log') . ' WHERE mobile_hash=%s AND result=%s AND created_at>=%s',
                [self::hashSubject($mobile), 'failed', $since]
            );
            if ((int)($row['total'] ?? 0) >= $limit) {
                $unlockAt = time() + $window;
                self::recordRisk('login', 'mobile', $mobile, 'AUTH_LOGIN_FAILURE_LIMIT', 'lock', $unlockAt, ['login_type' => $loginType]);
                self::recordRisk('login', 'device', $deviceId, 'AUTH_LOGIN_FAILURE_LIMIT', 'lock', $unlockAt, ['login_type' => $loginType]);
            }
        } catch (Throwable $ignored) {
        }
    }

    public static function assertSmsAllowed(string $mobile, string $deviceId, string $scene): void {
        if (!class_exists('DB')) return;
        $now = time();
        $hour = self::dateTime($now - 3600);
        $day = self::dateTime($now - 86400);
        $mobileHash = self::hashSubject($mobile);
        $ipHash = self::requestIpHash();
        $deviceHash = self::deviceHash($deviceId);
        $checks = [
            ['mobile_hash=%s AND scene=%s AND created_at>=%s', [$mobileHash, $scene, $hour], StkSettings::int('sms_phone_hour_limit', 5, 1, 20)],
            ['mobile_hash=%s AND scene=%s AND created_at>=%s', [$mobileHash, $scene, $day], StkSettings::int('sms_phone_day_limit', 10, 1, 50)],
            ['request_ip_hash=%s AND created_at>=%s', [$ipHash, $hour], StkSettings::int('sms_ip_hour_limit', 20, 5, 200)],
            ['device_hash=%s AND created_at>=%s', [$deviceHash, $hour], StkSettings::int('sms_device_hour_limit', 10, 3, 100)],
        ];
        foreach ($checks as [$where, $args, $limit]) {
            $row = DB::fetch_first('SELECT COUNT(*) AS total FROM ' . DB::table('stk_auth_sms_code') . ' WHERE ' . $where, $args);
            if ((int)($row['total'] ?? 0) >= $limit) {
                self::recordRisk('sms_send', 'mobile', $mobile, 'SMS_RATE_LIMIT', 'block', null, ['scene' => $scene]);
                throw new StkApiException(429, 'SMS_RATE_LIMITED', '短信发送过于频繁，请稍后重试');
            }
        }
        $interval = StkSettings::int('sms_resend_interval_seconds', 60, 30, 300);
        $row = DB::fetch_first(
            'SELECT created_at FROM ' . DB::table('stk_auth_sms_code') . ' WHERE mobile_hash=%s AND scene=%s ORDER BY id DESC LIMIT 1',
            [$mobileHash, $scene]
        );
        if ($row && strtotime((string)$row['created_at'] . ' UTC') !== false && time() - strtotime((string)$row['created_at'] . ' UTC') < $interval) {
            throw new StkApiException(429, 'SMS_RATE_LIMITED', '短信发送过于频繁，请稍后重试');
        }
    }
}
