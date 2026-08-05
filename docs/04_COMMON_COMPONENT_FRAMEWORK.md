# 04 公共页面框架与组件清单

| 组件ID | 组件 | 类型 | 高度 | 圆角 | 文字 | 硬规则 |
|---|---|---|---|---|---|---|
| CMP-001 | StkRootScaffold | layout | screen | none | N/A | 一级首页/发布/我的统一承载顶部区、内容区和三项底部导航；切换保留状态 |
| CMP-002 | StkPageScaffold | layout | screen | none | page_title | 二级页面统一状态栏、56dp 顶部栏、返回按钮和错误/加载容器 |
| CMP-003 | StkTopAppBar | navigation | 56dp | none | 18sp/26sp/600 | 一级标题左对齐；二级标题居中；返回与右侧操作触控区均 48dp |
| CMP-004 | StkBottomNavigation | navigation | 64dp + bottom inset | none | 11sp/16sp/500 | 固定首页/发布/我的三项，图标24dp，等宽；详情和二级页不显示 |
| CMP-005 | StkPrimaryButton | control | 48dp | 12dp | 15sp/22sp/600 | 全宽或内容宽；加载态禁止重复点击；最小触控48dp |
| CMP-006 | StkSecondaryButton | control | 40dp | 12dp | 14sp/22sp/500 | 不得用作页面唯一主操作 |
| CMP-007 | StkTextField | input | 48dp | 12dp | 14sp/22sp/400 | 标签、计数、帮助、错误占位固定；错误不会改变页面整体宽度 |
| CMP-008 | StkPasswordField | input | 48dp | 12dp | 14sp/22sp/400 | 右侧可见性按钮24dp图标、48dp触控；不记录明文 |
| CMP-009 | StkSearchBar | input | 44dp | 12dp | 14sp/22sp/400 | 搜索图标20dp、清除触控48dp、输入防抖300ms |
| CMP-010 | StkProjectCard | content | content | 16dp | 15sp title;14sp body;12sp caption | 单列信息流；封面16:9或80dp缩略图按布局合同；标题最多2行、简介最多3行 |
| CMP-011 | StkStatusBadge | feedback | 24dp | 999dp | 12sp/18sp/500 | pending warning; published success; rejected error; offline tertiary；文本与颜色固定映射 |
| CMP-012 | StkCaptchaDialog | overlay | content | 20dp | 18sp title;14sp body | 宽326dp；验证码278x96dp；输入48dp；刷新20dp图标/48dp触控；确认48dp |
| CMP-013 | StkMemberCard | marketing_display | 176dp | 20dp | 20sp title;14sp body;12sp labels | 只允许会员卡使用渐变；必须显示真实 inactive/active/expired 状态；在线开通按钮打开阶段说明 |
| CMP-014 | StkWalletCard | data_display | 96dp | 16dp | 24sp amount;13sp label | 金额两位小数；取不到数据时显示错误，不得回退假0.00；注册基础值确为0.00时正常显示 |
| CMP-015 | StkPropCard | content | 112dp | 16dp | 14sp title;12sp caption | 图标40dp；本期只展示，点击打开阶段说明，不出现购买价格 |
| CMP-016 | StkImageUploadTile | input | 112dp | 12dp | 12sp caption | 上传进度、失败、重试、删除、封面标识在同一组件内；网格间距8dp |
| CMP-017 | StkLoadingSkeleton | feedback | target-dependent | same as target | none | 骨架结构必须与最终页面结构一致，动画200ms，不允许整页转圈代替 |
| CMP-018 | StkEmptyState | feedback | content | none | 16sp title;14sp body | 插图96dp；标题与说明间8dp；可选操作按钮48dp |
| CMP-019 | StkErrorState | feedback | content | none | 16sp title;14sp body;12sp request id | 插图96dp；错误原因不暴露堆栈；重试按钮48dp |
| CMP-020 | StkSnackbar | feedback | 48dp min | 12dp | 14sp/22sp/500 | 默认2500ms；成功/复制/缓存清理等非阻断反馈；同一时刻只显示一条 |
| CMP-021 | StkConfirmDialog | overlay | content | 20dp | 18sp title;14sp body | 宽326dp；最多两个按钮；破坏性操作右侧为 error 色；不能点击遮罩误关关键操作 |
| CMP-022 | StkBottomSheet | overlay | content <= 80% screen | 24dp top | 16sp title;14sp item | 拖拽条32x4dp；每个选项最小48dp；安全区适配 |

## 组件优先顺序

1. 开发页面前先查公共组件；已有组件必须复用。
2. 公共组件无法覆盖真实需求时，只允许在 `core-designsystem` 新增或扩展，并同步组件板效果图和测试。
3. 业务模块不得复制一份“差不多”的按钮、输入框、顶部栏或卡片。
4. 公共组件变更必须重跑所有引用页面的核心 Golden 截图，不能只验收新增页面。

## 统一状态容器

所有数据页通过同一状态模型表达：`Initial / Loading / Content / Empty / OfflineWithCache / NetworkError / Timeout / ServerError / Unauthorized`。提交型页面额外使用 `Validating / Submitting / Success / Failure / RateLimited / Conflict`。页面只组合适用状态，不创建私有错误样式。
