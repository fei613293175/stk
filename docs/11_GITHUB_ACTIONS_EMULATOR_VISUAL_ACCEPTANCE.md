# 11 GitHub Actions、模拟器、按钮遍历与视觉验收

## 1. 每版必跑的流水线

```text
合同与残留扫描
→ PHP/后端单元与接口集成
→ Android 编译、Lint、单元、Room Migration
→ API 37 固定模拟器安装上一版（V1.1 起）
→ adb install -r 当前版
→ Compose UI 测试遍历本版全部新增 Interaction ID
→ 历史已发布核心交互回归
→ 逐 State fixture 截图
→ 与 APPROVED 效果图对比
→ 安全/性能/崩溃检查
→ 构建同一 Commit 的 Universal Release APK
→ 生成 Provenance、Build Info 和 SHA-256
```

## 2. 固定模拟器

- Android API 37、x86_64、Pixel 6 画像、390×844dp 等效窗口；语言 `zh-CN`；时区 `Asia/Shanghai`；字体缩放 1.0；浅色主题；动画比例固定。
- 视觉测试使用确定性 Fixture 数据、固定系统时间、固定图片和 Mock Server；不得连接随机线上数据截图。
- 响应式结构额外在 360、412、430dp 运行布局/截断测试。

## 3. 每一个按钮都必须测试

`contracts/ui-interaction-map.csv` 中每条 Interaction 有唯一 `test_tag`。`interaction-test-bindings.csv` 必须一一绑定自动测试。新增按钮、图标点击、菜单、输入、滑动或系统结果未登记时合同校验失败。

## 4. 视觉差异规则

- Baseline 必须是 Manifest 状态 `APPROVED` 且 SHA-256 匹配的图片。
- 输出 baseline、actual、diff、全图差异比例、最大连续差异区域和结构区域结果。
- 默认全图像素差异比例不超过 1.0%；顶部栏、底部导航、主按钮和输入框等结构区不超过 0.2%。抗锯齿造成的微小文字边缘差异可由阈值吸收，但不能用高阈值掩盖位置、字号、圆角或颜色偏差。
- 差异超阈值只能修代码或通过正式设计变更更新 Baseline；禁止自动覆盖已批准图片。

## 5. 外部能力测试

阿里云短信、微信、QQ、拨号、浏览器和安装器在 CI 使用 Adapter/Fake 验证调用参数、白名单与回退；发布前由项目所有者在真机进行小流量真实短信和外部 App 验收。支付不在本五版本范围。

## 6. 证据

每版 Artifact 必须保留 JUnit XML、API 报告、Room schema、模拟器截图、视觉差异 JSON/HTML、logcat、APK、Build Info、Provenance 和 SHA-256。失败不得仍标记发布成功。
