-- 商推客 V1.2.0：多图上传、项目图片关系、审核闭环
CREATE TABLE IF NOT EXISTS pre_stk_upload_asset (
  asset_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  uid BIGINT UNSIGNED NOT NULL,
  upload_session_id CHAR(36) NOT NULL,
  storage_key VARCHAR(512) NOT NULL,
  thumbnail_key VARCHAR(512) NOT NULL,
  mime VARCHAR(64) NOT NULL,
  size_bytes BIGINT UNSIGNED NOT NULL,
  width INT UNSIGNED NOT NULL,
  height INT UNSIGNED NOT NULL,
  sha256 CHAR(64) NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'uploaded',
  attached_project_id BIGINT UNSIGNED NULL,
  sort_order SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  attached_at DATETIME NULL,
  deleted_at DATETIME NULL,
  PRIMARY KEY (asset_id),
  UNIQUE KEY uk_storage_key (storage_key),
  KEY idx_uid_status_created (uid, status, created_at),
  KEY idx_upload_session (upload_session_id),
  KEY idx_attached_project_sort (attached_project_id, sort_order),
  KEY idx_sha256 (sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_project_image (
  project_id BIGINT UNSIGNED NOT NULL,
  asset_id BIGINT UNSIGNED NOT NULL,
  sort_order SMALLINT UNSIGNED NOT NULL,
  is_cover TINYINT UNSIGNED NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (project_id, asset_id),
  UNIQUE KEY uk_project_sort (project_id, sort_order),
  KEY idx_project_cover (project_id, is_cover)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pre_stk_project_audit_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  project_id BIGINT UNSIGNED NOT NULL,
  actor_type VARCHAR(24) NOT NULL,
  actor_uid BIGINT UNSIGNED NOT NULL,
  action VARCHAR(48) NOT NULL,
  from_status VARCHAR(24) NULL,
  to_status VARCHAR(24) NULL,
  reason VARCHAR(500) NULL,
  snapshot_digest CHAR(64) NOT NULL,
  request_id CHAR(36) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_project_created (project_id, created_at),
  KEY idx_actor_created (actor_uid, created_at),
  KEY idx_action_created (action, created_at),
  UNIQUE KEY uk_audit_request_action (request_id, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
