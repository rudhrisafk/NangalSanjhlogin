// CommunityFeedScreen.kt — FULL FILE (worker: job-post FAB restored. seeker: news shown as cards too)
// Ad/news grid uses fixed repeating pattern sections (FourGrid → Wide → TwoCol) via AdSection/buildAdSections.
// Images render at natural width-fit height (ContentScale.FillWidth) — no admin-controlled banner height.

package com.nangalsanjh.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nangalsanjh.app.theme.*
import com.nangalsanjh.app.viewmodel.Announcement
import com.nangalsanjh.app.viewmodel.AnnouncementViewModel
import com.nangalsanjh.app.viewmodel.Post
import com.nangalsanjh.app.viewmodel.PostViewModel
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun CommunityFeedScreen(userName: String, userRole: String = "seeker") {
    if (userRole == "worker") {
        WorkerFeedContent(userName = userName)
    } else {
        SeekerFeedContent()
    }
}

// ── SEEKER FEED ────────────────────────────────────────────────

@Composable
fun SeekerFeedContent() {
    val announcementViewModel: AnnouncementViewModel = viewModel()
    val announcements by announcementViewModel.announcements.collectAsState()
    val isLoading by announcementViewModel.isLoading.collectAsState()

    val activeAnnouncements = remember(announcements) { announcements.filter { it.isActive } }
    val emergencies = remember(activeAnnouncements) { activeAnnouncements.filter { it.type == "emergency" } }
    val newsItems = remember(activeAnnouncements) { activeAnnouncements.filter { it.type == "news" } }
    val ads = remember(activeAnnouncements) { activeAnnouncements.filter { it.type == "ad" } }

    var selectedAnnouncement by remember { mutableStateOf<Announcement?>(null) }

    val feedItems = remember(newsItems, ads) { newsItems + ads }
    val sections = remember(feedItems) { buildAdSections(feedItems) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HeadlineTicker(newsItems = newsItems)

        if (isLoading && activeAnnouncements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(emergencies) { announcement ->
                    EmergencyAnnouncementCard(announcement) { selectedAnnouncement = announcement }
                }

                if (feedItems.isEmpty() && emergencies.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                            NSEmptyState(
                                emoji = "📰",
                                title = "Nothing to show right now",
                                subtitle = "Community news, ads, and emergency alerts will appear here"
                            )
                        }
                    }
                } else {
                    items(sections) { section ->
                        when (section) {
                            is AdSection.FourGrid -> FourGridAdRow(section.items) { selectedAnnouncement = it }
                            is AdSection.Wide -> WideAdCard(section.item) { selectedAnnouncement = it }
                            is AdSection.TwoCol -> TwoColAdRow(section.items) { selectedAnnouncement = it }
                        }
                    }
                }
            }
        }
    }

    selectedAnnouncement?.let { announcement ->
        AnnouncementDetailDialog(announcement = announcement, onDismiss = { selectedAnnouncement = null })
    }
}

// ── AD SECTIONS (fixed repeating pattern: FourGrid → Wide → TwoCol) ──

sealed class AdSection {
    data class FourGrid(val items: List<Announcement>) : AdSection()
    data class Wide(val item: Announcement) : AdSection()
    data class TwoCol(val items: List<Announcement>) : AdSection()
}

fun buildAdSections(items: List<Announcement>): List<AdSection> {
    val sections = mutableListOf<AdSection>()
    var i = 0
    var patternIndex = 0
    while (i < items.size) {
        when (patternIndex % 3) {
            0 -> {
                val chunk = items.subList(i, minOf(i + 4, items.size))
                sections.add(AdSection.FourGrid(chunk))
                i += chunk.size
            }
            1 -> {
                sections.add(AdSection.Wide(items[i]))
                i += 1
            }
            else -> {
                val chunk = items.subList(i, minOf(i + 2, items.size))
                sections.add(AdSection.TwoCol(chunk))
                i += chunk.size
            }
        }
        patternIndex++
    }
    return sections
}

