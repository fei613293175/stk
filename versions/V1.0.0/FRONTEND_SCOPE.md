# 1.0.0 前端页面与状态范围

## 页面
| Page ID | 页面 | 类型 | 路由 | 布局 | 身份签名 |
|---|---|---|---|---|---|
| AUTH-001 | 登录 | PAGE | stk://auth/login | AUTH | 商推客品牌+密码/短信双模式+主登录按钮 |
| AUTH-002 | 注册 | PAGE | stk://auth/register | AUTH | 手机号+密码+确认密码+协议+注册按钮 |
| AUTH-003 | 找回并重置密码 | PAGE | stk://auth/reset-password | AUTH | 手机号+短信验证码+新密码+确认密码 |
| AUTH-004 | 用户协议 | PAGE | stk://legal/user-agreement | LEGAL | 固定标题+版本日期+协议正文 |
| AUTH-005 | 隐私政策 | PAGE | stk://legal/privacy-policy | LEGAL | 固定标题+版本日期+隐私正文 |
| AUTH-OV-001 | 安全验证码弹窗 | OVERLAY | overlay://captcha | OVERLAY | 验证码图片+输入框+刷新+确认 |
| AUTH-OV-002 | 账号与风控提示 | OVERLAY | overlay://auth-risk | OVERLAY | 状态图标+原因+恢复建议+确认 |
| COM-OV-002 | 阶段功能说明弹窗 | OVERLAY | overlay://stage-scope | OVERLAY | 功能名称+开放版本+返回 |
| DS-001 | 色彩、字体、间距与圆角规范板 | DESIGN_BOARD | design://tokens | DESIGN_BOARD | 颜色样本+字体层级+间距刻度+圆角样本 |
| DS-002 | 公共组件规范板 | COMPONENT_BOARD | design://components | DESIGN_BOARD | 顶部栏+底部导航+按钮+输入框+标签+卡片 |
| DS-003 | 全局状态与反馈规范板 | SYSTEM_STATE_BOARD | design://states | DESIGN_BOARD | 骨架+空状态+错误+成功+Snackbar+Dialog |
| HOME-001 | 首页项目流 | PAGE | stk://home | ROOT_FEED | 品牌顶部栏+搜索+分类+单列项目卡+底部导航 |
| HOME-004 | 项目详情 | PAGE | stk://project/{project_id} | DETAIL | 项目图片+标题简介+发布者+联系方式+底部操作 |
| ME-001 | 我的 | PAGE | stk://me | ROOT_PROFILE | 资料卡+双账户+会员卡+道具+常用功能+底部导航 |
| ME-OV-001 | 退出登录确认 | OVERLAY | overlay://logout | OVERLAY | 退出说明+取消/确认退出 |
| SYS-001 | 启动与路由判定 | PAGE | stk://bootstrap | SPLASH | 品牌标识+启动状态+恢复入口 |
| SYS-002 | 维护、离线与服务不可用 | PAGE | stk://system/unavailable | RESULT | 系统状态插图+明确原因+重试/退出 |

