# 商推客 Codex 长期规则

1. 仓库唯一远程地址是 `https://github.com/fei613293175/stk.git`，默认分支 `main`。
2. 唯一项目状态来自 `CURRENT_RELEASE.yaml`；不得根据聊天记忆猜测下一版本。
3. Android 用户端必须使用 Kotlin + Jetpack Compose 原生实现；内部业务页面禁止 WebView。
4. Discuz! X5.0 只通过标准插件 `stk_auth`、`stk_project` 扩展；禁止修改核心文件。
5. UI 只能使用 `contracts/ui-design-tokens.yaml` 与公共组件；业务页面禁止硬编码颜色、dp、sp、圆角和高度。
6. Page ID、State ID、Interaction ID、Feature ID、API、数据表、后台和测试必须保持一一追踪。
7. 效果图状态未为 `APPROVED` 时，不得宣称像素级页面完成；示例文字不构成功能范围。
8. 注册不发送短信；登录/注册/短信发送/密码重置必须通过动作绑定安全验证码。
9. 每个可操作控件必须有 testTag、Interaction ID 和自动测试；禁止空链接、无响应按钮和假成功。
10. 开发流程使用快速执行模式：一次预检后立即编码；输入未变化时不重复全仓扫描或无意义验证。
11. 每版只实现 `versions/<version>/` 范围，不提前混入后续业务，也不遗漏该版后端、后台和配置。
12. 发布 APK 只能来自通过 GitHub Actions 验收的同一 Commit Artifact；禁止本地重编另一份交付。
13. V1.1.0 起必须验证 `adb install -r` 覆盖更新、数据库迁移、登录态和缓存兼容。
14. 每版桌面交付必须包含 APK、计划/完成清单、Owner 测试、自动测试、视觉差异、部署域名、Build Info、Provenance、SHA-256 和已知问题。
15. 密钥、密码、正式签名和管理员凭据只在本机安全交付，禁止提交仓库。
16. 连续两轮没有代码、测试或可运行成果时，停止泛化思考，定位具体阻塞并继续可实施工作。
17. 线上基础设施事实：Discuz! X5.0 网站根目录为 `/www/wwwroot/stk_zz_yihao_com`，公共网址为 `https://stk.zz-yihao.com`；`stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由项目所有者解析，线上服务器已有 Android 构建环境，可按需使用。
18. 阿里云短信等外部配置由项目所有者后续在后台填写；未填写前使用正式 Adapter/Fake 和明确 Owner Action，不得伪造短信成功。
19. 线上后台账号为 `admin`；密码只允许保存在被 `.gitignore` 保护的 `ADMIN_ACCESS_HANDOFF.local.md`，版本合同、Git、CI、日志和聊天不得出现明文密码。
20. 构建环境唯一性：Android SDK/JDK/Gradle/AGP、PHP/Discuz、签名、模拟器和发布验收只能使用线上服务器或其线上 CI 环境；禁止在本机安装、下载、配置、升级或执行这些构建/部署环境。开发机只允许做源码编辑、静态合同检查和不依赖构建环境的证据整理；每个版本必须在其版本目录明确遵守本条。
21. 线上环境复用优先：开始任何版本前必须只读盘点目标线上服务器已有的 JDK、Gradle、Android SDK/Build Tools/模拟器、PHP、数据库、签名代理和部署服务；已有环境满足合同时直接复用，不得重复安装。若仅部分缺失，优先使用已有 Docker 镜像、SDK 缓存、Gradle 缓存或独立 volume；只有无法复用且确认不影响业务时，才在独立目录/容器中补齐。禁止替换系统运行时、覆盖现有版本、占用业务端口、改动现有 Nginx/PHP-FPM/MySQL/Discuz 配置或重启业务服务；每版记录实际复用环境、补齐项、路径、版本和验证证据。
22. 线上环境合同以 `docs/19_ONLINE_ENVIRONMENT_REUSE_CONTRACT.md` 为统一规则；每个版本必须先读取并填写 `versions/<version>/ENVIRONMENT_CONTRACT.yaml`，未完成只读盘点和证据字段不得构建、部署或宣称交付。后续版本复制该文件并只修改版本绑定和实际环境证据，不得弱化复用顺序或业务保护条款。
