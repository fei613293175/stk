# 1.3.0 前端页面与状态范围

## 页面
| Page ID | 页面 | 类型 | 路由 | 布局 | 身份签名 |
|---|---|---|---|---|---|
| COM-OV-002 | 阶段功能说明弹窗 | OVERLAY | overlay://stage-scope | OVERLAY | 功能名称+开放版本+返回 |
| ME-001 | 我的 | PAGE | stk://me | ROOT_PROFILE | 资料卡+双账户+会员卡+道具+常用功能+底部导航 |
| ME-003 | 个人资料只读页 | PAGE | stk://me/profile | STANDARD | 头像+昵称+UID+脱敏手机号+注册时间 |
| ME-004 | 会员中心展示 | PAGE | stk://me/member | STANDARD | 会员卡+权益说明+状态+有效期 |
| ME-005 | 账户余额展示 | PAGE | stk://me/wallets | STANDARD | 佣金账户+任务账户+只读说明 |
| ME-006 | 道具中心展示 | PAGE | stk://me/props | STANDARD | 刷新卡/超级头条/头条/变色卡展示 |
| ME-007 | 浏览记录占位 | PAGE | stk://me/history | PLACEHOLDER | 功能图标+范围说明+返回 |
| ME-008 | 收藏占位 | PAGE | stk://me/favorites | PLACEHOLDER | 功能图标+范围说明+返回 |
| ME-009 | 实名认证占位 | PAGE | stk://me/real-name | PLACEHOLDER | 安全图标+未开放说明+返回 |
| ME-010 | 联系客服 | PAGE | stk://me/support | STANDARD | 客服说明+联系方式+复制/外部打开 |
| ME-011 | 设置 | PAGE | stk://me/settings | STANDARD | 清理缓存+协议+隐私+关于+退出登录 |
| ME-012 | 关于商推客 | PAGE | stk://me/about | STANDARD | Logo+版本号+更新检查+备案/版权信息 |
| SYS-002 | 维护、离线与服务不可用 | PAGE | stk://system/unavailable | RESULT | 系统状态插图+明确原因+重试/退出 |

