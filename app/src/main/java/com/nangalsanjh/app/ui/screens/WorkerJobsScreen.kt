package com.nangalsanjh.app.ui.screens

import com.nangalsanjh.app.ui.bouncy.Button
import com.nangalsanjh.app.ui.bouncy.OutlinedButton
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nangalsanjh.app.viewmodel.Post
import com.nangalsanjh.app.viewmodel.PostViewModel

@Composable
fun WorkerJobsScreen() {
    val postViewModel: PostViewModel = viewModel()
    val posts by postViewModel.posts.collectAsState()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: ""
    val context = LocalContext.current

    var isAvailable by remember { mutableStateOf(value = false) }
    var workerCategory by remember { mutableStateOf("") }
    val withinHours: Boolean = remember { isWithinWorkingHours() }

    // NEW: track which job IDs this worker has declined
    var declinedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            db.collection("users").document(uid)
                .addSnapshotListener { doc, _ ->
                    val dbAvailable = doc?.getBoolean("isAvailable") ?: false
                    workerCategory = doc?.getString("category") ?: ""
                    isAvailable = dbAvailable && withinHours

                    // NEW: read declined job ids from the worker's document
                    @Suppress("UNCHECKED_CAST")
                    val declinedList = (doc?.get("declinedJobs") as? List<String>) ?: emptyList()
                    declinedIds = declinedList.toSet()

                    if (!withinHours && dbAvailable) {
                        db.collection("users").document(uid)
                            .update("isAvailable", false)
                    }
                }
        }
    }

    // Filter posts by worker category AND remove declined ones
    val relevantPosts = remember(posts, workerCategory, declinedIds) {
        val categoryFiltered = if (workerCategory.isEmpty()) posts
        else posts.filter { post ->
            post.category.contains(
                workerCategory.replace(Regex("[^a-zA-Z ]"), "").trim(),
                ignoreCase = true,
            )
        }
        // NEW: exclude posts this worker already declined
        categoryFiltered.filter { post -> post.id !in declinedIds }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Availability Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Availability Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (withinHours) "Working hours: 9AM - 7PM"
                            else "Outside working hours",
                            fontSize = 12.sp,
                            color = if (withinHours)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { newValue ->
                            if (withinHours) {
                                isAvailable = newValue
                                db.collection("users").document(uid)
                                    .update("isAvailable", newValue)
                            }
                        },
                        enabled = withinHours,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }

                if (!withinHours) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "⏰ Toggle only works 9AM-7PM. Turn ON tomorrow at 9AM.",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Job Requests",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (workerCategory.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        workerCategory,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (relevantPosts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyBlock(
                    icon = Icons.Default.Inbox,
                    title = "No job requests for your category",
                    subtitle = "New requests will show up here."
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(relevantPosts) { post ->
                    JobRequestCard(
                        post = post,
                        onAccept = {
                            // Call the seeker
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = "tel:${post.userPhone}".toUri()
                            }
                            context.startActivity(intent)
                        }) {
                        // NEW: actually save the decline so it disappears from the list
                        if (uid.isNotEmpty()) {
                            db.collection("users").document(uid)
                                .update("declinedJobs", FieldValue.arrayUnion(post.id))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JobRequestCard(
    post: Post,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    post.category,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    getTimeAgo(post.timeStamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                post.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                post.userName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline", fontSize = 12.sp)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Seeker", fontSize = 12.sp)
                }
            }
        }
    }
}
