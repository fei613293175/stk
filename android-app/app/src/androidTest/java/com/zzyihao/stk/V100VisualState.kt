package com.zzyihao.stk

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zzyihao.stk.designsystem.StkBrandMark
import com.zzyihao.stk.designsystem.StkSecondaryButton
import com.zzyihao.stk.designsystem.StkStatusMessage
import com.zzyihao.stk.designsystem.StkStatusTone
import com.zzyihao.stk.designsystem.StkTextField
import com.zzyihao.stk.designsystem.StkTokens

private fun stateNumber(stateId: String): Int = stateId.substringAfterLast("-S", "0").toIntOrNull() ?: 0

@Composable
internal fun V100VisualStateScreen(stateId: String) {
    when {
        stateId.startsWith("DS-") -> VisualDesignBoard(stateId)
        stateId.startsWith("SYS-001") -> VisualSplash(stateNumber(stateId))
        stateId.startsWith("SYS-002") -> VisualSystemResult(stateNumber(stateId))
        stateId.startsWith("COM-OV-002") -> VisualStage()
        stateId.startsWith("AUTH-001") -> VisualLogin(stateNumber(stateId))
        stateId.startsWith("AUTH-002") -> VisualRegister(stateNumber(stateId))
        stateId.startsWith("AUTH-003") -> VisualReset(stateNumber(stateId))
        stateId.startsWith("AUTH-004") -> VisualLegal("用户协议", stateNumber(stateId))
        stateId.startsWith("AUTH-005") -> VisualLegal("隐私政策", stateNumber(stateId))
        stateId.startsWith("AUTH-OV-001") -> VisualCaptcha(stateNumber(stateId))
        stateId.startsWith("AUTH-OV-002") -> VisualRisk(stateNumber(stateId))
        stateId.startsWith("HOME-001") -> VisualHome(stateNumber(stateId))
        stateId.startsWith("HOME-004") -> VisualDetail(stateNumber(stateId))
        stateId.startsWith("ME-001") -> VisualMe(stateNumber(stateId))
        stateId.startsWith("ME-OV-001") -> VisualLogout(stateNumber(stateId))
        else -> VisualSystemResult(2)
    }
}

@Composable
private fun VisualPage(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(modifier.fillMaxSize(), color = StkTokens.Background) { content() }
}

@Composable
private fun VisualAuthFrame(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = StkTokens.Space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StkTokens.Space12),
    ) {
        Spacer(Modifier.height(StkTokens.AuthTopGap))
        StkBrandMark()
        Text("欢迎使用商推客", style = StkTokens.Display)
        Text("登录后发现和发布推广项目", style = StkTokens.TitleLarge, color = StkTokens.TextSecondary)
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(StkTokens.FieldGap), content = content)
    }
}

@Composable
private fun VisualAuthTabs(sms: Boolean) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(StkTokens.Radius20)).border(1.dp, StkTokens.Border, RoundedCornerShape(StkTokens.Radius20)).padding(4.dp),
    ) {
        listOf("密码登录" to !sms, "短信登录" to sms).forEach { (label, selected) ->
            Box(
                Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(StkTokens.Radius16)).background(if (selected) StkTokens.Surface else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) { Text(label, style = StkTokens.TitleLarge, color = if (selected) StkTokens.BrandPrimary else StkTokens.TextSecondary) }
        }
    }
}

