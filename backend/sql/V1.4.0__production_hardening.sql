-- 商推客 V1.4.0：生产性能索引与发布查询加固。
-- 迁移器执行前必须逐项检查索引是否已存在；不得在高峰期直接锁大表。
ALTER TABLE pre_stk_auth_token ADD KEY idx_active_refresh_expiry (status, refresh_expires_at);
ALTER TABLE pre_stk_auth_sms_code ADD KEY idx_scene_send_created (scene, send_status, created_at);
ALTER TABLE pre_stk_project ADD KEY idx_owner_deleted_updated (uid, deleted_at, updated_at);
ALTER TABLE pre_stk_app_release ADD KEY idx_min_supported (application_id, min_supported_code, status);
