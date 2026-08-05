<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }

final class StkAuthService {
    private static function dateTime(int $timestamp): string { return gmdate('Y-m-d H:i:s', $timestamp); }

    public static function loginPassword(string $mobile, string $password, string $ticket, string $deviceId, string $deviceName): array {
        StkCaptchaService::consumeTicket($ticket, 'password_login', $deviceId);
        $binding = self::mobileBinding($mobile);
        if (!$binding || !function_exists('uc_user_login')) { throw new StkApiException(401, 'AUTH_INVALID_CREDENTIALS', '手机号或密码错误'); }
        $member = DB::fetch_first('SELECT uid,username,status FROM ' . DB::table('common_member') . ' WHERE uid=%d LIMIT 1', [(int)$binding['uid']]);
        if (!$member || (int)$member['status'] !== 0) { throw new StkApiException(403, 'AUTH_ACCOUNT_BLOCKED', '账号当前不可用'); }
        $result = uc_user_login((string)$member['username'], $password, 0);
        if (!is_array($result) || (int)$result[0] !== (int)$member['uid']) { throw new StkApiException(401, 'AUTH_INVALID_CREDENTIALS', '手机号或密码错误'); }
        return self::loginResult((int)$member['uid'], $deviceId, $deviceName, true);
    }

    public static function loginSms(string $mobile, string $code, string $ticket, string $deviceId, string $deviceName): array {
        StkCaptchaService::consumeTicket($ticket, 'sms_login', $deviceId);
        StkSmsService::consume($mobile, 'login', $code, $deviceId);
        $binding = self::mobileBinding($mobile);
        if (!$binding) { throw new StkApiException(401, 'AUTH_INVALID_CREDENTIALS', '手机号或验证码错误'); }
        DB::update('stk_auth_mobile', ['verified_at' => self::dateTime(time()), 'updated_at' => self::dateTime(time())], ['uid' => (int)$binding['uid']]);
        return self::loginResult((int)$binding['uid'], $deviceId, $deviceName, true);
    }

    public static function register(string $mobile, string $password, string $confirmation, string $agreement, string $privacy, string $ticket, string $deviceId): array {
        if ($password !== $confirmation || !StkPasswordPolicy::validate($password)) {
            throw new StkApiException(422, 'AUTH_PASSWORD_WEAK', '密码不符合安全要求');
        }
        if ($agreement !== '1.0' || $privacy !== '1.0') { throw new StkApiException(409, 'LEGAL_VERSION_STALE', '协议版本已更新'); }
        StkCaptchaService::consumeTicket($ticket, 'register', $deviceId);
        if (self::mobileBinding($mobile)) { throw new StkApiException(409, 'AUTH_MOBILE_EXISTS', '手机号已注册'); }
        if (!function_exists('uc_user_register') || !class_exists('DB')) { throw new StkApiException(503, 'SYS_UNAVAILABLE', '注册服务暂不可用'); }

        $username = 'stk_' . substr(bin2hex(random_bytes(10)), 0, 16);
        $email = $username . '@users.invalid';
        $uid = (int)uc_user_register($username, $password, $email);
        if ($uid <= 0) { throw new StkApiException(503, 'AUTH_REGISTER_FAILED', '注册失败'); }
        $now = time();
        DB::query('START TRANSACTION');
        try {
            DB::insert('common_member', ['uid' => $uid, 'username' => $username, 'password' => '', 'email' => $email, 'adminid' => 0, 'groupid' => 10, 'regdate' => $now, 'credits' => 0, 'timeoffset' => 9999]);
            foreach (['common_member_status', 'common_member_profile', 'common_member_field_forum', 'common_member_field_home', 'common_member_count'] as $table) { DB::insert($table, ['uid' => $uid]); }
            DB::insert('stk_auth_mobile', ['uid' => $uid, 'mobile_hash' => StkMobileCrypto::hash($mobile), 'mobile_ciphertext' => StkMobileCrypto::encrypt($mobile), 'mobile_last4' => substr($mobile, -4), 'country_code' => '+86', 'verified_at' => null, 'status' => 1, 'created_at' => self::dateTime($now), 'updated_at' => self::dateTime($now)]);
            DB::insert('stk_member_status', ['uid' => $uid, 'status' => 'inactive', 'level_code' => 'none', 'level_name' => '未开通', 'source' => 'registration', 'created_at' => self::dateTime($now), 'updated_at' => self::dateTime($now)]);
            DB::insert('stk_wallet_account', ['uid' => $uid, 'account_type' => 'commission', 'balance' => '0.00', 'frozen_balance' => '0.00', 'currency' => 'CNY', 'row_version' => 1, 'created_at' => self::dateTime($now), 'updated_at' => self::dateTime($now)]);
            DB::insert('stk_wallet_account', ['uid' => $uid, 'account_type' => 'task', 'balance' => '0.00', 'frozen_balance' => '0.00', 'currency' => 'CNY', 'row_version' => 1, 'created_at' => self::dateTime($now), 'updated_at' => self::dateTime($now)]);
            DB::query('COMMIT');
        } catch (Throwable $error) {
            DB::query('ROLLBACK');
            if (function_exists('uc_user_delete')) { uc_user_delete($uid); }
            throw new StkApiException(503, 'AUTH_REGISTER_FAILED', '注册失败');
        }
        $tokens = StkTokenService::issue($uid, $deviceId, 'Android');
        return ['uid' => $uid, 'username' => $username, 'display_name' => '商推客用户', 'masked_mobile' => StkMobileCrypto::mask($mobile), 'mobile_verified' => false, 'tokens' => $tokens, 'next_route' => 'home'];
    }