@Composable
private fun VisualLogin(number: Int) {
    val sms = number == 2 || number in 5..7 || number == 11 || number == 12
    val error = when (number) {
        4 -> "请输入正确的手机号和密码"
        10 -> "手机号或密码错误，请重试"
        11 -> "短信验证码错误，请重试"
        12 -> "短信验证码已过期，请重新发送"
        13 -> "操作过于频繁，请稍后再试"
        14 -> "账号暂时受限，请完成人工核验"
        15 -> "网络连接失败，请检查网络后重试"
        16 -> "请求超时，请稍后重试"
        else -> null
    }
    VisualAuthFrame {
        VisualAuthTabs(sms)
        StkTextField(if (number == 3) "13800138000" else "", {}, "手机号", Modifier.fillMaxWidth(), "login_visual_phone")
        if (sms) {
            StkTextField(if (number in 6..7 || number in 11..12) "123456" else "", {}, "短信验证码", Modifier.fillMaxWidth(), "login_visual_sms")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("${if (number == 6) "59" else "发送"} 秒后可重发", style = StkTokens.Caption, color = StkTokens.TextSecondary)
            }
        } else {
            StkTextField(if (number == 3) "Password123" else "", {}, "登录密码", Modifier.fillMaxWidth(), "login_visual_password")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Text("忘记密码", color = StkTokens.BrandPrimary, style = StkTokens.BodyMedium) }
        }
        if (error != null) StkStatusMessage("登录失败", error, StkStatusTone.Error)
        Button(onClick = {}, Modifier.fillMaxWidth().height(StkTokens.ButtonHeight), enabled = number !in setOf(5, 8)) {
            if (number == 5 || number == 8) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Text(if (number == 9) "登录成功" else "登录")
        }
        Text(if (number == 9) "登录成功，正在进入首页" else "还没有账号？立即注册", color = StkTokens.TextSecondary, style = StkTokens.Body)
        Text("登录即表示同意《用户协议》和《隐私政策》", color = StkTokens.TextTertiary, style = StkTokens.Caption)
    }
    if (number == 7) VisualCaptchaDialog("等待安全验证码", false)
}

@Composable
private fun VisualRegister(number: Int) {
    val error = when (number) {
        3 -> "该手机号已注册，请直接登录"
        4 -> "两次输入的密码不一致"
        5 -> "请先同意用户协议和隐私政策"
        6 -> "手机号或密码格式不正确"
        10 -> "注册操作过于频繁，请稍后再试"
        11 -> "网络连接失败，请稍后重试"
        12 -> "注册失败，请稍后重试"
        else -> null
    }
    VisualPage {
        Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) {
            Text("注册商推客", style = StkTokens.PageTitle)
            Text("手机号注册，不发送短信", color = StkTokens.TextSecondary, style = StkTokens.Body)
            StkTextField(if (number == 2) "13800138000" else "", {}, "手机号", Modifier.fillMaxWidth(), "register_visual_phone")
            StkTextField(if (number == 4) "Password123" else "", {}, "密码", Modifier.fillMaxWidth(), "register_visual_password")
            StkTextField(if (number == 4) "Password12" else "", {}, "确认密码", Modifier.fillMaxWidth(), "register_visual_confirm")
            Text(if (number == 5) "请同意用户协议和隐私政策" else "我已阅读并同意用户协议和隐私政策", color = if (number == 5) StkTokens.Error else StkTokens.TextSecondary)
            if (error != null) StkStatusMessage("注册失败", error, StkStatusTone.Error)
            Button(onClick = {}, Modifier.fillMaxWidth(), enabled = number != 8) { if (number == 8) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Text(if (number == 9) "注册成功" else "注册") }
            if (number == 9) StkStatusMessage("注册成功", "账户已创建，正在进入首页", StkStatusTone.Success)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Text("返回登录", color = StkTokens.BrandPrimary) }
        }
    }
    if (number == 7) VisualCaptchaDialog("注册安全验证", false)
}

@Composable
private fun VisualReset(number: Int) {
    val error = when (number) {
        5 -> "请检查短信验证码和新密码"
        8 -> "短信验证码已过期，请重新发送"
        9 -> "操作过于频繁，请稍后再试"
        10 -> "网络连接失败，请稍后重试"
        11 -> "密码重置失败，请稍后重试"
        else -> null
    }
    VisualPage {
        Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) {
            Text("找回密码", style = StkTokens.PageTitle)
            Text("通过短信验证码设置新的登录密码", color = StkTokens.TextSecondary)
            StkTextField(if (number == 2) "13800138000" else "", {}, "手机号", Modifier.fillMaxWidth(), "reset_visual_phone")
            OutlinedButton(onClick = {}, Modifier.fillMaxWidth()) { Text(if (number == 3) "发送中..." else if (number == 4) "59 秒后可重发" else "发送短信验证码") }
            StkTextField(if (number in 2..4) "123456" else "", {}, "短信验证码", Modifier.fillMaxWidth(), "reset_visual_sms")
            StkTextField("", {}, "新密码", Modifier.fillMaxWidth(), "reset_visual_password")
            StkTextField("", {}, "确认新密码", Modifier.fillMaxWidth(), "reset_visual_confirm")
            if (error != null) StkStatusMessage("重置失败", error, StkStatusTone.Error)
            Button(onClick = {}, Modifier.fillMaxWidth(), enabled = number != 6) { if (number == 6) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Text(if (number == 7) "重置成功" else "重置密码") }
            Text("返回登录", color = StkTokens.BrandPrimary)
        }
    }
    if (number == 2) VisualCaptchaDialog("安全验证", false)
}

