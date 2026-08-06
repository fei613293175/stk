# 发送给 Codex：开始商推客 1.0.0

**环境硬门禁：** V1.0.0 的 Android/PHP/Discuz 构建、签名、部署和 Release 验收只允许在线上服务器或 GitHub Actions；模拟器验收只能在 GitHub Actions 执行。本机不得安装或运行这些环境，本机只做源码和静态合同检查。
**API 版本硬门禁：** 只允许正式稳定通道的 API 36（Android 16）作为 compileSdk、targetSdk、SDK platform、system image 和模拟器版本；非 API 36（含预览/canary 和任何其他 API 版本）禁止使用，遇到问题不得切换版本。
**环境复用顺序：** 开发前先只读盘点线上已有 JDK/Gradle/Android SDK/模拟器/PHP/Discuz、Docker 镜像和缓存；满足合同即复用，JDK 17+ 均可，已有 JDK 21 优先。只在缺失且不影响现有业务时用独立目录/容器/volume 补齐，禁止替换系统运行时、改业务配置、占业务端口或重启服务，并记录实际路径、版本和证据。
**门禁分层：** `APK_BUILD_GATE` 负责编译、Lint、单元/后端测试、签名和同一 Commit Artifact 生成，不依赖 API 36 模拟器或 `/dev/kvm`；`EMULATOR_ACCEPTANCE_GATE` 下载该 Artifact 执行 API 36 模拟器、Instrumentation、交互和视觉验收，禁止重复编译。缺少 `/dev/kvm` 或模拟器运行库时切换 GitHub Actions 的 API 36 runner，不得阻塞 APK 构建、改动业务主机或伪造 KVM；模拟器门禁未通过不得宣称完整交付。
**版本合同文件：** 先读取并填写 `versions/V1.0.0/ENVIRONMENT_CONTRACT.yaml`，统一规则见 `docs/19_ONLINE_ENVIRONMENT_REUSE_CONTRACT.md`。

继续开发商推客，但不要根据聊天记忆重新规划项目或选择版本。先确认仓库 remote 是 `https://github.com/fei613293175/stk.git`，读取 `CURRENT_RELEASE.yaml`；只有当前版本为 `V1.0.0` 时才继续。

开发前先读取并确认线上环境基线：Discuz! X5.0 根目录 `/www/wwwroot/stk_zz_yihao_com`，公共站点 `https://stk.zz-yihao.com`，`stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由 Owner 报告解析，线上服务器已有 Android 构建环境。后台用户名为 `admin`，密码只从本机 `ADMIN_ACCESS_HANDOFF.local.md` 读取，不得复制到代码、CI、包清单或聊天；阿里云短信等配置由 Owner 后续填写。以上线上状态仍须在实际使用前按需验证，不得把 Owner 报告当成本机实测证据。

随后读取：

1. `AGENTS.md`
2. `versions/V1.0.0/VERSION_DEVELOPMENT_SPEC.md`
3. `versions/V1.0.0/UI_FIXED_RULES.md`
4. `versions/V1.0.0/FRONTEND_SCOPE.md`
5. `versions/V1.0.0/BACKEND_ADMIN_SCOPE.md`
6. `versions/V1.0.0/PAGE_STATE_MOCKUP_BINDINGS.csv`
7. 本版本相关 Feature、页面合同、API、数据表、后台、配置和测试合同

先运行一次 `python scripts/validate_package.py` 和 `python scripts/check_mockup_readiness.py --release V1.0.0`。通过后立即进入实际编码，不要重复全仓规划和无意义检查。严格按批准效果图与固定 Token 开发 UI，功能只按合同；前端、Discuz 插件后端、数据库、后台配置/查询/审计和自动测试必须同版闭环。

完成后先由 `APK_BUILD_GATE` 生成唯一 Release APK Artifact，再由 `EMULATOR_ACCEPTANCE_GATE` 下载该 Artifact 执行 API 36 模拟器、全部新增按钮、截图比对、历史核心回归和首次安装。只下载通过验收的精确 Artifact，核对 SHA 与来源后复制到本机桌面 `商推客交付\1.0.0`，附完整计划、完成、测试和部署清单。未满足关闭清单不得进入下一版本。
