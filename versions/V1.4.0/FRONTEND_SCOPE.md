# 1.4.0 前端页面与状态范围

## 页面
| Page ID | 页面 | 类型 | 路由 | 布局 | 身份签名 |
|---|---|---|---|---|---|
| ME-012 | 关于商推客 | PAGE | stk://me/about | STANDARD | Logo+版本号+更新检查+备案/版权信息 |
| SYS-001 | 启动与路由判定 | PAGE | stk://bootstrap | SPLASH | 品牌标识+启动状态+恢复入口 |
| SYS-002 | 维护、离线与服务不可用 | PAGE | stk://system/unavailable | RESULT | 系统状态插图+明确原因+重试/退出 |
| SYS-003 | 版本更新与下载状态 | PAGE | stk://system/update | STANDARD | 当前版本+新版本+更新日志+下载状态 |
| SYS-OV-001 | 可选更新弹窗 | SYSTEM_OVERLAY | overlay://optional-update | OVERLAY | 版本号+更新摘要+稍后/立即更新 |
| SYS-OV-002 | 强制更新弹窗 | SYSTEM_OVERLAY | overlay://forced-update | OVERLAY | 版本号+强制说明+立即更新 |

## 本版新增/增强状态
| State ID | Page ID | 状态 | 说明 | 效果图 |
|---|---|---|---|---|
| SYS-001-S05 | SYS-001 | 发现可选更新 | 触发可选更新弹窗 | ui/mockups/approved/V1.4.0/SYS-001/M-SYS-001-S05__OPTIONAL_UPDATE__1170x2532.png |
| SYS-001-S06 | SYS-001 | 发现强制更新 | 触发不可跳过更新弹窗 | ui/mockups/approved/V1.4.0/SYS-001/M-SYS-001-S06__FORCED_UPDATE__1170x2532.png |
| SYS-001-S07 | SYS-001 | 维护模式 | 转入维护页 | ui/mockups/approved/V1.4.0/SYS-001/M-SYS-001-S07__MAINTENANCE__1170x2532.png |
| SYS-002-S05 | SYS-002 | 服务已恢复 | 恢复提示并返回上一安全页面 | ui/mockups/approved/V1.4.0/SYS-002/M-SYS-002-S05__RECOVERED__1170x2532.png |
| SYS-003-S01 | SYS-003 | 检查版本 | 请求当前发布配置 | ui/mockups/approved/V1.4.0/SYS-003/M-SYS-003-S01__LOADING__1170x2532.png |
| SYS-003-S02 | SYS-003 | 已是最新版 | 展示当前版本及检查时间 | ui/mockups/approved/V1.4.0/SYS-003/M-SYS-003-S02__UP_TO_DATE__1170x2532.png |
| SYS-003-S03 | SYS-003 | 可选更新可用 | 展示新版本、大小和更新日志 | ui/mockups/approved/V1.4.0/SYS-003/M-SYS-003-S03__OPTIONAL_AVAILABLE__1170x2532.png |
| SYS-003-S04 | SYS-003 | 强制更新可用 | 展示最低版本限制 | ui/mockups/approved/V1.4.0/SYS-003/M-SYS-003-S04__FORCED_AVAILABLE__1170x2532.png |
| SYS-003-S05 | SYS-003 | 正在下载 | 展示进度、大小和取消策略 | ui/mockups/approved/V1.4.0/SYS-003/M-SYS-003-S05__DOWNLOADING__1170x2532.png |
| SYS-003-S06 | SYS-003 | 下载失败 | 保留重试并解释网络/存储错误 | ui/mockups/approved/V1.4.0/SYS-003/M-SYS-003-S06__DOWNLOAD_FAILED__1170x2532.png |
| SYS-003-S07 | SYS-003 | 等待安装 | 校验 SHA-256 后交给系统安装器 | ui/mockups/approved/V1.4.0/SYS-003/M-SYS-003-S07__READY_TO_INSTALL__1170x2532.png |
| SYS-003-S08 | SYS-003 | 安装受阻 | 未知来源权限或包签名不匹配提示 | ui/mockups/approved/V1.4.0/SYS-003/M-SYS-003-S08__INSTALL_BLOCKED__1170x2532.png |
| SYS-OV-001-S01 | SYS-OV-001 | 可选更新 | 立即更新或稍后处理 | ui/mockups/approved/V1.4.0/SYS-OV-001/M-SYS-OV-001-S01__DEFAULT__1170x2532.png |
| SYS-OV-001-S02 | SYS-OV-001 | 下载中 | 按钮锁定并显示进度 | ui/mockups/approved/V1.4.0/SYS-OV-001/M-SYS-OV-001-S02__DOWNLOADING__1170x2532.png |
| SYS-OV-001-S03 | SYS-OV-001 | 下载失败 | 允许重试或稍后 | ui/mockups/approved/V1.4.0/SYS-OV-001/M-SYS-OV-001-S03__FAILED__1170x2532.png |
| SYS-OV-002-S01 | SYS-OV-002 | 强制更新 | 只有立即更新按钮 | ui/mockups/approved/V1.4.0/SYS-OV-002/M-SYS-OV-002-S01__DEFAULT__1170x2532.png |
| SYS-OV-002-S02 | SYS-OV-002 | 强制更新下载中 | 不可关闭 | ui/mockups/approved/V1.4.0/SYS-OV-002/M-SYS-OV-002-S02__DOWNLOADING__1170x2532.png |
| SYS-OV-002-S03 | SYS-OV-002 | 强制更新失败 | 重试和客服入口 | ui/mockups/approved/V1.4.0/SYS-OV-002/M-SYS-OV-002-S03__FAILED__1170x2532.png |
| ME-012-S02 | ME-012 | 检查更新 | 进度 | ui/mockups/approved/V1.4.0/ME-012/M-ME-012-S02__CHECKING_UPDATE__1170x2532.png |
| ME-012-S03 | ME-012 | 已是最新版 | 结果提示 | ui/mockups/approved/V1.4.0/ME-012/M-ME-012-S03__UP_TO_DATE__1170x2532.png |
| ME-012-S04 | ME-012 | 发现更新 | 进入更新页 | ui/mockups/approved/V1.4.0/ME-012/M-ME-012-S04__UPDATE_AVAILABLE__1170x2532.png |
| ME-012-S05 | ME-012 | 检查失败 | 重试 | ui/mockups/approved/V1.4.0/ME-012/M-ME-012-S05__CHECK_FAILED__1170x2532.png |

