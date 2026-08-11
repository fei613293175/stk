package com.zzyihao.stk.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.zzyihao.stk.BuildConfig
import com.zzyihao.stk.data.account.AccountRepository
import com.zzyihao.stk.data.account.MembershipCard
import com.zzyihao.stk.data.account.MembershipStatus
import com.zzyihao.stk.data.account.ProfileCard
import com.zzyihao.stk.data.account.PropItem
import com.zzyihao.stk.data.account.PropsOverview
import com.zzyihao.stk.data.account.SupportCard
import com.zzyihao.stk.data.account.WalletOverview
import com.zzyihao.stk.data.auth.AuthSession
import com.zzyihao.stk.data.legal.LegalRepository
import com.zzyihao.stk.data.release.ReleaseRepository
import com.zzyihao.stk.ui.auth.LegalDocument
import com.zzyihao.stk.ui.auth.LegalScreen
import com.zzyihao.stk.ui.components.LogoutDialogState
import com.zzyihao.stk.ui.components.StkConfirmDialog
import com.zzyihao.stk.ui.components.StkFeedbackBanner
import com.zzyihao.stk.ui.components.StkFeedbackTone
import com.zzyihao.stk.ui.components.StkBrandMark
import com.zzyihao.stk.ui.components.StkInfoCard
import com.zzyihao.stk.ui.components.StkLogoutDialog
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkStatusDialog
import com.zzyihao.stk.ui.components.StkStatusKind
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens
import kotlinx.coroutines.launch

enum class MeDestination(val title: String) {
    Profile("个人资料"), Member("会员中心"), Wallet("账户余额"), Props("道具中心"),
    History("浏览记录"), Favorites("我的收藏"), RealName("实名认证"), Support("联系客服"),
    Settings("设置"), About("关于商推客"), UserAgreement("用户协议"), PrivacyPolicy("隐私政策"),
}

private sealed interface RemoteState<out T> {
    data object Loading : RemoteState<Nothing>
    data class Ready<T>(val value: T) : RemoteState<T>
    data class Failed(val message: String) : RemoteState<Nothing>
}

@Composable
fun SecondaryScreen(
    destination: MeDestination,
    session: AuthSession,
    contentPadding: PaddingValues,
    accountRepository: AccountRepository,
    legalRepository: LegalRepository,
    releaseRepository: ReleaseRepository,
    onClearCache: suspend () -> Unit,
    onLogout: suspend () -> Unit,
    onNavigate: (MeDestination) -> Unit,
    onBack: () -> Unit,
) {
    if (destination == MeDestination.UserAgreement || destination == MeDestination.PrivacyPolicy) {
        LegalScreen(
            document = if (destination == MeDestination.UserAgreement) LegalDocument.UserAgreement else LegalDocument.PrivacyPolicy,
            repository = legalRepository,
            onBack = onBack,
        )
        return
    }
    var retry by rememberSaveable(destination) { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        StkTopBar(
            title = destination.title,
            navigation = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
        )
        when (destination) {
            MeDestination.Profile -> RemoteContent(destination, retry, { accountRepository.getProfile() }, { retry++ }) {
                ProfilePage(session, it)
            }
            MeDestination.Member -> RemoteContent(destination, retry, { accountRepository.getMembership() }, { retry++ }) { MemberPage(it) }
            MeDestination.Wallet -> RemoteContent(destination, retry, { accountRepository.getWallets() }, { retry++ }) { WalletPage(it) }
            MeDestination.Props -> RemoteContent(destination, retry, { accountRepository.getProps() }, { retry++ }) { PropsPage(it) }
            MeDestination.Support -> RemoteContent(destination, retry, { accountRepository.getSupport() }, { retry++ }) { SupportPage(it) }
            MeDestination.History -> PlaceholderContent(destination, retry, accountRepository, { retry++ }, "本期不建立个人浏览记录。")
            MeDestination.Favorites -> PlaceholderContent(destination, retry, accountRepository, { retry++ }, "本期不建立收藏业务。")
            MeDestination.RealName -> PlaceholderContent(destination, retry, accountRepository, { retry++ }, "本期不采集身份信息。")
            MeDestination.Settings -> SettingsPage(onClearCache, onNavigate, onLogout)
            MeDestination.About -> AboutPage(releaseRepository)
            MeDestination.UserAgreement, MeDestination.PrivacyPolicy -> Unit
        }
    }
}

