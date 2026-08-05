# 08 API、鉴权、幂等与安全合同

## 1. 通用响应

成功和错误均返回 `code、message、data/field_errors、request_id、server_time`。客户端只能根据稳定 `code` 决策，不能解析中文 message。所有 API 使用 HTTPS，生产拒绝明文 HTTP。

## 2. Token

Access Token 默认 120 分钟；Refresh Token 默认 30 天。Refresh Token 每次使用原子轮换；检测旧 Token 重放时撤销整个 Token Family。Android 将 Access Token 保存在内存，Refresh Token 用 Keystore AES-GCM 加密。服务端只存哈希。

## 3. 幂等

注册、登录、短信发送、密码重置、上传、创建、编辑、状态变更和退出等写操作使用 `Idempotency-Key`。服务端保存请求摘要与响应摘要；同 Key 不同请求体返回冲突，同 Key 同请求返回首次结果。

## 4. 权限

- 匿名：验证码、短信、登录、注册、重置、协议、Bootstrap、版本检查。
- 登录用户：项目浏览、发布和本人资料。
- 项目 Owner：只能操作本人项目，并受状态机限制。
- 管理员：按后台角色执行；用户端 Token 不能调用后台接口。

## 5. PII 与日志

手机号、联系方式、验证码密文使用服务器主密钥加密；查找字段使用 keyed HMAC。日志、指标和异常中禁止输出手机号、短信码、密码、Token、AccessKey、联系信息和图片原始路径。请求编号用于用户报错与后台追踪。

## 6. API 总表

