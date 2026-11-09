// MyEarningsScreen.kt

package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.nangalsanjh.app.viewmodel.PostViewModel

@Composable
fun MyEarningsScreen(onBack: () -> Unit) {
    val postViewModel: PostViewModel = viewModel()
    val allPosts by postViewModel.posts.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val completedJobs = remember(allPosts, uid) {
        allPosts.filter { it.assignedWorkerUid == uid && it.status == "completed" }
            .sortedByDescending { it.timeStamp }
    }

    fun priceValue(price: String): Int = price.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0

    val totalEarnings = remember(completedJobs) { completedJobs.sumOf { priceValue(it.price) } }
    val thisMonthJobs = remember(completedJobs) {
        val cal = java.util.Calendar.getInstance()
        val currentMonth = cal.get(java.util.Calendar.MONTH)
        completedJobs.filter {
            val jobCal = java.util.Calendar.getInstance().apply { timeInMillis = it.timeStamp }
            jobCal.get(java.util.Calendar.MONTH) == currentMonth
        }
    }
    val avgRating = remember(completedJobs) {
        val rated = completedJobs.filter { it.rated }
        if (rated.isEmpty()) 0f else rated.map { it.feedbackRating }.average().toFloat()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("My Earnings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCard(count = "₹$totalEarnings", label = "Total Earned", emoji = "💰")
                StatCard(count = completedJobs.size.toString(), label = "Jobs Done", emoji = "✅")
                StatCard(count = thisMonthJobs.size.toString(), label = "This Month", emoji = "📅")
            }
        }
        if (avgRating > 0f) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center) {
                Text("⭐ Average rating: %.1f".format(avgRating), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (completedJobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No completed jobs yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(completedJobs) { job ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(job.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("👤 ${job.userName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (job.price.isNotEmpty()) Text("💰 ${job.price}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                if (job.rated) Text("⭐ ${job.feedbackRating}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}