@Composable
private fun <T> RemoteContent(
    key: Any,
    retry: Int,
    loader: suspend () -> T,
    onRetry: () -> Unit,
    content: @Composable (T) -> Unit,
) {
    val state by produceState<RemoteState<T>>(RemoteState.Loading, key, retry) {
        value = runCatching { loader() }
            .fold({ RemoteState.Ready(it) }, { RemoteState.Failed(it.message ?: "信息加载失败") })
    }
    when (val current = state) {
        RemoteState.Loading -> LoadingCards()
        is RemoteState.Failed -> LoadError(current.message, onRetry)
        is RemoteState.Ready -> content(current.value)
    }
}

@Composable
private fun LoadingCards() {
    Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        repeat(3) {
            StkInfoCard(Modifier.height(if (it == 0) StkDimens.MeProfileCardHeight else StkDimens.MeBalanceCardHeight)) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                    SkeletonLine(0.42f)
                    SkeletonLine(0.76f)
                }
            }
        }
    }
}

@Composable
private fun SkeletonLine(widthFraction: Float) {
    Box(
        Modifier
            .fillMaxWidth(widthFraction)
            .height(StkDimens.SpaceBase)
            .background(StkColors.Border, RoundedCornerShape(StkDimens.RadiusSmall)),
    )
}

@Composable
private fun LoadError(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(StkDimens.SpaceBase),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("加载失败", style = MaterialTheme.typography.titleLarge)
        Text(message, color = StkColors.TextSecondary, modifier = Modifier.padding(top = StkDimens.SpaceSm))
        StkPrimaryButton("重新加载", onRetry, Modifier.padding(top = StkDimens.SpaceXl))
    }
}

@Composable
private fun OfflineBanner(visible: Boolean) {
    if (visible) {
        StkFeedbackBanner("当前离线，正在展示最近同步的数据。", StkFeedbackTone.Warning)
    }
}

@Composable
private fun ProfilePage(session: AuthSession, profile: ProfileCard) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        OfflineBanner(profile.fromCache)
        Column(
            Modifier.fillMaxWidth().padding(vertical = StkDimens.SpaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
        ) {
            ProfileAvatar(profile)
            Text(profile.username.ifBlank { session.user.username }, style = MaterialTheme.typography.headlineLarge)
            Text(
                profile.memberLabel,
                style = MaterialTheme.typography.labelMedium,
                color = StkColors.TextSecondary,
                modifier = Modifier.background(StkColors.Surface, RoundedCornerShape(StkDimens.RadiusPill)).padding(horizontal = StkDimens.SpaceSm, vertical = StkDimens.SpaceXs),
            )
        }
        ReadOnlyRow("UID", (profile.uid.takeIf { it > 0 } ?: session.user.uid).toString())
        ReadOnlyRow("手机号", profile.mobileMasked.ifBlank { session.user.mobileMasked })
        ReadOnlyRow("简介", profile.bio.ifBlank { "暂未填写" })
        Text("个人资料由服务端只读提供。", color = StkColors.TextTertiary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProfileAvatar(profile: ProfileCard) {
    if (profile.avatarUrl.startsWith("https://")) {
        AsyncImage(
            model = profile.avatarUrl,
            contentDescription = "用户头像",
            modifier = Modifier.size(StkDimens.AvatarProfile).clip(CircleShape),
        )
    } else {
        Box(Modifier.size(StkDimens.AvatarProfile).background(StkColors.BrandPrimarySoft, CircleShape), contentAlignment = Alignment.Center) {
            Text("商", style = MaterialTheme.typography.headlineLarge, color = StkColors.BrandPrimary)
        }
    }
}

@Composable
private fun MemberPage(membership: MembershipCard) {
    var showStageDialog by rememberSaveable { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        item { OfflineBanner(membership.fromCache) }
        item { MemberHero(membership) { showStageDialog = true } }
        item { Text("会员权益说明", style = MaterialTheme.typography.titleLarge) }
        items(membership.benefits) { benefit ->
            StkInfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(StkDimens.MinTouch).background(StkColors.BrandPrimarySoft, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.StarBorder, contentDescription = null, tint = StkColors.BrandPrimary)
                    }
                    Column(Modifier.weight(1f).padding(start = StkDimens.SpaceMd), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceXs)) {
                        Text(benefit.title, style = MaterialTheme.typography.titleMedium)
                        Text(benefit.description, color = StkColors.TextSecondary)
                    }
                }
            }
        }
        if (membership.benefits.isEmpty()) item { StkFeedbackBanner("管理员暂未配置会员权益说明。", StkFeedbackTone.Warning) }
        if (membership.status == MembershipStatus.INACTIVE && membership.showCard) {
            item { StkPrimaryButton(membership.openButtonText, { showStageDialog = true }) }
        }
    }
    if (showStageDialog) StageScopeDialog(
        title = "会员在线开通未开放",
        message = "当前仅展示会员状态与后台配置的权益，不会创建订单、扣款或显示虚假开通结果。",
        onDismiss = { showStageDialog = false },
    )
}

