# 应用于当前仓库的方法

## 原则

本包是轻量覆盖层和可运行脚手架，不是重新开工命令。

## 操作顺序

1. 创建保护分支或 Tag，记录当前 HEAD；
2. 找到最近一次成功构建 APK 的 Commit；
3. 列出当前已经完成的 Android 页面、API、Discuz 文件和测试；
4. 将旧的重型工作流移出 `.github/workflows/`，不要删除历史；
5. 只安装本包的 `.github/workflows/fast-ci.yml`；
6. 现有 `android/` 可构建时，不覆盖整个工程，只合并缺失的设计 Token、Fake Repository、脚本和签名配置；
7. 现有 `discuz/source/plugin/stk_auth/` 更完整时，不覆盖，只对照 `CURRENT_API.md` 补缺失动作；
8. 运行静态快速检查；
9. 立即构建和交付 Beta APK；
10. APK 交付后再继续非阻断修复。

## 必须停用的旧门禁

普通 Push 不得触发：

- API 37 模拟器；
- API 36 全状态模拟器；
- 95 或 236 状态视觉采集；
- 正式签名验证；
- Discuz 生产部署；
- DNS/TLS 探测；
- 完整 Release Candidate 流程。

## 冲突处理

优先级：

```text
当前可工作代码
> 本包 CURRENT_TASK / CURRENT_ACCEPTANCE
> 本包脚手架实现
> 旧开发包规则
```

旧规则与当前任务冲突时，以本包为准，但不得借此删除已经有效的业务代码。
