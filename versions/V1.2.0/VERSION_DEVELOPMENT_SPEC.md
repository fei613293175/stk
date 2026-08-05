# 商推客 1.2.0 开发规格

## 1. 版本身份

- release_id：`V1.2.0`
- versionName / versionCode：`1.2.0 / 10200`
- 分支：`release/1.2.0`
- Tag：`stk-android-v1.2.0`
- APK：`商推客-1.2.0-release.apk`
- applicationId：`com.zzyihao.stk`
- 摘要：完成用户发布、编辑、下架、删除、驳回重提、多图上传和后台审核管理。

## 2. 线上环境基线

- Discuz! X5.0 根目录：`/www/wwwroot/stk_zz_yihao_com`；公共网站：`https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析；线上服务器已有 Android 构建环境，可按需使用。
- 后台账号为 `admin`；密码仅从本机 `ADMIN_ACCESS_HANDOFF.local.md` 读取，不写入版本合同、Git、CI、日志或聊天。
- 阿里云短信等配置由项目所有者后续填写；未填写前使用 Adapter/Fake 验证，不把模拟结果当作真实短信成功。

## 3. 开始门禁

1. `CURRENT_RELEASE.yaml` 当前版本必须为 `V1.2.0`。
2. 本版 `PAGE_STATE_MOCKUP_BINDINGS.csv` 的所有图片必须存在、Manifest 状态 APPROVED、SHA-256 匹配。
3. 分支和版本号必须正确；仓库 remote 必须是固定地址。
4. 只运行一次全量预检，然后立即编码；不得重新规划已锁定页面、技术和 UI。

## 4. Feature 纵向范围

| Feature | 价值 | 页面 | API | 数据 | 后台 | 明确排除 |
|---|---|---|---|---|---|---|
| PUBLISH-001 Photo Picker、多图处理与上传 | 用户只授权选中图片，获得压缩、进度、重试、排序和封面能力。 | PUB-001;PUB-002;PUB-OV-001 | uploadProjectImage;deleteUploadedImage | DB-PROJ-003;DB-PROJ-004;DB-SYS-003 | ADM-PROJ-010 | 无 |
| PUBLISH-002 发布项目 | 用户提交标题、简介、图片、分类和联系方式，获得真实审核状态。 | PUB-001;PUB-003;PUB-OV-002;PUB-OV-003 | createProject;listProjectCategories | DB-PROJ-002;DB-PROJ-003;DB-PROJ-004;DB-PROJ-005;DB-SYS-003 | ADM-PROJ-008;ADM-PROJ-009;ADM-PROJ-010 | 无 |
| PUBLISH-003 项目编辑和状态机 | 用户按状态编辑、下架、重提和软删除本人项目。 | PUB-004;ME-OV-002;HOME-004 | updateProject;offlineProject;resubmitProject;deleteProject | DB-PROJ-002;DB-PROJ-005;DB-SYS-003 | ADM-PROJ-002;ADM-PROJ-008;ADM-PROJ-012 | 无 |
| PUBLISH-004 我的发布 | 用户按状态查看和管理本人项目。 | ME-002;ME-OV-002 | listMyProjects;getProjectDetail | DB-PROJ-002;DB-PROJ-005 | ADM-PROJ-011 | 无 |
| PUBLISH-005 项目后台审核闭环 | 审核员可审核、驳回、推荐、上下架和追溯全部动作。 | 运维/后台 | 本地/后台 | DB-PROJ-002;DB-PROJ-005;DB-AUTH-008 | ADM-PROJ-001;ADM-PROJ-002;ADM-PROJ-008;ADM-PROJ-011;ADM-PROJ-012 | 无 |

## 5. 每次上下文恢复必须再次遵守

- 读取本文件、`UI_FIXED_RULES.md`、`FRONTEND_SCOPE.md`、`BACKEND_ADMIN_SCOPE.md` 和直接相关页面合同。
- 任何页面都不得根据个人审美改变字号、圆角、边距、顶部栏、底部导航或色值。
- UI 按批准效果图像素级还原；具体功能按 Feature/Interaction/API，不从图中虚构。
- Android、API、数据库、后台、配置、错误和测试同版闭环。
- 每个按钮有 testTag 和自动测试；禁止无响应、假成功、永久 Mock 和静态假数据。
- 快速执行：多写代码、少做重复验证；开发中只跑受影响测试，版本交付前跑全量。

## 6. GitHub Actions 和交付

当前 Commit 的 APK 必须先通过合同、后端、Android、API 37 模拟器、本版全部新增交互、适用截图和升级安装验收，再从同一 Artifact 下载到本机。复制到 `%USERPROFILE%\Desktop\商推客交付\1.2.0\`，同时交付计划/完成/Owner测试/自动报告/视觉差异/部署/DNS/Build Info/Provenance/SHA/已知问题。

## 7. 版本关闭

仅当本版所有 Feature 状态为 DONE、自动测试通过、部署健康、APK 来源一致、桌面文件齐全且项目所有者完成验收后，才能将 `CURRENT_RELEASE.yaml` 机械推进到下一版本。聊天中的“继续”不能绕过关闭条件。
