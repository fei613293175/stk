# Compose testTag 标准

- 固定控件使用 `snake_case`，与 `contracts/ui-interaction-map.csv` 完全一致。
- 列表动态项使用 `{entityId}` 模板，例如 `project_card_{projectId}`，测试通过语义前缀与 Fixture ID 定位。
- testTag 只作为测试标识，不向用户展示，不复用同页两个可操作控件。
- CI 扫描 Compose 可点击语义，发现无 Interaction ID/testTag 的控件时失败。
