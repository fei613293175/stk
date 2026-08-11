# Beta 签名说明

`stk-beta.jks` 是公开的测试签名，只用于 Beta 覆盖安装和 CI 产物一致性。

```text
alias: stk-beta
store password: stkbeta2026
key password: stkbeta2026
```

该密钥已进入开发包，因此不具备生产安全性。正式生产发布前必须替换为离线保存的正式签名，并建立单独的安全交付流程。
