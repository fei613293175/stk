package com.zzyihao.stk.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.zzyihao.stk.data.project.MyProject
import com.zzyihao.stk.data.project.ProjectContact
import com.zzyihao.stk.data.project.ProjectDetail
import com.zzyihao.stk.data.project.ProjectPaging
import com.zzyihao.stk.data.project.ProjectRepository
import com.zzyihao.stk.data.project.ProjectSubmissionRepository
import com.zzyihao.stk.data.project.publisherMeta
import com.zzyihao.stk.ui.components.StkInfoCard
import com.zzyihao.stk.ui.components.StkFeedbackBanner
import com.zzyihao.stk.ui.components.StkFeedbackTone
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkStatusDialog
import com.zzyihao.stk.ui.components.StkStatusKind
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens
import kotlinx.coroutines.launch

@Composable
fun ProjectDetailScreen(
    projectId: String,
    repository: ProjectRepository,
    submissionRepository: ProjectSubmissionRepository,
    contentPadding: PaddingValues,
    currentUid: Long,
    onBack: () -> Unit,
    onEditOwnProject: (MyProject) -> Unit,
) {
    val vm: ProjectViewModel = viewModel(key = "project-detail-$projectId", factory = ProjectViewModel.Factory(repository, loadInitialList = false))
    val state by vm.detail.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var actionError by rememberSaveable { mutableStateOf<String?>(null) }
    var actionLoading by rememberSaveable { mutableStateOf(false) }
    var confirmUnpublish by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var showOwnerActions by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(projectId) { vm.openDetail(projectId) }

    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        StkTopBar(
            title = "项目详情",
            navigation = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
        )
        when {
            state.loading -> ProjectDetailSkeleton()
            state.failure != null -> ProjectDetailFailure(state.failure!!, onRetry = { vm.retryDetail(projectId) }, onBack = onBack)
            state.detail != null -> DetailContent(
                detail = state.detail!!,
                isOwnProject = state.detail!!.publisherUid == currentUid,
                ownerActions = state.detail!!.ownerActions,
                actionLoading = actionLoading,
                actionError = actionError,
                onEditOwnProject = {
                    if (!actionLoading) scope.launch {
                        actionLoading = true
                        actionError = null
                        runCatching { submissionRepository.getMyProject(projectId) }
                            .onSuccess(onEditOwnProject)
                            .onFailure { actionError = it.message ?: "项目加载失败" }
                        actionLoading = false
                    }
                },
                onUnpublishOwnProject = { confirmUnpublish = true },
                onOpenOwnerActions = { showOwnerActions = true },
            )
        }
    }
    if (confirmUnpublish) {
        ActionDialog(
            title = "下架项目",
            message = "下架后前台将不再展示，确定继续吗？",
            primaryLabel = "确认下架",
            secondaryLabel = "取消",
            onPrimary = {
                confirmUnpublish = false
                scope.launch {
                    actionLoading = true
                    actionError = null
                    runCatching { submissionRepository.unpublishProject(projectId) }
                        .onSuccess { vm.retryDetail(projectId) }
                        .onFailure { actionError = it.message ?: "下架失败" }
                    actionLoading = false
                }
            },
            onSecondary = { confirmUnpublish = false },
            onDismiss = { confirmUnpublish = false },
        )
    }
    if (confirmDelete) {
        val withdrawingPending = state.detail?.status == "pending"
        ActionDialog(
            title = if (withdrawingPending) "撤回审核" else "删除项目",
            message = if (withdrawingPending) "撤回后项目不再进入审核流程，后台仍会保留操作记录。确定继续吗？" else "删除后项目不再对外展示，后台仍会保留审核记录。确定继续吗？",
            primaryLabel = if (withdrawingPending) "确认撤回" else "确认删除",
            secondaryLabel = "取消",
            onPrimary = {
                confirmDelete = false
                scope.launch {
                    actionLoading = true
                    actionError = null
                    runCatching { submissionRepository.softDeleteProject(projectId) }
                        .onSuccess { onBack() }
                        .onFailure { actionError = it.message ?: "删除失败" }
                    actionLoading = false
                }
            },
            onSecondary = { confirmDelete = false },
            onDismiss = { confirmDelete = false },
        )
    }
    state.detail?.takeIf { it.publisherUid == currentUid }?.let { detail ->
        if (showOwnerActions) {
            ProjectActionSheet(
                project = detail.asMyProject(),
                enabled = !actionLoading,
                onDismiss = { showOwnerActions = false },
                onView = { showOwnerActions = false },
                onEdit = {
                    showOwnerActions = false
                    if (!actionLoading) scope.launch {
                        actionLoading = true
                        actionError = null
                        runCatching { submissionRepository.getMyProject(projectId) }
                            .onSuccess(onEditOwnProject)
                            .onFailure { actionError = it.message ?: "项目加载失败" }
                        actionLoading = false
                    }
                },
                onOffline = { showOwnerActions = false; confirmUnpublish = true },
                onDelete = { showOwnerActions = false; confirmDelete = true },
            )
        }
    }
}