@Composable
private fun MemberHero(membership: MembershipCard, onOpen: () -> Unit) {
    val statusText = when (membership.status) {
        MembershipStatus.ACTIVE -> "已开通 · 有效期至 ${membership.expiresAt ?: "长期"}"
        MembershipStatus.EXPIRED -> "已于 ${membership.expiresAt ?: "--"} 到期"
        MembershipStatus.DISABLED -> "会员状态已关闭"
        MembershipStatus.INACTIVE -> "开通会员，推广更省心"
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(StkDimens.MeMembershipCardMinHeight)
            .background(Brush.horizontalGradient(listOf(StkColors.BrandPrimary, StkColors.MemberCardEnd)), RoundedCornerShape(StkDimens.RadiusCard))
            .padding(StkDimens.SpaceLg),
    ) {
        Column(Modifier.fillMaxWidth(0.74f), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
            Text(membership.title, style = MaterialTheme.typography.titleLarge, color = StkColors.Surface)
            Text(statusText, color = StkColors.Surface)
            Row(horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                membership.benefits.take(2).forEach { benefit ->
                    Text(
                        benefit.description,
                        style = MaterialTheme.typography.labelMedium,
                        color = StkColors.BrandPrimary,
                        modifier = Modifier.background(StkColors.BrandPrimarySoft, RoundedCornerShape(StkDimens.RadiusPill)).padding(horizontal = StkDimens.SpaceSm, vertical = StkDimens.SpaceXs),
                    )
                }
            }
            if (membership.status == MembershipStatus.ACTIVE && membership.startsAt != null) {
                Text("开通日期 ${membership.startsAt}", style = MaterialTheme.typography.bodySmall, color = StkColors.Surface)
            }
        }
        Box(Modifier.align(Alignment.TopEnd).size(StkDimens.AvatarCard).background(StkColors.BrandPrimary, CircleShape))
        if (membership.status == MembershipStatus.INACTIVE && membership.showCard) {
            Text(
                membership.openButtonText,
                style = MaterialTheme.typography.titleMedium,
                color = StkColors.Surface,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(StkColors.BrandAccent, RoundedCornerShape(StkDimens.RadiusControl))
                    .clickable(onClick = onOpen)
                    .padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceSm),
            )
        }
    }
}

