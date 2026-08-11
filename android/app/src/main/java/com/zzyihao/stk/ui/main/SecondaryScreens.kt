package com.zzyihao.stk.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.zzyihao.stk.BuildConfig
import com.zzyihao.stk.data.account.AccountOverview
import com.zzyihao.stk.data.account.MembershipStatus
import com.zzyihao.stk.data.account.AccountRepository
import com.zzyihao.stk.data.auth.AuthSession
import com.zzyihao.stk.data.legal.LegalRepository
import com.zzyihao.stk.data.release.ReleaseManifest
import com.zzyihao.stk.data.release.ReleaseRepository
import com.zzyihao.stk.ui.auth.LegalDocument
import com.zzyihao.stk.ui.auth.LegalScreen
import com.zzyihao.stk.ui.components.StkInfoCard
import com.zzyihao.stk.ui.components.StkLogoutDialog
import com.zzyihao.stk.ui.components.StkPrimaryButton
import com.zzyihao.stk.ui.components.StkTopBar
import com.zzyihao.stk.ui.theme.StkColors
import com.zzyihao.stk.ui.theme.StkDimens
import kotlinx.coroutines.launch

enum class MeDestination(val title: String) {
    Profile("个人资料"), Member("会员中心"), Wallet("账户余额"), Props("道具中心"),
    History("浏览记录"), Favorites("我的收藏"), RealName("实名认证"), Support("联系客服"),
    Settings("设置"), About("关于商推客"), UserAgreement("用户协议"), PrivacyPolicy("隐私政策"),
}

private sealed interface AccountLoadState {
    data object Loading : AccountLoadState
    data class Ready(val overview: AccountOverview) : AccountLoadState
    data class Failed(val message: String) : AccountLoadState
}

@Composable
fun SecondaryScreen(
    destination: MeDestination,
    session: AuthSession,
    contentPadding: PaddingValues,
    accountRepository: AccountRepository,
    legalRepository: LegalRepository,
    releaseRepository: ReleaseRepository,
    onClearCache: () -> Unit,
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
    var accountRetry by rememberSaveable { mutableIntStateOf(0) }
    val accountState by produceState<AccountLoadState>(AccountLoadState.Loading, accountRepository, accountRetry) {
        value = runCatching { accountRepository.getOverview() }
            .fold({ AccountLoadState.Ready(it) }, { AccountLoadState.Failed(it.message ?: "账户信息加载失败") })
    }
    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        StkTopBar(
            title = destination.title,
            navigation = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
        )
        when (destination) {
            MeDestination.Profile -> AccountContent(accountState, { accountRetry++ }) { ProfilePage(session, it) }
            MeDestination.Member -> AccountContent(accountState, { accountRetry++ }) { MemberPage(it) }
            MeDestination.Wallet -> AccountContent(accountState, { accountRetry++ }) { WalletPage(it) }
            MeDestination.Props -> AccountContent(accountState, { accountRetry++ }) { PropsPage(it) }
            MeDestination.History -> EmptyPage("暂无浏览记录", accountState.placeholder("本期不建立个人浏览历史业务。"))
            MeDestination.Favorites -> EmptyPage("暂无收藏项目", accountState.placeholder("收藏能力尚未纳入当前版本合同。"))
            MeDestination.RealName -> EmptyPage("实名认证未开放", accountState.placeholder("本期不采集身份信息。"))
            MeDestination.Support -> AccountContent(accountState, { accountRetry++ }) { SupportPage(it) }
            MeDestination.Settings -> SettingsPage(onClearCache, onNavigate, onLogout)
            MeDestination.About -> AboutPage(releaseRepository)
            MeDestination.UserAgreement, MeDestination.PrivacyPolicy -> Unit
        }
    }
}

private fun AccountLoadState.placeholder(fallback: String): String =
    (this as? AccountLoadState.Ready)?.overview?.display?.placeholderMessage?.takeIf(String::isNotBlank) ?: fallback

@Composable
private fun AccountContent(state: AccountLoadState, onRetry: () -> Unit, content: @Composable (AccountOverview) -> Unit) {
    when (state) {
        AccountLoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = StkColors.BrandPrimary) }
        is AccountLoadState.Failed -> Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(state.message, color = StkColors.Error)
            TextButton(onClick = onRetry) { Text("重新加载") }
        }
        is AccountLoadState.Ready -> content(state.overview)
    }
}

