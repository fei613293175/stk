# 1.0.0 自动测试与项目所有者验收

## 自动测试合同

本版共有 208 条首次引入测试合同；发布时还必须回归所有历史已发布 Feature 的核心交互和 Golden 状态。详细清单见 `contracts/test-catalog.csv`。

| Test ID | 层级 | 目标 | 名称 | 证据 |
|---|---|---|---|---|
| T-VIS-DS-001-S01 | ANDROID_SCREENSHOT | DS-001-S01 | 色彩、字体、间距与圆角规范板 - 完整规范 视觉比对 | actual/DS-001-S01.png + diff/DS-001-S01.png + baseline SHA + diff ratio |
| T-VIS-DS-002-S01 | ANDROID_SCREENSHOT | DS-002-S01 | 公共组件规范板 - 完整组件 视觉比对 | actual/DS-002-S01.png + diff/DS-002-S01.png + baseline SHA + diff ratio |
| T-VIS-DS-003-S01 | ANDROID_SCREENSHOT | DS-003-S01 | 全局状态与反馈规范板 - 完整状态 视觉比对 | actual/DS-003-S01.png + diff/DS-003-S01.png + baseline SHA + diff ratio |
| T-VIS-SYS-001-S01 | ANDROID_SCREENSHOT | SYS-001-S01 | 启动与路由判定 - 启动检查 视觉比对 | actual/SYS-001-S01.png + diff/SYS-001-S01.png + baseline SHA + diff ratio |
| T-VIS-SYS-001-S02 | ANDROID_SCREENSHOT | SYS-001-S02 | 启动与路由判定 - 需要登录 视觉比对 | actual/SYS-001-S02.png + diff/SYS-001-S02.png + baseline SHA + diff ratio |
| T-VIS-SYS-001-S03 | ANDROID_SCREENSHOT | SYS-001-S03 | 启动与路由判定 - 进入首页 视觉比对 | actual/SYS-001-S03.png + diff/SYS-001-S03.png + baseline SHA + diff ratio |
| T-VIS-SYS-001-S08 | ANDROID_SCREENSHOT | SYS-001-S08 | 启动与路由判定 - 启动配置失败 视觉比对 | actual/SYS-001-S08.png + diff/SYS-001-S08.png + baseline SHA + diff ratio |
| T-VIS-SYS-002-S01 | ANDROID_SCREENSHOT | SYS-002-S01 | 维护、离线与服务不可用 - 系统维护 视觉比对 | actual/SYS-002-S01.png + diff/SYS-002-S01.png + baseline SHA + diff ratio |
| T-VIS-SYS-002-S02 | ANDROID_SCREENSHOT | SYS-002-S02 | 维护、离线与服务不可用 - 服务不可用 视觉比对 | actual/SYS-002-S02.png + diff/SYS-002-S02.png + baseline SHA + diff ratio |
| T-VIS-SYS-002-S03 | ANDROID_SCREENSHOT | SYS-002-S03 | 维护、离线与服务不可用 - 离线且无缓存 视觉比对 | actual/SYS-002-S03.png + diff/SYS-002-S03.png + baseline SHA + diff ratio |
| T-VIS-SYS-002-S04 | ANDROID_SCREENSHOT | SYS-002-S04 | 维护、离线与服务不可用 - 系统异常 视觉比对 | actual/SYS-002-S04.png + diff/SYS-002-S04.png + baseline SHA + diff ratio |
| T-VIS-COM-OV-002-S01 | ANDROID_SCREENSHOT | COM-OV-002-S01 | 阶段功能说明弹窗 - 发布尚未开放 视觉比对 | actual/COM-OV-002-S01.png + diff/COM-OV-002-S01.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S01 | ANDROID_SCREENSHOT | AUTH-001-S01 | 登录 - 密码登录默认 视觉比对 | actual/AUTH-001-S01.png + diff/AUTH-001-S01.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S02 | ANDROID_SCREENSHOT | AUTH-001-S02 | 登录 - 短信登录默认 视觉比对 | actual/AUTH-001-S02.png + diff/AUTH-001-S02.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S03 | ANDROID_SCREENSHOT | AUTH-001-S03 | 登录 - 输入聚焦 视觉比对 | actual/AUTH-001-S03.png + diff/AUTH-001-S03.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S04 | ANDROID_SCREENSHOT | AUTH-001-S04 | 登录 - 本地校验错误 视觉比对 | actual/AUTH-001-S04.png + diff/AUTH-001-S04.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S05 | ANDROID_SCREENSHOT | AUTH-001-S05 | 登录 - 短信发送中 视觉比对 | actual/AUTH-001-S05.png + diff/AUTH-001-S05.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S06 | ANDROID_SCREENSHOT | AUTH-001-S06 | 登录 - 短信已发送 视觉比对 | actual/AUTH-001-S06.png + diff/AUTH-001-S06.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S07 | ANDROID_SCREENSHOT | AUTH-001-S07 | 登录 - 等待安全验证码 视觉比对 | actual/AUTH-001-S07.png + diff/AUTH-001-S07.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S08 | ANDROID_SCREENSHOT | AUTH-001-S08 | 登录 - 登录提交中 视觉比对 | actual/AUTH-001-S08.png + diff/AUTH-001-S08.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S09 | ANDROID_SCREENSHOT | AUTH-001-S09 | 登录 - 登录成功 视觉比对 | actual/AUTH-001-S09.png + diff/AUTH-001-S09.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S10 | ANDROID_SCREENSHOT | AUTH-001-S10 | 登录 - 手机号或密码错误 视觉比对 | actual/AUTH-001-S10.png + diff/AUTH-001-S10.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S11 | ANDROID_SCREENSHOT | AUTH-001-S11 | 登录 - 短信验证码错误 视觉比对 | actual/AUTH-001-S11.png + diff/AUTH-001-S11.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S12 | ANDROID_SCREENSHOT | AUTH-001-S12 | 登录 - 短信验证码过期 视觉比对 | actual/AUTH-001-S12.png + diff/AUTH-001-S12.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S13 | ANDROID_SCREENSHOT | AUTH-001-S13 | 登录 - 操作过于频繁 视觉比对 | actual/AUTH-001-S13.png + diff/AUTH-001-S13.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S14 | ANDROID_SCREENSHOT | AUTH-001-S14 | 登录 - 账号受限 视觉比对 | actual/AUTH-001-S14.png + diff/AUTH-001-S14.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S15 | ANDROID_SCREENSHOT | AUTH-001-S15 | 登录 - 网络错误 视觉比对 | actual/AUTH-001-S15.png + diff/AUTH-001-S15.png + baseline SHA + diff ratio |
| T-VIS-AUTH-001-S16 | ANDROID_SCREENSHOT | AUTH-001-S16 | 登录 - 请求超时 视觉比对 | actual/AUTH-001-S16.png + diff/AUTH-001-S16.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S01 | ANDROID_SCREENSHOT | AUTH-002-S01 | 注册 - 注册默认 视觉比对 | actual/AUTH-002-S01.png + diff/AUTH-002-S01.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S02 | ANDROID_SCREENSHOT | AUTH-002-S02 | 注册 - 输入聚焦 视觉比对 | actual/AUTH-002-S02.png + diff/AUTH-002-S02.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S03 | ANDROID_SCREENSHOT | AUTH-002-S03 | 注册 - 手机号已注册 视觉比对 | actual/AUTH-002-S03.png + diff/AUTH-002-S03.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S04 | ANDROID_SCREENSHOT | AUTH-002-S04 | 注册 - 两次密码不一致 视觉比对 | actual/AUTH-002-S04.png + diff/AUTH-002-S04.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S05 | ANDROID_SCREENSHOT | AUTH-002-S05 | 注册 - 未同意协议 视觉比对 | actual/AUTH-002-S05.png + diff/AUTH-002-S05.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S06 | ANDROID_SCREENSHOT | AUTH-002-S06 | 注册 - 其他校验错误 视觉比对 | actual/AUTH-002-S06.png + diff/AUTH-002-S06.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S07 | ANDROID_SCREENSHOT | AUTH-002-S07 | 注册 - 等待安全验证码 视觉比对 | actual/AUTH-002-S07.png + diff/AUTH-002-S07.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S08 | ANDROID_SCREENSHOT | AUTH-002-S08 | 注册 - 注册提交中 视觉比对 | actual/AUTH-002-S08.png + diff/AUTH-002-S08.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S09 | ANDROID_SCREENSHOT | AUTH-002-S09 | 注册 - 注册成功 视觉比对 | actual/AUTH-002-S09.png + diff/AUTH-002-S09.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S10 | ANDROID_SCREENSHOT | AUTH-002-S10 | 注册 - 注册限流 视觉比对 | actual/AUTH-002-S10.png + diff/AUTH-002-S10.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S11 | ANDROID_SCREENSHOT | AUTH-002-S11 | 注册 - 网络错误 视觉比对 | actual/AUTH-002-S11.png + diff/AUTH-002-S11.png + baseline SHA + diff ratio |
| T-VIS-AUTH-002-S12 | ANDROID_SCREENSHOT | AUTH-002-S12 | 注册 - 注册失败 视觉比对 | actual/AUTH-002-S12.png + diff/AUTH-002-S12.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S01 | ANDROID_SCREENSHOT | AUTH-003-S01 | 找回并重置密码 - 重置默认 视觉比对 | actual/AUTH-003-S01.png + diff/AUTH-003-S01.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S02 | ANDROID_SCREENSHOT | AUTH-003-S02 | 找回并重置密码 - 发送或提交前安全验证 视觉比对 | actual/AUTH-003-S02.png + diff/AUTH-003-S02.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S03 | ANDROID_SCREENSHOT | AUTH-003-S03 | 找回并重置密码 - 短信发送中 视觉比对 | actual/AUTH-003-S03.png + diff/AUTH-003-S03.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S04 | ANDROID_SCREENSHOT | AUTH-003-S04 | 找回并重置密码 - 短信已发送 视觉比对 | actual/AUTH-003-S04.png + diff/AUTH-003-S04.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S05 | ANDROID_SCREENSHOT | AUTH-003-S05 | 找回并重置密码 - 表单校验错误 视觉比对 | actual/AUTH-003-S05.png + diff/AUTH-003-S05.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S06 | ANDROID_SCREENSHOT | AUTH-003-S06 | 找回并重置密码 - 重置提交中 视觉比对 | actual/AUTH-003-S06.png + diff/AUTH-003-S06.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S07 | ANDROID_SCREENSHOT | AUTH-003-S07 | 找回并重置密码 - 重置成功 视觉比对 | actual/AUTH-003-S07.png + diff/AUTH-003-S07.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S08 | ANDROID_SCREENSHOT | AUTH-003-S08 | 找回并重置密码 - 短信码过期 视觉比对 | actual/AUTH-003-S08.png + diff/AUTH-003-S08.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S09 | ANDROID_SCREENSHOT | AUTH-003-S09 | 找回并重置密码 - 操作限流 视觉比对 | actual/AUTH-003-S09.png + diff/AUTH-003-S09.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S10 | ANDROID_SCREENSHOT | AUTH-003-S10 | 找回并重置密码 - 网络错误 视觉比对 | actual/AUTH-003-S10.png + diff/AUTH-003-S10.png + baseline SHA + diff ratio |
| T-VIS-AUTH-003-S11 | ANDROID_SCREENSHOT | AUTH-003-S11 | 找回并重置密码 - 重置失败 视觉比对 | actual/AUTH-003-S11.png + diff/AUTH-003-S11.png + baseline SHA + diff ratio |
| T-VIS-AUTH-004-S01 | ANDROID_SCREENSHOT | AUTH-004-S01 | 用户协议 - 协议加载 视觉比对 | actual/AUTH-004-S01.png + diff/AUTH-004-S01.png + baseline SHA + diff ratio |
| T-VIS-AUTH-004-S02 | ANDROID_SCREENSHOT | AUTH-004-S02 | 用户协议 - 协议正文 视觉比对 | actual/AUTH-004-S02.png + diff/AUTH-004-S02.png + baseline SHA + diff ratio |
| T-VIS-AUTH-004-S03 | ANDROID_SCREENSHOT | AUTH-004-S03 | 用户协议 - 离线缓存 视觉比对 | actual/AUTH-004-S03.png + diff/AUTH-004-S03.png + baseline SHA + diff ratio |
| T-VIS-AUTH-004-S04 | ANDROID_SCREENSHOT | AUTH-004-S04 | 用户协议 - 加载失败 视觉比对 | actual/AUTH-004-S04.png + diff/AUTH-004-S04.png + baseline SHA + diff ratio |
| T-VIS-AUTH-005-S01 | ANDROID_SCREENSHOT | AUTH-005-S01 | 隐私政策 - 政策加载 视觉比对 | actual/AUTH-005-S01.png + diff/AUTH-005-S01.png + baseline SHA + diff ratio |
| T-VIS-AUTH-005-S02 | ANDROID_SCREENSHOT | AUTH-005-S02 | 隐私政策 - 政策正文 视觉比对 | actual/AUTH-005-S02.png + diff/AUTH-005-S02.png + baseline SHA + diff ratio |
| T-VIS-AUTH-005-S03 | ANDROID_SCREENSHOT | AUTH-005-S03 | 隐私政策 - 离线缓存 视觉比对 | actual/AUTH-005-S03.png + diff/AUTH-005-S03.png + baseline SHA + diff ratio |
| T-VIS-AUTH-005-S04 | ANDROID_SCREENSHOT | AUTH-005-S04 | 隐私政策 - 加载失败 视觉比对 | actual/AUTH-005-S04.png + diff/AUTH-005-S04.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-001-S01 | ANDROID_SCREENSHOT | AUTH-OV-001-S01 | 安全验证码弹窗 - 验证码加载 视觉比对 | actual/AUTH-OV-001-S01.png + diff/AUTH-OV-001-S01.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-001-S02 | ANDROID_SCREENSHOT | AUTH-OV-001-S02 | 安全验证码弹窗 - 等待输入 视觉比对 | actual/AUTH-OV-001-S02.png + diff/AUTH-OV-001-S02.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-001-S03 | ANDROID_SCREENSHOT | AUTH-OV-001-S03 | 安全验证码弹窗 - 输入错误 视觉比对 | actual/AUTH-OV-001-S03.png + diff/AUTH-OV-001-S03.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-001-S04 | ANDROID_SCREENSHOT | AUTH-OV-001-S04 | 安全验证码弹窗 - 验证码已过期 视觉比对 | actual/AUTH-OV-001-S04.png + diff/AUTH-OV-001-S04.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-001-S05 | ANDROID_SCREENSHOT | AUTH-OV-001-S05 | 安全验证码弹窗 - 刷新中 视觉比对 | actual/AUTH-OV-001-S05.png + diff/AUTH-OV-001-S05.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-001-S06 | ANDROID_SCREENSHOT | AUTH-OV-001-S06 | 安全验证码弹窗 - 验证中 视觉比对 | actual/AUTH-OV-001-S06.png + diff/AUTH-OV-001-S06.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-001-S07 | ANDROID_SCREENSHOT | AUTH-OV-001-S07 | 安全验证码弹窗 - 验证成功 视觉比对 | actual/AUTH-OV-001-S07.png + diff/AUTH-OV-001-S07.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-001-S08 | ANDROID_SCREENSHOT | AUTH-OV-001-S08 | 安全验证码弹窗 - 验证码网络失败 视觉比对 | actual/AUTH-OV-001-S08.png + diff/AUTH-OV-001-S08.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-002-S01 | ANDROID_SCREENSHOT | AUTH-OV-002-S01 | 账号与风控提示 - 登录暂时锁定 视觉比对 | actual/AUTH-OV-002-S01.png + diff/AUTH-OV-002-S01.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-002-S02 | ANDROID_SCREENSHOT | AUTH-OV-002-S02 | 账号与风控提示 - 账号被禁用 视觉比对 | actual/AUTH-OV-002-S02.png + diff/AUTH-OV-002-S02.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-002-S03 | ANDROID_SCREENSHOT | AUTH-OV-002-S03 | 账号与风控提示 - 需要人工核验 视觉比对 | actual/AUTH-OV-002-S03.png + diff/AUTH-OV-002-S03.png + baseline SHA + diff ratio |
| T-VIS-AUTH-OV-002-S04 | ANDROID_SCREENSHOT | AUTH-OV-002-S04 | 账号与风控提示 - 登录方式暂不可用 视觉比对 | actual/AUTH-OV-002-S04.png + diff/AUTH-OV-002-S04.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S01 | ANDROID_SCREENSHOT | HOME-001-S01 | 首页项目流 - 首次骨架 视觉比对 | actual/HOME-001-S01.png + diff/HOME-001-S01.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S02 | ANDROID_SCREENSHOT | HOME-001-S02 | 首页项目流 - 正常内容 视觉比对 | actual/HOME-001-S02.png + diff/HOME-001-S02.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S03 | ANDROID_SCREENSHOT | HOME-001-S03 | 首页项目流 - 下拉刷新 视觉比对 | actual/HOME-001-S03.png + diff/HOME-001-S03.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S05 | ANDROID_SCREENSHOT | HOME-001-S05 | 首页项目流 - 加载下一页 视觉比对 | actual/HOME-001-S05.png + diff/HOME-001-S05.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S06 | ANDROID_SCREENSHOT | HOME-001-S06 | 首页项目流 - 下一页失败 视觉比对 | actual/HOME-001-S06.png + diff/HOME-001-S06.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S07 | ANDROID_SCREENSHOT | HOME-001-S07 | 首页项目流 - 暂无项目 视觉比对 | actual/HOME-001-S07.png + diff/HOME-001-S07.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S10 | ANDROID_SCREENSHOT | HOME-001-S10 | 首页项目流 - 网络错误 视觉比对 | actual/HOME-001-S10.png + diff/HOME-001-S10.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S11 | ANDROID_SCREENSHOT | HOME-001-S11 | 首页项目流 - 请求超时 视觉比对 | actual/HOME-001-S11.png + diff/HOME-001-S11.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S12 | ANDROID_SCREENSHOT | HOME-001-S12 | 首页项目流 - 服务异常 视觉比对 | actual/HOME-001-S12.png + diff/HOME-001-S12.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S13 | ANDROID_SCREENSHOT | HOME-001-S13 | 首页项目流 - 登录过期 视觉比对 | actual/HOME-001-S13.png + diff/HOME-001-S13.png + baseline SHA + diff ratio |
| T-VIS-HOME-004-S01 | ANDROID_SCREENSHOT | HOME-004-S01 | 项目详情 - 详情加载 视觉比对 | actual/HOME-004-S01.png + diff/HOME-004-S01.png + baseline SHA + diff ratio |
| T-VIS-HOME-004-S02 | ANDROID_SCREENSHOT | HOME-004-S02 | 项目详情 - 公开项目详情 视觉比对 | actual/HOME-004-S02.png + diff/HOME-004-S02.png + baseline SHA + diff ratio |
| T-VIS-HOME-004-S07 | ANDROID_SCREENSHOT | HOME-004-S07 | 项目详情 - 图片加载失败 视觉比对 | actual/HOME-004-S07.png + diff/HOME-004-S07.png + baseline SHA + diff ratio |
| T-VIS-HOME-004-S08 | ANDROID_SCREENSHOT | HOME-004-S08 | 项目详情 - 项目不存在 视觉比对 | actual/HOME-004-S08.png + diff/HOME-004-S08.png + baseline SHA + diff ratio |
| T-VIS-HOME-004-S09 | ANDROID_SCREENSHOT | HOME-004-S09 | 项目详情 - 项目不可公开 视觉比对 | actual/HOME-004-S09.png + diff/HOME-004-S09.png + baseline SHA + diff ratio |
| T-VIS-HOME-004-S10 | ANDROID_SCREENSHOT | HOME-004-S10 | 项目详情 - 详情网络错误 视觉比对 | actual/HOME-004-S10.png + diff/HOME-004-S10.png + baseline SHA + diff ratio |
| T-VIS-HOME-004-S11 | ANDROID_SCREENSHOT | HOME-004-S11 | 项目详情 - 详情服务异常 视觉比对 | actual/HOME-004-S11.png + diff/HOME-004-S11.png + baseline SHA + diff ratio |
| T-VIS-ME-001-S01 | ANDROID_SCREENSHOT | ME-001-S01 | 我的 - 资料加载 视觉比对 | actual/ME-001-S01.png + diff/ME-001-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-001-S02 | ANDROID_SCREENSHOT | ME-001-S02 | 我的 - V1 基础资料 视觉比对 | actual/ME-001-S02.png + diff/ME-001-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-001-S07 | ANDROID_SCREENSHOT | ME-001-S07 | 我的 - 资料加载失败 视觉比对 | actual/ME-001-S07.png + diff/ME-001-S07.png + baseline SHA + diff ratio |
| T-VIS-ME-001-S08 | ANDROID_SCREENSHOT | ME-001-S08 | 我的 - 登录过期 视觉比对 | actual/ME-001-S08.png + diff/ME-001-S08.png + baseline SHA + diff ratio |
| T-VIS-ME-OV-001-S01 | ANDROID_SCREENSHOT | ME-OV-001-S01 | 退出登录确认 - 确认退出 视觉比对 | actual/ME-OV-001-S01.png + diff/ME-OV-001-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-OV-001-S02 | ANDROID_SCREENSHOT | ME-OV-001-S02 | 退出登录确认 - 退出中 视觉比对 | actual/ME-OV-001-S02.png + diff/ME-OV-001-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-OV-001-S03 | ANDROID_SCREENSHOT | ME-OV-001-S03 | 退出登录确认 - 退出请求失败 视觉比对 | actual/ME-OV-001-S03.png + diff/ME-OV-001-S03.png + baseline SHA + diff ratio |
| T-UI-INT-NAV-001 | ANDROID_UI | INT-NAV-001 | 交互：底部导航进入首页 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-NAV-002 | ANDROID_UI | INT-NAV-002 | 交互：底部导航进入发布 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-NAV-003 | ANDROID_UI | INT-NAV-003 | 交互：底部导航进入我的 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SYS-001 | ANDROID_UI | INT-SYS-001 | 交互：重试启动配置 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SYS-002 | ANDROID_UI | INT-SYS-002 | 交互：离线进入缓存首页 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SYS-003 | ANDROID_UI | INT-SYS-003 | 交互：服务异常重试 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-STAGE-001 | ANDROID_UI | INT-STAGE-001 | 交互：关闭阶段说明 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-001 | ANDROID_UI | INT-AUTH-001 | 交互：切换密码登录 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-002 | ANDROID_UI | INT-AUTH-002 | 交互：切换短信登录 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-003 | ANDROID_UI | INT-AUTH-003 | 交互：输入手机号 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-004 | ANDROID_UI | INT-AUTH-004 | 交互：输入登录密码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-005 | ANDROID_UI | INT-AUTH-005 | 交互：切换密码可见性 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-006 | ANDROID_UI | INT-AUTH-006 | 交互：输入短信验证码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-007 | ANDROID_UI | INT-AUTH-007 | 交互：发送登录短信验证码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-008 | ANDROID_UI | INT-AUTH-008 | 交互：提交密码登录 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-009 | ANDROID_UI | INT-AUTH-009 | 交互：提交短信登录 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-010 | ANDROID_UI | INT-AUTH-010 | 交互：进入注册 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-011 | ANDROID_UI | INT-AUTH-011 | 交互：进入找回密码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-012 | ANDROID_UI | INT-AUTH-012 | 交互：打开用户协议 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-AUTH-013 | ANDROID_UI | INT-AUTH-013 | 交互：打开隐私政策 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-REG-001 | ANDROID_UI | INT-REG-001 | 交互：输入注册手机号 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-REG-002 | ANDROID_UI | INT-REG-002 | 交互：输入注册密码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-REG-003 | ANDROID_UI | INT-REG-003 | 交互：输入确认密码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-REG-004 | ANDROID_UI | INT-REG-004 | 交互：切换密码可见性 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-REG-005 | ANDROID_UI | INT-REG-005 | 交互：同意用户协议 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-REG-006 | ANDROID_UI | INT-REG-006 | 交互：提交注册 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-REG-007 | ANDROID_UI | INT-REG-007 | 交互：返回登录 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-REG-008 | ANDROID_UI | INT-REG-008 | 交互：注册页打开协议 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-REG-009 | ANDROID_UI | INT-REG-009 | 交互：注册页打开隐私政策 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RESET-001 | ANDROID_UI | INT-RESET-001 | 交互：输入重置手机号 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RESET-002 | ANDROID_UI | INT-RESET-002 | 交互：发送重置短信 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RESET-003 | ANDROID_UI | INT-RESET-003 | 交互：输入重置短信码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RESET-004 | ANDROID_UI | INT-RESET-004 | 交互：输入新密码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RESET-005 | ANDROID_UI | INT-RESET-005 | 交互：输入确认新密码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RESET-006 | ANDROID_UI | INT-RESET-006 | 交互：提交密码重置 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RESET-007 | ANDROID_UI | INT-RESET-007 | 交互：重置页返回登录 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-LEGAL-001 | ANDROID_UI | INT-LEGAL-001 | 交互：用户协议返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-LEGAL-002 | ANDROID_UI | INT-LEGAL-002 | 交互：重试加载用户协议 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-LEGAL-003 | ANDROID_UI | INT-LEGAL-003 | 交互：隐私政策返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-LEGAL-004 | ANDROID_UI | INT-LEGAL-004 | 交互：重试加载隐私政策 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CAPTCHA-001 | ANDROID_UI | INT-CAPTCHA-001 | 交互：输入安全验证码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CAPTCHA-002 | ANDROID_UI | INT-CAPTCHA-002 | 交互：刷新安全验证码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CAPTCHA-003 | ANDROID_UI | INT-CAPTCHA-003 | 交互：确认安全验证码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CAPTCHA-004 | ANDROID_UI | INT-CAPTCHA-004 | 交互：取消安全验证码 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RISK-001 | ANDROID_UI | INT-RISK-001 | 交互：关闭风控提示 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-HOME-001 | ANDROID_UI | INT-HOME-001 | 交互：下拉刷新项目流 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-HOME-004 | ANDROID_UI | INT-HOME-004 | 交互：打开项目详情 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-HOME-005 | ANDROID_UI | INT-HOME-005 | 交互：下一页自动加载 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-HOME-006 | ANDROID_UI | INT-HOME-006 | 交互：重试首屏 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-HOME-007 | ANDROID_UI | INT-HOME-007 | 交互：重试下一页 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-HOME-008 | ANDROID_UI | INT-HOME-008 | 交互：顶部头像进入我的 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DETAIL-001 | ANDROID_UI | INT-DETAIL-001 | 交互：详情返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DETAIL-005 | ANDROID_UI | INT-DETAIL-005 | 交互：详情重试 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-011 | ANDROID_UI | INT-ME-011 | 交互：我的页面重试 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-LOGOUT-001 | ANDROID_UI | INT-LOGOUT-001 | 交互：确认退出登录 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-LOGOUT-002 | ANDROID_UI | INT-LOGOUT-002 | 交互：取消退出登录 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-API-getBootstrap | API_CONTRACT_INTEGRATION | getBootstrap | 接口合同与错误路径：GET /v1/bootstrap | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-getCaptchaChallenge | API_CONTRACT_INTEGRATION | getCaptchaChallenge | 接口合同与错误路径：POST /v1/auth/captcha/challenges | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-verifyCaptcha | API_CONTRACT_INTEGRATION | verifyCaptcha | 接口合同与错误路径：POST /v1/auth/captcha/verify | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-sendSmsCode | API_CONTRACT_INTEGRATION | sendSmsCode | 接口合同与错误路径：POST /v1/auth/sms/send | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-loginWithPassword | API_CONTRACT_INTEGRATION | loginWithPassword | 接口合同与错误路径：POST /v1/auth/login/password | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-loginWithSms | API_CONTRACT_INTEGRATION | loginWithSms | 接口合同与错误路径：POST /v1/auth/login/sms | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-registerUser | API_CONTRACT_INTEGRATION | registerUser | 接口合同与错误路径：POST /v1/auth/register | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-resetPassword | API_CONTRACT_INTEGRATION | resetPassword | 接口合同与错误路径：POST /v1/auth/password/reset | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-refreshToken | API_CONTRACT_INTEGRATION | refreshToken | 接口合同与错误路径：POST /v1/auth/token/refresh | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-logout | API_CONTRACT_INTEGRATION | logout | 接口合同与错误路径：POST /v1/auth/logout | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-getMySummary | API_CONTRACT_INTEGRATION | getMySummary | 接口合同与错误路径：GET /v1/me/summary | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-getLegalDocument | API_CONTRACT_INTEGRATION | getLegalDocument | 接口合同与错误路径：GET /v1/legal/{document_type} | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-listProjectCategories | API_CONTRACT_INTEGRATION | listProjectCategories | 接口合同与错误路径：GET /v1/project/categories | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-listProjects | API_CONTRACT_INTEGRATION | listProjects | 接口合同与错误路径：GET /v1/projects | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-getProjectDetail | API_CONTRACT_INTEGRATION | getProjectDetail | 接口合同与错误路径：GET /v1/projects/{project_id} | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-recordProjectView | API_CONTRACT_INTEGRATION | recordProjectView | 接口合同与错误路径：POST /v1/projects/{project_id}/views | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-healthLive | API_CONTRACT_INTEGRATION | healthLive | 接口合同与错误路径：GET /healthz | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-healthReady | API_CONTRACT_INTEGRATION | healthReady | 接口合同与错误路径：GET /readyz | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-DB-DB-AUTH-001 | DATABASE | DB-AUTH-001 | 数据表安装、迁移与索引：pre_stk_auth_mobile | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-AUTH-002 | DATABASE | DB-AUTH-002 | 数据表安装、迁移与索引：pre_stk_auth_sms_code | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-AUTH-003 | DATABASE | DB-AUTH-003 | 数据表安装、迁移与索引：pre_stk_auth_captcha_challenge | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-AUTH-004 | DATABASE | DB-AUTH-004 | 数据表安装、迁移与索引：pre_stk_auth_token | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-AUTH-005 | DATABASE | DB-AUTH-005 | 数据表安装、迁移与索引：pre_stk_auth_login_log | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-AUTH-006 | DATABASE | DB-AUTH-006 | 数据表安装、迁移与索引：pre_stk_auth_risk_event | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-AUTH-007 | DATABASE | DB-AUTH-007 | 数据表安装、迁移与索引：pre_stk_secret_config | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-AUTH-008 | DATABASE | DB-AUTH-008 | 数据表安装、迁移与索引：pre_stk_admin_audit | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-PROJ-001 | DATABASE | DB-PROJ-001 | 数据表安装、迁移与索引：pre_stk_project_category | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-PROJ-002 | DATABASE | DB-PROJ-002 | 数据表安装、迁移与索引：pre_stk_project | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-PROJ-006 | DATABASE | DB-PROJ-006 | 数据表安装、迁移与索引：pre_stk_project_view | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-PROJ-007 | DATABASE | DB-PROJ-007 | 数据表安装、迁移与索引：pre_stk_project_view_daily | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-ME-001 | DATABASE | DB-ME-001 | 数据表安装、迁移与索引：pre_stk_member_status | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-ME-002 | DATABASE | DB-ME-002 | 数据表安装、迁移与索引：pre_stk_wallet_account | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-SYS-003 | DATABASE | DB-SYS-003 | 数据表安装、迁移与索引：pre_stk_idempotency_key | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-ADM-ADM-AUTH-001 | ADMIN_UI_API | ADM-AUTH-001 | 后台页面与权限：登录注册概览 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-002 | ADMIN_UI_API | ADM-AUTH-002 | 后台页面与权限：基础与跳转设置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-003 | ADMIN_UI_API | ADM-AUTH-003 | 后台页面与权限：登录策略 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-004 | ADMIN_UI_API | ADM-AUTH-004 | 后台页面与权限：注册策略 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-005 | ADMIN_UI_API | ADM-AUTH-005 | 后台页面与权限：安全验证码设置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-006 | ADMIN_UI_API | ADM-AUTH-006 | 后台页面与权限：阿里云短信设置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-007 | ADMIN_UI_API | ADM-AUTH-007 | 后台页面与权限：短信验证码记录 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-008 | ADMIN_UI_API | ADM-AUTH-008 | 后台页面与权限：注册用户列表 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-009 | ADMIN_UI_API | ADM-AUTH-009 | 后台页面与权限：登录日志 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-010 | ADMIN_UI_API | ADM-AUTH-010 | 后台页面与权限：Token 与设备会话 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-011 | ADMIN_UI_API | ADM-AUTH-011 | 后台页面与权限：风控与限流事件 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-012 | ADMIN_UI_API | ADM-AUTH-012 | 后台页面与权限：协议与隐私版本 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-013 | ADMIN_UI_API | ADM-AUTH-013 | 后台页面与权限：管理操作审计 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-AUTH-014 | ADMIN_UI_API | ADM-AUTH-014 | 后台页面与权限：认证系统诊断 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-PROJ-001 | ADMIN_UI_API | ADM-PROJ-001 | 后台页面与权限：项目运营概览 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-PROJ-002 | ADMIN_UI_API | ADM-PROJ-002 | 后台页面与权限：项目管理 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-PROJ-003 | ADMIN_UI_API | ADM-PROJ-003 | 后台页面与权限：项目分类 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-PROJ-004 | ADMIN_UI_API | ADM-PROJ-004 | 后台页面与权限：首页展示设置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-PROJ-005 | ADMIN_UI_API | ADM-PROJ-005 | 后台页面与权限：项目详情设置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-REL-V1.0.0-BUILD | RELEASE | V1.0.0 | 同一 Commit Release APK 构建与来源校验 | APK + Build Info + CI Provenance + SHA-256 |
| T-REL-V1.0.0-EMULATOR | RELEASE | V1.0.0 | API 36 模拟器全量交互回归 | JUnit XML + 视频/截图 + logcat + 失败重现步骤 |
| T-REL-V1.0.0-VISUAL | RELEASE | V1.0.0 | 本版新增状态与历史核心状态视觉回归 | 视觉差异 HTML/JSON + actual/baseline/diff |
| T-REL-V1.0.0-UPGRADE | RELEASE | V1.0.0 | 覆盖安装与数据兼容 | 安装日志、版本号、登录态/缓存/数据库迁移结果 |
| T-REL-V1.0.0-DESKTOP | RELEASE | V1.0.0 | 桌面交付内容完整性 | 桌面目录清单、全部 SHA-256、来源 Commit |

## 项目所有者真机测试

1. 安装首版 APK，确认启动立即出现原生商推客界面而非网页白屏。
2. 注册新手机号：只输入手机号和两次密码，确认过程未发送短信；后台注册用户列表出现 UID/手机号/日期。
3. 密码登录正确/错误各一次；检查安全验证码、错误提示和后台登录日志。
4. 短信登录：发送前完成安全验证码，后台验证码记录显示发送/使用状态；登录成功后手机号验证状态更新。
5. 找回密码并重新登录；旧 Token 失效。
6. 检查首页基础项目、项目详情、我的基础资料、手机号脱敏和退出登录。
7. 点击底部发布，必须出现 V1.2 开放说明，不得无响应。
8. 登录 stk-admin 后台，修改密码，检查 stk_auth/stk_project 当前菜单、短信测试和健康诊断。

## 失败提交内容

记录设备型号、Android 版本、App versionName/versionCode、操作步骤、期望/实际、截图/录屏、发生时间和 request_id。涉及外部 App 时同时记录目标 App 是否安装及版本。
