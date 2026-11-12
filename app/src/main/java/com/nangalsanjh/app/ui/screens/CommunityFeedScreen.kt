package com.nangalsanjh.app.ui.screens

import androidx.compose.ui.draw.clipToBounds
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
import androidx.compose.ui.unit.IntOffset
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
import kotlin.math.roundToInt

@Composable
fun CommunityFeedScreen(
    userName: String,
    userRole: String = "seeker",
    onNavigate: (Int) -> Unit = {}
) {
    if (userRole == "worker") {
        WorkerFeedContent(userName = userName)
    } else {
        SeekerFeedContent(userName = userName, onNavigate = onNavigate)
    }
}

// ============================================================
// SEEKER FEED
// ============================================================

@Composable
fun SeekerFeedContent(userName: String = "", onNavigate: (Int) -> Unit = {}) {

    val announcementViewModel: AnnouncementViewModel = viewModel()
    val announcements by announcementViewModel.announcements.collectAsState()
    val isLoading by announcementViewModel.isLoading.collectAsState()

    val activeAnnouncements = remember(announcements) {
        announcements.filter { it.isActive }
    }

    val emergencies = remember(activeAnnouncements) {
        activeAnnouncements.filter { it.type == "emergency" }
    }

    val newsItems = remember(activeAnnouncements) {
        activeAnnouncements.filter { it.type == "news" }
    }

    val ads = remember(activeAnnouncements) {
        activeAnnouncements.filter { it.type == "ad" }
    }

    val tickerItems = remember(activeAnnouncements) {
        activeAnnouncements.filter { it.type == "headline" }
    }

    var selectedAnnouncement by remember {
        mutableStateOf<Announcement?>(null)
    }

    val feedItems = remember(newsItems, ads) {
        newsItems + ads
    }

    val sections = remember(feedItems) {
        buildAdSections(feedItems)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        if (tickerItems.isNotEmpty()) {
            HeadlineTicker(newsItems = tickerItems)
        }

        if (isLoading && activeAnnouncements.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSurface,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(32.dp)
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {

                item {
                    SeekerHome(userName = userName, onNavigate = onNavigate)
                }

                items(emergencies) { announcement ->
                    EmergencyAnnouncementCard(announcement) {
                        selectedAnnouncement = announcement
                    }
                }

                if (feedItems.isEmpty() && emergencies.isEmpty()) {

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            NSEmptyState(
                                icon = androidx.compose.material.icons.Icons.Default.Newspaper,
                                title = "Nothing to show right now",
                                subtitle = "Community news, ads, and emergency alerts will appear here"
                            )
                        }
                    }

                } else {

                    items(sections) { section ->

                        when (section) {
                            is AdSection.FourGrid ->
                                FourGridAdRow(section.items) {
                                    selectedAnnouncement = it
                                }
                            is AdSection.Wide ->
                                WideAdCard(section.item) {
                                    selectedAnnouncement = it
                                }
                            is AdSection.TwoCol ->
                                TwoColAdRow(section.items) {
                                    selectedAnnouncement = it
                                }
                        }
                    }
                }
            }
        }
    }

    selectedAnnouncement?.let { announcement ->

        AnnouncementDetailDialog(
            announcement = announcement,
            onDismiss = { selectedAnnouncement = null }
        )
    }
}

// ============================================================
// AD SECTIONS
// ============================================================

sealed class AdSection {

    data class FourGrid(
        val items: List<Announcement>
    ) : AdSection()

    data class Wide(
        val item: Announcement
    ) : AdSection()

    data class TwoCol(
        val items: List<Announcement>
    ) : AdSection()
}

