# 商推客 1.4.0 开发规格

## 1. 版本身份

- release_id：`V1.4.0`
- versionName / versionCode：`1.4.0 / 10400`
- 分支：`release/1.4.0`
- Tag：`stk-android-v1.4.0`
- APK：`商推客-1.4.0-release.apk`
- applicationId：`com.zzyihao.stk`
- 摘要：完成更新检查、下载分发、深链、维护模式、安全性能加固和五版本全链路终验。

## 2. 线上环境基线

- Discuz! X5.0 根目录：`/www/wwwroot/stk_zz_yihao_com`；公共网站：`https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析；线上服务器已有 Android 构建环境，可按需使用。
- 后台账号为 `admin`；密码仅从本机 `ADMIN_ACCESS_HANDOFF.local.md` 读取，不写入版本合同、Git、CI、日志或聊天。
- 阿里云短信等配置由项目所有者后续填写；未填写前使用 Adapter/Fake 验证，不把模拟结果当作真实短信成功。

## 3. 开始门禁

1. `CURRENT_RELEASE.yaml` 当前版本必须为 `V1.4.0`。
2. 本版 `PAGE_STATE_MOCKUP_BINDINGS.csv` 的所有图片必须存在、Manifest 状态 APPROVED、SHA-256 匹配。
3. 分支和版本号必须正确；仓库 remote 必须是固定地址。
4. 只运行一次全量预检，然后立即编码；不得重新规划已锁定页面、技术和 UI。

## 4. Feature 纵向范围

| Feature | 价值 | 页面 | API | 数据 | 后台 | 明确排除 |
|---|---|---|---|---|---|---|
| SYS-001 App 更新与覆盖安装 | App 检查、下载、校验并交给系统安装器，保持同包名同签名覆盖更新。 | SYS-003;SYS-OV-001;SYS-OV-002;ME-012 | getCurrentRelease | DB-SYS-002 | ADM-SYS-001;ADM-SYS-002;ADM-SYS-003;ADM-SYS-004 | 无 |
| SYS-002 App Links 与下载落地 | stk.zz-yihao.com 项目链接打开 App，未安装时进入移动下载落地页。 | SYS-001;SYS-003 | resolveAppLink | DB-SYS-001;DB-SYS-002 | ADM-SYS-003 | 无 |

## 5. 每次上下文恢复必须再次遵守

- 读取本文件、`UI_FIXED_RULES.md`、`FRONTEND_SCOPE.md`、`BACKEND_ADMIN_SCOPE.md` 和直接相关页面合同。
- 任何页面都不得根据个人审美改变字号、圆角、边距、顶部栏、底部导航或色值。
- UI 按批准效果图像素级还原；具体功能按 Feature/Interaction/API，不从图中虚构。
- Android、API、数据库、后台、配置、错误和测试同版闭环。
- 每个按钮有 testTag 和自动测试；禁止无响应、假成功、永久 Mock 和静态假数据。
- 快速执行：多写代码、少做重复验证；开发中只跑受影响测试，版本交付前跑全量。

## 6. GitHub Actions 和交付

当前 Commit 的 APK 必须先通过合同、后端、Android、API 37 模拟器、本版全部新增交互、适用截图和升级安装验收，再从同一 Artifact 下载到本机。复制到 `%USERPROFILE%\Desktop\商推客交付\1.4.0\`，同时交付计划/完成/Owner测试/自动报告/视觉差异/部署/DNS/Build Info/Provenance/SHA/已知问题。

## 7. 版本关闭

仅当本版所有 Feature 状态为 DONE、自动测试通过、部署健康、APK 来源一致、桌面文件齐全且项目所有者完成验收后，才能将 `CURRENT_RELEASE.yaml` 机械推进到下一版本。聊天中的“继续”不能绕过关闭条件。
