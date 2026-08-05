# 05 登录注册插件：前端、后端与后台完整规格

## 1. 插件定位

`stk_auth` 替代商推客 Android 用户端的登录注册入口，但不删除 Discuz 原生 UID/密码体系。App 使用手机号作为登录标识，手机号通过安全绑定表关联 UID；公开用户名和昵称不能使用完整手机号。

## 2. 注册完整链路

```text
Android AUTH-002
→ 本地校验手机号、密码、确认密码、协议版本
→ AUTH-OV-001 安全验证码
→ POST /v1/auth/register（不发送短信）
→ 服务端事务内创建 Discuz UID
→ 生成内部用户名 stk_<uid/序列> 和显示昵称 商推客用户<序列>
→ 写手机号加密绑定，mobile_verified=0
→ 创建 inactive 会员记录
→ 创建 commission/task 两个余额均为 0.00 的账户记录
→ 创建 Token 族与登录日志
→ 返回真实 UID、脱敏手机号、Token 和跳转路由
```

任一步失败必须整体回滚，禁止出现“Discuz 用户已创建但手机号/会员/账户未创建”的半成品。注册后首次成功短信登录将 `mobile_verified` 更新为已验证。

## 3. 密码登录

- 手机号标准化为 +86 E.164；使用 keyed HMAC 查找绑定，解密只用于授权展示。
- 服务端验证 Discuz 密码、账号状态、失败次数、验证码 Ticket、设备和限流。
- 对不存在手机号与密码错误统一返回 `AUTH_INVALID_CREDENTIALS`，防止枚举。
- 成功后发放短期 Access Token 和可轮换 Refresh Token；Token 明文只返回一次，数据库只保存哈希。

## 4. 短信登录

- 点击发送短信必须先通过 `sms_send` 动作的安全验证码。
- 最终短信登录还必须通过 `sms_login` 动作的安全验证码，不能复用发送短信 Ticket。
- 验证码默认 6 位、5 分钟、60 秒重发；手机号/IP/设备/场景四维限流。
- 阿里云调用在服务端完成；APK 不含 AccessKey。
- 发送状态与使用状态分开：`pending/submitted/sent/failed` 与 `unused/used/expired/void/locked`。
- 成功登录后短信码一次性作废，并将手机号标为已验证。

## 5. 安全验证码

Challenge 包含 `challenge_id、action、session/device/ip 摘要、expires_at、attempts`。验证成功返回一次性 `captcha_ticket`；Ticket 只能用于同一 action、同一设备/会话，在 90 秒内使用一次。登录、注册、发送短信、短信登录和密码重置均必须绑定。

## 6. 找回密码

短信场景为 `password_reset`；发送和提交分别需要安全验证码。成功后撤销全部 Token 族并记录审计，用户回登录页重新登录。

## 7. 后台必须交付

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

验证码记录页必须默认脱敏；授权查看完整验证码前进行二次确认，查看动作写入审计。AccessKey Secret 保存为服务器主密钥加密密文，保存后永不完整回显。

## 8. 登录注册 API

| operationId | 方法 | 路径 | 鉴权 | 请求合同 | 成功合同 |
|---|---|---|---|---|---|
| getBootstrap | GET | /v1/bootstrap | OPTIONAL_BEARER | headers: app_version,version_code,device_id,locale | maintenance,auth_state,feature_flags,legal_versions,external_link_policy,release_policy |
| getCaptchaChallenge | POST | /v1/auth/captcha/challenges | ANONYMOUS | action,password_login\|sms_send\|sms_login\|register\|password_reset; device_id | challenge_id,image_base64_or_url,expires_in |
| verifyCaptcha | POST | /v1/auth/captcha/verify | ANONYMOUS | challenge_id,answer,action,device_id | captcha_ticket,ticket_expires_in |
| sendSmsCode | POST | /v1/auth/sms/send | ANONYMOUS | mobile,scene=login\|password_reset,captcha_ticket,device_id | request_id,resend_after,expires_in,masked_mobile |
| loginWithPassword | POST | /v1/auth/login/password | ANONYMOUS | mobile,password,captcha_ticket,device_id,device_name | access_token,access_expires_in,refresh_token,refresh_expires_in,user,next_route |
| loginWithSms | POST | /v1/auth/login/sms | ANONYMOUS | mobile,sms_code,captcha_ticket,device_id,device_name | access_token,access_expires_in,refresh_token,refresh_expires_in,user,next_route |
| registerUser | POST | /v1/auth/register | ANONYMOUS | mobile,password,password_confirmation,agreement_version,privacy_version,captcha_ticket,device_id | uid,username,display_name,masked_mobile,mobile_verified=false,tokens,next_route |
| resetPassword | POST | /v1/auth/password/reset | ANONYMOUS | mobile,sms_code,new_password,password_confirmation,captcha_ticket,device_id | reset=true,next_route |
| refreshToken | POST | /v1/auth/token/refresh | REFRESH_TOKEN | refresh_token,device_id | rotated access_token and refresh_token |
| logout | POST | /v1/auth/logout | BEARER | refresh_token_family,device_id | revoked=true |
| getMySummary | GET | /v1/me/summary | BEARER | none | uid,display_name,avatar_url,masked_mobile,mobile_verified,member_summary,wallet_summary |
| getLegalDocument | GET | /v1/legal/{document_type} | ANONYMOUS | document_type=user_agreement\|privacy_policy | document_type,version,effective_at,title,content_html_sanitized,content_text,sha256 |

## 9. 完成门禁

登录注册 Feature 必须同时通过：Android 正常/错误/离线/限流状态、API 契约、数据库事务、短信 Adapter Mock、阿里云小流量真实联调、Token 重放测试、后台查询配置、敏感操作审计和 V1.0 Owner 真机测试。
