# 复制给 Codex 的启动指令

```text
继续开发商推客，但本次只执行 V1.1.0 项目浏览交付。

不要重置仓库，不要删除当前已经完成的 Android、Discuz、数据库迁移、测试或有效修复。先完整读取：

1. 00_START_HERE.md
2. AGENTS.md
3. RECOVER_EXISTING_REPO.md
4. CURRENT_TASK.md
5. CURRENT_API.md
6. CURRENT_ACCEPTANCE.md

只执行任务 STK-V110，不扩展到 V1.2；不恢复旧包的 API 37、95 个视觉状态、236 图逐图门禁、生产 DNS/TLS 或完整发布证据链。

先用最多 10 分钟检查当前仓库已有成果。现有实现比本包脚手架更完整时必须保留，只补缺失部分。随后运行 scripts/fast-check.sh，并尽快生成第一个 APK。

第一次 assembleDebug 成功后立即运行 scripts/package-beta.sh，把 APK、SHA-256、完成清单、已知问题和 Owner 测试指南放入 deliveries/1.1.0/。不得把无设备的人工流程写成已通过。

同类错误无实质变化最多重试两次；总排障超过 30 分钟立即停止循环，交付最后一个成功 APK并提交单一阻塞报告。禁止继续通过增加规则文件解决问题。
```

补充：仓库已提供官方 Gradle Wrapper，不得再编写自定义 Gradle 下载脚本。Windows 构建完成后必须执行 `scripts/copy-delivery-to-desktop.ps1`。
