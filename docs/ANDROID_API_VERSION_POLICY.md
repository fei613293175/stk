# Android API 版本政策（全局硬门禁）

本文件是仓库级 Android API 版本规则，适用于 `V1.0.0`–`V1.4.0` 及后续所有小版本。任何版本合同、CI 工作流、构建脚本或交付证据都必须与本文件一致。

## 唯一允许的版本

- `compileSdk`、`targetSdk`、Android SDK platform、system image 和模拟器验收唯一允许 **Android API 36（Android 16）正式 Stable**。
- API 37 以及所有其他 API level、Preview、Canary、Beta、RC 或临时版本均禁止安装、配置、构建、测试、验收和交付。
- 遇到 API 36 环境故障不得升级或降级 API；应修复 API 36 Stable 环境，或把模拟器门禁转移到 GitHub Actions。

## 执行边界

- `APK_BUILD_GATE`（编译、Lint、单元/后端测试、签名、Artifact）不依赖模拟器或 `/dev/kvm`。
- `EMULATOR_ACCEPTANCE_GATE`（Instrumentation、交互和视觉截图）只能在 GitHub Actions 使用 API 36 Stable 模拟器，并下载同一 Commit 的 APK Artifact，禁止重复编译。
- 本机与业务线上服务器禁止启动模拟器；缺少 `/dev/kvm` 不得阻塞 APK 构建、不得伪造 KVM、不得改动业务服务。

## 工具包与 API level 的区分

`platform-tools`、`emulator` 等 SDK 工具的发行号不是 Android API level。工具必须使用稳定、非 Preview 的发行版，并在 CI 证据中记录实际版本；工具版本不得带入任何 API 37 platform、system image 或构建目标。

每个版本的 `ENVIRONMENT_CONTRACT.yaml` 必须声明：

```yaml
api_policy:
  allowed_api_level: 36
  api_channel: stable
  forbidden_api_levels: all_except_36
  explicitly_forbidden_api_levels: [37]
  emulator_execution: github_actions_only
  on_mismatch: reject_before_build
```
