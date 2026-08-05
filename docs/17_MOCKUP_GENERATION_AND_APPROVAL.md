# 17 效果图生成、批准与基线维护

## 当前状态

- 视觉对象：44
- 页面状态：236
- 独立效果图：236
- 页面状态审阅总览板：44
- Manifest 状态：全部 `APPROVED`
- 独立图片尺寸：1170×2532px（390×844dp，3.0 密度）
- 设计系统：STK-DS-1.0
- 生成方式：确定性代码渲染，中文使用 Noto Sans CJK SC 直接排版
- 图片校验：236 个唯一 SHA-256，无完全重复图片，无缺失或尺寸错误

## 目录与权威关系

1. `contracts/mockup-manifest.csv`：每张图片的唯一合同、路径、状态、SHA、审阅标识和时间。
2. `ui/mockups/approved/<版本>/<Page ID>/`：Codex 开发和 GitHub Actions 截图对比使用的正式基线。
3. `ui/mockups/review-sheets/`：按 Page ID 汇总的检查板，只用于快速审阅，不能替代独立基线。
4. `ui/page-contracts/`：页面布局、必要内容、禁止内容、交互和各状态图片绑定。
5. `ui/MOCKUP_GENERATION_REPORT.md`、`audit/MOCKUP_RENDER_AUDIT.json`：生成与自动校验结果。

## Codex 使用规则

- 先运行 `python scripts/check_mockup_readiness.py --release <版本>`；只有全部 `APPROVED` 且 SHA 匹配才可开发该版本 UI。
- UI 必须同时服从批准效果图和 STK-DS-1.0 Token；效果图不能授权新增未在功能合同中的业务能力。
- 同 Page 不同 State 必须实现真实状态差异；不得用一张图替代多个 State ID。
- GitHub Actions 的实际截图必须按 Manifest 对应 State ID 比较，禁止与总览板比较。
- 未经正式变更记录，不得重新生成、覆盖、压缩、裁剪或改名任何批准图片。

## 重新生成流程

1. 先更新页面合同、状态合同或固定 Token，并记录变更原因。
2. 运行 `python scripts/render_all_mockups.py` 重新生成受影响图片。
3. 逐页检查页面身份、状态差异、Token、中文文案、功能边界和重复情况。
4. 更新 Manifest 的 SHA、审阅标识和时间。
5. 运行：
   - `python scripts/check_mockup_readiness.py --all`
   - `python scripts/generate_mockup_jobs.py --check-only`
   - `python scripts/validate_package.py`
6. 三项全部通过后，新的图片才成为正式基线。

## 不可漂移边界

- 顶部栏、底部导航、字号、圆角、边距、高度和色值必须来自 STK-DS-1.0。
- 示例数据只来自 `ui/FIXTURE_DATA.yaml`；不得自由创造订单、支付、提现、返佣、收藏或实名认证表单。
- Overlay 必须保留父页面上下文；系统状态页不得伪装成普通业务页。
- 中文必须由确定性字体渲染，不使用图像模型生成文字。
