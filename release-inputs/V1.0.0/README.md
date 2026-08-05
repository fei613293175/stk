# V1.0.0 发布证据输入

此目录在开发过程中更新，但不得提前标记通过。只有以下条件全部真实成立时，才把 `RELEASE_EVIDENCE.yaml` 更新为：

- `status: READY_FOR_DELIVERY`
- 本版全部 Feature 为 `DONE`
- `automated_test_status: PASS`
- `visual_status: PASS`
- `upgrade_status: PASS`
- `deployment_status: PASS`
- `known_issues` 据实填写，可为空数组但不能保留模板占位文字

项目所有者验收不在这里伪造 PASS；桌面包中的 `OWNER_TEST_CHECKLIST.md` 保持 `PENDING_OWNER`。
