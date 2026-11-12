package com.nangalsanjh.app.ui.screens

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.nangalsanjh.app.viewmodel.AppTheme
import com.nangalsanjh.app.viewmodel.ThemeViewModel
import kotlinx.coroutines.delay
import kotlin.math.*

private class Branch(val at: Float, val angle: Float, val len: Float)
private class Tendril(
    val angle: Float, val len: Float, val delay: Float, val wave: Float,
    val freq: Float, val phase: Float, val width: Float, val branches: List<Branch>
)

/**
 * "Venom" theme switch: black, glossy symbiote-style tendrils crawl out of the TOP-RIGHT
 * corner, branch and creep across the screen while a viscous mass slowly floods in behind
 * them. Once everything is covered the theme is swapped underneath and the mass dissolves.
 * Slow on purpose (about 3.2 s spread + 0.8 s dissolve).
 */
@Composable
fun VenomThemeTransition(themeViewModel: ThemeViewModel) {
    val request by themeViewModel.transition.collectAsState()
    val progress = remember { Animatable(0f) }
    val fade = remember { Animatable(1f) }
    var active by remember { mutableStateOf<com.nangalsanjh.app.viewmodel.ThemeTransition?>(null) }
    val context = LocalContext.current

    // Fixed random layout so the pattern is the same every run
    val tendrils = remember {
        val rnd = kotlin.random.Random(11)
        List(17) { i ->
            val base = (PI / 2 - 0.12 + (PI / 2 + 0.24) * i / 16.0 + rnd.nextDouble(-0.07, 0.07)).toFloat()
            Tendril(
                angle = base,
                len = 0.55f + rnd.nextFloat() * 0.5f,
                delay = rnd.nextFloat() * 0.25f,
                wave = 0.04f + rnd.nextFloat() * 0.07f,
                freq = 1.5f + rnd.nextFloat() * 2.5f,
                phase = rnd.nextFloat() * 6.28f,
                width = 9f + rnd.nextFloat() * 14f,
                branches = List(2 + rnd.nextInt(2)) {
                    Branch(
                        at = 0.25f + rnd.nextFloat() * 0.5f,
                        angle = (if (rnd.nextBoolean()) 1 else -1) * (0.5f + rnd.nextFloat() * 0.6f),
                        len = 0.25f + rnd.nextFloat() * 0.3f
                    )
                }
            )
        }
    }

    LaunchedEffect(request) {
        val tr = request ?: return@LaunchedEffect
        val animScale = Settings.Global.getFloat(
            context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        )
        if (animScale == 0f) {
            themeViewModel.applyThemeNow(tr.target)
            themeViewModel.finishTransition()
            return@LaunchedEffect
        }
        active = tr
        fade.snapTo(1f)
        progress.snapTo(0f)
        progress.animateTo(1f, tween(3200, easing = FastOutSlowInEasing))
        themeViewModel.applyThemeNow(tr.target)
        withFrameNanos { }
        delay(250)
        fade.animateTo(0f, tween(800))
        active = null
        themeViewModel.finishTransition()
    }

    val tr = active ?: return
    val toDark = tr.target == AppTheme.DARK
    val fill = if (toDark) Color(0xFF05080F) else Color.White
    val rim = if (toDark) Color(0xFF8FB0F5) else Color(0xFF9DB8F0)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fade.value }
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        val w = size.width
        val h = size.height
        val o = Offset(w, 0f) // top-right corner
        val maxR = hypot(w, h) * 1.15f
        val p = progress.value
        val twoPi = (2 * PI).toFloat()

        fun smooth(x: Float): Float { val t = x.coerceIn(0f, 1f); return t * t * (3 - 2 * t) }

        // Tendril polyline points for a given growth 0..1 along its path
        fun pathPoint(start: Offset, ang: Float, length: Float, s: Float, wave: Float, freq: Float, ph: Float): Offset {
            val dx = cos(ang); val dy = sin(ang)
            val along = length * s
            val off = length * wave * sin(freq * s * twoPi * 0.5f + ph) * s
            return Offset(start.x + dx * along - dy * off, start.y + dy * along + dx * off)
        }

        // Pass lists so rims are drawn under all fills (merges overlaps into one mass)
        data class Seg(val a: Offset, val b: Offset, val wd: Float)
        val segs = ArrayList<Seg>(2500)

        fun addStroke(start: Offset, ang: Float, length: Float, grow: Float, wave: Float, freq: Float, ph: Float, wd: Float) {
            if (grow <= 0f) return
            val steps = 36
            var prev = start
            for (k in 1..steps) {
                val s = grow * k / steps
                val pt = pathPoint(start, ang, length, s, wave, freq, ph)
                val taper = 1f - 0.9f * (k.toFloat() / steps)
                segs.add(Seg(prev, pt, max(2.5f, wd * taper)))
                prev = pt
            }
        }

        for (t in tendrils) {
            val g = smooth((p * 1.5f - t.delay) / 1.0f)
            val L = maxR * t.len
            addStroke(o, t.angle, L, g, t.wave, t.freq, t.phase, t.width)
            for (b in t.branches) {
                if (g > b.at) {
                    val bs = pathPoint(o, t.angle, L, b.at, t.wave, t.freq, t.phase)
                    val bg = smooth((g - b.at) * 1.6f)
                    addStroke(bs, t.angle + b.angle, L * b.len, bg, t.wave * 1.3f, t.freq + 1f, t.phase + b.at * 5f, t.width * 0.55f)
                }
            }
        }

        // The slow mass behind the tendrils
        val mp = smooth((p - 0.22f) / 0.78f)
        val r0 = maxR * mp
        val path = Path()
        if (mp > 0f) {
            val steps = 160
            for (i in 0..steps) {
                val th = twoPi * i / steps
                val wob = 0.5f + 0.3f * sin(4 * th + p * 5f) + 0.2f * sin(11 * th - p * 7f)
                val r = r0 * (1f + 0.10f * (1f - mp * 0.7f) * wob)
                val pt = Offset(o.x + r * cos(th), o.y + r * sin(th))
                if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            path.close()
        }

        // Pass 1: glossy rim
        val rimA = 0.55f * (1f - p * 0.6f)
        for (s in segs) drawLine(rim.copy(alpha = rimA), s.a, s.b, s.wd + 6f, StrokeCap.Round)
        if (mp > 0f) drawPath(path, rim.copy(alpha = rimA), style = Stroke(width = 8f))
        // Pass 2: body
        for (s in segs) drawLine(fill, s.a, s.b, s.wd, StrokeCap.Round)
        if (mp > 0f) drawPath(path, fill)
    }
}

/** Remembers the on-screen centre of a view; used as the origin of the venom blob. */
@Composable
fun Modifier.captureScreenCenter(state: MutableState<Offset>): Modifier {
    val view = LocalView.current
    return this.onGloballyPositioned { c ->
        val loc = IntArray(2)
        view.getLocationOnScreen(loc)
        val r = c.positionInRoot()
        state.value = Offset(loc[0] + r.x + c.size.width / 2f, loc[1] + r.y + c.size.height / 2f)
    }
}