@Composable
private fun ProfilePage(session: AuthSession, overview: AccountOverview) {
    Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        StkInfoCard { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(StkDimens.AvatarProfile).background(StkColors.BrandPrimarySoft, CircleShape), contentAlignment = Alignment.Center) { Text("商", style = MaterialTheme.typography.headlineLarge, color = StkColors.BrandPrimary) }; Column(Modifier.padding(start = StkDimens.SpaceMd)) { Text(overview.profile.username.ifBlank { session.user.username }, style = MaterialTheme.typography.titleLarge); Text(overview.profile.memberLabel, color = StkColors.TextSecondary) } } }
        ReadOnlyRow("UID", session.user.uid.toString())
        ReadOnlyRow("手机号", overview.profile.mobileMasked.ifBlank { session.user.mobileMasked })
        ReadOnlyRow("简介", overview.profile.bio)
        Text("个人资料为只读信息，编辑能力不在当前版本范围内。", color = StkColors.TextTertiary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MemberPage(overview: AccountOverview) {
    val membership = overview.membership
    LazyColumn(contentPadding = PaddingValues(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        item {
            val statusText = when (membership.status) {
                MembershipStatus.ACTIVE -> "有效期至 ${membership.expiresAt ?: "长期"}"
                MembershipStatus.EXPIRED -> "已于 ${membership.expiresAt ?: "--"} 到期"
                MembershipStatus.INACTIVE -> "暂未开通"
            }
            StkInfoCard { Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) { Text(overview.display.memberTitle, style = MaterialTheme.typography.titleLarge); Text(membership.label + " · " + membership.level, color = StkColors.TextSecondary); Text(statusText, color = StkColors.TextSecondary) } }
        }
        items(overview.benefits) { benefit -> StkInfoCard { Column { Text(benefit.title, style = MaterialTheme.typography.titleMedium); Text(benefit.description, color = StkColors.TextSecondary); Text(if (benefit.enabled) "已生效" else "未生效", color = if (benefit.enabled) StkColors.Success else StkColors.TextTertiary) } } }
        item { Text("本期会员能力仅展示，不提供支付、续费、折扣结算或返佣结算。", color = StkColors.TextTertiary, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun WalletPage(overview: AccountOverview) {
    Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        BalanceSection(overview.commission.title.ifBlank { overview.display.commissionLabel }, overview.commission.amount, overview.commission.description)
        BalanceSection(overview.tasks.title.ifBlank { overview.display.taskLabel }, overview.tasks.amount, overview.tasks.description)
        Text("账户为服务端只读展示，本期不提供提现、转账和流水。", color = StkColors.TextTertiary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BalanceSection(title: String, amount: String, description: String) {
    StkInfoCard { Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(amount, style = MaterialTheme.typography.headlineLarge, color = StkColors.BrandPrimary); Text(description, color = StkColors.TextSecondary) } }
}

@Composable
private fun PropsPage(overview: AccountOverview) {
    if (overview.props.isEmpty()) {
        EmptyPage("暂无可用道具", "道具目录由后台维护，本期不提供购买、库存或使用。")
        return
    }
    LazyColumn(contentPadding = PaddingValues(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        items(overview.props) { prop -> StkInfoCard { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(prop.title, style = MaterialTheme.typography.titleMedium); Text(prop.description, color = StkColors.TextSecondary) }; Text("数量 ${prop.quantity}", color = StkColors.BrandPrimary) } } }
        item { Text("道具仅展示，不提供购买、库存变更或使用。", color = StkColors.TextTertiary, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun SupportPage(overview: AccountOverview) {
    val context = LocalContext.current
    val support = overview.support
    var message by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        StkInfoCard { Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) { Text(support.label, style = MaterialTheme.typography.titleLarge); Text(support.value.ifBlank { "后台尚未配置客服值" }, color = StkColors.TextSecondary); Text(support.serviceHours, color = StkColors.TextTertiary) } }
        if (support.value.isNotBlank() && support.copyEnabled) StkPrimaryButton("复制客服信息", onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(support.label, support.value)); message = "已复制"
        })
        if (support.value.isNotBlank()) StkPrimaryButton("打开客服", onClick = { message = openSupport(context, support.type, support.value) })
        message?.let { Text(it, color = if (it.startsWith("无法")) StkColors.Error else StkColors.Success) }
    }
}

private fun openSupport(context: Context, type: String, value: String): String {
    val uri = when (type.lowercase()) {
        "phone" -> Uri.fromParts("tel", value, null)
        "qq" -> Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=${Uri.encode(value)}")
        "wechat" -> Uri.parse("weixin://")
        "email" -> Uri.fromParts("mailto", value, null)
        "url" -> Uri.parse(value).takeIf { it.scheme == "https" } ?: return "无法打开非 HTTPS 客服地址"
        else -> return "无法识别客服类型"
    }
    val action = if (type.lowercase() == "phone") Intent.ACTION_DIAL else Intent.ACTION_VIEW
    return runCatching { context.startActivity(Intent(action, uri)); "已打开外部应用" }.getOrElse { "无法打开对应应用，请复制客服信息" }
}

@Composable
private fun EmptyPage(title: String, description: String) {
    Column(Modifier.fillMaxSize().padding(StkDimens.SpaceBase), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        StkInfoCard { Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(description, color = StkColors.TextSecondary) } }
    }
}

private data class SettingsItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val tint: Color = StkColors.BrandPrimary,
    val onClick: () -> Unit,
)

@Composable
private fun SettingsPage(
    onClearCache: () -> Unit,
    onNavigate: (MeDestination) -> Unit,
    onLogout: suspend () -> Unit,
) {
    var status by remember { mutableStateOf<String?>(null) }
    var showLogout by rememberSaveable { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val rows = listOf(
        SettingsItem("清理缓存", "清除项目列表、分类和详情缓存", Icons.Default.DeleteOutline) { onClearCache(); status = "缓存已清理" },
        SettingsItem("用户协议", "查看服务端版本化协议", Icons.AutoMirrored.Filled.Article) { onNavigate(MeDestination.UserAgreement) },
        SettingsItem("隐私政策", "查看服务端版本化隐私政策", Icons.Default.Shield) { onNavigate(MeDestination.PrivacyPolicy) },
        SettingsItem("关于商推客", "V${BuildConfig.VERSION_NAME}", Icons.Default.Info) { onNavigate(MeDestination.About) },
        SettingsItem("退出登录", "清除本机登录状态", Icons.AutoMirrored.Filled.ExitToApp, StkColors.Error) { showLogout = true },
    )
    LazyColumn(contentPadding = PaddingValues(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) {
        items(rows) { row -> SettingRow(row) }
        status?.let { item { Text(it, color = StkColors.Success) } }
    }
    if (showLogout) {
        StkLogoutDialog(
            onDismiss = { showLogout = false },
            onConfirm = { showLogout = false; scope.launch { onLogout() } },
        )
    }
}

@Composable
private fun SettingRow(item: SettingsItem) {
    StkInfoCard(Modifier.clickable(onClick = item.onClick)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(StkDimens.AvatarList).background(item.tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(item.icon, contentDescription = null, tint = item.tint) }
            Column(Modifier.weight(1f).padding(start = StkDimens.SpaceMd)) {
                Text(item.label, style = MaterialTheme.typography.titleMedium, color = if (item.tint == StkColors.Error) StkColors.Error else StkColors.TextPrimary)
                Text(item.value, color = StkColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StkColors.TextTertiary)
        }
    }
}

@Composable
private fun AboutPage(releaseRepository: ReleaseRepository) {
    var retry by rememberSaveable { mutableIntStateOf(0) }
    val result by produceState<Result<ReleaseManifest>?>(null, releaseRepository, retry) {
        value = if (retry == 0) null else runCatching { releaseRepository.getCurrentRelease() }
    }
    val release = result?.getOrNull()
    val error = result?.exceptionOrNull()?.message
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(StkDimens.SpaceBase), verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) {
        StkInfoCard { Column(verticalArrangement = Arrangement.spacedBy(StkDimens.SpaceSm)) { Text("商推客", style = MaterialTheme.typography.headlineLarge); Text("当前版本：${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", color = StkColors.TextSecondary); Text("项目发现、发布与审核客户端", color = StkColors.TextSecondary) } }
        release?.let { ReadOnlyRow("线上版本", "${it.versionName} (${it.versionCode})") }
        error?.let { Text(it, color = StkColors.Error) }
        StkPrimaryButton("检查更新", onClick = { retry++ })
    }
}

@Composable
private fun ReadOnlyRow(label: String, value: String) {
    StkInfoCard { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StkDimens.SpaceMd)) { if (label.isNotBlank()) Text(label, color = StkColors.TextSecondary); Text(value, color = StkColors.TextPrimary) } }
}