@Composable
private fun WalletPage(wallets: WalletOverview) {
    var showStageDialog by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        OfflineBanner(wallets.fromCache)
        if (!wallets.showWallets) {
            EmptyPage("账户暂不展示", "账户展示已由管理员关闭。")
        } else {
            Text("账户概览", style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                WalletSummaryCard(wallets.commission.title, wallets.commission.amount, Modifier.weight(1f))
                WalletSummaryCard(wallets.tasks.title, wallets.tasks.amount, Modifier.weight(1f))
            }
            ReadOnlyNotice("本期仅展示余额，不提供提现、转账或账户流水。")
            Text("账户说明", style = MaterialTheme.typography.titleLarge)
            BalanceSection(wallets.commission.title, wallets.commission.description) { showStageDialog = true }
            BalanceSection(wallets.tasks.title, wallets.tasks.description) { showStageDialog = true }
        }
    }
    if (showStageDialog) StageScopeDialog(
        title = "账户操作未开放",
        message = "当前仅展示余额，不提供提现、转账、资金增减或流水操作。",
        onDismiss = { showStageDialog = false },
    )
}

@Composable
private fun WalletSummaryCard(title: String, amount: String, modifier: Modifier) {
    StkInfoCard(modifier.height(StkDimens.MeSecondaryCardHeight)) {
        Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceLg)) {
            Text(title, color = StkColors.TextSecondary)
            Text(displayAmount(amount), style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Composable
private fun BalanceSection(title: String, description: String, onClick: () -> Unit) {
    StkInfoCard(Modifier.clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(StkDimens.MinTouch).background(StkColors.BrandPrimarySoft, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Info, contentDescription = null, tint = StkColors.BrandPrimary)
            }
            Column(Modifier.weight(1f).padding(start = StkDimens.SpaceMd)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, color = StkColors.TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StkColors.TextTertiary)
        }
    }
}

@Composable
private fun PropsPage(props: PropsOverview) {
    var selectedProp by remember { mutableStateOf<PropItem?>(null) }
    if (!props.showCenter || props.items.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase)) {
            OfflineBanner(props.fromCache)
            EmptyPage("暂无展示道具", "道具目录由管理员维护。")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
            item { OfflineBanner(props.fromCache) }
            items(props.items.chunked(2)) { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
                    rowItems.forEach { prop -> PropGridCard(prop, Modifier.weight(1f)) { selectedProp = prop } }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            item { ReadOnlyNotice("本期只展示道具，不显示购买价格、库存或使用入口。") }
        }
    }
    selectedProp?.let { prop ->
        StageScopeDialog(
            title = "道具购买未开放",
            message = "${prop.title}：${prop.description}\n\n当前不提供购买、库存、使用或项目排名影响。",
            onDismiss = { selectedProp = null },
        )
    }
}

@Composable
private fun PropGridCard(prop: PropItem, modifier: Modifier, onClick: () -> Unit) {
    StkInfoCard(modifier.height(StkDimens.MeSecondaryCardHeight).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(StkDimens.MinTouch).background(StkColors.BrandPrimarySoft, CircleShape), contentAlignment = Alignment.Center) {
                if (prop.iconUrl.startsWith("https://")) AsyncImage(prop.iconUrl, contentDescription = prop.title, modifier = Modifier.size(StkDimens.Icon))
                else Icon(Icons.Default.StarBorder, contentDescription = null, tint = StkColors.BrandPrimary)
            }
            Text(prop.title, style = MaterialTheme.typography.titleMedium)
            Text(prop.description, color = StkColors.TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PlaceholderContent(
    destination: MeDestination,
    retry: Int,
    repository: AccountRepository,
    onRetry: () -> Unit,
    detail: String,
) {
    RemoteContent(destination, retry, { repository.getProfile() }, onRetry) { profile ->
        Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase)) {
            OfflineBanner(profile.fromCache)
            EmptyPage(destination.title, "${profile.placeholderMessage}\n$detail")
        }
    }
}

