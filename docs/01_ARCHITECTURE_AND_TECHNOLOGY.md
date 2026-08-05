# 01 架构与技术基线

## Android

- Kotlin 2.4.10；Jetpack Compose；Material 3 仅作为底层能力，视觉由 STK-DS-1.0 覆盖。
- applicationId 固定 `com.zzyihao.stk`；minSdk 26；compile/target SDK 37；JDK 17。
- AGP 9.3.0、Gradle 9.5.0、SDK Build Tools 36.0.0；依赖全部写入 Version Catalog，禁止 `+` 动态版本。
- 模块：`app`、`core-designsystem`、`core-network`、`core-database`、`core-data`、`core-security`、`core-external`、`core-update`、`feature-auth`、`feature-home`、`feature-project-detail`、`feature-publish`、`feature-profile`。
- 单向数据流：UI State → User Intent → ViewModel → Use Case/Repository → Local/Remote Data Source。
- UI 从 Room/StateFlow 读取；网络成功后写入本地，再由 UI 响应更新。DataStore 保存非敏感偏好；Refresh Token 使用 Android Keystore AES-GCM 加密。
- Photo Picker 只授予用户选中图片；不申请读取整个相册的长期权限。
- 外部 H5 使用 Custom Tabs；微信、QQ、支付宝、拨号和分享使用受控 Intent；内部业务页面禁用 WebView。

## Discuz 后端

```text
source/plugin/stk_auth/
  api/ admin/ service/ repository/ integration/ install.php upgrade.php uninstall.php
source/plugin/stk_project/
  api/ admin/ service/ repository/ storage/ install.php upgrade.php uninstall.php
```

- `stk_auth`：手机号绑定、密码/短信登录、注册、安全验证码、Token、密码重置、协议、日志、风控、阿里云短信和认证后台。
- `stk_project`：分类、首页、详情、浏览、上传、发布、审核、我的发布、会员/账户/道具展示、外链白名单、版本更新和项目后台。
- 复用 Discuz UID、密码机制、权限和数据库层；不复制完整用户系统，不改核心文件。
- API 统一 HTTPS JSON v1；写操作使用 Idempotency-Key；服务端校验权限、字段、状态、配额、MIME 和资源归属。

## 前后端边界

服务器下发数据、文案、开关、排序和业务限制。字号、圆角、边距、坐标、卡片样式、顶部栏和底部导航永远由 APK 内 STK-DS-1.0 决定。任何远程 UI 布局字段都属于禁止项。
