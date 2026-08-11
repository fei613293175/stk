# 交给 Codex 的直接指令

```text
继续当前商推客仓库，当前只执行 V1.1.0“项目浏览”。不要重新初始化仓库，不要重新规划五个版本。

先读取：
1. STK_V110_VISUAL/00_READ_ME_FIRST.md
2. STK_V110_VISUAL/CURRENT_VISUAL_TASK_V1.1.0.md
3. STK_V110_VISUAL/docs/UI_DESIGN_SYSTEM.md
4. STK_V110_VISUAL/VISUAL_BATCHES_V1.1.0.csv
5. STK_V110_VISUAL/contracts/V1.1.0_STATE_MAP.csv
6. STK_V110_VISUAL/contracts/V1.1.0_FEATURE_BACKEND_MAP.csv

然后运行安装脚本，将 45 张批准效果图复制到：
ui-reference/detailed/V1.1.0/

必须保留当前已经能运行、登录、注册、退出、构建 APK 和覆盖安装的 V1.0.0 代码、签名、Gradle、AGP、Kotlin、JDK、SDK、数据库和 CI。不要恢复 API 37、全量模拟器、45 图自动截图、正式签名轮换、DNS/TLS 或生产发布门禁。

本版只实现：
- 首页项目流、刷新、游标分页和离线缓存；
- 搜索和分类；
- 项目详情和图片查看；
- 手机、微信、QQ、网址联系方式；
- 受控外链和未安装回退；
- 对应 stk_project 浏览 API 与后台配置。

不得实现用户发布、图片上传、审核、收藏、评论、私聊、会员购买、返佣或支付。效果图中的项目文案、头像、浏览量、号码和日期都是示例数据，不能据此扩展功能。

执行分两阶段：
A. 先完成首页正常内容→项目详情→搜索/分类→图片查看→联系方式复制的最小链路，第一次 assembleDebug 成功后立即交付 1.1.0-beta.1 APK，不等待 45 状态全部完成。
B. 再按 B01→B06 逐批完成 45 个状态，交付 beta.2/正式版。

先用最多 15 分钟审计当前实现，更新 VISUAL_IMPLEMENTATION_STATUS_V1.1.0.csv。不同状态必须由同一 Screen + UI State + 公共组件实现，不得按图片复制页面。每完成一个批次只运行现有 fast-check、单元测试和 assembleDebug；不要每张图提交一次，不要新增视觉 CI。

同类环境或构建错误最多重试两次，总排障超过 30 分钟立即停止循环，交付最后一个成功 APK和单一阻塞报告。

交付时提供：APK、SHA-256、Commit、功能完成清单、45 状态完成表、已知问题和项目所有者测试步骤。versionCode 必须高于已安装版本，确保直接覆盖安装。
```