fun buildAdSections(items: List<Announcement>): List<AdSection> {

    val sections = mutableListOf<AdSection>()

    var i = 0
    var patternIndex = 0

    while (i < items.size) {

        when (patternIndex % 3) {
            0 -> {
                val chunk = items.subList(
                    i,
                    minOf(i + 4, items.size)
                )

                sections.add(AdSection.FourGrid(chunk))
                i += chunk.size
            }

            1 -> {
                sections.add(AdSection.Wide(items[i]))
                i++
            }

            else -> {
                val chunk = items.subList(
                    i,
                    minOf(i + 2, items.size)
                )

                sections.add(AdSection.TwoCol(chunk))
                i += chunk.size
            }
        }

        patternIndex++
    }

    return sections
}

// ============================================================
// FOUR GRID ADS
// ============================================================

@Composable
fun FourGridAdRow(
    items: List<Announcement>,
    onClick: (Announcement) -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {

        items.chunked(2).forEach { rowItems ->

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {

                rowItems.forEach { item ->

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onClick(item) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {

                            AdImageArea(
                                item.imageUrl,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                item.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                if (item.type == "news") "News" else "Sponsored",
                                fontSize = 9.sp,
                                color = if (item.type == "news")
                                    MaterialTheme.colorScheme.primary
                                else Color(0xFF138A4B)
                            )
                        }
                    }
                }

                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// ============================================================
// WIDE AD
// ============================================================

@Composable
fun WideAdCard(
    item: Announcement,
    onClick: (Announcement) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(Modifier.padding(12.dp)) {

            AdImageArea(
                item.imageUrl,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            NSStatusBadge(
                text = if (item.type == "news") "News" else "Sponsored",
                color = if (item.type == "news")
                    MaterialTheme.colorScheme.primary
                else NangalColors.Green
            )

            Spacer(Modifier.height(4.dp))

            Text(
                item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(2.dp))

            Text(
                item.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )
        }
    }
}

// ============================================================
// TWO COLUMN ADS
// ============================================================

@Composable
fun TwoColAdRow(
    items: List<Announcement>,
    onClick: (Announcement) -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {

        items.forEach { item ->

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick(item) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(Modifier.padding(10.dp)) {

                    AdImageArea(
                        item.imageUrl,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        item.description,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2
                    )
                }
            }
        }

        if (items.size == 1) {
            Spacer(Modifier.weight(1f))
        }
    }
}

// ============================================================
// HEADLINE TICKER (reversed: left → right)
// ============================================================

@Composable
fun HeadlineTicker(newsItems: List<Announcement>) {
    val tickerText = remember(newsItems) {
        if (newsItems.isEmpty()) "NANGAL TIMES — Stay tuned for local updates"
        else newsItems.joinToString("      •      ") { it.title }
    }
    var containerWidth by remember { mutableStateOf(0f) }
    var textWidth by remember { mutableStateOf(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Text enters fully from the right edge, scrolls to the left until it has completely
    // left the screen, then restarts from the right edge again (no mid-screen pop-in).
    val progress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(containerWidth, textWidth) {
        if (containerWidth <= 0f || textWidth <= 0f) return@LaunchedEffect
        val distancePx = containerWidth + textWidth
        val speedPxPerSec = with(density) { 70.dp.toPx() }
        val durationMs = ((distancePx / speedPxPerSec) * 1000).toInt().coerceAtLeast(4000)
        while (true) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMs, easing = LinearEasing))
        }
    }

    val startX = containerWidth
    val x = startX - progress.value * (containerWidth + textWidth)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fixed label chip
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onBackground)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(com.nangalsanjh.app.theme.BrandAmber, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "NEWS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.background,
                    letterSpacing = 1.2.sp
                )
            }

            // Scrolling area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clipToBounds()
                    .onGloballyPositioned { containerWidth = it.size.width.toFloat() },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = tickerText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .wrapContentWidth(align = Alignment.Start, unbounded = true)
                        .onGloballyPositioned { textWidth = it.size.width.toFloat() }
                        .offset { IntOffset(x.roundToInt(), 0) }
                )
            }
        }
        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

// ============================================================
// EMERGENCY ANNOUNCEMENT
// ============================================================

