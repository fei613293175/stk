# 1.4.0 后端、数据库、后台和配置范围

## API
| operationId | 方法 | 路径 | 插件 | 鉴权 | 请求 | 成功 |
|---|---|---|---|---|---|---|
| resolveAppLink | GET | /v1/app-links/resolve | stk_project | OPTIONAL_BEARER | https_url | route,parameters,fallback_url |

## 数据实体/迁移
本版不新增业务表；执行索引、保留策略和历史迁移回归。

## 后台页面
| 后台ID | 页面 | 能力 | 角色 |
|---|---|---|---|
| ADM-SYS-002 | 维护模式与功能开关 | 维护开关、提示、功能版本门控 | 超级管理员\|发布管理员 |
| ADM-SYS-003 | App Links 与下载配置 | 深链路由、fallback、下载域名和校验文件 | 超级管理员\|发布管理员 |
| ADM-SYS-004 | 项目系统诊断 | 数据库索引、存储、CDN、版本、深链、队列和健康 | 超级管理员 |

## 后台配置
| 配置ID | Key | 名称 | 默认 | 校验 | 后台 |
|---|---|---|---|---|---|
| CFG-REL-002 | release.download_host | APK 下载域名 | stk-download.zz-yihao.com | 必须在域名合同中登记并启用 HTTPS | ADM-SYS-001 |
| CFG-REL-004 | release.signature_fingerprint_required | 签名指纹校验 | true | 必须为 true | ADM-SYS-001 |
| CFG-MAINT-001 | maintenance.enabled | 维护模式开关 | false | true\|false | ADM-SYS-002 |
| CFG-MAINT-002 | maintenance.title | 维护标题 | 系统维护中 | 1..30 字 | ADM-SYS-002 |
| CFG-MAINT-003 | maintenance.message | 维护说明 | 服务正在维护，请稍后再试。 | 1..200 字 | ADM-SYS-002 |
| CFG-MAINT-004 | maintenance.allow_cached_home | 维护时允许缓存首页 | false | true\|false | ADM-SYS-002 |
| CFG-LINK-001 | app_links.host | App Links 主机 | stk.zz-yihao.com | 必须与 assetlinks.json 一致 | ADM-SYS-003 |
| CFG-LINK-002 | app_links.allowed_paths | App Links 路径 | /project/,/download/,/open/ | 只能为明确路径前缀 | ADM-SYS-003 |

前端功能不得先做壳后补后端。凡本版 Feature 适用的 API、数据、后台配置/查询/审计必须与页面同版完成。

## 线上环境基线（开发前必读）
- 线上 Discuz! X5.0 已搭建，站点根目录为 `/www/wwwroot/stk_zz_yihao_com`，公共站点为 `https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析；开发或部署时仍须实测 DNS、TLS、Host 路由、健康检查和日志，不得把 Owner 提供状态当作本机验证证据。
- 线上服务器已有 Android 构建环境，可按需复用；先盘点实际工具链和版本，再决定构建命令。
- 后台管理员用户名为 `admin`。密码不写入跟踪文档、包清单、CI 或聊天，按本机 `ADMIN_ACCESS_HANDOFF.local.md` 交接；首次登录后建议修改。
- 阿里云短信等外部 Secret 与审核配置由项目所有者后续在后台填写；未配置前只能使用正式 Adapter/Fake 和明确 Owner Action，不得伪造真实发送成功。