## 本版新增/增强状态
| State ID | Page ID | 状态 | 说明 | 效果图 |
|---|---|---|---|---|
| COM-OV-002-S02 | COM-OV-002 | 会员在线开通未开放 | 只展示会员状态和权益 | ui/mockups/approved/V1.3.0/COM-OV-002/M-COM-OV-002-S02__MEMBER_STAGE__1170x2532.png |
| COM-OV-002-S03 | COM-OV-002 | 账户操作未开放 | 只展示余额 | ui/mockups/approved/V1.3.0/COM-OV-002/M-COM-OV-002-S03__WALLET_STAGE__1170x2532.png |
| COM-OV-002-S04 | COM-OV-002 | 道具购买未开放 | 只展示道具 | ui/mockups/approved/V1.3.0/COM-OV-002/M-COM-OV-002-S04__PROP_STAGE__1170x2532.png |
| ME-001-S03 | ME-001 | 未开通会员 | 双账户、营销会员卡、道具与常用功能 | ui/mockups/approved/V1.3.0/ME-001/M-ME-001-S03__INACTIVE_MEMBER__1170x2532.png |
| ME-001-S04 | ME-001 | 已开通会员 | 等级和有效期 | ui/mockups/approved/V1.3.0/ME-001/M-ME-001-S04__ACTIVE_MEMBER__1170x2532.png |
| ME-001-S05 | ME-001 | 会员已过期 | 过期标识和仅展示状态 | ui/mockups/approved/V1.3.0/ME-001/M-ME-001-S05__EXPIRED_MEMBER__1170x2532.png |
| ME-001-S06 | ME-001 | 离线缓存 | 显示最近资料和离线提示 | ui/mockups/approved/V1.3.0/ME-001/M-ME-001-S06__OFFLINE_CACHED__1170x2532.png |
| ME-003-S01 | ME-003 | 资料加载 | 骨架 | ui/mockups/approved/V1.3.0/ME-003/M-ME-003-S01__LOADING__1170x2532.png |
| ME-003-S02 | ME-003 | 只读资料 | 真实基础资料 | ui/mockups/approved/V1.3.0/ME-003/M-ME-003-S02__CONTENT__1170x2532.png |
| ME-003-S03 | ME-003 | 加载失败 | 重试 | ui/mockups/approved/V1.3.0/ME-003/M-ME-003-S03__ERROR__1170x2532.png |
| ME-004-S01 | ME-004 | 会员加载 | 卡片骨架 | ui/mockups/approved/V1.3.0/ME-004/M-ME-004-S01__LOADING__1170x2532.png |
| ME-004-S02 | ME-004 | 未开通 | 展示后台配置权益与阶段提示 | ui/mockups/approved/V1.3.0/ME-004/M-ME-004-S02__INACTIVE__1170x2532.png |
| ME-004-S03 | ME-004 | 有效会员 | 等级、开始和到期日期 | ui/mockups/approved/V1.3.0/ME-004/M-ME-004-S03__ACTIVE__1170x2532.png |
| ME-004-S04 | ME-004 | 已过期 | 历史状态和到期日期 | ui/mockups/approved/V1.3.0/ME-004/M-ME-004-S04__EXPIRED__1170x2532.png |
| ME-004-S05 | ME-004 | 会员加载失败 | 重试 | ui/mockups/approved/V1.3.0/ME-004/M-ME-004-S05__ERROR__1170x2532.png |
| ME-005-S01 | ME-005 | 账户加载 | 双卡骨架 | ui/mockups/approved/V1.3.0/ME-005/M-ME-005-S01__LOADING__1170x2532.png |
| ME-005-S02 | ME-005 | 只读余额 | 佣金和任务账户真实余额 | ui/mockups/approved/V1.3.0/ME-005/M-ME-005-S02__CONTENT__1170x2532.png |
| ME-005-S03 | ME-005 | 账户加载失败 | 重试 | ui/mockups/approved/V1.3.0/ME-005/M-ME-005-S03__ERROR__1170x2532.png |
| ME-006-S01 | ME-006 | 道具加载 | 卡片骨架 | ui/mockups/approved/V1.3.0/ME-006/M-ME-006-S01__LOADING__1170x2532.png |
| ME-006-S02 | ME-006 | 道具展示 | 后台配置的四类道具 | ui/mockups/approved/V1.3.0/ME-006/M-ME-006-S02__CONTENT__1170x2532.png |
| ME-006-S03 | ME-006 | 无展示道具 | 管理员未启用 | ui/mockups/approved/V1.3.0/ME-006/M-ME-006-S03__EMPTY__1170x2532.png |
| ME-006-S04 | ME-006 | 离线缓存 | 最近展示配置 | ui/mockups/approved/V1.3.0/ME-006/M-ME-006-S04__OFFLINE_CACHED__1170x2532.png |
| ME-006-S05 | ME-006 | 道具加载失败 | 重试 | ui/mockups/approved/V1.3.0/ME-006/M-ME-006-S05__ERROR__1170x2532.png |
| ME-007-S01 | ME-007 | 浏览记录未开放 | 明确一期不实现 | ui/mockups/approved/V1.3.0/ME-007/M-ME-007-S01__SCOPE_PLACEHOLDER__1170x2532.png |
| ME-008-S01 | ME-008 | 收藏未开放 | 明确一期不实现 | ui/mockups/approved/V1.3.0/ME-008/M-ME-008-S01__SCOPE_PLACEHOLDER__1170x2532.png |
| ME-009-S01 | ME-009 | 实名认证未开放 | 明确一期不采集身份信息 | ui/mockups/approved/V1.3.0/ME-009/M-ME-009-S01__SCOPE_PLACEHOLDER__1170x2532.png |
| ME-010-S01 | ME-010 | 客服配置加载 | 骨架 | ui/mockups/approved/V1.3.0/ME-010/M-ME-010-S01__LOADING__1170x2532.png |
| ME-010-S02 | ME-010 | 客服信息 | 电话/微信/网址按后台配置 | ui/mockups/approved/V1.3.0/ME-010/M-ME-010-S02__CONTENT__1170x2532.png |
| ME-010-S03 | ME-010 | 复制成功 | Snackbar | ui/mockups/approved/V1.3.0/ME-010/M-ME-010-S03__COPY_SUCCESS__1170x2532.png |
| ME-010-S04 | ME-010 | 外部应用不可用 | 回退 | ui/mockups/approved/V1.3.0/ME-010/M-ME-010-S04__APP_UNAVAILABLE__1170x2532.png |
| ME-010-S05 | ME-010 | 配置失败 | 重试 | ui/mockups/approved/V1.3.0/ME-010/M-ME-010-S05__ERROR__1170x2532.png |
| ME-011-S01 | ME-011 | 设置列表 | 仅已规划入口 | ui/mockups/approved/V1.3.0/ME-011/M-ME-011-S01__CONTENT__1170x2532.png |
| ME-011-S02 | ME-011 | 清理缓存中 | 进度 | ui/mockups/approved/V1.3.0/ME-011/M-ME-011-S02__CLEARING_CACHE__1170x2532.png |
| ME-011-S03 | ME-011 | 清理成功 | Snackbar | ui/mockups/approved/V1.3.0/ME-011/M-ME-011-S03__CACHE_CLEARED__1170x2532.png |
| ME-011-S04 | ME-011 | 清理失败 | 错误提示 | ui/mockups/approved/V1.3.0/ME-011/M-ME-011-S04__CACHE_CLEAR_FAILED__1170x2532.png |
| ME-011-S05 | ME-011 | 离线设置 | 本地操作可用，网络操作说明 | ui/mockups/approved/V1.3.0/ME-011/M-ME-011-S05__OFFLINE__1170x2532.png |
| ME-012-S01 | ME-012 | 关于信息 | 版本和品牌 | ui/mockups/approved/V1.3.0/ME-012/M-ME-012-S01__CONTENT__1170x2532.png |

