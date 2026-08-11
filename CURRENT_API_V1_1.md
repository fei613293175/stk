# V1.1 项目浏览 API

真实接口默认地址：`https://stk.zz-yihao.com/plugin.php?id=stk_project:api`。

所有响应使用 `{code,message,data,request_id}`。除插件关闭提示外，项目接口均要求 `Authorization: Bearer <access_token>`。开发 Token 仅在 `stk_auth.dev_fake_enabled=1` 时接受；真实 Token 按哈希、类型、撤销状态和过期时间校验。

| resource | 方法 | 用途 |
|---|---|---|
| `categories` | GET | 分类列表 |
| `projects` | GET | 首页、搜索、分类、游标分页；参数 `q`、`category_id`、`cursor` |
| `projects/{id}` | GET | 项目详情、图片、发布者和联系方式 |
| `projects/{id}/view` | POST | 记录浏览；服务端按项目/日期/IP/User-Agent/Token 去重并返回 `recorded` |

Fake 模式提供 8 个示例项目、4 个分类、分页和缓存回退。
