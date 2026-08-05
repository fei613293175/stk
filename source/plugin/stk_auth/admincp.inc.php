<?php
if (!defined('IN_DISCUZ') || !defined('IN_ADMINCP')) { exit('Access Denied'); }

require_once __DIR__ . '/admin/AdminSupport.php';

$page = (string)($_GET['page'] ?? 'overview');
$pluginId = (int)$pluginid;
$pages = [
    'overview' => ['ADM-AUTH-001', ['auth_operator']],
    'base' => ['ADM-AUTH-002', []],
    'login' => ['ADM-AUTH-003', []],
    'register' => ['ADM-AUTH-004', []],
    'captcha' => ['ADM-AUTH-005', []],
    'sms' => ['ADM-AUTH-006', []],
    'sms_records' => ['ADM-AUTH-007', ['sms_auditor']],
    'users' => ['ADM-AUTH-008', ['user_operator']],
    'login_logs' => ['ADM-AUTH-009', ['security_auditor']],
    'tokens' => ['ADM-AUTH-010', ['security_auditor']],
    'risks' => ['ADM-AUTH-011', ['security_auditor']],
    'legal' => ['ADM-AUTH-012', ['content_admin']],
    'audit' => ['ADM-AUTH-013', ['security_auditor']],
    'diagnostics' => ['ADM-AUTH-014', []],
];
if (!isset($pages[$page])) $page = 'overview';
StkAdminSupport::requireRoles($pages[$page][1]);

function stk_auth_settings_input(): array {
    $value = $_GET['settings'] ?? $_POST['settings'] ?? [];
    return is_array($value) ? $value : [];
}

function stk_auth_secret_input(): array {
    $value = $_GET['secrets'] ?? $_POST['secrets'] ?? [];
    return is_array($value) ? $value : [];
}

function stk_auth_save_page(string $page, int $pluginId, array $rules): void {
    global $_G;
    if (!StkAdminSupport::submitted('stk_submit')) return;
    try {
        $before = [];
        foreach (array_keys($rules) as $key) $before[$key] = StkAdminSupport::setting('stk_auth', $key);
        $saved = StkAdminSupport::saveSettings('stk_auth', stk_auth_settings_input(), $rules);
        if ($page === 'sms') {
            foreach (stk_auth_secret_input() as $key => $value) {
                if (in_array($key, ['sms.aliyun_access_key_id', 'sms.aliyun_access_key_secret'], true)) {
                    StkAdminSupport::saveSecret($key, trim((string)$value), (int)($_G['uid'] ?? 0));
                }
            }
        }
        StkAdminSupport::audit('stk_auth', 'save_' . $page, 'plugin_config', $page, $before, $saved);
        cpmsg('plugins_setting_succeed', StkAdminSupport::pageUrl('stk_auth', $pluginId, $page), 'succeed');
    } catch (Throwable $error) {
        echo '<div class="infobox"><h4 class="infotitle3">保存失败</h4><p>' . StkAdminSupport::esc($error->getMessage()) . '</p></div>';
    }
}

function stk_auth_render_config(string $page, int $pluginId, string $title, array $fields, array $rules): void {
    stk_auth_save_page($page, $pluginId, $rules);
    StkAdminSupport::formStart('stk_auth', $pluginId, $page);
    showtableheader($title);
    foreach ($fields as $key => $definition) {
        StkAdminSupport::field($key, $definition[0], StkAdminSupport::setting('stk_auth', $key, $definition[1]), $definition[2] ?? 'text', $definition[3] ?? '');
    }
    if ($page === 'sms') {
        StkAdminSupport::field('sms.aliyun_access_key_id', 'CFG-SMS-002 AccessKey ID', '', 'password', StkAdminSupport::secretConfigured('sms.aliyun_access_key_id') ? '已安全配置；留空保持不变' : '尚未配置');
        StkAdminSupport::field('sms.aliyun_access_key_secret', 'CFG-SMS-003 AccessKey Secret', '', 'password', StkAdminSupport::secretConfigured('sms.aliyun_access_key_secret') ? '已安全配置；留空保持不变' : '尚未配置');
    }
    showtablefooter();
    StkAdminSupport::formEnd();
}

