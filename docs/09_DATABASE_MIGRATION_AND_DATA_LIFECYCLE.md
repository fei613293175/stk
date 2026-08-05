# 09 数据库、迁移与数据生命周期

## 1. 原则

- 使用 Discuz 实际表前缀，文档以 `pre_` 表示。
- 插件安装、升级和卸载脚本独立；不得手工改生产表。
- 迁移只向前、可重复检查、在事务或安全补偿范围执行；每版保存 schema snapshot。
- 不复制 Discuz 完整用户记录，只保存 UID 关联和插件专属字段。
- 项目和关联数据默认软删除；敏感密文按保留策略清理。

## 2. 实体目录

| 实体ID | 表 | 版本 | 用途 | 关键字段 | 索引 | 隐私 |
|---|---|---|---|---|---|---|
| DB-AUTH-001 | pre_stk_auth_mobile | V1.0.0 | Discuz UID 与手机号安全绑定 | uid PK; mobile_hash UNIQUE; mobile_ciphertext; mobile_last4; verified_at; status | UNIQUE mobile_hash; status+created_at | mobile_ciphertext encrypted; hash keyed HMAC |
| DB-AUTH-002 | pre_stk_auth_sms_code | V1.0.0 | 短信发送、验证码使用与阿里云回执 | id; scene; mobile_hash; code_hash; code_ciphertext; send_status; use_status; expires_at; biz_id | mobile_hash+scene+created_at; biz_id; use_status+expires_at | mobile/code encrypted; admin reveal audited |
| DB-AUTH-003 | pre_stk_auth_captcha_challenge | V1.0.0 | 动作绑定安全验证码 challenge/ticket | challenge_id; action; answer_hash; session_hash; device_hash; expires_at; attempts; used_at | challenge_id UNIQUE; expires_at; session_hash+action | 不保存明文答案 |
| DB-AUTH-004 | pre_stk_auth_token | V1.0.0 | Access/Refresh token 族、轮换与撤销 | id; uid; token_family; access_jti_hash; refresh_hash; device_id_hash; expires_at; status | refresh_hash UNIQUE; uid+status; token_family | 仅哈希，不保存明文 token |
| DB-AUTH-005 | pre_stk_auth_login_log | V1.0.0 | 登录成功失败日志 | id; uid; mobile_hash; login_type; result; error_code; ip_hash; device_hash; created_at | uid+created_at; mobile_hash+created_at; result+created_at | 手机号/IP/设备只存安全摘要和脱敏展示 |
| DB-AUTH-006 | pre_stk_auth_risk_event | V1.0.0 | 限流、锁定和异常事件 | id; scene; subject_type; subject_hash; rule_code; action; unlock_at; created_at | subject_hash+scene+created_at; action+unlock_at | 主体值只存哈希 |
| DB-AUTH-007 | pre_stk_secret_config | V1.0.0 | 阿里云 Secret 等敏感配置密文 | config_key PK; ciphertext; key_version; updated_by; updated_at | updated_at | 主密钥只存在服务器环境变量 |
| DB-AUTH-008 | pre_stk_admin_audit | V1.0.0 | 后台敏感操作不可抵赖审计 | id; admin_uid; module; action; target_type; target_id; before_digest; after_digest; ip_hash; created_at | admin_uid+created_at; target_type+target_id; action+created_at | 不记录 Secret 明文 |
| DB-PROJ-001 | pre_stk_project_category | V1.0.0 | 项目分类、排序和可见性 | category_id; name; icon_asset; sort_order; enabled | enabled+sort_order | 无 |
| DB-PROJ-002 | pre_stk_project | V1.0.0 | 项目主体、状态机、联系信息和统计 | project_id; uid; category_id; title; summary; contact_type; contact_ciphertext; status; version; view_count; cover_asset_id | status+recommended+published_at+project_id; uid+status+updated_at; category_id+status+published_at; FULLTEXT title+summary where supported | 联系方式加密，日志不得输出明文 |
| DB-PROJ-003 | pre_stk_upload_asset | V1.2.0 | 上传会话、图片元数据和生命周期 | asset_id; uid; upload_session_id; storage_key; mime; size; width; height; sha256; status; attached_project_id | uid+status+created_at; upload_session_id; attached_project_id+sort_order; sha256 | 图片 EXIF 在服务端移除 |
| DB-PROJ-004 | pre_stk_project_image | V1.2.0 | 项目与图片关联、顺序和封面 | project_id+asset_id; sort_order; is_cover | project_id+sort_order UNIQUE; project_id+is_cover | 无 |
| DB-PROJ-005 | pre_stk_project_audit_log | V1.2.0 | 项目审核、驳回、上下架与用户状态动作 | id; project_id; actor_type; actor_uid; action; from_status; to_status; reason; snapshot_digest; created_at | project_id+created_at; actor_uid+created_at; action+created_at | reason 过滤敏感内容 |
| DB-PROJ-006 | pre_stk_project_view | V1.0.0 | 短时浏览去重 | project_id; viewer_uid; bucket_key; created_at | project_id+viewer_uid+bucket_key UNIQUE; created_at | 只使用 UID |
| DB-PROJ-007 | pre_stk_project_view_daily | V1.0.0 | 项目日浏览统计 | project_id+date_key; view_count | date_key; project_id+date_key UNIQUE | 无 |
| DB-ME-001 | pre_stk_member_status | V1.0.0 | 会员展示状态与有效期 | uid PK; status; level_code; level_name; started_at; expires_at; source; updated_by | status+expires_at | 无 |
| DB-ME-002 | pre_stk_wallet_account | V1.0.0 | 佣金/任务账户只读余额基础记录 | uid+account_type PK; balance DECIMAL(18,2); frozen_balance; currency; row_version | account_type+updated_at | 金融展示数据；本期无写入业务 |
| DB-ME-003 | pre_stk_prop_catalog | V1.3.0 | 道具展示目录，不代表库存或可购买 | prop_code PK; name; icon_asset; description; sort_order; enabled | enabled+sort_order | 无 |
| DB-SYS-001 | pre_stk_external_link_whitelist | V1.1.0 | 外部域名、Scheme、包名白名单 | id; target_type; host_or_package; path_prefix; action; enabled; expires_at | target_type+host_or_package+enabled | 无 |
| DB-SYS-002 | pre_stk_app_release | V1.3.0 | App 版本、最低版本、APK 校验和与发布状态 | id; application_id; version_name; version_code; min_supported_code; apk_url; apk_sha256; size_bytes; release_notes; status | application_id+status+version_code; version_code UNIQUE | 无 |
| DB-SYS-003 | pre_stk_idempotency_key | V1.0.0 | 写操作幂等去重 | idempotency_key_hash; uid_or_subject; operation_id; request_digest; response_digest; status; expires_at | operation_id+idempotency_key_hash UNIQUE; expires_at | 不保存请求明文 |

## 3. 版本迁移

- V1.0.0：认证、分类、项目、浏览、会员、双账户、幂等和审计基础表。
- V1.1.0：外链白名单及搜索/缓存相关索引。
- V1.2.0：上传资产、项目图片关联、审核状态完整字段和索引。
- V1.3.0：道具目录、App Release 元数据和会员运营字段。
- V1.4.0：不新增无必要业务表，只做索引、保留策略和生产诊断完善。

## 4. 覆盖更新测试

Android Room 数据库每个 versionCode 导出 schema JSON。V1.1.0 起，CI 安装上一版本 APK，写入登录态/首页缓存/草稿测试数据，再 `adb install -r` 当前版，验证 Room Migration 不丢数据。服务端插件升级在测试数据库执行从上一版到当前版的真实迁移和回滚演练。
