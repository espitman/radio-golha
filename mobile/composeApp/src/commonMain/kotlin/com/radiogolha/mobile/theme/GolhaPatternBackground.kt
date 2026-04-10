package com.radiogolha.mobile.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

@Composable
fun GolhaPatternBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GolhaColors.ScreenBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val fill = GolhaColors.PrimaryAccent.copy(alpha = 0.12f)
            val stemStroke = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            )

            // ── MAIN VINE (left composition — like the image) ────────────────
            // Big S-curve from bottom-left rising to top-center
            val mainVine = Path().apply {
                moveTo(w * 0.22f, h * 1.02f)
                cubicTo(w * 0.10f, h * 0.82f, w * 0.38f, h * 0.68f, w * 0.30f, h * 0.52f)
                cubicTo(w * 0.22f, h * 0.36f, w * 0.05f, h * 0.28f, w * 0.18f, h * 0.14f)
                cubicTo(w * 0.30f, h * 0.00f, w * 0.52f, h * -0.04f, w * 0.55f, h * -0.08f)
            }
            drawPath(mainVine, fill, style = stemStroke)

            // ── SECONDARY RIGHT VINE ─────────────────────────────────────────
            val rightVine = Path().apply {
                moveTo(w * 0.70f, h * 1.02f)
                cubicTo(w * 0.80f, h * 0.80f, w * 0.55f, h * 0.65f, w * 0.62f, h * 0.50f)
                cubicTo(w * 0.68f, h * 0.35f, w * 0.85f, h * 0.28f, w * 0.80f, h * 0.15f)
            }
            drawPath(rightVine, fill, style = stemStroke)

            // Branch from main vine to right
            val branch1 = Path().apply {
                moveTo(w * 0.30f, h * 0.52f)
                cubicTo(w * 0.45f, h * 0.50f, w * 0.55f, h * 0.55f, w * 0.62f, h * 0.50f)
            }
            drawPath(branch1, fill, style = stemStroke)

            // Branch upper area
            val branch2 = Path().apply {
                moveTo(w * 0.18f, h * 0.26f)
                cubicTo(w * 0.30f, h * 0.22f, w * 0.55f, h * 0.20f, w * 0.62f, h * 0.18f)
            }
            drawPath(branch2, fill, style = stemStroke)

            // Lower branch right
            val branch3 = Path().apply {
                moveTo(w * 0.30f, h * 0.70f)
                cubicTo(w * 0.45f, h * 0.72f, w * 0.60f, h * 0.68f, w * 0.68f, h * 0.72f)
            }
            drawPath(branch3, fill, style = stemStroke)

            // ── FLOWERS ──────────────────────────────────────────────────────

            // Big central lotus — the hero flower (top left area)
            translate(w * 0.18f, h * 0.14f) {
                drawLotusBloom(fill, w * 0.22f)
            }

            // Large lotus right-center
            translate(w * 0.78f, h * 0.38f) {
                rotate(20f, Offset.Zero) {
                    drawLotusBloom(fill, w * 0.18f)
                }
            }

            // Mid-left tulip bud on branch
            translate(w * 0.30f, h * 0.52f) {
                rotate(-30f, Offset.Zero) {
                    drawTulipBud(fill, w * 0.13f)
                }
            }

            // Lower-right tulip bud
            translate(w * 0.68f, h * 0.72f) {
                rotate(25f, Offset.Zero) {
                    drawTulipBud(fill, w * 0.11f)
                }
            }

            // Upper branch bud
            translate(w * 0.62f, h * 0.18f) {
                rotate(-15f, Offset.Zero) {
                    drawTulipBud(fill, w * 0.10f)
                }
            }

            // ── LARGE FEATHER LEAVES ─────────────────────────────────────────

            // Left of hero flower
            translate(w * 0.06f, h * 0.18f) {
                rotate(-55f, Offset.Zero) { drawFeatherLeaf(fill, w * 0.20f) }
            }

            // Right of hero flower
            translate(w * 0.35f, h * 0.12f) {
                rotate(45f, Offset.Zero) { drawFeatherLeaf(fill, w * 0.18f) }
            }

            // Left mid
            translate(w * 0.06f, h * 0.48f) {
                rotate(-40f, Offset.Zero) { drawFeatherLeaf(fill, w * 0.18f) }
            }

            // Right mid-lower
            translate(w * 0.92f, h * 0.55f) {
                rotate(50f, Offset.Zero) { drawFeatherLeaf(fill, w * 0.17f) }
            }

            // Lower left
            translate(w * 0.10f, h * 0.75f) {
                rotate(-60f, Offset.Zero) { drawFeatherLeaf(fill, w * 0.16f) }
            }

            // Lower right
            translate(w * 0.82f, h * 0.78f) {
                rotate(35f, Offset.Zero) { drawFeatherLeaf(fill, w * 0.15f) }
            }

            // Bottom center
            translate(w * 0.45f, h * 0.90f) {
                rotate(10f, Offset.Zero) { drawFeatherLeaf(fill, w * 0.14f) }
            }

            // ── PAISLEY / BOTEH ──────────────────────────────────────────────

            translate(w * 0.62f, h * 0.50f) {
                rotate(30f, Offset.Zero) { drawPaisleyBoteh(fill, w * 0.13f) }
            }

            translate(w * 0.14f, h * 0.35f) {
                rotate(-25f, Offset.Zero) { drawPaisleyBoteh(fill, w * 0.11f) }
            }

            translate(w * 0.80f, h * 0.15f) {
                rotate(15f, Offset.Zero) { drawPaisleyBoteh(fill, w * 0.10f) }
            }

            translate(w * 0.35f, h * 0.80f) {
                rotate(-40f, Offset.Zero) { drawPaisleyBoteh(fill, w * 0.12f) }
            }

            // ── SCATTERED SMALL FLOWERS ──────────────────────────────────────
            drawSmallFlower(fill, w * 0.048f, Offset(w * 0.50f, h * 0.30f))
            drawSmallFlower(fill, w * 0.042f, Offset(w * 0.88f, h * 0.25f))
            drawSmallFlower(fill, w * 0.040f, Offset(w * 0.22f, h * 0.62f))
            drawSmallFlower(fill, w * 0.044f, Offset(w * 0.55f, h * 0.66f))
            drawSmallFlower(fill, w * 0.038f, Offset(w * 0.78f, h * 0.88f))
            drawSmallFlower(fill, w * 0.036f, Offset(w * 0.12f, h * 0.92f))
            drawSmallFlower(fill, w * 0.042f, Offset(w * 0.90f, h * 0.70f))

            // ── CURLING TENDRILS ─────────────────────────────────────────────
            val tendrils = listOf(
                Triple(Offset(w * 0.22f, h * 0.80f), Offset(w * 0.05f, h * 0.75f), Offset(w * 0.04f, h * 0.67f)),
                Triple(Offset(w * 0.30f, h * 0.38f), Offset(w * 0.12f, h * 0.32f), Offset(w * 0.08f, h * 0.25f)),
                Triple(Offset(w * 0.62f, h * 0.34f), Offset(w * 0.78f, h * 0.32f), Offset(w * 0.85f, h * 0.24f)),
                Triple(Offset(w * 0.70f, h * 0.62f), Offset(w * 0.88f, h * 0.60f), Offset(w * 0.94f, h * 0.54f)),
            )
            val tendrilStroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            tendrils.forEach { (start, ctrl, end) ->
                val t = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(ctrl.x, ctrl.y, ctrl.x, ctrl.y, end.x, end.y)
                }
                drawPath(t, fill, style = tendrilStroke)
                // Small curl at end
                val curl = Path().apply {
                    moveTo(end.x, end.y)
                    cubicTo(
                        end.x + (end.x - ctrl.x) * 0.5f, end.y + (end.y - ctrl.y) * 0.5f,
                        end.x + (end.x - ctrl.x) * 0.8f, end.y - (end.y - ctrl.y) * 0.2f,
                        end.x + (end.x - ctrl.x) * 0.3f, end.y - (end.y - ctrl.y) * 0.6f
                    )
                }
                drawPath(curl, fill, style = tendrilStroke)
            }
        }

        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lotus / Mughal Bloom — multi-layered petals like the large flower in the image
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawLotusBloom(color: Color, r: Float) {
    // Outer 8 petals
    repeat(8) { i ->
        rotate(i * 45f, Offset.Zero) {
            val p = Path().apply {
                moveTo(0f, 0f)
                cubicTo(-r * 0.30f, -r * 0.30f, -r * 0.28f, -r * 0.82f, 0f, -r)
                cubicTo(r * 0.28f, -r * 0.82f, r * 0.30f, -r * 0.30f, 0f, 0f)
            }
            drawPath(p, color)
        }
    }
    // Inner 8 narrower petals offset 22.5
    repeat(8) { i ->
        rotate(i * 45f + 22.5f, Offset.Zero) {
            val p = Path().apply {
                moveTo(0f, r * 0.10f)
                cubicTo(-r * 0.18f, -r * 0.20f, -r * 0.16f, -r * 0.60f, 0f, -r * 0.72f)
                cubicTo(r * 0.16f, -r * 0.60f, r * 0.18f, -r * 0.20f, 0f, r * 0.10f)
            }
            drawPath(p, color)
        }
    }
    // Innermost tight petals
    repeat(6) { i ->
        rotate(i * 60f, Offset.Zero) {
            val p = Path().apply {
                moveTo(0f, r * 0.08f)
                cubicTo(-r * 0.10f, -r * 0.05f, -r * 0.10f, -r * 0.38f, 0f, -r * 0.46f)
                cubicTo(r * 0.10f, -r * 0.38f, r * 0.10f, -r * 0.05f, 0f, r * 0.08f)
            }
            drawPath(p, color)
        }
    }
    // Centre disk
    drawCircle(color, radius = r * 0.18f, center = Offset.Zero)
}

