package com.radiogolha.mobile.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun GolhaPatternBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GolhaColors.ScreenBackground),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRepeatingStarPattern()
        }

        content()
    }
}

private fun DrawScope.drawRepeatingStarPattern() {
    val lineColor = GolhaColors.PrimaryAccent.copy(alpha = 0.105f)
    val detailColor = GolhaColors.PrimaryAccent.copy(alpha = 0.075f)
    val frameColor = GolhaColors.PrimaryAccent.copy(alpha = 0.055f)

    val majorStroke = Stroke(
        width = 2.2.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )
    val minorStroke = Stroke(
        width = 1.25.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )

    val tileWidth = min(size.width * 0.52f, size.height * 0.26f)
    val tileHeight = tileWidth * 1.72f
    val columns = (size.width / tileWidth).toInt() + 3
    val rows = (size.height / (tileHeight * 0.72f)).toInt() + 3

    for (row in -1..rows) {
        for (col in -1..columns) {
            val x = col * tileWidth + if (row % 2 == 0) 0f else tileWidth * 0.5f
            val y = row * tileHeight * 0.72f

            translate(x, y) {
                drawStarColumnTile(
                    tileWidth = tileWidth,
                    tileHeight = tileHeight,
                    lineColor = lineColor,
                    detailColor = detailColor,
                    frameColor = frameColor,
                    majorStroke = majorStroke,
                    minorStroke = minorStroke,
                )
            }
        }
    }
}

private fun DrawScope.drawStarColumnTile(
    tileWidth: Float,
    tileHeight: Float,
    lineColor: Color,
    detailColor: Color,
    frameColor: Color,
    majorStroke: Stroke,
    minorStroke: Stroke,
) {
    val centerX = tileWidth * 0.5f
    val topCenter = Offset(centerX, tileHeight * 0.32f)
    val bottomCenter = Offset(centerX, tileHeight * 0.84f)

    drawStarCluster(topCenter, tileWidth, lineColor, detailColor, majorStroke, minorStroke)
    drawStarCluster(bottomCenter, tileWidth, lineColor, detailColor, majorStroke, minorStroke)

    drawVerticalConnector(topCenter, bottomCenter, frameColor, minorStroke)
    drawSideStars(tileWidth, tileHeight, lineColor, minorStroke)
}

private fun DrawScope.drawStarCluster(
    center: Offset,
    tileWidth: Float,
    lineColor: Color,
    detailColor: Color,
    majorStroke: Stroke,
    minorStroke: Stroke,
) {
    drawTwelvePointStar(center, tileWidth * 0.16f, lineColor, majorStroke)

    repeat(6) { index ->
        rotate(index * 60f, center) {
            val arm = Path().apply {
                moveTo(center.x, center.y - tileWidth * 0.13f)
                lineTo(center.x, center.y - tileWidth * 0.24f)
                lineTo(center.x + tileWidth * 0.085f, center.y - tileWidth * 0.345f)
            }
            drawPath(arm, lineColor, style = majorStroke)

            val kiteCenter = Offset(center.x, center.y - tileWidth * 0.38f)
            drawKite(
                center = kiteCenter,
                width = tileWidth * 0.19f,
                height = tileWidth * 0.22f,
                color = detailColor,
                stroke = majorStroke,
            )
        }
    }

    scale(scaleX = 1.34f, scaleY = 1.34f, pivot = center) {
        val hex = polygonPoints(center, tileWidth * 0.19f, 6, -30f)
        drawClosedPath(hex, detailColor, minorStroke)
    }
}

