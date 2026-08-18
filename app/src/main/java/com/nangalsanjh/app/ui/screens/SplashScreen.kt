package com.nangalsanjh.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.nangalsanjh.app.R
import com.nangalsanjh.app.theme.Spacing
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var startAnimation by remember { mutableStateOf(value = false) }

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = EaseOut),
        label = "alpha",
    )
    val translateY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 24f,
        animationSpec = tween(durationMillis = 900, easing = EaseOut),
        label = "translateY",
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2400.milliseconds)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha)
                .offset(y = translateY.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.nangal_seva_logo),
                contentDescription = "Nangal Seva",
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Text(
                text = "Nangal Seva",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "ਨੰਗਲ ਸੇਵਾ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Bottom tagline
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Apna Shehar, Apni Zimmedari",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "v1.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}