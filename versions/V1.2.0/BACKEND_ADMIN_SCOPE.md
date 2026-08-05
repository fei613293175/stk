# 1.2.0 后端、数据库、后台和配置范围

## API
| operationId | 方法 | 路径 | 插件 | 鉴权 | 请求 | 成功 |
|---|---|---|---|---|---|---|
| uploadProjectImage | POST | /v1/uploads/project-images | stk_project | BEARER_MULTIPART | file,upload_session_id,client_sha256,width,height,mime | asset_id,thumbnail_url,display_url,width,height,size,mime,status |
| deleteUploadedImage | DELETE | /v1/uploads/project-images/{asset_id} | stk_project | BEARER | asset_id | deleted=true |
| createProject | POST | /v1/projects | stk_project | BEARER | title,summary,category_id,asset_ids,cover_asset_id,contact_type,contact_value,contact_name,publish_agreement_version | project_id,status=pending\|published,submitted_at,next_route |
| updateProject | PUT | /v1/projects/{project_id} | stk_project | BEARER | project_id,version,title,summary,category_id,asset_ids,cover_asset_id,contact fields | project_id,status,version,updated_at |
| listMyProjects | GET | /v1/me/projects | stk_project | BEARER | status, cursor, limit<=30 | items,next_cursor,has_more,status_counts |
| offlineProject | POST | /v1/projects/{project_id}/offline | stk_project | BEARER | project_id,version | status=offline,version,updated_at |
| resubmitProject | POST | /v1/projects/{project_id}/resubmit | stk_project | BEARER | project_id,version | status=pending,version,submitted_at |
| deleteProject | DELETE | /v1/projects/{project_id} | stk_project | BEARER | project_id,version | deleted=true |

## 数据实体/迁移
| 实体ID | 表 | 用途 | 关键字段 | 索引 |
|---|---|---|---|---|
| DB-PROJ-003 | pre_stk_upload_asset | 上传会话、图片元数据和生命周期 | asset_id; uid; upload_session_id; storage_key; mime; size; width; height; sha256; status; attached_project_id | uid+status+created_at; upload_session_id; attached_project_id+sort_order; sha256 |
| DB-PROJ-004 | pre_stk_project_image | 项目与图片关联、顺序和封面 | project_id+asset_id; sort_order; is_cover | project_id+sort_order UNIQUE; project_id+is_cover |
| DB-PROJ-005 | pre_stk_project_audit_log | 项目审核、驳回、上下架与用户状态动作 | id; project_id; actor_type; actor_uid; action; from_status; to_status; reason; snapshot_digest; created_at | project_id+created_at; actor_uid+created_at; action+created_at |

## 后台页面
| 后台ID | 页面 | 能力 | 角色 |
|---|---|---|---|
| ADM-PROJ-008 | 待审核项目 | 批量/单项审核、驳回原因 | 超级管理员\|项目审核员 |
| ADM-PROJ-009 | 发布规则 | 字段长度、每日上限、待审核上限、编辑后审核 | 超级管理员\|项目运营 |
| ADM-PROJ-010 | 图片上传设置 | 数量、体积、格式、尺寸、压缩、清理 | 超级管理员 |
| ADM-PROJ-011 | 用户项目查询 | 按 UID/手机号/昵称/状态查询 | 超级管理员\|项目审核员 |
| ADM-PROJ-012 | 项目审核与状态日志 | 全部审核/上下架/删除动作审计 | 超级管理员\|安全审计员 |

## 后台配置
| 配置ID | Key | 名称 | 默认 | 校验 | 后台 |
|---|---|---|---|---|---|
| CFG-PUB-001 | publish.enabled | 用户发布开关 | true | true\|false | ADM-PROJ-009 |
| CFG-PUB-002 | publish.review_required | 先审后发 | true | true\|false | ADM-PROJ-009 |
| CFG-PUB-003 | publish.title_min | 标题最短长度 | 4 | 2..20 | ADM-PROJ-009 |
| CFG-PUB-004 | publish.title_max | 标题最长长度 | 40 | 20..80 | ADM-PROJ-009 |
| CFG-PUB-005 | publish.summary_min | 简介最短长度 | 20 | 10..100 | ADM-PROJ-009 |
| CFG-PUB-006 | publish.summary_max | 简介最长长度 | 1000 | 200..5000 | ADM-PROJ-009 |
| CFG-PUB-007 | publish.contact_name_max | 联系人最长长度 | 30 | 0..60 | ADM-PROJ-009 |
| CFG-PUB-008 | publish.daily_limit | 单用户每日发布上限 | 10 | 1..100 | ADM-PROJ-009 |
| CFG-PUB-009 | publish.pending_limit | 单用户待审核上限 | 5 | 1..50 | ADM-PROJ-009 |
| CFG-PUB-010 | publish.edit_requires_review | 已发布项目编辑后重新审核 | true | 建议保持 true | ADM-PROJ-009 |
| CFG-PUB-011 | publish.user_offline_enabled | 允许用户下架 | true | true\|false | ADM-PROJ-009 |
| CFG-PUB-012 | publish.user_delete_enabled | 允许用户删除 | true | true\|false | ADM-PROJ-009 |
| CFG-PUB-013 | publish.agreement_version | 发布规范版本 | 1.0 | 有效已发布版本 | ADM-PROJ-009 |
| CFG-UPL-001 | upload.image_min_count | 最少图片数 | 1 | 0..20 且不大于最大数 | ADM-PROJ-010 |
| CFG-UPL-002 | upload.image_max_count | 最多图片数 | 9 | 1..20 | ADM-PROJ-010 |
| CFG-UPL-003 | upload.max_bytes | 单图最大字节数 | 10485760 | 1048576..20971520 | ADM-PROJ-010 |
| CFG-UPL-004 | upload.allowed_mime | 允许 MIME | image/jpeg,image/png,image/webp | 仅安全位图格式 | ADM-PROJ-010 |
| CFG-UPL-005 | upload.max_long_edge_px | 最长边限制 | 1920 | 1080..4096 | ADM-PROJ-010 |
| CFG-UPL-006 | upload.jpeg_quality | JPEG 目标质量 | 82 | 70..92 | ADM-PROJ-010 |
| CFG-UPL-007 | upload.concurrent_count | 客户端上传并发 | 2 | 1..3 | ADM-PROJ-010 |
| CFG-UPL-008 | upload.unattached_cleanup_hours | 未绑定图片清理小时 | 24 | 6..168 | ADM-PROJ-010 |
| CFG-UPL-009 | upload.strip_exif | 移除 EXIF | true | 必须为 true | ADM-PROJ-010 |

前端功能不得先做壳后补后端。凡本版 Feature 适用的 API、数据、后台配置/查询/审计必须与页面同版完成。

## 线上环境基线（开发前必读）
- 线上 Discuz! X5.0 已搭建，站点根目录为 `/www/wwwroot/stk_zz_yihao_com`，公共站点为 `https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析；开发或部署时仍须实测 DNS、TLS、Host 路由、健康检查和日志，不得把 Owner 提供状态当作本机验证证据。
- 线上服务器已有 Android 构建环境，可按需复用；先盘点实际工具链和版本，再决定构建命令。
- 后台管理员用户名为 `admin`。密码不写入跟踪文档、包清单、CI 或聊天，按本机 `ADMIN_ACCESS_HANDOFF.local.md` 交接；首次登录后建议修改。
- 阿里云短信等外部 Secret 与审核配置由项目所有者后续在后台填写；未配置前只能使用正式 Adapter/Fake 和明确 Owner Action，不得伪造真实发送成功。