## 本版新增交互
| Interaction ID | Page ID | 动作 | testTag | 目标/API | 预期 |
|---|---|---|---|---|---|
| INT-UPD-001 | SYS-003 | 检查更新 | update_check | getCurrentRelease | 产生合同规定的唯一结果 |
| INT-UPD-002 | SYS-003 | 开始下载更新 | update_download | DOWNLOAD_RELEASE_APK | 产生合同规定的唯一结果 |
| INT-UPD-003 | SYS-003 | 重试更新下载 | update_retry | DOWNLOAD_RELEASE_APK | 产生合同规定的唯一结果 |
| INT-UPD-004 | SYS-003 | 启动系统安装器 | update_install | LOCAL_PACKAGE_INSTALL | 产生合同规定的唯一结果 |
| INT-UPD-005 | SYS-OV-001 | 可选更新立即更新 | optional_update_now | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-UPD-006 | SYS-OV-001 | 可选更新稍后 | optional_update_later | LOCAL_DISMISS | 产生合同规定的唯一结果 |
| INT-UPD-007 | SYS-OV-002 | 强制更新立即更新 | forced_update_now | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-ABOUT-001 | ME-012 | 关于页检查更新 | about_check_update | getCurrentRelease | 产生合同规定的唯一结果 |

所有页面必须同时遵循同目录 `UI_FIXED_RULES.md`。没有列入本表的控件不能新增业务行为。
