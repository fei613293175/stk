# 当前 API 契约：V1.1 项目浏览（认证底座兼容）

## 默认入口

```text
https://stk.zz-yihao.com/plugin.php?id=stk_auth:api
```

当前 `gradle.properties` 默认使用真实 Discuz Repository。若仅需本地界面演示，才显式传入 `-PstkUseFakeBackend=true`；真实验收构建使用以下接口：

```text
-PstkUseFakeBackend=false
-PstkApiEndpoint=https://stk.zz-yihao.com/plugin.php?id=stk_auth:api
-PstkProjectApiEndpoint=https://stk.zz-yihao.com/plugin.php?id=stk_project:api
```

## 通用响应

成功：

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "request_id": "..."
}
```

失败：

```json
{
  "code": 4001,
  "message": "参数错误",
  "data": null,
  "request_id": "..."
}
```

## 认证动作

所有请求均使用：

```text
POST plugin.php?id=stk_auth:api&action=<action>
Content-Type: application/json
```

| action | 用途 | Beta 1 状态 |
|---|---|---|
| `health` | 插件和数据库状态 | 必须实现 |
| `config` | 返回客户端开发配置 | 必须实现 |
| `captcha_create` | 创建安全验证码挑战 | 生产为 IP/场景绑定的图形验证码；Fake 明示 `2468` |
| `password_login` | 手机号密码登录 | 已接入 Discuz 用户体系 |
| `sms_send` | 发送短信验证码 | 生产未配置时明确返回 503；Fake 不调用阿里云 |
| `sms_login` | 短信验证码登录 | 仅受控 Fake 环境可用 |
| `register` | 手机号注册 | 已创建 Discuz 用户并绑定手机号 |
| `refresh` | Token 刷新 | 轮换刷新令牌 |
| `logout` | 服务端退出 | 撤销当前令牌 |
| `legal_document` | 获取用户协议或隐私政策 | 服务端优先，后台可版本化配置 |

## 请求字段

### `captcha_create`

```json
{"scene":"password_login"}
```

生产响应返回 `challenge_id`、`image_url` 和过期时间，不会返回验证码答案。挑战仅限创建时的 IP 与场景使用，5 分钟过期；同一挑战失败 5 次会锁定，同一 IP/场景每分钟最多创建 5 次。

### `password_login`

```json
{
  "mobile":"13800138000",
  "password":"123456",
  "captcha_code":"2468",
  "captcha_challenge":"dev-challenge"
}
```

### `sms_send`

```json
{
  "mobile":"13800138000",
  "captcha_code":"2468",
  "captcha_challenge":"dev-challenge"
}
```

### `sms_login`

```json
{
  "mobile":"13800138000",
  "sms_code":"123456",
  "captcha_code":"2468",
  "captcha_challenge":"dev-challenge"
}
```

### `register`

```json
{
  "mobile":"13800138000",
  "password":"123456",
  "confirm_password":"123456",
  "captcha_code":"2468",
  "captcha_challenge":"dev-challenge"
}
```

## 认证成功数据

```json
{
  "access_token":"dev-access-token",
  "refresh_token":"dev-refresh-token",
  "expires_at":1786000000,
  "user":{
    "uid":10001,
    "username":"商推客用户0001",
    "mobile_masked":"138****8000",
    "member_label":"普通用户"
  }
}
```

## 安全限制

- `dev_fake_enabled=0` 时，服务端 Fake 认证动作必须拒绝；
- Fake Token 不得进入生产环境；
- 图形验证码使用公开 HTTPS 地址和 PHP GD 扩展；未配置时返回 503，不能退化为返回验证码答案；
- 阿里云 AccessKey、正式签名和管理员密码不得写入 App；

## 项目浏览入口

```text
https://stk.zz-yihao.com/plugin.php?id=stk_project:api
```

项目接口要求 `Authorization: Bearer <access_token>`，资源和分页规则见 `CURRENT_API_V1_1.md`。
