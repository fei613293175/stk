# stk_project V1.1 安装说明

1. 将 `stk_project` 目录复制到 Discuz `source/plugin/`。
2. 在插件设计器中导入 `discuz_plugin_stk_project_SC_UTF8.xml`，标识符为 `stk_project`。
3. 添加前台模块 `api`，程序文件 `api.inc.php`；后台模块使用 `admin/` 下的五个页面。
4. 执行 `install.php`，确认分类、项目、图片、浏览去重和配置表创建成功。
5. 确保 `stk_auth` 已安装；开发环境需显式开启 `dev_fake_enabled`，生产环境需写入真实 `stk_auth_token`。
6. 通过后台项目管理创建 `published` 测试项目，再从带 Bearer Token 的客户端访问：

```text
plugin.php?id=stk_project:api&resource=categories
plugin.php?id=stk_project:api&resource=projects
plugin.php?id=stk_project:api&resource=projects/1
plugin.php?id=stk_project:api&resource=projects/1/view
```

V1.1 仅实现登录用户的项目浏览；用户发布、图片上传和审核属于 V1.2。生产环境必须启用 HTTPS、真实 Token 校验和域名白名单。
