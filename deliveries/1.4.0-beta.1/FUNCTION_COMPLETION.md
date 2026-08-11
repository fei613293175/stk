# V1.4.0 Function Completion

- `versionCode 10401 / versionName 1.4.0-beta.1` 已实现。
- 可选更新可稍后进入 App；强制更新禁止绕过业务页。
- 更新页覆盖检查中、最新版、可选/强制可用、下载、失败、等待安装和安装受阻状态。
- APK 仅接受官方 HTTPS 白名单域名，最多 3 次受控跳转，最大 200 MB，并逐字节计算 SHA-256。
- SHA 不匹配和下载不完整均删除 `.part` 文件且禁止安装。
- 未授权安装未知应用时打开应用专属系统设置；授权后用 FileProvider 调起系统安装器，不静默安装。
- 关于页使用真实发布服务检查更新；维护、离线、服务恢复和系统错误使用统一状态页。
- `/project/{id}` 与 `/update` App Links 路由已实现，仅接受 `stk.zz-yihao.com`。

服务端构建、合同测试和生产联调已通过；最终真机终态见 `DEVICE_TEST_REPORT.md`。
