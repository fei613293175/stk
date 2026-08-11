# Domain And TLS Report

核验时间：2026-08-12（Asia/Shanghai）。

- `stk.zz-yihao.com`：A=`103.96.149.219`；首页、下载页、APK、发布 API 和 `assetlinks.json` 均为 HTTPS 200，证书有效。
- `stk-api.zz-yihao.com`：A=`103.96.149.219`；TLS 握手因自签名证书失败，未关闭。
- `stk-admin.zz-yihao.com`：A=`103.96.149.219`；TLS 握手因自签名证书失败，未关闭。
- `stk-static.zz-yihao.com`：NXDOMAIN。
- `stk-download.zz-yihao.com`：NXDOMAIN。

主域名生产健康：

- `health/live`：`code=0, status=live, version=1.4.0`。
- `health/ready`：`code=0, ready=true`。
- 当前发布：`1.4.0-beta.1 / 10401`。

结论：主域名 Beta 发布链路可用；四个规划子域名的 DNS/TLS 关闭项仍需所有者处理，不能报告为全部通过。
