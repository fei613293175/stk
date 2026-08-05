# 06 项目推广主插件：前端、后端与后台完整规格

## 1. 首页

- 单列项目流，不采用双列瀑布流。
- 项目卡展示封面、标题、简介、分类、发布者头像/昵称、会员标识、发布时间和浏览量。
- 只返回 `published` 项目；推荐可优先但排序必须稳定。
- 游标由排序字段与 project_id 组成，切换搜索/分类时重置；下一页失败只在列表底部局部重试。
- V1.0 后台可创建测试项目和分类，确保首版可以验证真实列表/详情；V1.2 起用户发布接入同一表和状态机。

## 2. 搜索与分类

搜索字段固定为标题和简介；默认最短 2 字、最长 40 字。分类由后台排序和启停，客户端不得自造分类。搜索结果同样使用游标分页和缓存；本期不保存个人搜索历史。

## 3. 项目详情

图片轮播、完整标题/简介、分类、发布者、时间、浏览量和联系方式。浏览量按 UID 与时间桶去重；图片失败不影响正文。非本人只能查看 published；本人可查看 pending/rejected/offline 并看到状态允许的操作。

## 4. 联系方式和外部跳转

支持 phone、wechat、qq、website。完整值只在已登录详情中按授权显示；数据库加密保存，日志不输出明文。拨号走系统拨号器；复制走剪贴板；微信/QQ/网址走统一 ExternalAppLauncher。网址必须命中后台白名单并使用 Custom Tabs，禁止内部 WebView。

## 5. 发布与图片

- 标题默认 4～40 字；简介 20～1000 字；联系人 0～30 字。
- 图片默认 1～9 张，后台可调 1～20；单张服务端上限 10MB；JPG/PNG/WEBP；最长边 1920px；JPEG 质量 82。
- Android Photo Picker → 本地读取/旋转/压缩 → 计算 SHA-256 → 并发 2 上传 → 服务端 finfo、解码、尺寸、恶意文件、EXIF 清理和归属校验。
- 上传资产先属于 upload_session，24 小时未绑定项目自动清理。发布事务把 asset 与 project 绑定并确定唯一封面。
- 创建项目使用 Idempotency-Key；重复请求返回首次结果，不创建重复项目。

## 6. 项目状态机

```text
create → pending（默认）或 published（后台关闭审核）
pending → published | rejected | deleted
published → pending（编辑后重审） | offline | deleted
rejected → pending（修改/重提） | deleted
offline → pending（编辑后重审） | deleted
```

任何状态变化均校验当前 version 乐观锁，并写 `pre_stk_project_audit_log`。客户端只显示服务端返回的 `owner_actions`，但服务端仍必须再次鉴权。

## 7. 我的页面

- 资料卡：头像、昵称、UID、会员标识、脱敏手机号。
- 账户：佣金账户和任务账户，注册时真实创建为 0.00；只读，不实现流水、提现、转账或后台改余额。
- 会员：inactive/active/expired，后台可人工开通/续期/关闭用于实际运营展示；不实现在线付费、折扣或返佣结算。
- 道具：刷新卡、超级头条、头条、变色卡展示目录；不实现购买、库存或使用。
- 浏览记录、收藏、实名认证：固定占位页，不建立对应业务数据。
- 我的发布：真实列表、状态筛选、编辑、下架、重提、删除。

## 8. 项目后台

