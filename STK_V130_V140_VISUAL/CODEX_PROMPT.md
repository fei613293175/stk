继续当前商推客仓库。本次不是重写项目，而是连续执行 V1.3.0 与 V1.4.0 的详细视觉开发。

先完整读取：
1. STK_V130_V140_VISUAL/00_READ_ME_FIRST.md
2. STK_V130_V140_VISUAL/CURRENT_VISUAL_TASK_V1.3.0_V1.4.0.md
3. STK_V130_V140_VISUAL/docs/UI_DESIGN_SYSTEM.md
4. STK_V130_V140_VISUAL/VISUAL_BATCHES_V1.3.0_V1.4.0.csv
5. STK_V130_V140_VISUAL/contracts/V1.3.0_V1.4.0_STATE_MAP.csv
6. STK_V130_V140_VISUAL/CODEX_PROMPT.md

然后运行覆盖包中的安装脚本，将 60 张详细效果图复制到：
- ui-reference/detailed/V1.3.0/
- ui-reference/detailed/V1.4.0/

必须保留当前已经能够构建、登录、浏览、发布和交付 APK 的代码。
不要重置仓库，不要重新初始化 Android 工程，不要重写后端，不要升级 Gradle、AGP、Kotlin、JDK 或 SDK。

先用最多 20 分钟检查当前实现，将 VISUAL_IMPLEMENTATION_STATUS_V1.3.0_V1.4.0.csv 中所有 State ID 标记为：
- ALREADY_MATCHED
- NEEDS_ADJUSTMENT
- NOT_IMPLEMENTED
- NOT_APPLICABLE
并写明理由。

随后严格按批次顺序开发：B30 → B31 → B32 → B33 → B40 → B41 → B42。

V1.3.0 完成后：
- versionName 更新为 1.3.0-beta.1
- versionCode 更新为 10301
- 使用与上一版本相同的 Beta 签名
- 验证可覆盖安装上一版
- 立即交付 APK、Commit、APK SHA-256、功能完成说明与测试步骤

再继续 V1.4.0：
- versionName 更新为 1.4.0-beta.1
- versionCode 更新为 10401
- 继续使用相同 Beta 签名
- 验证可覆盖安装 1.3.0-beta.1
- 立即交付 APK、Commit、APK SHA-256、功能完成说明与测试步骤

只抽查每个页面至少一个代表状态，不要求 60 张图全部自动截图比对。
同类环境或构建问题最多重试两次，总排障超过 30 分钟立即停止，并交付最后一个成功 APK 与单一阻塞报告。
