// WorkerCategoryScreen.kt — FULL FILE (adds Years of Experience field)

package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val workerCategories = listOf(
    "🔧 Plumber",
    "⚡ Electrician",
    "🪚 Carpenter",
    "🎨 Painter",
    "🧱 Mason",
    "🧹 Cleaner",
    "👷 Labour",
    "🚗 Automobile",
    "🩺 Medical Tests",
    "🚕 Taxi Service",
    "🐾 Veterinary",
    "❓ Other",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerCategoryScreen(
    onCategorySelected: (String, String, String, String, String) -> Unit,
    isLoading: Boolean = false
) {
    var selectedCategory by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var aadhar by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    val isOther = selectedCategory == "❓ Other"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Work Details",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tell us about your services",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Select Category",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(500.dp)
        ) {
            items(workerCategories) { category ->
                val isSelected = selectedCategory == category
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable { selectedCategory = category },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        if (isOther) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customCategory,
                onValueChange = { customCategory = it },
                label = { Text("Enter your work type") },
                placeholder = { Text("Eg. AC Repair, DJ Services...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = experience,
            onValueChange = { if (it.length <= 2) experience = it.filter { c -> c.isDigit() } },
            label = { Text("Years of Experience") },
            placeholder = { Text("Eg. 5") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Your Charges / Price") },
            placeholder = { Text("Eg. ₹500/day or ₹200/hour") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = about,
            onValueChange = { about = it },
            label = { Text("About Your Services") },
            placeholder = { Text("Describe your skills and experience...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = aadhar,
            onValueChange = { if (it.length <= 12) aadhar = it },
            label = { Text("Aadhar Card Number") },
            placeholder = { Text("12-digit Aadhar number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "🔒 Aadhar details are stored securely for verification only",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val finalCategory = if (isOther) customCategory.trim() else selectedCategory
                onCategorySelected(finalCategory, price, about, aadhar, experience)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = selectedCategory.isNotEmpty() &&
                    (!isOther || customCategory.isNotBlank()) &&
                    !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save & Continue →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}