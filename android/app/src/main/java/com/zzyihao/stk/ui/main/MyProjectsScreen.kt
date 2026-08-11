package com.zzyihao.stk.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.zzyihao.stk.data.project.MyProject
import com.zzyihao.stk.data.project.MyProjectStatus
import com.zzyihao.stk.data.project.ProjectCategory
import com.zzyihao.stk.data.project.ProjectSubmissionRepository
import com.zzyihao.stk.ui.components.StkInfoCard
import com.zzyihao.stk.ui.components.StkFeedbackBanner
import com.zzyihao.stk.ui.components.StkFeedbackTone
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkSquareImagePlaceholder
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens

private data class PendingProjectAction(val project: MyProject, val action: String)

@Composable
fun MyProjectsScreen(
    contentPadding: PaddingValues,
    repository: ProjectSubmissionRepository,
    categories: List<ProjectCategory>,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onView: (MyProject) -> Unit,
    onEdit: (MyProject) -> Unit,
) {
    val vm: ProjectSubmissionViewModel = viewModel(factory = ProjectSubmissionViewModel.Factory(repository))
    val state by vm.mine.collectAsStateWithLifecycle()
    var actionProject by remember { mutableStateOf<MyProject?>(null) }
    var pendingAction by remember { mutableStateOf<PendingProjectAction?>(null) }
    LaunchedEffect(vm) { vm.loadMine(MyProjectStatus.ALL) }

    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        StkTopBar(
            title = "我的发布",
            navigation = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceLg),
            horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
        ) {
            items(myProjectTabs) { status ->
                val selected = status == state.status
                val count = state.statusCounts[status]
                Text(
                    text = buildString {
                        append(status.label)
                        if (count != null && status != MyProjectStatus.ALL) append(" ").append(count)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) StkColors.Surface else StkColors.TextSecondary,
                    modifier = Modifier
                        .height(StkDimens.SecondaryButtonHeight)
                        .clip(RoundedCornerShape(StkDimens.RadiusPill))
                        .background(if (selected) StkColors.BrandPrimary else StkColors.Surface)
                        .border(StkDimens.Divider, if (selected) StkColors.BrandPrimary else StkColors.Border, RoundedCornerShape(StkDimens.RadiusPill))
                        .clickable { vm.loadMine(status) }
                        .padding(horizontal = StkDimens.SpaceBase),
                )
            }
        }
        if (state.fromCache) {
            StkFeedbackBanner(
                message = "离线状态，仅可查看最近缓存，操作按钮暂不可用。",
                tone = StkFeedbackTone.Warning,
                modifier = Modifier.padding(horizontal = StkDimens.SpaceBase),
            )
        }
        when {
            state.loading && state.items.isEmpty() -> MyProjectsLoading()
            state.error != null && state.items.isEmpty() -> MyProjectsFailure(state.error ?: "我的发布加载失败") { vm.loadMine() }
            state.items.isEmpty() -> MyProjectsEmpty(onCreate)
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceSm),
                verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
            ) {
                items(state.items, key = { it.id }) { project ->
                    MyProjectCard(project = project, allowActions = !state.fromCache, onView = { onView(project) }, onActions = { actionProject = project })
                }
                if (state.error != null) {
                    item { TextButton(onClick = vm::loadMoreMine, modifier = Modifier.fillMaxWidth()) { Text("加载更多失败，重新加载", color = StkColors.Error) } }
                } else if (state.hasMore) {
                    item {
                        TextButton(onClick = vm::loadMoreMine, enabled = !state.loadingMore, modifier = Modifier.fillMaxWidth()) {
                            if (state.loadingMore) CircularProgressIndicator(modifier = Modifier.size(StkDimens.IconSmall), color = StkColors.BrandPrimary)
                            else Text("加载更多")
                        }
                    }
                }
            }
        }
    }

    actionProject?.let { project ->
        ProjectActionSheet(
            project = project,
            onDismiss = { actionProject = null },
            onView = { actionProject = null; onView(project) },
            onEdit = { actionProject = null; onEdit(project) },
            onOffline = { actionProject = null; pendingAction = PendingProjectAction(project, "offline") },
            onDelete = { actionProject = null; pendingAction = PendingProjectAction(project, "delete") },
        )
    }
    pendingAction?.let { pending ->
        val isDelete = pending.action == "delete"
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(if (isDelete && pending.project.status == MyProjectStatus.PENDING) "撤回审核" else if (isDelete) "删除项目" else "下架项目") },
            text = { Text(if (isDelete && pending.project.status == MyProjectStatus.PENDING) "撤回后项目不再进入审核流程，后台仍保留操作记录。" else if (isDelete) "删除后项目不再对外展示，后台仍保留审核记录。" else "下架后项目将不再在首页公开展示。") },
            confirmButton = {
                TextButton(onClick = {
                    if (isDelete) vm.softDelete(pending.project) else vm.unpublish(pending.project)
                    pendingAction = null
                }) { Text(if (isDelete && pending.project.status == MyProjectStatus.PENDING) "确认撤回" else if (isDelete) "确认删除" else "确认下架", color = StkColors.Error) }
            },
            dismissButton = { TextButton(onClick = { pendingAction = null }) { Text("取消") } },
            containerColor = StkColors.Surface,
        )
    }
}

private val myProjectTabs = listOf(
    MyProjectStatus.ALL,
    MyProjectStatus.PENDING,
    MyProjectStatus.PUBLISHED,
    MyProjectStatus.REJECTED,
    MyProjectStatus.UNPUBLISHED,
)

@Composable
private fun MyProjectsLoading() {
    ProjectSkeletonList(
        count = 4,
        modifier = Modifier.fillMaxSize().padding(top = StkDimens.SpaceSm),
    )
}

