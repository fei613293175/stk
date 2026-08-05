# 1.2.0 前端页面与状态范围

## 页面
| Page ID | 页面 | 类型 | 路由 | 布局 | 身份签名 |
|---|---|---|---|---|---|
| HOME-004 | 项目详情 | PAGE | stk://project/{project_id} | DETAIL | 项目图片+标题简介+发布者+联系方式+底部操作 |
| ME-001 | 我的 | PAGE | stk://me | ROOT_PROFILE | 资料卡+双账户+会员卡+道具+常用功能+底部导航 |
| ME-002 | 我的发布 | PAGE | stk://me/projects | LIST | 状态标签页+本人项目列表+项目操作 |
| ME-OV-002 | 本人项目操作面板 | OVERLAY | overlay://my-project-actions | OVERLAY | 查看/编辑/下架/重提/删除按状态显示 |
| PUB-001 | 发布项目 | PAGE | stk://publish | FORM | 标题+简介+分类+多图+联系方式+协议+提交 |
| PUB-002 | 图片排序与封面 | PAGE | stk://publish/images | FORM | 可拖动图片网格+封面标识+完成 |
| PUB-003 | 发布提交结果 | PAGE | stk://publish/result | RESULT | 结果插图+审核状态+查看/继续发布 |
| PUB-004 | 编辑项目 | PAGE | stk://project/{project_id}/edit | FORM | 已有内容表单+审核提示+保存 |
| PUB-OV-001 | 删除图片确认 | OVERLAY | overlay://delete-image | OVERLAY | 缩略图+删除说明+取消/删除 |
| PUB-OV-002 | 未保存退出确认 | OVERLAY | overlay://discard-draft | OVERLAY | 未保存说明+继续编辑/放弃 |
| PUB-OV-003 | 联系方式类型选择 | OVERLAY | overlay://contact-type | OVERLAY | 手机/微信/QQ/网址等后台允许类型 |

