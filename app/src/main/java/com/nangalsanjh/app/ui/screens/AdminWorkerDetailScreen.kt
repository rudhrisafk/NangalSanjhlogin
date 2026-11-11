// AdminWorkerDetailScreen.kt — NEW FILE

package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Worker Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        val p = profile
        if (p == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(p.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(p.category, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (p.isBlocked) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                else if (p.isAvailable) Color(0xFF138A4B).copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    if (p.isBlocked) "Blocked" else if (p.isAvailable) "Online" else "Offline",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = if (p.isBlocked) MaterialTheme.colorScheme.error else if (p.isAvailable) Color(0xFF138A4B) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("📞 ${p.phone}", fontSize = 13.sp)
                        if (p.address.isNotEmpty()) Text("📍 ${p.address}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        if (p.experience.isNotEmpty()) Text("🛠️ ${p.experience} years experience", fontSize = 13.sp)
                        if (p.aadhar.isNotEmpty()) Text("🆔 Aadhar: ${p.aadhar}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        if (p.about.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(p.about, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            AdminDetailStatCard(count = "%.1f".format(p.rating), label = "Rating", emoji = "⭐")
                            AdminDetailStatCard(count = p.jobsDone.toString(), label = "Jobs Done", emoji = "✅")
                            AdminDetailStatCard(count = "₹$totalEarned", label = "Total Fees", emoji = "💰")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (p.isBlocked) {
                            Button(
                                onClick = { db.collection("users").document(workerUid).update("isBlocked", false) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Unblock Worker") }
                        } else {
                            OutlinedButton(
                                onClick = { db.collection("users").document(workerUid).update("isBlocked", true) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) { Text("Block Worker") }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Job History & Fee Slips", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))

                if (completedJobs.isEmpty()) {
                    Text("No completed jobs yet", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                } else {
                    completedJobs.forEach { job ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(job.category, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(getTimeAgo(job.timeStamp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("👤 ${job.userName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                if (job.price.isNotEmpty()) Text("💰 Fee: ${job.price}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
private fun AdminDetailStatCard(count: String, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            count,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}