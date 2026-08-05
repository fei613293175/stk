# 1.4.0 自动测试与项目所有者验收

## 自动测试合同

本版共有 39 条首次引入测试合同；发布时还必须回归所有历史已发布 Feature 的核心交互和 Golden 状态。详细清单见 `contracts/test-catalog.csv`。

| Test ID | 层级 | 目标 | 名称 | 证据 |
|---|---|---|---|---|
| T-VIS-SYS-001-S05 | ANDROID_SCREENSHOT | SYS-001-S05 | 启动与路由判定 - 发现可选更新 视觉比对 | actual/SYS-001-S05.png + diff/SYS-001-S05.png + baseline SHA + diff ratio |
| T-VIS-SYS-001-S06 | ANDROID_SCREENSHOT | SYS-001-S06 | 启动与路由判定 - 发现强制更新 视觉比对 | actual/SYS-001-S06.png + diff/SYS-001-S06.png + baseline SHA + diff ratio |
| T-VIS-SYS-001-S07 | ANDROID_SCREENSHOT | SYS-001-S07 | 启动与路由判定 - 维护模式 视觉比对 | actual/SYS-001-S07.png + diff/SYS-001-S07.png + baseline SHA + diff ratio |
| T-VIS-SYS-002-S05 | ANDROID_SCREENSHOT | SYS-002-S05 | 维护、离线与服务不可用 - 服务已恢复 视觉比对 | actual/SYS-002-S05.png + diff/SYS-002-S05.png + baseline SHA + diff ratio |
| T-VIS-SYS-003-S01 | ANDROID_SCREENSHOT | SYS-003-S01 | 版本更新与下载状态 - 检查版本 视觉比对 | actual/SYS-003-S01.png + diff/SYS-003-S01.png + baseline SHA + diff ratio |
| T-VIS-SYS-003-S02 | ANDROID_SCREENSHOT | SYS-003-S02 | 版本更新与下载状态 - 已是最新版 视觉比对 | actual/SYS-003-S02.png + diff/SYS-003-S02.png + baseline SHA + diff ratio |
| T-VIS-SYS-003-S03 | ANDROID_SCREENSHOT | SYS-003-S03 | 版本更新与下载状态 - 可选更新可用 视觉比对 | actual/SYS-003-S03.png + diff/SYS-003-S03.png + baseline SHA + diff ratio |
| T-VIS-SYS-003-S04 | ANDROID_SCREENSHOT | SYS-003-S04 | 版本更新与下载状态 - 强制更新可用 视觉比对 | actual/SYS-003-S04.png + diff/SYS-003-S04.png + baseline SHA + diff ratio |
| T-VIS-SYS-003-S05 | ANDROID_SCREENSHOT | SYS-003-S05 | 版本更新与下载状态 - 正在下载 视觉比对 | actual/SYS-003-S05.png + diff/SYS-003-S05.png + baseline SHA + diff ratio |
| T-VIS-SYS-003-S06 | ANDROID_SCREENSHOT | SYS-003-S06 | 版本更新与下载状态 - 下载失败 视觉比对 | actual/SYS-003-S06.png + diff/SYS-003-S06.png + baseline SHA + diff ratio |
| T-VIS-SYS-003-S07 | ANDROID_SCREENSHOT | SYS-003-S07 | 版本更新与下载状态 - 等待安装 视觉比对 | actual/SYS-003-S07.png + diff/SYS-003-S07.png + baseline SHA + diff ratio |
| T-VIS-SYS-003-S08 | ANDROID_SCREENSHOT | SYS-003-S08 | 版本更新与下载状态 - 安装受阻 视觉比对 | actual/SYS-003-S08.png + diff/SYS-003-S08.png + baseline SHA + diff ratio |
| T-VIS-SYS-OV-001-S01 | ANDROID_SCREENSHOT | SYS-OV-001-S01 | 可选更新弹窗 - 可选更新 视觉比对 | actual/SYS-OV-001-S01.png + diff/SYS-OV-001-S01.png + baseline SHA + diff ratio |
| T-VIS-SYS-OV-001-S02 | ANDROID_SCREENSHOT | SYS-OV-001-S02 | 可选更新弹窗 - 下载中 视觉比对 | actual/SYS-OV-001-S02.png + diff/SYS-OV-001-S02.png + baseline SHA + diff ratio |
| T-VIS-SYS-OV-001-S03 | ANDROID_SCREENSHOT | SYS-OV-001-S03 | 可选更新弹窗 - 下载失败 视觉比对 | actual/SYS-OV-001-S03.png + diff/SYS-OV-001-S03.png + baseline SHA + diff ratio |
| T-VIS-SYS-OV-002-S01 | ANDROID_SCREENSHOT | SYS-OV-002-S01 | 强制更新弹窗 - 强制更新 视觉比对 | actual/SYS-OV-002-S01.png + diff/SYS-OV-002-S01.png + baseline SHA + diff ratio |
| T-VIS-SYS-OV-002-S02 | ANDROID_SCREENSHOT | SYS-OV-002-S02 | 强制更新弹窗 - 强制更新下载中 视觉比对 | actual/SYS-OV-002-S02.png + diff/SYS-OV-002-S02.png + baseline SHA + diff ratio |
| T-VIS-SYS-OV-002-S03 | ANDROID_SCREENSHOT | SYS-OV-002-S03 | 强制更新弹窗 - 强制更新失败 视觉比对 | actual/SYS-OV-002-S03.png + diff/SYS-OV-002-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-012-S02 | ANDROID_SCREENSHOT | ME-012-S02 | 关于商推客 - 检查更新 视觉比对 | actual/ME-012-S02.png + diff/ME-012-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-012-S03 | ANDROID_SCREENSHOT | ME-012-S03 | 关于商推客 - 已是最新版 视觉比对 | actual/ME-012-S03.png + diff/ME-012-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-012-S04 | ANDROID_SCREENSHOT | ME-012-S04 | 关于商推客 - 发现更新 视觉比对 | actual/ME-012-S04.png + diff/ME-012-S04.png + baseline SHA + diff ratio |
| T-VIS-ME-012-S05 | ANDROID_SCREENSHOT | ME-012-S05 | 关于商推客 - 检查失败 视觉比对 | actual/ME-012-S05.png + diff/ME-012-S05.png + baseline SHA + diff ratio |
| T-UI-INT-UPD-001 | ANDROID_UI | INT-UPD-001 | 交互：检查更新 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-UPD-002 | ANDROID_UI | INT-UPD-002 | 交互：开始下载更新 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-UPD-003 | ANDROID_UI | INT-UPD-003 | 交互：重试更新下载 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-UPD-004 | ANDROID_UI | INT-UPD-004 | 交互：启动系统安装器 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-UPD-005 | ANDROID_UI | INT-UPD-005 | 交互：可选更新立即更新 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-UPD-006 | ANDROID_UI | INT-UPD-006 | 交互：可选更新稍后 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-UPD-007 | ANDROID_UI | INT-UPD-007 | 交互：强制更新立即更新 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ABOUT-001 | ANDROID_UI | INT-ABOUT-001 | 交互：关于页检查更新 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-API-resolveAppLink | API_CONTRACT_INTEGRATION | resolveAppLink | 接口合同与错误路径：GET /v1/app-links/resolve | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-ADM-ADM-SYS-002 | ADMIN_UI_API | ADM-SYS-002 | 后台页面与权限：维护模式与功能开关 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-SYS-003 | ADMIN_UI_API | ADM-SYS-003 | 后台页面与权限：App Links 与下载配置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-SYS-004 | ADMIN_UI_API | ADM-SYS-004 | 后台页面与权限：项目系统诊断 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-REL-V1.4.0-BUILD | RELEASE | V1.4.0 | 同一 Commit Release APK 构建与来源校验 | APK + Build Info + CI Provenance + SHA-256 |
| T-REL-V1.4.0-EMULATOR | RELEASE | V1.4.0 | API 37 模拟器全量交互回归 | JUnit XML + 视频/截图 + logcat + 失败重现步骤 |
| T-REL-V1.4.0-VISUAL | RELEASE | V1.4.0 | 本版新增状态与历史核心状态视觉回归 | 视觉差异 HTML/JSON + actual/baseline/diff |
| T-REL-V1.4.0-UPGRADE | RELEASE | V1.4.0 | 覆盖安装与数据兼容 | 安装日志、版本号、登录态/缓存/数据库迁移结果 |
| T-REL-V1.4.0-DESKTOP | RELEASE | V1.4.0 | 桌面交付内容完整性 | 桌面目录清单、全部 SHA-256、来源 Commit |

## 项目所有者真机测试

1. 覆盖安装 1.4.0，执行五版本数据库、登录态、缓存和签名兼容终验。
2. 后台发布可选更新，验证稍后与立即更新；发布强制更新，验证无法绕过进入业务。
3. 下载 APK 后校验 SHA-256、包名和签名，再启动系统安装器；篡改包必须拒绝。
4. 点击 stk.zz-yihao.com/project/<id>，安装 App 时进入详情，未安装时进入移动下载落地。
5. 开启/关闭维护模式，验证 App 状态、重试和后台恢复。
6. 运行全部按钮、全部当前状态、关键历史 Golden、安全、性能和外部能力真机终验。

## 失败提交内容

记录设备型号、Android 版本、App versionName/versionCode、操作步骤、期望/实际、截图/录屏、发生时间和 request_id。涉及外部 App 时同时记录目标 App 是否安装及版本。
