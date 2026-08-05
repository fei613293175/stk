# 商推客 Codex 开发文档与效果图包

**包版本：V1.0.0**  
**效果图完成版日期：2026-08-05**  
**主域名：`zz-yihao.com`**  
**现有入口：`stk.zz-yihao.com`**  
**代码仓库：`https://github.com/fei613293175/stk.git`**

## 0. 当前线上环境基线（所有版本共用）

- Discuz! X5.0 已部署在服务器目录 `/www/wwwroot/stk_zz_yihao_com`，公共网址为 `https://stk.zz-yihao.com`。
- `stk-api.zz-yihao.com`、`stk-admin.zz-yihao.com` 已由项目所有者完成解析；线上服务器已有 Android 构建等环境，可按需使用。
- 后台账号为 `admin`；密码仅记录在本机 `ADMIN_ACCESS_HANDOFF.local.md`，不复制到版本合同、Git、CI、日志或聊天。
- 阿里云短信等配置由项目所有者后续自行在后台填写；在填写前，开发使用 Adapter/Fake 和待办动作验证链路，不伪造真实短信成功。
- 服务器真实公网 IP/CNAME 仍不得猜测；需要时从线上只读盘点或健康检查取得证据。

## 1. 本包已经完成的内容

本包已经把商推客五个可交付 Android 版本的前端页面、页面状态、固定 UI 数值、交互、API、Discuz 插件后端、数据表、后台页面、后台配置、错误码、测试、域名、仓库和桌面交付做成一一映射合同。

本包现已包含 **44 个视觉对象、236 个页面状态对应的 236 张独立 PNG 效果图**，以及 **44 张页面状态审阅总览板**。所有独立效果图均使用 STK-DS-1.0 固定 Token 和 `ui/FIXTURE_DATA.yaml` 的固定测试数据，以确定性代码直接排版中文，输出尺寸统一为 **1170×2532px**。`contracts/mockup-manifest.csv` 已写入每张图片的实际路径、`APPROVED` 状态、SHA-256、审阅标识和时间。

效果图只决定 UI、布局、层级和状态表现；实际功能仍以 Feature、Interaction、API、数据库、后台配置和测试合同为唯一依据。效果图中的项目、头像、日期、金额和联系方式均是测试数据，不能据此扩展功能。

## 2. 五个正式 APK 版本

| 版本 | versionCode | 范围 | 分支 | Tag |
|---|---:|---|---|---|
| 1.0.0 | 10000 | 原生底座、统一设计系统、登录注册与最小可浏览闭环 | release/1.0.0 | stk-android-v1.0.0 |
| 1.1.0 | 10100 | 首页发现、搜索筛选、缓存与外部联系动作 | release/1.1.0 | stk-android-v1.1.0 |
| 1.2.0 | 10200 | 项目发布、图片上传、我的发布与后台审核闭环 | release/1.2.0 | stk-android-v1.2.0 |
| 1.3.0 | 10300 | 我的页面、会员/账户/道具展示与运营配置 | release/1.3.0 | stk-android-v1.3.0 |
| 1.4.0 | 10400 | 更新分发、App Links、生产加固与全量验收 | release/1.4.0 | stk-android-v1.4.0 |

所有版本使用固定 `applicationId=com.zzyihao.stk` 和同一正式签名。V1.1.0 起必须先安装上一版，再执行覆盖安装测试，禁止卸载旧版掩盖迁移问题。

## 3. 正确使用顺序

1. 阅读 `AGENTS.md`、`PROJECT_CONTEXT.yaml`、`MASTER_DEVELOPMENT_PLAN.md`。
2. 运行一次 `python scripts/validate_package.py`。
3. 运行一次 `python scripts/check_mockup_readiness.py --release V1.0.0`；当前 95 张 V1.0.0 效果图应全部通过。
4. 将本包完整合入固定仓库，Codex 从 `versions/V1.0.0/CODEX_START_PROMPT.md` 开始。
5. 每版只按该版本目录实施，优先编码、测试和可运行结果，禁止重新设计已批准页面。
6. 线上 APK 构建任务对同一 Commit 只生成一次 Artifact；GitHub Actions 的 API 37 模拟器、全部新增交互、截图和覆盖安装任务下载该 Artifact 验收，不重新编译。API 37 模拟器和 `/dev/kvm` 不是 APK 打包前置条件。
7. 只允许下载通过验收的精确 Artifact，并复制到本机桌面版本目录。

需要重新生成视觉资产时，使用 `scripts/render_all_mockups.py`；重新生成会改变图片 SHA，必须重新完成视觉审阅并更新 Manifest，不能静默替换已批准基线。

## 4. 权威文件

| 事实 | 唯一权威文件 |
|---|---|
| 固定项目事实 | `PROJECT_CONTEXT.yaml` |
| 当前版本 | `CURRENT_RELEASE.yaml` |
| 五版本范围 | `contracts/release-version-matrix.yaml` |
| 功能与前后端映射 | `contracts/feature-map.yaml` |
| UI 固定数值 | `contracts/ui-design-tokens.yaml` |
| 公共组件 | `contracts/ui-component-specs.yaml` |
| 页面与固定布局 | `contracts/ui-page-catalog.yaml`、`ui-page-layout-matrix.csv` |
| 页面状态与效果图 | `ui-state-catalog.csv`、`mockup-manifest.csv`、`ui/mockups/approved/` |
| 页面审阅总览 | `ui/mockups/review-sheets/` |
| 效果图生成审计 | `ui/MOCKUP_GENERATION_REPORT.md`、`audit/MOCKUP_RENDER_AUDIT.json` |
| 交互与按钮测试 | `ui-interaction-map.csv`、`interaction-test-bindings.csv` |
| API | `api-inventory.csv`、`openapi.yaml` |
| 数据表 | `database-entity-catalog.yaml`、`backend/sql/` |
| 后台与配置 | `admin-menu-map.yaml`、`config-inventory.csv` |
| 域名 | `domain-delivery-map.yaml` |
| 仓库和推送 | `repository-policy.yaml` |
| APK 与桌面交付 | `owner-delivery-contract.yaml` |

## 5. 重要边界

### 构建环境硬门禁（全部版本）

Android SDK/JDK/Gradle/AGP、PHP/Discuz、签名、模拟器、部署和 Release 验收只能在线上服务器或 GitHub Actions 执行。本机不得安装、下载、配置、升级或运行这些环境；本机只允许源码编辑、合同/静态检查和线上证据整理。线上执行遵循“复用优先”：先只读盘点服务器已有环境，满足合同就直接复用；优先复用已有 Docker 镜像、SDK/Gradle 缓存和独立 volume；只有缺失且不影响现有业务时，才在独立目录/容器补齐。禁止替换系统运行时、改动现有 Nginx/PHP-FPM/MySQL/Discuz 配置、占用业务端口或重启业务服务。该门禁分别写入 `versions/V1.0.0` 至 `versions/V1.4.0` 的开发规格、启动提示和交付清单，并要求每版记录实际环境路径、版本、复用/补齐决策和验证证据。

- 不允许把原生 APK 做成远程 H5 WebView 外壳。
- 不开发 PC 用户前端；Discuz 管理后台仍供管理员在电脑使用。
- 不实现支付、提现、返佣结算、会员在线开通、道具购买、收藏、浏览记录或实名认证。
- 所有占位入口必须打开有设计、有文案的明确占位页或阶段说明，禁止无响应。
- 示例用户名、金额、日期和项目内容只用于视觉验收，不产生新功能。
- 阿里云密钥、管理员密码、APK 签名和服务器 Secret 不得进入 Git、CI Artifact、日志或聊天。
