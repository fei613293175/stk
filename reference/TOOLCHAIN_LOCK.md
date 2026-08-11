# 工具链锁定：Beta 1

当前工具链只为快速产出 `1.0.0-beta.1`，除非出现明确、可复现的编译错误，不得主动升级。

| 项目 | 固定值 |
|---|---|
| JDK | 17 |
| Gradle Wrapper | 8.13 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.3.10 |
| compileSdk / targetSdk | 36 |
| minSdk | 23 |
| Compose BOM | 2026.06.01 |
| applicationId | `com.zzyihao.stk` |
| versionName | `1.0.0-beta.1` |
| versionCode | `10001` |

## 固定规则

- 使用仓库内官方 Gradle Wrapper：`./gradlew` 或 `gradlew.bat`。
- Wrapper 已固定 Gradle 分发包 SHA-256，不再编写自定义 Gradle 下载器。
- 普通提交不运行 Android 模拟器、截图比对、生产部署或 API 37 兼容测试。
- Beta 使用仓库内公开测试签名，只用于覆盖安装测试；正式版必须替换。