## 本版新增/增强状态
| State ID | Page ID | 状态 | 说明 | 效果图 |
|---|---|---|---|---|
| HOME-004-S03 | HOME-004 | 本人待审核项目 | 显示待审核状态和可编辑动作 | ui/mockups/approved/V1.2.0/HOME-004/M-HOME-004-S03__OWNER_PENDING__1170x2532.png |
| HOME-004-S04 | HOME-004 | 本人被驳回项目 | 显示原因和重提 | ui/mockups/approved/V1.2.0/HOME-004/M-HOME-004-S04__OWNER_REJECTED__1170x2532.png |
| HOME-004-S05 | HOME-004 | 本人已下架项目 | 显示重新上架/编辑策略 | ui/mockups/approved/V1.2.0/HOME-004/M-HOME-004-S05__OWNER_OFFLINE__1170x2532.png |
| PUB-001-S01 | PUB-001 | 空表单 | 默认发布表单 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S01__DEFAULT__1170x2532.png |
| PUB-001-S02 | PUB-001 | 已输入草稿 | 表单内容保留 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S02__DRAFT__1170x2532.png |
| PUB-001-S03 | PUB-001 | 选择图片 | 系统 Photo Picker | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S03__IMAGE_PICKING__1170x2532.png |
| PUB-001-S04 | PUB-001 | 图片压缩 | 逐张本地处理 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S04__COMPRESSING__1170x2532.png |
| PUB-001-S05 | PUB-001 | 图片上传 | 逐张进度 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S05__UPLOADING__1170x2532.png |
| PUB-001-S06 | PUB-001 | 部分上传失败 | 逐张重试/删除 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S06__UPLOAD_FAILED__1170x2532.png |
| PUB-001-S07 | PUB-001 | 发布校验错误 | 字段、图片或协议 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S07__VALIDATION_ERROR__1170x2532.png |
| PUB-001-S08 | PUB-001 | 提交中 | 防重复和幂等键 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S08__SUBMITTING__1170x2532.png |
| PUB-001-S09 | PUB-001 | 提交成功 | 进入结果页 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S09__SUBMIT_SUCCESS__1170x2532.png |
| PUB-001-S10 | PUB-001 | 提交网络失败 | 保留表单和已上传资产 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S10__NETWORK_ERROR__1170x2532.png |
| PUB-001-S11 | PUB-001 | 达到每日上限 | 展示后台配置上限 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S11__DAILY_LIMIT__1170x2532.png |
| PUB-001-S12 | PUB-001 | 待审核数量上限 | 引导管理已有项目 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S12__PENDING_LIMIT__1170x2532.png |
| PUB-001-S13 | PUB-001 | 登录过期 | 安全保存非敏感草稿并重新登录 | ui/mockups/approved/V1.2.0/PUB-001/M-PUB-001-S13__SESSION_EXPIRED__1170x2532.png |
| PUB-002-S01 | PUB-002 | 图片排序 | 网格和封面标识 | ui/mockups/approved/V1.2.0/PUB-002/M-PUB-002-S01__CONTENT__1170x2532.png |
| PUB-002-S02 | PUB-002 | 拖动排序 | 拖动反馈和占位 | ui/mockups/approved/V1.2.0/PUB-002/M-PUB-002-S02__DRAGGING__1170x2532.png |
| PUB-002-S03 | PUB-002 | 已更换封面 | 新封面高亮 | ui/mockups/approved/V1.2.0/PUB-002/M-PUB-002-S03__COVER_CHANGED__1170x2532.png |
| PUB-002-S04 | PUB-002 | 排序保存失败 | 保留本地顺序并重试 | ui/mockups/approved/V1.2.0/PUB-002/M-PUB-002-S04__SAVE_FAILED__1170x2532.png |
| PUB-003-S01 | PUB-003 | 已提交待审核 | 进入我的发布 | ui/mockups/approved/V1.2.0/PUB-003/M-PUB-003-S01__PENDING__1170x2532.png |
| PUB-003-S02 | PUB-003 | 直接发布成功 | 查看详情 | ui/mockups/approved/V1.2.0/PUB-003/M-PUB-003-S02__PUBLISHED__1170x2532.png |
| PUB-003-S03 | PUB-003 | 服务端未接受 | 返回编辑并保留数据 | ui/mockups/approved/V1.2.0/PUB-003/M-PUB-003-S03__FAILED__1170x2532.png |
| PUB-004-S01 | PUB-004 | 编辑加载 | 读取本人项目 | ui/mockups/approved/V1.2.0/PUB-004/M-PUB-004-S01__LOADING__1170x2532.png |
| PUB-004-S02 | PUB-004 | 编辑内容 | 已填充表单 | ui/mockups/approved/V1.2.0/PUB-004/M-PUB-004-S02__CONTENT__1170x2532.png |
| PUB-004-S03 | PUB-004 | 重新审核提示 | 保存前明确说明 | ui/mockups/approved/V1.2.0/PUB-004/M-PUB-004-S03__REVIEW_WARNING__1170x2532.png |
| PUB-004-S04 | PUB-004 | 保存中 | 幂等提交 | ui/mockups/approved/V1.2.0/PUB-004/M-PUB-004-S04__SAVING__1170x2532.png |
| PUB-004-S05 | PUB-004 | 保存成功 | 进入真实状态详情 | ui/mockups/approved/V1.2.0/PUB-004/M-PUB-004-S05__SUCCESS__1170x2532.png |
| PUB-004-S06 | PUB-004 | 版本冲突 | 提示刷新后重试 | ui/mockups/approved/V1.2.0/PUB-004/M-PUB-004-S06__CONFLICT__1170x2532.png |
| PUB-004-S07 | PUB-004 | 无权或项目不存在 | 返回我的发布 | ui/mockups/approved/V1.2.0/PUB-004/M-PUB-004-S07__UNAVAILABLE__1170x2532.png |
| PUB-004-S08 | PUB-004 | 保存失败 | 保留表单 | ui/mockups/approved/V1.2.0/PUB-004/M-PUB-004-S08__NETWORK_ERROR__1170x2532.png |
| PUB-OV-001-S01 | PUB-OV-001 | 确认删除图片 | 取消或删除 | ui/mockups/approved/V1.2.0/PUB-OV-001/M-PUB-OV-001-S01__DEFAULT__1170x2532.png |
| PUB-OV-001-S02 | PUB-OV-001 | 删除中 | 按钮锁定 | ui/mockups/approved/V1.2.0/PUB-OV-001/M-PUB-OV-001-S02__DELETING__1170x2532.png |
| PUB-OV-001-S03 | PUB-OV-001 | 删除失败 | 允许重试 | ui/mockups/approved/V1.2.0/PUB-OV-001/M-PUB-OV-001-S03__FAILED__1170x2532.png |
| PUB-OV-002-S01 | PUB-OV-002 | 放弃未保存内容 | 继续编辑或放弃 | ui/mockups/approved/V1.2.0/PUB-OV-002/M-PUB-OV-002-S01__DEFAULT__1170x2532.png |
| PUB-OV-003-S01 | PUB-OV-003 | 选择联系方式 | 后台允许类型 | ui/mockups/approved/V1.2.0/PUB-OV-003/M-PUB-OV-003-S01__DEFAULT__1170x2532.png |
| PUB-OV-003-S02 | PUB-OV-003 | 已选类型 | 显示对应输入规则 | ui/mockups/approved/V1.2.0/PUB-OV-003/M-PUB-OV-003-S02__TYPE_SELECTED__1170x2532.png |
| PUB-OV-003-S03 | PUB-OV-003 | 无可用类型 | 阻断发布并提示管理员配置 | ui/mockups/approved/V1.2.0/PUB-OV-003/M-PUB-OV-003-S03__EMPTY__1170x2532.png |
| ME-002-S01 | ME-002 | 项目列表加载 | 状态标签页骨架 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S01__LOADING__1170x2532.png |
| ME-002-S02 | ME-002 | 全部项目 | 本人所有非删除项目 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S02__ALL__1170x2532.png |
| ME-002-S03 | ME-002 | 待审核 | 可查看/编辑/撤回策略 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S03__PENDING__1170x2532.png |
| ME-002-S04 | ME-002 | 已发布 | 可查看/编辑/下架 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S04__PUBLISHED__1170x2532.png |
| ME-002-S05 | ME-002 | 已驳回 | 原因和重提 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S05__REJECTED__1170x2532.png |
| ME-002-S06 | ME-002 | 已下架 | 查看/编辑/删除 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S06__OFFLINE__1170x2532.png |
| ME-002-S07 | ME-002 | 无项目 | 引导发布 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S07__EMPTY__1170x2532.png |
| ME-002-S08 | ME-002 | 加载更多 | 底部进度 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S08__PAGINATING__1170x2532.png |
| ME-002-S09 | ME-002 | 加载更多失败 | 局部重试 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S09__PAGINATION_FAILED__1170x2532.png |
| ME-002-S10 | ME-002 | 离线缓存 | 操作类按钮禁用并说明 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S10__OFFLINE_CACHED__1170x2532.png |
| ME-002-S11 | ME-002 | 列表失败 | 重试 | ui/mockups/approved/V1.2.0/ME-002/M-ME-002-S11__ERROR__1170x2532.png |
| ME-OV-002-S01 | ME-OV-002 | 待审核项目操作 | 查看/编辑/撤回策略 | ui/mockups/approved/V1.2.0/ME-OV-002/M-ME-OV-002-S01__PENDING__1170x2532.png |
| ME-OV-002-S02 | ME-OV-002 | 已发布项目操作 | 查看/编辑/下架 | ui/mockups/approved/V1.2.0/ME-OV-002/M-ME-OV-002-S02__PUBLISHED__1170x2532.png |
| ME-OV-002-S03 | ME-OV-002 | 驳回项目操作 | 查看原因/编辑/重提/删除 | ui/mockups/approved/V1.2.0/ME-OV-002/M-ME-OV-002-S03__REJECTED__1170x2532.png |
| ME-OV-002-S04 | ME-OV-002 | 下架项目操作 | 查看/编辑/删除 | ui/mockups/approved/V1.2.0/ME-OV-002/M-ME-OV-002-S04__OFFLINE__1170x2532.png |