@Composable
private fun ProjectDetailSkeleton() {
    Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceBase)) {
        SkeletonBlock(Modifier.fillMaxWidth().height(StkDimens.ProjectCoverHeight))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SkeletonBlock(Modifier.fillMaxWidth(0.66f).height(StkDimens.SpaceLg))
            SkeletonBlock(Modifier.size(StkDimens.AvatarList, StkDimens.SpaceLg))
        }
        SkeletonBlock(Modifier.fillMaxWidth().height(StkDimens.MeEntryHeight))
        SkeletonBlock(Modifier.fillMaxWidth().height(StkDimens.ProjectImageHeight))
        SkeletonBlock(Modifier.fillMaxWidth().height(StkDimens.MeEntryHeight))
    }
}

@Composable
private fun ProjectDetailFailure(failure: ProjectFailure, onRetry: () -> Unit, onBack: () -> Unit) {
    val (title, message) = when (failure.kind) {
        ProjectFailureKind.NotFound -> "项目不存在" to "项目可能已被删除或链接无效。"
        ProjectFailureKind.Forbidden -> "项目暂不可查看" to "该项目尚未发布或已下架。"
        ProjectFailureKind.Timeout -> "详情加载失败" to "请求超时，请稍后重新加载项目详情。"
        ProjectFailureKind.Service -> "服务暂时异常" to "项目详情未能加载，请使用请求编号排查。"
        ProjectFailureKind.Offline -> "当前没有网络" to "连接网络后可重新加载项目详情。"
        else -> "详情加载失败" to "请检查网络后重新加载项目详情。"
    }
    val returnOnly = failure.kind in setOf(ProjectFailureKind.NotFound, ProjectFailureKind.Forbidden)
    ProjectFullPageState(
        title = title,
        message = message,
        buttonLabel = if (returnOnly) "返回首页" else "重新加载",
        onAction = if (returnOnly) onBack else onRetry,
        kind = failure.kind,
        requestId = failure.requestId,
    )
}

