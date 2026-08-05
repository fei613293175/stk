# 线上环境复用合同

本合同适用于 `V1.0.0` 至 `V1.4.0`，并作为后续版本环境合同的模板。它不授权在本机安装或运行 Android/PHP/Discuz/模拟器环境。

## 决策顺序

每个版本开始前，在目标线上服务器执行只读盘点，并按以下顺序选择：

1. 服务器现有运行时和服务；
2. 服务器现有 Docker 镜像、SDK、Gradle/Maven 缓存；
3. 服务器独立目录、容器或 volume 中补齐缺失组件；
4. 前三项不能满足合同时才使用 GitHub Actions。

已有 JDK 21 且满足版本合同，必须复用 JDK 21；JDK 17 或更高版本均可作为兼容环境。不得为了“统一版本”重新部署已经满足合同的环境。

## API 版本固定与构建、模拟器门禁分离

- API 36（Android 16）是本合同唯一允许的 Android SDK platform、compileSdk、targetSdk、system image 和模拟器版本。任何非 API 36 版本均禁止安装、配置、构建、验收或写入版本证据；版本合同必须逐版锁定 API 36。

- `APK_BUILD_GATE` 包含 Android 编译、Lint、单元/后端测试、签名和 Artifact 生成；它只要求合同规定的 JDK、Gradle、Android SDK/Build Tools 和业务测试环境，**不要求 API 36 模拟器或 `/dev/kvm`**。
- `EMULATOR_ACCEPTANCE_GATE` 包含 API 36 模拟器、Instrumentation、全部新增交互和视觉截图；它必须只在 GitHub Actions 执行，并下载 `APK_BUILD_GATE` 生成的同一 Commit Artifact，禁止重复编译。
- `/dev/kvm` 只提供模拟器硬件加速，不是 APK 打包前置条件。服务器没有 `/dev/kvm`、模拟器运行库缺失或模拟器无法启动时，`APK_BUILD_GATE` 可以继续；不得伪造设备、替换业务主机运行时、重启业务服务或为此阻塞 APK 构建。必须切换 GitHub Actions 的 API 36 runner，并在版本合同记录实际阻塞原因。

## 不影响业务

- 只允许使用独立构建目录、容器、volume 和临时 HOME。
- 禁止替换系统 JDK/PHP/Gradle/SDK，修改现有 Nginx、PHP-FPM、MySQL、Discuz 配置，绑定业务端口或重启业务服务。
- 正式部署前必须备份目标插件目录、相关表和配置，并具备可验证回滚路径。
- 正式签名密钥、密码、Token 和管理员凭据不得进入聊天、Git、构建日志或证据文件。

## 每版必须记录

版本目录的 `ENVIRONMENT_CONTRACT.yaml` 必须填写实际使用的：主机/别名、JDK 路径和版本、Gradle/AGP、SDK/Build Tools、镜像 digest 或 tag、缓存/volume 路径、模拟器/API、PHP/Discuz/MySQL、复用或补齐决策、验证命令和日志位置。未盘点、未记录或只有“服务器已就绪”文字时，环境门禁不通过。

## 完成判定

环境复用只证明工具来源合规，不等于版本交付完成。`APK_BUILD_GATE` 通过只代表 APK Artifact 可生成；完整版本交付仍须让同一 Commit Artifact 分别通过 API/Discuz、`EMULATOR_ACCEPTANCE_GATE`、视觉、部署、回滚和来源证据。
