<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

require_once __DIR__ . '/lib/response.php';
require_once __DIR__ . '/lib/config.php';
require_once __DIR__ . '/lib/auth_service.php';
require_once __DIR__ . '/lib/dev_auth_service.php';
require_once __DIR__ . '/lib/sms_service.php';
require_once __DIR__ . '/lib/routes.php';

if (stk_auth_get_config('api_enabled', '1') !== '1') {
    stk_auth_json(5030, 'API 已关闭', null, 503);
}

$action = isset($_GET['action']) ? (string) $_GET['action'] : 'health';
$resource = trim((string) ($_GET['resource'] ?? ''), '/');
$route = stk_auth_normalize_route($action, $resource, (string) ($_SERVER['REQUEST_METHOD'] ?? 'GET'));
$action = $route['action'];
$input = stk_auth_json_input();
if ($route['method'] === 'GET') $input = array_replace($_GET, $input);
if ($route['document_id'] !== '') $input['document_id'] = $route['document_id'];
$fakeEnabled = stk_auth_get_config('dev_fake_enabled', '0') === '1';

function stk_auth_validate_dev_action_captcha(array $input, string $scene): void
{
    $ticket = trim((string) ($input['captcha_ticket'] ?? ''));
    if ($ticket === 'dev-ticket-' . preg_replace('/[^a-z_]/', '', $scene)) return;
    StkAuthDevService::validateCaptcha(
        (string) ($input['captcha_challenge'] ?? ''),
        (string) ($input['captcha_code'] ?? '')
    );
}