@Composable
private fun DetailContent(
    detail: ProjectDetail,
    isOwnProject: Boolean,
    ownerActions: Set<String>,
    actionLoading: Boolean,
    actionError: String?,
    onEditOwnProject: () -> Unit,
    onUnpublishOwnProject: () -> Unit,
    onOpenOwnerActions: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val gallery = remember(detail) {
        (listOf(detail.summary.coverUrl) + detail.images.map { it.url }).filter(String::isNotBlank).distinct()
    }
    var imageIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var showContactList by rememberSaveable { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<ProjectContact?>(null) }
    var pendingUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var blockedUrl by rememberSaveable { mutableStateOf(false) }
    var invalidTarget by rememberSaveable { mutableStateOf(false) }
    var missingContact by remember { mutableStateOf<ProjectContact?>(null) }
    var contactFeedback by rememberSaveable { mutableStateOf<String?>(null) }
    var shareMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val canEdit = isOwnProject && (ownerActions.contains("edit") || ownerActions.contains("resubmit"))
    val isOwnerMode = isOwnProject && ownerActions.isNotEmpty()
    val statusColors = projectStatusColors(detail.status)

    fun selectContact(contact: ProjectContact) {
        contactFeedback = null
        if (contact.type == "url" && isHttpsUrl(contact.value) && !ProjectPaging.isAllowedExternalUrl(contact.value, detail.allowedExternalHosts)) {
            blockedUrl = true
        } else if (!ProjectPaging.isValidContactTarget(contact, detail.allowedExternalHosts)) {
            invalidTarget = true
        } else selectedContact = contact
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().padding(bottom = StkDimens.PrimaryControlHeight + StkDimens.Space2Xl),
            contentPadding = PaddingValues(StkDimens.SpaceBase),
            verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceBase),
        ) {
            if (detail.fromCache) item { ProjectOfflineBanner("当前离线，显示已缓存详情；外部联系动作已暂停。") }
            item {
                DetailHero(
                    url = detail.summary.coverUrl,
                    title = detail.summary.title,
                    onClick = { if (gallery.isNotEmpty()) imageIndex = 0 },
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(detail.summary.title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = StkColors.TextPrimary)
                    Text(
                        projectStatusLabel(detail.status),
                        Modifier.background(statusColors.first, RoundedCornerShape(StkDimens.RadiusTag)).padding(horizontal = StkDimens.SpaceSm, vertical = StkDimens.SpaceXs),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColors.second,
                    )
                }
            }
            if (isOwnProject && detail.status == "rejected" && detail.rejectionReason.isNotBlank()) {
                item {
                    StkFeedbackBanner(
                        message = "驳回原因：${detail.rejectionReason}",
                        tone = StkFeedbackTone.Error,
                    )
                }
            }
            item {
                StkInfoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(StkDimens.AvatarList).background(StkColors.BrandPrimarySoft, CircleShape), contentAlignment = Alignment.Center) {
                            Text(detail.summary.publisherName.takeLast(1), color = StkColors.BrandPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                        Column(Modifier.padding(start = StkDimens.SpaceMd)) {
                            Text(detail.summary.publisherName, style = MaterialTheme.typography.bodyMedium, color = StkColors.TextPrimary)
                            Text(detail.summary.publisherMeta(detail.publisherUid), style = MaterialTheme.typography.labelMedium, color = StkColors.TextTertiary)
                        }
                    }
                }
            }
            item {
                Text("项目简介", style = MaterialTheme.typography.titleMedium, color = StkColors.TextPrimary)
                Text(detail.summary.summary, Modifier.padding(top = StkDimens.SpaceSm), style = MaterialTheme.typography.bodyLarge, color = StkColors.TextSecondary)
            }
            val contentImages = detail.images.filter { it.url.isNotBlank() && it.url != detail.summary.coverUrl }
            if (contentImages.isNotEmpty()) {
                item { Text("项目图片", style = MaterialTheme.typography.titleMedium, color = StkColors.TextPrimary) }
                items(contentImages) { image ->
                    DetailHero(image.url, image.alt) {
                        imageIndex = gallery.indexOf(image.url).takeIf { it >= 0 }
                    }
                }
            }
            detail.contacts.firstOrNull()?.let { contact ->
                item {
                    StkInfoCard(Modifier.clickable(enabled = !detail.fromCache) { if (detail.contacts.size == 1) selectContact(contact) else showContactList = true }) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Phone, null, tint = StkColors.BrandPrimary, modifier = Modifier.size(StkDimens.IconSmall))
                            Column(Modifier.weight(1f).padding(start = StkDimens.SpaceMd)) {
                                Text("联系方式", style = MaterialTheme.typography.labelMedium, color = StkColors.TextTertiary)
                                Text("${contact.label} ${maskedContactValue(contact)}", style = MaterialTheme.typography.bodyMedium, color = StkColors.TextPrimary)
                            }
                            Text("›", color = StkColors.TextTertiary)
                        }
                    }
                }
            }
            item {
                TextButton(onClick = {
                    val url = "https://stk.zz-yihao.com/project/${detail.summary.id}"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${detail.summary.title}\n$url")
                    }
                    runCatching { context.startActivity(Intent.createChooser(intent, "分享项目")) }
                        .onFailure { clipboard.setText(AnnotatedString(url)); shareMessage = "未找到分享应用，链接已复制" }
                }, modifier = Modifier.fillMaxWidth()) { Text("分享项目") }
                actionError?.let { Text(it, color = StkColors.Error, style = MaterialTheme.typography.bodySmall) }
                shareMessage?.let { Text(it, color = StkColors.Success, style = MaterialTheme.typography.bodySmall) }
            }
        }
        if (isOwnerMode) {
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(StkColors.Surface).padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceSm),
                horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
            ) {
                Button(
                    enabled = canEdit && !actionLoading,
                    onClick = onEditOwnProject,
                    modifier = Modifier.weight(1f).height(StkDimens.PrimaryControlHeight).border(StkDimens.Divider, StkColors.BrandPrimary, RoundedCornerShape(StkDimens.RadiusControl)),
                    colors = ButtonDefaults.buttonColors(containerColor = StkColors.Surface, contentColor = StkColors.BrandPrimary),
                    shape = RoundedCornerShape(StkDimens.RadiusControl),
                ) { Text("编辑项目") }
                StkPrimaryButton(
                    text = "查看操作",
                    enabled = !actionLoading,
                    onClick = onOpenOwnerActions,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            StkPrimaryButton(
                text = if (detail.contacts.isEmpty()) "暂无联系方式" else "查看联系方式",
                enabled = detail.contacts.isNotEmpty() && !detail.fromCache,
                onClick = {
                    if (detail.contacts.size == 1) selectContact(detail.contacts.first()) else showContactList = true
                },
                modifier = Modifier.align(Alignment.BottomCenter).background(StkColors.Surface).padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceSm),
            )
        }
    }

    imageIndex?.let { ImageViewer(gallery, it, onDismiss = { imageIndex = null }) }
    if (showContactList) ContactListSheet(detail.contacts, onDismiss = { showContactList = false }, onSelect = { showContactList = false; selectContact(it) })
    selectedContact?.let { contact ->
        ContactActionSheet(
            contact = contact,
            feedback = contactFeedback,
            onDismiss = { selectedContact = null },
            onCopy = {
                clipboard.setText(AnnotatedString(contact.value))
                contactFeedback = "已复制${contact.label}"
            },
            onOpen = {
                when (contact.type) {
                    "phone" -> Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", contact.value.filter { it.isDigit() || it == '+' }, null))
                    "wechat" -> Intent(Intent.ACTION_VIEW, Uri.parse("weixin://dl/chat?${Uri.encode(contact.value)}")).setPackage("com.tencent.mm")
                    "qq" -> Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=${Uri.encode(contact.value)}")).setPackage("com.tencent.mobileqq")
                    "url" -> null
                    else -> null
                }.let { intent ->
                    if (contact.type == "url") {
                        selectedContact = null
                        pendingUrl = contact.value
                    } else if (intent == null || runCatching { context.startActivity(intent) }.isFailure) {
                        selectedContact = null
                        missingContact = contact
                    }
                }
            },
        )
    }
    pendingUrl?.let { url ->
        val host = runCatching { Uri.parse(url).host }.getOrNull().orEmpty()
        ActionDialog(
            title = "即将打开外部网址",
            message = "目标域名：$host\n请确认后使用系统浏览器打开。",
            tone = ActionDialogTone.Info,
            primaryLabel = "继续打开",
            secondaryLabel = "取消",
            onPrimary = {
                pendingUrl = null
                if (runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }.isFailure) {
                    missingContact = ProjectContact("url", url, "网址")
                }
            },
            onSecondary = { pendingUrl = null },
            onDismiss = { pendingUrl = null },
        )
    }
    if (blockedUrl) StkStatusDialog("外部链接已被阻断", "目标地址不在白名单中，商推客不会继续打开。", StkStatusKind.Security, "返回") { blockedUrl = false }
    if (invalidTarget) StkStatusDialog("联系方式无效", "目标信息未通过安全校验，已阻止继续操作。", StkStatusKind.Security, "返回") { invalidTarget = false }
    missingContact?.let { contact ->
        val browserUrl = when (contact.type) {
            "wechat" -> "https://weixin.qq.com/"
            "qq" -> "https://im.qq.com/"
            "url" -> contact.value
            else -> null
        }
        ActionDialog(
            title = "目标应用未安装",
            message = "无法打开${contact.label}，可以复制信息${if (browserUrl != null) "或使用浏览器" else "后手动处理"}。",
            primaryLabel = if (browserUrl != null) "使用浏览器" else "复制信息",
            secondaryLabel = if (browserUrl != null) "复制信息" else "返回",
            onPrimary = {
                if (browserUrl != null && runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(browserUrl))) }.isSuccess) {
                    missingContact = null
                } else {
                    clipboard.setText(AnnotatedString(contact.value))
                    missingContact = null
                }
            },
            onSecondary = {
                if (browserUrl != null) clipboard.setText(AnnotatedString(contact.value))
                missingContact = null
            },
            onDismiss = { missingContact = null },
        )
    }
}

