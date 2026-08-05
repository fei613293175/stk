# 1.1.0 自动测试与项目所有者验收

## 自动测试合同

本版共有 64 条首次引入测试合同；发布时还必须回归所有历史已发布 Feature 的核心交互和 Golden 状态。详细清单见 `contracts/test-catalog.csv`。

| Test ID | 层级 | 目标 | 名称 | 证据 |
|---|---|---|---|---|
| T-VIS-SYS-001-S04 | ANDROID_SCREENSHOT | SYS-001-S04 | 启动与路由判定 - 离线但有登录态 视觉比对 | actual/SYS-001-S04.png + diff/SYS-001-S04.png + baseline SHA + diff ratio |
| T-VIS-COM-OV-001-S01 | ANDROID_SCREENSHOT | COM-OV-001-S01 | 受控外部链接确认 - 白名单外链确认 视觉比对 | actual/COM-OV-001-S01.png + diff/COM-OV-001-S01.png + baseline SHA + diff ratio |
| T-VIS-COM-OV-001-S02 | ANDROID_SCREENSHOT | COM-OV-001-S02 | 受控外部链接确认 - 外链被阻断 视觉比对 | actual/COM-OV-001-S02.png + diff/COM-OV-001-S02.png + baseline SHA + diff ratio |
| T-VIS-COM-OV-001-S03 | ANDROID_SCREENSHOT | COM-OV-001-S03 | 受控外部链接确认 - 目标应用未安装 视觉比对 | actual/COM-OV-001-S03.png + diff/COM-OV-001-S03.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S04 | ANDROID_SCREENSHOT | HOME-001-S04 | 首页项目流 - 分类筛选结果 视觉比对 | actual/HOME-001-S04.png + diff/HOME-001-S04.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S08 | ANDROID_SCREENSHOT | HOME-001-S08 | 首页项目流 - 离线缓存 视觉比对 | actual/HOME-001-S08.png + diff/HOME-001-S08.png + baseline SHA + diff ratio |
| T-VIS-HOME-001-S09 | ANDROID_SCREENSHOT | HOME-001-S09 | 首页项目流 - 离线无缓存 视觉比对 | actual/HOME-001-S09.png + diff/HOME-001-S09.png + baseline SHA + diff ratio |
| T-VIS-HOME-002-S01 | ANDROID_SCREENSHOT | HOME-002-S01 | 项目搜索 - 搜索默认 视觉比对 | actual/HOME-002-S01.png + diff/HOME-002-S01.png + baseline SHA + diff ratio |
| T-VIS-HOME-002-S02 | ANDROID_SCREENSHOT | HOME-002-S02 | 项目搜索 - 输入中 视觉比对 | actual/HOME-002-S02.png + diff/HOME-002-S02.png + baseline SHA + diff ratio |
| T-VIS-HOME-002-S03 | ANDROID_SCREENSHOT | HOME-002-S03 | 项目搜索 - 搜索中 视觉比对 | actual/HOME-002-S03.png + diff/HOME-002-S03.png + baseline SHA + diff ratio |
| T-VIS-HOME-002-S04 | ANDROID_SCREENSHOT | HOME-002-S04 | 项目搜索 - 有结果 视觉比对 | actual/HOME-002-S04.png + diff/HOME-002-S04.png + baseline SHA + diff ratio |
| T-VIS-HOME-002-S05 | ANDROID_SCREENSHOT | HOME-002-S05 | 项目搜索 - 无结果 视觉比对 | actual/HOME-002-S05.png + diff/HOME-002-S05.png + baseline SHA + diff ratio |
| T-VIS-HOME-002-S06 | ANDROID_SCREENSHOT | HOME-002-S06 | 项目搜索 - 离线缓存结果 视觉比对 | actual/HOME-002-S06.png + diff/HOME-002-S06.png + baseline SHA + diff ratio |
| T-VIS-HOME-002-S07 | ANDROID_SCREENSHOT | HOME-002-S07 | 项目搜索 - 搜索失败 视觉比对 | actual/HOME-002-S07.png + diff/HOME-002-S07.png + baseline SHA + diff ratio |
| T-VIS-HOME-003-S01 | ANDROID_SCREENSHOT | HOME-003-S01 | 项目分类筛选 - 分类列表 视觉比对 | actual/HOME-003-S01.png + diff/HOME-003-S01.png + baseline SHA + diff ratio |
| T-VIS-HOME-003-S02 | ANDROID_SCREENSHOT | HOME-003-S02 | 项目分类筛选 - 选中分类 视觉比对 | actual/HOME-003-S02.png + diff/HOME-003-S02.png + baseline SHA + diff ratio |
| T-VIS-HOME-003-S03 | ANDROID_SCREENSHOT | HOME-003-S03 | 项目分类筛选 - 无可用分类 视觉比对 | actual/HOME-003-S03.png + diff/HOME-003-S03.png + baseline SHA + diff ratio |
| T-VIS-HOME-003-S04 | ANDROID_SCREENSHOT | HOME-003-S04 | 项目分类筛选 - 分类加载失败 视觉比对 | actual/HOME-003-S04.png + diff/HOME-003-S04.png + baseline SHA + diff ratio |
| T-VIS-HOME-004-S06 | ANDROID_SCREENSHOT | HOME-004-S06 | 项目详情 - 离线缓存详情 视觉比对 | actual/HOME-004-S06.png + diff/HOME-004-S06.png + baseline SHA + diff ratio |
| T-VIS-HOME-OV-001-S01 | ANDROID_SCREENSHOT | HOME-OV-001-S01 | 项目图片查看器 - 图片正常 视觉比对 | actual/HOME-OV-001-S01.png + diff/HOME-OV-001-S01.png + baseline SHA + diff ratio |
| T-VIS-HOME-OV-001-S02 | ANDROID_SCREENSHOT | HOME-OV-001-S02 | 项目图片查看器 - 缩放查看 视觉比对 | actual/HOME-OV-001-S02.png + diff/HOME-OV-001-S02.png + baseline SHA + diff ratio |
| T-VIS-HOME-OV-001-S03 | ANDROID_SCREENSHOT | HOME-OV-001-S03 | 项目图片查看器 - 单图加载失败 视觉比对 | actual/HOME-OV-001-S03.png + diff/HOME-OV-001-S03.png + baseline SHA + diff ratio |
| T-VIS-HOME-OV-002-S01 | ANDROID_SCREENSHOT | HOME-OV-002-S01 | 联系方式操作面板 - 手机号操作 视觉比对 | actual/HOME-OV-002-S01.png + diff/HOME-OV-002-S01.png + baseline SHA + diff ratio |
| T-VIS-HOME-OV-002-S02 | ANDROID_SCREENSHOT | HOME-OV-002-S02 | 联系方式操作面板 - 微信操作 视觉比对 | actual/HOME-OV-002-S02.png + diff/HOME-OV-002-S02.png + baseline SHA + diff ratio |
| T-VIS-HOME-OV-002-S03 | ANDROID_SCREENSHOT | HOME-OV-002-S03 | 联系方式操作面板 - QQ 操作 视觉比对 | actual/HOME-OV-002-S03.png + diff/HOME-OV-002-S03.png + baseline SHA + diff ratio |
| T-VIS-HOME-OV-002-S04 | ANDROID_SCREENSHOT | HOME-OV-002-S04 | 联系方式操作面板 - 网址操作 视觉比对 | actual/HOME-OV-002-S04.png + diff/HOME-OV-002-S04.png + baseline SHA + diff ratio |
| T-VIS-HOME-OV-002-S05 | ANDROID_SCREENSHOT | HOME-OV-002-S05 | 联系方式操作面板 - 外链确认 视觉比对 | actual/HOME-OV-002-S05.png + diff/HOME-OV-002-S05.png + baseline SHA + diff ratio |
| T-VIS-HOME-OV-002-S06 | ANDROID_SCREENSHOT | HOME-OV-002-S06 | 联系方式操作面板 - 应用未安装 视觉比对 | actual/HOME-OV-002-S06.png + diff/HOME-OV-002-S06.png + baseline SHA + diff ratio |
| T-VIS-HOME-OV-002-S07 | ANDROID_SCREENSHOT | HOME-OV-002-S07 | 联系方式操作面板 - 无效目标 视觉比对 | actual/HOME-OV-002-S07.png + diff/HOME-OV-002-S07.png + baseline SHA + diff ratio |
| T-UI-INT-EXT-001 | ANDROID_UI | INT-EXT-001 | 交互：确认打开白名单外链 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-EXT-002 | ANDROID_UI | INT-EXT-002 | 交互：取消外部跳转 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-HOME-002 | ANDROID_UI | INT-HOME-002 | 交互：打开搜索 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-HOME-003 | ANDROID_UI | INT-HOME-003 | 交互：打开分类筛选 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SEARCH-001 | ANDROID_UI | INT-SEARCH-001 | 交互：输入搜索关键词 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SEARCH-002 | ANDROID_UI | INT-SEARCH-002 | 交互：提交搜索 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SEARCH-003 | ANDROID_UI | INT-SEARCH-003 | 交互：清空搜索 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SEARCH-004 | ANDROID_UI | INT-SEARCH-004 | 交互：打开搜索结果详情 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SEARCH-005 | ANDROID_UI | INT-SEARCH-005 | 交互：搜索重试 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SEARCH-006 | ANDROID_UI | INT-SEARCH-006 | 交互：返回首页 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CAT-001 | ANDROID_UI | INT-CAT-001 | 交互：选择分类 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CAT-002 | ANDROID_UI | INT-CAT-002 | 交互：确认分类 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CAT-003 | ANDROID_UI | INT-CAT-003 | 交互：重置分类 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CAT-004 | ANDROID_UI | INT-CAT-004 | 交互：关闭分类 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DETAIL-002 | ANDROID_UI | INT-DETAIL-002 | 交互：打开图片查看器 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DETAIL-003 | ANDROID_UI | INT-DETAIL-003 | 交互：打开联系方式 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DETAIL-004 | ANDROID_UI | INT-DETAIL-004 | 交互：复制项目链接 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-IMG-001 | ANDROID_UI | INT-IMG-001 | 交互：切换项目图片 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-IMG-002 | ANDROID_UI | INT-IMG-002 | 交互：缩放项目图片 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-IMG-003 | ANDROID_UI | INT-IMG-003 | 交互：重试图片加载 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-IMG-004 | ANDROID_UI | INT-IMG-004 | 交互：关闭图片查看器 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CONTACT-001 | ANDROID_UI | INT-CONTACT-001 | 交互：复制联系方式 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CONTACT-002 | ANDROID_UI | INT-CONTACT-002 | 交互：拨打手机号 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CONTACT-003 | ANDROID_UI | INT-CONTACT-003 | 交互：打开微信 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CONTACT-004 | ANDROID_UI | INT-CONTACT-004 | 交互：打开 QQ | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CONTACT-005 | ANDROID_UI | INT-CONTACT-005 | 交互：打开网址 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CONTACT-006 | ANDROID_UI | INT-CONTACT-006 | 交互：关闭联系面板 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-DB-DB-SYS-001 | DATABASE | DB-SYS-001 | 数据表安装、迁移与索引：pre_stk_external_link_whitelist | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-ADM-ADM-PROJ-006 | ADMIN_UI_API | ADM-PROJ-006 | 后台页面与权限：外部联系方式与白名单 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-PROJ-007 | ADMIN_UI_API | ADM-PROJ-007 | 后台页面与权限：搜索与发现设置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-REL-V1.1.0-BUILD | RELEASE | V1.1.0 | 同一 Commit Release APK 构建与来源校验 | APK + Build Info + CI Provenance + SHA-256 |
| T-REL-V1.1.0-EMULATOR | RELEASE | V1.1.0 | API 37 模拟器全量交互回归 | JUnit XML + 视频/截图 + logcat + 失败重现步骤 |
| T-REL-V1.1.0-VISUAL | RELEASE | V1.1.0 | 本版新增状态与历史核心状态视觉回归 | 视觉差异 HTML/JSON + actual/baseline/diff |
| T-REL-V1.1.0-UPGRADE | RELEASE | V1.1.0 | 覆盖安装与数据兼容 | 安装日志、版本号、登录态/缓存/数据库迁移结果 |
| T-REL-V1.1.0-DESKTOP | RELEASE | V1.1.0 | 桌面交付内容完整性 | 桌面目录清单、全部 SHA-256、来源 Commit |

## 项目所有者真机测试

1. 在保留 V1.0.0 的情况下覆盖安装 1.1.0，确认登录态和首页缓存保留。
2. 测试搜索、清空、无结果、分类筛选、下一页加载和下一页失败局部重试。
3. 有网打开首页后断网重启，确认缓存立即显示并有离线标识；首次无缓存断网显示正确错误页。
4. 查看多图、缩放和单图加载失败状态。
5. 测试复制、拨号、微信、QQ、网址和目标 App 未安装回退；非白名单网址必须阻断。
6. 发布入口仍显示 V1.2 开放说明。

## 失败提交内容

记录设备型号、Android 版本、App versionName/versionCode、操作步骤、期望/实际、截图/录屏、发生时间和 request_id。涉及外部 App 时同时记录目标 App 是否安装及版本。
