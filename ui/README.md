# UI 目录

- `FIXTURE_DATA.yaml`：效果图与截图测试的固定数据，不具有功能权威。
- `page-contracts/`：44 个 Page ID 的独立合同，已同步实际效果图路径与批准状态。
- `mockups/approved/`：236 张已经批准、Manifest SHA 匹配的独立页面状态基线。
- `mockups/review-sheets/`：44 张按页面汇总的状态检查板，仅供审阅，不替代独立基线。
- `MOCKUP_GENERATION_REPORT.md`：生成数量、版本分布、唯一 SHA 和校验结论。

所有独立图片均为 1170×2532px PNG。Codex 和 CI 必须通过 `contracts/mockup-manifest.csv` 按 State ID 读取基线，不得自行选择相似图片，也不得用审阅总览板代替单状态截图。
