# 1.1.0 后端、数据库、后台和配置范围

## API
本版无新增 API；必须回归历史 API。

## 数据实体/迁移
| 实体ID | 表 | 用途 | 关键字段 | 索引 |
|---|---|---|---|---|
| DB-SYS-001 | pre_stk_external_link_whitelist | 外部域名、Scheme、包名白名单 | id; target_type; host_or_package; path_prefix; action; enabled; expires_at | target_type+host_or_package+enabled |

## 后台页面
| 后台ID | 页面 | 能力 | 角色 |
|---|---|---|---|
| ADM-PROJ-006 | 外部联系方式与白名单 | 电话/微信/QQ/网址类型、域名/Scheme/包名白名单 | 超级管理员\|安全审计员 |
| ADM-PROJ-007 | 搜索与发现设置 | 搜索开关、字段、关键词限制和游标分页 | 超级管理员\|项目运营 |

## 后台配置
| 配置ID | Key | 名称 | 默认 | 校验 | 后台 |
|---|---|---|---|---|---|
| CFG-HOME-009 | home.cache_ttl_seconds | 首页缓存新鲜期 | 300 | 60..3600 | ADM-PROJ-004 |
| CFG-SEARCH-001 | search.enabled | 搜索开关 | true | true\|false | ADM-PROJ-007 |
| CFG-SEARCH-002 | search.min_keyword_length | 关键词最短长度 | 2 | 1..10 | ADM-PROJ-007 |
| CFG-SEARCH-003 | search.max_keyword_length | 关键词最长长度 | 40 | 10..100 | ADM-PROJ-007 |
| CFG-SEARCH-004 | search.fields | 搜索字段 | title,summary | 只能选择 title\|summary | ADM-PROJ-007 |
| CFG-EXT-001 | external.confirm_before_open | 外链打开前确认 | true | 必须为 true，系统拨号器除外 | ADM-PROJ-006 |
| CFG-EXT-002 | external.allow_custom_tabs | 允许 Custom Tabs | true | 必须为 true | ADM-PROJ-006 |
| CFG-EXT-003 | external.internal_webview_enabled | 内部 WebView | false | 必须为 false | ADM-PROJ-006 |
| CFG-EXT-004 | contact.allowed_types | 允许联系方式类型 | phone,wechat,qq,website | phone\|wechat\|qq\|website 子集且至少一个 | ADM-PROJ-006 |
| CFG-EXT-005 | contact.phone_dial_enabled | 允许系统拨号 | true | true\|false | ADM-PROJ-006 |
| CFG-EXT-006 | contact.copy_enabled | 允许复制 | true | true\|false | ADM-PROJ-006 |

前端功能不得先做壳后补后端。凡本版 Feature 适用的 API、数据、后台配置/查询/审计必须与页面同版完成。

## 线上环境基线（开发前必读）
- 线上 Discuz! X5.0 已搭建，站点根目录为 `/www/wwwroot/stk_zz_yihao_com`，公共站点为 `https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析；开发或部署时仍须实测 DNS、TLS、Host 路由、健康检查和日志，不得把 Owner 提供状态当作本机验证证据。
- 线上服务器已有 Android 构建环境，可按需复用；先盘点实际工具链和版本，再决定构建命令。
- 后台管理员用户名为 `admin`。密码不写入跟踪文档、包清单、CI 或聊天，按本机 `ADMIN_ACCESS_HANDOFF.local.md` 交接；首次登录后建议修改。
- 阿里云短信等外部 Secret 与审核配置由项目所有者后续在后台填写；未配置前只能使用正式 Adapter/Fake 和明确 Owner Action，不得伪造真实发送成功。
