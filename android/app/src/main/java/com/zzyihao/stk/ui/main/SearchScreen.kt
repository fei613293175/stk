package com.zzyihao.stk.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zzyihao.stk.data.project.ProjectRepository
import com.zzyihao.stk.ui.components.StkTextField
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    contentPadding: PaddingValues,
    repository: ProjectRepository,
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    val vm: ProjectViewModel = viewModel(factory = ProjectViewModel.Factory(repository))
    val state by vm.list.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var submittedQuery by rememberSaveable { mutableStateOf<String?>(null) }
    val exitSearch = {
        vm.updateQuery("")
        vm.search()
        onBack()
    }

    BackHandler(onBack = exitSearch)

    LaunchedEffect(state.query) {
        val query = state.query.trim()
        if (query.isEmpty()) {
            submittedQuery = null
            return@LaunchedEffect
        }
        delay(450)
        submittedQuery = query
        vm.search()
    }

    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        StkTopBar(
            title = "搜索项目",
            navigation = { IconButton(onClick = exitSearch) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
        )
        StkTextField(
            value = state.query,
            onValueChange = vm::updateQuery,
            label = "输入项目标题或简介",
            modifier = Modifier.padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceMd),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (state.query.isNotBlank()) {
                    submittedQuery = state.query.trim()
                    vm.search()
                    focusManager.clearFocus()
                }
            }),
            leadingContent = { Icon(Icons.Outlined.Search, null, tint = StkColors.TextTertiary) },
            trailingContent = if (state.query.isNotEmpty()) ({
                IconButton(onClick = { vm.updateQuery(""); submittedQuery = null }) {
                    Icon(Icons.Outlined.Close, "清空搜索", tint = StkColors.TextTertiary)
                }
            }) else null,
            controlHeight = StkDimens.SearchHeight,
        )
        SearchContent(state, submittedQuery, onRetry = vm::search, onClear = { vm.updateQuery(""); submittedQuery = null }, onOpenDetail = onOpenDetail)
    }
}

@Composable
private fun SearchContent(
    state: ProjectListState,
    submittedQuery: String?,
    onRetry: () -> Unit,
    onClear: () -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    when {
        state.query.isBlank() -> ProjectFullPageState(
            title = "搜索商推客项目",
            message = "输入项目标题或简介开始搜索，不保存个人搜索历史。",
            buttonLabel = null,
            onAction = {},
            kind = ProjectFailureKind.Info,
        )
        submittedQuery == null -> Text(
            "正在输入关键词",
            Modifier.fillMaxWidth().padding(StkDimens.SpaceBase),
            style = MaterialTheme.typography.bodySmall,
            color = StkColors.TextSecondary,
        )
        state.refreshing || (state.loading && state.items.isEmpty()) -> ProjectSkeletonList(modifier = Modifier.padding(top = StkDimens.SpaceSm))
        state.failure != null && state.items.isEmpty() -> ProjectFullPageState(
            title = "搜索失败",
            message = "无法获取搜索结果，请稍后重试。",
            buttonLabel = "重新搜索",
            onAction = onRetry,
            kind = state.failure.kind,
            requestId = state.failure.requestId,
        )
        state.items.isEmpty() -> ProjectFullPageState(
            title = "没有找到相关项目",
            message = "尝试缩短关键词或更换搜索内容。",
            buttonLabel = "清空关键词",
            onAction = onClear,
            kind = ProjectFailureKind.NotFound,
        )
        else -> LazyColumn(
            contentPadding = PaddingValues(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceSm),
            verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
        ) {
            if (state.offline) item { ProjectOfflineBanner("离线状态，仅匹配本地缓存结果。") }
            item { Text("找到 ${state.items.size} 个相关项目", style = MaterialTheme.typography.bodySmall, color = StkColors.TextSecondary) }
            items(state.items, key = { it.id }) { StkProjectCard(it, onClick = { onOpenDetail(it.id) }) }
        }
    }
}
