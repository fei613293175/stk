# 交给 Codex 的直接指令

```text
继续当前商推客仓库。本次不是重新开发功能，而是执行 V1.0.0 Beta 2 的详细视觉完善。

先完整读取：
1. STK_V1_VISUAL/00_READ_ME_FIRST.md
2. STK_V1_VISUAL/CURRENT_VISUAL_TASK.md
3. STK_V1_VISUAL/docs/UI_DESIGN_SYSTEM.md
4. STK_V1_VISUAL/VISUAL_BATCHES.csv
5. STK_V1_VISUAL/contracts/V1.0.0_STATE_MAP.csv

然后运行安装脚本，把 67 张 V1.0.0 详细效果图复制到仓库的 ui-reference/detailed/V1.0.0/。

必须保留当前已经能运行、能登录、能注册、能退出和能构建 APK 的代码。不要重置仓库，不要重新搭建环境，不要重写后端，不要升级 Gradle/AGP/Kotlin/JDK/SDK，不要恢复 API 37、全量模拟器、67 图自动截图或生产发布门禁。

先用最多 15 分钟审计当前实现，将 67 个 State ID 标记为“已符合、需调整、尚未实现或不适用说明”。随后按 B00→B05 顺序逐批修改。效果图只决定 UI、布局、字号、圆角、边距、高度、颜色和状态反馈；功能与数据仍以当前开发文档和现有后端为准，效果图里的示例文案或数据不得变成新增功能。

每完成一个批次，只运行现有 fast-check、单元测试和 assembleDebug。不要等待模拟器，不要为每张图提交一次，不要新增视觉 CI。全部 67 状态处理完成后，生成可覆盖安装的 1.0.0-beta.2（versionCode 10002）APK并立即交付，同时提供 VISUAL_IMPLEMENTATION_STATUS.csv、APK SHA-256、Commit 和 Owner 测试步骤。

同类环境或构建错误最多重试两次，总排障超过 30 分钟立即停止循环，交付最后一个成功 APK和单一阻塞报告。
```
