# V1.4.0 Final Device Test Report

- 最终设备：Vivo `V2203A`，序列号 `10AC9T177B002T4`
- Android：14 / API 34
- APK：`STK-1.4.0-beta.1.apk`
- 大小：`14611703` bytes
- SHA-256：`6087c2a09743fd121b5d74e16eef7bb16dd28f69e3eb298e574d77fdfb881ff2`
- 包名/版本：`com.zzyihao.stk`, `10401`, `1.4.0-beta.1`
- 最终覆盖安装：PASS，登录与应用数据保留
- 崩溃、ANR、`FATAL EXCEPTION`：0

覆盖与签名：

- Redmi `24094RAD4C` 上从 `1.3.0-beta.1 / 10301` 覆盖安装到同证书 V1.4，安装返回 `Success`，`firstInstallTime` 不变。
- 最终错误文案修复 APK 在 V2203A 上再次覆盖安装，安装返回 `Success`，签名对象、包名和版本保持一致。

真实操作覆盖：

- 冷启动、登录保留、首页缓存与离线进入；关于页检查中、最新版、发现更新与失败回退。
- 可选更新及“稍后”；强制更新及 Back 不可绕过；版本详情、真实字节进度、取消/重试。
- 正确 SHA 下载、错误 SHA 拒绝且不保留可安装缓存；断网下载失败显示脱敏通用文案，联网后重试。
- 未知来源专属授权页、授权后 FileProvider 调起 Vivo 系统安装器；没有静默安装。
- 维护模式、维护重试、服务恢复；`/update` App Link。
- 从真实项目“分享项目”得到 `https://stk.zz-yihao.com/project/9`，该 App Link 直接进入项目 9 详情。
- 对页面执行真实点击、输入/状态触发、Back、滚动和关键业务流程，并保留截图与 UI 层级。

最终诊断：

- 当前进程 logcat 无崩溃、ANR、AndroidRuntime 或 `FATAL EXCEPTION`。
- `dumpsys activity exit-info` 仅记录测试触发的强停、覆盖安装和未知来源权限变更，没有 crash/ANR 退出原因。
- 最终页面显示 `当前版本 1.4.0-beta.1` 和“已是最新版”；有效项目 App Link 显示项目 9。

设备释放：

- IME 恢复为 `com.sohu.inputmethod.sogou.vivo/.SogouIME`，字号恢复 `1.0`。
- Wi-Fi/移动数据恢复为 `1/1`，飞行模式为 `0`；未知来源 appop 恢复 `default`。
- `/sdcard/stk-*` 已清空，手机已回到 `com.bbk.launcher2/.Launcher` 主界面。
- 释放前最终状态为 `device`；票据已改名 `.ticket.done`，锁归档为 `active.lock.released-stk-v140-final-20260812-045654`。
- 锁释放后未再发送 ADB 指令。
