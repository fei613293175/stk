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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import com.zzyihao.stk.data.account.AccountOverview
import com.zzyihao.stk.data.account.AccountRepository
import com.zzyihao.stk.data.account.MembershipStatus
import com.zzyihao.stk.data.auth.AuthSession
import com.zzyihao.stk.ui.components.StkInfoCard
import com.zzyihao.stk.ui.components.StkLogoutDialog
import com.zzyihao.stk.ui.components.LogoutDialogState
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkProjectListGlyph
import com.zzyihao.stk.ui.components.StkSettingsGlyph
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens
import kotlinx.coroutines.launch

@Composable
fun MeScreen(
    contentPadding: PaddingValues,
    session: AuthSession,
    onLogout: suspend () -> Unit,
    onOpenMyProjects: () -> Unit,
    onOpenDestination: (MeDestination) -> Unit,
    accountRepository: AccountRepository,
) {
    var showLogout by rememberSaveable { mutableStateOf(false) }
    var showAccountServices by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var overview by remember { mutableStateOf<AccountOverview?>(null) }
    var accountError by remember { mutableStateOf<String?>(null) }
    var accountRetry by rememberSaveable { mutableIntStateOf(0) }
    var logoutLoading by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(accountRepository, accountRetry) {
        overview = null
        accountError = null
        runCatching { accountRepository.getOverview() }
            .onSuccess { overview = it }
            .onFailure { accountError = it.message ?: "无法读取个人资料" }
    }

    Column(Modifier.fillMaxSize().padding(contentPadding).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(StkDimens.MeHeaderHeight).background(StkColors.BrandPrimary)) {
            Text(
                "我的",
                style = MaterialTheme.typography.headlineLarge,
                color = StkColors.Surface,
                modifier = Modifier.align(Alignment.TopStart).padding(StkDimens.SpaceBase),
            )
            IconButton(
                onClick = { onOpenDestination(MeDestination.Settings) },
                modifier = Modifier.align(Alignment.TopEnd).padding(StkDimens.SpaceSm),
            ) { StkSettingsGlyph(Modifier.size(StkDimens.Icon), StkColors.Surface) }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = StkDimens.MeHeaderHeight - StkDimens.MeHeaderOverlap)
                    .padding(horizontal = StkDimens.SpaceBase),
                verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceBase),
            ) {
            if (overview == null && accountError == null) {
                MeLoadingContent(session)
            } else {
                MeProfileCard(session, overview, onClick = { onOpenDestination(MeDestination.Profile) })
                if (accountError != null) {
                    MeLoadError(accountError!!, onRetry = { accountRetry++ })
                } else {
            Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                Text("常用入口", style = MaterialTheme.typography.titleLarge)
                MeEntry(
                    title = "我的发布",
                    subtitle = "管理已发布和审核中的项目",
                    onClick = onOpenMyProjects,
                    icon = { StkProjectListGlyph(Modifier.size(StkDimens.Icon), StkColors.BrandPrimary) },
                )
                MeEntry(
                    title = "会员、账户与道具",
                    subtitle = if (showAccountServices) "收起账户与服务资料" else "查看会员权益和账户资料",
                    onClick = { showAccountServices = !showAccountServices },
                    icon = { Icon(Icons.Default.StarBorder, contentDescription = null, tint = StkColors.BrandPrimary, modifier = Modifier.size(StkDimens.Icon)) },
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = StkDimens.MeNoticeMinHeight)
                    .background(StkColors.BrandPrimarySoft, RoundedCornerShape(StkDimens.RadiusControl))
                    .padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
            ) {
                Text("i", color = StkColors.BrandPrimary, style = MaterialTheme.typography.titleMedium)
                Text(
                    accountError?.let { "账户资料暂未同步：$it" } ?: "账户资料已同步，会员权益以当前状态为准。",
                    color = StkColors.TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
            }

            if (showAccountServices) {
                if (overview?.display?.showWallets != false) {
                    Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                        Text("账户概览", style = MaterialTheme.typography.titleLarge)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                            BalanceCard(overview?.commission?.title ?: overview?.display?.commissionLabel ?: "佣金账户", overview?.commission?.amount ?: "--", Modifier.weight(1f)) { onOpenDestination(MeDestination.Wallet) }
                            BalanceCard(overview?.tasks?.title ?: overview?.display?.taskLabel ?: "任务账户", overview?.tasks?.amount ?: "--", Modifier.weight(1f)) { onOpenDestination(MeDestination.Wallet) }
                        }
                    }
                }

                if (overview?.display?.showMemberCard != false) MembershipCard(overview, onClick = { onOpenDestination(MeDestination.Member) })

                Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                    Text("服务与资料", style = MaterialTheme.typography.titleLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                        QuickAction("浏览记录", Icons.Default.History, Modifier.weight(1f)) { onOpenDestination(MeDestination.History) }
                        QuickAction("收藏", Icons.Default.StarBorder, Modifier.weight(1f)) { onOpenDestination(MeDestination.Favorites) }
                        QuickAction("实名认证", Icons.Default.Shield, Modifier.weight(1f)) { onOpenDestination(MeDestination.RealName) }
                        QuickAction("个人资料", Icons.Default.Person, Modifier.weight(1f)) { onOpenDestination(MeDestination.Profile) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                        if (overview?.display?.showPropsCenter != false) QuickAction("道具中心", Icons.Default.StarBorder, Modifier.weight(1f)) { onOpenDestination(MeDestination.Props) }
                        QuickAction("联系客服", Icons.Default.Shield, Modifier.weight(1f)) { onOpenDestination(MeDestination.Support) }
                        QuickAction("关于商推客", Icons.Outlined.Settings, Modifier.weight(1f)) { onOpenDestination(MeDestination.About) }
                    }
                }

                TextButton(onClick = { showLogout = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = StkColors.TextSecondary)
                    Text("退出登录", color = StkColors.TextSecondary, modifier = Modifier.padding(start = StkDimens.SpaceSm))
                }
            }
                }
            }
            Spacer(Modifier.height(StkDimens.Space2Xl))
            }
        }
    }

    if (showLogout) {
        StkLogoutDialog(
            state = if (logoutLoading) LogoutDialogState.Loading else LogoutDialogState.Confirm,
            onDismiss = { if (!logoutLoading) showLogout = false },
            onConfirm = {
                if (!logoutLoading) {
                    logoutLoading = true
                    scope.launch {
                        onLogout()
                        logoutLoading = false
                        showLogout = false
                    }
                }
            },
        )
    }
}

