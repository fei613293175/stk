# 发送给 Codex：开始商推客 1.0.0

继续开发商推客，但不要根据聊天记忆重新规划项目或选择版本。先确认仓库 remote 是 `https://github.com/fei613293175/stk.git`，读取 `CURRENT_RELEASE.yaml`；只有当前版本为 `V1.0.0` 时才继续。

开发前先读取并确认线上环境基线：Discuz! X5.0 根目录 `/www/wwwroot/stk_zz_yihao_com`，公共站点 `https://stk.zz-yihao.com`，`stk-api.zz-yihao.com` 与 `stk-admin.zz-yihao.com` 已由 Owner 报告解析，线上服务器已有 Android 构建环境。后台用户名为 `admin`，密码只从本机 `ADMIN_ACCESS_HANDOFF.local.md` 读取，不得复制到代码、CI、包清单或聊天；阿里云短信等配置由 Owner 后续填写。以上线上状态仍须在实际使用前按需验证，不得把 Owner 报告当成本机实测证据。

随后读取：

1. `AGENTS.md`
2. `versions/V1.0.0/VERSION_DEVELOPMENT_SPEC.md`
3. `versions/V1.0.0/UI_FIXED_RULES.md`
4. `versions/V1.0.0/FRONTEND_SCOPE.md`
5. `versions/V1.0.0/BACKEND_ADMIN_SCOPE.md`
6. `versions/V1.0.0/PAGE_STATE_MOCKUP_BINDINGS.csv`
7. 本版本相关 Feature、页面合同、API、数据表、后台、配置和测试合同

先运行一次 `python scripts/validate_package.py` 和 `python scripts/check_mockup_readiness.py --release V1.0.0`。通过后立即进入实际编码，不要重复全仓规划和无意义检查。严格按批准效果图与固定 Token 开发 UI，功能只按合同；前端、Discuz 插件后端、数据库、后台配置/查询/审计和自动测试必须同版闭环。

完成后在 GitHub Actions 对同一 Commit 执行 API 37 模拟器、全部新增按钮、截图比对、历史核心回归和首次安装。只下载通过验收的精确 Release APK Artifact，核对 SHA 与来源后复制到本机桌面 `商推客交付\1.0.0`，附完整计划、完成、测试和部署清单。未满足关闭清单不得进入下一版本。
