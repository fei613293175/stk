<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }
require_once dirname(__DIR__) . '/service/ApiResponse.php';
require_once dirname(__DIR__) . '/service/Input.php';
require_once dirname(__DIR__) . '/service/Settings.php';
require_once dirname(__DIR__) . '/service/SecurityService.php';
require_once dirname(__DIR__) . '/service/IdempotencyService.php';
require_once dirname(__DIR__) . '/service/PasswordPolicy.php';
require_once dirname(__DIR__) . '/service/CaptchaService.php';
require_once dirname(__DIR__) . '/service/TokenService.php';
require_once dirname(__DIR__) . '/service/MobileCrypto.php';
require_once dirname(__DIR__) . '/service/SmsService.php';
require_once dirname(__DIR__) . '/service/AuthService.php';

final class StkAuthRouter {
    public static function dispatch(string $operation): void {
        try {
            $input = StkInput::json();
            $method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
            if ($method === 'GET' && $operation === 'bootstrap') { self::bootstrap(); }
            if ($method === 'POST' && $operation === 'captcha_challenge') { self::captchaChallenge($input); }
            if ($method === 'POST' && $operation === 'captcha_verify') { self::captchaVerify($input); }
            if ($method === 'POST' && $operation === 'sms_send') { self::sendSms($input); }
            if ($method === 'POST' && $operation === 'login_password') { self::loginPassword($input); }
            if ($method === 'POST' && $operation === 'login_sms') { self::loginSms($input); }
            if ($method === 'POST' && $operation === 'register') { self::register($input); }
            if ($method === 'POST' && $operation === 'password_reset') { self::resetPassword($input); }
            if ($method === 'POST' && $operation === 'token_refresh') { self::refreshToken($input); }
            if ($method === 'POST' && $operation === 'logout') { self::logout($input); }
            if ($method === 'GET' && $operation === 'me_summary') { self::mySummary(); }
            if ($method === 'GET' && strpos($operation, 'legal:') === 0) { self::legal(substr($operation, 6)); }
            self::notFound();
        } catch (StkApiException $error) { StkApiResponse::fail($error); }
          catch (InvalidArgumentException $error) { StkApiResponse::send(422, $error->getMessage(), '请求参数错误'); }
          catch (RuntimeException $error) { StkApiResponse::send(503, 'SYS_UNAVAILABLE', '服务暂不可用'); }
    }

    private static function bootstrap(): void {
        $authState = 'anonymous';
        if (StkTokenService::bearerToken() !== '') {
            StkTokenService::authenticateAccess(StkTokenService::bearerToken());
            $authState = 'authenticated';
        }
        StkApiResponse::send(200, 'OK', 'ok', ['maintenance' => false, 'auth_state' => $authState, 'feature_flags' => ['project_read' => true], 'legal_versions' => ['user_agreement' => '1.0', 'privacy_policy' => '1.0'], 'external_link_policy' => ['allowlist_only' => true], 'release_policy' => ['version_name' => '1.0.0', 'version_code' => 10000]]);
    }

    private static function captchaChallenge(array $input): void {
        $action = StkInput::string($input, 'action', 32);
        $device = StkInput::string($input, 'device_id', 128);
        StkApiResponse::send(200, 'OK', 'ok', StkCaptchaService::challenge($action, $device));
    }

    private static function captchaVerify(array $input): void {
        $ticket = StkCaptchaService::verify(StkInput::string($input, 'challenge_id', 64), StkInput::string($input, 'answer', 16), StkInput::string($input, 'action', 32), StkInput::string($input, 'device_id', 128));
        StkApiResponse::send(200, 'OK', 'ok', ['captcha_ticket' => $ticket, 'ticket_expires_in' => 90]);
    }

    private static function sendSms(array $input): void {
        $mobile = StkInput::mobile($input);
        $device = StkInput::string($input, 'device_id', 128);
        if ($mobile === '' || $device === '') { throw new StkApiException(422, 'SYS_REQUEST_INVALID', '请求参数错误'); }
        $scene = StkInput::string($input, 'scene', 32);
        $data = StkIdempotencyService::run('sendSmsCode', 'mobile:' . hash('sha256', $mobile), $input, static function () use ($input, $mobile, $device, $scene): array {
            StkCaptchaService::consumeTicket(StkInput::string($input, 'captcha_ticket', 128), 'sms_send', $device);
            return StkSmsService::send($mobile, $scene, $device);
        });
        StkApiResponse::send(200, 'OK', 'ok', $data);
    }