@Composable
private fun DetailHero(url: String, title: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(StkDimens.ProjectCoverHeight).clip(RoundedCornerShape(StkDimens.RadiusControl)).clickable(enabled = url.isNotBlank(), onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isBlank()) ImageFailurePlaceholder() else SubcomposeAsyncImage(
            model = url,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                is AsyncImagePainter.State.Loading -> CircularProgressIndicator(color = StkColors.BrandPrimary)
                else -> ImageFailurePlaceholder()
            }
        }
    }
}

@Composable
private fun ImageFailurePlaceholder() {
    Column(Modifier.fillMaxSize().background(StkColors.Background), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.ImageNotSupported, null, tint = StkColors.TextTertiary, modifier = Modifier.size(StkDimens.Icon))
        Text("图片加载失败", Modifier.padding(top = StkDimens.SpaceSm), style = MaterialTheme.typography.labelMedium, color = StkColors.TextTertiary)
    }
}

@Composable
private fun ImageViewer(urls: List<String>, selected: Int, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = selected.coerceIn(urls.indices)) { urls.size }
    var scale by rememberSaveable { mutableFloatStateOf(1f) }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var retryKey by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val transformState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 4f)
        if (scale > 1f) {
            offsetX += pan.x
            offsetY += pan.y
        }
    }
    LaunchedEffect(pagerState.currentPage) { scale = 1f; offsetX = 0f; offsetY = 0f; retryKey = 0 }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize().background(StkColors.ViewerBackground)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context).data(urls[page]).memoryCacheKey("${urls[page]}-$retryKey").build(),
                    contentDescription = "项目图片 ${page + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY).transformable(transformState),
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = StkColors.Surface) }
                        is AsyncImagePainter.State.Error -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(Modifier.size(StkDimens.EmptyStateIcon).background(StkColors.TextPrimary, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.ImageNotSupported, null, tint = StkColors.Surface) }
                            Text("图片加载失败", Modifier.padding(top = StkDimens.SpaceLg), style = MaterialTheme.typography.titleLarge, color = StkColors.Surface)
                            Text("可以重试或切换到下一张图片", style = MaterialTheme.typography.bodySmall, color = StkColors.TextTertiary)
                            Button(onClick = { retryKey++ }, modifier = Modifier.padding(top = StkDimens.SpaceXl), colors = ButtonDefaults.buttonColors(containerColor = StkColors.Surface, contentColor = StkColors.TextPrimary)) { Text("重新加载") }
                        }
                        else -> SubcomposeAsyncImageContent()
                    }
                }
            }
            Row(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(StkDimens.SpaceBase), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "关闭", tint = StkColors.Surface) }
                Spacer(Modifier.weight(1f))
                Text("${pagerState.currentPage + 1} / ${urls.size}", style = MaterialTheme.typography.bodyMedium, color = StkColors.Surface)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(StkDimens.MinTouch))
            }
            Row(Modifier.align(Alignment.BottomCenter).padding(bottom = StkDimens.Space2Xl), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                repeat(urls.size) { index -> Box(Modifier.size(StkDimens.SpaceSm).background(if (index == pagerState.currentPage) StkColors.Surface else StkColors.TextSecondary, CircleShape)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactListSheet(contacts: List<ProjectContact>, onDismiss: () -> Unit, onSelect: (ProjectContact) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = StkColors.Surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = StkDimens.SpaceXl).padding(bottom = StkDimens.SpaceXl)) {
            Text("选择联系方式", style = MaterialTheme.typography.titleLarge)
            contacts.forEach { contact ->
                Row(Modifier.fillMaxWidth().padding(top = StkDimens.SpaceMd).heightIn(min = StkDimens.MeEntryHeight).background(StkColors.Background, RoundedCornerShape(StkDimens.RadiusControl)).clickable { onSelect(contact) }.padding(StkDimens.SpaceBase), verticalAlignment = Alignment.CenterVertically) {
                    Text(contactDisplayLabel(contact), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(maskedContactValue(contact), style = MaterialTheme.typography.bodySmall, color = StkColors.TextSecondary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactActionSheet(contact: ProjectContact, feedback: String?, onDismiss: () -> Unit, onCopy: () -> Unit, onOpen: () -> Unit) {
    val openLabel = when (contact.type) {
        "phone" -> "拨打电话"
        "wechat" -> "尝试打开"
        "qq" -> "尝试打开"
        "url" -> "安全打开网址"
        else -> "打开"
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = StkColors.Surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = StkDimens.SpaceXl).padding(bottom = StkDimens.SpaceXl)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(contactDisplayLabel(contact), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "关闭") }
            }
            Column(Modifier.fillMaxWidth().background(StkColors.Background, RoundedCornerShape(StkDimens.RadiusControl)).padding(StkDimens.SpaceBase)) {
                Text("联系方式", style = MaterialTheme.typography.labelMedium, color = StkColors.TextTertiary)
                Text(contactDisplayValue(contact), Modifier.padding(top = StkDimens.SpaceXs), style = MaterialTheme.typography.bodyMedium, color = StkColors.TextPrimary)
            }
            feedback?.let { Row(Modifier.padding(top = StkDimens.SpaceSm), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.CheckCircle, null, tint = StkColors.Success, modifier = Modifier.size(StkDimens.IconSmall)); Text(it, Modifier.padding(start = StkDimens.SpaceSm), color = StkColors.Success, style = MaterialTheme.typography.bodySmall) } }
            Row(Modifier.fillMaxWidth().padding(top = StkDimens.SpaceBase), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                Button(onClick = onCopy, modifier = Modifier.weight(1f).height(StkDimens.PrimaryControlHeight), colors = ButtonDefaults.buttonColors(containerColor = StkColors.Surface, contentColor = StkColors.TextPrimary), shape = RoundedCornerShape(StkDimens.RadiusControl)) {
                    Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(StkDimens.IconSmall))
                    Text("复制", Modifier.padding(start = StkDimens.SpaceSm))
                }
                StkPrimaryButton(openLabel, onOpen, Modifier.weight(1f))
            }
        }
    }
}

private enum class ActionDialogTone { Info, Warning }

@Composable
private fun ActionDialog(
    title: String,
    message: String,
    primaryLabel: String,
    secondaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    onDismiss: () -> Unit,
    tone: ActionDialogTone = ActionDialogTone.Warning,
) {
    val iconBackground = if (tone == ActionDialogTone.Info) StkColors.BrandPrimarySoft else StkColors.BrandAccentSoft
    val iconTint = if (tone == ActionDialogTone.Info) StkColors.BrandPrimary else StkColors.Warning
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth(StkDimens.DialogWidthFraction).widthIn(max = StkDimens.CaptchaDialogMaxWidth).background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusDialog)).padding(StkDimens.SpaceXl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(StkDimens.MinTouch).background(iconBackground, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Launch, null, tint = iconTint) }
            Text(title, Modifier.padding(top = StkDimens.SpaceLg), style = MaterialTheme.typography.titleLarge, color = StkColors.TextPrimary, textAlign = TextAlign.Center)
            Text(message, Modifier.padding(top = StkDimens.SpaceMd), style = MaterialTheme.typography.bodyLarge, color = StkColors.TextSecondary, textAlign = TextAlign.Center)
            Row(Modifier.fillMaxWidth().padding(top = StkDimens.Space2Xl), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                Button(onClick = onSecondary, modifier = Modifier.weight(1f).height(StkDimens.PrimaryControlHeight).border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl)), colors = ButtonDefaults.buttonColors(containerColor = StkColors.Surface, contentColor = StkColors.TextPrimary), shape = RoundedCornerShape(StkDimens.RadiusControl)) { Text(secondaryLabel) }
                StkPrimaryButton(primaryLabel, onPrimary, Modifier.weight(1f))
            }
        }
    }
}

