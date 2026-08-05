# Android 工程蓝图

```text
android-app/
  app/
  core-designsystem/
  core-network/
  core-database/
  core-data/
  core-security/
  core-external/
  core-update/
  feature-auth/
  feature-home/
  feature-project-detail/
  feature-publish/
  feature-profile/
```

`app` 只组装依赖、启动和导航；业务逻辑不得堆入 MainActivity。每个 Feature 模块含 route/screen/components/viewmodel/domain/data/testFixtures/tests。核心模块不依赖 Feature；Feature 之间通过导航合同和共享 model 通信，禁止循环依赖。

Release/Debug 分离：Debug 允许 FixtureServer、状态选择器和截图入口；Release 不包含调试菜单、Mock URL 或明文日志。网络 Base URL 由 BuildConfig 从受控环境配置生成，生产固定 `https://stk-api.zz-yihao.com`。
