# 1.3.0 后端、数据库、后台和配置范围

## API
| operationId | 方法 | 路径 | 插件 | 鉴权 | 请求 | 成功 |
|---|---|---|---|---|---|---|
| getMyMember | GET | /v1/me/member | stk_project | BEARER | none | status,level_name,badge,started_at,expires_at,benefit_copy |
| getMyWallets | GET | /v1/me/wallets | stk_project | BEARER | none | accounts[type,name,balance,currency,operation_enabled=false] |
| getPropDisplay | GET | /v1/props/display | stk_project | BEARER | none | items[code,name,icon_url,description,display_order,operation_enabled=false] |
| getSupportConfig | GET | /v1/support | stk_project | BEARER | none | title,description,channels[type,label,value,masked_value,external_target] |
| getCurrentRelease | GET | /v1/app/releases/current | stk_project | OPTIONAL_BEARER | application_id,version_code,version_name,device_api,abi | latest,minimum_supported,update_type,apk_url,apk_sha256,size_bytes,release_notes |

## 数据实体/迁移
| 实体ID | 表 | 用途 | 关键字段 | 索引 |
|---|---|---|---|---|
| DB-ME-003 | pre_stk_prop_catalog | 道具展示目录，不代表库存或可购买 | prop_code PK; name; icon_asset; description; sort_order; enabled | enabled+sort_order |
| DB-SYS-002 | pre_stk_app_release | App 版本、最低版本、APK 校验和与发布状态 | id; application_id; version_name; version_code; min_supported_code; apk_url; apk_sha256; size_bytes; release_notes; status | application_id+status+version_code; version_code UNIQUE |

## 后台页面
| 后台ID | 页面 | 能力 | 角色 |
|---|---|---|---|
| ADM-ME-001 | 我的页面展示配置 | 账户名称、会员文案、常用功能显示、占位说明 | 超级管理员\|项目运营 |
| ADM-ME-002 | 会员状态管理 | 查询、人工开通/续期/关闭、审计 | 超级管理员\|会员运营 |
| ADM-ME-003 | 账户余额查询 | 佣金/任务账户只读查询，禁止后台直接改余额 | 超级管理员\|财务查看员 |
| ADM-ME-004 | 道具展示目录 | 名称、图标、说明、排序、启停 | 超级管理员\|项目运营 |
| ADM-ME-005 | 客服与关于配置 | 客服渠道、备案、版权、品牌信息 | 超级管理员\|内容管理员 |
| ADM-SYS-001 | App 版本发布配置 | 版本号、最低版本、更新类型、APK URL/SHA/日志 | 超级管理员\|发布管理员 |

## 后台配置
| 配置ID | Key | 名称 | 默认 | 校验 | 后台 |
|---|---|---|---|---|---|
| CFG-ME-001 | me.commission_account_name | 佣金账户名称 | 佣金账户 | 1..12 字 | ADM-ME-001 |
| CFG-ME-002 | me.task_account_name | 任务账户名称 | 任务账户 | 1..12 字 | ADM-ME-001 |
| CFG-ME-003 | me.show_wallets | 显示账户余额 | true | true\|false | ADM-ME-001 |
| CFG-ME-004 | me.show_member_card | 显示会员卡 | true | true\|false | ADM-ME-001 |
| CFG-ME-005 | me.show_props | 显示道具中心 | true | true\|false | ADM-ME-001 |
| CFG-ME-006 | me.placeholder_copy | 占位页统一说明 | 该功能尚未在本期开放，当前不会产生任何业务数据。 | 10..120 字 | ADM-ME-001 |
| CFG-MEMBER-001 | member.inactive_title | 未开通会员标题 | 开通会员，推广更省心 | 1..30 字 | ADM-ME-001 |
| CFG-MEMBER-002 | member.benefit_discount_copy | 折扣权益展示文案 | 消费 5 折 | 1..20 字；仅展示文案 | ADM-ME-001 |
| CFG-MEMBER-003 | member.benefit_commission_copy | 返佣权益展示文案 | 消费返佣 40% | 1..20 字；仅展示文案 | ADM-ME-001 |
| CFG-MEMBER-004 | member.operation_enabled | 在线开通功能 | false | 本五版本必须为 false | ADM-ME-001 |
| CFG-WALLET-001 | wallet.currency | 账户币种 | CNY | CNY | ADM-ME-003 |
| CFG-WALLET-002 | wallet.operation_enabled | 账户操作功能 | false | 本五版本必须为 false | ADM-ME-003 |
| CFG-PROP-001 | props.operation_enabled | 道具购买/使用功能 | false | 本五版本必须为 false | ADM-ME-004 |
| CFG-SUPPORT-001 | support.title | 客服标题 | 联系商推客客服 | 1..30 字 | ADM-ME-005 |
| CFG-SUPPORT-002 | support.description | 客服说明 | 工作时间内我们会尽快回复。 | 1..120 字 | ADM-ME-005 |
| CFG-SUPPORT-003 | about.company_name | 运营主体名称 | 待项目所有者配置 | 1..80 字 | ADM-ME-005 |
| CFG-SUPPORT-004 | about.record_number | 备案号 | 待项目所有者配置 | 0..80 字 | ADM-ME-005 |
| CFG-REL-001 | release.application_id | Android applicationId | com.zzyihao.stk | 必须固定且与 APK 一致 | ADM-SYS-001 |
| CFG-REL-003 | release.sha256_required | APK SHA-256 必填 | true | 必须为 true | ADM-SYS-001 |

前端功能不得先做壳后补后端。凡本版 Feature 适用的 API、数据、后台配置/查询/审计必须与页面同版完成。

## 线上环境基线（开发前必读）
- 线上 Discuz! X5.0 已搭建，站点根目录为 `/www/wwwroot/stk_zz_yihao_com`，公共站点为 `https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析；开发或部署时仍须实测 DNS、TLS、Host 路由、健康检查和日志，不得把 Owner 提供状态当作本机验证证据。
- 线上服务器已有 Android 构建环境，可按需复用；先盘点实际工具链和版本，再决定构建命令。
- 后台管理员用户名为 `admin`。密码不写入跟踪文档、包清单、CI 或聊天，按本机 `ADMIN_ACCESS_HANDOFF.local.md` 交接；首次登录后建议修改。
- 阿里云短信等外部 Secret 与审核配置由项目所有者后续在后台填写；未配置前只能使用正式 Adapter/Fake 和明确 Owner Action，不得伪造真实发送成功。