@Composable
private fun VisualLegal(title: String, number: Int) {
    VisualPage {
        Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) {
            Text(title, style = StkTokens.PageTitle)
            when (number) {
                1 -> CircularProgressIndicator()
                2, 3 -> LazyColumn(Modifier.weight(1f)) { item { Text("商推客服务条款\n\n本页面展示当前版本的正式协议正文。协议版本由线上后台发布并记录，提交敏感操作前请阅读并确认。\n\n一、服务说明\n二、账户与安全\n三、隐私与数据保护", color = StkTokens.TextSecondary, style = StkTokens.Body) } }
                else -> StkStatusMessage("加载失败", "协议内容暂时无法获取，请检查网络后重试。", StkStatusTone.Error)
            }
            Button(onClick = {}, Modifier.fillMaxWidth()) { Text("返回") }
        }
    }
}

@Composable
private fun VisualCaptcha(number: Int) {
    VisualPage { Box(Modifier.fillMaxSize()) { Text("登录", Modifier.padding(StkTokens.Space24), style = StkTokens.PageTitle); VisualCaptchaDialog("安全验证码", number == 6) } }
}

@Composable
private fun VisualCaptchaDialog(title: String, verifying: Boolean) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) {
                Box(Modifier.fillMaxWidth().height(96.dp).background(StkTokens.BrandPrimarySoft, RoundedCornerShape(StkTokens.Radius8)), contentAlignment = Alignment.Center) { Text("4 8 2 6", style = StkTokens.Display, color = StkTokens.BrandPrimary) }
                StkTextField("", {}, "请输入图中验证码", Modifier.fillMaxWidth(), "visual_captcha_input")
                Text("验证码 60 秒内有效", color = StkTokens.TextSecondary, style = StkTokens.Caption)
            }
        },
        confirmButton = { Button(onClick = {}, enabled = !verifying) { if (verifying) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("确认") } },
        dismissButton = { TextButton(onClick = {}) { Text("取消") } },
    )
}

@Composable
private fun VisualRisk(number: Int) {
    VisualPage { Box(Modifier.fillMaxSize()) { VisualAuthFrame { Text("登录状态需要进一步确认", color = StkTokens.TextSecondary) }; VisualCaptchaDialog(if (number == 4) "登录方式暂不可用" else "账号安全提示", false) } }
}

@Composable
private fun VisualTopBar(me: Boolean = false) {
    if (me) {
        Box(Modifier.fillMaxWidth().height(136.dp).background(StkTokens.BrandPrimary).padding(horizontal = StkTokens.Space20), contentAlignment = Alignment.TopStart) {
            Text("我的", color = Color.White, style = StkTokens.Display)
            Text("*", Modifier.align(Alignment.TopEnd).padding(top = 8.dp), color = Color.White, style = StkTokens.TitleLarge)
        }
    } else {
        Row(Modifier.fillMaxWidth().height(56.dp).background(StkTokens.Surface).padding(horizontal = StkTokens.Space16), verticalAlignment = Alignment.CenterVertically) {
            StkBrandMark(Modifier.size(40.dp))
            Spacer(Modifier.width(StkTokens.Space12))
            Text("商推客", style = StkTokens.Display)
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(40.dp).background(StkTokens.BrandPrimarySoft, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) { Text("商", color = StkTokens.BrandPrimary, style = StkTokens.TitleLarge) }
        }
    }
}

@Composable
private fun VisualBottomNav(selected: Int = 0) { StkBottomBar(selected) {} }

