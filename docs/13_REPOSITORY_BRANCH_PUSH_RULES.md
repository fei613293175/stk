# 13 仓库、分支与推送规则

## 固定仓库

```text
https://github.com/fei613293175/stk.git
```

当前远程为空时，首次可用项目包和工程骨架允许建立 `main`。首次推送后立即启用保护：禁止 force push、禁止删除 main、要求 CI 状态、要求 PR。

## 每版分支

- `release/1.0.0`
- `release/1.1.0`
- `release/1.2.0`
- `release/1.3.0`
- `release/1.4.0`

每版一个 PR，合并后创建不可变 Tag。Commit 说明至少引用 Feature ID，例如：`feat(AUTH-003): complete transactional mobile registration`。

## 推送节奏

完成一个可编译、带测试的纵向单元后推送；需要远程模拟器/截图时推送。禁止每改一个文档就制造噪声提交，也禁止积压整个版本最后一次性大提交。服务器不是源码仓库，生产服务器不得直接修改未提交源码。

## 公共仓库安全

仓库为 public：任何 AccessKey、数据库密码、管理员密码、正式签名、Token、服务器私钥、真实验证码和 `.env` 都不得进入历史。PR 前运行 secret scan；发现 Secret 后必须先撤销/轮换，再清理历史，不能只删除当前文件。