// ─────────────────────────────────────────────────────────────────────────────
// Feather Leaf — the long multi-lobed leaf prominent in the image
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawFeatherLeaf(color: Color, size: Float) {
    // Main solid body
    val body = Path().apply {
        moveTo(0f, 0f)
        cubicTo(-size * 0.40f, -size * 0.35f, -size * 0.38f, -size * 0.78f, 0f, -size)
        cubicTo(size * 0.38f, -size * 0.78f, size * 0.40f, -size * 0.35f, 0f, 0f)
    }
    drawPath(body, color)

    // Negative lobes cut into left edge (gives feathery feel)
    val bg = GolhaColors.ScreenBackground.copy(alpha = 0.60f)
    listOf(0.22f, 0.40f, 0.56f, 0.70f, 0.82f).forEach { t ->
        val cx = -size * 0.34f * (1f - t * 0.35f)
        val cy = -size * t
        val lSz = size * 0.12f * (1f - t * 0.25f)
        val lobe = Path().apply {
            moveTo(cx + lSz * 0.3f, cy + lSz * 0.5f)
            cubicTo(cx - lSz * 1.5f, cy + lSz * 0.3f, cx - lSz * 1.8f, cy - lSz, cx - lSz * 0.5f, cy - lSz * 1.8f)
            cubicTo(cx + lSz * 0.5f, cy - lSz * 2.0f, cx + lSz * 0.8f, cy - lSz, cx + lSz * 0.3f, cy + lSz * 0.5f)
        }
        drawPath(lobe, bg)
    }
    // Mirror on right
    listOf(0.22f, 0.40f, 0.56f, 0.70f, 0.82f).forEach { t ->
        val cx = size * 0.34f * (1f - t * 0.35f)
        val cy = -size * t
        val lSz = size * 0.12f * (1f - t * 0.25f)
        val lobe = Path().apply {
            moveTo(cx - lSz * 0.3f, cy + lSz * 0.5f)
            cubicTo(cx + lSz * 1.5f, cy + lSz * 0.3f, cx + lSz * 1.8f, cy - lSz, cx + lSz * 0.5f, cy - lSz * 1.8f)
            cubicTo(cx - lSz * 0.5f, cy - lSz * 2.0f, cx - lSz * 0.8f, cy - lSz, cx - lSz * 0.3f, cy + lSz * 0.5f)
        }
        drawPath(lobe, bg)
    }
    // Midrib vein
    val vein = Path().apply {
        moveTo(0f, 0f)
        lineTo(0f, -size * 0.92f)
    }
    drawPath(vein, color, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
}

// ─────────────────────────────────────────────────────────────────────────────
// Paisley / Boteh — با جزئیات داخلی
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawPaisleyBoteh(color: Color, r: Float) {
    // Outer body
    val body = Path().apply {
        moveTo(0f, 0f)
        cubicTo(-r * 0.55f, -r * 0.10f, -r * 0.70f, -r * 0.65f, -r * 0.38f, -r * 1.0f)
        cubicTo(-r * 0.08f, -r * 1.35f, r * 0.52f, -r * 1.28f, r * 0.60f, -r * 0.90f)
        cubicTo(r * 0.68f, -r * 0.52f, r * 0.42f, -r * 0.12f, r * 0.22f, r * 0.04f)
        cubicTo(r * 0.10f, r * 0.14f, -r * 0.10f, r * 0.16f, 0f, 0f)
    }
    drawPath(body, color)

    // Inner negative for detail
    val bg = GolhaColors.ScreenBackground.copy(alpha = 0.55f)
    val inner = Path().apply {
        moveTo(-r * 0.02f, -r * 0.14f)
        cubicTo(-r * 0.32f, -r * 0.22f, -r * 0.44f, -r * 0.60f, -r * 0.22f, -r * 0.88f)
        cubicTo(-r * 0.02f, -r * 1.12f, r * 0.32f, -r * 1.08f, r * 0.38f, -r * 0.84f)
        cubicTo(r * 0.44f, -r * 0.60f, r * 0.26f, -r * 0.26f, r * 0.12f, -r * 0.14f)
        cubicTo(r * 0.04f, -r * 0.06f, -r * 0.06f, -r * 0.08f, -r * 0.02f, -r * 0.14f)
    }
    drawPath(inner, bg)

    // Small inner petal detail
    val detail = Path().apply {
        moveTo(-r * 0.06f, -r * 0.38f)
        cubicTo(-r * 0.20f, -r * 0.44f, -r * 0.24f, -r * 0.68f, -r * 0.10f, -r * 0.78f)
        cubicTo(r * 0.06f, -r * 0.88f, r * 0.20f, -r * 0.76f, r * 0.18f, -r * 0.60f)
        cubicTo(r * 0.14f, -r * 0.44f, r * 0.02f, -r * 0.36f, -r * 0.06f, -r * 0.38f)
    }
    drawPath(detail, color)
}

// ─────────────────────────────────────────────────────────────────────────────
// Tulip / Carnation Bud — three-petal closed bud with calyx
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawTulipBud(color: Color, r: Float) {
    val centre = Path().apply {
        moveTo(0f, r * 0.05f)
        cubicTo(-r * 0.22f, -r * 0.10f, -r * 0.20f, -r * 0.72f, 0f, -r * 0.92f)
        cubicTo(r * 0.20f, -r * 0.72f, r * 0.22f, -r * 0.10f, 0f, r * 0.05f)
    }
    drawPath(centre, color)

    val left = Path().apply {
        moveTo(-r * 0.06f, -r * 0.05f)
        cubicTo(-r * 0.45f, -r * 0.10f, -r * 0.58f, -r * 0.52f, -r * 0.38f, -r * 0.83f)
        cubicTo(-r * 0.22f, -r * 1.02f, -r * 0.06f, -r * 0.90f, 0f, -r * 0.76f)
        lineTo(-r * 0.06f, -r * 0.05f)
    }
    drawPath(left, color)

    val right = Path().apply {
        moveTo(r * 0.06f, -r * 0.05f)
        cubicTo(r * 0.45f, -r * 0.10f, r * 0.58f, -r * 0.52f, r * 0.38f, -r * 0.83f)
        cubicTo(r * 0.22f, -r * 1.02f, r * 0.06f, -r * 0.90f, 0f, -r * 0.76f)
        lineTo(r * 0.06f, -r * 0.05f)
    }
    drawPath(right, color)

    // Calyx sepals
    val calyxL = Path().apply {
        moveTo(0f, r * 0.05f)
        cubicTo(-r * 0.30f, r * 0.08f, -r * 0.42f, r * 0.28f, -r * 0.22f, r * 0.38f)
        cubicTo(-r * 0.10f, r * 0.44f, 0f, r * 0.35f, 0f, r * 0.22f)
    }
    drawPath(calyxL, color)
    val calyxR = Path().apply {
        moveTo(0f, r * 0.05f)
        cubicTo(r * 0.30f, r * 0.08f, r * 0.42f, r * 0.28f, r * 0.22f, r * 0.38f)
        cubicTo(r * 0.10f, r * 0.44f, 0f, r * 0.35f, 0f, r * 0.22f)
    }
    drawPath(calyxR, color)

    // Stem
    val stem = Path().apply {
        moveTo(-r * 0.038f, r * 0.35f)
        lineTo(-r * 0.038f, r * 0.72f)
        lineTo(r * 0.038f, r * 0.72f)
        lineTo(r * 0.038f, r * 0.35f)
        close()
    }
    drawPath(stem, color)
}

// ─────────────────────────────────────────────────────────────────────────────
// Small scattered flower — tiny 6-petal
// ─────────────────────────────────────────────────────────────────────────────
private fun DrawScope.drawSmallFlower(color: Color, r: Float, pos: Offset) {
    translate(pos.x, pos.y) {
        repeat(6) { i ->
            rotate(i * 60f, Offset.Zero) {
                val petal = Path().apply {
                    moveTo(0f, r * 0.22f)
                    cubicTo(-r * 0.32f, r * 0.30f, -r * 0.32f, r * 0.95f, 0f, r * 1.0f)
                    cubicTo(r * 0.32f, r * 0.95f, r * 0.32f, r * 0.30f, 0f, r * 0.22f)
                }
                drawPath(petal, color)
            }
        }
        drawCircle(color, radius = r * 0.30f, center = Offset.Zero)
    }
}
