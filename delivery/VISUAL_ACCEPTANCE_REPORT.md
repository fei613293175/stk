# STK FAST Android 视觉验收报告

日期：2026-08-07

## 结论

当前代码已按离线设计系统与效果图完成主要页面、公共组件及状态覆盖层实现，Debug/Release 构建和单元测试通过；但 **像素级视觉验收尚未通过**。原因是本机 Android Emulator 无法完成 ADB 握手，设备持续处于 `offline`，因此尚未生成真实运行截图、像素差异图及 50 个阻断状态的逐项证据。

不得将“编译成功”解释为“像素级还原完成”。本报告保留这一缺口，目标应继续保持 active。

## 已完成

- 固定浅色主题、颜色、字号、间距、圆角与控件尺寸 Token。
- 登录、注册、验证码、协议、首页、搜索、分类、详情、图片查看、联系方式、发布、我的发布、个人中心、系统更新与维护状态页面。
- 品牌标识由通用 Material 图标改为按效果图几何绘制的折线箭头。
- Fake 视觉 Fixture 分类名称对齐效果图；真实后端继续通过分类 API 返回数据，未改线上数据库合同。
- 系统反馈页通过 `stk_system_state` Intent extra 提供可重复的视觉状态入口。
- 修复 Fake 登录验证码：完整展示挑战提示、刷新按钮实际重新请求挑战，并在弹窗内保留校验错误。
- 单元测试、Debug APK、Release APK、Release lintVital 均成功。

## 尚未完成的阻断证据

- `visual-smoke-gates.csv` 中 50 个代表状态的真实运行截图。
- 360/390/412/430dp 四个宽度的布局截图。
- 参考图与实际截图的像素差异图、阈值和人工复核记录。
- Android instrumentation/Compose UI test 的真实设备执行结果。

## 模拟器诊断事实

- 新建冷启动 AVD：`stk-visual-clean`。
- Android 36 AOSP ATD 镜像存在且声明 emulator 依赖 `35.4.9`。
- 当前 emulator 为 `37.1.11`，SDK 同时报告 `emulator` 与 `emulator-2` 路径冲突。
- 已排除旧快照、孤立 QEMU 锁、ADB 启动顺序及授权对话。
- 使用硬件加速、软件模拟、SwiftShader、GPU off、`-skip-adb-auth` 均重复得到 `emulator-xxxx offline`。

这表明截图缺失是当前工作站运行时/镜像组合的外部阻断，不是 APK 编译失败。

## APK

- Release SHA-256：`C7E21EA1EC090BCCC8E6D00AF2C986C6E2D1D48CB75B1D1B7C4F5065B2955674`
- Debug SHA-256：`054918E66FB3603572A7520E7334BFBB7110E8F5129F82109C937E0DE9AFFDEA`

当前默认构建使用 Fake backend，仅用于 Fixture 演示与视觉验收，不代表线上后台真实数据。

## 版本追踪风险

`C:\Users\小白\Desktop\stk` 与 `STK_FAST` 当前均不是 Git 仓库，因此没有可核实的 Commit ID。交付中不能伪造 Commit。
