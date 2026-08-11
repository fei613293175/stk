package com.zzyihao.stk.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zzyihao.stk.data.auth.AuthSession
import com.zzyihao.stk.data.project.ProjectRepository
import com.zzyihao.stk.data.project.ProjectSubmissionRepository
import com.zzyihao.stk.data.project.ProjectCategory
import com.zzyihao.stk.data.project.MyProject
import com.zzyihao.stk.data.project.PublishingConfig
import com.zzyihao.stk.data.account.AccountRepository
import com.zzyihao.stk.data.legal.LegalRepository
import com.zzyihao.stk.data.release.ReleaseRepository
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens
import com.zzyihao.stk.ui.components.StkHomeGlyph
import com.zzyihao.stk.ui.components.StkProfileGlyph
import com.zzyihao.stk.ui.components.StkPublishGlyph
import com.zzyihao.stk.ui.components.StkConfirmDialog
import com.zzyihao.stk.ui.components.StkFeedbackTone

private enum class MainTab(val label: String) { Home("首页"), Publish("发布"), Me("我的") }

@Composable
fun MainShell(
    session: AuthSession,
    onLogout: suspend () -> Unit,
    projectRepository: ProjectRepository,
    projectSubmissionRepository: ProjectSubmissionRepository,
    accountRepository: AccountRepository,
    legalRepository: LegalRepository,
    releaseRepository: ReleaseRepository,
    onClearCache: suspend () -> Unit,
    onMeSectionChanged: (Boolean) -> Unit = {},
    initialDetailId: String? = null,
    initialRoute: String = "stk://home",
) {
    var selected by rememberSaveable(initialRoute) { mutableStateOf(mainTabForRoute(initialRoute)) }
    var detailId by rememberSaveable { mutableStateOf(initialDetailId) }
    var showingSearch by rememberSaveable { mutableStateOf(initialRoute == "stk://search") }
    var homeScrollIndex by rememberSaveable { mutableIntStateOf(0) }
    var homeScrollOffset by rememberSaveable { mutableIntStateOf(0) }
    var showingMyProjects by rememberSaveable(initialRoute) { mutableStateOf(initialRoute == "stk://me/projects") }
    var secondaryStack by remember { mutableStateOf<List<MeDestination>>(emptyList()) }
    var editingProject by remember { mutableStateOf<MyProject?>(null) }
    var publishResult by remember { mutableStateOf<MyProject?>(null) }
    var publishDirty by remember { mutableStateOf(false) }
    var pendingTab by remember { mutableStateOf<MainTab?>(null) }
    val categoriesState = produceState<List<ProjectCategory>>(initialValue = emptyList(), projectRepository) {
        value = runCatching { projectRepository.listCategories() }.getOrDefault(emptyList())
    }
    val categories = categoriesState.value.ifEmpty {
        listOf(ProjectCategory("all", "全部", 0))
    }
    val publishingConfigState = produceState(initialValue = PublishingConfig(), projectSubmissionRepository) {
        value = runCatching { projectSubmissionRepository.getPublishingConfig() }.getOrDefault(PublishingConfig())
    }
    androidx.compose.runtime.LaunchedEffect(initialDetailId) {
        if (initialDetailId != null) {
            detailId = initialDetailId
            showingSearch = false
            selected = MainTab.Home
            showingMyProjects = false
            secondaryStack = emptyList()
        }
    }
    androidx.compose.runtime.LaunchedEffect(selected) {
        onMeSectionChanged(selected == MainTab.Me)
    }
    val showBottomNavigation = secondaryStack.isEmpty() && detailId == null && !showingMyProjects && !showingSearch

    Scaffold(
        containerColor = StkColors.Background,
        bottomBar = {
            if (showBottomNavigation) NavigationBar(modifier = Modifier.height(StkDimens.BottomBarHeight).navigationBarsPadding(), containerColor = StkColors.Surface) {
                MainTab.entries.forEach { tab ->
                    Box(Modifier.weight(1f).fillMaxSize().clickable {
                            if (selected == MainTab.Publish && publishDirty && tab != MainTab.Publish) {
                                pendingTab = tab
                                return@clickable
                            }
                            detailId = null
                            showingSearch = false
                            showingMyProjects = false
                            secondaryStack = emptyList()
                            if (tab == MainTab.Publish && selected == MainTab.Publish) publishResult = null
                            selected = tab
                        }, contentAlignment = androidx.compose.ui.Alignment.Center) {
                        val active = selected == tab
                        androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Box(Modifier.size(if (tab == MainTab.Publish) StkDimens.BottomNavSelectedCircle else StkDimens.Icon + StkDimens.SpaceMd).background(if (tab == MainTab.Publish) StkColors.BrandPrimarySoft else StkColors.Surface, CircleShape), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                val tint = if (active) StkColors.BrandPrimary else StkColors.TextTertiary
                                when (tab) {
                                    MainTab.Home -> StkHomeGlyph(Modifier.size(StkDimens.Icon), tint)
                                    MainTab.Publish -> StkPublishGlyph(Modifier.size(StkDimens.Icon), tint)
                                    MainTab.Me -> StkProfileGlyph(Modifier.size(StkDimens.Icon), tint)
                                }
                            }
                            Text(tab.label, color = if (active) StkColors.BrandPrimary else StkColors.TextTertiary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            val projectId = detailId
            val secondaryDestination = secondaryStack.lastOrNull()
            if (secondaryDestination != null) {
                SecondaryScreen(
                    destination = secondaryDestination,
                    session = session,
                    contentPadding = padding,
                    accountRepository = accountRepository,
                    legalRepository = legalRepository,
                    releaseRepository = releaseRepository,
                    onClearCache = onClearCache,
                    onLogout = onLogout,
                    onNavigate = { secondaryStack = secondaryStack + it },
                    onBack = { secondaryStack = secondaryStack.dropLast(1) },
                )
            } else if (projectId != null) {
                ProjectDetailScreen(
                    projectId = projectId,
                    repository = projectRepository,
                    submissionRepository = projectSubmissionRepository,
                    contentPadding = padding,
                    currentUid = session.user.uid,
                    onBack = { detailId = null },
                    onEditOwnProject = { project ->
                        detailId = null
                        showingMyProjects = false
                        editingProject = project
                        publishResult = null
                        selected = MainTab.Publish
                    },
                )
            } else when (selected) {
                MainTab.Home -> if (showingSearch) SearchScreen(
                    contentPadding = padding,
                    repository = projectRepository,
                    onBack = { showingSearch = false },
                    onOpenDetail = { detailId = it },
                ) else HomeScreen(
                        contentPadding = padding,
                        repository = projectRepository,
                        initialScrollIndex = homeScrollIndex,
                        initialScrollOffset = homeScrollOffset,
                        onScrollPositionChanged = { index, offset ->
                            homeScrollIndex = index
                            homeScrollOffset = offset
                        },
                        onOpenSearch = { showingSearch = true },
                        onOpenDetail = { detailId = it },
                        onOpenProfile = {
                            detailId = null
                            showingSearch = false
                            showingMyProjects = false
                            secondaryStack = emptyList()
                            selected = MainTab.Me
                        },
                    )
                MainTab.Publish -> if (publishResult != null) {
                    PublishResultScreen(
                        project = publishResult!!,
                        contentPadding = padding,
                        onViewProject = {
                            detailId = publishResult!!.id
                            publishResult = null
                            selected = MainTab.Home
                        },
                        onViewMine = {
                            publishResult = null
                            editingProject = null
                            showingMyProjects = true
                            selected = MainTab.Me
                        },
                        onPublishAnother = {
                            publishResult = null
                            editingProject = null
                        },
                    )
                } else PublishScreen(
                    contentPadding = padding,
                    repository = projectSubmissionRepository,
                    categories = categories,
                    publishingConfig = publishingConfigState.value,
                    initialProject = editingProject,
                    onDirtyChanged = { publishDirty = it },
                    onBack = {
                        if (publishDirty) pendingTab = MainTab.Home
                        else {
                            editingProject = null
                            selected = MainTab.Home
                        }
                    },
                    onEditUnavailable = {
                        editingProject = null
                        publishResult = null
                        showingMyProjects = true
                        selected = MainTab.Me
                    },
                    onDone = { project ->
                        publishDirty = false
                        editingProject = null
                        publishResult = project
                    },
                )
                MainTab.Me -> if (!showingMyProjects) MeScreen(
                    contentPadding = padding,
                    session = session,
                    onLogout = onLogout,
                    onOpenMyProjects = { showingMyProjects = true },
                    onOpenDestination = { showingMyProjects = false; secondaryStack = listOf(it) },
                    accountRepository = accountRepository,
                )
            }
            if (selected == MainTab.Me && showingMyProjects) MyProjectsScreen(
                contentPadding = padding,
                repository = projectSubmissionRepository,
                categories = categories,
                onBack = { showingMyProjects = false },
                onCreate = {
                    showingMyProjects = false
                    editingProject = null
                    publishResult = null
                    selected = MainTab.Publish
                },
                onView = { showingMyProjects = false; detailId = it.id },
                onEdit = { project ->
                    showingMyProjects = false
                    editingProject = project
                    publishResult = null
                    selected = MainTab.Publish
                },
            )
        }
    }
    pendingTab?.let { target ->
        StkConfirmDialog(
            title = "放弃未保存内容？",
            message = "当前表单已被修改，离开后本次尚未提交的内容将被清除。",
            primaryLabel = "放弃",
            secondaryLabel = "继续编辑",
            icon = Icons.Outlined.WarningAmber,
            tone = StkFeedbackTone.Warning,
            onConfirm = { publishDirty = false; editingProject = null; selected = target; pendingTab = null },
            onDismiss = { pendingTab = null },
        )
    }
    BackHandler(enabled = secondaryStack.isNotEmpty()) {
        secondaryStack = secondaryStack.dropLast(1)
    }
    BackHandler(enabled = secondaryStack.isEmpty() && detailId != null) {
        detailId = null
    }
    BackHandler(enabled = secondaryStack.isEmpty() && detailId == null && selected == MainTab.Me && showingMyProjects) {
        showingMyProjects = false
    }
    BackHandler(enabled = selected == MainTab.Publish && secondaryStack.isEmpty() && detailId == null) {
        if (publishDirty) pendingTab = MainTab.Home
        else selected = MainTab.Home
    }
}

private fun mainTabForRoute(route: String): MainTab = when (route) {
    "stk://publish" -> MainTab.Publish
    "stk://me", "stk://me/projects" -> MainTab.Me
    else -> MainTab.Home
}
