# 商推客 1.1.0 开发规格

## 1. 版本身份

- release_id：`V1.1.0`
- versionName / versionCode：`1.1.0 / 10100`
- 分支：`release/1.1.0`
- Tag：`stk-android-v1.1.0`
- APK：`商推客-1.1.0-release.apk`
- applicationId：`com.zzyihao.stk`
- 摘要：完善项目发现、搜索、分类、游标分页、离线缓存、图片查看和受控外部跳转。

## 2. 线上环境基线

- Discuz! X5.0 根目录：`/www/wwwroot/stk_zz_yihao_com`；公共网站：`https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析；线上服务器已有 Android 构建环境，可按需使用。
- 后台账号为 `admin`；密码仅从本机 `ADMIN_ACCESS_HANDOFF.local.md` 读取，不写入版本合同、Git、CI、日志或聊天。
- 阿里云短信等配置由项目所有者后续填写；未填写前使用 Adapter/Fake 验证，不把模拟结果当作真实短信成功。

## 3. 开始门禁

1. `CURRENT_RELEASE.yaml` 当前版本必须为 `V1.1.0`。
2. 本版 `PAGE_STATE_MOCKUP_BINDINGS.csv` 的所有图片必须存在、Manifest 状态 APPROVED、SHA-256 匹配。
3. 分支和版本号必须正确；仓库 remote 必须是固定地址。
4. 只运行一次全量预检，然后立即编码；不得重新规划已锁定页面、技术和 UI。

## 4. Feature 纵向范围

| Feature | 价值 | 页面 | API | 数据 | 后台 | 明确排除 |
|---|---|---|---|---|---|---|
| HOME-002 搜索与分类筛选 | 用户按标题/简介和分类快速查找项目。 | HOME-002;HOME-003 | listProjectCategories;listProjects | DB-PROJ-001;DB-PROJ-002 | ADM-PROJ-003;ADM-PROJ-007 | 无 |
| HOME-004 首页与详情离线优先缓存 | 弱网或断网仍可立即显示已缓存项目和明确离线状态。 | HOME-001;HOME-002;HOME-004 | listProjects;getProjectDetail | 无 | 无 | 无 |
| HOME-005 联系方式与受控外部跳转 | 用户复制、拨号或打开微信/QQ/白名单网址，失败有回退。 | COM-OV-001;HOME-OV-002 | getProjectDetail | DB-SYS-001 | ADM-PROJ-005;ADM-PROJ-006 | 无 |

## 5. 每次上下文恢复必须再次遵守

- 读取本文件、`UI_FIXED_RULES.md`、`FRONTEND_SCOPE.md`、`BACKEND_ADMIN_SCOPE.md` 和直接相关页面合同。
- 任何页面都不得根据个人审美改变字号、圆角、边距、顶部栏、底部导航或色值。
- UI 按批准效果图像素级还原；具体功能按 Feature/Interaction/API，不从图中虚构。
- Android、API、数据库、后台、配置、错误和测试同版闭环。
- 每个按钮有 testTag 和自动测试；禁止无响应、假成功、永久 Mock 和静态假数据。
- 快速执行：多写代码、少做重复验证；开发中只跑受影响测试，版本交付前跑全量。

## 6. GitHub Actions 和交付

当前 Commit 的 APK 必须先通过合同、后端、Android、API 37 模拟器、本版全部新增交互、适用截图和升级安装验收，再从同一 Artifact 下载到本机。复制到 `%USERPROFILE%\Desktop\商推客交付\1.1.0\`，同时交付计划/完成/Owner测试/自动报告/视觉差异/部署/DNS/Build Info/Provenance/SHA/已知问题。

## 7. 版本关闭

仅当本版所有 Feature 状态为 DONE、自动测试通过、部署健康、APK 来源一致、桌面文件齐全且项目所有者完成验收后，才能将 `CURRENT_RELEASE.yaml` 机械推进到下一版本。聊天中的“继续”不能绕过关闭条件。