@Composable
private fun MyProjectsFailure(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message, color = StkColors.Error, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onRetry) { Text("重新加载") }
    }
}

@Composable
private fun MyProjectsEmpty(onCreate: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("暂无我的发布", style = MaterialTheme.typography.titleMedium, color = StkColors.TextPrimary)
        Text("发布项目后，可以在这里查看审核进度。", Modifier.padding(top = StkDimens.SpaceSm), style = MaterialTheme.typography.bodyMedium, color = StkColors.TextSecondary)
        StkPrimaryButton("发布项目", onCreate, Modifier.padding(top = StkDimens.SpaceXl))
    }
}

@Composable
private fun MyProjectCard(project: MyProject, allowActions: Boolean, onView: () -> Unit, onActions: () -> Unit) {
    StkInfoCard(Modifier.clickable(onClick = onView)) {
        Column {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                ProjectThumbnail(project.coverUrl, project.title)
                Column(Modifier.weight(1f).padding(start = StkDimens.SpaceMd)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text(project.title, Modifier.weight(1f), maxLines = 1, style = MaterialTheme.typography.titleMedium, color = StkColors.TextPrimary)
                        if (allowActions && project.ownerActions.isNotEmpty()) IconButton(onClick = onActions, modifier = Modifier.size(StkDimens.MinTouch)) {
                            Icon(Icons.Outlined.MoreHoriz, contentDescription = "项目操作", tint = StkColors.TextTertiary)
                        }
                    }
                    Text(project.summary, Modifier.padding(top = StkDimens.SpaceXs), maxLines = 2, style = MaterialTheme.typography.bodyMedium, color = StkColors.TextSecondary)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = StkDimens.SpaceSm), verticalAlignment = Alignment.CenterVertically) {
                StatusPill(project.status)
                Spacer(Modifier.weight(1f))
                Text("更新 ${project.updatedAt}", style = MaterialTheme.typography.labelMedium, color = StkColors.TextTertiary)
            }
            if (project.rejectionReason.isNotBlank()) {
                Text("驳回原因：${project.rejectionReason}", Modifier.padding(top = StkDimens.SpaceSm), style = MaterialTheme.typography.labelMedium, color = StkColors.Error)
            }
        }
    }
}

@Composable
private fun ProjectThumbnail(url: String, title: String) {
    Box(
        Modifier.width(StkDimens.PublishImageWidth).height(StkDimens.PublishImageHeight).clip(RoundedCornerShape(StkDimens.RadiusControl)),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isBlank()) StkSquareImagePlaceholder("项目图片", Modifier.fillMaxSize())
        else AsyncImage(model = url, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun StatusPill(status: MyProjectStatus) {
    val colors = when (status) {
        MyProjectStatus.PENDING -> StkColors.BrandAccentSoft to StkColors.Warning
        MyProjectStatus.PUBLISHED -> StkColors.SuccessSoft to StkColors.Success
        MyProjectStatus.REJECTED -> StkColors.ErrorSoft to StkColors.Error
        else -> StkColors.Background to StkColors.TextSecondary
    }
    Text(status.label, Modifier.background(colors.first, RoundedCornerShape(StkDimens.RadiusPill)).padding(horizontal = StkDimens.SpaceSm, vertical = StkDimens.SpaceXs), style = MaterialTheme.typography.labelMedium, color = colors.second)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectActionSheet(
    project: MyProject,
    onDismiss: () -> Unit,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onOffline: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean = true,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = StkColors.Surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = StkDimens.SpaceBase).padding(bottom = StkDimens.SpaceXl)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("项目操作", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = StkColors.TextPrimary)
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, contentDescription = "关闭", tint = StkColors.TextSecondary) }
            }
            if (project.ownerActions.contains("view")) ProjectActionRow(if (project.status == MyProjectStatus.REJECTED) "查看驳回原因" else "查看项目", Icons.Outlined.Visibility, onView, enabled = enabled)
            if (project.ownerActions.contains("edit")) ProjectActionRow("编辑项目", Icons.Outlined.EditNote, onEdit, enabled = enabled)
            if (project.status == MyProjectStatus.REJECTED && project.ownerActions.contains("resubmit")) ProjectActionRow("重新提交", Icons.Outlined.EditNote, onEdit, enabled = enabled)
            if (project.ownerActions.contains("offline")) ProjectActionRow("下架项目", Icons.Outlined.RemoveCircleOutline, onOffline, enabled = enabled)
            if (project.ownerActions.contains("delete")) ProjectActionRow(if (project.status == MyProjectStatus.PENDING) "撤回审核" else "删除项目", Icons.Outlined.DeleteOutline, onDelete, isDestructive = true, enabled = enabled)
        }
    }
}

@Composable
private fun ProjectActionRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, isDestructive: Boolean = false, enabled: Boolean = true) {
    val tint = if (isDestructive) StkColors.Error else StkColors.BrandPrimary
    Row(
        Modifier.fillMaxWidth().padding(top = StkDimens.SpaceMd).height(StkDimens.MeEntryHeight)
            .border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl))
            .clickable(enabled = enabled, onClick = onClick).padding(horizontal = StkDimens.SpaceBase),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(StkDimens.MinTouch).background(StkColors.BrandPrimarySoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(StkDimens.IconSmall))
        }
        Text(label, Modifier.padding(start = StkDimens.SpaceMd).weight(1f), style = MaterialTheme.typography.titleMedium, color = if (isDestructive) StkColors.Error else StkColors.TextPrimary)
        Text("›", style = MaterialTheme.typography.titleLarge, color = StkColors.TextTertiary)
    }
}