@Composable
private fun MeProfileCard(session: AuthSession, overview: AccountOverview?, onClick: () -> Unit) {
    StkInfoCard(Modifier.height(StkDimens.MeProfileCardHeight).clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(StkDimens.AvatarProfile).background(StkColors.BrandPrimarySoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text("商", color = StkColors.BrandPrimary, style = MaterialTheme.typography.headlineLarge) }
            Column(Modifier.padding(start = StkDimens.SpaceBase).weight(1f), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceXs)) {
                Text(
                    overview?.profile?.username?.takeIf(String::isNotBlank) ?: session.user.username,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                )
                Text(
                    overview?.profile?.memberLabel?.takeIf(String::isNotBlank) ?: session.user.memberLabel,
                    color = StkColors.TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .background(StkColors.Background, RoundedCornerShape(StkDimens.RadiusPill))
                        .padding(horizontal = StkDimens.SpaceSm, vertical = StkDimens.SpaceXs),
                )
                Text("UID ${session.user.uid} · ${session.user.mobileMasked}", color = StkColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MeLoadingContent(session: AuthSession) {
    StkInfoCard(Modifier.height(StkDimens.MeProfileCardHeight)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(StkDimens.AvatarProfile).background(StkColors.Border.copy(alpha = 0.55f), CircleShape))
            Column(
                Modifier.padding(start = StkDimens.SpaceBase).weight(1f),
                verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
            ) {
                MeSkeletonLine(0.72f)
                MeSkeletonLine(0.48f)
                Text("UID ${session.user.uid} · ${session.user.mobileMasked}", color = StkColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    Text("账户概览", style = MaterialTheme.typography.titleLarge)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        repeat(2) {
            StkInfoCard(Modifier.weight(1f).height(StkDimens.MeBalanceCardHeight)) {
                Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceLg)) {
                    MeSkeletonLine(0.64f)
                    MeSkeletonLine(0.88f)
                }
            }
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(StkDimens.MeMembershipCardMinHeight)
            .background(StkColors.Border.copy(alpha = 0.42f), RoundedCornerShape(StkDimens.RadiusCard)),
    )
}

@Composable
private fun MeSkeletonLine(fraction: Float) {
    Box(
        Modifier
            .fillMaxWidth(fraction)
            .height(StkDimens.SpaceBase)
            .background(StkColors.Border.copy(alpha = 0.65f), RoundedCornerShape(StkDimens.RadiusSmall)),
    )
}

@Composable
private fun MeLoadError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().height(StkDimens.MeMembershipCardMinHeight + StkDimens.MeMembershipCardMinHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(StkDimens.SystemFeedbackIcon).background(StkColors.ErrorSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = StkColors.Error, modifier = Modifier.size(StkDimens.SystemFeedbackInnerIcon))
        }
        Text("资料加载失败", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = StkDimens.SpaceXl))
        Text(message, color = StkColors.TextSecondary, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = StkDimens.SpaceSm))
        StkPrimaryButton(
            text = "重新加载",
            onClick = onRetry,
            modifier = Modifier.width(StkDimens.EmptyStateButtonWidth).padding(top = StkDimens.SpaceXl),
        )
    }
}

