# 1.0.0 后端、数据库、后台和配置范围

## API
| operationId | 方法 | 路径 | 插件 | 鉴权 | 请求 | 成功 |
|---|---|---|---|---|---|---|
| getBootstrap | GET | /v1/bootstrap | stk_auth | OPTIONAL_BEARER | headers: app_version,version_code,device_id,locale | maintenance,auth_state,feature_flags,legal_versions,external_link_policy,release_policy |
| getCaptchaChallenge | POST | /v1/auth/captcha/challenges | stk_auth | ANONYMOUS | action,password_login\|sms_send\|sms_login\|register\|password_reset; device_id | challenge_id,image_base64_or_url,expires_in |
| verifyCaptcha | POST | /v1/auth/captcha/verify | stk_auth | ANONYMOUS | challenge_id,answer,action,device_id | captcha_ticket,ticket_expires_in |
| sendSmsCode | POST | /v1/auth/sms/send | stk_auth | ANONYMOUS | mobile,scene=login\|password_reset,captcha_ticket,device_id | request_id,resend_after,expires_in,masked_mobile |
| loginWithPassword | POST | /v1/auth/login/password | stk_auth | ANONYMOUS | mobile,password,captcha_ticket,device_id,device_name | access_token,access_expires_in,refresh_token,refresh_expires_in,user,next_route |
| loginWithSms | POST | /v1/auth/login/sms | stk_auth | ANONYMOUS | mobile,sms_code,captcha_ticket,device_id,device_name | access_token,access_expires_in,refresh_token,refresh_expires_in,user,next_route |
| registerUser | POST | /v1/auth/register | stk_auth | ANONYMOUS | mobile,password,password_confirmation,agreement_version,privacy_version,captcha_ticket,device_id | uid,username,display_name,masked_mobile,mobile_verified=false,tokens,next_route |
| resetPassword | POST | /v1/auth/password/reset | stk_auth | ANONYMOUS | mobile,sms_code,new_password,password_confirmation,captcha_ticket,device_id | reset=true,next_route |
| refreshToken | POST | /v1/auth/token/refresh | stk_auth | REFRESH_TOKEN | refresh_token,device_id | rotated access_token and refresh_token |
| logout | POST | /v1/auth/logout | stk_auth | BEARER | refresh_token_family,device_id | revoked=true |
| getMySummary | GET | /v1/me/summary | stk_auth | BEARER | none | uid,display_name,avatar_url,masked_mobile,mobile_verified,member_summary,wallet_summary |
| getLegalDocument | GET | /v1/legal/{document_type} | stk_auth | ANONYMOUS | document_type=user_agreement\|privacy_policy | document_type,version,effective_at,title,content_html_sanitized,content_text,sha256 |
| listProjectCategories | GET | /v1/project/categories | stk_project | BEARER | include_all=true | categories[id,name,icon_url,sort_order] |
| listProjects | GET | /v1/projects | stk_project | BEARER | cursor,limit<=30,keyword,category_id,sort=latest\|recommended | items, next_cursor, has_more, applied_filters |
| getProjectDetail | GET | /v1/projects/{project_id} | stk_project | BEARER | project_id | project full detail, images, publisher, contact capability, owner_actions |
| recordProjectView | POST | /v1/projects/{project_id}/views | stk_project | BEARER | project_id,view_session_id | counted,view_count |
| healthLive | GET | /healthz | platform | INTERNAL_OR_PUBLIC_MINIMAL | none | status=ok,service,build |
| healthReady | GET | /readyz | platform | INTERNAL | none | database,discuz,storage,sms_config |

