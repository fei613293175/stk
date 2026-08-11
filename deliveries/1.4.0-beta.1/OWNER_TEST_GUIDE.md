# V1.4.0 Owner Test Guide

1. 核对 APK SHA-256、包名、版本号和签名报告。
2. 从已安装 V1.3.0 执行覆盖安装，确认未清除应用数据。
3. 启动检查无更新、可选更新、强制更新和维护模式；强制更新不得绕过。
4. 在更新页测试下载、进度、取消、断网失败、重试、SHA 校验和等待安装。
5. 未授权时检查“安装未知应用”引导；授权后确认打开 Android 系统安装器，不能静默安装。
6. 在关于页点击检查更新，验证检查中、最新版、发现更新和失败状态。
7. 用已验证的 `https://stk.zz-yihao.com/project/9` 与 `https://stk.zz-yihao.com/update` 验证 App Links。
8. 检查主域名、发布 API、live/ready、APK 与 assetlinks.json。
9. 检查崩溃、ANR、Fatal Exception 和明显网络/安装异常日志。

不要把效果图中的示例账号、金额、日期或手机号作为生产数据断言。
