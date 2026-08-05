# 10 域名、部署和管理员交付

## 1. 域名规划

| 域名 | 用途 | DNS版本 | 服务版本 | 项目所有者动作 |
|---|---|---|---|---|
| stk.zz-yihao.com | 现有公共入口、移动下载/分享落地、App Links | EXISTING | V1.4.0 | 保留现有解析；V1.4 部署移动落地页与 /.well-known/assetlinks.json |
| stk-api.zz-yihao.com | Android JSON API | V1.0.0 | V1.0.0 | 已由项目所有者解析；开发时验证 DNS、TLS、Host 路由和健康检查 |
| stk-admin.zz-yihao.com | Discuz 与插件管理后台 | V1.0.0 | V1.0.0 | 已由项目所有者解析；后台账号为 `admin`，密码只从本机交接文件读取 |
| stk-static.zz-yihao.com | 项目图片与缩略图 | V1.2.0 | V1.2.0 | V1.2 根据存储/反向代理真实目标解析 |
| stk-download.zz-yihao.com | 正式 APK 下载与校验 | V1.4.0 | V1.4.0 | V1.4 解析到不可变发布目录或受控下载服务 |
| stk-pay.zz-yihao.com | 未来第三方 H5 支付回跳保留 | FUTURE_RESERVED | OUT_OF_SCOPE | 本五版本不解析、不部署、不写支付业务 |

## 2. 当前线上环境基线

- Discuz! X5.0 根目录：`/www/wwwroot/stk_zz_yihao_com`。
- 公共网站：`https://stk.zz-yihao.com`。
- 线上服务器已有 Android 构建环境；`stk-api`、`stk-admin` 子域名已解析，可按需使用。
- 阿里云短信等 Secret 由项目所有者后续填写；未填写前不得把模拟短信结果宣称为真实发送成功。

## 3. 解析原则

Codex 第一次接触服务器时只读盘点系统、CPU、内存、磁盘、Docker、反向代理、数据库、端口、现有站点、PHP、SSL 和部署目录。获得真实公网 IP 或 CNAME 目标后生成 `DNS_ACTION_REQUIRED.md`；不得猜测解析目标。

每个需要上线的域名必须有：DNS 记录、TLS 证书、Host 路由、健康检查、访问控制、日志、回滚和 Owner 验收。API 与后台在 V1.0 同版交付，不能只交 APK 而后端地址不可用。

## 4. V1.0 强制交付

以下是发布前必须取得的实测证据，不代表本地当前已连接线上服务器验证；当前线上就绪状态来自项目所有者提供。

- `stk-api.zz-yihao.com`：`/healthz`、`/readyz` 和 `/v1/bootstrap` 可访问。
- `stk-admin.zz-yihao.com`：Discuz 后台与插件菜单可访问，HTTPS 有效。
- 管理员账号 `admin` 与密码在本机 `ADMIN_ACCESS_HANDOFF.local.md` 交付；密码不得进入版本合同或远程制品。
- 提供部署 Commit、容器/目录、数据库版本、插件版本、健康检查、回滚命令和日志位置。

## 5. 后续域名

- V1.2：`stk-static.zz-yihao.com` 服务项目图片和缩略图。
- V1.4：`stk-download.zz-yihao.com` 服务不可变 APK；`stk.zz-yihao.com/.well-known/assetlinks.json` 完成 App Links。
- `stk-pay.zz-yihao.com` 仅保留名称，本五版本不解析、不部署、不实现支付。