    private static function loginPassword(array $input): void {
        $mobile = StkInput::mobile($input);
        $device = StkInput::string($input, 'device_id', 128);
        if ($mobile === '' || $device === '') { throw new StkApiException(422, 'SYS_REQUEST_INVALID', '请求参数错误'); }
        $data = StkIdempotencyService::run('loginWithPassword', 'mobile:' . hash('sha256', $mobile), $input, static function () use ($input, $mobile, $device): array {
            return StkAuthService::loginPassword($mobile, StkInput::string($input, 'password', 64), StkInput::string($input, 'captcha_ticket', 128), $device, StkInput::string($input, 'device_name', 128));
        });
        StkApiResponse::send(200, 'OK', 'ok', $data);
    }

    private static function loginSms(array $input): void {
        $mobile = StkInput::mobile($input);
        $device = StkInput::string($input, 'device_id', 128);
        if ($mobile === '' || $device === '') { throw new StkApiException(422, 'SYS_REQUEST_INVALID', '请求参数错误'); }
        $data = StkIdempotencyService::run('loginWithSms', 'mobile:' . hash('sha256', $mobile), $input, static function () use ($input, $mobile, $device): array {
            return StkAuthService::loginSms($mobile, StkInput::string($input, 'sms_code', 16), StkInput::string($input, 'captcha_ticket', 128), $device, StkInput::string($input, 'device_name', 128));
        });
        StkApiResponse::send(200, 'OK', 'ok', $data);
    }

    private static function register(array $input): void {
        $mobile = StkInput::mobile($input);
        $device = StkInput::string($input, 'device_id', 128);
        if ($mobile === '' || $device === '') { throw new StkApiException(422, 'SYS_REQUEST_INVALID', '请求参数错误'); }
        $data = StkIdempotencyService::run('registerUser', 'mobile:' . hash('sha256', $mobile), $input, static function () use ($input, $mobile, $device): array {
            return StkAuthService::register($mobile, StkInput::string($input, 'password', 64), StkInput::string($input, 'password_confirmation', 64), StkInput::string($input, 'agreement_version', 32), StkInput::string($input, 'privacy_version', 32), StkInput::string($input, 'captcha_ticket', 128), $device);
        });
        StkApiResponse::send(200, 'OK', 'ok', $data);
    }

    private static function resetPassword(array $input): void {
        $mobile = StkInput::mobile($input);
        $device = StkInput::string($input, 'device_id', 128);
        if ($mobile === '' || $device === '') { throw new StkApiException(422, 'SYS_REQUEST_INVALID', '请求参数错误'); }
        $data = StkIdempotencyService::run('resetPassword', 'mobile:' . hash('sha256', $mobile), $input, static function () use ($input, $mobile, $device): array {
            return StkAuthService::resetPassword($mobile, StkInput::string($input, 'sms_code', 16), StkInput::string($input, 'new_password', 64), StkInput::string($input, 'password_confirmation', 64), StkInput::string($input, 'captcha_ticket', 128), $device);
        });
        StkApiResponse::send(200, 'OK', 'ok', $data);
    }

    private static function refreshToken(array $input): void {
        StkApiResponse::send(200, 'OK', 'ok', StkTokenService::rotate(StkInput::string($input, 'refresh_token', 256), StkInput::string($input, 'device_id', 128)));
    }

    private static function logout(array $input): void {
        $uid = StkTokenService::authenticateAccess(StkTokenService::bearerToken());
        $device = StkInput::string($input, 'device_id', 128);
        $family = StkInput::string($input, 'refresh_token_family', 64);
        if ($device === '' || $family === '') { throw new StkApiException(422, 'SYS_REQUEST_INVALID', '请求参数错误'); }
        $data = StkIdempotencyService::run('logout', 'uid:' . $uid . ':family:' . $family, $input, static function () use ($uid, $family, $device): array {
            StkTokenService::revokeFamily($uid, $family, $device);
            return ['revoked' => true];
        });
        StkApiResponse::send(200, 'OK', 'ok', $data);
    }

    private static function mySummary(): void {
        StkApiResponse::send(200, 'OK', 'ok', StkAuthService::summary(StkTokenService::authenticateAccess(StkTokenService::bearerToken())));
    }

    private static function legal(string $documentType): void {
        StkApiResponse::send(200, 'OK', 'ok', StkAuthService::legal($documentType));
    }

    private static function notFound(): void { StkApiResponse::send(404, 'SYS_REQUEST_INVALID', '请求路径不存在'); }
}
