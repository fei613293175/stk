package com.zzyihao.stk.data.account

import com.zzyihao.stk.data.session.SessionState
import com.zzyihao.stk.data.session.TokenStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class FakeAccountRepository(private val tokenStore: TokenStore) : AccountRepository {
    override suspend fun getOverview(): AccountOverview {
        delay(120)
        val user = (tokenStore.session.first() as? SessionState.LoggedIn)?.session?.user
        return AccountOverview(
            profile = ProfileCard(user?.username ?: "商推客用户", user?.mobileMasked ?: "", user?.memberLabel ?: "普通用户", "欢迎使用商推客，完善资料有助于项目展示。"),
            membership = MembershipCard(user?.memberLabel ?: "普通会员", "L1", null, 0, MembershipStatus.INACTIVE),
            commission = BalanceCard("佣金账户", "¥0.00", "当前版本只展示账户余额，不执行结算或提现。"),
            tasks = BalanceCard("任务账户", "¥0.00", "当前版本只展示账户余额，不执行结算或提现。"),
            benefits = listOf(
                BenefitItem("项目展示", "可发布并参与审核", true),
                BenefitItem("会员专属标识", "展示会员等级标签", user?.memberLabel != "普通用户"),
                BenefitItem("优先审核", "后续版本开放", false),
            ),
            props = listOf(
                PropItem("置顶卡", "用于项目置顶展示，购买功能未开放。", 0),
                PropItem("曝光券", "用于增加项目曝光，购买功能未开放。", 0),
            ),
            support = SupportCard("wechat", "微信客服", "stk-service", "工作日 09:00-18:00", true),
            display = AccountDisplayConfig("商推客会员", "佣金账户", "任务账户", "功能筹备中"),
        )
    }
}
