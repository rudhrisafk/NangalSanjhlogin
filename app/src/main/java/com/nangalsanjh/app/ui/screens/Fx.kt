package com.nangalsanjh.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/** Slowly drifting soft colour blobs behind content (aurora). Redraws only, no recomposition. */
@Composable
fun Modifier.aurora(colors: List<Color>, periodMs: Int = 14000): Modifier {
    val t by rememberInfiniteTransition(label = "aurora").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = LinearEasing)),
        label = "auroraT"
    )
    return this.drawBehind {
        val tau = (2 * Math.PI).toFloat()
        colors.forEachIndexed { i, c ->
            val cx = size.width * (0.5f + 0.42f * sin(tau * t + i * 2.1f))
            val cy = size.height * (0.5f + 0.42f * cos(tau * t * (if (i % 2 == 0) 1f else -1f) + i * 1.7f))
            val r = max(size.width, size.height) * 0.55f
            drawCircle(
                brush = Brush.radialGradient(listOf(c, Color.Transparent), center = Offset(cx, cy), radius = r),
                radius = r,
                center = Offset(cx, cy)
            )
        }
    }
}

/** A soft light band that sweeps across the element every few seconds. Clip the element first. */
@Composable
fun Modifier.shimmerSweep(periodMs: Int = 3400): Modifier {
    val t by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = LinearEasing)),
        label = "shimmerT"
    )
    return this.drawWithContent {
        drawContent()
        val k = (t / 0.55f).coerceIn(0f, 1f) // sweep in first 55 %, rest is pause
        if (k in 0.001f..0.999f) {
            val x = -0.3f * size.width + k * 1.6f * size.width
            drawRect(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.32f), Color.Transparent),
                    startX = x - size.width * 0.2f, endX = x + size.width * 0.2f
                )
            )
        }
    }
}

/** Expanding, fading ring behind an element (SOS / live indicators). Do not clip before this. */
@Composable
fun Modifier.pulseRing(color: Color, periodMs: Int = 1700): Modifier {
    val t by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = FastOutSlowInEasing)),
        label = "pulseT"
    )
    return this.drawBehind {
        val base = size.minDimension / 2f
        drawCircle(
            color = color.copy(alpha = 0.55f * (1f - t)),
            radius = base * (1f + 0.55f * t),
            style = Stroke(width = 3f + 3f * (1f - t))
        )
    }
}

/** Counts up to [target] when first shown / when it changes. */
@Composable
fun animatedInt(target: Int, durationMs: Int = 900): Int =
    animateIntAsState(target, tween(durationMs, easing = FastOutSlowInEasing), label = "count").value

/** Rotating multi-colour ring around a round element (e.g. the AI button). Draws outside the bounds. */
@Composable
fun Modifier.spinningRing(colors: List<Color>, ringWidthPx: Float = 7f, gapPx: Float = 6f, periodMs: Int = 3600): Modifier {
    val deg by rememberInfiniteTransition(label = "ring").animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = LinearEasing)),
        label = "ringDeg"
    )
    return this.drawBehind {
        androidx.compose.ui.graphics.drawscope.rotate(deg, center) {
            drawCircle(
                brush = Brush.sweepGradient(colors + colors.first(), center),
                radius = size.minDimension / 2f + gapPx,
                style = Stroke(width = ringWidthPx)
            )
        }
    }
}

/** Gentle repeating scale "breathing" value 1f..[to]; read it inside graphicsLayer/scale. */
@Composable
fun breathing(to: Float = 1.12f, periodMs: Int = 1800): Float {
    val v by rememberInfiniteTransition(label = "breath").animateFloat(
        initialValue = 1f, targetValue = to,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathV"
    )
    return v
}
