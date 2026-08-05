-- 商推客 V1.3.0：道具展示目录与 App 发布记录；不包含购买、库存、支付或资金流水。
CREATE TABLE IF NOT EXISTS pre_stk_prop_catalog (
  prop_code VARCHAR(32) NOT NULL,
  name VARCHAR(48) NOT NULL,
  icon_asset VARCHAR(255) NULL,
  description VARCHAR(255) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT UNSIGNED NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (prop_code),
  KEY idx_enabled_sort (enabled, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_app_release (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  application_id VARCHAR(128) NOT NULL,
  version_name VARCHAR(32) NOT NULL,
  version_code INT UNSIGNED NOT NULL,
  min_supported_code INT UNSIGNED NOT NULL,
  update_type VARCHAR(24) NOT NULL DEFAULT 'optional',
  apk_url VARCHAR(512) NOT NULL,
  apk_sha256 CHAR(64) NOT NULL,
  size_bytes BIGINT UNSIGNED NOT NULL,
  release_notes TEXT NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'draft',
  published_by BIGINT UNSIGNED NULL,
  published_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_version_code (version_code),
  KEY idx_app_status_version (application_id, status, version_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO pre_stk_prop_catalog
  (prop_code, name, icon_asset, description, sort_order, enabled, created_at, updated_at)
VALUES
  ('refresh_card', '刷新卡', NULL, '本期仅展示，购买与使用功能尚未开放', 10, 1, NOW(), NOW()),
  ('super_headline', '超级头条', NULL, '本期仅展示，购买与使用功能尚未开放', 20, 1, NOW(), NOW()),
  ('headline', '头条', NULL, '本期仅展示，购买与使用功能尚未开放', 30, 1, NOW(), NOW()),
  ('color_card', '变色卡', NULL, '本期仅展示，购买与使用功能尚未开放', 40, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name), description = VALUES(description), sort_order = VALUES(sort_order), updated_at = VALUES(updated_at);
