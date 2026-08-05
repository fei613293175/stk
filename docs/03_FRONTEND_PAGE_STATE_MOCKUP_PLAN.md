# 03 前端页面、状态与效果图编号计划

## 1. 本文档只规划 Android 用户前端视觉对象

后台页面不绑定 Android 效果图。每个用户页面、覆盖层、系统覆盖层和设计规范板都有唯一 Page ID；每个需要独立视觉验收的状态都有 State ID 和一张独立 1170×2532px 效果图记录。

当前共有 **44 个视觉对象、236 个页面状态、236 张独立效果图**。图片已全部生成到 `ui/mockups/approved/`，Manifest 状态为 `APPROVED`，并逐张记录 SHA-256。

## 2. 页面总表

| Page ID | 页面 | 类型 | 首次版本 | 路由 | 状态数 | 交互数 | 页面身份签名 |
|---|---|---|---|---|---|---|---|
| DS-001 | 色彩、字体、间距与圆角规范板 | DESIGN_BOARD | V1.0.0 | design://tokens | 1 | 0 | 颜色样本+字体层级+间距刻度+圆角样本 |
| DS-002 | 公共组件规范板 | COMPONENT_BOARD | V1.0.0 | design://components | 1 | 0 | 顶部栏+底部导航+按钮+输入框+标签+卡片 |
| DS-003 | 全局状态与反馈规范板 | SYSTEM_STATE_BOARD | V1.0.0 | design://states | 1 | 0 | 骨架+空状态+错误+成功+Snackbar+Dialog |
| SYS-001 | 启动与路由判定 | PAGE | V1.0.0 | stk://bootstrap | 8 | 2 | 品牌标识+启动状态+恢复入口 |
| SYS-002 | 维护、离线与服务不可用 | PAGE | V1.0.0 | stk://system/unavailable | 5 | 2 | 系统状态插图+明确原因+重试/退出 |
| SYS-003 | 版本更新与下载状态 | PAGE | V1.4.0 | stk://system/update | 8 | 4 | 当前版本+新版本+更新日志+下载状态 |
| SYS-OV-001 | 可选更新弹窗 | SYSTEM_OVERLAY | V1.4.0 | overlay://optional-update | 3 | 2 | 版本号+更新摘要+稍后/立即更新 |
| SYS-OV-002 | 强制更新弹窗 | SYSTEM_OVERLAY | V1.4.0 | overlay://forced-update | 3 | 1 | 版本号+强制说明+立即更新 |
| COM-OV-001 | 受控外部链接确认 | OVERLAY | V1.1.0 | overlay://external-link | 3 | 2 | 目标服务+域名+风险说明+继续/取消 |
| COM-OV-002 | 阶段功能说明弹窗 | OVERLAY | V1.0.0 | overlay://stage-scope | 4 | 1 | 功能名称+开放版本+返回 |
| AUTH-001 | 登录 | PAGE | V1.0.0 | stk://auth/login | 16 | 13 | 商推客品牌+密码/短信双模式+主登录按钮 |
| AUTH-002 | 注册 | PAGE | V1.0.0 | stk://auth/register | 12 | 9 | 手机号+密码+确认密码+协议+注册按钮 |
| AUTH-003 | 找回并重置密码 | PAGE | V1.0.0 | stk://auth/reset-password | 11 | 7 | 手机号+短信验证码+新密码+确认密码 |
| AUTH-004 | 用户协议 | PAGE | V1.0.0 | stk://legal/user-agreement | 4 | 2 | 固定标题+版本日期+协议正文 |
| AUTH-005 | 隐私政策 | PAGE | V1.0.0 | stk://legal/privacy-policy | 4 | 2 | 固定标题+版本日期+隐私正文 |
| AUTH-OV-001 | 安全验证码弹窗 | OVERLAY | V1.0.0 | overlay://captcha | 8 | 4 | 验证码图片+输入框+刷新+确认 |
| AUTH-OV-002 | 账号与风控提示 | OVERLAY | V1.0.0 | overlay://auth-risk | 4 | 1 | 状态图标+原因+恢复建议+确认 |
| HOME-001 | 首页项目流 | PAGE | V1.0.0 | stk://home | 13 | 11 | 品牌顶部栏+搜索+分类+单列项目卡+底部导航 |
| HOME-002 | 项目搜索 | PAGE | V1.1.0 | stk://search | 7 | 6 | 搜索输入+结果统计+项目列表 |
| HOME-003 | 项目分类筛选 | OVERLAY | V1.1.0 | overlay://categories | 4 | 4 | 分类列表+当前选中+确认/重置 |
| HOME-004 | 项目详情 | PAGE | V1.0.0 | stk://project/{project_id} | 11 | 8 | 项目图片+标题简介+发布者+联系方式+底部操作 |
| HOME-OV-001 | 项目图片查看器 | OVERLAY | V1.1.0 | overlay://project-images | 3 | 4 | 沉浸式图片+页码+缩放+关闭 |
| HOME-OV-002 | 联系方式操作面板 | OVERLAY | V1.1.0 | overlay://contact-actions | 7 | 6 | 联系方式类型+脱敏/完整值+复制/拨打/打开 |
| PUB-001 | 发布项目 | PAGE | V1.2.0 | stk://publish | 13 | 14 | 标题+简介+分类+多图+联系方式+协议+提交 |
| PUB-002 | 图片排序与封面 | PAGE | V1.2.0 | stk://publish/images | 4 | 4 | 可拖动图片网格+封面标识+完成 |
| PUB-003 | 发布提交结果 | PAGE | V1.2.0 | stk://publish/result | 3 | 3 | 结果插图+审核状态+查看/继续发布 |
| PUB-004 | 编辑项目 | PAGE | V1.2.0 | stk://project/{project_id}/edit | 8 | 2 | 已有内容表单+审核提示+保存 |
| PUB-OV-001 | 删除图片确认 | OVERLAY | V1.2.0 | overlay://delete-image | 3 | 2 | 缩略图+删除说明+取消/删除 |
| PUB-OV-002 | 未保存退出确认 | OVERLAY | V1.2.0 | overlay://discard-draft | 1 | 2 | 未保存说明+继续编辑/放弃 |
| PUB-OV-003 | 联系方式类型选择 | OVERLAY | V1.2.0 | overlay://contact-type | 3 | 2 | 手机/微信/QQ/网址等后台允许类型 |
| ME-001 | 我的 | PAGE | V1.0.0 | stk://me | 8 | 11 | 资料卡+双账户+会员卡+道具+常用功能+底部导航 |
| ME-002 | 我的发布 | PAGE | V1.2.0 | stk://me/projects | 11 | 6 | 状态标签页+本人项目列表+项目操作 |
| ME-003 | 个人资料只读页 | PAGE | V1.3.0 | stk://me/profile | 3 | 1 | 头像+昵称+UID+脱敏手机号+注册时间 |
| ME-004 | 会员中心展示 | PAGE | V1.3.0 | stk://me/member | 5 | 2 | 会员卡+权益说明+状态+有效期 |
| ME-005 | 账户余额展示 | PAGE | V1.3.0 | stk://me/wallets | 3 | 2 | 佣金账户+任务账户+只读说明 |
| ME-006 | 道具中心展示 | PAGE | V1.3.0 | stk://me/props | 5 | 2 | 刷新卡/超级头条/头条/变色卡展示 |
| ME-007 | 浏览记录占位 | PAGE | V1.3.0 | stk://me/history | 1 | 1 | 功能图标+范围说明+返回 |
| ME-008 | 收藏占位 | PAGE | V1.3.0 | stk://me/favorites | 1 | 1 | 功能图标+范围说明+返回 |
| ME-009 | 实名认证占位 | PAGE | V1.3.0 | stk://me/real-name | 1 | 1 | 安全图标+未开放说明+返回 |
| ME-010 | 联系客服 | PAGE | V1.3.0 | stk://me/support | 5 | 3 | 客服说明+联系方式+复制/外部打开 |
| ME-011 | 设置 | PAGE | V1.3.0 | stk://me/settings | 5 | 5 | 清理缓存+协议+隐私+关于+退出登录 |
| ME-012 | 关于商推客 | PAGE | V1.3.0 | stk://me/about | 5 | 2 | Logo+版本号+更新检查+备案/版权信息 |
| ME-OV-001 | 退出登录确认 | OVERLAY | V1.0.0 | overlay://logout | 3 | 2 | 退出说明+取消/确认退出 |
| ME-OV-002 | 本人项目操作面板 | OVERLAY | V1.2.0 | overlay://my-project-actions | 4 | 6 | 查看/编辑/下架/重提/删除按状态显示 |

## 3. 强制规则

- Overlay 必须绑定父页面；组件规范板不得实现成业务页面。
- 一个 State ID 对应一张独立图片，禁止用总拼图替代独立基线；总览板只能作为额外检查板。
- Canonical 图片统一 1170×2532px；手机状态栏/安全区布局与 390×844dp 基准一致。
- 图片文件名由 `contracts/mockup-manifest.csv` 固定；不得在生成时自行改 Page ID、State ID 或路径。
- 网络错误、超时、服务错误、空数据、离线、操作成功/失败等状态按页面适用性规划，不机械复制无意义状态。
- 视觉重复审计必须检查跨 Page ID 完全重复、同页不同状态完全重复和页面身份签名冲突。

## 4. 页面合同

`ui/page-contracts/` 为每个 Page ID 提供单独 YAML：固定布局、状态、交互、效果图路径、页面身份、必要内容和禁止内容。Codex 开发某页时只需读取该页合同、全局 Token 和所属版本文档，不得重新设计。