    public static function resetPassword(string $mobile, string $code, string $password, string $confirmation, string $ticket, string $deviceId): array {
        if ($password !== $confirmation || !StkPasswordPolicy::validate($password)) { throw new StkApiException(422, 'AUTH_PASSWORD_WEAK', '密码不符合安全要求'); }
        StkCaptchaService::consumeTicket($ticket, 'password_reset', $deviceId);
        StkSmsService::consume($mobile, 'password_reset', $code, $deviceId);
        $binding = self::mobileBinding($mobile);
        if (!$binding || !function_exists('uc_user_edit')) { throw new StkApiException(422, 'SMS_CODE_INVALID', '手机号或短信验证码错误'); }
        $member = DB::fetch_first('SELECT username FROM ' . DB::table('common_member') . ' WHERE uid=%d LIMIT 1', [(int)$binding['uid']]);
        $result = uc_user_edit((string)$member['username'], '', $password, '', 1);
        if ($result < 0) { throw new StkApiException(503, 'AUTH_RESET_FAILED', '密码重置失败'); }
        DB::update('stk_auth_token', ['status' => 'revoked', 'revoked_at' => self::dateTime(time())], ['uid' => (int)$binding['uid'], 'status' => 'active']);
        return ['reset' => true, 'next_route' => 'login'];
    }

    public static function summary(int $uid): array {
        $member = DB::fetch_first('SELECT m.uid,m.username,m.avatarstatus FROM ' . DB::table('common_member') . ' m WHERE m.uid=%d LIMIT 1', [$uid]);
        $mobile = DB::fetch_first('SELECT mobile_ciphertext,mobile_last4,verified_at FROM ' . DB::table('stk_auth_mobile') . ' WHERE uid=%d LIMIT 1', [$uid]);
        $status = DB::fetch_first('SELECT status,level_code,level_name,expires_at FROM ' . DB::table('stk_member_status') . ' WHERE uid=%d LIMIT 1', [$uid]);
        $wallets = DB::fetch_all('SELECT account_type,balance,frozen_balance,currency FROM ' . DB::table('stk_wallet_account') . ' WHERE uid=%d', [$uid]);
        if (!$member) { throw new StkApiException(404, 'AUTH_ACCOUNT_NOT_FOUND', '账号不存在'); }
        return ['uid' => $uid, 'display_name' => (string)$member['username'], 'avatar_url' => '', 'masked_mobile' => $mobile ? '***' . (string)$mobile['mobile_last4'] : '', 'mobile_verified' => $mobile && $mobile['verified_at'] !== null, 'member_summary' => $status ?: ['status' => 'inactive', 'level_code' => 'none', 'level_name' => '未开通', 'expires_at' => null], 'wallet_summary' => $wallets ?: []];
    }

    public static function legal(string $type): array {
        global $_G;
        if (!in_array($type, ['user_agreement', 'privacy_policy'], true)) { throw new StkApiException(404, 'LEGAL_NOT_FOUND', '协议不存在'); }
        $settings = $_G['cache']['plugin']['stk_auth'] ?? [];
        $prefix = $type === 'user_agreement' ? 'agreement' : 'privacy';
        $text = trim((string)($settings[$prefix . '_content'] ?? ''));
        if ($text === '') { throw new StkApiException(404, 'LEGAL_NOT_FOUND', '协议不存在'); }
        $version = (string)($settings[$prefix . '_version'] ?? '1.0');
        $title = $type === 'user_agreement' ? '用户协议' : '隐私政策';
        $sanitized = strip_tags($text, '<p><br><strong><b><em><i><ul><ol><li><h1><h2><h3><a>');
        return ['document_type' => $type, 'version' => $version, 'effective_at' => (string)($settings[$prefix . '_effective_at'] ?? ''), 'title' => $title, 'content_html_sanitized' => $sanitized, 'content_text' => trim(strip_tags($sanitized)), 'sha256' => hash('sha256', $sanitized)];
    }

    private static function mobileBinding(string $mobile) {
        if ($mobile === '' || !class_exists('DB')) { return false; }
        return DB::fetch_first('SELECT * FROM ' . DB::table('stk_auth_mobile') . ' WHERE mobile_hash=%s AND status=1 LIMIT 1', [StkMobileCrypto::hash($mobile)]);
    }

    private static function loginResult(int $uid, string $deviceId, string $deviceName, bool $verified): array {
        $tokens = StkTokenService::issue($uid, $deviceId, $deviceName);
        $summary = self::summary($uid);
        return array_merge($tokens, ['user' => ['uid' => $uid, 'display_name' => $summary['display_name'], 'masked_mobile' => $summary['masked_mobile'], 'mobile_verified' => $verified], 'next_route' => 'home']);
    }
}
