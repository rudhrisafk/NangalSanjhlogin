// AdminWorkerDetailScreen.kt — NEW FILE

package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.nangalsanjh.app.viewmodel.PostViewModel

data class AdminWorkerProfile(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val category: String = "",
    val price: String = "",
    val about: String = "",
    val aadhar: String = "",
    val experience: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val jobsDone: Int = 0,
    val isAvailable: Boolean = false,
    val isBlocked: Boolean = false
)

@Composable
fun AdminWorkerDetailScreen(workerUid: String, onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    var profile by remember { mutableStateOf<AdminWorkerProfile?>(null) }
    val postViewModel: PostViewModel = viewModel()
    val allPosts by postViewModel.posts.collectAsState()

    LaunchedEffect(workerUid) {
        db.collection("users").document(workerUid).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                profile = AdminWorkerProfile(
                    uid = workerUid,
                    name = doc.getString("name") ?: "",
                    phone = doc.getString("phone") ?: "",
                    address = doc.getString("address") ?: "",
                    category = doc.getString("category") ?: "",
                    price = doc.getString("price") ?: "",
                    about = doc.getString("about") ?: "",
                    aadhar = doc.getString("aadhar") ?: "",
                    experience = doc.getString("experience") ?: "",
                    rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                    reviewCount = (doc.getLong("reviewCount") ?: 0L).toInt(),
                    jobsDone = (doc.getLong("jobsDone") ?: 0L).toInt(),
                    isAvailable = doc.getBoolean("isAvailable") ?: false,
                    isBlocked = doc.getBoolean("isBlocked") ?: false
                )
            }
        }
    }

    val completedJobs = remember(allPosts, workerUid) {
        allPosts.filter { it.assignedWorkerUid == workerUid && it.status == "completed" }
            .sortedByDescending { it.timeStamp }
    }
    fun priceValue(price: String): Int = price.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    val totalEarned = remember(completedJobs) { completedJobs.sumOf { priceValue(it.price) } }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Column {
                Text("ADMIN · ਵਰਕਰ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.2.sp)
                Text("Worker details", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }

        val p = profile
        if (p == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(52.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    p.name.trim().take(1).uppercase().ifEmpty { "?" },
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold, fontSize = 22.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(p.category.substringAfter(' '), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Spacer(Modifier.width(8.dp))
                            val green = Color(0xFF138A4B)
                            val badgeColor = if (p.isBlocked) MaterialTheme.colorScheme.error else if (p.isAvailable) green else MaterialTheme.colorScheme.onSurfaceVariant
                            Surface(shape = RoundedCornerShape(20.dp), color = badgeColor.copy(alpha = 0.12f)) {
                                Text(
                                    if (p.isBlocked) "Blocked" else if (p.isAvailable) "Online" else "Offline",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = badgeColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        AdminInfoRow(Icons.Default.Phone, p.phone)
                        if (p.address.isNotEmpty()) AdminInfoRow(Icons.Default.LocationOn, p.address)
                        if (p.experience.isNotEmpty()) AdminInfoRow(Icons.Default.Build, "${p.experience} years experience")
                        if (p.aadhar.isNotEmpty()) AdminInfoRow(Icons.Default.Badge, "Aadhar: ${p.aadhar}")
                        if (p.about.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(p.about, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminDetailStatCard(Modifier.weight(1f), "%.1f".format(p.rating), "Rating", Icons.Default.Star)
                            AdminDetailStatCard(Modifier.weight(1f), p.jobsDone.toString(), "Jobs done", Icons.Default.CheckCircle)
                            AdminDetailStatCard(Modifier.weight(1f), "₹$totalEarned", "Total fees", Icons.Default.AccountBalanceWallet)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        if (p.isBlocked) {
                            Button(
                                onClick = { db.collection("users").document(workerUid).update("isBlocked", false) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Unblock worker", fontWeight = FontWeight.Bold) }
                        } else {
                            OutlinedButton(
                                onClick = { db.collection("users").document(workerUid).update("isBlocked", true) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) { Text("Block worker", fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Job history & fee slips", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 10.dp))

                if (completedJobs.isEmpty()) {
                    Text("No completed jobs yet", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    completedJobs.forEach { job ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(job.category.substringAfter(' '), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(getTimeAgo(job.timeStamp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Customer: ${job.userName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (job.price.isNotEmpty()) Text("Fee: ${job.price}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                if (job.rated) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFF59F00), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${job.feedbackRating}/5 — ${job.feedbackComment.ifEmpty { "No comment" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                                if (job.beforePhotoUrl.isNotEmpty() || job.afterPhotoUrl.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (job.beforePhotoUrl.isNotEmpty()) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Before", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                AsyncImage(
                                                    model = job.beforePhotoUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(8.dp))
                                                )
                                            }
                                        }
                                        if (job.afterPhotoUrl.isNotEmpty()) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("After", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                AsyncImage(
                                                    model = job.afterPhotoUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(8.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminInfoRow(icon: ImageVector, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AdminDetailStatCard(modifier: Modifier, count: String, label: String, icon: ImageVector) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(count, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
