package com.nangalsanjh.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nangalsanjh.app.theme.*
import com.nangalsanjh.app.viewmodel.Post
import com.nangalsanjh.app.viewmodel.PostViewModel

@Composable
fun CommunityFeedScreen(userName: String, userRole: String = "seeker") {
    val postViewModel: PostViewModel = viewModel()
    val posts by postViewModel.posts.collectAsState()
    val isLoading by postViewModel.isLoading.collectAsState()
    val postSuccess by postViewModel.postSuccess.collectAsState()
    val context = LocalContext.current

    var showPostDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    val filters = listOf("All", "🔧 Plumber", "⚡ Electrician", "🎨 Painter", "🪚 Carpenter", "🧹 Cleaner", "👷 Labour", "🚗 Automobile", "🩺 Medical", "🚕 Taxi", "🐾 Vet")

    LaunchedEffect(postSuccess) {
        if (postSuccess) {
            showPostDialog = false
            postViewModel.resetPostSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(filters) { filter ->
                    val selected = selectedFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                filter,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = Color.Transparent,
                            borderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            NSDivider()

            if (isLoading && posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                val filtered = if (selectedFilter == "All") posts
                else posts.filter {
                    it.category.contains(
                        selectedFilter.replace(Regex("[^a-zA-Z ]"), "").trim(),
                        ignoreCase = true
                    )
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        NSEmptyState(
                            emoji = "📭",
                            title = "No posts yet",
                            subtitle = if (userRole == "worker") "Share your services with the community" else "Workers will post their services here",
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(filtered) { post ->
                            RealPostCard(
                                post = post,
                                userRole = userRole,
                                onContactClick = { selectedPost = post }
                            )
                        }
                    }
                }
            }
        }

        // FAB — Worker only
        if (userRole == "worker") {
            FloatingActionButton(
                onClick = { showPostDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.lg),
                shape = Radius.md,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    if (showPostDialog) {
        WorkerPostDialog(
            onDismiss = { showPostDialog = false },
            onPost = { cat, desc, price, avail ->
                postViewModel.createWorkerPost(userName, cat, desc, price, avail)
            },
            isLoading = isLoading
        )
    }

    selectedPost?.let { post ->
        ContactWorkerDialog(
            post = post,
            onDismiss = { selectedPost = null },
            onCall = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${post.userPhone}")
                }
                context.startActivity(intent)
                selectedPost = null
            }
        )
    }
}

@Composable
fun RealPostCard(post: Post, userRole: String, onContactClick: () -> Unit) {
    val timeAgo = remember(post.timeStamp) { getTimeAgo(post.timeStamp) }

    NSCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.lg)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    NSAvatar(emoji = post.category.take(2), size = 40.dp)
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column {
                        Text(
                            post.userName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            timeAgo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                NSStatusBadge(
                    text = post.category.replace(Regex("[^a-zA-Z/ ]"), "").trim(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                post.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            // Price + Availability
            if (post.price.isNotEmpty() || post.availability.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.md))
                NSDivider()
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    if (post.price.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💰", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                post.price,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = NangalColors.Green
                            )
                        }
                    }
                    if (post.availability.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🕐", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                post.availability,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (userRole == "seeker") {
                Spacer(modifier = Modifier.height(Spacing.md))
                NSButton(
                    text = "Contact Worker",
                    onClick = onContactClick,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Default.Call
                )
            }
        }
    }
}

@Composable
fun ContactWorkerDialog(post: Post, onDismiss: () -> Unit, onCall: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Radius.xl,
        containerColor = MaterialTheme.colorScheme.surface,
        title = null,
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NSAvatar(emoji = post.category.take(2), size = 52.dp)
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column {
                        Text(post.userName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(post.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))
                NSDivider()
                Spacer(modifier = Modifier.height(Spacing.lg))

                if (post.price.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💰", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(post.price, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = NangalColors.Green)
                    }
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }

                if (post.availability.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🕐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(post.availability, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }

                if (post.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(post.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                }
            }
        },
        confirmButton = {
            NSButton(text = "Call Now", onClick = onCall, leadingIcon = Icons.Default.Call, modifier = Modifier.fillMaxWidth())
        },
        dismissButton = {
            NSOutlinedButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerPostDialog(onDismiss: () -> Unit, onPost: (String, String, String, String) -> Unit, isLoading: Boolean) {
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("🔧 Plumber") }
    var showAvailabilityMenu by remember { mutableStateOf(false) }

    val categories = listOf("🔧 Plumber", "⚡ Electrician", "🎨 Painter", "🪚 Carpenter", "🧹 Cleaner", "👷 Labour", "🚗 Automobile", "🩺 Medical Tests", "🚕 Taxi Service", "🐾 Veterinary", "❓ Other")
    val availabilityOptions = listOf("9 AM - 7 PM", "Morning only", "Evening only", "Weekdays only", "Weekends only", "Anytime")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Radius.xl,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Post Service", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                // Category chips
                Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                NSTextField(value = description, onValueChange = { description = it }, label = "Describe your service", placeholder = "Skills, experience...", singleLine = false, minLines = 2, modifier = Modifier.fillMaxWidth())
                NSTextField(value = price, onValueChange = { price = it }, label = "Your Rate", placeholder = "₹500/day", modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(expanded = showAvailabilityMenu, onExpandedChange = { showAvailabilityMenu = it }) {
                    OutlinedTextField(
                        value = availability,
                        onValueChange = { availability = it },
                        label = { Text("Availability") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true,
                        shape = Radius.md,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAvailabilityMenu) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline)
                    )
                    ExposedDropdownMenu(expanded = showAvailabilityMenu, onDismissRequest = { showAvailabilityMenu = false }) {
                        availabilityOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option, style = MaterialTheme.typography.bodyMedium) }, onClick = { availability = option; showAvailabilityMenu = false })
                        }
                    }
                }
            }
        },
        confirmButton = { NSButton(text = "Post Service", onClick = { onPost(selectedCategory, description, price, availability) }, enabled = description.isNotBlank(), isLoading = isLoading, modifier = Modifier.fillMaxWidth()) },
        dismissButton = { NSOutlinedButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.fillMaxWidth()) }
    )
}

fun getTimeAgo(timeStamp: Long): String {
    val diff = System.currentTimeMillis() - timeStamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}