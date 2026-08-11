package com.zzyihao.stk.data.account

data class AccountOverview(
    val profile: ProfileCard,
    val membership: MembershipCard,
    val commission: BalanceCard,
    val tasks: BalanceCard,
    val benefits: List<BenefitItem>,
    val props: List<PropItem>,
    val support: SupportCard,
    val display: AccountDisplayConfig,
)

data class ProfileCard(
    val username: String,
    val mobileMasked: String,
    val memberLabel: String,
    val bio: String,
)

data class MembershipCard(
    val label: String,
    val level: String,
    val expiresAt: String?,
    val progress: Int,
    val status: MembershipStatus = MembershipStatus.INACTIVE,
)

enum class MembershipStatus { INACTIVE, ACTIVE, EXPIRED }

data class BalanceCard(
    val title: String,
    val amount: String,
    val description: String,
)

data class BenefitItem(val title: String, val description: String, val enabled: Boolean)
data class PropItem(val title: String, val description: String, val quantity: Int)
data class SupportCard(
    val type: String,
    val label: String,
    val value: String,
    val serviceHours: String,
    val copyEnabled: Boolean,
)
data class AccountDisplayConfig(
    val memberTitle: String,
    val commissionLabel: String,
    val taskLabel: String,
    val placeholderMessage: String,
    val showWallets: Boolean = true,
    val showMemberCard: Boolean = true,
    val showPropsCenter: Boolean = true,
    val memberOpenButtonText: String = "了解会员",
)

interface AccountRepository {
    suspend fun getOverview(): AccountOverview
}

class AccountException(val code: Int, override val message: String) : Exception(message)
