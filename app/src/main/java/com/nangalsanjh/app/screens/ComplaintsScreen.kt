package com.nangalsanjh.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nangalsanjh.app.viewmodel.ComplaintModel
import com.nangalsanjh.app.viewmodel.ComplaintViewModel

@Composable
fun ComplaintsScreen(userRole: String, userName: String = "") {
    val complaintViewModel: ComplaintViewModel = viewModel()
    val complaints by complaintViewModel.complaints.collectAsState()
    val isLoading by complaintViewModel.isLoading.collectAsState()
    val submitSuccess by complaintViewModel.submitSuccess.collectAsState()

    var showComplaintDialog by remember { mutableStateOf(false) }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            showComplaintDialog = false
            complaintViewModel.resetSubmitSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && complaints.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (complaints.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No complaints yet",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (userRole == "seeker") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showComplaintDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Report an Issue")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(complaints) { complaint ->
                    RealComplaintCard(
                        complaint = complaint,
                        userRole = userRole,
                        onStatusUpdate = { id, status, update ->
                            complaintViewModel.updateComplaintStatus(id, status, update)
                        }
                    )
                }
            }
        }

        if (userRole == "seeker" || userRole.isEmpty()) {
            FloatingActionButton(
                onClick = { showComplaintDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Complaint")
            }
        }
    }

    if (showComplaintDialog) {
        NewComplaintDialog(
            onDismiss = { showComplaintDialog = false },
            onSubmit = { category, description, location ->
                complaintViewModel.submitComplaint(userName, category, description, location)
            },
            isLoading = isLoading
        )
    }
}

@Composable
fun RealComplaintCard(
    complaint: ComplaintModel,
    userRole: String,
    onStatusUpdate: (String, String, String) -> Unit
) {
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("In Review") }

    val timeAgo = remember(complaint.timeStamp) { getTimeAgo(complaint.timeStamp) }

    val statusColor = when (complaint.status) {
        "Resolved" -> MaterialTheme.colorScheme.primary
        "In Review" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    complaint.category,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        complaint.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "👤 ${complaint.reporter}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                complaint.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "📍 ${complaint.location}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    timeAgo,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (complaint.departmentUpdate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Text(
                        "🏛️ ${complaint.departmentUpdate}",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (userRole == "admin") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            selectedStatus = "In Review"
                            showUpdateDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("In Review", fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            selectedStatus = "Resolved"
                            showUpdateDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Resolve", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update Status", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Status: $selectedStatus",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = updateText,
                        onValueChange = { updateText = it },
                        label = { Text("Department Update (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onStatusUpdate(complaint.id, selectedStatus, updateText)
                    showUpdateDialog = false
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NewComplaintDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit,
    isLoading: Boolean = false
) {
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("🕳️ Pothole") }
    var customCategory by remember { mutableStateOf("") }

    val categories = listOf(
        "🕳️ Pothole",
        "💡 Street Light",
        "🗑️ Garbage",
        "💧 Water Leakage",
        "🚧 Road",
        "⚡ Electricity",
        "🏫 School/Education",
        "🏥 Hospital/Health",
        "🌳 Parks/Environment",
        "❓ Other"
    )

    val isOther = selectedCategory == "❓ Other"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Complaint", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Category",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                    edgePadding = 0.dp
                ) {
                    categories.forEach { cat ->
                        Tab(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            text = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Custom category field for Other
                if (isOther) {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Specify Issue") },
                        placeholder = { Text("Eg. Broken bench in park") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe the issue") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalCategory = if (isOther && customCategory.isNotBlank())
                        "❓ $customCategory"
                    else
                        selectedCategory
                    onSubmit(finalCategory, description, location)
                },
                enabled = description.isNotBlank() &&
                        location.isNotBlank() &&
                        (!isOther || customCategory.isNotBlank()) &&
                        !isLoading,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}