private fun DrawScope.drawVerticalConnector(
    topCenter: Offset,
    bottomCenter: Offset,
    color: Color,
    stroke: Stroke,
) {
    val left = Path().apply {
        moveTo(topCenter.x - 1f, topCenter.y + topCenter.y * 0.02f)
        lineTo(topCenter.x - topCenter.y * 0.16f, topCenter.y + topCenter.y * 0.20f)
        lineTo(bottomCenter.x - bottomCenter.y * 0.11f, bottomCenter.y - bottomCenter.y * 0.22f)
        lineTo(bottomCenter.x, bottomCenter.y - bottomCenter.y * 0.04f)
    }
    val right = Path().apply {
        moveTo(topCenter.x + 1f, topCenter.y + topCenter.y * 0.02f)
        lineTo(topCenter.x + topCenter.y * 0.16f, topCenter.y + topCenter.y * 0.20f)
        lineTo(bottomCenter.x + bottomCenter.y * 0.11f, bottomCenter.y - bottomCenter.y * 0.22f)
        lineTo(bottomCenter.x, bottomCenter.y - bottomCenter.y * 0.04f)
    }
    drawPath(left, color, style = stroke)
    drawPath(right, color, style = stroke)
}

private fun DrawScope.drawSideStars(
    tileWidth: Float,
    tileHeight: Float,
    color: Color,
    stroke: Stroke,
) {
    val stars = listOf(
        Offset(tileWidth * 0.18f, tileHeight * 0.13f),
        Offset(tileWidth * 0.82f, tileHeight * 0.13f),
        Offset(tileWidth * 0.18f, tileHeight * 0.54f),
        Offset(tileWidth * 0.82f, tileHeight * 0.54f),
        Offset(tileWidth * 0.18f, tileHeight * 0.95f),
        Offset(tileWidth * 0.82f, tileHeight * 0.95f),
    )
    stars.forEach { starCenter ->
        drawSixPointStar(starCenter, tileWidth * 0.06f, color, stroke)
    }
}

private fun DrawScope.drawTwelvePointStar(
    center: Offset,
    radius: Float,
    color: Color,
    stroke: Stroke,
) {
    val points = List(24) { index ->
        val currentRadius = if (index % 2 == 0) radius else radius * 0.54f
        val angle = Math.toRadians((-90.0) + index * 15.0)
        Offset(
            x = center.x + (kotlin.math.cos(angle) * currentRadius).toFloat(),
            y = center.y + (kotlin.math.sin(angle) * currentRadius).toFloat(),
        )
    }
    drawClosedPath(points, color, stroke)
}

private fun DrawScope.drawSixPointStar(
    center: Offset,
    radius: Float,
    color: Color,
    stroke: Stroke,
) {
    val points = List(12) { index ->
        val currentRadius = if (index % 2 == 0) radius else radius * 0.46f
        val angle = Math.toRadians((-90.0) + index * 30.0)
        Offset(
            x = center.x + (kotlin.math.cos(angle) * currentRadius).toFloat(),
            y = center.y + (kotlin.math.sin(angle) * currentRadius).toFloat(),
        )
    }
    drawClosedPath(points, color, stroke)
}

private fun DrawScope.drawKite(
    center: Offset,
    width: Float,
    height: Float,
    color: Color,
    stroke: Stroke,
) {
    val path = Path().apply {
        moveTo(center.x, center.y - height * 0.5f)
        lineTo(center.x + width * 0.42f, center.y - height * 0.14f)
        lineTo(center.x + width * 0.32f, center.y + height * 0.46f)
        lineTo(center.x, center.y + height * 0.5f)
        lineTo(center.x - width * 0.32f, center.y + height * 0.46f)
        lineTo(center.x - width * 0.42f, center.y - height * 0.14f)
        close()
    }
    drawPath(path, color, style = stroke)
}

private fun DrawScope.drawClosedPath(
    points: List<Offset>,
    color: Color,
    stroke: Stroke,
) {
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { point -> lineTo(point.x, point.y) }
        close()
    }
    drawPath(path, color, style = stroke)
}

private fun polygonPoints(
    center: Offset,
    radius: Float,
    sides: Int,
    rotationDeg: Float = 0f,
): List<Offset> {
    return List(sides) { index ->
        val angle = Math.toRadians((rotationDeg + index * (360f / sides)).toDouble())
        Offset(
            x = center.x + (kotlin.math.cos(angle) * radius).toFloat(),
            y = center.y + (kotlin.math.sin(angle) * radius).toFloat(),
        )
    }
}
