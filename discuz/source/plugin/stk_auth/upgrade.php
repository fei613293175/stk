<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

require_once __DIR__ . '/lib/config.php';

$sql = <<<SQL
CREATE TABLE IF NOT EXISTS `pre_stk_auth_captcha` (
  `challenge_id` varchar(64) NOT NULL,
  `scene` varchar(32) NOT NULL,
  `code_hash` varchar(255) NOT NULL,
  `client_ip` varchar(64) NOT NULL DEFAULT '',
  `attempt_count` tinyint unsigned NOT NULL DEFAULT '0',
  `status` varchar(16) NOT NULL DEFAULT 'active',
  `expires_at` int unsigned NOT NULL DEFAULT '0',
  `used_at` int unsigned NOT NULL DEFAULT '0',
  `created_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`challenge_id`),
  KEY `idx_ip_scene_created` (`client_ip`,`scene`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_auth_captcha_ticket` (
  `ticket_hash` char(64) NOT NULL,`scene` varchar(32) NOT NULL,`client_ip` varchar(64) NOT NULL DEFAULT '',
  `status` varchar(16) NOT NULL DEFAULT 'active',`expires_at` int unsigned NOT NULL DEFAULT 0,
  `used_at` int unsigned NOT NULL DEFAULT 0,`created_at` int unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`ticket_hash`),KEY `idx_scene_ip_created` (`scene`,`client_ip`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_security_event` (
  `event_id` bigint unsigned NOT NULL AUTO_INCREMENT,`uid` int unsigned NOT NULL DEFAULT 0,
  `event_type` varchar(32) NOT NULL,`result` varchar(16) NOT NULL,`ip` varchar(64) NOT NULL DEFAULT '',
  `metadata_json` text NOT NULL,`created_at` int unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`event_id`),KEY `idx_type_created` (`event_type`,`created_at`),KEY `idx_uid_created` (`uid`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `pre_stk_app_config` (`config_key` varchar(64) NOT NULL,`config_value` text NOT NULL,`updated_at` int unsigned NOT NULL DEFAULT 0,PRIMARY KEY (`config_key`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `pre_stk_auth_refresh_token` (`token_hash` char(64) NOT NULL,`uid` int unsigned NOT NULL,`device_id` varchar(128) NOT NULL DEFAULT '',`expires_at` int unsigned NOT NULL DEFAULT 0,`revoked_at` int unsigned NOT NULL DEFAULT 0,`created_at` int unsigned NOT NULL DEFAULT 0,PRIMARY KEY (`token_hash`),KEY `idx_uid_expiry` (`uid`,`expires_at`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `pre_stk_member_status` (`uid` int unsigned NOT NULL,`status` varchar(16) NOT NULL DEFAULT 'inactive',`member_label` varchar(32) NOT NULL DEFAULT '普通用户',`level` varchar(16) NOT NULL DEFAULT 'L1',`expires_at` int unsigned NOT NULL DEFAULT 0,`updated_at` int unsigned NOT NULL DEFAULT 0,PRIMARY KEY (`uid`),KEY `idx_status_expiry` (`status`,`expires_at`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `pre_stk_wallet_account` (`uid` int unsigned NOT NULL,`commission_amount` decimal(12,2) NOT NULL DEFAULT 0,`task_points` int unsigned NOT NULL DEFAULT 0,`updated_at` int unsigned NOT NULL DEFAULT 0,PRIMARY KEY (`uid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `pre_stk_auth_request_guard` (`request_hash` char(64) NOT NULL,`client_key` char(64) NOT NULL,`action` varchar(64) NOT NULL,`created_at` int unsigned NOT NULL DEFAULT 0,PRIMARY KEY (`request_hash`),KEY `idx_client_action_created` (`client_key`,`action`,`created_at`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
SQL;
runquery($sql);

function stk_auth_upgrade_column(string $table, string $column, string $definition): void
{
    $exists = DB::fetch_first('SHOW COLUMNS FROM %t LIKE %s', [$table, $column]);
    if (!$exists) {
        DB::query('ALTER TABLE %t ADD `' . $column . '` ' . $definition, [$table]);
    }
}

stk_auth_upgrade_column('stk_auth_sms_code', 'attempt_count', "tinyint unsigned NOT NULL DEFAULT '0'");
stk_auth_upgrade_column('stk_auth_sms_code', 'code_audit_ciphertext', "text NOT NULL AFTER `code_hash`");
stk_auth_upgrade_column('stk_auth_sms_code', 'client_ip', "varchar(64) NOT NULL DEFAULT ''");
stk_auth_upgrade_column('stk_auth_sms_code', 'provider_request_id', "varchar(64) NOT NULL DEFAULT ''");
stk_auth_upgrade_column('stk_auth_sms_code', 'provider_code', "varchar(32) NOT NULL DEFAULT ''");
stk_auth_upgrade_column('stk_auth_sms_code', 'last_error', "varchar(255) NOT NULL DEFAULT ''");

foreach ([
    'api_enabled' => '1',
    'allow_password_login' => '1',
    'allow_sms_login' => '1',
    'allow_register' => '1',
    'default_login_tab' => 'password',
    'dev_fake_enabled' => '0',
    'access_token_ttl' => '1800',
    'refresh_token_ttl' => '2592000',
    'legal_user_agreement_title' => '用户协议',
    'legal_user_agreement_version' => '1.4.0',
    'legal_privacy_policy_title' => '隐私政策',
    'legal_privacy_policy_version' => '1.4.0',
    'public_base_url' => '',
    'sms_enabled' => '0',
    'sms_access_key_id' => '',
    'sms_access_key_secret' => '',
    'sms_sign_name' => '',
    'sms_template_code' => '',
    'sms_code_length' => '6',
    'sms_expire_minutes' => '5',
    'sms_resend_seconds' => '60',
    'sms_phone_hour_limit' => '5',
    'sms_phone_day_limit' => '10',
    'sms_ip_hour_limit' => '20',
    'password_min_length' => '8',
    'password_max_length' => '64',
    'login_fail_limit' => '5',
    'login_lock_minutes' => '15',
    'captcha_expire_seconds' => '300',
    'captcha_max_attempts' => '5',
    'login_success_route' => 'stk://home',
    'register_success_route' => 'stk://home',
    'legal_user_agreement_url' => '/legal/user-agreement',
    'legal_privacy_url' => '/legal/privacy',
    'sms_retention_days' => '90',
    'login_retention_days' => '180',
    'member_title' => '商推客会员',
    'member_benefit_discount' => '消费 5 折（仅展示）',
    'member_benefit_rebate' => '消费返佣 40%（仅展示）',
    'member_open_button_text' => '了解会员',
    'show_wallets' => '1',
    'show_member_card' => '1',
    'show_props_center' => '1',
    'profile_bio' => '欢迎使用商推客，完善资料有助于项目展示。',
    'commission_label' => '佣金账户',
    'task_label' => '任务账户',
    'support_type' => 'wechat',
    'support_label' => '在线客服',
    'support_value' => '',
    'support_hours' => '工作日 09:00-18:00',
    'support_copy_enabled' => '1',
    'placeholder_message' => '功能筹备中',
] as $key => $value) {
    if (stk_auth_get_config($key, '') === '') {
        stk_auth_set_config($key, $value);
    }
}
DB::query('INSERT INTO %t (config_key,config_value,updated_at) SELECT config_key,config_value,updated_at FROM %t ON DUPLICATE KEY UPDATE config_value=VALUES(config_value),updated_at=VALUES(updated_at)', ['stk_app_config', 'stk_auth_config']);
DB::query("INSERT INTO %t (token_hash,uid,device_id,expires_at,revoked_at,created_at) SELECT token_hash,uid,'',expires_at,revoked_at,created_at FROM %t WHERE token_type='refresh' ON DUPLICATE KEY UPDATE uid=VALUES(uid),expires_at=VALUES(expires_at),revoked_at=VALUES(revoked_at)", ['stk_auth_refresh_token', 'stk_auth_token']);
DB::query("INSERT INTO %t (uid,status,member_label,level,expires_at,updated_at) SELECT uid,CASE WHEN expires_at=0 THEN 'inactive' WHEN expires_at<UNIX_TIMESTAMP() THEN 'expired' ELSE 'active' END,member_label,level,expires_at,updated_at FROM %t ON DUPLICATE KEY UPDATE member_label=VALUES(member_label),level=VALUES(level),expires_at=VALUES(expires_at),updated_at=VALUES(updated_at)", ['stk_member_status', 'stk_account_membership']);
DB::query('INSERT INTO %t (uid,commission_amount,task_points,updated_at) SELECT uid,commission_amount,task_points,updated_at FROM %t ON DUPLICATE KEY UPDATE commission_amount=VALUES(commission_amount),task_points=VALUES(task_points),updated_at=VALUES(updated_at)', ['stk_wallet_account', 'stk_account_balance']);
DB::query('DELETE FROM %t WHERE created_at<%d', ['stk_auth_request_guard', TIMESTAMP - 86400]);
$finish = true;
