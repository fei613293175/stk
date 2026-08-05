-- 商推客 V1.0.0：登录注册、项目只读基础、会员/双账户基础数据
-- 执行器：Discuz 插件 install/upgrade 迁移器；表前缀 pre_ 由 Discuz 替换。
-- 规则：只允许向前迁移；生产环境执行前必须备份；不得把密钥或明文验证码写入 SQL。

CREATE TABLE IF NOT EXISTS pre_stk_auth_mobile (
  uid BIGINT UNSIGNED NOT NULL,
  mobile_hash CHAR(64) NOT NULL,
  mobile_ciphertext TEXT NOT NULL,
  mobile_last4 CHAR(4) NOT NULL,
  country_code VARCHAR(8) NOT NULL DEFAULT '+86',
  verified_at DATETIME NULL,
  status TINYINT UNSIGNED NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (uid),
  UNIQUE KEY uk_mobile_hash (mobile_hash),
  KEY idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_auth_sms_code (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  scene VARCHAR(32) NOT NULL,
  mobile_hash CHAR(64) NOT NULL,
  mobile_ciphertext TEXT NOT NULL,
  code_hash CHAR(64) NOT NULL,
  code_ciphertext TEXT NULL,
  send_status VARCHAR(24) NOT NULL,
  use_status VARCHAR(24) NOT NULL DEFAULT 'unused',
  provider_code VARCHAR(64) NULL,
  provider_message VARCHAR(255) NULL,
  biz_id VARCHAR(128) NULL,
  request_id CHAR(36) NOT NULL,
  request_ip_hash CHAR(64) NOT NULL,
  device_hash CHAR(64) NOT NULL,
  failed_attempts SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  expires_at DATETIME NOT NULL,
  sent_at DATETIME NULL,
  used_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_mobile_scene_created (mobile_hash, scene, created_at),
  KEY idx_biz_id (biz_id),
  KEY idx_use_expiry (use_status, expires_at),
  UNIQUE KEY uk_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_auth_captcha_challenge (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  challenge_id CHAR(36) NOT NULL,
  action VARCHAR(32) NOT NULL,
  answer_hash CHAR(64) NOT NULL,
  session_hash CHAR(64) NOT NULL,
  device_hash CHAR(64) NOT NULL,
  attempts SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  max_attempts SMALLINT UNSIGNED NOT NULL DEFAULT 5,
  ticket_hash CHAR(64) NULL,
  ticket_expires_at DATETIME NULL,
  expires_at DATETIME NOT NULL,
  verified_at DATETIME NULL,
  used_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_challenge_id (challenge_id),
  UNIQUE KEY uk_ticket_hash (ticket_hash),
  KEY idx_expiry (expires_at),
  KEY idx_session_action (session_hash, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_auth_token (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  uid BIGINT UNSIGNED NOT NULL,
  token_family CHAR(36) NOT NULL,
  access_jti_hash CHAR(64) NOT NULL,
  refresh_hash CHAR(64) NOT NULL,
  previous_refresh_hash CHAR(64) NULL,
  device_id_hash CHAR(64) NOT NULL,
  device_name VARCHAR(128) NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'active',
  access_expires_at DATETIME NOT NULL,
  refresh_expires_at DATETIME NOT NULL,
  rotated_at DATETIME NULL,
  revoked_at DATETIME NULL,
  last_seen_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_refresh_hash (refresh_hash),
  UNIQUE KEY uk_access_jti_hash (access_jti_hash),
  KEY idx_uid_status (uid, status),
  KEY idx_family (token_family),
  KEY idx_refresh_expiry (refresh_expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_auth_login_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  uid BIGINT UNSIGNED NULL,
  mobile_hash CHAR(64) NULL,
  login_type VARCHAR(24) NOT NULL,
  result VARCHAR(16) NOT NULL,
  error_code VARCHAR(64) NULL,
  request_id CHAR(36) NOT NULL,
  ip_hash CHAR(64) NOT NULL,
  device_hash CHAR(64) NOT NULL,
  user_agent_digest CHAR(64) NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_login_request (request_id),
  KEY idx_uid_created (uid, created_at),
  KEY idx_mobile_created (mobile_hash, created_at),
  KEY idx_result_created (result, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_auth_risk_event (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  scene VARCHAR(32) NOT NULL,
  subject_type VARCHAR(24) NOT NULL,
  subject_hash CHAR(64) NOT NULL,
  rule_code VARCHAR(64) NOT NULL,
  action VARCHAR(32) NOT NULL,
  metadata_json JSON NULL,
  unlock_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_subject_scene_created (subject_hash, scene, created_at),
  KEY idx_action_unlock (action, unlock_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_secret_config (
  config_key VARCHAR(128) NOT NULL,
  ciphertext MEDIUMTEXT NOT NULL,
  key_version SMALLINT UNSIGNED NOT NULL,
  updated_by BIGINT UNSIGNED NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (config_key),
  KEY idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_admin_audit (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  admin_uid BIGINT UNSIGNED NOT NULL,
  module VARCHAR(48) NOT NULL,
  action VARCHAR(64) NOT NULL,
  target_type VARCHAR(48) NOT NULL,
  target_id VARCHAR(128) NULL,
  before_digest CHAR(64) NULL,
  after_digest CHAR(64) NULL,
  request_id CHAR(36) NOT NULL,
  ip_hash CHAR(64) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_admin_created (admin_uid, created_at),
  KEY idx_target (target_type, target_id),
  KEY idx_action_created (action, created_at),
  UNIQUE KEY uk_admin_request_action (request_id, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_idempotency_key (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  idempotency_key_hash CHAR(64) NOT NULL,
  uid_or_subject VARCHAR(128) NOT NULL,
  operation_id VARCHAR(64) NOT NULL,
  request_digest CHAR(64) NOT NULL,
  response_digest CHAR(64) NULL,
  response_payload MEDIUMTEXT NULL,
  response_status SMALLINT UNSIGNED NULL,
  status VARCHAR(24) NOT NULL,
  expires_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_operation_key (operation_id, idempotency_key_hash),
  KEY idx_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_project_category (
  category_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(48) NOT NULL,
  icon_asset VARCHAR(255) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT UNSIGNED NOT NULL DEFAULT 1,
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (category_id),
  KEY idx_enabled_sort (enabled, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_project (
  project_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  uid BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED NULL,
  title VARCHAR(80) NOT NULL,
  summary TEXT NOT NULL,
  contact_type VARCHAR(24) NOT NULL,
  contact_ciphertext TEXT NOT NULL,
  contact_name VARCHAR(48) NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'pending',
  recommended TINYINT UNSIGNED NOT NULL DEFAULT 0,
  recommendation_weight INT NOT NULL DEFAULT 0,
  row_version INT UNSIGNED NOT NULL DEFAULT 1,
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  cover_asset_id BIGINT UNSIGNED NULL,
  reject_reason VARCHAR(500) NULL,
  publish_agreement_version VARCHAR(32) NULL,
  submitted_at DATETIME NULL,
  published_at DATETIME NULL,
  offline_at DATETIME NULL,
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (project_id),
  KEY idx_feed (status, recommended, published_at, project_id),
  KEY idx_uid_status_updated (uid, status, updated_at),
  KEY idx_category_status_published (category_id, status, published_at),
  KEY idx_updated (updated_at),
  FULLTEXT KEY ft_title_summary (title, summary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_project_view (
  project_id BIGINT UNSIGNED NOT NULL,
  viewer_uid BIGINT UNSIGNED NOT NULL,
  bucket_key VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (project_id, viewer_uid, bucket_key),
  KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_project_view_daily (
  project_id BIGINT UNSIGNED NOT NULL,
  date_key DATE NOT NULL,
  view_count BIGINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (project_id, date_key),
  KEY idx_date (date_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_member_status (
  uid BIGINT UNSIGNED NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'inactive',
  level_code VARCHAR(32) NOT NULL DEFAULT 'none',
  level_name VARCHAR(48) NOT NULL DEFAULT '未开通',
  started_at DATETIME NULL,
  expires_at DATETIME NULL,
  source VARCHAR(32) NOT NULL DEFAULT 'registration',
  updated_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (uid),
  KEY idx_status_expiry (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_wallet_account (
  uid BIGINT UNSIGNED NOT NULL,
  account_type VARCHAR(24) NOT NULL,
  balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  frozen_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  currency CHAR(3) NOT NULL DEFAULT 'CNY',
  row_version INT UNSIGNED NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (uid, account_type),
  KEY idx_type_updated (account_type, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