function stk_auth_rows(string $sql, array $arguments, callable $mapper): array {
    try {
        return array_map($mapper, DB::fetch_all($sql, $arguments));
    } catch (Throwable $error) {
        return [];
    }
}

try { StkAdminSupport::audit('stk_auth', 'view_' . $page, 'admin_page', $pages[$page][0]); } catch (Throwable $ignored) {}

if ($page === 'overview') {
    $metrics = [];
    $queries = [
        '今日注册' => 'SELECT COUNT(*) total FROM ' . DB::table('stk_auth_mobile') . ' WHERE created_at>=CURDATE()',
        '今日登录成功' => 'SELECT COUNT(*) total FROM ' . DB::table('stk_auth_login_log') . " WHERE result='success' AND created_at>=CURDATE()",
        '今日短信' => 'SELECT COUNT(*) total FROM ' . DB::table('stk_auth_sms_code') . ' WHERE created_at>=CURDATE()',
        '今日登录失败' => 'SELECT COUNT(*) total FROM ' . DB::table('stk_auth_login_log') . " WHERE result<>'success' AND created_at>=CURDATE()",
        '今日风控事件' => 'SELECT COUNT(*) total FROM ' . DB::table('stk_auth_risk_event') . ' WHERE created_at>=CURDATE()',
    ];
    foreach ($queries as $label => $sql) {
        try { $row = DB::fetch_first($sql); $metrics[] = [$label, (int)($row['total'] ?? 0)]; }
        catch (Throwable $error) { $metrics[] = [$label, '不可用']; }
    }
    StkAdminSupport::table('ADM-AUTH-001 登录注册概览', ['指标', '数量'], $metrics);
} elseif ($page === 'base') {
    stk_auth_render_config($page, $pluginId, 'ADM-AUTH-002 基础与跳转设置', [
        'auth_enabled' => ['CFG-AUTH-001 插件开关', '1'],
        'auth_force_login' => ['CFG-AUTH-002 强制登录', '1'],
        'auth_login_success_route' => ['CFG-AUTH-003 登录成功路由', 'stk://home'],
        'auth_register_success_route' => ['CFG-AUTH-004 注册成功路由', 'stk://home'],
        'auth_logout_route' => ['CFG-AUTH-005 退出路由', 'stk://auth/login'],
        'auth_default_login_mode' => ['CFG-AUTH-006 默认登录方式', 'password'],
        'admin_role_bindings' => ['后台角色绑定 JSON', '{}', 'textarea', '格式：{"UID":["security_auditor"]}'],
    ], [
        'auth_enabled' => ['type'=>'bool'], 'auth_force_login' => ['type'=>'bool'],
        'auth_login_success_route' => ['type'=>'enum','values'=>['stk://home']],
        'auth_register_success_route' => ['type'=>'enum','values'=>['stk://home']],
        'auth_logout_route' => ['type'=>'enum','values'=>['stk://auth/login']],
        'auth_default_login_mode' => ['type'=>'enum','values'=>['password','sms']],
        'admin_role_bindings' => ['type'=>'json_roles'],
    ]);
} elseif ($page === 'login') {
    stk_auth_render_config($page, $pluginId, 'ADM-AUTH-003 登录策略', [
        'auth_password_min_length'=>['CFG-AUTH-011 密码最短长度','8','number'],
        'auth_password_max_length'=>['CFG-AUTH-012 密码最长长度','32','number'],
        'auth_password_category_min'=>['CFG-AUTH-013 密码字符类别','2','number'],
        'auth_login_failure_limit'=>['CFG-AUTH-014 失败上限','5','number'],
        'auth_login_lock_seconds'=>['CFG-AUTH-015 锁定秒数','900','number'],
        'auth_access_token_ttl_seconds'=>['CFG-AUTH-016 Access Token 秒数','7200','number'],
        'auth_refresh_token_ttl_seconds'=>['CFG-AUTH-017 Refresh Token 秒数','2592000','number'],
        'auth_max_active_devices'=>['CFG-AUTH-018 活跃设备上限','5','number'],
        'auth_refresh_rotation_enabled'=>['CFG-AUTH-019 刷新轮换','1'],
        'auth_revoke_on_password_reset'=>['CFG-AUTH-020 重置撤销令牌','1'],
    ], [
        'auth_password_min_length'=>['type'=>'int','min'=>8,'max'=>32], 'auth_password_max_length'=>['type'=>'int','min'=>16,'max'=>64],
        'auth_password_category_min'=>['type'=>'int','min'=>1,'max'=>4], 'auth_login_failure_limit'=>['type'=>'int','min'=>3,'max'=>10],
        'auth_login_lock_seconds'=>['type'=>'int','min'=>60,'max'=>86400], 'auth_access_token_ttl_seconds'=>['type'=>'int','min'=>900,'max'=>14400],
        'auth_refresh_token_ttl_seconds'=>['type'=>'int','min'=>86400,'max'=>7776000], 'auth_max_active_devices'=>['type'=>'int','min'=>1,'max'=>20],
        'auth_refresh_rotation_enabled'=>['type'=>'bool'], 'auth_revoke_on_password_reset'=>['type'=>'bool'],
    ]);
} elseif ($page === 'register') {
    stk_auth_render_config($page, $pluginId, 'ADM-AUTH-004 注册策略', [
        'auth_registration_enabled'=>['CFG-AUTH-007 开放注册','1'],
        'auth_internal_username_prefix'=>['CFG-AUTH-008 用户名前缀','stk_'],
        'auth_display_name_prefix'=>['CFG-AUTH-009 昵称前缀','商推客用户'],
        'auth_mobile_country_code'=>['CFG-AUTH-010 国家码','+86'],
    ], [
        'auth_registration_enabled'=>['type'=>'bool'], 'auth_internal_username_prefix'=>['type'=>'regex','pattern'=>'/^[a-z][a-z0-9_]{1,12}$/','min'=>2,'max'=>13],
        'auth_display_name_prefix'=>['type'=>'string','min'=>1,'max'=>12], 'auth_mobile_country_code'=>['type'=>'enum','values'=>['+86']],
    ]);
} elseif ($page === 'captcha') {
    stk_auth_render_config($page, $pluginId, 'ADM-AUTH-005 安全验证码设置', [
        'captcha_enabled'=>['CFG-CAP-001 开关','1'], 'captcha_type'=>['CFG-CAP-002 类型','alphanumeric_image'],
        'captcha_length'=>['CFG-CAP-003 长度','4','number'], 'captcha_challenge_ttl_seconds'=>['CFG-CAP-004 Challenge 秒数','120','number'],
        'captcha_ticket_ttl_seconds'=>['CFG-CAP-005 Ticket 秒数','90','number'], 'captcha_max_attempts'=>['CFG-CAP-006 错误上限','5','number'],
        'captcha_refresh_interval_seconds'=>['CFG-CAP-007 刷新间隔','3','number'],
        'captcha_required_actions'=>['CFG-CAP-008 强制动作','password_login,sms_send,sms_login,register,password_reset'],
    ], [
        'captcha_enabled'=>['type'=>'enum','values'=>['1']], 'captcha_type'=>['type'=>'enum','values'=>['alphanumeric_image','arithmetic_image']],
        'captcha_length'=>['type'=>'int','min'=>4,'max'=>6], 'captcha_challenge_ttl_seconds'=>['type'=>'int','min'=>60,'max'=>300],
        'captcha_ticket_ttl_seconds'=>['type'=>'int','min'=>30,'max'=>180], 'captcha_max_attempts'=>['type'=>'int','min'=>2,'max'=>10],
        'captcha_refresh_interval_seconds'=>['type'=>'int','min'=>2,'max'=>30],
        'captcha_required_actions'=>['type'=>'enum','values'=>['password_login,sms_send,sms_login,register,password_reset']],
    ]);
} elseif ($page === 'sms') {
    stk_auth_render_config($page, $pluginId, 'ADM-AUTH-006 阿里云短信设置', [
        'sms_enabled'=>['CFG-SMS-001 短信开关','0'], 'sms_region_id'=>['CFG-SMS-004 Region','cn-hangzhou'],
        'sms_sign_name'=>['CFG-SMS-005 短信签名',''], 'sms_template_login'=>['CFG-SMS-006 登录模板',''],
        'sms_template_password_reset'=>['CFG-SMS-007 重置模板',''], 'sms_template_code_variable'=>['CFG-SMS-008 验证码变量','code'],
        'sms_code_digits'=>['CFG-SMS-009 验证码位数','6','number'], 'sms_code_ttl_seconds'=>['CFG-SMS-010 有效期','300','number'],
        'sms_resend_interval_seconds'=>['CFG-SMS-011 重发间隔','60','number'], 'sms_phone_hour_limit'=>['CFG-SMS-012 手机号小时上限','5','number'],
        'sms_phone_day_limit'=>['CFG-SMS-013 手机号日上限','10','number'], 'sms_ip_hour_limit'=>['CFG-SMS-014 IP 小时上限','20','number'],
        'sms_device_hour_limit'=>['CFG-SMS-015 设备小时上限','10','number'], 'sms_provider_timeout_seconds'=>['CFG-SMS-016 超时秒数','5','number'],
        'sms_receipt_query_enabled'=>['CFG-SMS-017 回执查询','1'], 'sms_test_mobile'=>['CFG-SMS-018 测试手机号',''],
        'sms_code_reveal_roles'=>['CFG-SMS-019 明文查看角色','super_admin,sms_auditor'],
        'sms_code_ciphertext_retention_days'=>['CFG-SMS-020 密文保留天数','90','number'],
    ], [
        'sms_enabled'=>['type'=>'bool'], 'sms_region_id'=>['type'=>'string','min'=>2,'max'=>32], 'sms_sign_name'=>['type'=>'string','min'=>0,'max'=>64],
        'sms_template_login'=>['type'=>'regex','pattern'=>'/^(|SMS_[0-9]+)$/','min'=>0,'max'=>32], 'sms_template_password_reset'=>['type'=>'regex','pattern'=>'/^(|SMS_[0-9]+)$/','min'=>0,'max'=>32],
        'sms_template_code_variable'=>['type'=>'regex','pattern'=>'/^[A-Za-z][A-Za-z0-9_]{0,31}$/','min'=>1,'max'=>32],
        'sms_code_digits'=>['type'=>'int','min'=>4,'max'=>8], 'sms_code_ttl_seconds'=>['type'=>'int','min'=>60,'max'=>900],
        'sms_resend_interval_seconds'=>['type'=>'int','min'=>30,'max'=>300], 'sms_phone_hour_limit'=>['type'=>'int','min'=>1,'max'=>20],
        'sms_phone_day_limit'=>['type'=>'int','min'=>1,'max'=>50], 'sms_ip_hour_limit'=>['type'=>'int','min'=>5,'max'=>200],
        'sms_device_hour_limit'=>['type'=>'int','min'=>3,'max'=>100], 'sms_provider_timeout_seconds'=>['type'=>'int','min'=>2,'max'=>15],
        'sms_receipt_query_enabled'=>['type'=>'bool'], 'sms_test_mobile'=>['type'=>'regex','pattern'=>'/^(|1[3-9][0-9]{9})$/','min'=>0,'max'=>11],
        'sms_code_reveal_roles'=>['type'=>'enum','values'=>['super_admin,sms_auditor']], 'sms_code_ciphertext_retention_days'=>['type'=>'int','min'=>7,'max'=>180],
    ]);
} elseif ($page === 'legal') {
    stk_auth_render_config($page, $pluginId, 'ADM-AUTH-012 协议与隐私版本', [
        'agreement_version'=>['CFG-LEGAL-001 用户协议版本','1.0'], 'agreement_effective_at'=>['用户协议生效日期',''],
        'agreement_content'=>['用户协议正文','', 'textarea'], 'privacy_version'=>['CFG-LEGAL-002 隐私政策版本','1.0'],
        'privacy_effective_at'=>['隐私政策生效日期',''], 'privacy_content'=>['隐私政策正文','', 'textarea'],
        'legal_require_current_version_on_register'=>['CFG-LEGAL-003 注册要求当前版本','1'],
    ], [
        'agreement_version'=>['type'=>'regex','pattern'=>'/^[0-9]+(\.[0-9]+){0,2}$/','min'=>1,'max'=>32],
        'agreement_effective_at'=>['type'=>'string','min'=>0,'max'=>32], 'agreement_content'=>['type'=>'string','min'=>1,'max'=>200000],
        'privacy_version'=>['type'=>'regex','pattern'=>'/^[0-9]+(\.[0-9]+){0,2}$/','min'=>1,'max'=>32],
        'privacy_effective_at'=>['type'=>'string','min'=>0,'max'=>32], 'privacy_content'=>['type'=>'string','min'=>1,'max'=>200000],
        'legal_require_current_version_on_register'=>['type'=>'enum','values'=>['1']],
    ]);
} elseif ($page === 'sms_records') {
    $rows = stk_auth_rows('SELECT id,scene,mobile_hash,send_status,use_status,biz_id,created_at FROM ' . DB::table('stk_auth_sms_code') . ' ORDER BY id DESC LIMIT 100', [], fn($r) => [$r['id'],$r['scene'],substr($r['mobile_hash'],0,12).'…',$r['send_status'],$r['use_status'],$r['biz_id'] ?? '',$r['created_at']]);
    StkAdminSupport::table('ADM-AUTH-007 短信验证码记录', ['ID','场景','手机号摘要','发送','使用','BizId','时间'], $rows);
} elseif ($page === 'users') {
    $rows = stk_auth_rows('SELECT a.uid,a.mobile_last4,a.verified_at,a.status,a.created_at,m.username FROM ' . DB::table('stk_auth_mobile') . ' a LEFT JOIN ' . DB::table('common_member') . ' m ON m.uid=a.uid ORDER BY a.uid DESC LIMIT 100', [], fn($r) => [$r['uid'],$r['username'] ?? '','***'.$r['mobile_last4'],$r['verified_at'] ? '已验证':'未验证',$r['status'],$r['created_at']]);
    StkAdminSupport::table('ADM-AUTH-008 注册用户列表', ['UID','内部用户名','手机号','验证','状态','注册时间'], $rows);
} elseif ($page === 'login_logs') {
    $rows = stk_auth_rows('SELECT id,uid,login_type,result,error_code,ip_hash,device_hash,created_at FROM ' . DB::table('stk_auth_login_log') . ' ORDER BY id DESC LIMIT 100', [], fn($r) => [$r['id'],$r['uid'] ?? '',$r['login_type'],$r['result'],$r['error_code'] ?? '',substr($r['ip_hash'],0,10).'…',substr($r['device_hash'],0,10).'…',$r['created_at']]);
    StkAdminSupport::table('ADM-AUTH-009 登录日志', ['ID','UID','方式','结果','错误码','IP 摘要','设备摘要','时间'], $rows);
} elseif ($page === 'tokens') {
    if (StkAdminSupport::submitted('revoke_submit')) {
        $tokenInput = $_GET['settings'] ?? $_POST['settings'] ?? [];
        $tokenId = (int)(is_array($tokenInput) ? ($tokenInput['token_id'] ?? 0) : 0);
        $before = DB::fetch_first('SELECT id,uid,token_family,status FROM ' . DB::table('stk_auth_token') . ' WHERE id=%d LIMIT 1', [$tokenId]);
        if ($before) {
            DB::update('stk_auth_token', ['status'=>'revoked','revoked_at'=>gmdate('Y-m-d H:i:s')], ['id'=>$tokenId]);
            StkAdminSupport::audit('stk_auth','revoke_token','auth_token',(string)$tokenId,$before,['status'=>'revoked']);
        }
    }
    StkAdminSupport::formStart('stk_auth',$pluginId,$page); showtableheader('撤销 Token'); StkAdminSupport::field('token_id','要撤销的 Token ID','','number'); showtablefooter(); StkAdminSupport::formEnd('revoke_submit','撤销');
    $rows = stk_auth_rows('SELECT id,uid,token_family,device_name,status,access_expires_at,refresh_expires_at FROM ' . DB::table('stk_auth_token') . ' ORDER BY id DESC LIMIT 100', [], fn($r) => [$r['id'],$r['uid'],substr($r['token_family'],0,12).'…',$r['device_name'] ?? '',$r['status'],$r['access_expires_at'],$r['refresh_expires_at']]);
    StkAdminSupport::table('ADM-AUTH-010 Token 与设备会话', ['ID','UID','Token 族','设备','状态','Access 到期','Refresh 到期'], $rows);
} elseif ($page === 'risks') {
    $rows = stk_auth_rows('SELECT id,scene,subject_type,subject_hash,rule_code,action,unlock_at,created_at FROM ' . DB::table('stk_auth_risk_event') . ' ORDER BY id DESC LIMIT 100', [], fn($r) => [$r['id'],$r['scene'],$r['subject_type'],substr($r['subject_hash'],0,12).'…',$r['rule_code'],$r['action'],$r['unlock_at'] ?? '',$r['created_at']]);
    StkAdminSupport::table('ADM-AUTH-011 风控与限流事件', ['ID','场景','主体','摘要','规则','动作','解锁','时间'], $rows);
} elseif ($page === 'audit') {
    $rows = stk_auth_rows('SELECT id,admin_uid,module,action,target_type,target_id,request_id,created_at FROM ' . DB::table('stk_admin_audit') . ' ORDER BY id DESC LIMIT 200', [], fn($r) => [$r['id'],$r['admin_uid'],$r['module'],$r['action'],$r['target_type'],$r['target_id'] ?? '',$r['request_id'],$r['created_at']]);
    StkAdminSupport::table('ADM-AUTH-013 管理操作审计', ['ID','管理员','模块','动作','对象','对象ID','请求ID','时间'], $rows);
} elseif ($page === 'diagnostics') {
    $checks = [];
    foreach (['stk_auth_mobile','stk_auth_captcha_challenge','stk_auth_token','stk_admin_audit','stk_project'] as $table) {
        try { DB::fetch_first('SELECT 1 FROM ' . DB::table($table) . ' LIMIT 1'); $checks[] = ['数据库表 ' . $table,'正常']; }
        catch (Throwable $error) { $checks[] = ['数据库表 ' . $table,'缺失或不可访问']; }
    }
    foreach (['openssl','curl','mbstring','mysqli','gd'] as $extension) $checks[] = ['PHP 扩展 ' . $extension, extension_loaded($extension) ? '正常' : '缺失'];
    $checks[] = ['服务端加密主密钥', !empty($_G['config']['security']['authkey']) ? '正常' : '缺失'];
    $checks[] = ['阿里云 AccessKey ID', StkAdminSupport::secretConfigured('sms.aliyun_access_key_id') ? '已配置' : '待 Owner 配置'];
    $checks[] = ['阿里云 AccessKey Secret', StkAdminSupport::secretConfigured('sms.aliyun_access_key_secret') ? '已配置' : '待 Owner 配置'];
    $checks[] = ['API 网关文件', is_file(__DIR__ . '/gateway.inc.php') ? '正常' : '缺失'];
    StkAdminSupport::table('ADM-AUTH-014 认证系统诊断', ['检查项','状态'], $checks);
}
