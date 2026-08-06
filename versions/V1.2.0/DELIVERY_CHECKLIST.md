# 1.2.0 交付关闭清单

- [ ] 环境唯一性硬门禁：V1.2.0 的 Android SDK/JDK/Gradle/AGP、PHP/Discuz、签名、部署和验收只能在线上服务器或 GitHub Actions 执行；正式稳定通道 API 36 模拟器只能在 GitHub Actions 执行，API 37、其他非 API 36、Preview 和 Canary 禁止使用；本机不得安装、下载、配置、升级或运行这些环境。本机仅允许源码编辑、合同/静态检查和证据整理。
- [ ] 环境复用记录：先只读盘点并复用线上已有 JDK 17+（JDK 21 优先）、Gradle、Android SDK/模拟器、PHP/Discuz、镜像和缓存；仅缺失项可在独立目录/容器/volume 补齐，且不得替换系统运行时、改业务配置、占端口或重启服务。
- [ ] 已填写 `ENVIRONMENT_CONTRACT.yaml` 的实际路径、版本、复用/补齐决策、验证日志和同一 Commit 来源证据。
- [ ] `APK_BUILD_GATE` 已独立通过：编译、Lint、单元/后端测试、签名和 Artifact 只执行一次，且不依赖 API 36 模拟器或 `/dev/kvm`。
- [ ] `EMULATOR_ACCEPTANCE_GATE` 已下载同一 Commit Artifact 完成 API 36 模拟器、Instrumentation、交互和视觉验收；禁止重复编译。
- [ ] 缺少 `/dev/kvm` 或模拟器运行库时已切换 GitHub Actions 的 API 36 runner，未改动业务主机、伪造 KVM 或阻塞 APK 构建；门禁证据已记录。

- [ ] 构建、签名、上传/审核验收证据全部来自线上服务器/GitHub Actions，禁止本机 APK

- [ ] 所有本版效果图 APPROVED 且 SHA 匹配
- [ ] Android versionName=1.2.0、versionCode=10200、applicationId 固定
- [ ] 本版 Feature 前端/后端/数据库/后台/配置/错误/日志全部完成
- [ ] 本版每个 Interaction 有 testTag 和通过测试
- [ ] API 36 模拟器全量本版交互通过（否则不得宣称完整版本交付）
- [ ] 本版全部新增 State 截图比对通过
- [ ] 历史核心交互和 Golden 回归通过
- [ ] 从上一版本 adb install -r 覆盖安装通过
- [ ] 部署、健康检查、DNS/TLS 和后台通过
- [ ] GitHub Actions 精确 Artifact、Build Info、Provenance、SHA-256 一致
- [ ] Universal Release APK 真实大小 ≥10MB，无填充文件
- [ ] 桌面目录 13 类交付文件齐全
- [ ] 项目所有者测试清单已执行并记录
- [ ] 已知问题真实、无阻断问题被隐藏
