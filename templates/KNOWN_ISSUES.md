# V1.5.0 已知限制

- 按项目拥有者要求不执行 APK 模拟器测试；覆盖安装、真实账号流程和视觉体验由项目拥有者在真机验证。
- APK 使用已公开的固定 Beta 签名，支持旧 Beta 覆盖安装，但不得用于正式生产发布。
- App Links 绑定当前 Beta 证书指纹；切换正式签名时必须同步更新 `assetlinks.json`。
- 阿里云短信发送逻辑已实现并可后台配置，但没有生产密钥时不能证明真实短信送达。
- 未来 `stk-download.zz-yihao.com` 尚未启用；当前发布使用 `https://stk.zz-yihao.com/stk-release/`。
- 工程来自 ZIP 快照且没有 Git 历史，交付记录使用服务器构建时间和文件哈希追踪。
