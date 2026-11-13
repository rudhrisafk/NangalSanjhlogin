package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nangalsanjh.app.theme.Radius
import com.nangalsanjh.app.theme.Spacing

/** Shared page header: small saffron eyebrow, big title, muted subtitle. */
@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = Spacing.lg
) {
    var shown by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) { shown = true }
    val a by androidx.compose.animation.core.animateFloatAsState(
        if (shown) 1f else 0f, androidx.compose.animation.core.tween(500), label = "headerIn"
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = a; translationX = (1f - a) * -24f }
            .padding(horizontal = horizontalPadding, vertical = Spacing.md)
    ) {
        Text(
            eyebrow,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        if (subtitle.isNotEmpty()) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Pill-style segmented tabs (replaces the stock underlined TabRow). */
@Composable
fun SegmentedTabs(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
            .clip(Radius.full)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        labels.forEachIndexed { i, label ->
            val isSel = i == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(Radius.full)
                    .background(if (isSel) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent)
                    .then(if (isSel) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, Radius.full) else Modifier)
                    .clickable(role = Role.Tab) { onSelect(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


/** Neutral (navy) selected state for chips so saffron stays a rare accent. */
@Composable
fun neutralChipColors() = androidx.compose.material3.FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.onBackground,
    selectedLabelColor = MaterialTheme.colorScheme.background,
    selectedLeadingIconColor = MaterialTheme.colorScheme.background
)

/** Icon-based empty state (no emoji). */
@Composable
fun EmptyBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val floatY by androidx.compose.animation.core.rememberInfiniteTransition(label = "float").animateFloat(
            initialValue = -5f, targetValue = 5f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                androidx.compose.animation.core.tween(1800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "floatY"
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer { translationY = floatY * density }
                .pulseRing(MaterialTheme.colorScheme.outline, periodMs = 2600)
                .clip(Radius.full)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(Spacing.xs))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}


/**
 * Glassmorphism for dark/gradient backgrounds: translucent white fill plus a
 * soft light border. Use only on top of a coloured surface (hero, splash) so the
 * "frosted" effect is actually visible.
 */
fun Modifier.glass(
    shape: androidx.compose.ui.graphics.Shape,
    fillAlpha: Float = 0.14f,
    borderAlpha: Float = 0.28f
): Modifier = this
    .clip(shape)
    .background(
        androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = fillAlpha + 0.06f),
                Color.White.copy(alpha = fillAlpha)
            )
        )
    )
    .border(
        androidx.compose.foundation.BorderStroke(
            1.dp,
            androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(Color.White.copy(alpha = borderAlpha + 0.12f), Color.White.copy(alpha = borderAlpha * 0.4f))
            )
        ),
        shape
    )

/**
 * Status accent for cards: a soft corner glow plus a slim bar on the left edge.
 * [pulse] makes the glow breathe (use for urgent items).
 */
@androidx.compose.runtime.Composable
fun Modifier.accentGlow(color: Color, pulse: Boolean = false): Modifier {
    val k = if (pulse) breathing(to = 2f, periodMs = 1100) - 1f else 0f
    return this.drawBehind {
        val a = if (pulse) 0.10f + 0.12f * k else 0.14f
        drawRect(
            androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(color.copy(alpha = a), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(0f, 0f),
                radius = size.width * 0.8f
            )
        )
        drawRect(
            androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(color, color.copy(alpha = 0.25f))
            ),
            size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
        )
    }
}