## 本版新增交互
| Interaction ID | Page ID | 动作 | testTag | 目标/API | 预期 |
|---|---|---|---|---|---|
| INT-SYS-004 | SYS-002 | 打开客服 | system_open_support | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-ME-001 | ME-001 | 打开个人资料 | me_profile | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-ME-003 | ME-001 | 打开账户余额 | me_wallets | getMyWallets | 产生合同规定的唯一结果 |
| INT-ME-004 | ME-001 | 打开会员中心 | me_member | getMyMember | 产生合同规定的唯一结果 |
| INT-ME-005 | ME-001 | 打开道具中心 | me_props | getPropDisplay | 产生合同规定的唯一结果 |
| INT-ME-006 | ME-001 | 打开浏览记录占位 | me_history | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-ME-007 | ME-001 | 打开收藏占位 | me_favorites | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-ME-008 | ME-001 | 打开实名认证占位 | me_real_name | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-ME-009 | ME-001 | 打开客服 | me_support | getSupportConfig | 产生合同规定的唯一结果 |
| INT-ME-010 | ME-001 | 打开设置 | me_settings | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-PROFILE-001 | ME-003 | 个人资料返回 | profile_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-MEMBER-001 | ME-004 | 会员开通引导 | member_open_action | LOCAL_STAGE_SCOPE | 明确本期不实现在线开通，不创建订单或假支付 |
| INT-MEMBER-002 | ME-004 | 会员页返回 | member_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-WALLET-001 | ME-005 | 账户操作说明 | wallet_action_info | LOCAL_STAGE_SCOPE | 产生合同规定的唯一结果 |
| INT-WALLET-002 | ME-005 | 账户页返回 | wallet_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-PROP-001 | ME-006 | 查看道具说明 | prop_{propCode} | LOCAL_STAGE_SCOPE | 产生合同规定的唯一结果 |
| INT-PROP-002 | ME-006 | 道具页返回 | props_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-PLACE-001 | ME-007 | 占位页返回 | history_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-PLACE-002 | ME-008 | 占位页返回 | favorites_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-PLACE-003 | ME-009 | 占位页返回 | real_name_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-SUPPORT-001 | ME-010 | 复制客服信息 | support_copy | LOCAL_CLIPBOARD | 产生合同规定的唯一结果 |
| INT-SUPPORT-002 | ME-010 | 打开客服外部应用 | support_external | LOCAL_EXTERNAL_OPEN | 产生合同规定的唯一结果 |
| INT-SUPPORT-003 | ME-010 | 客服配置重试 | support_retry | getSupportConfig | 产生合同规定的唯一结果 |
| INT-SET-001 | ME-011 | 清理图片与列表缓存 | settings_clear_cache | LOCAL_CLEAR_CACHE | 产生合同规定的唯一结果 |
| INT-SET-002 | ME-011 | 设置页打开协议 | settings_agreement | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-SET-003 | ME-011 | 设置页打开隐私 | settings_privacy | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-SET-004 | ME-011 | 打开关于 | settings_about | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-SET-005 | ME-011 | 打开退出确认 | settings_logout | LOCAL_OVERLAY | 产生合同规定的唯一结果 |
| INT-ABOUT-002 | ME-012 | 关于页返回 | about_back | LOCAL_BACK | 产生合同规定的唯一结果 |

所有页面必须同时遵循同目录 `UI_FIXED_RULES.md`。没有列入本表的控件不能新增业务行为。
