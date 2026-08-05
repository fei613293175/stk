# 商推客开发包目录

| 目录/文件 | 用途 |
|---|---|
| `00_READ_ME_FIRST.md` | Codex 和项目所有者首读入口 |
| `AGENTS.md` | 仓库级不可漂移开发规则 |
| `MASTER_DEVELOPMENT_PLAN.md` | 五个 APK 版本总计划 |
| `contracts/` | Page/State/Mockup/Interaction/API/DB/Admin/Config/Test 机器合同 |
| `versions/V1.0.0 ... V1.4.0/` | 每版重复 UI 固定参数、范围、效果图绑定、测试、DNS 和交付规则 |
| `ui/mockups/approved/` | 236 张已批准、独立、1170×2532px 页面状态效果图 |
| `ui/mockups/review-sheets/` | 44 张按 Page ID 汇总的视觉审阅板，不替代独立效果图 |
| `ui/page-contracts/` | 44 个页面合同，已同步实际效果图路径和批准状态 |
| `ui/FIXTURE_DATA.yaml` | 效果图与截图测试的固定数据，不具有功能权威 |
| `ui/MOCKUP_GENERATION_REPORT.md` | 效果图数量、版本分布、唯一 SHA 与校验报告 |
| `backend/` | Discuz 插件实现规范和五版 SQL 迁移蓝图 |
| `android/` | 原生工程模块、路由、依赖和 testTag 规范 |
| `.github/workflows/` | 合同、构建、API 36 模拟器、视觉和发布来源工作流 |
| `scripts/` | 合同校验、确定性效果图渲染、视觉比对、APK 校验和桌面复制 |
| `release-inputs/` | 每版真实发布证据状态；代码未开发前保持阻断 |
| `templates/` | 本机管理员凭据和桌面交付模板 |
| `references/` | 官方资料、工具链版本和通用模板适配报告 |
| `audit/` | 机器合同、图片、数据库和包完整性校验结果 |

## 规模

- 五个 APK 版本：5
- 前端视觉对象：44
- 页面状态：236
- 已批准独立效果图：236
- 页面状态审阅总览板：44
- 可操作交互：165
- JSON API：32
- 数据实体：21
- 后台页面：35
- 后台配置：122
- 纵向 Feature：30
- 自动测试合同：514
