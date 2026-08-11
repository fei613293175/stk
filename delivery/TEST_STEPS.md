# 用户测试步骤

1. 在 Android 6.0 或更高版本设备上安装 `app-debug.apk`。
2. 首次打开检查登录默认页、短信登录切换、注册页、验证码弹窗和协议入口。默认 Fake backend 的验证码区域明确显示 `2468`，应输入 `2468`；“安全验证”只是弹窗标题，不是验证码内容。
3. Fake backend 下登录，依次检查首页、搜索、分类、项目详情、图片查看和联系方式弹窗。
4. 检查发布页的空表单、图片选择、校验错误、上传进度与提交结果。
5. 检查我的页、退出确认、我的发布筛选、编辑、下架与删除确认。
6. 通过 ADB 启动系统状态示例：

   `adb shell am start -n com.zzyihao.stk/.MainActivity --es stk_system_state OptionalUpdate`

   可用值：`UpToDate`、`OptionalUpdate`、`ForcedUpdate`、`Downloading`、`InstallBlocked`、`Maintenance`。
7. 在 360、390、412、430dp 宽度设备上分别截图，与离线 `visual/states` 参考图对照。

注意：当前 APK 默认使用 Fake backend，Fixture 项目、金额、用户、日期等不是线上事实。
