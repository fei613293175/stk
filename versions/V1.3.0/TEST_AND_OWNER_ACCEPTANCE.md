# 1.3.0 自动测试与项目所有者验收

## 自动测试合同

本版共有 84 条首次引入测试合同；发布时还必须回归所有历史已发布 Feature 的核心交互和 Golden 状态。详细清单见 `contracts/test-catalog.csv`。

| Test ID | 层级 | 目标 | 名称 | 证据 |
|---|---|---|---|---|
| T-VIS-COM-OV-002-S02 | ANDROID_SCREENSHOT | COM-OV-002-S02 | 阶段功能说明弹窗 - 会员在线开通未开放 视觉比对 | actual/COM-OV-002-S02.png + diff/COM-OV-002-S02.png + baseline SHA + diff ratio |
| T-VIS-COM-OV-002-S03 | ANDROID_SCREENSHOT | COM-OV-002-S03 | 阶段功能说明弹窗 - 账户操作未开放 视觉比对 | actual/COM-OV-002-S03.png + diff/COM-OV-002-S03.png + baseline SHA + diff ratio |
| T-VIS-COM-OV-002-S04 | ANDROID_SCREENSHOT | COM-OV-002-S04 | 阶段功能说明弹窗 - 道具购买未开放 视觉比对 | actual/COM-OV-002-S04.png + diff/COM-OV-002-S04.png + baseline SHA + diff ratio |
| T-VIS-ME-001-S03 | ANDROID_SCREENSHOT | ME-001-S03 | 我的 - 未开通会员 视觉比对 | actual/ME-001-S03.png + diff/ME-001-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-001-S04 | ANDROID_SCREENSHOT | ME-001-S04 | 我的 - 已开通会员 视觉比对 | actual/ME-001-S04.png + diff/ME-001-S04.png + baseline SHA + diff ratio |
| T-VIS-ME-001-S05 | ANDROID_SCREENSHOT | ME-001-S05 | 我的 - 会员已过期 视觉比对 | actual/ME-001-S05.png + diff/ME-001-S05.png + baseline SHA + diff ratio |
| T-VIS-ME-001-S06 | ANDROID_SCREENSHOT | ME-001-S06 | 我的 - 离线缓存 视觉比对 | actual/ME-001-S06.png + diff/ME-001-S06.png + baseline SHA + diff ratio |
| T-VIS-ME-003-S01 | ANDROID_SCREENSHOT | ME-003-S01 | 个人资料只读页 - 资料加载 视觉比对 | actual/ME-003-S01.png + diff/ME-003-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-003-S02 | ANDROID_SCREENSHOT | ME-003-S02 | 个人资料只读页 - 只读资料 视觉比对 | actual/ME-003-S02.png + diff/ME-003-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-003-S03 | ANDROID_SCREENSHOT | ME-003-S03 | 个人资料只读页 - 加载失败 视觉比对 | actual/ME-003-S03.png + diff/ME-003-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-004-S01 | ANDROID_SCREENSHOT | ME-004-S01 | 会员中心展示 - 会员加载 视觉比对 | actual/ME-004-S01.png + diff/ME-004-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-004-S02 | ANDROID_SCREENSHOT | ME-004-S02 | 会员中心展示 - 未开通 视觉比对 | actual/ME-004-S02.png + diff/ME-004-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-004-S03 | ANDROID_SCREENSHOT | ME-004-S03 | 会员中心展示 - 有效会员 视觉比对 | actual/ME-004-S03.png + diff/ME-004-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-004-S04 | ANDROID_SCREENSHOT | ME-004-S04 | 会员中心展示 - 已过期 视觉比对 | actual/ME-004-S04.png + diff/ME-004-S04.png + baseline SHA + diff ratio |
| T-VIS-ME-004-S05 | ANDROID_SCREENSHOT | ME-004-S05 | 会员中心展示 - 会员加载失败 视觉比对 | actual/ME-004-S05.png + diff/ME-004-S05.png + baseline SHA + diff ratio |
| T-VIS-ME-005-S01 | ANDROID_SCREENSHOT | ME-005-S01 | 账户余额展示 - 账户加载 视觉比对 | actual/ME-005-S01.png + diff/ME-005-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-005-S02 | ANDROID_SCREENSHOT | ME-005-S02 | 账户余额展示 - 只读余额 视觉比对 | actual/ME-005-S02.png + diff/ME-005-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-005-S03 | ANDROID_SCREENSHOT | ME-005-S03 | 账户余额展示 - 账户加载失败 视觉比对 | actual/ME-005-S03.png + diff/ME-005-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-006-S01 | ANDROID_SCREENSHOT | ME-006-S01 | 道具中心展示 - 道具加载 视觉比对 | actual/ME-006-S01.png + diff/ME-006-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-006-S02 | ANDROID_SCREENSHOT | ME-006-S02 | 道具中心展示 - 道具展示 视觉比对 | actual/ME-006-S02.png + diff/ME-006-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-006-S03 | ANDROID_SCREENSHOT | ME-006-S03 | 道具中心展示 - 无展示道具 视觉比对 | actual/ME-006-S03.png + diff/ME-006-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-006-S04 | ANDROID_SCREENSHOT | ME-006-S04 | 道具中心展示 - 离线缓存 视觉比对 | actual/ME-006-S04.png + diff/ME-006-S04.png + baseline SHA + diff ratio |
| T-VIS-ME-006-S05 | ANDROID_SCREENSHOT | ME-006-S05 | 道具中心展示 - 道具加载失败 视觉比对 | actual/ME-006-S05.png + diff/ME-006-S05.png + baseline SHA + diff ratio |
| T-VIS-ME-007-S01 | ANDROID_SCREENSHOT | ME-007-S01 | 浏览记录占位 - 浏览记录未开放 视觉比对 | actual/ME-007-S01.png + diff/ME-007-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-008-S01 | ANDROID_SCREENSHOT | ME-008-S01 | 收藏占位 - 收藏未开放 视觉比对 | actual/ME-008-S01.png + diff/ME-008-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-009-S01 | ANDROID_SCREENSHOT | ME-009-S01 | 实名认证占位 - 实名认证未开放 视觉比对 | actual/ME-009-S01.png + diff/ME-009-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-010-S01 | ANDROID_SCREENSHOT | ME-010-S01 | 联系客服 - 客服配置加载 视觉比对 | actual/ME-010-S01.png + diff/ME-010-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-010-S02 | ANDROID_SCREENSHOT | ME-010-S02 | 联系客服 - 客服信息 视觉比对 | actual/ME-010-S02.png + diff/ME-010-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-010-S03 | ANDROID_SCREENSHOT | ME-010-S03 | 联系客服 - 复制成功 视觉比对 | actual/ME-010-S03.png + diff/ME-010-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-010-S04 | ANDROID_SCREENSHOT | ME-010-S04 | 联系客服 - 外部应用不可用 视觉比对 | actual/ME-010-S04.png + diff/ME-010-S04.png + baseline SHA + diff ratio |
| T-VIS-ME-010-S05 | ANDROID_SCREENSHOT | ME-010-S05 | 联系客服 - 配置失败 视觉比对 | actual/ME-010-S05.png + diff/ME-010-S05.png + baseline SHA + diff ratio |
| T-VIS-ME-011-S01 | ANDROID_SCREENSHOT | ME-011-S01 | 设置 - 设置列表 视觉比对 | actual/ME-011-S01.png + diff/ME-011-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-011-S02 | ANDROID_SCREENSHOT | ME-011-S02 | 设置 - 清理缓存中 视觉比对 | actual/ME-011-S02.png + diff/ME-011-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-011-S03 | ANDROID_SCREENSHOT | ME-011-S03 | 设置 - 清理成功 视觉比对 | actual/ME-011-S03.png + diff/ME-011-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-011-S04 | ANDROID_SCREENSHOT | ME-011-S04 | 设置 - 清理失败 视觉比对 | actual/ME-011-S04.png + diff/ME-011-S04.png + baseline SHA + diff ratio |
| T-VIS-ME-011-S05 | ANDROID_SCREENSHOT | ME-011-S05 | 设置 - 离线设置 视觉比对 | actual/ME-011-S05.png + diff/ME-011-S05.png + baseline SHA + diff ratio |
| T-VIS-ME-012-S01 | ANDROID_SCREENSHOT | ME-012-S01 | 关于商推客 - 关于信息 视觉比对 | actual/ME-012-S01.png + diff/ME-012-S01.png + baseline SHA + diff ratio |
| T-UI-INT-SYS-004 | ANDROID_UI | INT-SYS-004 | 交互：打开客服 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-001 | ANDROID_UI | INT-ME-001 | 交互：打开个人资料 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-003 | ANDROID_UI | INT-ME-003 | 交互：打开账户余额 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-004 | ANDROID_UI | INT-ME-004 | 交互：打开会员中心 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-005 | ANDROID_UI | INT-ME-005 | 交互：打开道具中心 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-006 | ANDROID_UI | INT-ME-006 | 交互：打开浏览记录占位 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-007 | ANDROID_UI | INT-ME-007 | 交互：打开收藏占位 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-008 | ANDROID_UI | INT-ME-008 | 交互：打开实名认证占位 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-009 | ANDROID_UI | INT-ME-009 | 交互：打开客服 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-010 | ANDROID_UI | INT-ME-010 | 交互：打开设置 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PROFILE-001 | ANDROID_UI | INT-PROFILE-001 | 交互：个人资料返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-MEMBER-001 | ANDROID_UI | INT-MEMBER-001 | 交互：会员开通引导 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-MEMBER-002 | ANDROID_UI | INT-MEMBER-002 | 交互：会员页返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-WALLET-001 | ANDROID_UI | INT-WALLET-001 | 交互：账户操作说明 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-WALLET-002 | ANDROID_UI | INT-WALLET-002 | 交互：账户页返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PROP-001 | ANDROID_UI | INT-PROP-001 | 交互：查看道具说明 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PROP-002 | ANDROID_UI | INT-PROP-002 | 交互：道具页返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PLACE-001 | ANDROID_UI | INT-PLACE-001 | 交互：占位页返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PLACE-002 | ANDROID_UI | INT-PLACE-002 | 交互：占位页返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PLACE-003 | ANDROID_UI | INT-PLACE-003 | 交互：占位页返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SUPPORT-001 | ANDROID_UI | INT-SUPPORT-001 | 交互：复制客服信息 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SUPPORT-002 | ANDROID_UI | INT-SUPPORT-002 | 交互：打开客服外部应用 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SUPPORT-003 | ANDROID_UI | INT-SUPPORT-003 | 交互：客服配置重试 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SET-001 | ANDROID_UI | INT-SET-001 | 交互：清理图片与列表缓存 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SET-002 | ANDROID_UI | INT-SET-002 | 交互：设置页打开协议 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SET-003 | ANDROID_UI | INT-SET-003 | 交互：设置页打开隐私 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SET-004 | ANDROID_UI | INT-SET-004 | 交互：打开关于 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SET-005 | ANDROID_UI | INT-SET-005 | 交互：打开退出确认 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ABOUT-002 | ANDROID_UI | INT-ABOUT-002 | 交互：关于页返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-API-getMyMember | API_CONTRACT_INTEGRATION | getMyMember | 接口合同与错误路径：GET /v1/me/member | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-getMyWallets | API_CONTRACT_INTEGRATION | getMyWallets | 接口合同与错误路径：GET /v1/me/wallets | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-getPropDisplay | API_CONTRACT_INTEGRATION | getPropDisplay | 接口合同与错误路径：GET /v1/props/display | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-getSupportConfig | API_CONTRACT_INTEGRATION | getSupportConfig | 接口合同与错误路径：GET /v1/support | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-getCurrentRelease | API_CONTRACT_INTEGRATION | getCurrentRelease | 接口合同与错误路径：GET /v1/app/releases/current | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-DB-DB-ME-003 | DATABASE | DB-ME-003 | 数据表安装、迁移与索引：pre_stk_prop_catalog | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-SYS-002 | DATABASE | DB-SYS-002 | 数据表安装、迁移与索引：pre_stk_app_release | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-ADM-ADM-ME-001 | ADMIN_UI_API | ADM-ME-001 | 后台页面与权限：我的页面展示配置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-ME-002 | ADMIN_UI_API | ADM-ME-002 | 后台页面与权限：会员状态管理 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-ME-003 | ADMIN_UI_API | ADM-ME-003 | 后台页面与权限：账户余额查询 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-ME-004 | ADMIN_UI_API | ADM-ME-004 | 后台页面与权限：道具展示目录 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-ME-005 | ADMIN_UI_API | ADM-ME-005 | 后台页面与权限：客服与关于配置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-SYS-001 | ADMIN_UI_API | ADM-SYS-001 | 后台页面与权限：App 版本发布配置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-REL-V1.3.0-BUILD | RELEASE | V1.3.0 | 同一 Commit Release APK 构建与来源校验 | APK + Build Info + CI Provenance + SHA-256 |
| T-REL-V1.3.0-EMULATOR | RELEASE | V1.3.0 | API 36 模拟器全量交互回归 | JUnit XML + 视频/截图 + logcat + 失败重现步骤 |
| T-REL-V1.3.0-VISUAL | RELEASE | V1.3.0 | 本版新增状态与历史核心状态视觉回归 | 视觉差异 HTML/JSON + actual/baseline/diff |
| T-REL-V1.3.0-UPGRADE | RELEASE | V1.3.0 | 覆盖安装与数据兼容 | 安装日志、版本号、登录态/缓存/数据库迁移结果 |
| T-REL-V1.3.0-DESKTOP | RELEASE | V1.3.0 | 桌面交付内容完整性 | 桌面目录清单、全部 SHA-256、来源 Commit |

## 项目所有者真机测试

1. 覆盖安装 1.3.0，检查我的完整资料卡、双账户、会员卡、道具和常用功能布局。
2. 新注册用户两个账户显示真实 0.00；接口失败时不得仍显示假 0.00。
3. 后台分别设置会员 inactive/active/expired，App 状态和有效期同步。
4. 点击会员立即开通、账户、道具，必须显示本期范围说明，不得创建订单或假成功。
5. 浏览记录、收藏、实名认证必须进入明确占位页；不出现空白、404 或采集表单。
6. 测试客服复制/外部打开、清理缓存、协议、隐私、关于和退出。

## 失败提交内容

记录设备型号、Android 版本、App versionName/versionCode、操作步骤、期望/实际、截图/录屏、发生时间和 request_id。涉及外部 App 时同时记录目标 App 是否安装及版本。
