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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
        animationSpec = tween(durationMillis = 450, easing = EaseOut),
        label = "alpha",
    )
    val translateY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 24f,
        animationSpec = tween(durationMillis = 450, easing = EaseOut),
        label = "translateY",
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(600.milliseconds)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .aurora(
                listOf(
                    Color(0xFF3A7BFF).copy(alpha = 0.16f),
                    Color(0xFF8E5BFF).copy(alpha = 0.12f),
                    Color(0xFF19C3B1).copy(alpha = 0.12f)
                ),
                periodMs = 6000
            ),
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
                modifier = Modifier.size(96.dp).scale(breathing(1.07f, 1600))
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Text(
                text = "Nangal Seva",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = tr("ਨੰਗਲ ਸੇਵਾ"),
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
                text = "ਆਪਣਾ ਸ਼ਹਿਰ, ਆਪਣੀ ਜ਼ਿੰਮੇਵਾਰੀ  ·  Apna Shehar, Apni Zimmedari",
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