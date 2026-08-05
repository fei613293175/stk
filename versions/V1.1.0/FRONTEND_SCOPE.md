# 1.1.0 前端页面与状态范围

## 页面
| Page ID | 页面 | 类型 | 路由 | 布局 | 身份签名 |
|---|---|---|---|---|---|
| COM-OV-001 | 受控外部链接确认 | OVERLAY | overlay://external-link | OVERLAY | 目标服务+域名+风险说明+继续/取消 |
| HOME-001 | 首页项目流 | PAGE | stk://home | ROOT_FEED | 品牌顶部栏+搜索+分类+单列项目卡+底部导航 |
| HOME-002 | 项目搜索 | PAGE | stk://search | SEARCH | 搜索输入+结果统计+项目列表 |
| HOME-003 | 项目分类筛选 | OVERLAY | overlay://categories | OVERLAY | 分类列表+当前选中+确认/重置 |
| HOME-004 | 项目详情 | PAGE | stk://project/{project_id} | DETAIL | 项目图片+标题简介+发布者+联系方式+底部操作 |
| HOME-OV-001 | 项目图片查看器 | OVERLAY | overlay://project-images | OVERLAY | 沉浸式图片+页码+缩放+关闭 |
| HOME-OV-002 | 联系方式操作面板 | OVERLAY | overlay://contact-actions | OVERLAY | 联系方式类型+脱敏/完整值+复制/拨打/打开 |
| SYS-001 | 启动与路由判定 | PAGE | stk://bootstrap | SPLASH | 品牌标识+启动状态+恢复入口 |

