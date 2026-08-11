package com.zzyihao.stk.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.zzyihao.stk.ui.theme.StkColors

@Composable
fun StkBackGlyph(modifier: Modifier = Modifier, tint: Color = StkColors.TextPrimary) {
    StkGlyphCanvas(modifier, "返回") {
        val stroke = Stroke(width = size.minDimension * .09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * .68f, size.height * .18f)
            lineTo(size.width * .34f, size.height * .50f)
            lineTo(size.width * .68f, size.height * .82f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
fun StkPhoneGlyph(modifier: Modifier = Modifier, tint: Color = StkColors.TextTertiary) {
    StkGlyphCanvas(modifier, null) {
        val stroke = Stroke(width = size.minDimension * .16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * .25f, size.height * .18f)
            cubicTo(size.width * .12f, size.height * .38f, size.width * .38f, size.height * .74f, size.width * .72f, size.height * .84f)
            cubicTo(size.width * .80f, size.height * .86f, size.width * .86f, size.height * .78f, size.width * .82f, size.height * .69f)
        }
        drawPath(path, tint, style = stroke)
        drawLine(tint, Offset(size.width * .24f, size.height * .18f), Offset(size.width * .39f, size.height * .31f), strokeWidth = size.minDimension * .16f, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * .68f, size.height * .72f), Offset(size.width * .82f, size.height * .69f), strokeWidth = size.minDimension * .16f, cap = StrokeCap.Round)
    }
}

@Composable
fun StkLockGlyph(modifier: Modifier = Modifier, tint: Color = StkColors.TextTertiary) {
    StkGlyphCanvas(modifier, null) {
        val strokeWidth = size.minDimension * .10f
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * .27f, size.height * .08f),
            size = Size(size.width * .46f, size.height * .52f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * .18f, size.height * .42f),
            size = Size(size.width * .64f, size.height * .46f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension * .08f),
        )
        drawCircle(StkColors.Surface, size.minDimension * .045f, Offset(size.width * .50f, size.height * .62f))
    }
}

