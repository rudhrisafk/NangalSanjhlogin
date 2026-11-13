package com.nangalsanjh.app.ui.screens

import com.nangalsanjh.app.ui.bouncy.Button
import com.nangalsanjh.app.ui.bouncy.OutlinedButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ResolvedComplaintsScreen() {
    val complaintViewModel: ComplaintViewModel = viewModel()
    val complaints by complaintViewModel.complaints.collectAsState()
    val resolved = complaints.filter { it.status == "Resolved" }

    if (resolved.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("RESOLVED  ·  ਹੱਲ ਹੋਈਆਂ", "Resolved complaints", "Reopen a complaint if the issue comes back.")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✅", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    tr("No resolved complaints yet"),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreenHeader("RESOLVED  ·  ਹੱਲ ਹੋਈਆਂ", "Resolved complaints", "Reopen a complaint if the issue comes back.", horizontalPadding = 0.dp) }
            items(resolved) { complaint ->
                ResolvedComplaintCard(
                    complaint = complaint,
                    onStatusUpdate = { id, status, update ->
                        complaintViewModel.updateComplaintStatus(id, status, update)
                    }
                )
            }
        }
    }
}

@Composable
fun ResolvedComplaintCard(
    complaint: ComplaintModel,
    onStatusUpdate: (String, String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var updateText by remember { mutableStateOf("") }
    val timeAgo: String = remember(complaint.timeStamp) { getTimeAgo(complaint.timeStamp) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        "✅ Resolved",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
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
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        "🏛️ ${complaint.departmentUpdate}",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("↩️ Mark as In Review", fontSize = 12.sp)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(tr("Reopen Complaint?"), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Yeh complaint wapas 'In Review' mein chali jayegi.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = updateText,
                        onValueChange = { updateText = it },
                        label = { Text(tr("Reason (optional)")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onStatusUpdate(complaint.id, "In Review", updateText)
                    showDialog = false
                }) { Text(tr("Confirm")) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(tr("Cancel")) }
            }
        )
    }
}