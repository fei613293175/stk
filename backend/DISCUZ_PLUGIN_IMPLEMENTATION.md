# Discuz! X5.0 插件实现合同

## 1. 固定插件边界

```text
source/plugin/stk_auth/
├── discuz_plugin_stk_auth.xml
├── install.php / upgrade.php / uninstall.php
├── api/                 # /v1/auth、bootstrap、legal、health
├── service/             # 认证、Token、验证码、短信、风控、审计
├── table/               # Discuz DB::table 封装
├── admin/               # ADM-AUTH-*
├── cron/                # 回执同步、过期数据清理
└── language/

source/plugin/stk_project/
├── discuz_plugin_stk_project.xml
├── install.php / upgrade.php / uninstall.php
├── api/                 # /v1/projects、me、release、app-links
├── service/             # 项目、上传、审核、会员展示、账户只读、发布
├── table/
├── admin/               # ADM-PROJ-*、ADM-ME-*、ADM-SYS-*
├── storage/             # 附件适配器，不保存凭据
└── language/
```

## 2. 核心禁止项

- 禁止修改 Discuz 核心 PHP、模板、用户表密码算法或系统登录源文件。
- 禁止 Android 直接访问 Discuz 数据库、阿里云短信或服务器文件路径。
- 禁止 API 返回用户业务 HTML；所有 App 页面由 Compose 本地渲染。
- 禁止在插件设置、日志、异常栈或 Git 中保存 Secret 明文。
- 禁止仅做前端按钮而遗漏 API、权限、数据库、后台、审计和错误路径。

## 3. API 网关与响应

所有 `/v1/` 请求由统一入口完成：HTTPS 检查、request_id、版本头、Token、限流、幂等、参数校验、权限、业务服务、审计和统一响应。响应固定为 `code/message/data/request_id/server_time`，HTTP 状态与错误码同时正确，不允许所有错误都返回 200。

## 4. 认证落地

- 密码仍由 Discuz 原生安全机制创建和验证；插件不另存密码。
- 手机号使用 `mobile_hash` 做唯一查询，密文只用于授权展示。
- Access Token 短期、Refresh Token 轮换；数据库只存 Hash。
- 刷新令牌重放立即撤销整个 Token Family。
- 安全验证码 challenge 与动作、设备、会话绑定；验证后返回一次性 ticket。
- 注册不发送短信；首次短信登录成功后把手机号标记为已验证。

## 5. 后台

后台必须通过 Discuz 管理员会话和细分角色能力校验。验证码查看、手机号修改、Token 撤销、Secret 更新、会员人工变更、项目审核和版本发布必须写 `pre_stk_admin_audit`。验证码列表默认只显示掩码，完整查看必须二次确认并设置短时权限。

## 6. 用户前端与后台职责

后台可配置内容、文案、开关、排序、长度和业务规则；不得下发字号、圆角、边距、组件位置、任意 Compose 代码或任意 WebView 页面。所有视觉参数只由 `contracts/ui-design-tokens.yaml` 决定。
