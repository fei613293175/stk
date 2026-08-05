# 02 全局 UI 设计系统与固定数值

## 1. 设计基准

- 标准设计画布：390×844dp，对应 3x 效果图 1170×2532px。
- 验收宽度：360、390、412、430dp；业务最大宽度 430dp；只支持竖屏。
- 浅色模式；禁止动态取色和暗色模式。
- 页面左右边距 16dp；卡片内边距 16dp；列表间隙 12dp；区块间隙 20dp；字段间隙 12dp；图片网格间隙 8dp。
- 所有点击区域至少 48×48dp。

## 2. 色值

| Token | 色值 | 用途 |
|---|---|---|
| brand_primary | #246BFD | 主按钮/选中 |
| brand_primary_pressed | #1D56D8 | 按下 |
| brand_primary_soft | #EAF1FF | 浅品牌背景 |
| brand_accent | #FF7A1A | 会员营销 |
| brand_accent_pressed | #E56208 | 会员按下 |
| brand_accent_soft | #FFF0E5 | 浅营销背景 |
| background | #F5F7FA | 页面背景 |
| surface | #FFFFFF | 卡片/表单 |
| surface_secondary | #F9FAFB | 搜索/弱背景 |
| text_primary | #172033 | 主文字 |
| text_secondary | #667085 | 次文字 |
| text_tertiary | #98A2B3 | 提示 |
| border | #E4E7EC | 边框 |
| divider | #EAECF0 | 分割线 |
| success | #16A34A | 成功/已发布 |
| warning | #F59E0B | 待审核/警告 |
| error | #E5484D | 错误/驳回 |
| info | #246BFD | 信息 |
| disabled | #D0D5DD | 禁用 |
| overlay_scrim | #00000066 | 弹层遮罩 |

## 3. 字号、行高和字重

| Token | 字号 | 行高 | 字重 | 用途 |
|---|---|---|---|---|
| display | 28 | 36 | 700 | display |
| amount_large | 24 | 32 | 700 | amount large |
| title_large | 20 | 28 | 600 | title large |
| page_title | 18 | 26 | 600 | page title |
| section_title | 16 | 24 | 600 | section title |
| card_title | 15 | 22 | 600 | card title |
| button | 15 | 22 | 600 | button |
| body_medium | 14 | 22 | 500 | body medium |
| body | 14 | 22 | 400 | body |
| legal_body | 14 | 24 | 400 | legal body |
| secondary | 13 | 20 | 400 | secondary |
| caption | 12 | 18 | 400 | caption |
| nav_label | 11 | 16 | 500 | nav label |
| micro | 10 | 14 | 500 | micro |

禁止业务页面临时新增 13.5sp、15.5sp、17sp 等不在 Token 表中的字号。

## 4. 间距

| Token | dp |
|---|---|
| s2 | 2 |
| s4 | 4 |
| s6 | 6 |
| s8 | 8 |
| s12 | 12 |
| s16 | 16 |
| s20 | 20 |
| s24 | 24 |
| s32 | 32 |
| s40 | 40 |

## 5. 圆角

| Token | dp |
|---|---|
| r4 | 4 |
| r6 | 6 |
| r8 | 8 |
| r12 | 12 |
| r16 | 16 |
| r20 | 20 |
| r24 | 24 |
| pill | 999 |

## 6. 高度和尺寸

| 参数 | dp |
|---|---|
| top_bar | 56 |
| bottom_nav | 64 |
| primary_button | 48 |
| secondary_button | 40 |
| small_button | 36 |
| input | 48 |
| search | 44 |
| textarea_min | 120 |
| touch_min | 48 |
| icon_default | 24 |
| icon_small | 20 |
| avatar_project_card | 32 |
| avatar_detail | 40 |
| avatar_profile | 72 |
| divider | 1 |
| dialog_width | 326 |
| captcha_image_width | 278 |
| captcha_image_height | 96 |
| project_thumbnail | 80 |
| image_sort_tile | 112 |
| empty_illustration | 96 |
| status_illustration | 112 |
| member_card_height | 176 |
| sticky_action_area | 72 |
| screen_horizontal_padding | 16 |
| card_inner_padding | 16 |
| list_gap | 12 |
| section_gap | 20 |
| field_gap | 12 |
| image_gap | 8 |

## 7. 页面公共规则

- 一级页面首页/发布/我的使用 `StkRootScaffold`；二级页使用 `StkPageScaffold`。
- 顶部栏统一 56dp；一级标题左对齐，二级标题居中；返回按钮图标 24dp、触控区 48dp。
- 底部导航统一 64dp 加系统安全区，固定首页/发布/我的，图标 24dp，文字 11sp。
- 主按钮 48dp、圆角 12dp；输入框 48dp、圆角 12dp；卡片圆角 16dp；弹窗圆角 20dp。
- 会员卡是唯一允许使用品牌橙色渐变的业务组件，其他页面不得自行添加渐变或阴影。
- 页面代码只能引用 Token 和公共组件。CI 静态检查发现页面内直接声明 `Color(...)`、任意 `.dp`、`.sp` 或 `RoundedCornerShape(...)` 时失败，测试代码与设计系统模块除外。

## 8. 效果图与功能边界

效果图决定布局、视觉层级、颜色、间距、圆角和组件状态；功能只由 Feature、Interaction、API 和后台合同决定。效果图中的项目名、头像、金额、日期、会员状态、按钮文案均为固定测试数据，不能被 Codex 扩展成未规划功能。