## 数据实体/迁移
| 实体ID | 表 | 用途 | 关键字段 | 索引 |
|---|---|---|---|---|
| DB-AUTH-001 | pre_stk_auth_mobile | Discuz UID 与手机号安全绑定 | uid PK; mobile_hash UNIQUE; mobile_ciphertext; mobile_last4; verified_at; status | UNIQUE mobile_hash; status+created_at |
| DB-AUTH-002 | pre_stk_auth_sms_code | 短信发送、验证码使用与阿里云回执 | id; scene; mobile_hash; code_hash; code_ciphertext; send_status; use_status; expires_at; biz_id | mobile_hash+scene+created_at; biz_id; use_status+expires_at |
| DB-AUTH-003 | pre_stk_auth_captcha_challenge | 动作绑定安全验证码 challenge/ticket | challenge_id; action; answer_hash; session_hash; device_hash; expires_at; attempts; used_at | challenge_id UNIQUE; expires_at; session_hash+action |
| DB-AUTH-004 | pre_stk_auth_token | Access/Refresh token 族、轮换与撤销 | id; uid; token_family; access_jti_hash; refresh_hash; device_id_hash; expires_at; status | refresh_hash UNIQUE; uid+status; token_family |
| DB-AUTH-005 | pre_stk_auth_login_log | 登录成功失败日志 | id; uid; mobile_hash; login_type; result; error_code; ip_hash; device_hash; created_at | uid+created_at; mobile_hash+created_at; result+created_at |
| DB-AUTH-006 | pre_stk_auth_risk_event | 限流、锁定和异常事件 | id; scene; subject_type; subject_hash; rule_code; action; unlock_at; created_at | subject_hash+scene+created_at; action+unlock_at |
| DB-AUTH-007 | pre_stk_secret_config | 阿里云 Secret 等敏感配置密文 | config_key PK; ciphertext; key_version; updated_by; updated_at | updated_at |
| DB-AUTH-008 | pre_stk_admin_audit | 后台敏感操作不可抵赖审计 | id; admin_uid; module; action; target_type; target_id; before_digest; after_digest; ip_hash; created_at | admin_uid+created_at; target_type+target_id; action+created_at |
| DB-PROJ-001 | pre_stk_project_category | 项目分类、排序和可见性 | category_id; name; icon_asset; sort_order; enabled | enabled+sort_order |
| DB-PROJ-002 | pre_stk_project | 项目主体、状态机、联系信息和统计 | project_id; uid; category_id; title; summary; contact_type; contact_ciphertext; status; version; view_count; cover_asset_id | status+recommended+published_at+project_id; uid+status+updated_at; category_id+status+published_at; FULLTEXT title+summary where supported |
| DB-PROJ-006 | pre_stk_project_view | 短时浏览去重 | project_id; viewer_uid; bucket_key; created_at | project_id+viewer_uid+bucket_key UNIQUE; created_at |
| DB-PROJ-007 | pre_stk_project_view_daily | 项目日浏览统计 | project_id+date_key; view_count | date_key; project_id+date_key UNIQUE |
| DB-ME-001 | pre_stk_member_status | 会员展示状态与有效期 | uid PK; status; level_code; level_name; started_at; expires_at; source; updated_by | status+expires_at |
| DB-ME-002 | pre_stk_wallet_account | 佣金/任务账户只读余额基础记录 | uid+account_type PK; balance DECIMAL(18,2); frozen_balance; currency; row_version | account_type+updated_at |
| DB-SYS-003 | pre_stk_idempotency_key | 写操作幂等去重 | idempotency_key_hash; uid_or_subject; operation_id; request_digest; response_digest; status; expires_at | operation_id+idempotency_key_hash UNIQUE; expires_at |

