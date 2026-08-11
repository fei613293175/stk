package com.zzyihao.stk.data.account

data class AccountOverview(
    val profile: ProfileCard,
    val membership: MembershipCard,
    val wallets: WalletOverview,
    val props: PropsOverview,
    val support: SupportCard,
) {
    val fromCache: Boolean
        get() = profile.fromCache || membership.fromCache || wallets.fromCache || props.fromCache || support.fromCache
}

data class ProfileCard(
    val uid: Long,
    val username: String,
    val mobileMasked: String,
    val memberLabel: String,
    val bio: String,
    val avatarUrl: String = "",
    val placeholderMessage: String = "功能筹备中",
    val fromCache: Boolean = false,
)

data class MembershipCard(
    val label: String,
    val level: String,
    val startsAt: String?,
    val expiresAt: String?,
    val status: MembershipStatus = MembershipStatus.INACTIVE,
    val title: String = "商推客会员",
    val openButtonText: String = "立即开通",
    val benefits: List<BenefitItem> = emptyList(),
    val showCard: Boolean = true,
    val fromCache: Boolean = false,
)

enum class MembershipStatus { INACTIVE, ACTIVE, EXPIRED, DISABLED }

data class BalanceCard(
    val title: String,
    val amount: String,
    val description: String,
)

data class WalletOverview(
    val commission: BalanceCard,
    val tasks: BalanceCard,
    val showWallets: Boolean = true,
    val fromCache: Boolean = false,
)

data class BenefitItem(val title: String, val description: String, val enabled: Boolean)

data class PropItem(
    val id: String,
    val title: String,
    val description: String,
    val iconUrl: String,
    val sortOrder: Int,
)

data class PropsOverview(
    val items: List<PropItem>,
    val showCenter: Boolean = true,
    val fromCache: Boolean = false,
)

data class SupportCard(
    val type: String,
    val label: String,
    val value: String,
    val serviceHours: String,
    val copyEnabled: Boolean,
    val allowedUrlHosts: Set<String> = emptySet(),
    val fromCache: Boolean = false,
)

interface AccountRepository {
    suspend fun getProfile(): ProfileCard
    suspend fun getMembership(): MembershipCard
    suspend fun getWallets(): WalletOverview
    suspend fun getProps(): PropsOverview
    suspend fun getSupport(): SupportCard
    suspend fun getOverview(): AccountOverview
    fun clearCache() = Unit
}

class AccountException(
    val code: Int,
    override val message: String,
    val requestId: String? = null,
) : Exception(message)
