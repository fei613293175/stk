# 14 性能、安全与可运维性

## Android 性能目标

- 冷启动原生首帧 ≤1.5s；有缓存首页 ≤1.0s 出现内容；导航切换不等待网络。
- 项目列表稳定 60fps 目标；使用缩略图、按需加载和预取；图片解码限制尺寸，避免 OOM。
- 首屏 API p95 ≤800ms（不含用户网络），详情 p95 ≤600ms，写操作 p95 ≤1200ms；超时有明确重试。
- APK 真实大小 10～30MB 目标；R8、资源压缩和 Baseline Profile 在 V1.4 验收。

## 后端

- 连接池、稳定游标、联合索引、批量统计和缓存；避免 Offset 深分页。
- 阿里云短信使用独立超时、并发池、限流和失败告警；外部失败不拖垮认证线程。
- 上传流式处理，不把大文件完整读入 PHP 内存；病毒/图片解码/MIME 检查后再入正式存储。
- 项目写操作强幂等，状态机乐观锁，审计日志不可静默删除。

## 安全

- 全站 HTTPS；后台限制 Host、速率和可选 IP/二次认证。
- 密码使用 Discuz 安全机制；验证码、联系方式和 Secret 加密；查找使用 keyed HMAC。
- CSRF、XSS、SQL 注入、任意文件上传、开放跳转、SSRF、路径穿越、Token 重放和越权必须有自动测试。
- APK 只允许白名单域名；下载后校验 SHA-256、applicationId 和签名指纹。

## 可观测性

日志统一 JSON：`timestamp,level,service,module,operation_id,request_id,uid_hash,error_code,duration_ms`。指标覆盖请求量/失败率/延迟、短信成功率、验证码失败率、登录失败、项目审核积压、上传失败、API 5xx 和版本更新。后台诊断页显示配置状态而不显示 Secret。