@Composable
private fun VisualHome(number: Int) {
    val error = number in setOf(6, 10, 11, 12, 13)
    VisualPage {
        Column(Modifier.fillMaxSize()) {
            VisualTopBar()
            Column(Modifier.weight(1f).padding(horizontal = StkTokens.Space16), verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) {
                Spacer(Modifier.height(StkTokens.Space4))
                Box(Modifier.fillMaxWidth().height(52.dp).border(1.dp, StkTokens.Border, RoundedCornerShape(StkTokens.Radius12)).padding(horizontal = StkTokens.Space16), contentAlignment = Alignment.CenterStart) { Text("搜索项目标题或简介", color = StkTokens.TextTertiary, style = StkTokens.Body) }
                Row(horizontalArrangement = Arrangement.spacedBy(StkTokens.Space8)) { listOf("全部", "本地生活", "渠道推广", "企业服务").forEachIndexed { index, text -> Box(Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if (index == 0) StkTokens.BrandPrimary else StkTokens.Surface).border(1.dp, if (index == 0) StkTokens.BrandPrimary else StkTokens.Border, RoundedCornerShape(18.dp)).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) { Text(text, color = if (index == 0) Color.White else StkTokens.TextSecondary, style = StkTokens.BodyMedium) } } }
                when {
                    number == 1 -> repeat(3) { VisualProjectSkeleton() }
                    number == 7 -> StkStatusMessage("暂无项目", "当前没有可公开展示的推广项目。", StkStatusTone.Info)
                    error -> StkStatusMessage(if (number == 13) "登录状态已过期" else "项目加载失败", if (number == 11) "请求超时，请稍后重试。" else "无法获取项目列表，请稍后重试。", if (number == 12) StkStatusTone.Error else StkStatusTone.Warning)
                    else -> { VisualProjectCard(0); VisualProjectCard(1); VisualProjectCard(2); if (number == 5) { CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally)) } }
                }
            }
            VisualBottomNav(0)
        }
        if (number == 13) AlertDialog(onDismissRequest = {}, title = { Text("登录状态已过期") }, text = { Text("为保护账号安全，请重新登录后继续使用。") }, confirmButton = { Button(onClick = {}) { Text("重新登录") } })
    }
}

@Composable
private fun VisualProjectSkeleton() { Card(Modifier.fillMaxWidth().height(148.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = StkTokens.Surface)) { Row(Modifier.padding(StkTokens.Space12), horizontalArrangement = Arrangement.spacedBy(StkTokens.Space12)) { Box(Modifier.size(108.dp, 86.dp).background(StkTokens.SurfaceSecondary, RoundedCornerShape(StkTokens.Radius12))); Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Box(Modifier.width(180.dp).height(16.dp).background(StkTokens.SurfaceSecondary, RoundedCornerShape(6.dp))); Box(Modifier.width(150.dp).height(12.dp).background(StkTokens.SurfaceSecondary, RoundedCornerShape(6.dp))); Box(Modifier.width(100.dp).height(12.dp).background(StkTokens.SurfaceSecondary, RoundedCornerShape(6.dp))) } } } }

@Composable
private fun VisualProjectCard(index: Int) {
    val data = listOf("本地生活服务合作项目" to "用于效果图与自动测试的固定项目简介，不代表真实招商内容。", "社区团购渠道推广计划" to "固定测试数据，用于列表分页、搜索和详情状态。", "企业服务项目合作招募" to "固定测试数据，不产生任何真实联系、收益或承诺。")[index]
    Card(Modifier.fillMaxWidth().height(148.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = StkTokens.Surface)) {
        Row(Modifier.padding(StkTokens.Space12), horizontalArrangement = Arrangement.spacedBy(StkTokens.Space12)) {
            ProjectArt(index, Modifier.size(108.dp, 86.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(data.first, style = StkTokens.CardTitle); Text(data.second, style = StkTokens.Secondary, color = StkTokens.TextSecondary, maxLines = 2); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("商推客用户0${258 + index * 389}", style = StkTokens.Caption, color = StkTokens.TextSecondary); Text("${128 - index * 42}", style = StkTokens.Caption, color = StkTokens.TextTertiary) } }
        }
    }
}

