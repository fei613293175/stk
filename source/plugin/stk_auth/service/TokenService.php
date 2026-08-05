<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
require_once __DIR__ . '/Settings.php';

final class StkTokenService {
    private static function dateTime(int $timestamp): string { return gmdate('Y-m-d H:i:s', $timestamp); }
    private static function utcTimestamp(string $value): int {
        $parsed = strtotime($value . ' UTC');
        return $parsed === false ? 0 : $parsed;
    }

    public static function issue(int $uid, string $deviceId, string $deviceName = ''): array {
        $access = bin2hex(random_bytes(32));
        $refresh = bin2hex(random_bytes(48));
        $now = time();
        $family = bin2hex(random_bytes(16));
        $accessTtl = StkSettings::int('auth_access_token_ttl_seconds', 7200, 900, 14400);
        $refreshTtl = StkSettings::int('auth_refresh_token_ttl_seconds', 2592000, 86400, 7776000);
        if (class_exists('DB')) {
            DB::insert('stk_auth_token', [
                'uid' => $uid,
                'token_family' => $family,
                'access_jti_hash' => hash('sha256', $access),
                'refresh_hash' => hash('sha256', $refresh),
                'device_id_hash' => hash('sha256', $deviceId),
                'device_name' => $deviceName,
                'status' => 'active',
                'access_expires_at' => self::dateTime($now + $accessTtl),
                'refresh_expires_at' => self::dateTime($now + $refreshTtl),
                'created_at' => self::dateTime($now),
            ]);
            $rows = DB::fetch_all('SELECT id FROM ' . DB::table('stk_auth_token') . ' WHERE uid=%d AND status=%s ORDER BY created_at DESC,id DESC', [$uid, 'active']);
            $maxDevices = StkSettings::int('auth_max_active_devices', 5, 1, 20);
            foreach (array_slice($rows, $maxDevices) as $old) {
                DB::update('stk_auth_token', ['status' => 'revoked', 'revoked_at' => self::dateTime($now)], ['id' => (int)$old['id']]);
            }
        }
        return ['access_token' => $access, 'access_expires_in' => $accessTtl, 'refresh_token' => $refresh, 'refresh_expires_in' => $refreshTtl, 'refresh_token_family' => $family];
    }

    public static function authenticateAccess(string $accessToken): int {
        if ($accessToken === '' || !class_exists('DB')) { throw new StkApiException(401, 'AUTH_TOKEN_INVALID', '登录状态无效'); }
        $row = DB::fetch_first(
            'SELECT uid,status,access_expires_at FROM ' . DB::table('stk_auth_token') . ' WHERE access_jti_hash=%s LIMIT 1',
            [hash('sha256', $accessToken)]
        );
        if (!$row || (string)$row['status'] !== 'active') { throw new StkApiException(401, 'AUTH_TOKEN_INVALID', '登录状态无效'); }
        if (self::utcTimestamp((string)$row['access_expires_at']) < time()) { throw new StkApiException(401, 'AUTH_TOKEN_EXPIRED', '登录状态已过期'); }
        return (int)$row['uid'];
    }

    public static function rotate(string $refreshToken, string $deviceId): array {
        if ($refreshToken === '' || $deviceId === '' || !class_exists('DB')) {
            throw new StkApiException(401, 'AUTH_REFRESH_INVALID', '刷新凭据无效');
        }
        $row = DB::fetch_first(
            'SELECT * FROM ' . DB::table('stk_auth_token') . ' WHERE refresh_hash=%s OR previous_refresh_hash=%s LIMIT 1',
            [hash('sha256', $refreshToken), hash('sha256', $refreshToken)]
        );
        if (!$row) { throw new StkApiException(401, 'AUTH_REFRESH_INVALID', '刷新凭据无效'); }
        if (hash_equals((string)$row['previous_refresh_hash'], hash('sha256', $refreshToken))) {
            DB::update('stk_auth_token', ['status' => 'revoked', 'revoked_at' => self::dateTime(time())], ['token_family' => (string)$row['token_family']]);
            throw new StkApiException(401, 'AUTH_TOKEN_REUSED', '检测到重复使用的刷新凭据');
        }
        if ((string)$row['status'] !== 'active' || self::utcTimestamp((string)$row['refresh_expires_at']) < time()
            || !hash_equals((string)$row['device_id_hash'], hash('sha256', $deviceId))) {
            throw new StkApiException(401, 'AUTH_REFRESH_INVALID', '刷新凭据无效');
        }
        $access = bin2hex(random_bytes(32));
        $refresh = bin2hex(random_bytes(48));
        DB::update('stk_auth_token', [
            'access_jti_hash' => hash('sha256', $access),
            'previous_refresh_hash' => (string)$row['refresh_hash'],
            'refresh_hash' => hash('sha256', $refresh),
            'access_expires_at' => self::dateTime(time() + StkSettings::int('auth_access_token_ttl_seconds', 7200, 900, 14400)),
            'refresh_expires_at' => self::dateTime(time() + StkSettings::int('auth_refresh_token_ttl_seconds', 2592000, 86400, 7776000)),
            'rotated_at' => self::dateTime(time()),
            'last_seen_at' => self::dateTime(time()),
        ], ['id' => (int)$row['id']]);
        return ['access_token' => $access, 'access_expires_in' => StkSettings::int('auth_access_token_ttl_seconds', 7200, 900, 14400), 'refresh_token' => $refresh, 'refresh_expires_in' => StkSettings::int('auth_refresh_token_ttl_seconds', 2592000, 86400, 7776000), 'refresh_token_family' => (string)$row['token_family']];
    }

    public static function revokeFamily(int $uid, string $family, string $deviceId): void {
        if (!class_exists('DB')) { throw new StkApiException(503, 'SYS_UNAVAILABLE', '服务暂不可用'); }
        DB::query(
            'UPDATE ' . DB::table('stk_auth_token') . ' SET status=%s,revoked_at=%s WHERE uid=%d AND token_family=%s AND device_id_hash=%s AND status=%s',
            ['revoked', self::dateTime(time()), $uid, $family, hash('sha256', $deviceId), 'active']
        );
    }

    public static function bearerToken(): string {
        $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
        return preg_match('/^Bearer\s+([^\s]+)$/i', $header, $match) ? $match[1] : '';
    }
}