## 本版新增交互
| Interaction ID | Page ID | 动作 | testTag | 目标/API | 预期 |
|---|---|---|---|---|---|
| INT-DETAIL-006 | HOME-004 | 本人项目编辑 | owner_project_edit | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-DETAIL-007 | HOME-004 | 本人项目下架 | owner_project_offline | offlineProject | 产生合同规定的唯一结果 |
| INT-DETAIL-008 | HOME-004 | 本人驳回项目重提 | owner_project_resubmit | resubmitProject | 产生合同规定的唯一结果 |
| INT-PUB-001 | PUB-001 | 输入项目标题 | publish_title | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-PUB-002 | PUB-001 | 输入项目简介 | publish_summary | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-PUB-003 | PUB-001 | 选择项目分类 | publish_category | listProjectCategories | 产生合同规定的唯一结果 |
| INT-PUB-004 | PUB-001 | 打开系统相册 | publish_add_images | LOCAL_PHOTO_PICKER | 产生合同规定的唯一结果 |
| INT-PUB-005 | PUB-001 | 上传已选图片 | publish_image_upload | uploadProjectImage | 产生合同规定的唯一结果 |
| INT-PUB-006 | PUB-001 | 重试单张图片上传 | publish_image_retry_{assetId} | uploadProjectImage | 产生合同规定的唯一结果 |
| INT-PUB-007 | PUB-001 | 删除已选图片 | publish_image_delete_{assetId} | LOCAL_OVERLAY | 产生合同规定的唯一结果 |
| INT-PUB-008 | PUB-001 | 进入图片排序 | publish_image_sort | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-PUB-009 | PUB-001 | 选择联系方式类型 | publish_contact_type | LOCAL_OVERLAY | 产生合同规定的唯一结果 |
| INT-PUB-010 | PUB-001 | 输入联系方式 | publish_contact_value | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-PUB-011 | PUB-001 | 输入联系人 | publish_contact_name | LOCAL_VALIDATION | 产生合同规定的唯一结果 |
| INT-PUB-012 | PUB-001 | 同意发布规范 | publish_agreement | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-PUB-013 | PUB-001 | 提交发布 | publish_submit | createProject | 产生合同规定的唯一结果 |
| INT-PUB-014 | PUB-001 | 发布页返回 | publish_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-SORT-001 | PUB-002 | 拖动图片排序 | image_sort_grid | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-SORT-002 | PUB-002 | 设为封面 | image_set_cover_{assetId} | LOCAL_UI_STATE | 产生合同规定的唯一结果 |
| INT-SORT-003 | PUB-002 | 完成图片排序 | image_sort_done | LOCAL_BACK_WITH_RESULT | 产生合同规定的唯一结果 |
| INT-SORT-004 | PUB-002 | 排序页返回 | image_sort_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-RESULT-001 | PUB-003 | 查看已提交项目 | publish_result_view | getProjectDetail | 产生合同规定的唯一结果 |
| INT-RESULT-002 | PUB-003 | 继续发布 | publish_result_again | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-RESULT-003 | PUB-003 | 进入我的发布 | publish_result_my_projects | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-EDIT-001 | PUB-004 | 保存项目修改 | project_edit_save | updateProject | 产生合同规定的唯一结果 |
| INT-EDIT-002 | PUB-004 | 编辑页返回 | project_edit_back | LOCAL_BACK | 产生合同规定的唯一结果 |
| INT-DELIMG-001 | PUB-OV-001 | 确认删除图片 | delete_image_confirm | deleteUploadedImage | 产生合同规定的唯一结果 |
| INT-DELIMG-002 | PUB-OV-001 | 取消删除图片 | delete_image_cancel | LOCAL_DISMISS | 产生合同规定的唯一结果 |
| INT-DRAFT-001 | PUB-OV-002 | 继续编辑草稿 | draft_continue | LOCAL_DISMISS | 产生合同规定的唯一结果 |
| INT-DRAFT-002 | PUB-OV-002 | 放弃草稿 | draft_discard | LOCAL_DISCARD | 产生合同规定的唯一结果 |
| INT-CTYPE-001 | PUB-OV-003 | 选择联系方式类型 | contact_type_{type} | LOCAL_BACK_WITH_RESULT | 产生合同规定的唯一结果 |
| INT-CTYPE-002 | PUB-OV-003 | 关闭类型选择 | contact_type_close | LOCAL_DISMISS | 产生合同规定的唯一结果 |
| INT-ME-002 | ME-001 | 打开我的发布 | me_projects | listMyProjects | 产生合同规定的唯一结果 |
| INT-MYPROJ-001 | ME-002 | 切换项目状态标签 | my_projects_tab_{status} | listMyProjects | 产生合同规定的唯一结果 |
| INT-MYPROJ-002 | ME-002 | 打开本人项目详情 | my_project_{projectId} | getProjectDetail | 产生合同规定的唯一结果 |
| INT-MYPROJ-003 | ME-002 | 打开项目操作面板 | my_project_more_{projectId} | LOCAL_OVERLAY | 产生合同规定的唯一结果 |
| INT-MYPROJ-004 | ME-002 | 我的项目加载更多 | my_projects_list | listMyProjects | 产生合同规定的唯一结果 |
| INT-MYPROJ-005 | ME-002 | 我的项目重试 | my_projects_retry | listMyProjects | 产生合同规定的唯一结果 |
| INT-MYPROJ-006 | ME-002 | 空列表去发布 | my_projects_publish | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-ACT-001 | ME-OV-002 | 项目操作查看 | project_action_view | getProjectDetail | 产生合同规定的唯一结果 |
| INT-ACT-002 | ME-OV-002 | 项目操作编辑 | project_action_edit | LOCAL_NAVIGATION | 产生合同规定的唯一结果 |
| INT-ACT-003 | ME-OV-002 | 项目操作下架 | project_action_offline | offlineProject | 产生合同规定的唯一结果 |
| INT-ACT-004 | ME-OV-002 | 项目操作重提 | project_action_resubmit | resubmitProject | 产生合同规定的唯一结果 |
| INT-ACT-005 | ME-OV-002 | 项目操作删除 | project_action_delete | deleteProject | 产生合同规定的唯一结果 |
| INT-ACT-006 | ME-OV-002 | 关闭项目操作 | project_action_close | LOCAL_DISMISS | 产生合同规定的唯一结果 |

所有页面必须同时遵循同目录 `UI_FIXED_RULES.md`。没有列入本表的控件不能新增业务行为。