## 本版新增/增强状态
| State ID | Page ID | 状态 | 说明 | 效果图 |
|---|---|---|---|---|
| SYS-001-S04 | SYS-001 | 离线但有登录态 | 允许进入缓存首页并显示离线提示 | ui/mockups/approved/V1.1.0/SYS-001/M-SYS-001-S04__OFFLINE_WITH_SESSION__1170x2532.png |
| COM-OV-001-S01 | COM-OV-001 | 白名单外链确认 | 显示目标服务和域名 | ui/mockups/approved/V1.1.0/COM-OV-001/M-COM-OV-001-S01__ALLOWED__1170x2532.png |
| COM-OV-001-S02 | COM-OV-001 | 外链被阻断 | 非白名单地址不可继续 | ui/mockups/approved/V1.1.0/COM-OV-001/M-COM-OV-001-S02__BLOCKED__1170x2532.png |
| COM-OV-001-S03 | COM-OV-001 | 目标应用未安装 | 提供浏览器或复制回退 | ui/mockups/approved/V1.1.0/COM-OV-001/M-COM-OV-001-S03__APP_UNAVAILABLE__1170x2532.png |
| HOME-001-S04 | HOME-001 | 分类筛选结果 | 显示当前分类和项目 | ui/mockups/approved/V1.1.0/HOME-001/M-HOME-001-S04__FILTERED__1170x2532.png |
| HOME-001-S08 | HOME-001 | 离线缓存 | 保留缓存列表和离线提示 | ui/mockups/approved/V1.1.0/HOME-001/M-HOME-001-S08__OFFLINE_WITH_CACHE__1170x2532.png |
| HOME-001-S09 | HOME-001 | 离线无缓存 | 明确离线且无历史数据 | ui/mockups/approved/V1.1.0/HOME-001/M-HOME-001-S09__OFFLINE_EMPTY__1170x2532.png |
| HOME-002-S01 | HOME-002 | 搜索默认 | 空关键词和提示 | ui/mockups/approved/V1.1.0/HOME-002/M-HOME-002-S01__DEFAULT__1170x2532.png |
| HOME-002-S02 | HOME-002 | 输入中 | 清除按钮和键盘 | ui/mockups/approved/V1.1.0/HOME-002/M-HOME-002-S02__TYPING__1170x2532.png |
| HOME-002-S03 | HOME-002 | 搜索中 | 结果骨架 | ui/mockups/approved/V1.1.0/HOME-002/M-HOME-002-S03__LOADING__1170x2532.png |
| HOME-002-S04 | HOME-002 | 有结果 | 结果统计和列表 | ui/mockups/approved/V1.1.0/HOME-002/M-HOME-002-S04__RESULTS__1170x2532.png |
| HOME-002-S05 | HOME-002 | 无结果 | 建议更换关键词 | ui/mockups/approved/V1.1.0/HOME-002/M-HOME-002-S05__NO_RESULTS__1170x2532.png |
| HOME-002-S06 | HOME-002 | 离线缓存结果 | 只显示本地可匹配缓存 | ui/mockups/approved/V1.1.0/HOME-002/M-HOME-002-S06__OFFLINE_CACHED__1170x2532.png |
| HOME-002-S07 | HOME-002 | 搜索失败 | 重试 | ui/mockups/approved/V1.1.0/HOME-002/M-HOME-002-S07__ERROR__1170x2532.png |
| HOME-003-S01 | HOME-003 | 分类列表 | 全部分类和当前选择 | ui/mockups/approved/V1.1.0/HOME-003/M-HOME-003-S01__DEFAULT__1170x2532.png |
| HOME-003-S02 | HOME-003 | 选中分类 | 高亮并允许确认 | ui/mockups/approved/V1.1.0/HOME-003/M-HOME-003-S02__SELECTED__1170x2532.png |
| HOME-003-S03 | HOME-003 | 无可用分类 | 只保留全部 | ui/mockups/approved/V1.1.0/HOME-003/M-HOME-003-S03__EMPTY__1170x2532.png |
| HOME-003-S04 | HOME-003 | 分类加载失败 | 使用缓存或重试 | ui/mockups/approved/V1.1.0/HOME-003/M-HOME-003-S04__ERROR__1170x2532.png |
| HOME-004-S06 | HOME-004 | 离线缓存详情 | 联系方式动作按安全策略限制 | ui/mockups/approved/V1.1.0/HOME-004/M-HOME-004-S06__OFFLINE_CACHED__1170x2532.png |
| HOME-OV-001-S01 | HOME-OV-001 | 图片正常 | 当前图片和页码 | ui/mockups/approved/V1.1.0/HOME-OV-001/M-HOME-OV-001-S01__CONTENT__1170x2532.png |
| HOME-OV-001-S02 | HOME-OV-001 | 缩放查看 | 双指缩放和拖动 | ui/mockups/approved/V1.1.0/HOME-OV-001/M-HOME-OV-001-S02__ZOOMED__1170x2532.png |
| HOME-OV-001-S03 | HOME-OV-001 | 单图加载失败 | 重试或切换下一张 | ui/mockups/approved/V1.1.0/HOME-OV-001/M-HOME-OV-001-S03__LOAD_FAILED__1170x2532.png |
| HOME-OV-002-S01 | HOME-OV-002 | 手机号操作 | 复制或拨打 | ui/mockups/approved/V1.1.0/HOME-OV-002/M-HOME-OV-002-S01__PHONE__1170x2532.png |
| HOME-OV-002-S02 | HOME-OV-002 | 微信操作 | 复制微信号或尝试打开微信 | ui/mockups/approved/V1.1.0/HOME-OV-002/M-HOME-OV-002-S02__WECHAT__1170x2532.png |
| HOME-OV-002-S03 | HOME-OV-002 | QQ 操作 | 复制 QQ 或尝试打开 | ui/mockups/approved/V1.1.0/HOME-OV-002/M-HOME-OV-002-S03__QQ__1170x2532.png |
| HOME-OV-002-S04 | HOME-OV-002 | 网址操作 | 受控 Custom Tabs | ui/mockups/approved/V1.1.0/HOME-OV-002/M-HOME-OV-002-S04__WEBSITE__1170x2532.png |
| HOME-OV-002-S05 | HOME-OV-002 | 外链确认 | 显示目标域名 | ui/mockups/approved/V1.1.0/HOME-OV-002/M-HOME-OV-002-S05__CONFIRM_EXTERNAL__1170x2532.png |
| HOME-OV-002-S06 | HOME-OV-002 | 应用未安装 | 浏览器或复制回退 | ui/mockups/approved/V1.1.0/HOME-OV-002/M-HOME-OV-002-S06__APP_UNAVAILABLE__1170x2532.png |
| HOME-OV-002-S07 | HOME-OV-002 | 无效目标 | 阻断并记录 | ui/mockups/approved/V1.1.0/HOME-OV-002/M-HOME-OV-002-S07__INVALID_TARGET__1170x2532.png |

