package com.zzyihao.stk.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zzyihao.stk.data.project.ProjectCategory
import com.zzyihao.stk.data.project.ProjectRepository
import com.zzyihao.stk.ui.components.StkAppHeader
import com.zzyihao.stk.ui.components.StkPill
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    repository: ProjectRepository,
    initialScrollIndex: Int,
    initialScrollOffset: Int,
    onScrollPositionChanged: (Int, Int) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    val vm: ProjectViewModel = viewModel(factory = ProjectViewModel.Factory(repository))
    val state by vm.list.collectAsStateWithLifecycle()
    val listState = rememberLazyListState(initialScrollIndex, initialScrollOffset)
    val scope = rememberCoroutineScope()
    var showCategories by rememberSaveable { mutableStateOf(false) }
    DisposableEffect(listState) {
        onDispose { onScrollPositionChanged(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) }
    }

    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        StkAppHeader(onProfile = onOpenProfile)
        SearchEntry(onOpenSearch)
        CategoryPills(state.categories, state.categoryId, onOpen = { showCategories = true })
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = { vm.loadFirstPage(true) },
            modifier = Modifier.weight(1f),
        ) {
            HomeProjectContent(
                state = state,
                listState = listState,
                onRetry = { vm.loadFirstPage() },
                onLoadMore = vm::loadMore,
                onOpenDetail = onOpenDetail,
            )
        }
    }
    if (showCategories) {
        CategorySheet(
            categories = state.categories,
            selected = state.categoryId,
            failure = state.categoryFailure,
            onDismiss = { showCategories = false },
            onRetry = vm::loadCategories,
            onApply = {
                vm.setCategory(it)
                showCategories = false
                scope.launch { listState.scrollToItem(0) }
            },
        )
    }
}

@Composable
private fun SearchEntry(onOpen: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceMd)
            .heightIn(min = StkDimens.SearchHeight)
            .background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusControl))
            .border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl))
            .clickable(onClick = onOpen)
            .padding(horizontal = StkDimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
    ) {
        Icon(Icons.Outlined.Search, null, tint = StkColors.TextTertiary, modifier = Modifier.size(StkDimens.IconSmall))
        Text("搜索项目标题或简介", style = MaterialTheme.typography.bodyLarge, color = StkColors.TextTertiary)
    }
}

@Composable
private fun CategoryPills(categories: List<ProjectCategory>, selected: String?, onOpen: () -> Unit) {
    val visible = categories.filterNot { it.id == "all" }.take(3)
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceSm),
        horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm),
    ) {
        StkPill("全部", selected == null, onClick = onOpen)
        visible.forEach { category -> StkPill(category.name, selected == category.id, onClick = onOpen) }
        if (categories.size > visible.size + 1) TextButton(onClick = onOpen) { Text("更多", color = StkColors.BrandPrimary) }
    }
}