try {
    stk_auth_cleanup_retained_data();
    if ($route['method'] !== 'GET') stk_auth_write_guard($action);
    if ($route['expected_method'] !== '' && $route['method'] !== $route['expected_method']) {
        stk_auth_json(4050, '请求方法不允许', null, 405);
    }
    if ($action === 'health_live') {
        stk_auth_json(0, 'ok', ['plugin' => 'stk_auth', 'status' => 'live', 'version' => '1.4.0']);
    }

    if ($action === 'health_ready') {
        $ready = (int) DB::result_first('SELECT COUNT(*) FROM %t', ['stk_auth_config']) > 0;
        stk_auth_json($ready ? 0 : 5031, $ready ? 'ok' : '认证插件尚未完成数据库迁移', ['ready' => $ready], $ready ? 200 : 503);
    }

    if ($action === 'bootstrap') {
        $release = [];
        $maintenance = ['enabled' => false, 'message' => '', 'resume_at' => null];
        try {
            $release = DB::fetch_first(
                'SELECT version_name,version_code,minimum_version_name,minimum_version_code,mandatory FROM %t WHERE enabled=1 AND status=%s ORDER BY version_code DESC LIMIT 1',
                ['stk_release', 'published']
            ) ?: [];
            $maintenanceEnabled = stk_auth_get_config(
                'maintenance.enabled',
                stk_auth_get_config('maintenance_enabled', '0')
            );
            $maintenanceMessage = stk_auth_get_config(
                'maintenance.message',
                stk_auth_get_config('maintenance_message', '')
            );
            $maintenanceResumeAt = stk_auth_get_config(
                'maintenance.expected_end',
                stk_auth_get_config('maintenance_resume_at', '')
            );
            $maintenance = [
                'enabled' => $maintenanceEnabled === '1',
                'message' => $maintenanceMessage,
                'resume_at' => $maintenanceResumeAt !== '' ? $maintenanceResumeAt : null,
            ];
        } catch (Throwable $ignored) {
            $release = [];
        }
        stk_auth_json(0, 'ok', [
            'api_version' => 'v1',
            'maintenance' => $maintenance,
            'release' => [
                'version_name' => (string) ($release['version_name'] ?? ''),
                'version_code' => (int) ($release['version_code'] ?? 0),
                'minimum_version_name' => (string) ($release['minimum_version_name'] ?? ''),
                'minimum_version_code' => (int) ($release['minimum_version_code'] ?? 0),
                'mandatory' => !empty($release['mandatory']),
            ],
            'auth' => [
                'password_enabled' => stk_auth_get_config('allow_password_login', '1') === '1',
                'sms_enabled' => stk_auth_get_config('allow_sms_login', '1') === '1'
                    && stk_auth_get_config('sms_enabled', '0') === '1',
                'register_enabled' => stk_auth_get_config('allow_register', '1') === '1',
                'default_login_tab' => stk_auth_get_config('default_login_tab', 'password'),
                'login_success_route' => stk_auth_get_config('login_success_route', 'stk://home'),
                'register_success_route' => stk_auth_get_config('register_success_route', 'stk://home'),
            ],
            'legal' => [
                'user_agreement_url' => stk_auth_get_config('legal_user_agreement_url', '/legal/user-agreement'),
                'privacy_url' => stk_auth_get_config('legal_privacy_url', '/legal/privacy'),
            ],
        ]);
    }

    if ($action === 'health') {
        $databaseCheck = DB::fetch_first('SELECT 1 AS ok');
        stk_auth_json(0, 'ok', [
            'plugin' => 'stk_auth',
            'version' => '1.4.0',
            'database' => ((int) ($databaseCheck['ok'] ?? 0) === 1) ? 'reachable' : 'unknown',
            'fake_enabled' => $fakeEnabled,
            'password_auth' => stk_auth_get_config('allow_password_login', '1') === '1',
            'sms_enabled' => stk_auth_get_config('allow_sms_login', '1') === '1'
                && stk_auth_get_config('sms_enabled', '0') === '1',
            'sms_provider_ready' => stk_auth_get_config('sms_access_key_id', '') !== ''
                && stk_auth_get_config('sms_access_key_secret', '') !== ''
                && stk_auth_get_config('sms_sign_name', '') !== ''
                && stk_auth_get_config('sms_template_code', '') !== '',
        ]);
    }

    if ($action === 'config') {
        stk_auth_json(0, 'ok', [
            'api_version' => 'v1',
        'sms_enabled' => stk_auth_get_config('allow_sms_login', '1') === '1'
            && stk_auth_get_config('sms_enabled', '0') === '1',
            'register_enabled' => stk_auth_get_config('allow_register', '1') === '1',
            'password_enabled' => stk_auth_get_config('allow_password_login', '1') === '1',
            'default_login_tab' => stk_auth_get_config('default_login_tab', 'password'),
            'fake_enabled' => $fakeEnabled,
            'password_min_length' => max(8, (int) stk_auth_get_config('password_min_length', '8')),
            'password_max_length' => min(64, (int) stk_auth_get_config('password_max_length', '64')),
        ]);
    }

    if ($action === 'legal_document') {
        $documentId = trim((string) ($input['document_id'] ?? ''));
        stk_auth_json(0, 'ok', stk_auth_legal_document($documentId));
    }

    if ($action === 'captcha_create') {
        if ($fakeEnabled) {
            $scene = isset($input['scene']) ? (string) $input['scene'] : 'unknown';
            stk_auth_json(0, 'ok', [
                'challenge_id' => 'dev-' . preg_replace('/[^a-z_]/', '', $scene),
                'prompt' => '开发验证码：2468',
                'expires_in' => 300,
            ]);
        }
        stk_auth_json(0, 'ok', stk_auth_create_captcha((string) ($input['scene'] ?? 'unknown')));
    }

    if ($action === 'captcha_verify') {
        $scene = (string) ($input['scene'] ?? 'unknown');
        if ($fakeEnabled) {
            StkAuthDevService::validateCaptcha(
                (string) ($input['captcha_challenge'] ?? ''),
                (string) ($input['captcha_code'] ?? '')
            );
            stk_auth_json(0, 'ok', ['captcha_ticket' => 'dev-ticket-' . preg_replace('/[^a-z_]/', '', $scene), 'expires_in' => 180]);
        }
        stk_auth_validate_captcha(
            (string) ($input['captcha_challenge'] ?? ''),
            (string) ($input['captcha_code'] ?? ''),
            $scene
        );
        stk_auth_json(0, 'ok', stk_auth_issue_captcha_ticket($scene));
    }

    if ($action === 'account_overview') {
        $token = stk_auth_bearer_token();
        if ($token === '') {
            stk_auth_json(4010, '缺少访问令牌', null, 401);
        }
        $uid = ($fakeEnabled && strpos($token, 'dev-access-') === 0)
            ? 10001
            : stk_auth_token_uid($token, 'access');
        if ($uid <= 0) {
            stk_auth_json(4011, '访问令牌无效或已过期', null, 401);
        }
        DB::query("INSERT IGNORE INTO %t (uid,status,member_label,level,starts_at,expires_at,updated_at) VALUES (%d,'inactive','普通用户','L1',0,0,%d)", ['stk_member_status', $uid, TIMESTAMP]);
        DB::query('INSERT IGNORE INTO %t (uid,commission_amount,task_amount,task_points,updated_at) VALUES (%d,0,0,0,%d)', ['stk_wallet_account', $uid, TIMESTAMP]);
        $member = DB::fetch_first('SELECT status,member_label,level,expires_at FROM %t WHERE uid=%d', ['stk_member_status', $uid]) ?: [];
        $balance = DB::fetch_first('SELECT commission_amount,task_amount FROM %t WHERE uid=%d', ['stk_wallet_account', $uid]) ?: [];
        $props = DB::fetch_all('SELECT prop_id AS id,name AS title,description,icon_url,sort_order FROM %t WHERE enabled=1 ORDER BY sort_order,prop_id', ['stk_prop_catalog']);
        $user = stk_auth_member_data($uid);
        $memberExpiresAt = (int) ($member['expires_at'] ?? 0);
        $memberStatus = (string) ($member['status'] ?? 'inactive');
        if ($memberStatus === 'active' && $memberExpiresAt > 0 && $memberExpiresAt < TIMESTAMP) $memberStatus = 'expired';
        $memberActive = $memberStatus === 'active';
        stk_auth_json(0, 'ok', [
            'profile' => [
                'username' => $user['username'],
                'mobile_masked' => $user['mobile_masked'],
                'member_label' => $user['member_label'],
                'bio' => stk_auth_get_config('profile_bio', '欢迎使用商推客，完善资料有助于项目展示。'),
            ],
            'membership' => [
                'label' => $member['member_label'] ?? '普通会员',
                'level' => $member['level'] ?? 'L1',
                'expires_at' => !empty($member['expires_at']) ? date('Y-m-d', (int) $member['expires_at']) : null,
                'progress' => $memberActive ? 100 : 0,
                'status' => $memberStatus,
            ],
            'commission' => [
                'title' => stk_auth_get_config('commission_label', '佣金账户'),
                'amount' => number_format((float) ($balance['commission_amount'] ?? 0), 2),
                'description' => '当前版本只展示账户余额，不执行结算或提现。',
            ],
            'tasks' => [
                'title' => stk_auth_get_config('task_label', '任务账户'),
                'amount' => number_format((float) ($balance['task_amount'] ?? 0), 2),
                'description' => '当前版本只展示账户余额，不执行资金操作。',
            ],
            'benefits' => [
                ['title' => '消费折扣', 'description' => stk_auth_get_config('member_benefit_discount', '消费 5 折（仅展示）'), 'enabled' => $memberActive],
                ['title' => '消费返佣', 'description' => stk_auth_get_config('member_benefit_rebate', '消费返佣 40%（仅展示）'), 'enabled' => $memberActive],
            ],
            'props' => $props,
            'support' => [
                'type' => stk_auth_get_config('support_type', 'wechat'),
                'label' => stk_auth_get_config('support_label', '在线客服'),
                'value' => stk_auth_get_config('support_value', ''),
                'service_hours' => stk_auth_get_config('support_hours', '工作日 09:00-18:00'),
                'copy_enabled' => stk_auth_get_config('support_copy_enabled', '1') === '1',
            ],
            'display' => [
                'member_title' => stk_auth_get_config('member_title', '商推客会员'),
                'commission_label' => stk_auth_get_config('commission_label', '佣金账户'),
                'task_label' => stk_auth_get_config('task_label', '任务账户'),
                'placeholder_message' => stk_auth_get_config('placeholder_message', '功能筹备中'),
                'show_wallets' => stk_auth_get_config('show_wallets', '1') === '1',
                'show_member_card' => stk_auth_get_config('show_member_card', '1') === '1',
                'show_props_center' => stk_auth_get_config('show_props_center', '1') === '1',
                'member_open_button_text' => stk_auth_get_config('member_open_button_text', '了解会员'),
            ],
        ]);
    }

    if ($action === 'me_summary') {
        $token = stk_auth_bearer_token();
        $uid = stk_auth_token_uid($token, 'access');
        if ($uid <= 0) {
            stk_auth_json(4011, '访问令牌无效或已过期', null, 401);
        }
        stk_auth_json(0, 'ok', stk_auth_member_data($uid));
    }

    $mobile = trim((string) ($input['mobile'] ?? ''));
    $challenge = (string) ($input['captcha_challenge'] ?? '');
    $captchaCode = (string) ($input['captcha_code'] ?? '');

    if ($action === 'password_login') {
        if (stk_auth_get_config('allow_password_login', '1') !== '1') stk_auth_json(4030, '密码登录已关闭', null, 403);
        if ($fakeEnabled) {
            StkAuthDevService::validateMobile($mobile);
            stk_auth_validate_dev_action_captcha($input, 'password_login');
            if (($input['password'] ?? '') !== StkAuthDevService::PASSWORD) {
                stk_auth_json(4003, '手机号或密码错误', null, 401);
            }
            stk_auth_json(0, 'ok', StkAuthDevService::authData($mobile));
        }
        stk_auth_validate_action_captcha($input, 'password_login');
        stk_auth_json(0, 'ok', stk_auth_password_login($mobile, (string) ($input['password'] ?? '')));
    }

    if ($action === 'register') {
        if (stk_auth_get_config('allow_register', '1') !== '1') stk_auth_json(4030, '注册功能已关闭', null, 403);
        $password = (string) ($input['password'] ?? '');
        $confirm = (string) ($input['confirm_password'] ?? '');
        $passwordMin = max(8, (int) stk_auth_get_config('password_min_length', '8'));
        $passwordMax = min(64, (int) stk_auth_get_config('password_max_length', '64'));
        if (strlen($password) < $passwordMin || strlen($password) > $passwordMax) {
            stk_auth_json(4001, '密码长度必须为 ' . $passwordMin . ' 至 ' . $passwordMax . ' 位', null, 400);
        }
        if ($password !== $confirm) {
            stk_auth_json(4001, '两次输入的密码不一致', null, 400);
        }
        if ($fakeEnabled) {
            StkAuthDevService::validateMobile($mobile);
            stk_auth_validate_dev_action_captcha($input, 'register');
            stk_auth_json(0, 'ok', StkAuthDevService::authData($mobile));
        }
        stk_auth_validate_action_captcha($input, 'register');
        stk_auth_json(0, 'ok', stk_auth_register($mobile, $password));
    }

    if ($action === 'sms_send' || $action === 'sms_login') {
        if (stk_auth_get_config('allow_sms_login', '1') !== '1') stk_auth_json(4030, '短信登录已关闭', null, 403);
        if (stk_auth_get_config('sms_enabled', '0') !== '1' && !$fakeEnabled) stk_auth_json(5030, '短信服务已关闭', null, 503);
        if ($fakeEnabled) {
            StkAuthDevService::validateMobile($mobile);
            stk_auth_validate_dev_action_captcha($input, $action);
            if ($action === 'sms_send') {
                stk_auth_json(0, 'ok', ['retry_after_seconds' => 60, 'debug_code' => StkAuthDevService::SMS_CODE]);
            }
            if (($input['sms_code'] ?? '') !== StkAuthDevService::SMS_CODE) {
                stk_auth_json(4004, '短信验证码错误或已失效', null, 401);
            }
            stk_auth_json(0, 'ok', StkAuthDevService::authData($mobile));
        }
        stk_auth_validate_action_captcha($input, $action);
        if ($action === 'sms_send') {
            stk_auth_json(0, 'ok', stk_auth_send_sms_code($mobile));
        }
        stk_auth_json(0, 'ok', stk_auth_sms_login($mobile, (string) ($input['sms_code'] ?? '')));
    }

    if ($action === 'refresh') {
        $refreshToken = trim((string) ($input['refresh_token'] ?? ''));
        $uid = stk_auth_token_uid($refreshToken, 'refresh');
        if ($uid <= 0) {
            stk_auth_json(4011, '刷新令牌无效或已过期', null, 401);
        }
        stk_auth_revoke_token($refreshToken);
        stk_auth_json(0, 'ok', stk_auth_session_data($uid));
    }

    if ($action === 'logout') {
        stk_auth_revoke_session(stk_auth_bearer_token());
        stk_auth_json(0, 'ok', ['logged_out' => true]);
    }

    stk_auth_json(4040, '未知 action', null, 404);
} catch (InvalidArgumentException $error) {
    $code = $error->getCode() > 0 ? $error->getCode() : 4001;
    $httpStatus = $code === 4003 ? 401 : ($code === 4290 ? 429 : ($code === 4093 ? 409 : 400));
    stk_auth_json($code, $error->getMessage(), null, $httpStatus);
} catch (RuntimeException $error) {
    if (in_array((int) $error->getCode(), [5031, 5032, 5033], true)) {
        if ((int) $error->getCode() === 5031) {
            stk_auth_json(5031, $error->getMessage(), null, 503);
        }
        if ((int) $error->getCode() === 5033) {
            stk_auth_json(5033, '短信密钥安全存储不可用', null, 503);
        }
        stk_auth_json(5032, '图形验证码服务暂不可用，请稍后再试', null, 503);
    }
    stk_auth_json(5000, '服务器内部错误', null, 500);
} catch (Throwable $error) {
    stk_auth_json($error->getCode() > 0 ? $error->getCode() : 5000, '服务器内部错误', null, 500);
}