@Composable
fun StkVisibilityGlyph(
    visible: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = StkColors.TextTertiary,
) {
    StkGlyphCanvas(modifier, if (visible) "隐藏密码" else "显示密码") {
        val strokeWidth = size.minDimension * .085f
        val eye = Path().apply {
            moveTo(size.width * .08f, size.height * .50f)
            cubicTo(size.width * .28f, size.height * .18f, size.width * .72f, size.height * .18f, size.width * .92f, size.height * .50f)
            cubicTo(size.width * .72f, size.height * .82f, size.width * .28f, size.height * .82f, size.width * .08f, size.height * .50f)
        }
        drawPath(eye, tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(tint, size.minDimension * .13f, Offset(size.width * .50f, size.height * .50f))
        if (visible) {
            drawLine(
                StkColors.Surface,
                Offset(size.width * .18f, size.height * .18f),
                Offset(size.width * .82f, size.height * .82f),
                strokeWidth = strokeWidth * .8f,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun StkErrorGlyph(modifier: Modifier = Modifier) {
    StkGlyphCanvas(modifier, null) {
        drawCircle(StkColors.Error, size.minDimension * .46f)
        drawLine(
            StkColors.Surface,
            Offset(size.width * .50f, size.height * .26f),
            Offset(size.width * .50f, size.height * .56f),
            strokeWidth = size.minDimension * .11f,
            cap = StrokeCap.Round,
        )
        drawCircle(StkColors.Surface, size.minDimension * .055f, Offset(size.width * .50f, size.height * .73f))
    }
}

@Composable
fun StkEmptyImageGlyph(modifier: Modifier = Modifier, tint: Color = StkColors.BrandPrimary) {
    StkGlyphCanvas(modifier, null) {
        val stroke = Stroke(width = size.minDimension * .08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * .10f, size.height * .18f),
            size = Size(size.width * .80f, size.height * .64f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension * .08f),
            style = stroke,
        )
        drawCircle(tint, size.minDimension * .075f, Offset(size.width * .32f, size.height * .38f))
        val mountain = Path().apply {
            moveTo(size.width * .18f, size.height * .72f)
            lineTo(size.width * .42f, size.height * .50f)
            lineTo(size.width * .57f, size.height * .64f)
            lineTo(size.width * .72f, size.height * .44f)
            lineTo(size.width * .86f, size.height * .72f)
        }
        drawPath(mountain, tint, style = stroke)
    }
}

@Composable
fun StkHomeGlyph(modifier: Modifier = Modifier, tint: Color) {
    StkGlyphCanvas(modifier, "首页") {
        val stroke = Stroke(width = size.minDimension * .085f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * .15f, size.height * .48f)
            lineTo(size.width * .50f, size.height * .18f)
            lineTo(size.width * .85f, size.height * .48f)
            moveTo(size.width * .25f, size.height * .43f)
            lineTo(size.width * .25f, size.height * .82f)
            lineTo(size.width * .75f, size.height * .82f)
            lineTo(size.width * .75f, size.height * .43f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
fun StkPublishGlyph(modifier: Modifier = Modifier, tint: Color) {
    StkGlyphCanvas(modifier, "发布") {
        val strokeWidth = size.minDimension * .085f
        drawLine(tint, Offset(size.width * .50f, size.height * .20f), Offset(size.width * .50f, size.height * .80f), strokeWidth, StrokeCap.Round)
        drawLine(tint, Offset(size.width * .20f, size.height * .50f), Offset(size.width * .80f, size.height * .50f), strokeWidth, StrokeCap.Round)
    }
}

@Composable
fun StkProfileGlyph(modifier: Modifier = Modifier, tint: Color, description: String? = "我的") {
    StkGlyphCanvas(modifier, description) {
        val stroke = Stroke(width = size.minDimension * .085f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(tint, size.minDimension * .16f, Offset(size.width * .50f, size.height * .30f), style = stroke)
        drawArc(
            color = tint,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * .18f, size.height * .45f),
            size = Size(size.width * .64f, size.height * .42f),
            style = stroke,
        )
    }
}

@Composable
fun StkSettingsGlyph(modifier: Modifier = Modifier, tint: Color = StkColors.Surface) {
    StkGlyphCanvas(modifier, "设置") {
        val center = Offset(size.width * .50f, size.height * .50f)
        val strokeWidth = size.minDimension * .075f
        drawCircle(tint, size.minDimension * .13f, center, style = Stroke(strokeWidth))
        repeat(8) { index ->
            val angle = Math.toRadians((index * 45.0) - 90.0)
            val inner = size.minDimension * .31f
            val outer = size.minDimension * .43f
            drawLine(
                tint,
                Offset(center.x + kotlin.math.cos(angle).toFloat() * inner, center.y + kotlin.math.sin(angle).toFloat() * inner),
                Offset(center.x + kotlin.math.cos(angle).toFloat() * outer, center.y + kotlin.math.sin(angle).toFloat() * outer),
                strokeWidth,
                StrokeCap.Round,
            )
        }
    }
}

@Composable
fun StkProjectListGlyph(modifier: Modifier = Modifier, tint: Color = StkColors.BrandPrimary) {
    StkGlyphCanvas(modifier, null) {
        val stroke = Stroke(width = size.minDimension * .08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * .20f, size.height * .12f),
            size = Size(size.width * .60f, size.height * .76f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension * .04f),
            style = stroke,
        )
        listOf(.34f, .50f, .66f).forEach { y ->
            drawLine(tint, Offset(size.width * .34f, size.height * y), Offset(size.width * .68f, size.height * y), size.minDimension * .07f, StrokeCap.Round)
        }
    }
}

@Composable
private fun StkGlyphCanvas(
    modifier: Modifier,
    description: String?,
    draw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit,
) {
    Canvas(
        modifier = if (description == null) modifier else modifier.semantics { contentDescription = description },
        onDraw = draw,
    )
}
