# stk_auth 插件安装说明

本目录为 Discuz! X5.0 插件源码骨架。Beta 1 的重点是 API 结构、数据库迁移和开发 Fake 链路，不声称已经在目标生产站点完成安装。

## 推荐安装流程

1. 将 `stk_auth` 目录复制到目标 Discuz 的 `source/plugin/`；
2. 在 Discuz 插件设计器中创建标识符 `stk_auth`；
3. 添加前台模块：标识符 `api`（`api.inc.php`）和 `captcha`（`captcha.inc.php`）；
4. 添加后台模块：
   - `admin/overview.inc.php`
   - `admin/settings.inc.php`
5. 执行 `install.php` 或升级脚本，确认认证、验证码、令牌、登录日志及账户表已创建；
6. 默认关闭开发 Fake；只有受控测试环境可将 `dev_fake_enabled` 设为 `1`；
7. 在“认证设置”中确认 `public_base_url` 为客户端可访问的 HTTPS 地址；服务器必须启用 PHP GD 扩展。
8. 访问：

```text
plugin.php?id=stk_auth:api&action=health
```

## 注意

- XML 文件是导入起点，最终应在目标 Discuz 后台重新导出一次安装包；
- 生产环境不得开启开发 Fake；
- 密码登录和注册已接入 Discuz 用户体系；生产环境务必先备份数据库并在测试站验证升级脚本；
- 不修改 Discuz 核心文件。
