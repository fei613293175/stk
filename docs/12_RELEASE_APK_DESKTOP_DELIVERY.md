# 12 APK、覆盖更新与本机桌面交付

## 1. 版本号

| versionName | versionCode | 覆盖来源 | APK文件名 |
|---|---|---|---|
| 1.0.0 | 10000 | 首次安装 | 商推客-1.0.0-release.apk |
| 1.1.0 | 10100 | 1.0.0 | 商推客-1.1.0-release.apk |
| 1.2.0 | 10200 | 1.1.0 | 商推客-1.2.0-release.apk |
| 1.3.0 | 10300 | 1.2.0 | 商推客-1.3.0-release.apk |
| 1.4.0 | 10400 | 1.3.0 | 商推客-1.4.0-release.apk |

applicationId 和正式签名必须保持不变。APK 必须是 Universal Release APK，真实内容大于等于 10MB；禁止添加垃圾文件、重复图片或随机数据凑体积。

## 2. 精确来源

CI 测试的 APK 与交付 APK 必须是同一二进制。Codex 从 GitHub Actions 下载 Artifact 后核对 commit SHA、workflow run、artifact ID、versionName/versionCode、applicationId、签名指纹和 APK SHA-256。禁止下载后在本机重新编译另一份。

## 3. 桌面目录

```text
%USERPROFILE%\Desktop\商推客交付\1.0.0\
%USERPROFILE%\Desktop\商推客交付\1.1.0\
%USERPROFILE%\Desktop\商推客交付\1.2.0\
%USERPROFILE%\Desktop\商推客交付\1.3.0\
%USERPROFILE%\Desktop\商推客交付\1.4.0\
```

每版必须包含合同列出的 13 类文件。GitHub Actions 不能直接写用户本机桌面，因此流程是：远程 CI 生成并验收 → Codex 本机下载精确 Artifact → 本机脚本复核哈希与来源 → 复制到桌面。不得声称云端自动写入本机。

## 4. Owner 测试清单

每版 `OWNER_TEST_CHECKLIST.md` 按页面和 Feature 给出：准备条件、操作步骤、预期结果、失败时收集内容。V1.1 起必须在已安装上一版的手机上直接覆盖安装，验证登录态、缓存、数据库和配置。

## 5. 管理员凭据

V1.0.0 桌面目录额外包含 `ADMIN_ACCESS_HANDOFF.local.md`，但该文件不进入 ZIP Artifact、Git、CI 或聊天。正式 APK 签名材料同样只保存在项目所有者安全位置。
