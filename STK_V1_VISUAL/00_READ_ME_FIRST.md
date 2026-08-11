# 商推客 V1.0.0 详细视觉完善覆盖包

本包用于已经完成 `1.0.0-beta.1` 基础功能后的**视觉完善阶段**。

## 内容

- V1.0.0 当前应实现的详细视觉状态：**67 张**；
- 页面/状态/交互映射；
- 固定 UI Token 和页面状态规则；
- 6 个小批次，不要求一次性重做全部页面；
- 不包含未来 V1.1.0～V1.4.0 的效果图，避免当前开发范围膨胀。

## 使用原则

1. 这是“现有代码视觉校正包”，不是重新初始化工程的开发包。
2. 不重写已经正常工作的认证、Token、Discuz 插件、Gradle、CI 和签名。
3. 67 张图是状态参考，不是 67 个独立功能，也不是 67 次 CI 门禁。
4. 功能以现有 `CURRENT_TASK.md`、接口和后端实现为准；图片中的手机号、用户名、金额、日期、头像和项目内容均为示例数据。
5. 先统一公共 Token/组件，再按页面批次校正；禁止每张图单独写一套组件。
6. 每完成一个批次，只运行快速编译和现有单元测试；不要启动 API 37，不要新增全量截图工作流。
7. 全部完成后生成 `1.0.0-beta.2`（建议 `versionCode=10002`），立即交付 APK。

## 推荐放入仓库的位置

将本包解压到仓库根目录后，运行：

Windows：

```powershell
powershell -ExecutionPolicy Bypass -File .\STK_V1_VISUAL\scripts\install-visual-overlay.ps1 -RepoPath .
```

Linux：

```bash
bash ./STK_V1_VISUAL/scripts/install-visual-overlay.sh .
```

脚本只会复制视觉资料和任务文件，不会覆盖业务代码、AGENTS.md 或 CI。