@Composable
private fun HomeProjectContent(
    state: ProjectListState,
    listState: LazyListState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    when {
        state.loading && state.items.isEmpty() -> ProjectSkeletonList(modifier = Modifier.padding(top = StkDimens.SpaceSm))
        state.failure != null && state.items.isEmpty() -> ProjectFailureState(state.failure, onRetry)
        state.items.isEmpty() -> ProjectFullPageState(
            title = "暂无可展示项目",
            message = "后台当前没有已发布项目，稍后刷新看看。",
            buttonLabel = "重新加载",
            onAction = onRetry,
        )
        else -> LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceSm),
            verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
        ) {
            if (state.refreshing) item { ProjectOfflineBanner("正在刷新最新项目…") }
            if (state.offline) item { ProjectOfflineBanner("当前离线，正在显示最近缓存的项目。") }
            items(state.items, key = { it.id }) { item ->
                StkProjectCard(item, onClick = { onOpenDetail(item.id) })
                if (item.id == state.items.lastOrNull()?.id && state.nextCursor != null && !state.loadingMore && state.pagingFailure == null) {
                    LaunchedEffect(item.id, state.nextCursor) { onLoadMore() }
                }
            }
            item {
                when {
                    state.loadingMore -> Row(
                        Modifier.fillMaxWidth().padding(StkDimens.SpaceBase),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(Modifier.size(StkDimens.IconSmall), color = StkColors.BrandPrimary, strokeWidth = StkDimens.SplashProgressStroke)
                        Text("正在加载更多", Modifier.padding(start = StkDimens.SpaceSm), style = MaterialTheme.typography.labelMedium, color = StkColors.TextSecondary)
                    }
                    state.pagingFailure != null -> Row(
                        Modifier.fillMaxWidth().background(StkColors.ErrorSoft, RoundedCornerShape(StkDimens.RadiusSmall)).padding(StkDimens.SpaceMd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.ErrorOutline, null, tint = StkColors.Error, modifier = Modifier.size(StkDimens.IconSmall))
                        Text("加载下一页失败", Modifier.weight(1f).padding(start = StkDimens.SpaceSm), color = StkColors.Error, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = onLoadMore) { Text("重试") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectFailureState(failure: ProjectFailure, onRetry: () -> Unit) {
    val (title, message) = when (failure.kind) {
        ProjectFailureKind.Offline -> "离线且没有缓存" to "连接网络后即可加载项目内容。"
        ProjectFailureKind.Timeout -> "请求超时" to "服务器响应较慢，您可以再次尝试。"
        ProjectFailureKind.Service -> "服务暂时异常" to "项目列表未能加载，请使用请求编号排查。"
        else -> "网络连接失败" to "无法加载首页，请检查网络后重试。"
    }
    ProjectFullPageState(title, message, "重新加载", onRetry, failure.kind, failure.requestId)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySheet(
    categories: List<ProjectCategory>,
    selected: String?,
    failure: ProjectFailure?,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onApply: (String?) -> Unit,
) {
    var pending by rememberSaveable(selected) { mutableStateOf(selected) }
    val options = categories.ifEmpty { listOf(ProjectCategory("all", "全部")) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = StkColors.Surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = StkDimens.SpaceXl).padding(bottom = StkDimens.SpaceXl)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("项目分类", style = MaterialTheme.typography.titleLarge, color = StkColors.TextPrimary)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, "关闭", tint = StkColors.TextSecondary) }
            }
            if (failure != null && categories.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(vertical = StkDimens.Space2Xl), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.ErrorOutline, null, tint = StkColors.Error, modifier = Modifier.size(StkDimens.SystemFeedbackInnerIcon))
                    Text("分类加载失败", Modifier.padding(top = StkDimens.SpaceMd), style = MaterialTheme.typography.titleMedium)
                    Text("已优先使用缓存分类，也可以重新加载。", style = MaterialTheme.typography.bodySmall, color = StkColors.TextSecondary)
                    StkPrimaryButton("重新加载", onRetry, Modifier.padding(top = StkDimens.SpaceXl))
                }
            } else {
                options.forEach { category ->
                    val value = category.id.takeUnless { it == "all" }
                    val active = pending == value
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = StkDimens.SpaceXs)
                            .heightIn(min = StkDimens.MinTouch)
                            .background(if (active) StkColors.BrandPrimarySoft else StkColors.Background, RoundedCornerShape(StkDimens.RadiusControl))
                            .border(StkDimens.Divider, if (active) StkColors.BrandPrimary else StkColors.Border, RoundedCornerShape(StkDimens.RadiusControl))
                            .clickable { pending = value }
                            .padding(horizontal = StkDimens.SpaceBase),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(category.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = if (active) StkColors.BrandPrimary else StkColors.TextPrimary)
                        if (active) Icon(Icons.Outlined.Check, null, tint = StkColors.BrandPrimary, modifier = Modifier.size(StkDimens.IconSmall))
                    }
                }
                if (options.size == 1) Text("管理员尚未启用其他分类。", Modifier.padding(top = StkDimens.SpaceSm), style = MaterialTheme.typography.bodySmall, color = StkColors.TextSecondary)
                Row(Modifier.fillMaxWidth().padding(top = StkDimens.SpaceXl), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                    Button(
                        onClick = { pending = null },
                        modifier = Modifier.weight(1f).heightIn(min = StkDimens.PrimaryControlHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = StkColors.Surface, contentColor = StkColors.TextPrimary),
                        shape = RoundedCornerShape(StkDimens.RadiusControl),
                    ) { Text("重置") }
                    StkPrimaryButton("确认", { onApply(pending) }, Modifier.weight(1f))
                }
            }
        }
    }
}
