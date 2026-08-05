# 商推客 1.0.0 开发规格

## 1. 版本身份

- release_id：`V1.0.0`
- versionName / versionCode：`1.0.0 / 10000`
- 分支：`release/1.0.0`
- Tag：`stk-android-v1.0.0`
- APK：`商推客-1.0.0-release.apk`
- applicationId：`com.zzyihao.stk`
- 摘要：交付可覆盖安装的首个原生 APK、完整登录注册插件、管理员后台、基础项目列表/详情和我的基础资料。

## 1A. V1.0.0 环境执行硬门禁

- 本版本 Android 构建、签名、PHP/Discuz 测试、模拟器验收、部署和 Release Artifact 必须全部在线上服务器或 GitHub Actions 执行。
- 本机禁止安装、下载、配置或运行 JDK、Android SDK、Gradle、AGP、PHP、Discuz、模拟器和签名工具来替代线上环境。
- 本机仅可编辑源码、运行不依赖构建运行时的合同/静态检查和整理线上证据；本机任何 APK 不得作为交付物。
- 线上环境复用优先：先只读盘点服务器已有 JDK/Gradle/Android SDK/模拟器/PHP/Discuz/缓存；满足合同即复用，JDK 17+ 均可，已有 JDK 21 优先。仅在缺失且不影响现有业务时使用独立目录、容器或 volume 补齐；禁止替换系统运行时、改 Nginx/PHP-FPM/MySQL/Discuz 配置、占用业务端口或重启业务服务，并记录路径、版本和验证证据。

## 2. 线上环境基线

