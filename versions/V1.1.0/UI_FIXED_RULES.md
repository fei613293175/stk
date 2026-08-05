# STK-DS-1.0 固定 UI 规则（本版本必须再次读取）

## 画布和适配
- Canonical：390×844dp / 1170×2532px @3x；测试宽度 360/390/412/430dp；最大业务宽度430dp；仅竖屏。
- 主按钮、输入框和最小触控区域统一 48dp；页面左右与卡片内边距统一 16dp。
- 浅色模式；禁止动态取色、暗色、横屏、平板重排和 PC 用户界面。

## 颜色
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

## 字体
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

## 间距
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

## 圆角
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

## 固定尺寸
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

## 不可违反
1. 业务页面禁止硬编码颜色、dp、sp、圆角、顶部栏、底部导航或动画时长。
2. 一级页复用 StkRootScaffold，二级页复用 StkPageScaffold；顶部栏56dp，底部导航64dp+安全区。
3. 内部业务页面禁止 WebView；服务端禁止下发布局参数。
4. 只实现本版本 Interaction/Feature；效果图示例内容不产生额外功能。
5. 当前 Page/State 的 Manifest 必须为 APPROVED 且 SHA 匹配，才能做像素级实现和视觉验收。
6. 所有 Loading/Empty/Error/Offline/Success/Failure 使用公共状态组件，不得逐页自由设计。