@Composable
private fun ProjectArt(index: Int, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(StkTokens.Radius12)).background(Brush.linearGradient(listOf(if (index == 2) StkTokens.BrandAccent else StkTokens.BrandPrimary, StkTokens.BrandPrimaryPressed))), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) { drawCircle(Color.White.copy(alpha = .18f), size.minDimension * .18f, androidx.compose.ui.geometry.Offset(size.width * .78f, size.height * .18f)); val path = Path().apply { moveTo(size.width * .1f, size.height * .72f); lineTo(size.width * .42f, size.height * .46f); lineTo(size.width * .62f, size.height * .58f); lineTo(size.width * .82f, size.height * .24f) }; drawPath(path, Color.White, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)) }
        Column(Modifier.align(Alignment.BottomStart).padding(StkTokens.Space12)) { Text(if (index == 0) "本地生活" else if (index == 1) "渠道推广" else "企业服务", color = Color.White, fontWeight = FontWeight.Bold); Text("商推客项目", color = Color.White.copy(alpha = .88f), style = StkTokens.Caption) }
    }
}

@Composable
private fun VisualDetail(number: Int) {
    VisualPage {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().height(56.dp).background(StkTokens.Surface).padding(horizontal = StkTokens.Space16), verticalAlignment = Alignment.CenterVertically) { Text("<", style = StkTokens.TitleLarge); Spacer(Modifier.width(StkTokens.Space12)); Text("项目详情", style = StkTokens.PageTitle) }
            Column(Modifier.weight(1f).padding(StkTokens.Space16), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) {
                when (number) {
                    1 -> CircularProgressIndicator()
                    7 -> StkStatusMessage("图片加载失败", "项目图片暂时无法展示。", StkStatusTone.Warning)
                    8 -> StkStatusMessage("项目不存在", "该项目可能已被删除或下架。", StkStatusTone.Info)
                    9 -> StkStatusMessage("项目不可公开", "该项目当前不对外展示。", StkStatusTone.Warning)
                    10, 11 -> StkStatusMessage("详情加载失败", "无法获取项目详情，请稍后重试。", StkStatusTone.Error)
                    else -> { Text("本地生活服务合作项目", style = StkTokens.Display); Text("用于效果图与自动测试的固定项目简介，不代表真实招商内容。", color = StkTokens.TextSecondary, style = StkTokens.Body); ProjectArt(0, Modifier.fillMaxWidth().height(180.dp)); Text("项目浏览量 128", color = StkTokens.TextSecondary) }
                }
            }
            VisualBottomNav(0)
        }
    }
}

@Composable
private fun VisualMe(number: Int) {
    VisualPage {
        Column(Modifier.fillMaxSize()) {
            VisualTopBar(me = true)
            Column(Modifier.weight(1f).padding(horizontal = StkTokens.Space16), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) {
                Card(Modifier.fillMaxWidth(), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = StkTokens.Surface)) { Row(Modifier.padding(StkTokens.Space16), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(72.dp).background(StkTokens.BrandPrimarySoft, RoundedCornerShape(36.dp)), contentAlignment = Alignment.Center) { Text("商", color = StkTokens.BrandPrimary, style = StkTokens.Display) }; Spacer(Modifier.width(StkTokens.Space16)); Column { Text("商推客用户0001", style = StkTokens.TitleLarge); Text("UID 100001 · 138****5678", color = StkTokens.TextSecondary, style = StkTokens.Body) } } }
                if (number == 1) { VisualProjectSkeleton() } else if (number == 7) { StkStatusMessage("资料加载失败", "无法获取个人资料，请重试。", StkStatusTone.Error) } else if (number == 8) { StkStatusMessage("登录状态已过期", "请重新登录后查看个人资料。", StkStatusTone.Warning) } else { Text("常用入口", style = StkTokens.Display); VisualMeRow("我的发布", "V1.2.0 起开放项目管理", true); VisualMeRow("会员、账户与道具", "V1.3.0 起完善展示", false); StkStatusMessage("版本说明", "当前版本先提供基础资料展示，未开放入口不会无响应。", StkStatusTone.Info) }
            }
            VisualBottomNav(2)
        }
    }
}

