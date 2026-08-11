# 商推客 V1.3.0 + V1.4.0 详细视觉开发覆盖包

本包将 **V1.3.0 的 37 张效果图** 与 **V1.4.0 的 23 张效果图** 合并为一个轻量覆盖包，供 Codex 在当前仓库上连续完成两个版本。

## 包含内容
- V1.3.0：37 张页面状态效果图
- V1.4.0：23 张页面状态效果图
- 合计：60 张
- 页面/弹层：17 个
- 交互映射：43 项
- API 参考：6 项
- 后台页面参考：9 项
- 配置参考：24 项
- 数据实体参考：4 项

## 开发顺序
1. 先完成 V1.3.0（我的与运营展示）
2. 再完成 V1.4.0（更新分发与生产发布相关页面）
3. 不要重新初始化仓库，不要重做早期版本，不要重新引入 API 37 阻断

## 安装
### Windows
powershell -ExecutionPolicy Bypass -File .\STK_V130_V140_VISUAL\scripts\install-visual-overlay.ps1 -RepoPath .

### Linux
bash ./STK_V130_V140_VISUAL/scripts/install-visual-overlay.sh .
