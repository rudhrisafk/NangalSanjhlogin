package com.nangalsanjh.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nangalsanjh.app.theme.NSButton
import com.nangalsanjh.app.theme.Radius
import com.nangalsanjh.app.theme.Spacing
import com.nangalsanjh.app.theme.BrandOrange
import com.nangalsanjh.app.theme.BrandOrangeDeep
import com.nangalsanjh.app.theme.InkOnLight
import kotlinx.coroutines.delay

/** Fades + slides a block in once, with a small stagger. */
@Composable
fun RevealOnLoad(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 6 }
    ) { content() }
}

/** Card that gently scales down while pressed. */
@Composable
fun PressableCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onClickLabel: String? = null,
    background: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable ColumnScope.() -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "press"
    )
    Column(
        modifier = modifier
            .scale(scale)
            .clip(Radius.lg)
            .background(background)
            .border(BorderStroke(1.dp, borderColor), Radius.lg)
            .clickable(
                interactionSource = source,
                indication = androidx.compose.material3.ripple(),
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick
            ),
        content = content
    )
}

@Composable
fun SeekerHome(
    userName: String,
    onNavigate: (Int) -> Unit
) {
    val firstName = userName.trim().substringBefore(" ").ifEmpty { "there" }
    val ink = InkOnLight

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xl)) {

        // ── Hero ─────────────────────────────────────────────
        RevealOnLoad(0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Radius.xxl)
                    .background(ink)
                    .padding(Spacing.xl)
            ) {
                Text(
                    "NAMASTE  ·  ਸਤ ਸ੍ਰੀ ਅਕਾਲ",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFB36B),
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    "Hi $firstName, what do you need today?",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "ਅੱਜ ਤੁਹਾਨੂੰ ਕਿਸ ਚੀਜ਼ ਦੀ ਲੋੜ ਹੈ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(Spacing.lg))

                // Search-style entry into Services
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(Radius.md)
                        .background(Color.White)
                        .clickable(onClickLabel = "Search services") { onNavigate(1) }
                        .padding(horizontal = Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF55627A))
                    Spacer(Modifier.width(Spacing.md))
                    Text(
                        "Plumber, electrician, tutor…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF55627A)
                    )
                }
                Spacer(Modifier.height(Spacing.md))
                NSButton(
                    text = "Post a job request",
                    onClick = { onNavigate(1) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Default.Add
                )
            }
        }

        // ── Quick actions ────────────────────────────────────
        RevealOnLoad(1) {
            Column {
                SectionLabel("QUICK ACTIONS", "ਤੁਰੰਤ ਕਾਰਵਾਈ")
                Spacer(Modifier.height(Spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
                    QuickTile("Services", "ਸੇਵਾਵਾਂ", Icons.Default.Build, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { onNavigate(1) }
                    QuickTile("Complaints", "ਸ਼ਿਕਾਇਤ", Icons.Default.Report, Color(0xFF1F5FBF), Modifier.weight(1f)) { onNavigate(3) }
                }
                Spacer(Modifier.height(Spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
                    QuickTile("Live map", "ਨਕਸ਼ਾ", Icons.Default.Map, Color(0xFF138A4B), Modifier.weight(1f)) { onNavigate(4) }
                    QuickTile("Emergency", "ਐਮਰਜੈਂਸੀ", Icons.Default.Warning, Color(0xFFD92D3A), Modifier.weight(1f)) { onNavigate(2) }
                }
            }
        }

        // ── How it works ─────────────────────────────────────
        RevealOnLoad(2) {
            Column {
                SectionLabel("HOW IT WORKS", "ਇਹ ਕਿਵੇਂ ਕੰਮ ਕਰਦਾ ਹੈ")
                Spacer(Modifier.height(Spacing.md))
                StepRow("01", "Describe the work", "Pick a category and drop a pin where you need help.")
                Spacer(Modifier.height(Spacing.sm))
                StepRow("02", "A worker accepts", "Nearby workers get your request and accept within 10 minutes.")
                Spacer(Modifier.height(Spacing.sm))
                StepRow("03", "Track and rate", "Follow the worker on the map, then leave a rating.")
            }
        }

        // ── Trust strip ──────────────────────────────────────
        RevealOnLoad(3) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                TrustChip("No ads", Modifier.weight(1f))
                TrustChip("Free to use", Modifier.weight(1f))
                TrustChip("Local workers", Modifier.weight(1f))
            }
        }

        RevealOnLoad(4) { SectionLabel("LATEST FROM NANGAL", "ਨੰਗਲ ਦੀਆਂ ਤਾਜ਼ਾ ਖ਼ਬਰਾਂ") }
    }
}

@Composable
private fun SectionLabel(title: String, punjabi: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )
        Text(
            punjabi,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickTile(
    label: String,
    punjabi: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PressableCard(modifier = modifier, onClick = onClick, onClickLabel = label) {
        Column(modifier = Modifier.padding(Spacing.lg).fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(Radius.md)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(Spacing.md))
            Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(punjabi, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StepRow(number: String, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, Radius.lg)
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            number,
            style = MaterialTheme.typography.titleLarge,
            color = BrandOrangeDeep,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(Spacing.lg))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TrustChip(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(Radius.full)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF138A4B), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}
