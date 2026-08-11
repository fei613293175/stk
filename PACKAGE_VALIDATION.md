# V1.5.0 服务器构建验证

## 已通过

- 服务器 Gradle 8.13、JDK 17、Android SDK 36 工具链验证；
- `testReleaseUnitTest`：8 个测试套件、28 项测试、0 失败；
- `lintRelease`：0 Error，33 Warning，3 Hint；
- `assembleRelease`：通过，生成 `1.5.0 / 10500` Release APK；
- APK v2 签名验证通过，证书 SHA-256 为 `8d82da03e9133c1d60087e735b2abc9d9d2ecbb0f1815164297079d8eba537ee`；
- APK SHA-256 为 `70c140a6c13a563440fdc13c89fa4fcb5c5f1ebc4ab5b606f497cd889a0e6114`；
- Discuz PHP 全量语法检查通过；
- 认证、认证服务、开发认证、项目、路由、完整规格共 6 组 PHP 测试通过。

## 线上部署已通过

- 两个插件文件和数据库升级；
- 线上 `1.5.0` 发布记录、APK、更新清单、下载页和 App Links；
- 公网 live/ready/bootstrap/release、验证码和下载文件校验。

## 明确限制

- 按项目拥有者要求不执行 APK 模拟器测试，由项目拥有者真机验收；
- 当前 APK 使用公开 Beta 签名，只能用于测试；
- 真实短信发送需要项目拥有者提供阿里云生产凭据；
- 工程来自 ZIP 快照且没有 Git 历史。