@Composable
private fun MeEntry(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(StkDimens.MeEntryHeight)
            .background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusCard))
            .border(StkDimens.Divider, StkColors.Border, RoundedCornerShape(StkDimens.RadiusCard))
            .clickable(onClick = onClick)
            .padding(horizontal = StkDimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
            Box(
                Modifier.size(StkDimens.SecondaryButtonHeight).background(StkColors.BrandPrimarySoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) { icon() }
            Column(Modifier.padding(start = StkDimens.SpaceMd).weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = StkColors.TextPrimary, maxLines = 1)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StkColors.TextSecondary, maxLines = 1)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StkColors.TextTertiary, modifier = Modifier.size(StkDimens.IconSmall))
    }
}

@Composable
private fun BalanceCard(title: String, amount: String, modifier: Modifier, onClick: () -> Unit) {
    StkInfoCard(modifier.height(StkDimens.MeBalanceCardHeight).clickable(onClick = onClick)) {
        Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = StkColors.BrandPrimary, modifier = Modifier.size(StkDimens.IconSmall))
                Text(title, color = StkColors.TextSecondary, modifier = Modifier.padding(start = StkDimens.SpaceSm))
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StkColors.TextTertiary, modifier = Modifier.size(StkDimens.IconSmall))
            }
            Text(amount, style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Composable
private fun MembershipCard(overview: AccountOverview?, onClick: () -> Unit) {
    val membership = overview?.membership
    val status = membership?.status ?: MembershipStatus.INACTIVE
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = StkDimens.MeMembershipCardMinHeight)
            .background(
                Brush.horizontalGradient(listOf(StkColors.BrandPrimary, StkColors.MemberCardEnd)),
                RoundedCornerShape(StkDimens.RadiusCard),
            )
            .clickable(onClick = onClick)
            .padding(StkDimens.SpaceLg),
    ) {
        Column(Modifier.fillMaxWidth(0.68f), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
            Text(overview?.display?.memberTitle ?: "商推客会员", color = StkColors.Surface, style = MaterialTheme.typography.titleLarge)
            Text(
                when (status) {
                    MembershipStatus.ACTIVE -> "已开通 · 有效期至 ${membership?.expiresAt ?: "长期"}"
                    MembershipStatus.EXPIRED -> "会员已于 ${membership?.expiresAt ?: "--"} 到期"
                    MembershipStatus.INACTIVE -> "开通会员，推广更省心"
                },
                color = StkColors.Surface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                overview?.benefits?.take(2)?.forEach { benefit ->
                    Text(
                        benefit.description.substringBefore('（').ifBlank { benefit.title },
                        color = StkColors.BrandPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .background(StkColors.BrandPrimarySoft, RoundedCornerShape(StkDimens.RadiusPill))
                            .padding(horizontal = StkDimens.SpaceSm, vertical = StkDimens.SpaceXs),
                    )
                }
            }
        }
        Box(Modifier.align(Alignment.TopEnd).size(StkDimens.AvatarCard).background(StkColors.BrandPrimarySoft, CircleShape))
        Text(
            when (status) {
                MembershipStatus.ACTIVE -> "有效会员"
                MembershipStatus.EXPIRED -> "已过期"
                MembershipStatus.INACTIVE -> overview?.display?.memberOpenButtonText ?: "立即了解"
            },
            color = StkColors.Surface,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(StkColors.BrandAccent, RoundedCornerShape(StkDimens.RadiusControl))
                .padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceSm),
        )
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(StkDimens.QuickActionHeight)
            .background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusCard))
            .clickable(onClick = onClick)
            .padding(StkDimens.SpaceSm),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
            Icon(icon, contentDescription = null, tint = StkColors.BrandPrimary)
            Text(label, color = StkColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}
