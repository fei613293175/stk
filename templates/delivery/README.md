# 桌面交付文件模板说明

真实桌面交付由 `scripts/create_release_bundle.py` 从已经通过 GitHub Actions 验收的同一 APK Artifact、原始测试证据和 `release-inputs/<release>/RELEASE_EVIDENCE.yaml` 生成。模板不能手工填写成“PASS”来替代测试；脚本检测到未关闭状态会直接失败。
