<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

final class StkAuthDevService
{
    public const CAPTCHA_CODE = '2468';
    public const PASSWORD = '12345678';
    public const SMS_CODE = '123456';

    public static function validateMobile(string $mobile): void
    {
        if (!preg_match('/^1[3-9]\d{9}$/', $mobile)) {
            throw new InvalidArgumentException('请输入正确的 11 位手机号', 4001);
        }
    }

    public static function validateCaptcha(string $challenge, string $code): void
    {
        if (strpos($challenge, 'dev-') !== 0 || $code !== self::CAPTCHA_CODE) {
            throw new InvalidArgumentException('安全验证码错误或已失效', 4002);
        }
    }

    public static function authData(string $mobile): array
    {
        self::validateMobile($mobile);
        $suffix = substr($mobile, -4);
        return [
            'access_token' => 'dev-access-' . hash('sha256', $mobile . microtime(true)),
            'refresh_token' => 'dev-refresh-' . hash('sha256', microtime(true) . $mobile),
            'expires_at' => time() + 7200,
            'user' => [
                'uid' => 10000 + (int) $suffix,
                'username' => '商推客用户' . $suffix,
                'mobile_masked' => substr($mobile, 0, 3) . '****' . $suffix,
                'member_label' => '普通用户',
            ],
        ];
    }
}
