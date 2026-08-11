# DNS Action Required

2026-08-12 只读核验结果：

| Host | Required record | Value | Initial proxy | Current state |
| --- | --- | --- | --- | --- |
| `stk.zz-yihao.com` | A | `103.96.149.219` | 保持现状 | 已解析，TLS/HTTP 正常 |
| `stk-api.zz-yihao.com` | A | `103.96.149.219` | DNS-only | 已解析，但当前为自签名 TLS |
| `stk-admin.zz-yihao.com` | A | `103.96.149.219` | DNS-only | 已解析，但当前为自签名 TLS |
| `stk-static.zz-yihao.com` | A | `103.96.149.219` | DNS-only | NXDOMAIN，需所有者创建 |
| `stk-download.zz-yihao.com` | A | `103.96.149.219` | DNS-only | NXDOMAIN，需所有者创建 |

记录值来自当前三个已解析 STK 主机和服务器只读核验，不是猜测。所有者需在 DNS 提供商创建缺失记录；确认源站证书、Nginx vhost 和健康检查正常后，再决定是否启用代理。

验证命令：

```bash
dig +short stk-static.zz-yihao.com A
dig +short stk-download.zz-yihao.com A
curl -fsS https://stk-api.zz-yihao.com/health/live
curl -fsSI https://stk-download.zz-yihao.com/app/STK-1.4.0-beta.1.apk
```

DNS/TLS 未完成不阻断当前 Beta APK；当前发布暂由 `stk.zz-yihao.com` 的有效 HTTPS 地址承载。
