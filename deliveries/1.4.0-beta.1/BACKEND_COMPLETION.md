# V1.4.0 Backend Completion

生产根目录：`/www/wwwroot/stk_zz_yihao_com`

- 数据库 schema：`14001`。
- `pre_stk_release` 支持草稿、发布、停用、最低版本、APK URL/SHA/大小、强制标记、说明与时间字段。
- 生产规范路由 `GET /plugin.php?id=stk_project:api&resource=api/v1/releases/current` 已发布 `1.4.0-beta.1 / 10401`，最低版本为 `1.3.0-beta.1 / 10301`。
- `GET /health/live` 返回插件版本 `1.4.0`；`GET /health/ready` 返回 `ready=true`。
- 版本响应包含维护配置、发布状态、客户端更新/强制比较结果和请求编号。
- 生产异常响应不返回堆栈、数据库凭据或服务器路径。
- V1.4 合同测试、现有项目合同和 PHP 语法检查均通过。

生产 APK URL、大小和 SHA-256 与交付 APK 一致。

当前 Nginx/Discuz 部署没有把裸路径 `/api/v1/releases/current` 映射到插件接口，该裸路径返回 404；Android 使用的是上述规范插件路由，不受影响。
