# Android 工具链固定版本

| 项目 | 固定版本 |
|---|---:|
| Android Gradle Plugin | 9.3.0 |
| Gradle Wrapper | 9.5.0 |
| JDK | 17 |
| compileSdk / targetSdk | 36 |
| SDK Build Tools | 36.0.0 |
| minSdk | 26 |
| Kotlin | 2.4.10 |
| Compose BOM | 2026.06.00 |
| Lifecycle | 2.11.0 |
| Navigation | 2.9.8 |
| Room | 2.8.4 |

API 36（Android 16）是本项目唯一允许的稳定 compileSdk、targetSdk 和模拟器平台。任何其他 API 平台均禁止用于构建或验收。其余依赖由 V1.0.0 工程 `gradle/libs.versions.toml` 一次性锁定稳定兼容版本，禁止动态版本。升级依赖必须有单独变更记录、编译验证、API 36 模拟器和截图回归，不能在功能版本中静默漂移。
