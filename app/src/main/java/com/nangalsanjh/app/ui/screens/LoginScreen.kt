package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nangalsanjh.app.R
import com.nangalsanjh.app.theme.*

@Composable
fun LoginScreen(
    onLoginClick: (String) -> Unit,
    onBloodInfoSaved: (String, String, Boolean) -> Unit = { _, _, _ -> }, // kept for signature compatibility, unused now
    isLoading: Boolean = false,
    error: String? = null,
    isReturningUser: Boolean = false,
    existingBloodGroup: String = ""
) {
    var phoneNumber by remember { mutableStateOf("") }

    // NEW CHANGE: removed age, blood group, and donor consent state/UI — phone-number-only screen

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            Image(
                painter = painterResource(id = R.drawable.nangal_seva_logo),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Nangal Seva",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "ਨੰਗਲ ਸੇਵਾ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Phone section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Phone Number",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(Spacing.sm))

                // Phone field with +91 prefix
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { if (it.length <= 10) phoneNumber = it },
                    placeholder = {
                        Text(
                            "98765 43210",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    prefix = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "+91",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            VerticalDivider(
                                modifier = Modifier.height(20.dp),
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = Radius.md,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // NEW CHANGE: removed "Age" field, "Blood Group" card, and "existingBloodGroup" card entirely

            if (error != null) {
                Spacer(modifier = Modifier.height(Spacing.lg))
                Surface(
                    shape = Radius.md,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 16.sp)
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
                text = "Continue",
                onClick = {
                    // NEW CHANGE: no longer calls onBloodInfoSaved — phone submit only
                    onLoginClick("+91$phoneNumber")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = phoneNumber.length == 10,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                "By continuing, you agree to our Terms of Service",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xxxl))
        }
    }
}