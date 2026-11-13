package com.nangalsanjh.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.drawscope.rotate
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
        rotate(deg, center) {
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

/** One-shot confetti burst; fires every time [trigger] changes to a new value > 0. Never blocks touches. */
@Composable
fun ConfettiBurst(trigger: Int, modifier: Modifier = Modifier) {
    val progress = androidx.compose.runtime.remember { Animatable(1f) }
    val palette = androidx.compose.runtime.remember {
        listOf(Color(0xFFE8590C), Color(0xFF3A7BFF), Color(0xFF19C3B1), Color(0xFF8E5BFF), Color(0xFFF59F00), Color(0xFFD92D3A))
    }
    class P(val a: Float, val v: Float, val c: Color, val s: Float, val spin: Float)
    val parts = androidx.compose.runtime.remember(trigger) {
        val r = kotlin.random.Random(trigger * 31 + 7)
        List(70) {
            P(
                a = (Math.PI * (0.15 + 0.7 * r.nextDouble())).toFloat(), // upward fan
                v = 0.55f + r.nextFloat() * 0.75f,
                c = palette[r.nextInt(palette.size)],
                s = 0.8f + r.nextFloat() * 1.1f,
                spin = (r.nextFloat() - 0.5f) * 14f
            )
        }
    }
    androidx.compose.runtime.LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(1900, easing = LinearEasing))
        }
    }
    if (trigger == 0 || progress.value >= 1f) return
    androidx.compose.foundation.Canvas(modifier = modifier.androidx_fillMax()) {
        val t = progress.value
        val ox = size.width / 2f
        val oy = size.height * 0.82f
        val speed = size.height * 0.95f
        val g = size.height * 1.5f
        for (p in parts) {
            val x = ox + kotlin.math.cos(p.a) * p.v * speed * t * 0.9f
            val y = oy - kotlin.math.sin(p.a) * p.v * speed * t + 0.5f * g * t * t
            val fade = (1f - ((t - 0.7f) / 0.3f)).coerceIn(0f, 1f)
            val w = 9f * p.s
            rotate(p.spin * t * 60f, Offset(x, y)) {
                drawRect(p.c.copy(alpha = fade), topLeft = Offset(x - w / 2, y - w / 3), size = androidx.compose.ui.geometry.Size(w, w * 0.6f))
            }
        }
    }
}

private fun Modifier.androidx_fillMax(): Modifier = this.then(Modifier.fillMaxSize())

/** Fade + slide in once per item (remembered across scrolling and rotation). */
@Composable
fun RevealOnce(key: Any, index: Int = 0, content: @Composable () -> Unit) {
    var seen by androidx.compose.runtime.saveable.rememberSaveable(key = key.toString()) {
        androidx.compose.runtime.mutableStateOf(false)
    }
    var visible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(seen) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!seen) {
            kotlinx.coroutines.delay(index.coerceIn(0, 6) * 60L)
            visible = true
            seen = true
        }
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.fadeIn(tween(320)) +
            androidx.compose.animation.slideInVertically(tween(320)) { it / 8 }
    ) { content() }
}

/** Slowly flowing accent gradient for titles, built on the normal text colour. */
@Composable
fun flowingTitleBrush(base: Color, accent1: Color = Color(0xFF3A7BFF), accent2: Color = Color(0xFF8E5BFF)): Brush {
    val shift by rememberInfiniteTransition(label = "title").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Reverse),
        label = "titleShift"
    )
    val start = -120f + shift * 260f
    return Brush.horizontalGradient(
        colors = listOf(base, base, accent1, accent2, base, base),
        startX = start, endX = start + 260f,
        tileMode = androidx.compose.ui.graphics.TileMode.Clamp
    )
}