| operationId | 方法 | 路径 | 插件 | 版本 | 鉴权 | 幂等 | 错误码 |
|---|---|---|---|---|---|---|---|
| getBootstrap | GET | /v1/bootstrap | stk_auth | V1.0.0 | OPTIONAL_BEARER | N/A | SYS_MAINTENANCE\|AUTH_TOKEN_EXPIRED\|SYS_UNAVAILABLE |
| getCaptchaChallenge | POST | /v1/auth/captcha/challenges | stk_auth | V1.0.0 | ANONYMOUS | Idempotency-Key optional | CAPTCHA_RATE_LIMITED\|CAPTCHA_UNAVAILABLE |
| verifyCaptcha | POST | /v1/auth/captcha/verify | stk_auth | V1.0.0 | ANONYMOUS | challenge one-use | CAPTCHA_INVALID\|CAPTCHA_EXPIRED\|CAPTCHA_ATTEMPTS_EXCEEDED |
| sendSmsCode | POST | /v1/auth/sms/send | stk_auth | V1.0.0 | ANONYMOUS | Idempotency-Key required | SMS_RATE_LIMITED\|SMS_PROVIDER_FAILED\|SMS_SCENE_DISABLED\|CAPTCHA_TICKET_INVALID |
| loginWithPassword | POST | /v1/auth/login/password | stk_auth | V1.0.0 | ANONYMOUS | Idempotency-Key required | AUTH_INVALID_CREDENTIALS\|AUTH_ACCOUNT_BLOCKED\|CAPTCHA_TICKET_INVALID |
| loginWithSms | POST | /v1/auth/login/sms | stk_auth | V1.0.0 | ANONYMOUS | Idempotency-Key required | SMS_CODE_INVALID\|SMS_CODE_EXPIRED\|AUTH_ACCOUNT_BLOCKED\|CAPTCHA_TICKET_INVALID |
| registerUser | POST | /v1/auth/register | stk_auth | V1.0.0 | ANONYMOUS | Idempotency-Key required | AUTH_MOBILE_EXISTS\|AUTH_PASSWORD_WEAK\|LEGAL_VERSION_STALE\|CAPTCHA_TICKET_INVALID |
| resetPassword | POST | /v1/auth/password/reset | stk_auth | V1.0.0 | ANONYMOUS | Idempotency-Key required | SMS_CODE_INVALID\|SMS_CODE_EXPIRED\|AUTH_PASSWORD_WEAK\|CAPTCHA_TICKET_INVALID |
| refreshToken | POST | /v1/auth/token/refresh | stk_auth | V1.0.0 | REFRESH_TOKEN | refresh token rotates atomically | AUTH_REFRESH_INVALID\|AUTH_TOKEN_REUSED\|AUTH_ACCOUNT_BLOCKED |
| logout | POST | /v1/auth/logout | stk_auth | V1.0.0 | BEARER | Idempotency-Key required | AUTH_TOKEN_INVALID |
| getMySummary | GET | /v1/me/summary | stk_auth | V1.0.0 | BEARER | N/A | AUTH_TOKEN_EXPIRED\|AUTH_ACCOUNT_BLOCKED |
| getLegalDocument | GET | /v1/legal/{document_type} | stk_auth | V1.0.0 | ANONYMOUS | N/A | LEGAL_NOT_FOUND |
| listProjectCategories | GET | /v1/project/categories | stk_project | V1.0.0 | BEARER | N/A | PROJECT_CATEGORY_UNAVAILABLE |
| listProjects | GET | /v1/projects | stk_project | V1.0.0 | BEARER | N/A | PROJECT_QUERY_INVALID\|PROJECT_SERVICE_UNAVAILABLE |
| getProjectDetail | GET | /v1/projects/{project_id} | stk_project | V1.0.0 | BEARER | N/A | PROJECT_NOT_FOUND\|PROJECT_NOT_VISIBLE |
| recordProjectView | POST | /v1/projects/{project_id}/views | stk_project | V1.0.0 | BEARER | unique project+viewer+time bucket | PROJECT_NOT_FOUND |
| uploadProjectImage | POST | /v1/uploads/project-images | stk_project | V1.2.0 | BEARER_MULTIPART | Idempotency-Key required | UPLOAD_TOO_LARGE\|UPLOAD_TYPE_INVALID\|UPLOAD_IMAGE_INVALID\|UPLOAD_QUOTA_EXCEEDED |
| deleteUploadedImage | DELETE | /v1/uploads/project-images/{asset_id} | stk_project | V1.2.0 | BEARER | Idempotency-Key required | UPLOAD_NOT_FOUND\|UPLOAD_NOT_OWNER\|UPLOAD_ALREADY_ATTACHED |
| createProject | POST | /v1/projects | stk_project | V1.2.0 | BEARER | Idempotency-Key required | PROJECT_VALIDATION_FAILED\|PROJECT_DAILY_LIMIT\|PROJECT_PENDING_LIMIT\|PROJECT_DUPLICATE_SUBMISSION |
| updateProject | PUT | /v1/projects/{project_id} | stk_project | V1.2.0 | BEARER | Idempotency-Key required | PROJECT_NOT_OWNER\|PROJECT_CONFLICT\|PROJECT_VALIDATION_FAILED |
| listMyProjects | GET | /v1/me/projects | stk_project | V1.2.0 | BEARER | N/A | PROJECT_QUERY_INVALID |
| offlineProject | POST | /v1/projects/{project_id}/offline | stk_project | V1.2.0 | BEARER | Idempotency-Key required | PROJECT_NOT_OWNER\|PROJECT_STATE_INVALID\|PROJECT_CONFLICT |
| resubmitProject | POST | /v1/projects/{project_id}/resubmit | stk_project | V1.2.0 | BEARER | Idempotency-Key required | PROJECT_NOT_OWNER\|PROJECT_STATE_INVALID\|PROJECT_CONFLICT |
| deleteProject | DELETE | /v1/projects/{project_id} | stk_project | V1.2.0 | BEARER | Idempotency-Key required | PROJECT_NOT_OWNER\|PROJECT_STATE_INVALID\|PROJECT_CONFLICT |
| getMyMember | GET | /v1/me/member | stk_project | V1.3.0 | BEARER | N/A | MEMBER_UNAVAILABLE |
| getMyWallets | GET | /v1/me/wallets | stk_project | V1.3.0 | BEARER | N/A | WALLET_UNAVAILABLE |
| getPropDisplay | GET | /v1/props/display | stk_project | V1.3.0 | BEARER | N/A | PROP_CONFIG_UNAVAILABLE |
| getSupportConfig | GET | /v1/support | stk_project | V1.3.0 | BEARER | N/A | SUPPORT_CONFIG_UNAVAILABLE |
| getCurrentRelease | GET | /v1/app/releases/current | stk_project | V1.3.0 | OPTIONAL_BEARER | N/A | RELEASE_CONFIG_INVALID |
| resolveAppLink | GET | /v1/app-links/resolve | stk_project | V1.4.0 | OPTIONAL_BEARER | N/A | APP_LINK_UNSUPPORTED\|APP_LINK_BLOCKED |
| healthLive | GET | /healthz | platform | V1.0.0 | INTERNAL_OR_PUBLIC_MINIMAL | N/A | SYS_UNAVAILABLE |
| healthReady | GET | /readyz | platform | V1.0.0 | INTERNAL | N/A | SYS_DEPENDENCY_UNREADY |

字段级请求/响应合同以 `contracts/api-inventory.csv` 和 `api-field-contracts.yaml` 为准；OpenAPI 负责路径、方法、安全和自动契约测试。任何实现字段变更必须先更新合同再改代码。
