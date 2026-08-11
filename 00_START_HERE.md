# 商推客 V1.4.0-beta.1 交付入口

当前任务是从已交付 V1.3.0 增量完成 V1.4.0，并停止在 `1.4.0-beta.1 / 10401`。不开发 V1.5 或文档外功能。

执行依据：

1. `CURRENT_TASK.md`
2. `CURRENT_ACCEPTANCE.md`
3. `STK_V130_V140_VISUAL/00_READ_ME_FIRST.md`
4. `STK_V130_V140_VISUAL/CURRENT_VISUAL_TASK_V1.3.0_V1.4.0.md`
5. `STK_V130_V140_VISUAL/contracts/`

当前发布包必须保持：

- `applicationId = com.zzyihao.stk`
- `versionName = 1.4.0-beta.1`
- `versionCode = 10401`
- 沿用 V1.2/V1.3 同一 Beta 签名
- 构建、非设备测试和部署只在 `obx-test`
- 真机仅在队列锁和 `get-state=device` 后使用

交付目录：`deliveries/1.4.0-beta.1/`。