## 后台页面
| 后台ID | 页面 | 能力 | 角色 |
|---|---|---|---|
| ADM-AUTH-001 | 登录注册概览 | 今日注册/登录/短信/失败/风控摘要 | 超级管理员\|认证运营 |
| ADM-AUTH-002 | 基础与跳转设置 | 插件开关、登录/注册/退出跳转、协议入口 | 超级管理员 |
| ADM-AUTH-003 | 登录策略 | 登录方式、Token、失败锁定和设备策略 | 超级管理员 |
| ADM-AUTH-004 | 注册策略 | 注册开关、密码规则、用户名/昵称规则 | 超级管理员 |
| ADM-AUTH-005 | 安全验证码设置 | 类型、长度、有效期、错误上限、动作绑定 | 超级管理员 |
| ADM-AUTH-006 | 阿里云短信设置 | RAM AccessKey、签名、模板、发送策略、测试 | 超级管理员 |
| ADM-AUTH-007 | 短信验证码记录 | 日期、验证码授权查看、发送/使用状态、BizId | 超级管理员\|短信审计员 |
| ADM-AUTH-008 | 注册用户列表 | UID、手机号、日期、验证状态、最近登录 | 超级管理员\|用户运营 |
| ADM-AUTH-009 | 登录日志 | 方式、结果、错误码、IP/设备摘要 | 超级管理员\|安全审计员 |
| ADM-AUTH-010 | Token 与设备会话 | 活跃 Token 族、设备、撤销 | 超级管理员\|安全审计员 |
| ADM-AUTH-011 | 风控与限流事件 | 手机号/IP/设备摘要、规则、锁定和解锁 | 超级管理员\|安全审计员 |
| ADM-AUTH-012 | 协议与隐私版本 | 正文版本、生效日期、发布和历史 | 超级管理员\|内容管理员 |
| ADM-AUTH-013 | 管理操作审计 | 验证码查看、手机号修改、Secret 更新等 | 超级管理员\|安全审计员 |
| ADM-AUTH-014 | 认证系统诊断 | 数据库、短信、加密主密钥、计划任务和 API 健康 | 超级管理员 |
| ADM-PROJ-001 | 项目运营概览 | 项目状态、发布者、浏览和审核摘要 | 超级管理员\|项目运营 |
| ADM-PROJ-002 | 项目管理 | 查询、查看、创建测试数据、编辑、推荐、上下架、删除 | 超级管理员\|项目运营 |
| ADM-PROJ-003 | 项目分类 | 分类增改、排序、启停 | 超级管理员\|项目运营 |
| ADM-PROJ-004 | 首页展示设置 | 每页数量、默认排序、显示字段、空状态文案 | 超级管理员\|项目运营 |
| ADM-PROJ-005 | 项目详情设置 | 发布者字段、联系方式展示和分享规则 | 超级管理员\|项目运营 |