- Discuz! X5.0 根目录：`/www/wwwroot/stk_zz_yihao_com`；公共网站：`https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析；线上服务器已有 Android 构建环境，可按需使用。
- 后台账号为 `admin`；密码仅从本机 `ADMIN_ACCESS_HANDOFF.local.md` 读取，不写入版本合同、Git、CI、日志或聊天。
- 阿里云短信等配置由项目所有者后续填写；未填写前使用 Adapter/Fake 验证，不把模拟结果当作真实短信成功。

## 3. 开始门禁

1. `CURRENT_RELEASE.yaml` 当前版本必须为 `V1.0.0`。
2. 本版 `PAGE_STATE_MOCKUP_BINDINGS.csv` 的所有图片必须存在、Manifest 状态 APPROVED、SHA-256 匹配。
3. 分支和版本号必须正确；仓库 remote 必须是固定地址。
4. 只运行一次全量预检，然后立即编码；不得重新规划已锁定页面、技术和 UI。

## 4. Feature 纵向范围

| Feature | 价值 | 页面 | API | 数据 | 后台 | 明确排除 |
|---|---|---|---|---|---|---|
| FND-001 Android 原生底座与统一设计系统 | 打开即显示本地原生页面，所有页面使用统一视觉参数与组件。 | DS-001;DS-002;DS-003;HOME-001;ME-001;COM-OV-002 | 本地/后台 | 无 | 无 | 内部业务 WebView;动态颜色;暗色模式;横屏和平板专用布局 |
| FND-002 启动、配置和版本化路由 | 启动无白屏，按维护、更新、登录和缓存状态进入唯一正确页面。 | SYS-001;SYS-002 | getBootstrap;healthLive;healthReady | DB-SYS-003 | 无 | 无 |
| AUTH-001 手机号密码登录 | 用户通过手机号和密码安全登录 Discuz 账号。 | AUTH-001;AUTH-OV-002 | loginWithPassword;getMySummary | DB-AUTH-001;DB-AUTH-004;DB-AUTH-005;DB-AUTH-006;DB-SYS-003 | ADM-AUTH-001;ADM-AUTH-003;ADM-AUTH-008;ADM-AUTH-009;ADM-AUTH-010;ADM-AUTH-011 | 无 |
| AUTH-002 手机号短信验证码登录 | 用户经阿里云短信验证码登录并完成手机号持有验证。 | AUTH-001;AUTH-OV-001;AUTH-OV-002 | sendSmsCode;loginWithSms | DB-AUTH-001;DB-AUTH-002;DB-AUTH-004;DB-AUTH-005;DB-AUTH-006;DB-SYS-003 | ADM-AUTH-006;ADM-AUTH-007;ADM-AUTH-009;ADM-AUTH-011;ADM-AUTH-014 | 无 |
| AUTH-003 不发短信的手机号注册 | 用户只输入手机号、密码、确认密码即可注册，创建真实 Discuz UID 和基础账户。 | AUTH-002 | registerUser | DB-AUTH-001;DB-AUTH-004;DB-AUTH-005;DB-ME-001;DB-ME-002;DB-SYS-003 | ADM-AUTH-004;ADM-AUTH-008;ADM-AUTH-013 | 注册短信验证码;公开展示手机号作为用户名 |
| AUTH-004 动作绑定安全验证码 | 登录、注册、短信发送和密码重置提交前均必须通过一次一用安全验证码。 | AUTH-OV-001 | getCaptchaChallenge;verifyCaptcha | DB-AUTH-003;DB-AUTH-006 | ADM-AUTH-005;ADM-AUTH-011 | 无 |
| AUTH-005 找回与重置密码 | 用户通过短信和安全验证码重置密码，旧令牌全部撤销。 | AUTH-003;AUTH-OV-001 | sendSmsCode;resetPassword | DB-AUTH-002;DB-AUTH-004;DB-AUTH-005;DB-AUTH-006 | ADM-AUTH-003;ADM-AUTH-006;ADM-AUTH-007;ADM-AUTH-009 | 无 |
| AUTH-006 Token 轮换与退出登录 | 登录态可安全续期、撤销并在覆盖安装后保持兼容。 | ME-OV-001 | refreshToken;logout | DB-AUTH-004;DB-AUTH-008 | ADM-AUTH-003;ADM-AUTH-010;ADM-AUTH-013 | 无 |
| AUTH-007 用户协议与隐私政策 | 登录注册可查看版本化协议，注册记录同意版本。 | AUTH-004;AUTH-005 | getLegalDocument | 无 | ADM-AUTH-012 | 无 |
| AUTH-008 认证后台与审计 | 管理员可配置、查询、诊断认证链路，敏感操作完整审计。 | AUTH-OV-002 | 本地/后台 | DB-AUTH-005;DB-AUTH-006;DB-AUTH-007;DB-AUTH-008 | ADM-AUTH-001;ADM-AUTH-002;ADM-AUTH-003;ADM-AUTH-004;ADM-AUTH-005;ADM-AUTH-006;ADM-AUTH-007;ADM-AUTH-008;ADM-AUTH-009;ADM-AUTH-010;ADM-AUTH-011;ADM-AUTH-012;ADM-AUTH-013;ADM-AUTH-014 | 无 |
| HOME-001 首页项目信息流 | 用户查看后台允许发布的项目，支持稳定游标分页。 | HOME-001 | listProjectCategories;listProjects | DB-PROJ-001;DB-PROJ-002 | ADM-PROJ-001;ADM-PROJ-002;ADM-PROJ-003;ADM-PROJ-004 | 无 |
| HOME-003 项目详情与浏览统计 | 用户查看项目完整信息，浏览量去重记录。 | HOME-004;HOME-OV-001 | getProjectDetail;recordProjectView | DB-PROJ-002;DB-PROJ-006;DB-PROJ-007 | ADM-PROJ-002;ADM-PROJ-005 | 无 |
| PROFILE-001 我的基础资料 | 展示头像、昵称、UID、会员标识和脱敏手机号。 | ME-001;ME-003 | getMySummary | DB-AUTH-001;DB-ME-001;DB-ME-002 | ADM-AUTH-008 | 无 |
| OPS-001 域名、部署和管理员交付 | 按版本提醒解析、部署 API/后台/静态/下载，并在 V1.0 安全交付管理员入口。 | 运维/后台 | healthLive;healthReady | 无 | ADM-AUTH-014 | 无 |
| OPS-002 GitHub Actions、视觉验收和桌面交付 | 每版同一 commit 的 APK 经模拟器、全部交互和截图验收后复制到桌面。 | 运维/后台 | 本地/后台 | 无 | 无 | 无 |
| OPS-003 安全、性能、日志与诊断 | 认证、上传、项目和发布链路具有限流、审计、健康检查、日志和性能门禁。 | SYS-002 | healthLive;healthReady | DB-AUTH-005;DB-AUTH-006;DB-AUTH-008;DB-SYS-003 | ADM-AUTH-013;ADM-AUTH-014 | 无 |

## 5. 每次上下文恢复必须再次遵守

- 读取本文件、`UI_FIXED_RULES.md`、`FRONTEND_SCOPE.md`、`BACKEND_ADMIN_SCOPE.md` 和直接相关页面合同。
- 任何页面都不得根据个人审美改变字号、圆角、边距、顶部栏、底部导航或色值。
- UI 按批准效果图像素级还原；具体功能按 Feature/Interaction/API，不从图中虚构。
- Android、API、数据库、后台、配置、错误和测试同版闭环。
- 每个按钮有 testTag 和自动测试；禁止无响应、假成功、永久 Mock 和静态假数据。
- 快速执行：多写代码、少做重复验证；开发中只跑受影响测试，版本交付前跑全量。

## 6. GitHub Actions 和交付

当前 Commit 的 APK 必须先通过合同、后端、Android、API 37 模拟器、本版全部新增交互、适用截图和升级安装验收，再从同一 Artifact 下载到本机。复制到 `%USERPROFILE%\Desktop\商推客交付\1.0.0\`，同时交付计划/完成/Owner测试/自动报告/视觉差异/部署/DNS/Build Info/Provenance/SHA/已知问题。

## 7. 版本关闭

仅当本版所有 Feature 状态为 DONE、自动测试通过、部署健康、APK 来源一致、桌面文件齐全且项目所有者完成验收后，才能将 `CURRENT_RELEASE.yaml` 机械推进到下一版本。聊天中的“继续”不能绕过关闭条件。
