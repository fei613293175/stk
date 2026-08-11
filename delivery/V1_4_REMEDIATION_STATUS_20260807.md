# V1.4 代码完善状态（2026-08-07）

> 历史记录：本报告中的阻塞项已在 V1.4.1 构建、部署与验收中解决。当前事实以 `deliveries/1.4.1/DEPLOYMENT_REPORT.md` 为准。

## 本轮已完成

- 发布页图片操作收敛为独立操作面板；封面、前移、后移与移除不再在小图片卡内重叠。
- 修复草稿图片排序：编辑器当前顺序会直接重编号，移动或设封面不会再被旧 `sortOrder` 恢复。
- Discuz 认证补齐服务端协议文档、生产图形验证码、公开 HTTPS 地址配置与后台 GD 状态提示。
- 图形验证码挑战使用 128 位随机标识，并绑定 IP 与场景；5 分钟过期、同挑战失败 5 次锁定、同 IP/场景每分钟最多创建 5 次。验证码答案不出现在生产 JSON 响应。
- 修复发布维护脚本备份目录层级，备份与回滚都复制 `release/` 的内容。

## 已通过验证

- `python scripts/validate-kit.py`：PASS。
- PHP 7.4：所有 `discuz/**/*.php` 语法检查通过。
- PHP 测试：`project_contract_test`、`dev_auth_service_test`、`auth_contract_test`、`auth_service_unit_test` 均通过。
- 图片排序定向 Kotlin/JUnit：`ProjectDraftImagesTest` 3/3 通过。该验证仅覆盖纯 Kotlin 图片排序模型，不等同于 Android 整包构建。
- `release-maintenance.ps1` 状态、enable dry-run、backup dry-run 通过；隔离临时目录中的实际备份→显式回滚也通过。

## 尚未完成，不能宣称交付

- 本轮源码尚未完成 Android Gradle 单测、Debug/Release 构建，因 Gradle 8.13 在 Windows/JDK 文件系统关闭自身 JAR 时出现 `AccessDeniedException`；构建尚未进入 Kotlin 编译。
- 为验证是否是沙箱限制而申请的本机构建权限，审批服务返回 404，命令未执行；未采用规避方式。
- 因无法重新构建，`deliveries/1.4.0/STK-1.4.0.apk` 是本轮改动前的旧 APK，不能作为本轮交付或更新 `update.json` 的依据。
- 未部署到线上 Discuz；真实 HTTPS、GD、数据库迁移、API、App Links 及真机流程均未在线验证。
- 隔离维护脚本测试目录因本机删除命令被审批服务拒绝而留在系统临时目录；它不在项目或发布目录中。

## 后续恢复顺序

1. 在可用 JDK 17/Gradle 环境运行：`./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease -PstkUseFakeBackend=false`。
2. 校验新 APK 版本、V1/V2 签名和 SHA-256，再更新下载页、`release/update.json` 与交付目录。
3. 先在测试 Discuz 执行 `stk_auth/upgrade.php`，确认 `pre_stk_auth_captcha` 表和 PHP GD/HTTPS 配置；再进行真实 API 验证。
4. 完成真机/模拟器关键流程及 App Links 验收后，才可关闭目标。