## 本版新增/增强状态
| State ID | Page ID | 状态 | 说明 | 效果图 |
|---|---|---|---|---|
| DS-001-S01 | DS-001 | 完整规范 | 展示最终色值、字号、行高、字重、间距和圆角标尺 | ui/mockups/approved/V1.0.0/DS-001/M-DS-001-S01__REFERENCE__1170x2532.png |
| DS-002-S01 | DS-002 | 完整组件 | 展示所有公共组件默认、按下、禁用和错误样式 | ui/mockups/approved/V1.0.0/DS-002/M-DS-002-S01__REFERENCE__1170x2532.png |
| DS-003-S01 | DS-003 | 完整状态 | 展示加载、空、离线、错误、成功、Snackbar、Dialog 和 Bottom Sheet | ui/mockups/approved/V1.0.0/DS-003/M-DS-003-S01__REFERENCE__1170x2532.png |
| SYS-001-S01 | SYS-001 | 启动检查 | 本地原生首帧，检查配置、登录态和版本 | ui/mockups/approved/V1.0.0/SYS-001/M-SYS-001-S01__CHECKING__1170x2532.png |
| SYS-001-S02 | SYS-001 | 需要登录 | 无有效令牌，转入登录页前的短暂状态 | ui/mockups/approved/V1.0.0/SYS-001/M-SYS-001-S02__AUTH_REQUIRED__1170x2532.png |
| SYS-001-S03 | SYS-001 | 进入首页 | 引导进入已缓存首页内容 | ui/mockups/approved/V1.0.0/SYS-001/M-SYS-001-S03__READY__1170x2532.png |
| SYS-001-S08 | SYS-001 | 启动配置失败 | 显示重试，不出现白屏 | ui/mockups/approved/V1.0.0/SYS-001/M-SYS-001-S08__BOOTSTRAP_FAILED__1170x2532.png |
| SYS-002-S01 | SYS-002 | 系统维护 | 后台配置维护文案和预计恢复说明 | ui/mockups/approved/V1.0.0/SYS-002/M-SYS-002-S01__MAINTENANCE__1170x2532.png |
| SYS-002-S02 | SYS-002 | 服务不可用 | 后端暂时不可用并提供重试 | ui/mockups/approved/V1.0.0/SYS-002/M-SYS-002-S02__SERVICE_UNAVAILABLE__1170x2532.png |
| SYS-002-S03 | SYS-002 | 离线且无缓存 | 首次使用无网络，提供检查网络和重试 | ui/mockups/approved/V1.0.0/SYS-002/M-SYS-002-S03__OFFLINE_NO_CACHE__1170x2532.png |
| SYS-002-S04 | SYS-002 | 系统异常 | 服务端错误，显示请求编号供排查 | ui/mockups/approved/V1.0.0/SYS-002/M-SYS-002-S04__SERVER_ERROR__1170x2532.png |
| COM-OV-002-S01 | COM-OV-002 | 发布尚未开放 | V1.0/V1.1 点击发布时显示 V1.2 开放 | ui/mockups/approved/V1.0.0/COM-OV-002/M-COM-OV-002-S01__PUBLISH_STAGE__1170x2532.png |
| AUTH-001-S01 | AUTH-001 | 密码登录默认 | 手机号与密码表单 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S01__PASSWORD_DEFAULT__1170x2532.png |
| AUTH-001-S02 | AUTH-001 | 短信登录默认 | 手机号、短信码和发送按钮 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S02__SMS_DEFAULT__1170x2532.png |
| AUTH-001-S03 | AUTH-001 | 输入聚焦 | 键盘和输入高亮 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S03__FIELD_FOCUSED__1170x2532.png |
| AUTH-001-S04 | AUTH-001 | 本地校验错误 | 手机号/密码/验证码格式提示 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S04__VALIDATION_ERROR__1170x2532.png |
| AUTH-001-S05 | AUTH-001 | 短信发送中 | 发送按钮加载且防重复 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S05__SMS_SENDING__1170x2532.png |
| AUTH-001-S06 | AUTH-001 | 短信已发送 | 倒计时和有效期提示 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S06__SMS_SENT__1170x2532.png |
| AUTH-001-S07 | AUTH-001 | 等待安全验证码 | 弹出验证码且主表单保持 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S07__CAPTCHA_REQUIRED__1170x2532.png |
| AUTH-001-S08 | AUTH-001 | 登录提交中 | 主按钮加载且输入锁定 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S08__SUBMITTING__1170x2532.png |
| AUTH-001-S09 | AUTH-001 | 登录成功 | 短暂成功反馈后按规则跳转 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S09__SUCCESS__1170x2532.png |
| AUTH-001-S10 | AUTH-001 | 手机号或密码错误 | 统一错误文案，避免账户枚举 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S10__CREDENTIAL_ERROR__1170x2532.png |
| AUTH-001-S11 | AUTH-001 | 短信验证码错误 | 保留手机号并允许重试 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S11__SMS_CODE_ERROR__1170x2532.png |
| AUTH-001-S12 | AUTH-001 | 短信验证码过期 | 提示重新发送 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S12__SMS_CODE_EXPIRED__1170x2532.png |
| AUTH-001-S13 | AUTH-001 | 操作过于频繁 | 展示可重试时间 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S13__RATE_LIMITED__1170x2532.png |
| AUTH-001-S14 | AUTH-001 | 账号受限 | 打开风控提示 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S14__ACCOUNT_BLOCKED__1170x2532.png |
| AUTH-001-S15 | AUTH-001 | 网络错误 | 保留输入并提供重试 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S15__NETWORK_ERROR__1170x2532.png |
| AUTH-001-S16 | AUTH-001 | 请求超时 | 保留输入并提供重试 | ui/mockups/approved/V1.0.0/AUTH-001/M-AUTH-001-S16__TIMEOUT__1170x2532.png |
| AUTH-002-S01 | AUTH-002 | 注册默认 | 手机号、密码、确认密码和协议 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S01__DEFAULT__1170x2532.png |
| AUTH-002-S02 | AUTH-002 | 输入聚焦 | 键盘与聚焦样式 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S02__FIELD_FOCUSED__1170x2532.png |
| AUTH-002-S03 | AUTH-002 | 手机号已注册 | 引导返回登录或找回密码 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S03__PHONE_EXISTS__1170x2532.png |
| AUTH-002-S04 | AUTH-002 | 两次密码不一致 | 确认密码错误提示 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S04__PASSWORD_MISMATCH__1170x2532.png |
| AUTH-002-S05 | AUTH-002 | 未同意协议 | 协议勾选区域高亮 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S05__AGREEMENT_REQUIRED__1170x2532.png |
| AUTH-002-S06 | AUTH-002 | 其他校验错误 | 密码强度或手机号格式 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S06__VALIDATION_ERROR__1170x2532.png |
| AUTH-002-S07 | AUTH-002 | 等待安全验证码 | 注册提交前弹出 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S07__CAPTCHA_REQUIRED__1170x2532.png |
| AUTH-002-S08 | AUTH-002 | 注册提交中 | 禁止重复提交 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S08__SUBMITTING__1170x2532.png |
| AUTH-002-S09 | AUTH-002 | 注册成功 | 创建真实 Discuz UID 后进入配置跳转 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S09__SUCCESS__1170x2532.png |
| AUTH-002-S10 | AUTH-002 | 注册限流 | 展示重试时间 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S10__RATE_LIMITED__1170x2532.png |
| AUTH-002-S11 | AUTH-002 | 网络错误 | 保留表单 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S11__NETWORK_ERROR__1170x2532.png |
| AUTH-002-S12 | AUTH-002 | 注册失败 | 显示请求编号并可重试 | ui/mockups/approved/V1.0.0/AUTH-002/M-AUTH-002-S12__SERVER_ERROR__1170x2532.png |
| AUTH-003-S01 | AUTH-003 | 重置默认 | 手机号、短信码、新密码 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S01__DEFAULT__1170x2532.png |
| AUTH-003-S02 | AUTH-003 | 发送或提交前安全验证 | 动作绑定票据 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S02__CAPTCHA_REQUIRED__1170x2532.png |
| AUTH-003-S03 | AUTH-003 | 短信发送中 | 防重复 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S03__SMS_SENDING__1170x2532.png |
| AUTH-003-S04 | AUTH-003 | 短信已发送 | 倒计时 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S04__SMS_SENT__1170x2532.png |
| AUTH-003-S05 | AUTH-003 | 表单校验错误 | 代码、密码或确认密码 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S05__VALIDATION_ERROR__1170x2532.png |
| AUTH-003-S06 | AUTH-003 | 重置提交中 | 按钮加载 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S06__SUBMITTING__1170x2532.png |
| AUTH-003-S07 | AUTH-003 | 重置成功 | 清理旧令牌并返回登录 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S07__SUCCESS__1170x2532.png |
| AUTH-003-S08 | AUTH-003 | 短信码过期 | 提示重新发送 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S08__CODE_EXPIRED__1170x2532.png |
| AUTH-003-S09 | AUTH-003 | 操作限流 | 显示重试时间 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S09__RATE_LIMITED__1170x2532.png |
| AUTH-003-S10 | AUTH-003 | 网络错误 | 保留表单 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S10__NETWORK_ERROR__1170x2532.png |
| AUTH-003-S11 | AUTH-003 | 重置失败 | 请求编号和重试 | ui/mockups/approved/V1.0.0/AUTH-003/M-AUTH-003-S11__SERVER_ERROR__1170x2532.png |
| AUTH-004-S01 | AUTH-004 | 协议加载 | 读取版本化协议 | ui/mockups/approved/V1.0.0/AUTH-004/M-AUTH-004-S01__LOADING__1170x2532.png |
| AUTH-004-S02 | AUTH-004 | 协议正文 | 显示完整正文和生效日期 | ui/mockups/approved/V1.0.0/AUTH-004/M-AUTH-004-S02__CONTENT__1170x2532.png |
| AUTH-004-S03 | AUTH-004 | 离线缓存 | 显示已缓存版本和离线标识 | ui/mockups/approved/V1.0.0/AUTH-004/M-AUTH-004-S03__OFFLINE_CACHED__1170x2532.png |
| AUTH-004-S04 | AUTH-004 | 加载失败 | 重试 | ui/mockups/approved/V1.0.0/AUTH-004/M-AUTH-004-S04__ERROR__1170x2532.png |
| AUTH-005-S01 | AUTH-005 | 政策加载 | 读取版本化隐私政策 | ui/mockups/approved/V1.0.0/AUTH-005/M-AUTH-005-S01__LOADING__1170x2532.png |
| AUTH-005-S02 | AUTH-005 | 政策正文 | 显示完整正文和生效日期 | ui/mockups/approved/V1.0.0/AUTH-005/M-AUTH-005-S02__CONTENT__1170x2532.png |
| AUTH-005-S03 | AUTH-005 | 离线缓存 | 显示已缓存版本和离线标识 | ui/mockups/approved/V1.0.0/AUTH-005/M-AUTH-005-S03__OFFLINE_CACHED__1170x2532.png |
| AUTH-005-S04 | AUTH-005 | 加载失败 | 重试 | ui/mockups/approved/V1.0.0/AUTH-005/M-AUTH-005-S04__ERROR__1170x2532.png |
| AUTH-OV-001-S01 | AUTH-OV-001 | 验证码加载 | 加载图片和 challenge | ui/mockups/approved/V1.0.0/AUTH-OV-001/M-AUTH-OV-001-S01__LOADING__1170x2532.png |
| AUTH-OV-001-S02 | AUTH-OV-001 | 等待输入 | 验证码图片、输入和刷新 | ui/mockups/approved/V1.0.0/AUTH-OV-001/M-AUTH-OV-001-S02__READY__1170x2532.png |
| AUTH-OV-001-S03 | AUTH-OV-001 | 输入错误 | 错误反馈并计数 | ui/mockups/approved/V1.0.0/AUTH-OV-001/M-AUTH-OV-001-S03__INPUT_ERROR__1170x2532.png |
| AUTH-OV-001-S04 | AUTH-OV-001 | 验证码已过期 | 自动刷新 challenge | ui/mockups/approved/V1.0.0/AUTH-OV-001/M-AUTH-OV-001-S04__EXPIRED__1170x2532.png |
| AUTH-OV-001-S05 | AUTH-OV-001 | 刷新中 | 防连续刷新 | ui/mockups/approved/V1.0.0/AUTH-OV-001/M-AUTH-OV-001-S05__REFRESHING__1170x2532.png |
| AUTH-OV-001-S06 | AUTH-OV-001 | 验证中 | 确认按钮加载 | ui/mockups/approved/V1.0.0/AUTH-OV-001/M-AUTH-OV-001-S06__VERIFYING__1170x2532.png |
| AUTH-OV-001-S07 | AUTH-OV-001 | 验证成功 | 返回一次性 ticket | ui/mockups/approved/V1.0.0/AUTH-OV-001/M-AUTH-OV-001-S07__SUCCESS__1170x2532.png |
| AUTH-OV-001-S08 | AUTH-OV-001 | 验证码网络失败 | 重试或取消 | ui/mockups/approved/V1.0.0/AUTH-OV-001/M-AUTH-OV-001-S08__NETWORK_ERROR__1170x2532.png |
| AUTH-OV-002-S01 | AUTH-OV-002 | 登录暂时锁定 | 显示解锁时间 | ui/mockups/approved/V1.0.0/AUTH-OV-002/M-AUTH-OV-002-S01__LOGIN_LOCKED__1170x2532.png |
| AUTH-OV-002-S02 | AUTH-OV-002 | 账号被禁用 | 显示客服入口 | ui/mockups/approved/V1.0.0/AUTH-OV-002/M-AUTH-OV-002-S02__ACCOUNT_DISABLED__1170x2532.png |
| AUTH-OV-002-S03 | AUTH-OV-002 | 需要人工核验 | 不暴露内部风控规则 | ui/mockups/approved/V1.0.0/AUTH-OV-002/M-AUTH-OV-002-S03__RISK_REVIEW__1170x2532.png |
| AUTH-OV-002-S04 | AUTH-OV-002 | 登录方式暂不可用 | 提示使用另一登录方式 | ui/mockups/approved/V1.0.0/AUTH-OV-002/M-AUTH-OV-002-S04__SERVICE_DISABLED__1170x2532.png |
| HOME-001-S01 | HOME-001 | 首次骨架 | 无缓存时显示结构化骨架 | ui/mockups/approved/V1.0.0/HOME-001/M-HOME-001-S01__FIRST_LOADING__1170x2532.png |
| HOME-001-S02 | HOME-001 | 正常内容 | 缓存或网络项目流 | ui/mockups/approved/V1.0.0/HOME-001/M-HOME-001-S02__CONTENT__1170x2532.png |
| HOME-001-S03 | HOME-001 | 下拉刷新 | 保留列表并显示刷新进度 | ui/mockups/approved/V1.0.0/HOME-001/M-HOME-001-S03__REFRESHING__1170x2532.png |
| HOME-001-S05 | HOME-001 | 加载下一页 | 底部加载 | ui/mockups/approved/V1.0.0/HOME-001/M-HOME-001-S05__PAGINATING__1170x2532.png |
| HOME-001-S06 | HOME-001 | 下一页失败 | 保留已有列表并局部重试 | ui/mockups/approved/V1.0.0/HOME-001/M-HOME-001-S06__PAGINATION_FAILED__1170x2532.png |
| HOME-001-S07 | HOME-001 | 暂无项目 | 后台无 published 项目 | ui/mockups/approved/V1.0.0/HOME-001/M-HOME-001-S07__EMPTY__1170x2532.png |
| HOME-001-S10 | HOME-001 | 网络错误 | 首屏重试 | ui/mockups/approved/V1.0.0/HOME-001/M-HOME-001-S10__NETWORK_ERROR__1170x2532.png |
| HOME-001-S11 | HOME-001 | 请求超时 | 首屏超时重试 | ui/mockups/approved/V1.0.0/HOME-001/M-HOME-001-S11__TIMEOUT__1170x2532.png |
| HOME-001-S12 | HOME-001 | 服务异常 | 请求编号和重试 | ui/mockups/approved/V1.0.0/HOME-001/M-HOME-001-S12__SERVER_ERROR__1170x2532.png |
| HOME-001-S13 | HOME-001 | 登录过期 | 转登录前提示 | ui/mockups/approved/V1.0.0/HOME-001/M-HOME-001-S13__SESSION_EXPIRED__1170x2532.png |
| HOME-004-S01 | HOME-004 | 详情加载 | 图片与正文骨架 | ui/mockups/approved/V1.0.0/HOME-004/M-HOME-004-S01__LOADING__1170x2532.png |
| HOME-004-S02 | HOME-004 | 公开项目详情 | published 项目完整内容 | ui/mockups/approved/V1.0.0/HOME-004/M-HOME-004-S02__PUBLIC_CONTENT__1170x2532.png |
| HOME-004-S07 | HOME-004 | 图片加载失败 | 占位图但正文可用 | ui/mockups/approved/V1.0.0/HOME-004/M-HOME-004-S07__IMAGE_FAILED__1170x2532.png |
| HOME-004-S08 | HOME-004 | 项目不存在 | 返回首页 | ui/mockups/approved/V1.0.0/HOME-004/M-HOME-004-S08__NOT_FOUND__1170x2532.png |
| HOME-004-S09 | HOME-004 | 项目不可公开 | 非本人访问未发布项目 | ui/mockups/approved/V1.0.0/HOME-004/M-HOME-004-S09__UNAVAILABLE__1170x2532.png |
| HOME-004-S10 | HOME-004 | 详情网络错误 | 重试 | ui/mockups/approved/V1.0.0/HOME-004/M-HOME-004-S10__NETWORK_ERROR__1170x2532.png |
| HOME-004-S11 | HOME-004 | 详情服务异常 | 请求编号和重试 | ui/mockups/approved/V1.0.0/HOME-004/M-HOME-004-S11__SERVER_ERROR__1170x2532.png |
| ME-001-S01 | ME-001 | 资料加载 | 资料卡与模块骨架 | ui/mockups/approved/V1.0.0/ME-001/M-ME-001-S01__LOADING__1170x2532.png |
| ME-001-S02 | ME-001 | V1 基础资料 | 头像、昵称、UID、脱敏手机号和我的发布阶段入口 | ui/mockups/approved/V1.0.0/ME-001/M-ME-001-S02__BASIC_V1__1170x2532.png |
| ME-001-S07 | ME-001 | 资料加载失败 | 重试 | ui/mockups/approved/V1.0.0/ME-001/M-ME-001-S07__ERROR__1170x2532.png |
| ME-001-S08 | ME-001 | 登录过期 | 清理敏感内容并登录 | ui/mockups/approved/V1.0.0/ME-001/M-ME-001-S08__SESSION_EXPIRED__1170x2532.png |
| ME-OV-001-S01 | ME-OV-001 | 确认退出 | 取消或退出 | ui/mockups/approved/V1.0.0/ME-OV-001/M-ME-OV-001-S01__DEFAULT__1170x2532.png |
| ME-OV-001-S02 | ME-OV-001 | 退出中 | 撤销令牌 | ui/mockups/approved/V1.0.0/ME-OV-001/M-ME-OV-001-S02__SUBMITTING__1170x2532.png |
| ME-OV-001-S03 | ME-OV-001 | 退出请求失败 | 本地安全退出并记录待撤销 | ui/mockups/approved/V1.0.0/ME-OV-001/M-ME-OV-001-S03__FAILED__1170x2532.png |

