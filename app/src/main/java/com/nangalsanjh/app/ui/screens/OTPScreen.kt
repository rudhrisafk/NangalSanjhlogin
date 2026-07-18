package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nangalsanjh.app.theme.*
import kotlinx.coroutines.delay

@Composable
fun OTPScreen(
    phoneNumber: String,
    onVerifyClick: (String) -> Unit,
    onResendClick: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var otp by remember { mutableStateOf("") }
    var resendTimer by remember { mutableIntStateOf(30) }
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (resendTimer > 0) {
            delay(1000)
            resendTimer--
        }
        canResend = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Icon
            Surface(
                shape = Radius.lg,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🔐", fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Text(
                "Verify OTP",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                "Sent to $phoneNumber",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // OTP Field
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                placeholder = {
                    Text(
                        "000000",
                        style = MaterialTheme.typography.displayMedium.copy(
                            letterSpacing = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                shape = Radius.md,
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    textAlign = TextAlign.Center,
                    letterSpacing = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Surface(
                    shape = Radius.md,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxxl))

            NSButton(
                text = "Verify",
                onClick = { onVerifyClick(otp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = otp.length == 6,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            if (canResend) {
                TextButton(onClick = {
                    onResendClick()
                    resendTimer = 30
                    canResend = false
                }) {
                    Text(
                        "Resend OTP",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Text(
                    "Resend in ${resendTimer}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}