## 本版新增交互
| Interaction ID | Page ID | 动作 | testTag | 目标/API | 预期 |
|---|---|---|---|---|---|
| INT-EXT-001 | COM-OV-001 | 确认打开白名单外链 | external_continue | LOCAL_EXTERNAL_OPEN | 产生合同规定的唯一结果 |
| INT-EXT-002 | COM-OV-001 | 取消外部跳转 | external_cancel | LOCAL_DISMISS | 产生合同规定的唯一结果 |
| INT-HOME-002 | HOME-001 | 打开搜索 | home_search | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-HOME-003 | HOME-001 | 打开分类筛选 | home_category_filter | LOCAL_OVERLAY | 产生合同规定的唯一结果 |
| INT-SEARCH-001 | HOME-002 | 输入搜索关键词 | search_query | LOCAL_DEBOUNCE | 产生合同规定的唯一结果 |
| INT-SEARCH-002 | HOME-002 | 提交搜索 | search_submit | listProjects | 产生合同规定的唯一结果 |
| INT-SEARCH-003 | HOME-002 | 清空搜索 | search_clear | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-SEARCH-004 | HOME-002 | 打开搜索结果详情 | search_project_{projectId} | getProjectDetail | 产生合同规定的唯一结果 |
| INT-SEARCH-005 | HOME-002 | 搜索重试 | search_retry | listProjects | 产生合同规定的唯一结果 |
| INT-SEARCH-006 | HOME-002 | 返回首页 | search_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-CAT-001 | HOME-003 | 选择分类 | category_{categoryId} | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-CAT-002 | HOME-003 | 确认分类 | category_confirm | listProjects | 产生合同规定的唯一结果 |
| INT-CAT-003 | HOME-003 | 重置分类 | category_reset | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-CAT-004 | HOME-003 | 关闭分类 | category_close | LOCAL_DISMISS | 产生合同规定的唯一结果 |
| INT-DETAIL-002 | HOME-004 | 打开图片查看器 | project_image_{index} | LOCAL_OVERLAY | 产生合同规定的唯一结果 |
| INT-DETAIL-003 | HOME-004 | 打开联系方式 | project_contact | LOCAL_OVERLAY | 产生合同规定的唯一结果 |
| INT-DETAIL-004 | HOME-004 | 复制项目链接 | project_share | LOCAL_SHARE | 产生合同规定的唯一结果 |
| INT-IMG-001 | HOME-OV-001 | 切换项目图片 | image_viewer_pager | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-IMG-002 | HOME-OV-001 | 缩放项目图片 | image_viewer_image | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-IMG-003 | HOME-OV-001 | 重试图片加载 | image_viewer_retry | LOCAL_IMAGE_RETRY | 产生合同规定的唯一结果 |
| INT-IMG-004 | HOME-OV-001 | 关闭图片查看器 | image_viewer_close | LOCAL_DISMISS | 产生合同规定的唯一结果 |
| INT-CONTACT-001 | HOME-OV-002 | 复制联系方式 | contact_copy | LOCAL_CLIPBOARD | 产生合同规定的唯一结果 |
| INT-CONTACT-002 | HOME-OV-002 | 拨打手机号 | contact_call | LOCAL_DIAL | 产生合同规定的唯一结果 |
| INT-CONTACT-003 | HOME-OV-002 | 打开微信 | contact_open_wechat | LOCAL_EXTERNAL_OPEN | 产生合同规定的唯一结果 |
| INT-CONTACT-004 | HOME-OV-002 | 打开 QQ | contact_open_qq | LOCAL_EXTERNAL_OPEN | 产生合同规定的唯一结果 |
| INT-CONTACT-005 | HOME-OV-002 | 打开网址 | contact_open_url | LOCAL_EXTERNAL_OPEN | 产生合同规定的唯一结果 |
| INT-CONTACT-006 | HOME-OV-002 | 关闭联系面板 | contact_close | LOCAL_DISMISS | 产生合同规定的唯一结果 |

所有页面必须同时遵循同目录 `UI_FIXED_RULES.md`。没有列入本表的控件不能新增业务行为。
