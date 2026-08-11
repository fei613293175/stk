package com.zzyihao.stk.ui.main

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Phone
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zzyihao.stk.data.project.ImageUploadProcessor
import com.zzyihao.stk.data.project.ProjectCategory
import com.zzyihao.stk.data.project.ProjectDraft
import com.zzyihao.stk.data.project.ProjectImageDraft
import com.zzyihao.stk.data.project.ProjectDraftImages
import com.zzyihao.stk.data.project.ProjectSubmissionRepository
import com.zzyihao.stk.data.project.PublishingConfig
import com.zzyihao.stk.ui.components.StkFeedbackBanner
import com.zzyihao.stk.ui.components.StkFeedbackTone
import com.zzyihao.stk.ui.components.StkFormLabel
import com.zzyihao.stk.ui.components.StkSquareImagePlaceholder
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkTextField
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.components.StkConfirmDialog
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PublishScreen(contentPadding: PaddingValues, repository: ProjectSubmissionRepository, categories: List<ProjectCategory>, publishingConfig: PublishingConfig = PublishingConfig(), initialProject: com.zzyihao.stk.data.project.MyProject? = null, onDirtyChanged: (Boolean) -> Unit = {}, onBack: () -> Unit, onEditUnavailable: () -> Unit = onBack, onDone: (com.zzyihao.stk.data.project.MyProject) -> Unit) {
    val vm: ProjectSubmissionViewModel = viewModel(factory = ProjectSubmissionViewModel.Factory(repository))
    val state by vm.submission.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var imageError by rememberSaveable { mutableStateOf<String?>(null) }
    var showCategories by rememberSaveable { mutableStateOf(false) }
    var showContactTypes by rememberSaveable { mutableStateOf(false) }
    var contactType by rememberSaveable { mutableStateOf("phone") }
    var rulesAccepted by rememberSaveable { mutableStateOf(false) }
    var selectedImageIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var removeImageIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var showImageSort by rememberSaveable { mutableStateOf(false) }
    var showValidation by rememberSaveable { mutableStateOf(false) }
    var preparingImages by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(initialProject?.id) { if (initialProject == null) vm.resetDraft() else vm.loadDraft(initialProject) }
    LaunchedEffect(initialProject?.id, state.draft.id, publishingConfig.contactTypes) {
        val allowed = publishingConfig.contactTypes.map { it.wire }.toSet()
        if (initialProject == null) {
            contactType = publishingConfig.contactTypes.firstOrNull()?.wire.orEmpty()
        } else if (state.draft.id == initialProject.id) {
            contactType = listOf("phone", "wechat", "qq", "url").firstOrNull { it in allowed && state.draft.contactValue(it).isNotBlank() }
                ?: publishingConfig.contactTypes.firstOrNull()?.wire.orEmpty()
        }
    }
    LaunchedEffect(initialProject?.id, state.draft.id) {
        if (initialProject != null && state.draft.id == initialProject.id) rulesAccepted = true
    }
    LaunchedEffect(state.dirty) { onDirtyChanged(state.dirty) }
    LaunchedEffect(state.editLoadErrorCode) {
        if (state.editLoadErrorCode in setOf(4030, 4040)) onEditUnavailable()
    }
    LaunchedEffect(selectedImageIndex, state.draft.images.size) {
        if (selectedImageIndex?.let { it !in state.draft.images.indices } == true) {
            selectedImageIndex = null
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(publishingConfig.maxImages.coerceAtLeast(2))) { uris ->
        if (uris.isNotEmpty()) {
            val remaining = (publishingConfig.maxImages - state.draft.images.size).coerceAtLeast(0)
            val selected = uris.take(remaining)
            if (selected.isEmpty()) {
                imageError = "最多选择 ${publishingConfig.maxImages} 张图片"
                return@rememberLauncherForActivityResult
            }
            preparingImages = true
            scope.launch { ImageUploadProcessor.prepare(
                context = context,
                uris = selected,
                maxImages = remaining,
                maxImageBytes = publishingConfig.maxImageBytes,
                longEdge = publishingConfig.imageLongEdge,
                jpegQuality = publishingConfig.jpegQuality,
                allowedMimeTypes = publishingConfig.allowedMimeTypes,
            ).onSuccess { prepared ->
                val newImages = prepared.mapIndexed { index, image ->
                    ProjectImageDraft(image.uri.toString(), image.mimeType, image.sizeBytes, image.displayName, state.draft.images.size + index, false)
                }
                vm.addImages(newImages)
                imageError = if (uris.size > remaining) "已按后台上限保留 $remaining 张图片" else null
            }.onFailure { imageError = it.message ?: "图片处理失败" }
                .also { preparingImages = false }
            }
        }
    }
    val titleError = if (showValidation && state.draft.title.length !in publishingConfig.titleMin..publishingConfig.titleMax) "请输入 ${publishingConfig.titleMin}-${publishingConfig.titleMax} 个字的项目标题" else null
    val summaryError = if (showValidation && state.draft.summary.length !in publishingConfig.summaryMin..publishingConfig.summaryMax) "项目简介至少填写 ${publishingConfig.summaryMin} 个字" else null
    val categoryError = if (showValidation && state.draft.categoryId.isBlank()) "请选择项目分类" else null
    val uploadError = imageError ?: state.draft.images.firstOrNull { it.uploadError != null }?.uploadError
    val imagesReady = state.draft.images.all { it.storageKey != null && it.uploadError == null }
    val imageValidationError = when {
        uploadError != null -> uploadError
        showValidation && state.draft.images.isEmpty() -> "请至少选择 1 张项目图片"
        showValidation && !imagesReady -> "请等待图片上传完成"
        else -> null
    }
    val hasContact = publishingConfig.contactTypes.any { state.draft.contactValue(it.wire).isNotBlank() }
    val contactTypesUnavailable = publishingConfig.contactTypes.isEmpty()
    val contactNameError = if (showValidation && state.draft.contactName.isBlank()) "请输入联系人姓名" else null
    val contactError = if (showValidation && !hasContact) "请填写至少一种已配置的联系方式" else null
    val rulesError = if (showValidation && !rulesAccepted) "请先确认发布规范" else null
    val draftValid = state.draft.title.length in publishingConfig.titleMin..publishingConfig.titleMax &&
        state.draft.summary.length in publishingConfig.summaryMin..publishingConfig.summaryMax &&
        state.draft.categoryId.isNotBlank() && state.draft.images.size in 1..publishingConfig.maxImages &&
        imagesReady && state.draft.contactName.isNotBlank() && hasContact && !contactTypesUnavailable && rulesAccepted
    if (showImageSort) {
        ImageSortScreen(
            contentPadding = contentPadding,
            images = state.draft.images,
            onMove = { index, direction -> vm.setDraft(state.draft.copy(images = ProjectDraftImages.move(state.draft.images, index, direction))) },
            onCover = { index -> vm.setDraft(state.draft.copy(images = ProjectDraftImages.setCover(state.draft.images, index))) },
            onDismiss = { showImageSort = false },
        )
        return
    }
    val awaitingInitialDraft = initialProject != null && state.draft.id != initialProject.id && state.editLoadError == null
    if (awaitingInitialDraft || state.loadingDraft) {
        EditProjectLoading(contentPadding, onBack)
        return
    }
    if (initialProject != null && state.editLoadError != null) {
        EditProjectLoadFailure(
            contentPadding = contentPadding,
            message = state.editLoadError ?: "项目加载失败",
            onBack = onEditUnavailable,
            onRetry = { vm.loadDraft(initialProject) },
            returnOnly = state.editLoadErrorCode in setOf(4030, 4040),
        )
        return
    }
    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        StkTopBar(
            title = if (state.draft.id == null) "发布项目" else "编辑项目",
            navigation = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
        )
        Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceSm).padding(bottom = StkDimens.PublishSubmitAreaHeight), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
            if (initialProject?.status == com.zzyihao.stk.data.project.MyProjectStatus.PUBLISHED && publishingConfig.editRequiresReview) {
                StkFeedbackBanner(
                    message = "保存修改后，项目将重新进入审核，审核通过前不会作为已发布项目展示。",
                    tone = StkFeedbackTone.Warning,
                )
            }
            StkFormLabel("项目标题", "${state.draft.title.length}/${publishingConfig.titleMax}")
            StkTextField(state.draft.title, { vm.setDraft(state.draft.copy(title = it.take(publishingConfig.titleMax))) }, "请输入项目标题", error = titleError)
            StkFormLabel("项目简介", "${state.draft.summary.length}/${publishingConfig.summaryMax}")
            StkTextField(state.draft.summary, { vm.setDraft(state.draft.copy(summary = it.take(publishingConfig.summaryMax))) }, "请介绍项目内容、合作方式等", error = summaryError, singleLine = false)
            StkFormLabel("项目分类")
            Box(Modifier.fillMaxWidth().height(StkDimens.PrimaryControlHeight).background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusControl)).border(StkDimens.Divider, if (categoryError == null) StkColors.Border else StkColors.Error, RoundedCornerShape(StkDimens.RadiusControl)).padding(horizontal = StkDimens.SpaceMd), contentAlignment = Alignment.CenterStart) {
                Text(categories.firstOrNull { it.id == state.draft.categoryId }?.name ?: "请选择项目分类", color = if (state.draft.categoryId.isBlank()) StkColors.TextTertiary else StkColors.TextPrimary)
                TextButton(onClick = { showCategories = true }, modifier = Modifier.align(Alignment.CenterEnd)) { Text("选择", color = StkColors.BrandPrimary) }
            }
            categoryError?.let { FieldError(it) }
            StkFormLabel("项目图片")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                item {
                    Box(
                        Modifier
                            .width(StkDimens.PublishImageWidth)
                            .height(StkDimens.PublishImageHeight)
                            .clickable(enabled = !state.submitting && state.draft.images.size < publishingConfig.maxImages) {
                                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                    ) {
                        StkSquareImagePlaceholder("添加图片", Modifier.fillMaxSize())
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = StkColors.BrandPrimary,
                            modifier = Modifier.align(Alignment.Center).padding(bottom = StkDimens.SpaceXl),
                        )
                    }
                }
                itemsIndexed(state.draft.images, key = { _, item -> item.localUri }) { index, image ->
                    Box(
                        Modifier
                            .width(StkDimens.PublishImageWidth)
                            .height(StkDimens.PublishImageHeight)
                            .background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusControl))
                            .border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl)),
                    ) {
                        AsyncImage(
                            model = Uri.parse(image.localUri),
                            contentDescription = image.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(StkDimens.RadiusControl)),
                        )
                        Text(
                            if (index == 0) "封面" else "图片 ${index + 1}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = StkColors.Surface,
                            modifier = Modifier.align(Alignment.BottomStart).background(StkColors.TextSecondary, RoundedCornerShape(StkDimens.RadiusTag)).padding(horizontal = StkDimens.SpaceSm),
                        )
                        TextButton(
                            enabled = state.removingImageUri != image.localUri,
                            onClick = { selectedImageIndex = index },
                            modifier = Modifier.align(Alignment.TopEnd),
                        ) { Text("操作") }
                        when {
                            state.removingImageUri == image.localUri -> Text("正在删除", color = StkColors.Surface, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.BottomEnd).background(StkColors.TextSecondary, RoundedCornerShape(StkDimens.RadiusTag)).padding(horizontal = StkDimens.SpaceSm))
                            image.uploadError != null -> TextButton(onClick = { vm.retryImage(image.localUri) }, modifier = Modifier.align(Alignment.BottomEnd)) { Text("重试上传", color = StkColors.Error) }
                            image.storageKey == null -> Text("上传 ${image.uploadProgress}%", color = StkColors.Surface, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.BottomEnd).background(StkColors.TextSecondary, RoundedCornerShape(StkDimens.RadiusTag)).padding(horizontal = StkDimens.SpaceSm))
                        }
                    }
                }
            }
            Text("已选择 ${state.draft.images.size}/${publishingConfig.maxImages} 张", style = MaterialTheme.typography.bodySmall, color = StkColors.TextTertiary)
            if (state.draft.images.size > 1) TextButton(onClick = { showImageSort = true }, modifier = Modifier.fillMaxWidth()) { Text("图片排序") }
            if (preparingImages) Text("正在压缩图片", color = StkColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            imageValidationError?.let { FieldError(it) }
            StkFormLabel("联系人")
            StkTextField(state.draft.contactName, { vm.setDraft(state.draft.copy(contactName = it.take(40))) }, "请输入联系人姓名", error = contactNameError)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                    StkFormLabel("联系方式类型")
                    Box(Modifier.fillMaxWidth().height(StkDimens.PrimaryControlHeight).background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusControl)).border(StkDimens.Divider, if (contactTypesUnavailable) StkColors.Error else StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl)).clickable(enabled = !contactTypesUnavailable) { showContactTypes = true }.padding(horizontal = StkDimens.SpaceMd), contentAlignment = Alignment.CenterStart) {
                        Text(publishingConfig.contactTypes.firstOrNull { it.wire == contactType }?.label ?: "暂无可用类型", color = StkColors.TextSecondary)
                        Text("选择", color = StkColors.BrandPrimary, modifier = Modifier.align(Alignment.CenterEnd))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                    StkFormLabel("联系方式")
                    val contactValue = state.draft.contactValue(contactType)
                    StkTextField(contactValue, { value ->
                        vm.setDraft(state.draft.withContact(contactType, value))
                    }, "请输入内容", error = contactError, enabled = !contactTypesUnavailable)
                }
            }
            if (contactTypesUnavailable) FieldError("管理员尚未配置可用联系方式，当前不能提交项目")
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = rulesAccepted, onCheckedChange = { rulesAccepted = it }); Text("我已确认发布内容真实并遵守平台规则", color = StkColors.TextSecondary) }
            rulesError?.let { FieldError(it) }
            state.error?.let { error ->
                val conflict = state.errorCode == 4091
                StkFeedbackBanner(
                    message = if (conflict) "项目已被其他操作更新。当前填写内容已保留，请重新加载最新版本后再保存。" else error,
                    tone = StkFeedbackTone.Error,
                )
            }
            state.message?.let { StkFeedbackBanner(it, StkFeedbackTone.Success) }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(StkColors.Surface).padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceMd)) {
            StkPrimaryButton(
                text = when {
                    state.draft.id == null -> "提交发布"
                    state.draft.resubmit -> "修改并重提"
                    initialProject?.status == com.zzyihao.stk.data.project.MyProjectStatus.PUBLISHED && !publishingConfig.editRequiresReview -> "保存修改"
                    else -> "保存并重新审核"
                },
                onClick = { showValidation = true; if (draftValid) vm.submit(onDone) },
                enabled = !state.submitting,
                loading = state.submitting,
                loadingText = "正在提交 ${state.progress}%",
            )
        }
        }
    }
    if (showCategories) AlertDialog(onDismissRequest = { showCategories = false }, title = { Text("选择分类") }, text = { Column { categories.filterNot { it.id == "all" }.forEach { category -> TextButton(onClick = { vm.setDraft(state.draft.copy(categoryId = category.id)); showCategories = false }, modifier = Modifier.fillMaxWidth()) { Text(category.name) } } } }, confirmButton = { TextButton(onClick = { showCategories = false }) { Text("关闭") } }, containerColor = StkColors.Surface)
    if (showContactTypes) ContactTypeSheet(
        types = publishingConfig.contactTypes,
        selected = contactType,
        onSelect = { contactType = it; showContactTypes = false },
        onDismiss = { showContactTypes = false },
    )
    selectedImageIndex?.let { index ->
        state.draft.images.getOrNull(index)?.let { image ->
            AlertDialog(
                onDismissRequest = { selectedImageIndex = null },
                title = { Text("图片操作（第 ${index + 1} 张）") },
                text = {
                    Column {
                        if (!image.isCover) {
                            TextButton(
                                onClick = {
                                    vm.setDraft(state.draft.copy(images = ProjectDraftImages.setCover(state.draft.images, index)))
                                    selectedImageIndex = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("设为封面") }
                        }
                        TextButton(
                            enabled = index > 0,
                            onClick = {
                                vm.setDraft(state.draft.copy(images = ProjectDraftImages.move(state.draft.images, index, -1)))
                                selectedImageIndex = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("向前移动") }
                        TextButton(
                            enabled = index < state.draft.images.lastIndex,
                            onClick = {
                                vm.setDraft(state.draft.copy(images = ProjectDraftImages.move(state.draft.images, index, 1)))
                                selectedImageIndex = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("向后移动") }
                        TextButton(
                            onClick = {
                                removeImageIndex = index
                                selectedImageIndex = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("移除图片", color = StkColors.Error) }
                    }
                },
                confirmButton = { TextButton(onClick = { selectedImageIndex = null }) { Text("关闭") } },
                containerColor = StkColors.Surface,
            )
        }
    }
    removeImageIndex?.let { index ->
        state.draft.images.getOrNull(index)?.let { image ->
            StkConfirmDialog(
                title = "删除这张图片？",
                message = "删除后，提交时会按当前图片顺序更新项目展示。",
                primaryLabel = "删除",
                secondaryLabel = "取消",
                icon = Icons.Outlined.DeleteOutline,
                onConfirm = { vm.removeImage(index); removeImageIndex = null },
                onDismiss = { removeImageIndex = null },
                preview = {
                    Box(
                        modifier = Modifier
                            .size(StkDimens.DialogPreviewSize)
                            .background(StkColors.Background, RoundedCornerShape(StkDimens.RadiusControl))
                            .border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl)),
                    ) {
                        AsyncImage(
                            model = Uri.parse(image.localUri),
                            contentDescription = "待删除图片",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(StkDimens.RadiusControl)),
                        )
                        Text(
                            if (index == 0) "封面" else "图片 ${index + 1}",
                            color = StkColors.Surface,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(StkColors.TextSecondary, RoundedCornerShape(StkDimens.RadiusTag))
                                .padding(horizontal = StkDimens.SpaceSm),
                        )
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactTypeSheet(
    types: List<com.zzyihao.stk.data.project.ContactTypeOption>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = StkColors.Surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = StkDimens.SpaceBase).padding(bottom = StkDimens.SpaceXl)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("选择联系方式类型", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = StkColors.TextPrimary)
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, contentDescription = "关闭", tint = StkColors.TextSecondary) }
            }
            if (types.isEmpty()) {
                Text("管理员尚未配置可用联系方式", color = StkColors.Error, modifier = Modifier.padding(vertical = StkDimens.SpaceLg))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                types.forEach { type ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(StkDimens.MeEntryHeight)
                            .background(StkColors.Background, RoundedCornerShape(StkDimens.RadiusControl))
                            .border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl))
                            .clickable { onSelect(type.wire) }
                            .padding(horizontal = StkDimens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(StkDimens.SmallButtonHeight)
                                .background(StkColors.BrandPrimarySoft, androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(contactTypeIcon(type.wire), contentDescription = null, tint = StkColors.BrandPrimary, modifier = Modifier.size(StkDimens.IconSmall))
                        }
                        androidx.compose.foundation.layout.Spacer(Modifier.width(StkDimens.SpaceMd))
                        Text(type.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = StkColors.TextPrimary)
                        if (type.wire == selected) {
                            Text(
                                "已选择",
                                color = StkColors.BrandPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .background(StkColors.BrandPrimarySoft, RoundedCornerShape(StkDimens.RadiusPill))
                                    .padding(horizontal = StkDimens.SpaceMd, vertical = StkDimens.SpaceXs),
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

private fun contactTypeIcon(type: String) = when (type) {
    "wechat" -> Icons.Outlined.ChatBubbleOutline
    "qq" -> Icons.Outlined.AlternateEmail
    "url" -> Icons.Outlined.Link
    else -> Icons.Outlined.Phone
}

@Composable
private fun EditProjectLoading(contentPadding: PaddingValues, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        StkTopBar(
            title = "编辑项目",
            navigation = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
        )
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            androidx.compose.material3.CircularProgressIndicator(color = StkColors.BrandPrimary)
            Text("正在加载项目内容", Modifier.padding(top = StkDimens.SpaceMd), style = MaterialTheme.typography.bodyMedium, color = StkColors.TextSecondary)
        }
    }
}

@Composable
private fun EditProjectLoadFailure(
    contentPadding: PaddingValues,
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    returnOnly: Boolean,
) {
    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        StkTopBar(
            title = "编辑项目",
            navigation = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
        )
        Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(if (returnOnly) "项目不可编辑" else "项目加载失败", style = MaterialTheme.typography.titleMedium, color = StkColors.TextPrimary)
            Text(message, Modifier.padding(top = StkDimens.SpaceSm), style = MaterialTheme.typography.bodyMedium, color = StkColors.TextSecondary)
            StkPrimaryButton(if (returnOnly) "返回我的发布" else "重新加载", if (returnOnly) onBack else onRetry, Modifier.padding(top = StkDimens.SpaceXl))
        }
    }
}

@Composable
private fun FieldError(message: String) {
    Row(
        Modifier.padding(top = StkDimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceXs),
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = StkColors.Error, modifier = Modifier.size(StkDimens.SpaceBase))
        Text(message, style = MaterialTheme.typography.labelMedium, color = StkColors.Error)
    }
}

@Composable
private fun ImageSortScreen(
    contentPadding: PaddingValues,
    images: List<ProjectImageDraft>,
    onMove: (Int, Int) -> Unit,
    onCover: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(StkDimens.RadiusSmall),
            color = StkColors.Background,
        ) {
            Column(Modifier.fillMaxSize().padding(contentPadding)) {
                StkTopBar(
                    title = "图片排序与封面",
                    navigation = { IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
                )
                Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().padding(horizontal = StkDimens.SpaceBase).padding(top = StkDimens.Space2Xl, bottom = StkDimens.PublishSubmitAreaHeight)) {
                    Text("长按拖动调整顺序，第一张为项目封面", style = MaterialTheme.typography.bodyLarge, color = StkColors.TextSecondary)
                    LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().padding(top = StkDimens.SpaceXl),
                    horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
                    verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
                ) {
                    items(images, key = { it.localUri }) { image ->
                        val index = images.indexOfFirst { it.localUri == image.localUri }
                        var dragOffsetX by rememberSaveable(image.localUri) { mutableStateOf(0f) }
                        var dragOffsetY by rememberSaveable(image.localUri) { mutableStateOf(0f) }
                        var isDragging by rememberSaveable(image.localUri) { mutableStateOf(false) }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(StkDimens.PublishImageHeight)
                                    .pointerInput(index, images.size) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                isDragging = true
                                                dragOffsetX = 0f
                                                dragOffsetY = 0f
                                            },
                                            onDragEnd = {
                                                val horizontal = abs(dragOffsetX) > abs(dragOffsetY)
                                                val distance = if (horizontal) dragOffsetX else dragOffsetY
                                                val threshold = if (horizontal) size.width * 0.5f else size.height * 0.5f
                                                if (abs(distance) >= threshold) {
                                                    val direction = if (horizontal) {
                                                        if (distance > 0) 1 else -1
                                                    } else {
                                                        if (distance > 0) 3 else -3
                                                    }
                                                    if (index + direction in images.indices) onMove(index, direction)
                                                }
                                                isDragging = false
                                                dragOffsetX = 0f
                                                dragOffsetY = 0f
                                            },
                                            onDragCancel = {
                                                isDragging = false
                                                dragOffsetX = 0f
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, amount ->
                                                change.consume()
                                                dragOffsetX += amount.x
                                                dragOffsetY += amount.y
                                            },
                                        )
                                    },
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(StkColors.BrandPrimarySoft, RoundedCornerShape(StkDimens.RadiusControl))
                                        .border(StkDimens.Divider, StkColors.BrandPrimary, RoundedCornerShape(StkDimens.RadiusControl)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isDragging) Text("拖动位置", style = MaterialTheme.typography.labelLarge, color = StkColors.BrandPrimary)
                                }
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .zIndex(if (isDragging) 1f else 0f)
                                        .graphicsLayer {
                                            translationX = dragOffsetX
                                            translationY = dragOffsetY
                                            scaleX = if (isDragging) 1.04f else 1f
                                            scaleY = if (isDragging) 1.04f else 1f
                                            shadowElevation = if (isDragging) 16f else 0f
                                            shape = RoundedCornerShape(StkDimens.RadiusControl)
                                            clip = true
                                        },
                                ) {
                                    AsyncImage(
                                        model = Uri.parse(image.localUri),
                                        contentDescription = "排序图片 ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                    Text(if (index == 0) "封面" else "${index + 1}", color = StkColors.Surface, modifier = Modifier.align(Alignment.BottomStart).background(StkColors.TextSecondary, RoundedCornerShape(StkDimens.RadiusTag)).padding(horizontal = StkDimens.SpaceSm))
                                    Surface(
                                        modifier = Modifier.align(Alignment.TopEnd).padding(StkDimens.SpaceXs).size(StkDimens.SmallButtonHeight),
                                        shape = CircleShape,
                                        color = StkColors.TextPrimary.copy(alpha = 0.82f),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.DragIndicator, contentDescription = "拖动排序", tint = StkColors.Surface, modifier = Modifier.size(StkDimens.IconSmall))
                                        }
                                    }
                                }
                            }
                            if (index != 0) TextButton(onClick = { onCover(index) }) { Text("设为封面") }
                        }
                    }
                }
                }
                Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(StkColors.Surface).padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceMd)) {
                    StkPrimaryButton("完成", onDismiss)
                }
                }
            }
        }
    }
}

private fun ProjectDraft.contactValue(type: String): String = when (type) {
    "wechat" -> wechat
    "qq" -> qq
    "url" -> website
    else -> phone
}

private fun ProjectDraft.withContact(type: String, value: String): ProjectDraft = when (type) {
    "wechat" -> copy(wechat = value)
    "qq" -> copy(qq = value)
    "url" -> copy(website = value)
    else -> copy(phone = value)
}
