# V1.4.0-beta.1 最终验收

1. APK 为 `com.zzyihao.stk / 10401 / 1.4.0-beta.1`，与 V1.3 签名一致并可覆盖安装。
2. `fast-check`、PHP/合同、Android JVM 单测和 `assembleRelease` 在 `obx-test` 通过。
3. 最终 Commit 仅运行一次 Fast CI，CI 构建 Release Beta APK，其 SHA 与最终交付/生产一致。
4. 可选更新可稍后；强制更新不可绕过；网络失败可重试。
5. 下载只允许官方 HTTPS 白名单，有大小上限、字节进度和 SHA-256 校验；不匹配不安装。
6. 未知来源授权、FileProvider 和 Android 系统安装器路径可用，不静默安装。
7. 维护、离线、服务恢复、系统错误和关于页状态均使用真实配置/发布服务。
8. 生产 schema `14001`、10/10 合同配置、发布 API、live/ready、APK 与 App Links 完成。
9. 主域名端到端可用；未完成子域 DNS/TLS 必须如实列出，不虚报。
10. 真机安装、真实点击/输入/返回/滚动、截图、崩溃/ANR/日志完成；设备归 HOME 并释放队列锁。
11. 桌面 `商推客交付/1.4.0-beta.1/` 包含要求的 APK、SHA、Commit、功能、后端、后台、测试、视觉、DNS/TLS、签名和回滚资料。