@Composable
private fun VisualMeRow(title: String, subtitle: String, enabled: Boolean) { Row(Modifier.fillMaxWidth().height(76.dp).background(StkTokens.Surface, RoundedCornerShape(StkTokens.Radius16)).padding(StkTokens.Space12), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(StkTokens.BrandPrimarySoft, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) { Text("+", color = if (enabled) StkTokens.BrandPrimary else StkTokens.Disabled, style = StkTokens.TitleLarge) }; Spacer(Modifier.width(StkTokens.Space12)); Column(Modifier.weight(1f)) { Text(title, color = if (enabled) StkTokens.TextPrimary else StkTokens.Disabled, style = StkTokens.TitleLarge); Text(subtitle, color = StkTokens.TextSecondary, style = StkTokens.Caption) }; Text(">", color = StkTokens.TextTertiary, style = StkTokens.TitleLarge) } }

@Composable
private fun VisualLogout(number: Int) {
    VisualMe(2)
    AlertDialog(onDismissRequest = {}, title = { Text("确认退出登录？") }, text = { Text("退出后将清理本机登录状态。") }, confirmButton = { Button(onClick = {}) { if (number == 2) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Text("确认退出") } }, dismissButton = { TextButton(onClick = {}) { Text("取消") } })
}

@Composable
private fun VisualStage() {
    VisualPage { Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { Text("发布项目", style = StkTokens.PageTitle); StkStatusMessage("暂未开放", "项目发布将在后续版本开放，当前版本不会产生无响应按钮。", StkStatusTone.Info); Button(onClick = {}) { Text("知道了") } } }
}

@Composable
private fun VisualSplash(number: Int) {
    VisualPage { Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(StkTokens.Space12)) { Spacer(Modifier.height(160.dp)); StkBrandMark(); Text("商推客", style = StkTokens.Display); Text("发现项目 · 高效推广", color = StkTokens.TextSecondary); when (number) { 1 -> CircularProgressIndicator(); 2 -> StkStatusMessage("需要登录", "正在进入手机号登录页面", StkStatusTone.Info); 3 -> StkStatusMessage("准备就绪", "正在进入已缓存的首页内容", StkStatusTone.Success); else -> StkStatusMessage("启动配置加载失败", "没有出现白屏，您可以重新尝试", StkStatusTone.Error) } } }
}

@Composable
private fun VisualSystemResult(number: Int) {
    val title = listOf("系统维护中", "服务暂不可用", "当前没有网络", "系统出现异常")[number.coerceIn(1, 4) - 1]
    val detail = listOf("后台正在进行服务维护，请稍后再试。", "服务器暂时无法响应，请检查网络后重试。", "首次使用尚无缓存，请连接网络后继续。", "服务请求未完成，请使用请求编号联系管理员。")[number.coerceIn(1, 4) - 1]
    VisualPage { Column(Modifier.fillMaxSize().padding(StkTokens.Space24), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { Spacer(Modifier.height(160.dp)); Text("!", color = StkTokens.Warning, style = StkTokens.Display); Text(title, style = StkTokens.TitleLarge); Text(detail, color = StkTokens.TextSecondary); Button(onClick = {}) { Text("重新尝试") }; OutlinedButton(onClick = {}) { Text("返回") } } }
}

@Composable
private fun VisualDesignBoard(stateId: String) {
    VisualPage { Column(Modifier.fillMaxSize().padding(StkTokens.Space24), verticalArrangement = Arrangement.spacedBy(StkTokens.Space16)) { Text("STK-DS-1.0", style = StkTokens.Display); Text("商推客 Android 统一设计系统", color = StkTokens.TextSecondary); Text(if (stateId.startsWith("DS-001")) "颜色、字体、间距与圆角规范" else if (stateId.startsWith("DS-002")) "公共组件规范" else "全局状态与反馈", style = StkTokens.PageTitle); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(StkTokens.BrandPrimary, StkTokens.BrandAccent, StkTokens.Success, StkTokens.Warning, StkTokens.Error).forEach { Box(Modifier.size(52.dp).background(it, RoundedCornerShape(12.dp))) } }; repeat(5) { index -> Text("商推客 · 设计系统示例 ${index + 1}", style = if (index == 0) StkTokens.TitleLarge else StkTokens.Body, color = if (index == 0) StkTokens.TextPrimary else StkTokens.TextSecondary) } } }
}