@Composable
private fun SupportPage(support: SupportCard) {
    val context = LocalContext.current
    var feedback by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var confirmExternal by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        OfflineBanner(support.fromCache)
        StkInfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                Text(support.label, style = MaterialTheme.typography.titleLarge)
                Text(support.value.ifBlank { "后台尚未配置客服值" }, color = StkColors.TextSecondary)
                Text(support.serviceHours, color = StkColors.TextTertiary)
            }
        }
        if (support.value.isNotBlank() && support.copyEnabled) {
            StkPrimaryButton("复制客服信息", {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(support.label, support.value))
                feedback = "客服信息已复制" to false
            })
        }
        if (support.value.isNotBlank()) {
            StkPrimaryButton("打开客服", { confirmExternal = true })
        }
        feedback?.let { (message, failed) ->
            StkFeedbackBanner(message, if (failed) StkFeedbackTone.Error else StkFeedbackTone.Success)
        }
    }
    if (confirmExternal) {
        StkConfirmDialog(
            title = "打开外部应用？",
            message = "将离开商推客并打开${support.label}。",
            primaryLabel = "继续打开",
            secondaryLabel = "取消",
            icon = Icons.AutoMirrored.Outlined.OpenInNew,
            tone = StkFeedbackTone.Warning,
            onConfirm = {
                confirmExternal = false
                feedback = openSupport(context, support).let { it to it.startsWith("无法") }
            },
            onDismiss = { confirmExternal = false },
        )
    }
}

private fun openSupport(context: Context, support: SupportCard): String {
    val type = support.type.lowercase()
    val value = support.value.trim()
    val uri = when (type) {
        "phone" -> value.filter { it.isDigit() || it == '+' }.takeIf { it.matches(Regex("\\+?\\d{5,20}")) }
            ?.let { Uri.fromParts("tel", it, null) }
        "qq" -> value.takeIf { it.matches(Regex("\\d{5,15}")) }
            ?.let { Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=${Uri.encode(it)}") }
        "wechat" -> Uri.parse("weixin://")
        "email" -> value.takeIf { Patterns.EMAIL_ADDRESS.matcher(it).matches() }
            ?.let { Uri.fromParts("mailto", it, null) }
        "url" -> Uri.parse(value).takeIf {
            it.scheme.equals("https", true) && !it.host.isNullOrBlank() && it.host!!.lowercase() in support.allowedUrlHosts
        }
        else -> null
    } ?: return "无法打开未授权或格式不正确的客服地址"
    val action = if (type == "phone") Intent.ACTION_DIAL else Intent.ACTION_VIEW
    val intent = Intent(action, uri)
    if (intent.resolveActivity(context.packageManager) == null) return "无法打开对应应用，请复制客服信息"
    return runCatching {
        context.startActivity(intent)
        "已打开外部应用"
    }.getOrElse { "无法打开对应应用，请复制客服信息" }
}

@Composable
private fun EmptyPage(title: String, description: String) {
    Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(StkDimens.EmptyStateIcon).background(StkColors.BrandPrimarySoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Info, contentDescription = null, tint = StkColors.BrandPrimary, modifier = Modifier.size(StkDimens.SystemFeedbackInnerIcon))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = StkDimens.SpaceXl))
        Text(description, color = StkColors.TextSecondary, modifier = Modifier.padding(top = StkDimens.SpaceSm))
    }
}

private data class SettingsItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val tint: Color = StkColors.BrandPrimary,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

private sealed interface CacheClearState {
    data object Idle : CacheClearState
    data object Clearing : CacheClearState
    data object Success : CacheClearState
    data class Failed(val message: String) : CacheClearState
}