@Composable
fun FourGridAdRow(items: List<Announcement>, onClick: (Announcement) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                rowItems.forEach { item ->
                    Card(
                        modifier = Modifier.weight(1f).clickable { onClick(item) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            AdImageArea(item.imageUrl, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                if (item.type == "news") "News" else "Sponsored",
                                fontSize = 9.sp,
                                color = if (item.type == "news") MaterialTheme.colorScheme.primary else Color(0xFF2E7D32)
                            )
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun WideAdCard(item: Announcement, onClick: (Announcement) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AdImageArea(item.imageUrl, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            NSStatusBadge(
                text = if (item.type == "news") "News" else "Sponsored",
                color = if (item.type == "news") MaterialTheme.colorScheme.primary else NangalColors.Green
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(item.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 2)
        }
    }
}

@Composable
fun TwoColAdRow(items: List<Announcement>, onClick: (Announcement) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        items.forEach { item ->
            Card(
                modifier = Modifier.weight(1f).clickable { onClick(item) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    AdImageArea(item.imageUrl, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                    Text(item.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 2)
                }
            }
        }
        if (items.size == 1) Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun HeadlineTicker(newsItems: List<Announcement>) {
    val tickerText = remember(newsItems) {
        if (newsItems.isEmpty()) "🗞️ NANGAL TIMES — Stay tuned for local updates"
        else newsItems.joinToString("      •      ") { it.title }
    }

    var containerWidth by remember { mutableStateOf(0f) }
    var textWidth by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "ticker")
    val offset by infiniteTransition.animateFloat(
        initialValue = -textWidth,
        targetValue = containerWidth,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (12000 + (textWidth / 2).toInt()), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFFB71C1C))
            .onGloballyPositioned { containerWidth = it.size.width.toFloat() },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = tickerText,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            maxLines = 1,
            modifier = Modifier
                .onGloballyPositioned { textWidth = it.size.width.toFloat() }
                .offset { androidx.compose.ui.unit.IntOffset(offset.roundToInt(), 0) }
        )
    }
}

@Composable
fun EmergencyAnnouncementCard(announcement: Announcement, onClick: () -> Unit) {
    val context = LocalContext.current
    NSCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🚨", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text("Emergency Need", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.error)
                if (announcement.bloodType.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Surface(shape = Radius.md, color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)) {
                        Text("🩸 ${announcement.bloodType}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            AdImageArea(announcement.imageUrl, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(announcement.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(announcement.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 19.sp, maxLines = 2)
            if (announcement.location.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("📍 ${announcement.location}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            if (announcement.contact.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                NSButton(
                    text = "Call ${announcement.contact}",
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${announcement.contact}") }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Default.Call
                )
            }
        }
    }
}

@Composable
fun AdImageArea(imageUrl: String, modifier: Modifier = Modifier) {
    if (imageUrl.isNotEmpty()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = modifier.clip(RoundedCornerShape(12.dp))
        )
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
        val shimmerOffset by infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(animation = tween(1400, easing = LinearEasing), repeatMode = RepeatMode.Restart),
            label = "shimmerOffset"
        )
        Box(
            modifier = modifier
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                        start = Offset(shimmerOffset * 300f, 0f),
                        end = Offset(shimmerOffset * 300f + 300f, 300f)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun AnnouncementDetailDialog(announcement: Announcement, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = null,
        text = {
            Column {
                AdImageArea(announcement.imageUrl, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (announcement.type) {
                            "emergency" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            "ad" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            announcement.type.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = when (announcement.type) {
                                "emergency" -> MaterialTheme.colorScheme.error
                                "ad" -> Color(0xFF2E7D32)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                    Text(getTimeAgo(announcement.timeStamp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(announcement.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Text(announcement.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 20.sp)
                if (announcement.bloodType.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("🩸 Blood needed: ${announcement.bloodType}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                }
                if (announcement.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("📍 ${announcement.location}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        },
        confirmButton = {
            if (announcement.contact.isNotEmpty()) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${announcement.contact}") }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call ${announcement.contact}")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) { Text("Close") }
        }
    )
}

// ── WORKER FEED (FAB to post service restored) ──
@Composable
fun WorkerFeedContent(userName: String) {
    val postViewModel: PostViewModel = viewModel()
    val posts by postViewModel.posts.collectAsState()
    val isLoadingPosts by postViewModel.isLoading.collectAsState()
    val postSuccess by postViewModel.postSuccess.collectAsState()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var isAvailable by remember { mutableStateOf(false) }
    var workerCategory by remember { mutableStateOf("") }
    val withinHours = remember { isWithinWorkingHours() }
    var nowTick by remember { mutableStateOf(System.currentTimeMillis()) }
    var showPostDialog by remember { mutableStateOf(false) }

    LaunchedEffect(postSuccess) {
        if (postSuccess) {
            showPostDialog = false
            postViewModel.resetPostSuccess()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nowTick = System.currentTimeMillis()
        }
    }

    LaunchedEffect(uid) {
        while (true) {
            postViewModel.cleanupExpiredAcceptWindow()
            delay(30_000)
        }
    }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            postViewModel.cleanupExpiredWorkerPosts(uid)
            db.collection("users").document(uid)
                .addSnapshotListener { doc, _ ->
                    val dbAvailable = doc?.getBoolean("isAvailable") ?: false
                    workerCategory = doc?.getString("category") ?: ""
                    isAvailable = dbAvailable && withinHours
                    if (!withinHours && dbAvailable) {
                        db.collection("users").document(uid).update("isAvailable", false)
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                shape = Radius.md,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Availability Status", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            if (withinHours) "Working hours: 9AM - 7PM" else "Outside working hours",
                            fontSize = 11.sp,
                            color = if (withinHours) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                        )
                    }
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { newValue ->
                            if (withinHours) {
                                isAvailable = newValue
                                db.collection("users").document(uid).update("isAvailable", newValue)
                            }
                        },
                        enabled = withinHours
                    )
                }
            }

            NSDivider()

            val roleFiltered = posts.filter {
                it.userRole == "seeker" && it.status == "open" && !it.declinedBy.contains(uid) &&
                        (nowTick - it.timeStamp) < PostViewModel.JOB_ACCEPT_WINDOW_MS
            }
            val filtered = if (workerCategory.isNotEmpty()) {
                roleFiltered.filter { it.category.contains(workerCategory.replace(Regex("[^a-zA-Z ]"), "").trim(), ignoreCase = true) }
            } else roleFiltered

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    NSEmptyState(
                        emoji = "📭",
                        title = "No jobs in demand",
                        subtitle = "Seeker requests matching your category will show here"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(filtered) { post ->
                        JobDemandCard(
                            post = post,
                            nowTick = nowTick,
                            onAccept = {
                                postViewModel.acceptPost(post.id, uid, userName)
                                val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${post.userPhone}") }
                                context.startActivity(intent)
                            },
                            onDecline = { postViewModel.declinePost(post.id, uid) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showPostDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.lg),
            shape = Radius.md,
            containerColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
        }
    }

    if (showPostDialog) {
        WorkerPostDialog(
            onDismiss = { showPostDialog = false },
            onPost = { cat, desc, price, avail -> postViewModel.createWorkerPost(userName, cat, desc, price, avail) },
            isLoading = isLoadingPosts
        )
    }
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

@Composable
fun JobDemandCard(post: Post, nowTick: Long, onAccept: () -> Unit, onDecline: () -> Unit) {
    val timeAgo = remember(post.timeStamp) { getTimeAgo(post.timeStamp) }
    val remainingMs = (post.timeStamp + PostViewModel.JOB_ACCEPT_WINDOW_MS - nowTick).coerceAtLeast(0)
    val minutes = (remainingMs / 1000) / 60
    val seconds = (remainingMs / 1000) % 60
    val isUrgent = remainingMs < 2 * 60 * 1000

    NSCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(post.category, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Text(timeAgo, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(post.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text("👤 ${post.userName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (post.address.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("📍 ${post.address}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = Radius.md,
                color = if (isUrgent) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    "⏱️ Respond in ${minutes}:${seconds.toString().padStart(2, '0')}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NSOutlinedButton(text = "Decline", onClick = onDecline, modifier = Modifier.weight(1f))
                NSButton(text = "Accept & Call", onClick = onAccept, modifier = Modifier.weight(1f), leadingIcon = Icons.Default.Call)
            }
        }
    }
}