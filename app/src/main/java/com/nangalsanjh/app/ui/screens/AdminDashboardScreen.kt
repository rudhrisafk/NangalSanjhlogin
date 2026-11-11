// AdminDashboardScreen.kt — FULL FILE (registered workers list is clickable -> opens AdminWorkerDetailScreen)

package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.nangalsanjh.app.viewmodel.ComplaintViewModel
import com.nangalsanjh.app.viewmodel.PostViewModel

data class AdminWorkerRow(
    val uid: String = "",
    val name: String = "",
    val category: String = "",
    val isAvailable: Boolean = false,
    val isBlocked: Boolean = false
)

@Composable
fun AdminDashboardScreen() {
    val complaintViewModel: ComplaintViewModel = viewModel()
    val complaints by complaintViewModel.complaints.collectAsState()
    val postViewModel: PostViewModel = viewModel()
    val posts by postViewModel.posts.collectAsState()

    var allWorkers by remember { mutableStateOf<List<AdminWorkerRow>>(emptyList()) }
    var selectedWorkerUid by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("role", "worker")
            .addSnapshotListener { snapshot, _ ->
                allWorkers = snapshot?.documents?.mapNotNull { doc ->
                    AdminWorkerRow(
                        uid = doc.id,
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        isAvailable = doc.getBoolean("isAvailable") ?: false,
                        isBlocked = doc.getBoolean("isBlocked") ?: false
                    )
                } ?: emptyList()
            }
    }

    selectedWorkerUid?.let { uid ->
        AdminWorkerDetailScreen(workerUid = uid, onBack = { selectedWorkerUid = null })
        return
    }

    val pendingComplaints = remember(complaints) { complaints.count { it.status != "Resolved" } }
    val activeWorkers = remember(allWorkers) { allWorkers.count { it.isAvailable } }
    val todayJobs = remember(posts) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0); cal.set(java.util.Calendar.SECOND, 0)
        val startOfDay = cal.timeInMillis
        posts.count { it.userRole == "seeker" && it.timeStamp >= startOfDay }
    }
    val categoryWiseCount = remember(allWorkers) {
        allWorkers.groupBy { it.category.ifEmpty { "Other" } }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }
    val maxCount = categoryWiseCount.maxOfOrNull { it.second } ?: 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "ADMIN  ·  ਪ੍ਰਸ਼ਾਸਕ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("City overview", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "Live workers, complaints and jobs across Nangal.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardStatCard("Workers", allWorkers.size.toString(), Icons.Default.Groups, Modifier.weight(1f))
            DashboardStatCard("Active Now", activeWorkers.toString(), Icons.Default.Sensors, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardStatCard("Pending Complaints", pendingComplaints.toString(), Icons.Default.Report, Modifier.weight(1f))
            DashboardStatCard("Jobs Posted Today", todayJobs.toString(), Icons.Default.EditNote, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Workers by Category", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (categoryWiseCount.isEmpty()) {
                    Text("No workers registered yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                } else {
                    categoryWiseCount.forEach { (category, count) ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(category, fontSize = 13.sp)
                                Text(count.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { count.toFloat() / maxCount.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Registered Workers", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

        if (allWorkers.isEmpty()) {
            Text("No workers yet", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        } else {
            allWorkers.forEach { worker ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { selectedWorkerUid = worker.uid },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(worker.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (worker.isBlocked) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else if (worker.isAvailable) androidx.compose.ui.graphics.Color(0xFF138A4B).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ) {
                            Text(
                                if (worker.isBlocked) "Blocked" else if (worker.isAvailable) "Online" else "Offline",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                color = if (worker.isBlocked) MaterialTheme.colorScheme.error
                                else if (worker.isAvailable) androidx.compose.ui.graphics.Color(0xFF138A4B)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DashboardStatCard(label: String, count: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(count, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
