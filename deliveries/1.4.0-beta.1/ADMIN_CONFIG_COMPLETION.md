# V1.4.0 Admin And Config Completion

已实现并部署 10 个点号合同配置：

1. `release.current_version`
2. `release.minimum_version`
3. `release.force_update`
4. `release.download_url`
5. `release.apk_sha256`
6. `release.notes`
7. `maintenance.enabled`
8. `maintenance.message`
9. `maintenance.expected_end`
10. `app_links.host`

Discuz 后台已完成 Android 版本发布、维护与运行配置、域名/部署诊断、安全审计。发布校验覆盖语义版本、最低版本关系、HTTPS、下载白名单、SHA-256、APK 大小和发布状态；发布/维护/诊断操作写入审计日志。

生产配置核验为 10/10，当前发布记录为 `published, enabled=1`。
