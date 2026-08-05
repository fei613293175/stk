# 1.2.0 自动测试与项目所有者验收

## 自动测试合同

本版共有 119 条首次引入测试合同；发布时还必须回归所有历史已发布 Feature 的核心交互和 Golden 状态。详细清单见 `contracts/test-catalog.csv`。

| Test ID | 层级 | 目标 | 名称 | 证据 |
|---|---|---|---|---|
| T-VIS-HOME-004-S03 | ANDROID_SCREENSHOT | HOME-004-S03 | 项目详情 - 本人待审核项目 视觉比对 | actual/HOME-004-S03.png + diff/HOME-004-S03.png + baseline SHA + diff ratio |
| T-VIS-HOME-004-S04 | ANDROID_SCREENSHOT | HOME-004-S04 | 项目详情 - 本人被驳回项目 视觉比对 | actual/HOME-004-S04.png + diff/HOME-004-S04.png + baseline SHA + diff ratio |
| T-VIS-HOME-004-S05 | ANDROID_SCREENSHOT | HOME-004-S05 | 项目详情 - 本人已下架项目 视觉比对 | actual/HOME-004-S05.png + diff/HOME-004-S05.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S01 | ANDROID_SCREENSHOT | PUB-001-S01 | 发布项目 - 空表单 视觉比对 | actual/PUB-001-S01.png + diff/PUB-001-S01.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S02 | ANDROID_SCREENSHOT | PUB-001-S02 | 发布项目 - 已输入草稿 视觉比对 | actual/PUB-001-S02.png + diff/PUB-001-S02.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S03 | ANDROID_SCREENSHOT | PUB-001-S03 | 发布项目 - 选择图片 视觉比对 | actual/PUB-001-S03.png + diff/PUB-001-S03.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S04 | ANDROID_SCREENSHOT | PUB-001-S04 | 发布项目 - 图片压缩 视觉比对 | actual/PUB-001-S04.png + diff/PUB-001-S04.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S05 | ANDROID_SCREENSHOT | PUB-001-S05 | 发布项目 - 图片上传 视觉比对 | actual/PUB-001-S05.png + diff/PUB-001-S05.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S06 | ANDROID_SCREENSHOT | PUB-001-S06 | 发布项目 - 部分上传失败 视觉比对 | actual/PUB-001-S06.png + diff/PUB-001-S06.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S07 | ANDROID_SCREENSHOT | PUB-001-S07 | 发布项目 - 发布校验错误 视觉比对 | actual/PUB-001-S07.png + diff/PUB-001-S07.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S08 | ANDROID_SCREENSHOT | PUB-001-S08 | 发布项目 - 提交中 视觉比对 | actual/PUB-001-S08.png + diff/PUB-001-S08.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S09 | ANDROID_SCREENSHOT | PUB-001-S09 | 发布项目 - 提交成功 视觉比对 | actual/PUB-001-S09.png + diff/PUB-001-S09.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S10 | ANDROID_SCREENSHOT | PUB-001-S10 | 发布项目 - 提交网络失败 视觉比对 | actual/PUB-001-S10.png + diff/PUB-001-S10.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S11 | ANDROID_SCREENSHOT | PUB-001-S11 | 发布项目 - 达到每日上限 视觉比对 | actual/PUB-001-S11.png + diff/PUB-001-S11.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S12 | ANDROID_SCREENSHOT | PUB-001-S12 | 发布项目 - 待审核数量上限 视觉比对 | actual/PUB-001-S12.png + diff/PUB-001-S12.png + baseline SHA + diff ratio |
| T-VIS-PUB-001-S13 | ANDROID_SCREENSHOT | PUB-001-S13 | 发布项目 - 登录过期 视觉比对 | actual/PUB-001-S13.png + diff/PUB-001-S13.png + baseline SHA + diff ratio |
| T-VIS-PUB-002-S01 | ANDROID_SCREENSHOT | PUB-002-S01 | 图片排序与封面 - 图片排序 视觉比对 | actual/PUB-002-S01.png + diff/PUB-002-S01.png + baseline SHA + diff ratio |
| T-VIS-PUB-002-S02 | ANDROID_SCREENSHOT | PUB-002-S02 | 图片排序与封面 - 拖动排序 视觉比对 | actual/PUB-002-S02.png + diff/PUB-002-S02.png + baseline SHA + diff ratio |
| T-VIS-PUB-002-S03 | ANDROID_SCREENSHOT | PUB-002-S03 | 图片排序与封面 - 已更换封面 视觉比对 | actual/PUB-002-S03.png + diff/PUB-002-S03.png + baseline SHA + diff ratio |
| T-VIS-PUB-002-S04 | ANDROID_SCREENSHOT | PUB-002-S04 | 图片排序与封面 - 排序保存失败 视觉比对 | actual/PUB-002-S04.png + diff/PUB-002-S04.png + baseline SHA + diff ratio |
| T-VIS-PUB-003-S01 | ANDROID_SCREENSHOT | PUB-003-S01 | 发布提交结果 - 已提交待审核 视觉比对 | actual/PUB-003-S01.png + diff/PUB-003-S01.png + baseline SHA + diff ratio |
| T-VIS-PUB-003-S02 | ANDROID_SCREENSHOT | PUB-003-S02 | 发布提交结果 - 直接发布成功 视觉比对 | actual/PUB-003-S02.png + diff/PUB-003-S02.png + baseline SHA + diff ratio |
| T-VIS-PUB-003-S03 | ANDROID_SCREENSHOT | PUB-003-S03 | 发布提交结果 - 服务端未接受 视觉比对 | actual/PUB-003-S03.png + diff/PUB-003-S03.png + baseline SHA + diff ratio |
| T-VIS-PUB-004-S01 | ANDROID_SCREENSHOT | PUB-004-S01 | 编辑项目 - 编辑加载 视觉比对 | actual/PUB-004-S01.png + diff/PUB-004-S01.png + baseline SHA + diff ratio |
| T-VIS-PUB-004-S02 | ANDROID_SCREENSHOT | PUB-004-S02 | 编辑项目 - 编辑内容 视觉比对 | actual/PUB-004-S02.png + diff/PUB-004-S02.png + baseline SHA + diff ratio |
| T-VIS-PUB-004-S03 | ANDROID_SCREENSHOT | PUB-004-S03 | 编辑项目 - 重新审核提示 视觉比对 | actual/PUB-004-S03.png + diff/PUB-004-S03.png + baseline SHA + diff ratio |
| T-VIS-PUB-004-S04 | ANDROID_SCREENSHOT | PUB-004-S04 | 编辑项目 - 保存中 视觉比对 | actual/PUB-004-S04.png + diff/PUB-004-S04.png + baseline SHA + diff ratio |
| T-VIS-PUB-004-S05 | ANDROID_SCREENSHOT | PUB-004-S05 | 编辑项目 - 保存成功 视觉比对 | actual/PUB-004-S05.png + diff/PUB-004-S05.png + baseline SHA + diff ratio |
| T-VIS-PUB-004-S06 | ANDROID_SCREENSHOT | PUB-004-S06 | 编辑项目 - 版本冲突 视觉比对 | actual/PUB-004-S06.png + diff/PUB-004-S06.png + baseline SHA + diff ratio |
| T-VIS-PUB-004-S07 | ANDROID_SCREENSHOT | PUB-004-S07 | 编辑项目 - 无权或项目不存在 视觉比对 | actual/PUB-004-S07.png + diff/PUB-004-S07.png + baseline SHA + diff ratio |
| T-VIS-PUB-004-S08 | ANDROID_SCREENSHOT | PUB-004-S08 | 编辑项目 - 保存失败 视觉比对 | actual/PUB-004-S08.png + diff/PUB-004-S08.png + baseline SHA + diff ratio |
| T-VIS-PUB-OV-001-S01 | ANDROID_SCREENSHOT | PUB-OV-001-S01 | 删除图片确认 - 确认删除图片 视觉比对 | actual/PUB-OV-001-S01.png + diff/PUB-OV-001-S01.png + baseline SHA + diff ratio |
| T-VIS-PUB-OV-001-S02 | ANDROID_SCREENSHOT | PUB-OV-001-S02 | 删除图片确认 - 删除中 视觉比对 | actual/PUB-OV-001-S02.png + diff/PUB-OV-001-S02.png + baseline SHA + diff ratio |
| T-VIS-PUB-OV-001-S03 | ANDROID_SCREENSHOT | PUB-OV-001-S03 | 删除图片确认 - 删除失败 视觉比对 | actual/PUB-OV-001-S03.png + diff/PUB-OV-001-S03.png + baseline SHA + diff ratio |
| T-VIS-PUB-OV-002-S01 | ANDROID_SCREENSHOT | PUB-OV-002-S01 | 未保存退出确认 - 放弃未保存内容 视觉比对 | actual/PUB-OV-002-S01.png + diff/PUB-OV-002-S01.png + baseline SHA + diff ratio |
| T-VIS-PUB-OV-003-S01 | ANDROID_SCREENSHOT | PUB-OV-003-S01 | 联系方式类型选择 - 选择联系方式 视觉比对 | actual/PUB-OV-003-S01.png + diff/PUB-OV-003-S01.png + baseline SHA + diff ratio |
| T-VIS-PUB-OV-003-S02 | ANDROID_SCREENSHOT | PUB-OV-003-S02 | 联系方式类型选择 - 已选类型 视觉比对 | actual/PUB-OV-003-S02.png + diff/PUB-OV-003-S02.png + baseline SHA + diff ratio |
| T-VIS-PUB-OV-003-S03 | ANDROID_SCREENSHOT | PUB-OV-003-S03 | 联系方式类型选择 - 无可用类型 视觉比对 | actual/PUB-OV-003-S03.png + diff/PUB-OV-003-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S01 | ANDROID_SCREENSHOT | ME-002-S01 | 我的发布 - 项目列表加载 视觉比对 | actual/ME-002-S01.png + diff/ME-002-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S02 | ANDROID_SCREENSHOT | ME-002-S02 | 我的发布 - 全部项目 视觉比对 | actual/ME-002-S02.png + diff/ME-002-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S03 | ANDROID_SCREENSHOT | ME-002-S03 | 我的发布 - 待审核 视觉比对 | actual/ME-002-S03.png + diff/ME-002-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S04 | ANDROID_SCREENSHOT | ME-002-S04 | 我的发布 - 已发布 视觉比对 | actual/ME-002-S04.png + diff/ME-002-S04.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S05 | ANDROID_SCREENSHOT | ME-002-S05 | 我的发布 - 已驳回 视觉比对 | actual/ME-002-S05.png + diff/ME-002-S05.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S06 | ANDROID_SCREENSHOT | ME-002-S06 | 我的发布 - 已下架 视觉比对 | actual/ME-002-S06.png + diff/ME-002-S06.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S07 | ANDROID_SCREENSHOT | ME-002-S07 | 我的发布 - 无项目 视觉比对 | actual/ME-002-S07.png + diff/ME-002-S07.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S08 | ANDROID_SCREENSHOT | ME-002-S08 | 我的发布 - 加载更多 视觉比对 | actual/ME-002-S08.png + diff/ME-002-S08.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S09 | ANDROID_SCREENSHOT | ME-002-S09 | 我的发布 - 加载更多失败 视觉比对 | actual/ME-002-S09.png + diff/ME-002-S09.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S10 | ANDROID_SCREENSHOT | ME-002-S10 | 我的发布 - 离线缓存 视觉比对 | actual/ME-002-S10.png + diff/ME-002-S10.png + baseline SHA + diff ratio |
| T-VIS-ME-002-S11 | ANDROID_SCREENSHOT | ME-002-S11 | 我的发布 - 列表失败 视觉比对 | actual/ME-002-S11.png + diff/ME-002-S11.png + baseline SHA + diff ratio |
| T-VIS-ME-OV-002-S01 | ANDROID_SCREENSHOT | ME-OV-002-S01 | 本人项目操作面板 - 待审核项目操作 视觉比对 | actual/ME-OV-002-S01.png + diff/ME-OV-002-S01.png + baseline SHA + diff ratio |
| T-VIS-ME-OV-002-S02 | ANDROID_SCREENSHOT | ME-OV-002-S02 | 本人项目操作面板 - 已发布项目操作 视觉比对 | actual/ME-OV-002-S02.png + diff/ME-OV-002-S02.png + baseline SHA + diff ratio |
| T-VIS-ME-OV-002-S03 | ANDROID_SCREENSHOT | ME-OV-002-S03 | 本人项目操作面板 - 驳回项目操作 视觉比对 | actual/ME-OV-002-S03.png + diff/ME-OV-002-S03.png + baseline SHA + diff ratio |
| T-VIS-ME-OV-002-S04 | ANDROID_SCREENSHOT | ME-OV-002-S04 | 本人项目操作面板 - 下架项目操作 视觉比对 | actual/ME-OV-002-S04.png + diff/ME-OV-002-S04.png + baseline SHA + diff ratio |
| T-UI-INT-DETAIL-006 | ANDROID_UI | INT-DETAIL-006 | 交互：本人项目编辑 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DETAIL-007 | ANDROID_UI | INT-DETAIL-007 | 交互：本人项目下架 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DETAIL-008 | ANDROID_UI | INT-DETAIL-008 | 交互：本人驳回项目重提 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-001 | ANDROID_UI | INT-PUB-001 | 交互：输入项目标题 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-002 | ANDROID_UI | INT-PUB-002 | 交互：输入项目简介 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-003 | ANDROID_UI | INT-PUB-003 | 交互：选择项目分类 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-004 | ANDROID_UI | INT-PUB-004 | 交互：打开系统相册 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-005 | ANDROID_UI | INT-PUB-005 | 交互：上传已选图片 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-006 | ANDROID_UI | INT-PUB-006 | 交互：重试单张图片上传 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-007 | ANDROID_UI | INT-PUB-007 | 交互：删除已选图片 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-008 | ANDROID_UI | INT-PUB-008 | 交互：进入图片排序 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-009 | ANDROID_UI | INT-PUB-009 | 交互：选择联系方式类型 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-010 | ANDROID_UI | INT-PUB-010 | 交互：输入联系方式 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-011 | ANDROID_UI | INT-PUB-011 | 交互：输入联系人 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-012 | ANDROID_UI | INT-PUB-012 | 交互：同意发布规范 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-013 | ANDROID_UI | INT-PUB-013 | 交互：提交发布 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-PUB-014 | ANDROID_UI | INT-PUB-014 | 交互：发布页返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SORT-001 | ANDROID_UI | INT-SORT-001 | 交互：拖动图片排序 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SORT-002 | ANDROID_UI | INT-SORT-002 | 交互：设为封面 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SORT-003 | ANDROID_UI | INT-SORT-003 | 交互：完成图片排序 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-SORT-004 | ANDROID_UI | INT-SORT-004 | 交互：排序页返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RESULT-001 | ANDROID_UI | INT-RESULT-001 | 交互：查看已提交项目 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RESULT-002 | ANDROID_UI | INT-RESULT-002 | 交互：继续发布 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-RESULT-003 | ANDROID_UI | INT-RESULT-003 | 交互：进入我的发布 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-EDIT-001 | ANDROID_UI | INT-EDIT-001 | 交互：保存项目修改 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-EDIT-002 | ANDROID_UI | INT-EDIT-002 | 交互：编辑页返回 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DELIMG-001 | ANDROID_UI | INT-DELIMG-001 | 交互：确认删除图片 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DELIMG-002 | ANDROID_UI | INT-DELIMG-002 | 交互：取消删除图片 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DRAFT-001 | ANDROID_UI | INT-DRAFT-001 | 交互：继续编辑草稿 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-DRAFT-002 | ANDROID_UI | INT-DRAFT-002 | 交互：放弃草稿 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CTYPE-001 | ANDROID_UI | INT-CTYPE-001 | 交互：选择联系方式类型 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-CTYPE-002 | ANDROID_UI | INT-CTYPE-002 | 交互：关闭类型选择 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ME-002 | ANDROID_UI | INT-ME-002 | 交互：打开我的发布 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-MYPROJ-001 | ANDROID_UI | INT-MYPROJ-001 | 交互：切换项目状态标签 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-MYPROJ-002 | ANDROID_UI | INT-MYPROJ-002 | 交互：打开本人项目详情 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-MYPROJ-003 | ANDROID_UI | INT-MYPROJ-003 | 交互：打开项目操作面板 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-MYPROJ-004 | ANDROID_UI | INT-MYPROJ-004 | 交互：我的项目加载更多 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-MYPROJ-005 | ANDROID_UI | INT-MYPROJ-005 | 交互：我的项目重试 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-MYPROJ-006 | ANDROID_UI | INT-MYPROJ-006 | 交互：空列表去发布 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ACT-001 | ANDROID_UI | INT-ACT-001 | 交互：项目操作查看 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ACT-002 | ANDROID_UI | INT-ACT-002 | 交互：项目操作编辑 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ACT-003 | ANDROID_UI | INT-ACT-003 | 交互：项目操作下架 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ACT-004 | ANDROID_UI | INT-ACT-004 | 交互：项目操作重提 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ACT-005 | ANDROID_UI | INT-ACT-005 | 交互：项目操作删除 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-UI-INT-ACT-006 | ANDROID_UI | INT-ACT-006 | 交互：关闭项目操作 | JUnit XML + logcat 摘要 + 关键步骤截图 |
| T-API-uploadProjectImage | API_CONTRACT_INTEGRATION | uploadProjectImage | 接口合同与错误路径：POST /v1/uploads/project-images | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-deleteUploadedImage | API_CONTRACT_INTEGRATION | deleteUploadedImage | 接口合同与错误路径：DELETE /v1/uploads/project-images/{asset_id} | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-createProject | API_CONTRACT_INTEGRATION | createProject | 接口合同与错误路径：POST /v1/projects | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-updateProject | API_CONTRACT_INTEGRATION | updateProject | 接口合同与错误路径：PUT /v1/projects/{project_id} | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-listMyProjects | API_CONTRACT_INTEGRATION | listMyProjects | 接口合同与错误路径：GET /v1/me/projects | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-offlineProject | API_CONTRACT_INTEGRATION | offlineProject | 接口合同与错误路径：POST /v1/projects/{project_id}/offline | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-resubmitProject | API_CONTRACT_INTEGRATION | resubmitProject | 接口合同与错误路径：POST /v1/projects/{project_id}/resubmit | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-API-deleteProject | API_CONTRACT_INTEGRATION | deleteProject | 接口合同与错误路径：DELETE /v1/projects/{project_id} | 请求/响应样例、状态码、错误码、权限、限流、幂等和数据库断言 |
| T-DB-DB-PROJ-003 | DATABASE | DB-PROJ-003 | 数据表安装、迁移与索引：pre_stk_upload_asset | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-PROJ-004 | DATABASE | DB-PROJ-004 | 数据表安装、迁移与索引：pre_stk_project_image | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-DB-DB-PROJ-005 | DATABASE | DB-PROJ-005 | 数据表安装、迁移与索引：pre_stk_project_audit_log | DDL diff、迁移往返、索引存在性、唯一约束和回滚说明 |
| T-ADM-ADM-PROJ-008 | ADMIN_UI_API | ADM-PROJ-008 | 后台页面与权限：待审核项目 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-PROJ-009 | ADMIN_UI_API | ADM-PROJ-009 | 后台页面与权限：发布规则 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-PROJ-010 | ADMIN_UI_API | ADM-PROJ-010 | 后台页面与权限：图片上传设置 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-PROJ-011 | ADMIN_UI_API | ADM-PROJ-011 | 后台页面与权限：用户项目查询 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-ADM-ADM-PROJ-012 | ADMIN_UI_API | ADM-PROJ-012 | 后台页面与权限：项目审核与状态日志 | 页面截图、角色矩阵、查询/保存/审计结果 |
| T-REL-V1.2.0-BUILD | RELEASE | V1.2.0 | 同一 Commit Release APK 构建与来源校验 | APK + Build Info + CI Provenance + SHA-256 |
| T-REL-V1.2.0-EMULATOR | RELEASE | V1.2.0 | API 37 模拟器全量交互回归 | JUnit XML + 视频/截图 + logcat + 失败重现步骤 |
| T-REL-V1.2.0-VISUAL | RELEASE | V1.2.0 | 本版新增状态与历史核心状态视觉回归 | 视觉差异 HTML/JSON + actual/baseline/diff |
| T-REL-V1.2.0-UPGRADE | RELEASE | V1.2.0 | 覆盖安装与数据兼容 | 安装日志、版本号、登录态/缓存/数据库迁移结果 |
| T-REL-V1.2.0-DESKTOP | RELEASE | V1.2.0 | 桌面交付内容完整性 | 桌面目录清单、全部 SHA-256、来源 Commit |

## 项目所有者真机测试

1. 覆盖安装 1.2.0，确认底部发布现在进入真实发布页，旧阶段分支已移除。
2. 使用系统 Photo Picker 选择多图，测试压缩、进度、单图失败重试、删除、排序和设封面。
3. 提交合法项目，后台待审核出现；审核通过后首页和详情可见。
4. 后台驳回项目并填写原因；App 我的发布显示原因，可编辑并重新提交。
5. 测试本人项目编辑、下架、删除、状态标签和覆盖安装后草稿/缓存。
6. 尝试超图片数、超大小、每日上限、待审核上限和越权操作，确认服务端阻断。

## 失败提交内容

记录设备型号、Android 版本、App versionName/versionCode、操作步骤、期望/实际、截图/录屏、发生时间和 request_id。涉及外部 App 时同时记录目标 App 是否安装及版本。