## 后台配置
| 配置ID | Key | 名称 | 默认 | 校验 | 后台 |
|---|---|---|---|---|---|
| CFG-AUTH-001 | auth.enabled | 登录注册插件开关 | true | true\|false | ADM-AUTH-002 |
| CFG-AUTH-002 | auth.force_login | 用户前端强制登录 | true | 必须保持 true，除维护或紧急恢复外不得关闭 | ADM-AUTH-002 |
| CFG-AUTH-003 | auth.login_success_route | 登录成功默认路由 | stk://home | 仅允许已登记 stk:// 路由 | ADM-AUTH-002 |
| CFG-AUTH-004 | auth.register_success_route | 注册成功默认路由 | stk://home | 仅允许已登记 stk:// 路由 | ADM-AUTH-002 |
| CFG-AUTH-005 | auth.logout_route | 退出后路由 | stk://auth/login | 仅允许登录相关路由 | ADM-AUTH-002 |
| CFG-AUTH-006 | auth.default_login_mode | 默认登录方式 | password | password\|sms | ADM-AUTH-002 |
| CFG-AUTH-007 | auth.registration_enabled | 开放注册 | true | true\|false | ADM-AUTH-004 |
| CFG-AUTH-008 | auth.internal_username_prefix | 内部用户名规则前缀 | stk_ | ^[a-z][a-z0-9_]{1,12}$ | ADM-AUTH-004 |
| CFG-AUTH-009 | auth.display_name_prefix | 默认显示昵称前缀 | 商推客用户 | 1-12 个中文/字母/数字 | ADM-AUTH-004 |
| CFG-AUTH-010 | auth.mobile_country_code | 默认国家码 | +86 | 当前版本固定 +86 | ADM-AUTH-004 |
| CFG-AUTH-011 | auth.password_min_length | 密码最短长度 | 8 | 8..32 | ADM-AUTH-003 |
| CFG-AUTH-012 | auth.password_max_length | 密码最长长度 | 32 | 16..64 且不小于最短长度 | ADM-AUTH-003 |
| CFG-AUTH-013 | auth.password_category_min | 密码字符类别最少数 | 2 | 1..4；数字/小写/大写/符号 | ADM-AUTH-003 |
| CFG-AUTH-014 | auth.login_failure_limit | 连续登录失败上限 | 5 | 3..10 | ADM-AUTH-003 |
| CFG-AUTH-015 | auth.login_lock_seconds | 登录锁定秒数 | 900 | 60..86400 | ADM-AUTH-003 |
| CFG-AUTH-016 | auth.access_token_ttl_seconds | Access Token 有效期 | 7200 | 900..14400 | ADM-AUTH-003 |
| CFG-AUTH-017 | auth.refresh_token_ttl_seconds | Refresh Token 有效期 | 2592000 | 86400..7776000 | ADM-AUTH-003 |
| CFG-AUTH-018 | auth.max_active_devices | 每账号活跃设备上限 | 5 | 1..20 | ADM-AUTH-003 |
| CFG-AUTH-019 | auth.refresh_rotation_enabled | 刷新令牌轮换 | true | 必须为 true | ADM-AUTH-003 |
| CFG-AUTH-020 | auth.revoke_on_password_reset | 重置密码撤销全部令牌 | true | 必须为 true | ADM-AUTH-003 |
| CFG-CAP-001 | captcha.enabled | 安全验证码开关 | true | 必须为 true | ADM-AUTH-005 |
| CFG-CAP-002 | captcha.type | 验证码类型 | alphanumeric_image | alphanumeric_image\|arithmetic_image | ADM-AUTH-005 |
| CFG-CAP-003 | captcha.length | 验证码长度 | 4 | 4..6 | ADM-AUTH-005 |
| CFG-CAP-004 | captcha.challenge_ttl_seconds | Challenge 有效期 | 120 | 60..300 | ADM-AUTH-005 |
| CFG-CAP-005 | captcha.ticket_ttl_seconds | Ticket 有效期 | 90 | 30..180 | ADM-AUTH-005 |
| CFG-CAP-006 | captcha.max_attempts | 单 Challenge 最大错误次数 | 5 | 2..10 | ADM-AUTH-005 |
| CFG-CAP-007 | captcha.refresh_interval_seconds | 刷新最短间隔 | 3 | 2..30 | ADM-AUTH-005 |
| CFG-CAP-008 | captcha.required_actions | 强制动作集合 | password_login,sms_send,sms_login,register,password_reset | 必须包含五个既定动作 | ADM-AUTH-005 |
| CFG-SMS-001 | sms.enabled | 短信功能开关 | true | true\|false；关闭时短信登录和找回密码入口禁用 | ADM-AUTH-006 |
| CFG-SMS-002 | sms.aliyun_access_key_id | 阿里云 RAM AccessKey ID | 未配置 | 非空且由独立 RAM 用户提供 | ADM-AUTH-006 |
| CFG-SMS-003 | sms.aliyun_access_key_secret | 阿里云 RAM AccessKey Secret | 未配置 | 非空；保存后永不完整回显 | ADM-AUTH-006 |
| CFG-SMS-004 | sms.aliyun_region | 阿里云短信地域 | cn-hangzhou | 允许阿里云短信支持的 Region | ADM-AUTH-006 |
| CFG-SMS-005 | sms.sign_name | 短信签名 | 未配置 | 必须为阿里云已审核签名 | ADM-AUTH-006 |
| CFG-SMS-006 | sms.login_template_code | 登录验证码模板 | 未配置 | ^SMS_[0-9]+$ | ADM-AUTH-006 |
| CFG-SMS-007 | sms.reset_template_code | 重置密码模板 | 未配置 | ^SMS_[0-9]+$ | ADM-AUTH-006 |
| CFG-SMS-008 | sms.template_code_variable | 验证码变量名 | code | ^[a-zA-Z][a-zA-Z0-9_]{0,31}$ | ADM-AUTH-006 |
| CFG-SMS-009 | sms.code_digits | 短信验证码位数 | 6 | 4..8 | ADM-AUTH-006 |
| CFG-SMS-010 | sms.code_ttl_seconds | 短信验证码有效期 | 300 | 60..900 | ADM-AUTH-006 |
| CFG-SMS-011 | sms.resend_interval_seconds | 重发间隔 | 60 | 30..300 | ADM-AUTH-006 |
| CFG-SMS-012 | sms.phone_hour_limit | 单手机号每小时上限 | 5 | 1..20 | ADM-AUTH-006 |
| CFG-SMS-013 | sms.phone_day_limit | 单手机号每日上限 | 10 | 1..50 | ADM-AUTH-006 |
| CFG-SMS-014 | sms.ip_hour_limit | 单 IP 每小时上限 | 20 | 5..200 | ADM-AUTH-006 |
| CFG-SMS-015 | sms.device_hour_limit | 单设备每小时上限 | 10 | 3..100 | ADM-AUTH-006 |
| CFG-SMS-016 | sms.provider_timeout_seconds | 阿里云请求超时 | 5 | 2..15 | ADM-AUTH-006 |
| CFG-SMS-017 | sms.receipt_query_enabled | 短信回执查询 | true | true\|false | ADM-AUTH-006 |
| CFG-SMS-018 | sms.test_mobile | 后台测试手机号 | 未配置 | 中国大陆手机号；仅测试时使用 | ADM-AUTH-006 |
| CFG-SMS-019 | sms.code_reveal_roles | 验证码明文查看角色 | super_admin,sms_auditor | 不得包含普通运营角色 | ADM-AUTH-007 |
| CFG-SMS-020 | sms.code_ciphertext_retention_days | 验证码密文保留天数 | 90 | 7..180 | ADM-AUTH-007 |
| CFG-LEGAL-001 | legal.agreement_current_version | 用户协议当前版本 | 1.0 | ^[0-9]+(\.[0-9]+){0,2}$ | ADM-AUTH-012 |
| CFG-LEGAL-002 | legal.privacy_current_version | 隐私政策当前版本 | 1.0 | ^[0-9]+(\.[0-9]+){0,2}$ | ADM-AUTH-012 |
| CFG-LEGAL-003 | legal.require_current_version_on_register | 注册必须同意当前版本 | true | 必须为 true | ADM-AUTH-012 |
| CFG-HOME-001 | home.page_size | 首页单次加载数量 | 10 | 5..30 | ADM-PROJ-004 |
| CFG-HOME-002 | home.default_sort | 首页默认排序 | latest | latest\|recommended | ADM-PROJ-004 |
| CFG-HOME-003 | home.recommended_first | 推荐项目优先 | true | true\|false | ADM-PROJ-004 |
| CFG-HOME-004 | home.show_category | 显示分类 | true | true\|false | ADM-PROJ-004 |
| CFG-HOME-005 | home.show_publisher | 显示发布者 | true | true\|false | ADM-PROJ-004 |
| CFG-HOME-006 | home.show_member_badge | 显示会员标识 | true | true\|false | ADM-PROJ-004 |
| CFG-HOME-007 | home.show_view_count | 显示浏览量 | true | true\|false | ADM-PROJ-004 |
| CFG-HOME-008 | home.empty_copy | 首页空状态文案 | 暂时还没有可展示的项目 | 1..80 字 | ADM-PROJ-004 |
| CFG-DETAIL-001 | detail.contact_login_required | 联系方式需登录 | true | 必须为 true | ADM-PROJ-005 |
| CFG-DETAIL-002 | detail.view_dedupe_seconds | 浏览量去重窗口 | 1800 | 60..86400 | ADM-PROJ-005 |
| CFG-DETAIL-003 | detail.recommendations_enabled | 相关推荐 | true | true\|false | ADM-PROJ-005 |

前端功能不得先做壳后补后端。凡本版 Feature 适用的 API、数据、后台配置/查询/审计必须与页面同版完成。

## 线上环境基线（开发前必读）
- 线上 Discuz! X5.0 已搭建，站点根目录为 `/www/wwwroot/stk_zz_yihao_com`，公共站点为 `https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析；开发或部署时仍须实测 DNS、TLS、Host 路由、健康检查和日志，不得把 Owner 提供状态当作本机验证证据。
- 线上服务器已有 Android 构建环境，可按需复用；先盘点实际工具链和版本，再决定构建命令。
- 后台管理员用户名为 `admin`。密码不写入跟踪文档、包清单、CI 或聊天，按本机 `ADMIN_ACCESS_HANDOFF.local.md` 交接；首次登录后建议修改。
- 阿里云短信等外部 Secret 与审核配置由项目所有者后续在后台填写；未配置前只能使用正式 Adapter/Fake 和明确 Owner Action，不得伪造真实发送成功。