@Composable
fun EmergencyAnnouncementCard(
    announcement: Announcement,
    onClick: () -> Unit
) {

    val context = LocalContext.current

    NSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {

        Column(Modifier.padding(Spacing.lg)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text("🚨", fontSize = 22.sp)

                Spacer(Modifier.width(Spacing.sm))

                Text(
                    "Emergency Need",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.error
                )

                if (announcement.bloodType.isNotEmpty()) {

                    Spacer(Modifier.width(Spacing.sm))

                    Surface(
                        shape = Radius.md,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ) {

                        Text(
                            "🩸 ${announcement.bloodType}",
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 3.dp
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))

            AdImageArea(
                announcement.imageUrl,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Spacing.sm))

            Text(
                announcement.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            Text(
                announcement.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 19.sp,
                maxLines = 2
            )

            if (announcement.location.isNotEmpty()) {

                Spacer(Modifier.height(6.dp))

                Text(
                    "📍 ${announcement.location}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (announcement.contact.isNotEmpty()) {

                Spacer(Modifier.height(10.dp))

                NSButton(
                    text = "Call ${announcement.contact}",
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:${announcement.contact}")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Default.Call
                )
            }
        }
    }
}

// ============================================================
// IMAGE AREA
// ============================================================

@Composable
fun AdImageArea(
    imageUrl: String,
    modifier: Modifier = Modifier
) {

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
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
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

            Icon(
                Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ============================================================
// ANNOUNCEMENT DETAIL DIALOG
// ============================================================

@Composable
fun AnnouncementDetailDialog(
    announcement: Announcement,
    onDismiss: () -> Unit
) {

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = null,

        text = {

            Column {

                AdImageArea(
                    announcement.imageUrl,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (announcement.type) {
                            "emergency" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            "ad" -> Color(0xFF138A4B).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {

                        Text(
                            announcement.type.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 3.dp
                            ),
                            fontSize = 11.sp,
                            color = when (announcement.type) {
                                "emergency" -> MaterialTheme.colorScheme.error
                                "ad" -> Color(0xFF138A4B)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }

                    Text(
                        getTimeAgo(announcement.timeStamp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    announcement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    announcement.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )

                if (announcement.bloodType.isNotEmpty()) {

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "🩸 Blood needed: ${announcement.bloodType}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (announcement.location.isNotEmpty()) {

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "📍 ${announcement.location}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        },

        confirmButton = {

            if (announcement.contact.isNotEmpty()) {

                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:${announcement.contact}")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {

                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text("Call ${announcement.contact}")
                }
            }
        },

        dismissButton = {

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close")
            }
        }
    )
}

// ============================================================
// WORKER FEED — 24/7 AVAILABILITY + OpenToAll support
// ============================================================

@Composable
fun WorkerFeedContent(userName: String) {

    val postViewModel: PostViewModel = viewModel()

    val reportViewModel: com.nangalsanjh.app.viewmodel.ReportViewModel = viewModel()

    val blockedUids by reportViewModel.blockedUids.collectAsState()

    val posts by postViewModel.posts.collectAsState()

    val isLoadingPosts by postViewModel.isLoading.collectAsState()

    val postSuccess by postViewModel.postSuccess.collectAsState()

    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var isAvailable by remember {
        mutableStateOf(false)
    }

    var workerCategory by remember {
        mutableStateOf("")
    }

    var nowTick by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    var showPostDialog by remember {
        mutableStateOf(false)
    }

    // Subscribe every worker to the broadcast topic
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            com.google.firebase.messaging.FirebaseMessaging
                .getInstance()
                .subscribeToTopic("all_workers")
        }
    }

    // Subscribe worker to their category topic
    LaunchedEffect(workerCategory) {

        if (workerCategory.isNotEmpty()) {

            val topic = "category_" +
                    workerCategory.replace(Regex("[^a-zA-Z]"), "")

            com.google.firebase.messaging.FirebaseMessaging
                .getInstance()
                .subscribeToTopic(topic)
        }
    }

    // Close post dialog after successful posting
    LaunchedEffect(postSuccess) {

        if (postSuccess) {
            showPostDialog = false
            postViewModel.resetPostSuccess()
        }
    }

    // Update timer every second
    LaunchedEffect(Unit) {

        while (true) {
            delay(1000)
            nowTick = System.currentTimeMillis()
        }
    }

    // Cleanup expired jobs
    LaunchedEffect(uid) {

        while (true) {
            postViewModel.cleanupExpiredAcceptWindow()
            delay(30_000)
        }
    }

    // Firebase user listener
    DisposableEffect(uid) {

        if (uid.isEmpty()) {
            onDispose { }
        } else {

            postViewModel.cleanupExpiredWorkerPosts(uid)

            val listener = db.collection("users")
                .document(uid)
                .addSnapshotListener { doc, _ ->

                    isAvailable = doc?.getBoolean("isAvailable") ?: false

                    workerCategory = doc?.getString("category") ?: ""
                }

            onDispose {
                listener.remove()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // WORKER HEADER
            Column(modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.lg)) {
                Text(
                    "WORKER  ·  ਕਾਮਾ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    "Hi ${userName.trim().substringBefore(" ").ifEmpty { "there" }}, ready for work?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // AVAILABILITY CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Spacing.lg,
                        vertical = Spacing.md
                    ),
                shape = Radius.lg,
                colors = CardDefaults.cardColors(
                    containerColor = if (isAvailable) NangalColors.Green.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAvailable) NangalColors.Green.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column {

                        Text(
                            "Availability Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            if (isAvailable)
                                "You're online — jobs will find you"
                            else
                                "You're offline",

                            fontSize = 11.sp,

                            color = if (isAvailable)
                                NangalColors.Green
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isAvailable,

                        onCheckedChange = { newValue ->

                            if (uid.isNotEmpty()) {

                                isAvailable = newValue

                                db.collection("users")
                                    .document(uid)
                                    .update("isAvailable", newValue)
                                    .addOnFailureListener {
                                        isAvailable = !newValue
                                    }
                            }
                        }
                    )
                }
            }

            NSDivider()

            // FILTER JOBS — category match OR open-to-all
            val roleFiltered = posts.filter {
                it.userRole == "seeker" &&
                        it.status == "open" &&
                        uid !in it.declinedBy &&
                        it.uid !in blockedUids &&
                        (nowTick - it.timeStamp) < PostViewModel.JOB_ACCEPT_WINDOW_MS
            }

            val filtered = roleFiltered.filter { post ->
                post.isOpenToAll ||
                        workerCategory.isEmpty() ||
                        post.category.contains(
                            workerCategory
                                .replace(Regex("[^a-zA-Z ]"), "")
                                .trim(),
                            ignoreCase = true
                        )
            }

            // EMPTY STATE
            if (filtered.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    NSEmptyState(
                        emoji = "📭",
                        title = "No jobs in demand",
                        subtitle = "Seeker requests matching your category (or open to all) will show here"
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

                                postViewModel.acceptPost(
                                    post.id,
                                    uid,
                                    userName
                                )

                                val intent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:${post.userPhone}")
                                )

                                context.startActivity(intent)
                            },

                            onDecline = {
                                postViewModel.declinePost(
                                    post.id,
                                    uid
                                )
                            }
                        )
                    }
                }
            }
        }

        // POST SERVICE BUTTON
        FloatingActionButton(
            onClick = {
                showPostDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.lg),
            shape = Radius.md,
            containerColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {

            Icon(
                Icons.Default.Add,
                contentDescription = "Post Service",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    // POST SERVICE DIALOG
    if (showPostDialog) {

        WorkerPostDialog(
            onDismiss = {
                showPostDialog = false
            },

            onPost = { cat, desc, price, avail ->

                postViewModel.createWorkerPost(
                    userName,
                    cat,
                    desc,
                    price,
                    avail
                )
            },

            isLoading = isLoadingPosts
        )
    }
}

// ============================================================
// WORKER POST DIALOG
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerPostDialog(
    onDismiss: () -> Unit,
    onPost: (String, String, String, String) -> Unit,
    isLoading: Boolean
) {

    var description by remember { mutableStateOf("") }

    var price by remember { mutableStateOf("") }

    var availability by remember { mutableStateOf("") }

    var selectedCategory by remember {
        mutableStateOf("🔧 Plumber")
    }

    var showAvailabilityMenu by remember {
        mutableStateOf(false)
    }

    val categories = listOf(
        "🔧 Plumber",
        "⚡ Electrician",
        "🎨 Painter",
        "🪚 Carpenter",
        "🧹 Cleaner",
        "👷 Labour",
        "🚗 Automobile",
        "🩺 Medical Tests",
        "🚕 Taxi Service",
        "🐾 Veterinary",
        "❓ Other"
    )

    val availabilityOptions = listOf(
        "9 AM - 7 PM",
        "Morning only",
        "Evening only",
        "Weekdays only",
        "Weekends only",
        "Anytime"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Radius.xl,
        containerColor = MaterialTheme.colorScheme.surface,

        title = {
            Text(
                "Post Service",
                style = MaterialTheme.typography.titleMedium
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {

                Text(
                    "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {

                    items(categories) { cat ->

                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                            },
                            label = {
                                Text(
                                    cat,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.onBackground,
                                selectedLabelColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                }

                NSTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Describe your service",
                    placeholder = "Skills, experience...",
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                NSTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = "Your Rate",
                    placeholder = "₹500/day",
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = showAvailabilityMenu,
                    onExpandedChange = {
                        showAvailabilityMenu = it
                    }
                ) {

                    OutlinedTextField(
                        value = availability,
                        onValueChange = {
                            availability = it
                        },
                        label = {
                            Text("Availability")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        shape = Radius.md,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = showAvailabilityMenu
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = showAvailabilityMenu,
                        onDismissRequest = {
                            showAvailabilityMenu = false
                        }
                    ) {

                        availabilityOptions.forEach { option ->

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        option,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    availability = option
                                    showAvailabilityMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {

            NSButton(
                text = "Post Service",
                onClick = {
                    onPost(
                        selectedCategory,
                        description,
                        price,
                        availability
                    )
                },
                enabled = description.isNotBlank(),
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        },

        dismissButton = {

            NSOutlinedButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

// ============================================================
// JOB DEMAND CARD (with Broadcast badge)
// ============================================================

@Composable
fun JobDemandCard(
    post: Post,
    nowTick: Long,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val timeAgo = remember(post.timeStamp) { getTimeAgo(post.timeStamp) }
    val remainingMs = (post.timeStamp + PostViewModel.JOB_ACCEPT_WINDOW_MS - nowTick).coerceAtLeast(0)
    val minutes = (remainingMs / 1000) / 60
    val seconds = (remainingMs / 1000) % 60
    val isUrgent = remainingMs < 2 * 60 * 1000
    val cleanCategory = post.category.replace(Regex("[^a-zA-Z ]"), "").trim().ifEmpty { post.category }
    val progress = (remainingMs.toFloat() / PostViewModel.JOB_ACCEPT_WINDOW_MS.toFloat()).coerceIn(0f, 1f)
    val timerColor = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    NSCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.lg)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        cleanCategory,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (post.isOpenToAll) {
                        Spacer(modifier = Modifier.width(8.dp))
                        NSStatusBadge(text = "Broadcast", color = Color(0xFF1F5FBF))
                    }
                }
                Text(
                    timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                post.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(post.userName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (post.address.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(post.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Countdown
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = timerColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Respond in ${minutes}:${seconds.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelLarge,
                    color = timerColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(Radius.full),
                color = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                trackColor = MaterialTheme.colorScheme.secondaryContainer
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NSOutlinedButton(
                    text = "Decline",
                    onClick = onDecline,
                    modifier = Modifier.weight(1f)
                )
                NSButton(
                    text = "Accept & Call",
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Call
                )
            }
        }
    }
}
