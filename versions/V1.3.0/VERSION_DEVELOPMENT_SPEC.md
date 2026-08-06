# 商推客 1.3.0 开发规格

## V1.3.0 环境执行硬门禁

- 正式稳定通道的 API 36（Android 16）是本版本唯一允许的 compileSdk、targetSdk、SDK platform、system image 和模拟器版本；API 37、其他非 API 36、Preview 和 Canary 一律禁止，遇到环境问题不得切换版本。
- 门禁分层：`APK_BUILD_GATE` 包含编译、Lint、单元/后端测试、签名和 Artifact 生成，不依赖 API 36 模拟器或 `/dev/kvm`；`EMULATOR_ACCEPTANCE_GATE` 包含 API 36 模拟器、Instrumentation、交互和视觉截图，只能在 GitHub Actions 执行。
- APK 只构建一次并上传同一 Commit Artifact，模拟器任务下载该 Artifact，禁止重复编译。缺少 `/dev/kvm` 或模拟器运行库不得阻塞 APK 构建，也不得改动业务主机或伪造 KVM；切换到 GitHub Actions 的 API 36 runner 并记录证据。模拟器门禁未通过时只能交付构建中间结果，不能宣称完整版本交付。
- Android 构建、签名、PHP/Discuz、部署和交付只允许在线上服务器或 GitHub Actions 执行；API 36 模拟器只允许在 GitHub Actions 执行。
- 本机禁止安装/下载/运行 JDK、Android SDK、Gradle、AGP、PHP、Discuz、模拟器和签名工具；本机仅可编辑源码和运行静态合同检查。
- 线上环境复用优先：先只读盘点已有运行时、镜像、SDK/Gradle 缓存和模拟器；满足合同即复用，JDK 17+ 均可，已有 JDK 21 优先。仅在缺失且不影响业务时用独立目录、容器或 volume 补齐，禁止替换系统运行时、改业务配置、占端口或重启服务，并记录实际路径、版本和验证证据。
- 本版本必须先读取并填写 `ENVIRONMENT_CONTRACT.yaml`；统一规则见 `docs/19_ONLINE_ENVIRONMENT_REUSE_CONTRACT.md`。

## 1. 版本身份

- release_id：`V1.3.0`
- versionName / versionCode：`1.3.0 / 10300`
- 分支：`release/1.3.0`
- Tag：`stk-android-v1.3.0`
- APK：`商推客-1.3.0-release.apk`
- applicationId：`com.zzyihao.stk`
- 摘要：完成我的页面信息展示、会员状态、双账户只读余额、道具展示和所有明确占位页面。

## 2. 线上环境基线

- Discuz! X5.0 根目录：`/www/wwwroot/stk_zz_yihao_com`；公共网站：`https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析；线上服务器已有 Android 构建环境，可按需使用。
- 后台账号为 `admin`；密码仅从本机 `ADMIN_ACCESS_HANDOFF.local.md` 读取，不写入版本合同、Git、CI、日志或聊天。
- 阿里云短信等配置由项目所有者后续填写；未填写前使用 Adapter/Fake 验证，不把模拟结果当作真实短信成功。

## 3. 开始门禁

1. `CURRENT_RELEASE.yaml` 当前版本必须为 `V1.3.0`。
2. 本版 `PAGE_STATE_MOCKUP_BINDINGS.csv` 的所有图片必须存在、Manifest 状态 APPROVED、SHA-256 匹配。
3. 分支和版本号必须正确；仓库 remote 必须是固定地址。
4. 只运行一次全量预检，然后立即编码；不得重新规划已锁定页面、技术和 UI。

## 4. Feature 纵向范围

| Feature | 价值 | 页面 | API | 数据 | 后台 | 明确排除 |
|---|---|---|---|---|---|---|
| PROFILE-002 会员状态展示 | 展示未开通、有效和过期状态及后台配置权益文案。 | ME-001;ME-004;COM-OV-002 | getMyMember | DB-ME-001;DB-AUTH-008 | ADM-ME-001;ADM-ME-002 | 在线开通;支付;折扣结算;返佣结算 |
| PROFILE-003 双账户余额只读展示 | 展示真实佣金账户和任务账户余额，默认 0.00。 | ME-001;ME-005;COM-OV-002 | getMyWallets | DB-ME-002 | ADM-ME-001;ADM-ME-003 | 余额流水;提现;转账;后台直接改余额 |
| PROFILE-004 道具目录展示 | 展示刷新卡、超级头条、头条和变色卡。 | ME-001;ME-006;COM-OV-002 | getPropDisplay | DB-ME-003 | ADM-ME-001;ADM-ME-004 | 购买;库存;使用;有效期 |
| PROFILE-005 常用功能、客服、设置和明确占位 | 所有入口有真实页面或明确占位，不出现空链接和假功能。 | SYS-002;ME-007;ME-008;ME-009;ME-010;ME-011;ME-012 | getSupportConfig;getLegalDocument;getCurrentRelease | DB-SYS-001 | ADM-ME-001;ADM-ME-005 | 无 |

## 5. 每次上下文恢复必须再次遵守

- 读取本文件、`UI_FIXED_RULES.md`、`FRONTEND_SCOPE.md`、`BACKEND_ADMIN_SCOPE.md` 和直接相关页面合同。
- 任何页面都不得根据个人审美改变字号、圆角、边距、顶部栏、底部导航或色值。
- UI 按批准效果图像素级还原；具体功能按 Feature/Interaction/API，不从图中虚构。
- Android、API、数据库、后台、配置、错误和测试同版闭环。
- 每个按钮有 testTag 和自动测试；禁止无响应、假成功、永久 Mock 和静态假数据。
- 快速执行：多写代码、少做重复验证；开发中只跑受影响测试，版本交付前跑全量。

## 6. GitHub Actions 和交付

当前 Commit 的 APK 先通过 `APK_BUILD_GATE` 生成唯一 Artifact；随后 `EMULATOR_ACCEPTANCE_GATE` 下载该 Artifact，通过合同、后端、Android、API 36 模拟器、本版全部新增交互、适用截图和升级安装验收后，才能从同一 Artifact 下载到本机。复制到 `%USERPROFILE%\Desktop\商推客交付\1.3.0\`，同时交付计划/完成/Owner测试/自动报告/视觉差异/部署/DNS/Build Info/Provenance/SHA/已知问题。

## 7. 版本关闭

仅当本版所有 Feature 状态为 DONE、自动测试通过、部署健康、APK 来源一致、桌面文件齐全且项目所有者完成验收后，才能将 `CURRENT_RELEASE.yaml` 机械推进到下一版本。聊天中的“继续”不能绕过关闭条件。