## 本版新增交互
| Interaction ID | Page ID | 动作 | testTag | 目标/API | 预期 |
|---|---|---|---|---|---|
| INT-NAV-001 | HOME-001 | 底部导航进入首页 | nav_home | LOCAL_NAVIGATION | 进入首页并保留或恢复滚动位置；在首页再次点击回到顶部 |
| INT-NAV-002 | HOME-001 | 底部导航进入发布 | nav_publish | LOCAL_VERSIONED_NAVIGATION | V1.0/V1.1 打开阶段说明；V1.2 起进入 PUB-001 |
| INT-NAV-003 | HOME-001 | 底部导航进入我的 | nav_me | LOCAL_NAVIGATION | 进入我的页面并保留首页状态 |
| INT-SYS-001 | SYS-001 | 重试启动配置 | bootstrap_retry | getBootstrap | 产生合同规定的唯一结果 |
| INT-SYS-002 | SYS-001 | 离线进入缓存首页 | bootstrap_continue_offline | LOCAL_CACHE_ROUTE | 产生合同规定的唯一结果 |
| INT-SYS-003 | SYS-002 | 服务异常重试 | system_retry | LOCAL_RETRY | 产生合同规定的唯一结果 |
| INT-STAGE-001 | COM-OV-002 | 关闭阶段说明 | stage_scope_close | LOCAL_DISMISS | 产生合同规定的唯一结果 |
| INT-AUTH-001 | AUTH-001 | 切换密码登录 | login_mode_password | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-AUTH-002 | AUTH-001 | 切换短信登录 | login_mode_sms | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-AUTH-003 | AUTH-001 | 输入手机号 | login_phone | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-AUTH-004 | AUTH-001 | 输入登录密码 | login_password | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-AUTH-005 | AUTH-001 | 切换密码可见性 | login_password_visibility | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-AUTH-006 | AUTH-001 | 输入短信验证码 | login_sms_code | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-AUTH-007 | AUTH-001 | 发送登录短信验证码 | login_send_sms | sendSmsCode | 安全验证码通过后服务端发送阿里云短信并启动倒计时 |
| INT-AUTH-008 | AUTH-001 | 提交密码登录 | login_password_submit | loginWithPassword | 产生合同规定的唯一结果 |
| INT-AUTH-009 | AUTH-001 | 提交短信登录 | login_sms_submit | loginWithSms | 产生合同规定的唯一结果 |
| INT-AUTH-010 | AUTH-001 | 进入注册 | login_open_register | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-AUTH-011 | AUTH-001 | 进入找回密码 | login_open_reset | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-AUTH-012 | AUTH-001 | 打开用户协议 | login_open_agreement | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-AUTH-013 | AUTH-001 | 打开隐私政策 | login_open_privacy | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-REG-001 | AUTH-002 | 输入注册手机号 | register_phone | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-REG-002 | AUTH-002 | 输入注册密码 | register_password | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-REG-003 | AUTH-002 | 输入确认密码 | register_password_confirm | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-REG-004 | AUTH-002 | 切换密码可见性 | register_password_visibility | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-REG-005 | AUTH-002 | 同意用户协议 | register_agreement_check | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-REG-006 | AUTH-002 | 提交注册 | register_submit | registerUser | 不发送短信；创建 Discuz UID、手机号绑定、会员基础记录和双账户记录 |
| INT-REG-007 | AUTH-002 | 返回登录 | register_back_login | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-REG-008 | AUTH-002 | 注册页打开协议 | register_open_agreement | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-REG-009 | AUTH-002 | 注册页打开隐私政策 | register_open_privacy | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-RESET-001 | AUTH-003 | 输入重置手机号 | reset_phone | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-RESET-002 | AUTH-003 | 发送重置短信 | reset_send_sms | sendSmsCode | 产生合同规定的唯一结果 |
| INT-RESET-003 | AUTH-003 | 输入重置短信码 | reset_sms_code | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-RESET-004 | AUTH-003 | 输入新密码 | reset_new_password | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-RESET-005 | AUTH-003 | 输入确认新密码 | reset_new_password_confirm | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-RESET-006 | AUTH-003 | 提交密码重置 | reset_submit | resetPassword | 产生合同规定的唯一结果 |
| INT-RESET-007 | AUTH-003 | 重置页返回登录 | reset_back_login | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-LEGAL-001 | AUTH-004 | 用户协议返回 | agreement_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-LEGAL-002 | AUTH-004 | 重试加载用户协议 | agreement_retry | getLegalDocument | 产生合同规定的唯一结果 |
| INT-LEGAL-003 | AUTH-005 | 隐私政策返回 | privacy_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-LEGAL-004 | AUTH-005 | 重试加载隐私政策 | privacy_retry | getLegalDocument | 产生合同规定的唯一结果 |
| INT-CAPTCHA-001 | AUTH-OV-001 | 输入安全验证码 | captcha_input | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-CAPTCHA-002 | AUTH-OV-001 | 刷新安全验证码 | captcha_refresh | getCaptchaChallenge | 产生合同规定的唯一结果 |
| INT-CAPTCHA-003 | AUTH-OV-001 | 确认安全验证码 | captcha_confirm | verifyCaptcha | 产生合同规定的唯一结果 |
| INT-CAPTCHA-004 | AUTH-OV-001 | 取消安全验证码 | captcha_cancel | LOCAL_DISMISS | 产生合同规定的唯一结果 |
| INT-RISK-001 | AUTH-OV-002 | 关闭风控提示 | auth_risk_confirm | LOCAL_DISMISS | 产生合同规定的唯一结果 |
| INT-HOME-001 | HOME-001 | 下拉刷新项目流 | home_pull_refresh | listProjects | 产生合同规定的唯一结果 |
| INT-HOME-004 | HOME-001 | 打开项目详情 | project_card_{projectId} | getProjectDetail | 产生合同规定的唯一结果 |
| INT-HOME-005 | HOME-001 | 下一页自动加载 | home_project_list | listProjects | 产生合同规定的唯一结果 |
| INT-HOME-006 | HOME-001 | 重试首屏 | home_retry | listProjects | 产生合同规定的唯一结果 |
| INT-HOME-007 | HOME-001 | 重试下一页 | home_load_more_retry | listProjects | 产生合同规定的唯一结果 |
| INT-HOME-008 | HOME-001 | 顶部头像进入我的 | home_avatar | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-DETAIL-001 | HOME-004 | 详情返回 | project_detail_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-DETAIL-005 | HOME-004 | 详情重试 | project_detail_retry | getProjectDetail | 产生合同规定的唯一结果 |
| INT-ME-011 | ME-001 | 我的页面重试 | me_retry | getMySummary | 产生合同规定的唯一结果 |
| INT-LOGOUT-001 | ME-OV-001 | 确认退出登录 | logout_confirm | logout | 产生合同规定的唯一结果 |
| INT-LOGOUT-002 | ME-OV-001 | 取消退出登录 | logout_cancel | LOCAL_DISMISS | 产生合同规定的唯一结果 |

所有页面必须同时遵循同目录 `UI_FIXED_RULES.md`。没有列入本表的控件不能新增业务行为。
