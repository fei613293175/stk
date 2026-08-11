# V1.4.0 Backup And Rollback

生产备份：`/opt/stk-build/backups/stk-v140-predeploy-20260812022643`

- 数据库：`discuz_x5`，`1613125` bytes。
- 数据库 SHA-256：`e6c346410a6cbcd262b29aeda812139dbee446dd29e7c535f47f86c581d97b43`。
- 包含 `stk_auth`、`stk_project`、`.well-known` 和 `stk-release`。
- 服务器备份目录总数保持 5，构建工作区 4 份，均未超过上限。

最终错误文案修复前另有增量回退点：`/opt/stk-build/backups/stk-v140-pre-errorfix-20260812044153`。服务器当前仅保留 2 份 V1.4 备份，未超过 5 份上限。

回滚顺序：

1. 开启受控维护窗口并停止新发布操作。
2. 从同一备份点恢复两个插件目录、`.well-known` 与 `stk-release`，保持生产属主和权限。
3. 恢复 `database.sql`，不得只恢复代码或只恢复数据库。
4. 在 `obx-test` 执行 PHP 语法、合同测试和健康检查。
5. 核对当前发布、APK SHA、live/ready 和 Android 启动后再解除维护。

禁止把旧 V1.5 发布记录误删；它们保留用于历史审计和人工回滚判断。
