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

/**
 * "Venom" theme switch: a blob of the NEW theme's colour oozes out from the tapped point,
 * throwing tendrils and droplets, floods the screen, the theme is swapped underneath,
 * then the goo fades away. Sits on top of the whole app.
 */
@Composable
fun VenomThemeTransition(themeViewModel: ThemeViewModel) {
    val request by themeViewModel.transition.collectAsState()
    val progress = remember { Animatable(0f) }
    val fade = remember { Animatable(1f) }
    var active by remember { mutableStateOf<com.nangalsanjh.app.viewmodel.ThemeTransition?>(null) }
    val context = LocalContext.current

    LaunchedEffect(request) {
        val tr = request ?: return@LaunchedEffect
        val animScale = Settings.Global.getFloat(
            context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        )
        if (animScale == 0f) {
            // Respect "remove animations": just switch
            themeViewModel.applyThemeNow(tr.target)
            themeViewModel.finishTransition()
            return@LaunchedEffect
        }
        active = tr
        fade.snapTo(1f)
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        themeViewModel.applyThemeNow(tr.target)
        withFrameNanos { }
        delay(80)
        fade.animateTo(0f, tween(400))
        active = null
        themeViewModel.finishTransition()
    }

    val tr = active ?: return
    val toDark = tr.target == AppTheme.DARK
    val fill = if (toDark) Color(0xFF0A1220) else Color.White
    val rim = if (toDark) Color(0xFF7FA6FF) else Color(0xFF9DB8F0)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fade.value }
            .pointerInput(Unit) { detectTapGestures { } } // block touches while animating
    ) {
        val w = size.width
        val h = size.height
        val o = Offset(tr.originX.coerceIn(0f, w), tr.originY.coerceIn(0f, h))
        val maxR = hypot(max(o.x, w - o.x), max(o.y, h - o.y)) * 1.2f
        val p = progress.value
        val r0 = maxR * p
        val phase = p * 9f
        val twoPi = (2 * PI).toFloat()
        val steps = 140
        val tendrils = 7
        val lobeAmp = 0.12f * (1f - p * 0.6f)
        val tendrilGain = 0.38f * sqrt(max(0f, 1f - p))

        val path = Path()
        for (i in 0..steps) {
            val th = twoPi * i / steps
            val wobble = 0.5f + 0.25f * sin(5 * th + phase) + 0.25f * sin(9 * th - phase * 1.4f)
            var bump = 0f
            for (j in 0 until tendrils) {
                val tj = j * twoPi / tendrils + 0.4f + 0.3f * sin(j * 3.1f)
                var d = (th - tj) % twoPi
                if (d > PI) d -= twoPi
                if (d < -PI) d += twoPi
                val k = d / 0.09f
                bump += tendrilGain * exp(-k * k) * (0.6f + 0.4f * sin(phase + j))
            }
            val r = r0 * (1f + lobeAmp * wobble + bump)
            val pt = Offset(o.x + r * cos(th), o.y + r * sin(th))
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        path.close()

        // droplets flung ahead of the blob
        if (p < 0.85f) {
            for (j in 0 until 9) {
                val a = j * twoPi / 9 + 0.9f * sin(j * 2.3f)
                val frac = (j % 3) / 2f
                val dist = r0 * (1.2f + 0.25f * frac) + 12f
                val rad = (7f + 9f * frac) * (1f - p)
                drawCircle(rim.copy(alpha = 0.5f), rad + 2f, Offset(o.x + dist * cos(a), o.y + dist * sin(a)))
                drawCircle(fill, rad, Offset(o.x + dist * cos(a), o.y + dist * sin(a)))
            }
        }

        drawPath(path, rim.copy(alpha = 0.55f * (1f - p * 0.5f)), style = Stroke(width = 7f))
        drawPath(path, fill)

        // glossy sheen that dies out as the screen fills, so the end frame is a flat colour
        if (r0 > 1f) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = (if (toDark) 0.10f else 0.0f) * (1f - p)), Color.Transparent),
                    center = Offset(o.x - r0 * 0.25f, o.y - r0 * 0.25f),
                    radius = r0 * 0.6f
                ),
                radius = r0 * 0.6f,
                center = Offset(o.x - r0 * 0.25f, o.y - r0 * 0.25f)
            )
        }
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
