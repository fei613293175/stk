# 商推客 V1.5.0 服务器交付入口

当前交付版本为 `1.5.0 / 10500`。Android、Discuz 插件、发布资料和交付证据必须以 `CURRENT_TASK.md`、`CURRENT_API.md` 与 `CURRENT_ACCEPTANCE.md` 为准。

## 执行约束

- 禁止在本机安装、搭建或运行项目开发、构建、测试或模拟器环境。
- Android 构建、JVM 单测、Lint、PHP 检查和部署均在已连接服务器执行。
- 本项目不执行 APK 模拟器测试；服务器生成 Release APK 后交付项目拥有者真机验证。
- APK 使用公开 Beta 签名，只用于安装测试，不等同于正式生产签名。

## 服务器工作流

1. 将源码同步到服务器独立构建目录，不直接在网站运行目录构建。
2. 使用 JDK 17、Gradle 8.13、Android SDK 36 执行 `scripts/build-beta-apk.sh`。
3. 执行 Discuz PHP 语法检查和 `discuz/tests/*.php` 合同测试。
4. 核对 APK 版本、SHA-256、签名证书和 Lint/单测报告。
5. 备份线上插件和发布目录后部署，运行数据库升级和公网接口检查。
6. 生成 `deliveries/1.5.0/`，交付 APK、校验值、完成清单、已知限制和用户测试指南。

## 当前外部限制

- 没有正式生产签名私钥；当前 App Links 绑定公开 Beta 证书指纹。
- 没有阿里云短信生产密钥；代码支持后台配置，真实发送由凭据到位后验收。
- APK 的覆盖安装、真实账号流程和视觉体验由项目拥有者在真机验证。

完整五版本规格仍保存在 `offline-spec/STK_FULL_SPEC_OFFLINE.zip`，仅作为需求追溯资料，不在本机搭建运行环境。
