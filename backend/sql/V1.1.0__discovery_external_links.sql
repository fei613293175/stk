-- 商推客 V1.1.0：受控外部联系与发现性能
CREATE TABLE IF NOT EXISTS pre_stk_external_link_whitelist (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  target_type VARCHAR(24) NOT NULL,
  host_or_package VARCHAR(255) NOT NULL,
  path_prefix VARCHAR(255) NULL,
  action VARCHAR(48) NOT NULL,
  enabled TINYINT UNSIGNED NOT NULL DEFAULT 1,
  expires_at DATETIME NULL,
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_target_rule (target_type, host_or_package, path_prefix, action),
  KEY idx_target_enabled (target_type, host_or_package, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 首次执行一次；迁移器必须先检查索引是否存在。
ALTER TABLE pre_stk_project ADD KEY idx_latest_cursor (status, published_at, project_id);
ALTER TABLE pre_stk_project ADD KEY idx_recommended_cursor (status, recommended, recommendation_weight, published_at, project_id);