| 后台ID | 页面 | 能力 | 角色 |
|---|---|---|---|
| ADM-PROJ-001 | 项目运营概览 | 项目状态、发布者、浏览和审核摘要 | 超级管理员\|项目运营 |
| ADM-PROJ-002 | 项目管理 | 查询、查看、创建测试数据、编辑、推荐、上下架、删除 | 超级管理员\|项目运营 |
| ADM-PROJ-003 | 项目分类 | 分类增改、排序、启停 | 超级管理员\|项目运营 |
| ADM-PROJ-004 | 首页展示设置 | 每页数量、默认排序、显示字段、空状态文案 | 超级管理员\|项目运营 |
| ADM-PROJ-005 | 项目详情设置 | 发布者字段、联系方式展示和分享规则 | 超级管理员\|项目运营 |
| ADM-PROJ-006 | 外部联系方式与白名单 | 电话/微信/QQ/网址类型、域名/Scheme/包名白名单 | 超级管理员\|安全审计员 |
| ADM-PROJ-007 | 搜索与发现设置 | 搜索开关、字段、关键词限制和游标分页 | 超级管理员\|项目运营 |
| ADM-PROJ-008 | 待审核项目 | 批量/单项审核、驳回原因 | 超级管理员\|项目审核员 |
| ADM-PROJ-009 | 发布规则 | 字段长度、每日上限、待审核上限、编辑后审核 | 超级管理员\|项目运营 |
| ADM-PROJ-010 | 图片上传设置 | 数量、体积、格式、尺寸、压缩、清理 | 超级管理员 |
| ADM-PROJ-011 | 用户项目查询 | 按 UID/手机号/昵称/状态查询 | 超级管理员\|项目审核员 |
| ADM-PROJ-012 | 项目审核与状态日志 | 全部审核/上下架/删除动作审计 | 超级管理员\|安全审计员 |
| ADM-ME-001 | 我的页面展示配置 | 账户名称、会员文案、常用功能显示、占位说明 | 超级管理员\|项目运营 |
| ADM-ME-002 | 会员状态管理 | 查询、人工开通/续期/关闭、审计 | 超级管理员\|会员运营 |
| ADM-ME-003 | 账户余额查询 | 佣金/任务账户只读查询，禁止后台直接改余额 | 超级管理员\|财务查看员 |
| ADM-ME-004 | 道具展示目录 | 名称、图标、说明、排序、启停 | 超级管理员\|项目运营 |
| ADM-ME-005 | 客服与关于配置 | 客服渠道、备案、版权、品牌信息 | 超级管理员\|内容管理员 |
| ADM-SYS-001 | App 版本发布配置 | 版本号、最低版本、更新类型、APK URL/SHA/日志 | 超级管理员\|发布管理员 |
| ADM-SYS-002 | 维护模式与功能开关 | 维护开关、提示、功能版本门控 | 超级管理员\|发布管理员 |
| ADM-SYS-003 | App Links 与下载配置 | 深链路由、fallback、下载域名和校验文件 | 超级管理员\|发布管理员 |
| ADM-SYS-004 | 项目系统诊断 | 数据库索引、存储、CDN、版本、深链、队列和健康 | 超级管理员 |

账户余额查询页明确只读。项目管理的删除是软删除；审核、推荐、上下架、会员人工变更和配置修改全部记录管理员 UID、前后摘要、目标、IP 摘要和时间。

## 9. 项目 API

| operationId | 方法 | 路径 | 版本 | 请求合同 | 成功合同 |
|---|---|---|---|---|---|
| listProjectCategories | GET | /v1/project/categories | V1.0.0 | include_all=true | categories[id,name,icon_url,sort_order] |
| listProjects | GET | /v1/projects | V1.0.0 | cursor,limit<=30,keyword,category_id,sort=latest\|recommended | items, next_cursor, has_more, applied_filters |
| getProjectDetail | GET | /v1/projects/{project_id} | V1.0.0 | project_id | project full detail, images, publisher, contact capability, owner_actions |
| recordProjectView | POST | /v1/projects/{project_id}/views | V1.0.0 | project_id,view_session_id | counted,view_count |
| uploadProjectImage | POST | /v1/uploads/project-images | V1.2.0 | file,upload_session_id,client_sha256,width,height,mime | asset_id,thumbnail_url,display_url,width,height,size,mime,status |
| deleteUploadedImage | DELETE | /v1/uploads/project-images/{asset_id} | V1.2.0 | asset_id | deleted=true |
| createProject | POST | /v1/projects | V1.2.0 | title,summary,category_id,asset_ids,cover_asset_id,contact_type,contact_value,contact_name,publish_agreement_version | project_id,status=pending\|published,submitted_at,next_route |
| updateProject | PUT | /v1/projects/{project_id} | V1.2.0 | project_id,version,title,summary,category_id,asset_ids,cover_asset_id,contact fields | project_id,status,version,updated_at |
| listMyProjects | GET | /v1/me/projects | V1.2.0 | status, cursor, limit<=30 | items,next_cursor,has_more,status_counts |
| offlineProject | POST | /v1/projects/{project_id}/offline | V1.2.0 | project_id,version | status=offline,version,updated_at |
| resubmitProject | POST | /v1/projects/{project_id}/resubmit | V1.2.0 | project_id,version | status=pending,version,submitted_at |
| deleteProject | DELETE | /v1/projects/{project_id} | V1.2.0 | project_id,version | deleted=true |
| getMyMember | GET | /v1/me/member | V1.3.0 | none | status,level_name,badge,started_at,expires_at,benefit_copy |
| getMyWallets | GET | /v1/me/wallets | V1.3.0 | none | accounts[type,name,balance,currency,operation_enabled=false] |
| getPropDisplay | GET | /v1/props/display | V1.3.0 | none | items[code,name,icon_url,description,display_order,operation_enabled=false] |
| getSupportConfig | GET | /v1/support | V1.3.0 | none | title,description,channels[type,label,value,masked_value,external_target] |
| getCurrentRelease | GET | /v1/app/releases/current | V1.3.0 | application_id,version_code,version_name,device_api,abi | latest,minimum_supported,update_type,apk_url,apk_sha256,size_bytes,release_notes |
| resolveAppLink | GET | /v1/app-links/resolve | V1.4.0 | https_url | route,parameters,fallback_url |
