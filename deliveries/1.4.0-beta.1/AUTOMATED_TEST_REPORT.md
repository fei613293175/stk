# V1.4.0 Automated Test Report

所有构建和非设备测试均在已连接服务器 `obx-test` 执行；本机未搭建或运行项目构建、测试、模拟器或虚拟设备。

服务器工作区：`/opt/stk-build/stk-fast-v140-beta1-20260812`

- `scripts/fast-check.sh`：PASS，0 errors / 0 warnings。
- PHP 语法检查：PASS。
- 现有合同、V1.3 合同、V1.4 合同：PASS。
- Android `testDebugUnitTest`：PASS。
- Android `assembleRelease`：PASS。
- Gradle：`BUILD SUCCESSFUL in 2m 30s`，89 tasks。
- APK Signature Scheme v2：verified。

APK：

- 大小：`14611703` bytes。
- SHA-256：`6087c2a09743fd121b5d74e16eef7bb16dd28f69e3eb298e574d77fdfb881ff2`。
- 包名/版本：`com.zzyihao.stk`, `10401`, `1.4.0-beta.1`。
- 证书 SHA-256：`8d82da03e9133c1d60087e735b2abc9d9d2ecbb0f1815164297079d8eba537ee`。

GitHub Fast CI 结果见 `CI_AUDIT.md`；发布分支仅手动运行一次主流程。
