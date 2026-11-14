// MyEarningsScreen.kt

package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
        val currentYear = cal.get(java.util.Calendar.YEAR)
        completedJobs.filter {
            val jobCal = java.util.Calendar.getInstance().apply { timeInMillis = it.timeStamp }
            jobCal.get(java.util.Calendar.MONTH) == currentMonth &&
                jobCal.get(java.util.Calendar.YEAR) == currentYear
        }
    }
    val avgRating = remember(completedJobs) {
        val rated = completedJobs.filter { it.rated }
        if (rated.isEmpty()) 0f else rated.map { it.feedbackRating }.average().toFloat()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Column {
                Text(
                    tr("WORKER · ਕਮਾਈ"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                Text(tr("My earnings"), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }

        // Summary card
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    tr("Total earned"),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                )
                Text(
                    "₹${animatedInt(totalEarnings)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        Text(
                            completedJobs.size.toString(),
                            fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(tr("Jobs done"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
                    }
                    Column {
                        Text(
                            thisMonthJobs.size.toString(),
                            fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(tr("This month"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
                    }
                    if (avgRating > 0f) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star, null,
                                    tint = Color(0xFFF59F00),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    "%.1f".format(avgRating),
                                    fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Text(tr("Avg rating"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        if (completedJobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyBlock(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = tr("No completed jobs yet"),
                    subtitle = tr("Finished jobs and their fees will show up here.")
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(completedJobs) { job ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    trCat(job.category.substringAfter(' ')),
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Person, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        job.userName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                if (job.price.isNotEmpty()) {
                                    Text(
                                        job.price,
                                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                        color = Color(0xFF138A4B),
                                        maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (job.rated) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Star, null,
                                            tint = Color(0xFFF59F00),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            "${job.feedbackRating}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
