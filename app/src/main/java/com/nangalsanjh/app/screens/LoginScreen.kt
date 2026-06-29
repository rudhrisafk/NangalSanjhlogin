package com.nangalsanjh.app.screens

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

@Composable
fun LoginScreen(
    onLoginClick: (String) -> Unit,
    onBloodInfoSaved: (String, String, Boolean) -> Unit = { _, _, _ -> },
    isLoading: Boolean = false,
    error: String? = null,
    isReturningUser: Boolean = false,
    existingBloodGroup: String = ""
) {
    var phoneNumber by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("") }
    var donorConsent by remember { mutableStateOf(false) }
    var showBloodSection by remember { mutableStateOf(false) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.nangal_seva_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nangal Seva",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "ਨੰਗਲ ਸੇਵਾ",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Welcome! 👋",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your phone number to continue",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Phone with +91
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { if (it.length <= 10) phoneNumber = it },
                label = { Text("Phone Number") },
                placeholder = { Text("98765 43210") },
                leadingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    ) {
                        Text(
                            "+91",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Divider(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Sirf naye user ke liye age aur blood group dikhao
            if (!isReturningUser) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = age,
                    onValueChange = {
                        if (it.length <= 3) {
                            age = it
                            showBloodSection =
                                it.toIntOrNull()?.let { a -> a in 18..65 } ?: false
                            if (!showBloodSection) {
                                selectedBloodGroup = ""
                                donorConsent = false
                            }
                        }
                    },
                    label = { Text("Age") },
                    placeholder = { Text("Your age") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                if (showBloodSection) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🩸", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "Blood Group",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Optional — Help save lives in Nangal",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Column {
                                bloodGroups.chunked(4).forEach { row ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        row.forEach { group ->
                                            FilterChip(
                                                selected = selectedBloodGroup == group,
                                                onClick = {
                                                    selectedBloodGroup =
                                                        if (selectedBloodGroup == group) ""
                                                        else group
                                                    if (selectedBloodGroup.isEmpty())
                                                        donorConsent = false
                                                },
                                                label = {
                                                    Text(
                                                        group,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFF880E4F),
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }

                            if (selectedBloodGroup.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = donorConsent,
                                        onCheckedChange = { donorConsent = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF880E4F)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "I am willing to donate blood if needed. Anyone in need may contact me through Nangal Seva.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.8f
                                        ),
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Returning user — show existing blood group info
                if (existingBloodGroup.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF880E4F).copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🩸", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Blood Group: $existingBloodGroup",
                                fontSize = 13.sp,
                                color = Color(0xFF880E4F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "⚠️ $error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!isReturningUser) {
                        onBloodInfoSaved(selectedBloodGroup, age, donorConsent)
                    }
                    onLoginClick("+91$phoneNumber")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = phoneNumber.length == 10 && !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Continue →",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "By continuing, you agree to our Terms of Service",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}