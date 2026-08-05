# 1.0.0 DNS 与域名动作

## 本版新增解析
| Host | 用途 | TLS | Owner动作 |
|---|---|---|---|
| stk-api.zz-yihao.com | Android JSON API | True | 项目所有者已报告完成解析；发布前实测 DNS、TLS、Host 路由和健康检查 |
| stk-admin.zz-yihao.com | Discuz 与插件管理后台 | True | 项目所有者已报告完成解析；发布前实测并限制后台安全访问 |

## 发布前检查
| Host | 服务版本 | 检查 |
|---|---|---|
| stk-api.zz-yihao.com | V1.0.0 | DNS生效、TLS有效、Host路由正确、健康检查通过、日志可追踪 |
| stk-admin.zz-yihao.com | V1.0.0 | DNS生效、TLS有效、Host路由正确、健康检查通过、日志可追踪 |

线上基线：Discuz! X5.0 根目录为 `/www/wwwroot/stk_zz_yihao_com`，公共站点为 `https://stk.zz-yihao.com`；服务器已有 Android 构建环境。以上 API/admin DNS 状态是项目所有者提供的信息，当前未由本机实测验证。

真实 A/CNAME 目标仍必须来自服务器只读盘点，不得在文档或聊天中猜测。新增域名需要目标时，先取得只读盘点结果，再由项目所有者完成解析并留下 TLS、Host、健康检查和日志证据。