private fun isHttpsUrl(value: String): Boolean = runCatching {
    val uri = Uri.parse(value)
    uri.scheme.equals("https", true) && !uri.host.isNullOrBlank()
}.getOrDefault(false)

private fun maskedContactValue(contact: ProjectContact): String = when (contact.type) {
    "phone" -> contact.value.filter(Char::isDigit).let { digits -> if (digits.length >= 7) digits.take(3) + "****" + digits.takeLast(4) else "***" }
    else -> contact.value
}

private fun contactDisplayLabel(contact: ProjectContact): String = when (contact.type) {
    "phone" -> "手机号"
    "wechat" -> "微信"
    "qq" -> "QQ"
    "url" -> "网址"
    else -> contact.label.ifBlank { "联系方式" }
}

private fun contactDisplayValue(contact: ProjectContact): String = when (contact.type) {
    "phone" -> maskedContactValue(contact)
    else -> contact.value
}

private fun projectStatusLabel(status: String): String = when (status) {
    "pending" -> "待审核"
    "rejected" -> "已驳回"
    "offline", "unpublished" -> "已下架"
    else -> "已发布"
}

private fun projectStatusColors(status: String) = when (status) {
    "pending" -> StkColors.BrandAccentSoft to StkColors.Warning
    "rejected" -> StkColors.ErrorSoft to StkColors.Error
    "offline", "unpublished" -> StkColors.Background to StkColors.TextSecondary
    else -> StkColors.SuccessSoft to StkColors.Success
}

private fun ProjectDetail.asMyProject(): MyProject = MyProject(
    id = summary.id,
    title = summary.title,
    summary = summary.summary,
    categoryId = summary.categoryId,
    categoryName = summary.categoryName,
    status = when (status) {
        "pending" -> com.zzyihao.stk.data.project.MyProjectStatus.PENDING
        "rejected" -> com.zzyihao.stk.data.project.MyProjectStatus.REJECTED
        "offline", "unpublished" -> com.zzyihao.stk.data.project.MyProjectStatus.UNPUBLISHED
        else -> com.zzyihao.stk.data.project.MyProjectStatus.PUBLISHED
    },
    rejectionReason = rejectionReason,
    updatedAt = summary.publishedAt,
    rowVersion = rowVersion,
    ownerActions = ownerActions,
)
