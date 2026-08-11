# 商推客固定 UI 规则（V1.1.0 必须继续复用）

## 1. 终端与布局

- 仅 Android 手机竖屏；设计基准 `390×844dp`。
- 验收宽度：`360dp / 390dp / 412dp / 430dp`；不做 PC 用户端、横屏或平板专用重排。
- 页面左右边距统一 `16dp`；卡片内边距统一 `16dp`；列表卡片间距 `12dp`；大区块间距 `20dp`。
- 所有页面继续使用统一 `StkScaffold`、`StkTopAppBar`、`StkBottomNavigation`；不得为 V1.1.0 重写第二套导航。

## 2. 固定色值

| Token | 色值 | 用途 |
|---|---|---|
| BrandPrimary | `#246BFD` | 主按钮、选中态、主品牌色 |
| BrandPrimaryPressed | `#1955D1` | 主按钮按下 |
| BrandPrimarySoft | `#EDF3FF` | 浅蓝背景 |
| BrandAccent | `#FF7A1A` | 会员、营销引导 |
| BrandAccentSoft | `#FFF1E6` | 橙色浅背景 |
| Background | `#F5F7FA` | 页面背景 |
| Surface | `#FFFFFF` | 卡片、输入区域 |
| TextPrimary | `#182230` | 主文字 |
| TextSecondary | `#667085` | 次级文字 |
| TextTertiary | `#98A2B3` | 弱提示 |
| Border | `#E4E7EC` | 边框、分隔线 |
| Success | `#16A34A` | 成功 |
| Warning | `#F59E0B` | 待审核、警告 |
| Error | `#E5484D` | 错误、失败 |
| Disabled | `#D0D5DD` | 禁用 |

禁止 Material You 动态取色；本阶段只做浅色主题。

## 3. 固定字号、行高与字重

| Token | 字号 | 行高 | 字重 | 用途 |
|---|---:|---:|---:|---|
| AmountLarge | 24sp | 32sp | 700 | 重点数字 |
| PageTitle | 18sp | 26sp | 600 | 顶部标题 |
| SectionTitle | 16sp | 24sp | 600 | 区块标题 |
| CardTitle | 15sp | 22sp | 600 | 项目卡标题 |
| BodyMedium | 14sp | 22sp | 500 | 强调正文 |
| Body | 14sp | 22sp | 400 | 普通正文 |
| Secondary | 13sp | 20sp | 400 | 次要信息 |
| Caption | 12sp | 18sp | 400 | 时间、标签、提示 |
| NavLabel | 11sp | 16sp | 500 | 底部导航 |

业务页面不得新建临时字号。

## 4. 间距、圆角、高度

- 间距 Token：`4 / 8 / 12 / 16 / 20 / 24 / 32dp`。
- 圆角：标签 `6dp`；小状态块 `8dp`；输入框/按钮 `12dp`；卡片 `16dp`；弹窗/底部面板 `20dp`；胶囊 `999dp`。
- 顶部导航 `56dp`；底部导航 `64dp + 系统安全区`；主按钮/输入框 `48dp`；搜索框 `44dp`；次按钮 `40dp`；小按钮 `36dp`。
- 最小触控区 `48×48dp`；底部导航/普通图标 `24dp`；小图标 `20dp`。
- 列表头像 `40dp`；分隔线 `1dp`；项目封面默认 `16:9`，图片不得拉伸。

## 5. V1.1.0 组件约束

- 首页项目卡、搜索结果卡必须复用同一个 `StkProjectCard`，不得出现两套字号、圆角或封面比例。
- 首页骨架、搜索骨架和详情骨架复用统一 Skeleton Token。
- 分页失败只显示列表底部局部错误，不得用整页错误替换已有内容。
- 离线缓存状态保留内容并显示轻量离线标识；无缓存时才使用整页离线状态。
- 分类使用统一 Bottom Sheet/Overlay；当前选中、确认、重置状态必须明确。
- 图片查看器保持图片原比例，支持滑动、缩放和单图重试；不添加保存到相册按钮。
- 联系方式面板使用统一按钮高度、图标和回退提示；网址只允许白名单或确认后的 Custom Tabs。
- 业务页面禁止硬编码颜色、字号、圆角和常用 dp，统一读取现有设计系统 Token。
