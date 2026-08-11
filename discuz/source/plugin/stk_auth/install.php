<?php

if (!defined('IN_DISCUZ')) {
    exit('Access Denied');
}

$sql = <<<SQL
CREATE TABLE IF NOT EXISTS `pre_stk_auth_config` (
  `config_key` varchar(64) NOT NULL,
  `config_value` text NOT NULL,
  `updated_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_app_config` (
  `config_key` varchar(64) NOT NULL,
  `config_value` text NOT NULL,
  `updated_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_auth_mobile` (
  `uid` int unsigned NOT NULL,
  `mobile` varchar(20) NOT NULL,
  `verified` tinyint unsigned NOT NULL DEFAULT '0',
  `created_at` int unsigned NOT NULL DEFAULT '0',
  `updated_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uk_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_auth_sms_code` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `mobile` varchar(20) NOT NULL,
  `scene` varchar(32) NOT NULL,
  `code_hash` varchar(255) NOT NULL,
  `code_audit_ciphertext` text NOT NULL,
  `send_status` varchar(24) NOT NULL DEFAULT 'pending',
  `use_status` varchar(24) NOT NULL DEFAULT 'unused',
  `attempt_count` tinyint unsigned NOT NULL DEFAULT '0',
  `client_ip` varchar(64) NOT NULL DEFAULT '',
  `provider_request_id` varchar(64) NOT NULL DEFAULT '',
  `provider_code` varchar(32) NOT NULL DEFAULT '',
  `last_error` varchar(255) NOT NULL DEFAULT '',
  `expires_at` int unsigned NOT NULL DEFAULT '0',
  `used_at` int unsigned NOT NULL DEFAULT '0',
  `created_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_mobile_created` (`mobile`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  `ticket_hash` char(64) NOT NULL,
  `scene` varchar(32) NOT NULL,
  `client_ip` varchar(64) NOT NULL DEFAULT '',
  `status` varchar(16) NOT NULL DEFAULT 'active',
  `expires_at` int unsigned NOT NULL DEFAULT '0',
  `used_at` int unsigned NOT NULL DEFAULT '0',
  `created_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`ticket_hash`),
  KEY `idx_scene_ip_created` (`scene`,`client_ip`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_auth_token` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `uid` int unsigned NOT NULL,
  `token_hash` varchar(255) NOT NULL,
  `token_type` varchar(16) NOT NULL,
  `expires_at` int unsigned NOT NULL DEFAULT '0',
  `revoked_at` int unsigned NOT NULL DEFAULT '0',
  `created_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token_hash` (`token_hash`),
  KEY `idx_uid_type` (`uid`,`token_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_auth_refresh_token` (
  `token_hash` char(64) NOT NULL,
  `uid` int unsigned NOT NULL,
  `device_id` varchar(128) NOT NULL DEFAULT '',
  `expires_at` int unsigned NOT NULL DEFAULT '0',
  `revoked_at` int unsigned NOT NULL DEFAULT '0',
  `created_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`token_hash`),
  KEY `idx_uid_expiry` (`uid`,`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_auth_login_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `uid` int unsigned NOT NULL DEFAULT '0',
  `mobile_masked` varchar(20) NOT NULL DEFAULT '',
  `login_type` varchar(16) NOT NULL,
  `result` varchar(16) NOT NULL,
  `error_code` int NOT NULL DEFAULT '0',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `created_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_uid_created` (`uid`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_security_event` (
  `event_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `uid` int unsigned NOT NULL DEFAULT '0',
  `event_type` varchar(32) NOT NULL,
  `result` varchar(16) NOT NULL,
  `ip` varchar(64) NOT NULL DEFAULT '',
  `metadata_json` text NOT NULL,
  `created_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`event_id`),
  KEY `idx_type_created` (`event_type`,`created_at`),
  KEY `idx_uid_created` (`uid`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_auth_request_guard` (
  `request_hash` char(64) NOT NULL,
  `client_key` char(64) NOT NULL,
  `action` varchar(64) NOT NULL,
  `created_at` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`request_hash`),
  KEY `idx_client_action_created` (`client_key`,`action`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pre_stk_account_membership` (`uid` int unsigned NOT NULL,`member_label` varchar(32) NOT NULL DEFAULT '普通用户',`level` varchar(16) NOT NULL DEFAULT 'L1',`expires_at` int unsigned NOT NULL DEFAULT 0,`updated_at` int unsigned NOT NULL DEFAULT 0,PRIMARY KEY (`uid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `pre_stk_account_balance` (`uid` int unsigned NOT NULL,`commission_amount` decimal(12,2) NOT NULL DEFAULT 0,`task_points` int unsigned NOT NULL DEFAULT 0,`updated_at` int unsigned NOT NULL DEFAULT 0,PRIMARY KEY (`uid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `pre_stk_account_prop` (`prop_id` bigint unsigned NOT NULL AUTO_INCREMENT,`uid` int unsigned NOT NULL,`prop_name` varchar(64) NOT NULL,`description` varchar(255) NOT NULL DEFAULT '',`quantity` int unsigned NOT NULL DEFAULT 0,`updated_at` int unsigned NOT NULL DEFAULT 0,PRIMARY KEY (`prop_id`),KEY `idx_uid` (`uid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `pre_stk_member_status` (`uid` int unsigned NOT NULL,`status` varchar(16) NOT NULL DEFAULT 'inactive',`member_label` varchar(32) NOT NULL DEFAULT '普通用户',`level` varchar(16) NOT NULL DEFAULT 'L1',`starts_at` int unsigned NOT NULL DEFAULT 0,`expires_at` int unsigned NOT NULL DEFAULT 0,`updated_at` int unsigned NOT NULL DEFAULT 0,PRIMARY KEY (`uid`),KEY `idx_status_expiry` (`status`,`expires_at`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `pre_stk_wallet_account` (`uid` int unsigned NOT NULL,`commission_amount` decimal(12,2) NOT NULL DEFAULT 0,`task_amount` decimal(12,2) NOT NULL DEFAULT 0,`task_points` int unsigned NOT NULL DEFAULT 0,`updated_at` int unsigned NOT NULL DEFAULT 0,PRIMARY KEY (`uid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `pre_stk_auth_config` (`config_key`,`config_value`,`updated_at`) VALUES
('api_enabled','1',UNIX_TIMESTAMP()),
('allow_password_login','1',UNIX_TIMESTAMP()),
('allow_sms_login','1',UNIX_TIMESTAMP()),
('allow_register','1',UNIX_TIMESTAMP()),
('default_login_tab','password',UNIX_TIMESTAMP()),
('dev_fake_enabled','0',UNIX_TIMESTAMP()),
('sms_enabled','0',UNIX_TIMESTAMP()),
('sms_access_key_id','',UNIX_TIMESTAMP()),
('sms_access_key_secret','',UNIX_TIMESTAMP()),
('sms_sign_name','',UNIX_TIMESTAMP()),
('sms_template_code','',UNIX_TIMESTAMP()),
('sms_code_length','6',UNIX_TIMESTAMP()),
('sms_expire_minutes','5',UNIX_TIMESTAMP()),
('sms_resend_seconds','60',UNIX_TIMESTAMP()),
('sms_phone_hour_limit','5',UNIX_TIMESTAMP()),
('sms_phone_day_limit','10',UNIX_TIMESTAMP()),
('sms_ip_hour_limit','20',UNIX_TIMESTAMP()),
('password_min_length','8',UNIX_TIMESTAMP()),
('password_max_length','64',UNIX_TIMESTAMP()),
('login_fail_limit','5',UNIX_TIMESTAMP()),
('login_lock_minutes','15',UNIX_TIMESTAMP()),
('captcha_expire_seconds','300',UNIX_TIMESTAMP()),
('captcha_max_attempts','5',UNIX_TIMESTAMP()),
('access_token_ttl','1800',UNIX_TIMESTAMP()),
('refresh_token_ttl','2592000',UNIX_TIMESTAMP()),
('login_success_route','stk://home',UNIX_TIMESTAMP()),
('register_success_route','stk://home',UNIX_TIMESTAMP()),
('legal_user_agreement_url','/legal/user-agreement',UNIX_TIMESTAMP()),
('legal_privacy_url','/legal/privacy',UNIX_TIMESTAMP()),
('sms_retention_days','90',UNIX_TIMESTAMP()),
('login_retention_days','180',UNIX_TIMESTAMP()),
('public_base_url','',UNIX_TIMESTAMP()),
('legal_user_agreement_title','用户协议',UNIX_TIMESTAMP()),
('legal_user_agreement_version','1.4.0',UNIX_TIMESTAMP()),
('legal_privacy_policy_title','隐私政策',UNIX_TIMESTAMP()),
('legal_privacy_policy_version','1.4.0',UNIX_TIMESTAMP()),
('member_title','商推客会员',UNIX_TIMESTAMP()),
('member_benefit_discount','消费 5 折（仅展示）',UNIX_TIMESTAMP()),
('member_benefit_rebate','消费返佣 40%（仅展示）',UNIX_TIMESTAMP()),
('member_open_button_text','了解会员',UNIX_TIMESTAMP()),
('show_wallets','1',UNIX_TIMESTAMP()),
('show_member_card','1',UNIX_TIMESTAMP()),
('show_props_center','1',UNIX_TIMESTAMP()),
('profile_bio','欢迎使用商推客，完善资料有助于项目展示。',UNIX_TIMESTAMP()),
('commission_label','佣金账户',UNIX_TIMESTAMP()),
('task_label','任务账户',UNIX_TIMESTAMP()),
('support_type','wechat',UNIX_TIMESTAMP()),
('support_label','在线客服',UNIX_TIMESTAMP()),
('support_value','',UNIX_TIMESTAMP()),
('support_hours','工作日 09:00-18:00',UNIX_TIMESTAMP()),
('support_copy_enabled','1',UNIX_TIMESTAMP()),
('placeholder_message','功能筹备中',UNIX_TIMESTAMP())
ON DUPLICATE KEY UPDATE `config_key`=VALUES(`config_key`);
INSERT INTO `pre_stk_app_config` (`config_key`,`config_value`,`updated_at`) SELECT `config_key`,`config_value`,`updated_at` FROM `pre_stk_auth_config` ON DUPLICATE KEY UPDATE `config_value`=VALUES(`config_value`),`updated_at`=VALUES(`updated_at`);
SQL;

runquery($sql);
$finish = true;
