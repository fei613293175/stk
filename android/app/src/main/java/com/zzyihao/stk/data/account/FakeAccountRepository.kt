package com.zzyihao.stk.data.account

import com.zzyihao.stk.data.session.SessionState
import com.zzyihao.stk.data.session.TokenStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class FakeAccountRepository(private val tokenStore: TokenStore) : AccountRepository {
    private suspend fun user() = (tokenStore.session.first() as? SessionState.LoggedIn)?.session?.user

    override suspend fun getProfile(): ProfileCard {
        delay(120)
        val user = user()
        return ProfileCard(
            uid = user?.uid ?: 10001,
            username = user?.username ?: "测试用户",
            mobileMasked = user?.mobileMasked ?: "138****8000",
            memberLabel = user?.memberLabel ?: "普通用户",
            bio = "资料由服务端只读提供。",
        )
    }

    override suspend fun getMembership() = MembershipCard(
        label = "普通用户",
        level = "L1",
        startsAt = null,
        expiresAt = null,
        status = MembershipStatus.INACTIVE,
        benefits = listOf(
            BenefitItem("消费权益", "消费 5 折（仅展示）", false),
            BenefitItem("推广权益", "消费返佣 40%（仅展示）", false),
        ),
    )

    override suspend fun getWallets() = WalletOverview(
        commission = BalanceCard("佣金账户", "0.00", "余额由服务端只读提供"),
        tasks = BalanceCard("任务账户", "0.00", "余额由服务端只读提供"),
    )

    override suspend fun getProps() = PropsOverview(
        items = listOf(
            PropItem("refresh", "刷新卡", "刷新项目展示时间", "", 1),
            PropItem("super_headline", "超级头条", "超级头条展示道具", "", 2),
            PropItem("headline", "头条", "头条展示道具", "", 3),
            PropItem("color", "变色卡", "项目标题变色展示", "", 4),
        ),
    )

    override suspend fun getSupport() = SupportCard(
        type = "wechat",
        label = "微信客服",
        value = "stk-service",
        serviceHours = "工作日 09:00-18:00",
        copyEnabled = true,
    )

    override suspend fun getOverview() = AccountOverview(
        profile = getProfile(),
        membership = getMembership(),
        wallets = getWallets(),
        props = getProps(),
        support = getSupport(),
    )
}