@Composable
private fun SettingsPage(
    onClearCache: suspend () -> Unit,
    onNavigate: (MeDestination) -> Unit,
    onLogout: suspend () -> Unit,
) {
    var clearState by remember { mutableStateOf<CacheClearState>(CacheClearState.Idle) }
    var showLogout by rememberSaveable { mutableStateOf(false) }
    var logoutLoading by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clearing = clearState == CacheClearState.Clearing
    val rows = listOf(
        SettingsItem("清理缓存", "清除图片、项目列表、分类、详情与账户缓存", Icons.Default.DeleteOutline, enabled = !clearing) {
            scope.launch {
                clearState = CacheClearState.Clearing
                clearState = runCatching { onClearCache() }
                    .fold({ CacheClearState.Success }, { CacheClearState.Failed(it.message ?: "缓存清理失败") })
            }
        },
        SettingsItem("用户协议", "查看服务端版本化协议", Icons.AutoMirrored.Filled.Article) { onNavigate(MeDestination.UserAgreement) },
        SettingsItem("隐私政策", "查看服务端版本化隐私政策", Icons.Default.Shield) { onNavigate(MeDestination.PrivacyPolicy) },
        SettingsItem("关于商推客", "V${BuildConfig.VERSION_NAME}", Icons.Default.Info) { onNavigate(MeDestination.About) },
        SettingsItem("退出登录", "清除本机登录状态", Icons.AutoMirrored.Filled.ExitToApp, StkColors.Error) { showLogout = true },
    )
    LazyColumn(contentPadding = PaddingValues(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
        items(rows) { row -> SettingRow(row) }
        when (val state = clearState) {
            CacheClearState.Clearing -> item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
                    CircularProgressIndicator(Modifier.size(StkDimens.InlineProgress), color = StkColors.BrandPrimary, strokeWidth = StkDimens.SplashProgressStroke)
                    Text("正在清理缓存", color = StkColors.TextSecondary)
                }
            }
            CacheClearState.Success -> item { StkFeedbackBanner("缓存已清理", StkFeedbackTone.Success) }
            is CacheClearState.Failed -> item { StkFeedbackBanner(state.message, StkFeedbackTone.Error, actionLabel = "重试", onAction = rows.first().onClick) }
            CacheClearState.Idle -> Unit
        }
    }
    if (showLogout) {
        StkLogoutDialog(
            state = if (logoutLoading) LogoutDialogState.Loading else LogoutDialogState.Confirm,
            onDismiss = { if (!logoutLoading) showLogout = false },
            onConfirm = {
                if (!logoutLoading) scope.launch {
                    logoutLoading = true
                    onLogout()
                    logoutLoading = false
                    showLogout = false
                }
            },
        )
    }
}

@Composable
private fun SettingRow(item: SettingsItem) {
    StkInfoCard(Modifier.clickable(enabled = item.enabled, onClick = item.onClick)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(StkDimens.AvatarList).background(item.tint.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(item.icon, contentDescription = null, tint = if (item.enabled) item.tint else StkColors.Disabled)
            }
            Column(Modifier.weight(1f).padding(start = StkDimens.SpaceMd)) {
                Text(item.label, style = MaterialTheme.typography.titleMedium, color = if (item.tint == StkColors.Error) StkColors.Error else StkColors.TextPrimary)
                Text(item.value, color = StkColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StkColors.TextTertiary)
        }
    }
}

@Composable
private fun AboutPage(@Suppress("UNUSED_PARAMETER") releaseRepository: ReleaseRepository) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(StkDimens.SpaceBase),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = StkDimens.Space2Xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
        ) {
            StkBrandMark(markSizeOverride = StkDimens.AboutBrandMark, iconSizeOverride = StkDimens.MinTouch)
            Text("商推客", style = MaterialTheme.typography.headlineLarge)
            Text("Android V${BuildConfig.VERSION_NAME} · versionCode ${BuildConfig.VERSION_CODE}", color = StkColors.TextSecondary)
        }
        ReadOnlyRow("应用标识", BuildConfig.APPLICATION_ID)
        Text("项目发现、发布与审核客户端", color = StkColors.TextTertiary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StageScopeDialog(title: String, message: String, onDismiss: () -> Unit) {
    StkStatusDialog(title, message, StkStatusKind.Info, "我知道了", onDismiss)
}

@Composable
private fun ReadOnlyRow(label: String, value: String) {
    StkInfoCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = StkColors.TextSecondary)
            Spacer(Modifier.weight(1f))
            Text(value, color = StkColors.TextPrimary)
        }
    }
}

@Composable
private fun ReadOnlyNotice(message: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(StkColors.BrandPrimarySoft, RoundedCornerShape(StkDimens.RadiusControl))
            .padding(horizontal = StkDimens.SpaceBase, vertical = StkDimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd),
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = StkColors.BrandPrimary, modifier = Modifier.size(StkDimens.IconSmall))
        Text(message, color = StkColors.TextSecondary, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

private fun displayAmount(amount: String): String = when {
    amount.startsWith("¥") || amount.startsWith("￥") || amount == "--" -> amount
    else -> "¥ $amount"
}
