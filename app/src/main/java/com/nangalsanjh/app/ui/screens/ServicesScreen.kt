package com.nangalsanjh.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nangalsanjh.app.viewmodel.FavoriteViewModel
import com.nangalsanjh.app.viewmodel.Post
import com.nangalsanjh.app.viewmodel.PostViewModel
import com.nangalsanjh.app.viewmodel.WorkerModel
import com.nangalsanjh.app.viewmodel.WorkerViewModel
import java.util.Locale

enum class JobSort(val label: String) {
    DEFAULT("Default"),
    TOP_RATED("⭐ Top Rated"),
    PRICE_LOW_HIGH("💰 Price: Low-High"),
    PRICE_HIGH_LOW("💰 Price: High-Low")
}

@Composable
fun ServicesScreen() {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Job Posts, 1 = My Favorites

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            eyebrow = "SERVICES  ·  ਸੇਵਾਵਾਂ",
            title = "Find trusted workers in Nangal",
            subtitle = "Browse job posts, save favourites or post your own request."
        )
        SegmentedTabs(
            labels = listOf("Job Posts", "My Favorites"),
            selected = selectedTab,
            onSelect = { selectedTab = it }
        )
        Spacer(Modifier.height(4.dp))

        if (selectedTab == 0) JobPostsTab() else FavoritesTab()
    }
}

@Composable
fun JobPostsTab() {
    val workerViewModel: WorkerViewModel = viewModel()
    val workers by workerViewModel.workers.collectAsState()
    val postViewModel: PostViewModel = viewModel()
    val allPosts by postViewModel.posts.collectAsState()
    val isLoading by postViewModel.isLoading.collectAsState()
    val postSuccess by postViewModel.postSuccess.collectAsState()
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf(JobSort.DEFAULT) }
    var detailPost by remember { mutableStateOf<Post?>(null) }
    var showJobRequestDialog by remember { mutableStateOf(false) }

    val filters = listOf(
        "All", "🔧 Plumber", "⚡ Electrician", "🎨 Painter", "🪚 Carpenter",
        "🧹 Cleaner", "👷 Labour", "🚗 Automobile", "🩺 Medical Tests", "🚕 Taxi Service", "🐾 Veterinary"
    )

    val workerByUid = remember(workers) { workers.associateBy { it.uid } }

    val jobPosts = remember(allPosts) {
        val cutoff = System.currentTimeMillis() - PostViewModel.WORKER_POST_EXPIRY_MS
        allPosts.filter { it.userRole == "worker" && it.timeStamp >= cutoff }
    }

    LaunchedEffect(postSuccess) {
        if (postSuccess) {
            showJobRequestDialog = false
            postViewModel.resetPostSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search job posts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null) }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(JobSort.entries.toList()) { sort ->
                    FilterChip(
                        selected = sortBy == sort,
                        onClick = { sortBy = sort },
                        label = { Text(sort.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(selectedFilter).coerceAtLeast(0),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp
            ) {
                filters.forEach { filter ->
                    Tab(selected = selectedFilter == filter, onClick = { selectedFilter = filter }, text = { Text(filter, fontSize = 12.sp) })
                }
            }

            if (isLoading && jobPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                fun priceValue(price: String): Double = price.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: Double.MAX_VALUE

                var filtered = jobPosts
                    .filter { post ->
                        if (searchQuery.isNotEmpty()) post.userName.contains(searchQuery, ignoreCase = true) || post.category.contains(searchQuery, ignoreCase = true)
                        else true
                    }
                    .filter { post ->
                        if (selectedFilter == "All") true
                        else post.category.contains(selectedFilter.replace(Regex("[^a-zA-Z ]"), "").trim(), ignoreCase = true)
                    }

                filtered = when (sortBy) {
                    JobSort.TOP_RATED -> filtered.sortedByDescending { workerByUid[it.uid]?.rating ?: 0f }
                    JobSort.PRICE_LOW_HIGH -> filtered.sortedBy { priceValue(it.price) }
                    JobSort.PRICE_HIGH_LOW -> filtered.sortedByDescending { priceValue(it.price) }
                    JobSort.DEFAULT -> filtered.sortedByDescending { it.timeStamp }
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📭", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (searchQuery.isNotEmpty()) "No job posts found for \"$searchQuery\"" else "No job posts in this category yet",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filtered) { post ->
                            JobPostCard(
                                post = post,
                                worker = workerByUid[post.uid],
                                onCall = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${post.userPhone}") }
                                    context.startActivity(intent)
                                },
                                onMoreClick = { detailPost = post }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showJobRequestDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.AddLocationAlt, contentDescription = "Post Job Request", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }

    if (showJobRequestDialog) {
        SeekerJobRequestDialog(
            onDismiss = { showJobRequestDialog = false },
            onPost = { cat, desc, lat, lng, addr, openToAll ->
                postViewModel.createJobRequest("You", cat, desc, lat, lng, addr, openToAll)
            },
            isLoading = isLoading
        )
    }

    detailPost?.let { post ->
        PostDetailDialog(
            post = post,
            worker = workerByUid[post.uid],
            onDismiss = { detailPost = null },
            onCall = {
                val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${post.userPhone}") }
                context.startActivity(intent)
                detailPost = null
            }
        )
    }
}

@Composable
fun FavoritesTab() {
    val viewModel: FavoriteViewModel = viewModel()
    val favorites by viewModel.favorites.collectAsState()
    val context = LocalContext.current

    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💔", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("No saved workers yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text("Tap the heart icon on a job post to save it", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(favorites) { fav ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(fav.workerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(fav.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Row {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${fav.phone}") }
                                context.startActivity(intent)
                            }) { Icon(Icons.Default.Call, contentDescription = "Call") }
                            TextButton(onClick = { viewModel.removeFavorite(fav.workerUid) }) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JobPostCard(post: Post, worker: WorkerModel?, onCall: () -> Unit, onMoreClick: () -> Unit) {
    val favoriteViewModel: FavoriteViewModel = viewModel()
    val favorites by favoriteViewModel.favorites.collectAsState()
    val isFavorite = favorites.any { it.workerUid == post.uid }
    var showReportDialog by remember { mutableStateOf(false) }

    val hoursLeft = ((PostViewModel.WORKER_POST_EXPIRY_MS - (System.currentTimeMillis() - post.timeStamp)) / (60 * 60 * 1000)).toInt().coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(post.category, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.userName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        if (worker != null && worker.rating > 0f) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Star, null, tint = Color(0xFFF59F00), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${"%.1f".format(worker.rating)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
                Row {
                    IconButton(onClick = {
                        if (isFavorite) favoriteViewModel.removeFavorite(post.uid)
                        else favoriteViewModel.addFavorite(post.uid, post.userName, post.category, post.userPhone)
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFD92D3A) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onMoreClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(post.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), maxLines = 2)
            if (post.price.isNotEmpty() || post.availability.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (post.price.isNotEmpty()) Text("💰 ${post.price}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    if (post.availability.isNotEmpty()) Text("🕐 ${post.availability}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Expires in ${hoursLeft}h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCall, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call", fontSize = 14.sp)
                }
                OutlinedButton(onClick = { showReportDialog = true }, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Flag, contentDescription = "Report", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showReportDialog) {
        ReportUserDialog(
            reportedUid = post.uid,
            reportedName = post.userName,
            reporterName = "You",
            onDismiss = { showReportDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekerJobRequestDialog(
    onDismiss: () -> Unit,
    onPost: (String, String, Double, Double, String, Boolean) -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("🔧 Plumber") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var address by remember { mutableStateOf("") }
    var locationFetching by remember { mutableStateOf(false) }
    var isOpenToAll by remember { mutableStateOf(false) }

    val categories = listOf(
        "🔧 Plumber", "⚡ Electrician", "🎨 Painter", "🪚 Carpenter",
        "🧹 Cleaner", "👷 Labour", "🚗 Automobile", "🩺 Medical Tests",
        "🚕 Taxi Service", "🐾 Veterinary", "❓ Other"
    )

    fun fetchLocation() {
        locationFetching = true
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val loc = try {
                lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (ex: SecurityException) {
                null
            }
            if (loc != null) {
                latitude = loc.latitude
                longitude = loc.longitude
                address = try {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault())
                        .getFromLocation(loc.latitude, loc.longitude, 1)
                        ?.firstOrNull()?.getAddressLine(0)
                        ?: "${loc.latitude}, ${loc.longitude}"
                } catch (ex: Exception) {
                    "${loc.latitude}, ${loc.longitude}"
                }
            }
        }
        locationFetching = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLocation()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Post a Job Request") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // "Not Sure / Any Worker" toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOpenToAll)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Not sure who to ask?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Broadcast this to every worker, regardless of category",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = isOpenToAll,
                            onCheckedChange = { isOpenToAll = it }
                        )
                    }
                }

                // Category chips (hidden when open to all)
                if (!isOpenToAll) {
                    Text(
                        "Category",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text(
                            if (isOpenToAll) "What do you need? (e.g. DJ for event)"
                            else "Describe the problem"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) fetchLocation()
                        else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (locationFetching) "Fetching..."
                        else if (address.isNotEmpty()) "📍 $address"
                        else "📍 Use Current Location"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalCategory = if (isOpenToAll) "🌐 Any Worker" else selectedCategory
                    onPost(finalCategory, description, latitude, longitude, address, isOpenToAll)
                },
                enabled = description.isNotBlank() && latitude != 0.0 && longitude != 0.0
            ) {
                Text("Post Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PostDetailDialog(
    post: Post,
    worker: WorkerModel?,
    onDismiss: () -> Unit,
    onCall: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = null,
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(post.category.take(2), fontSize = 26.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            post.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            post.category,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (worker != null && worker.rating > 0f) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = Color(0xFFF59F00),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    "${"%.1f".format(worker.rating)} · ${worker.reviewCount} reviews",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                if (worker != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard(count = worker.jobsDone.toString(), label = "Jobs Done", emoji = "✅")
                        StatCard(
                            count = if (worker.isAvailable) "Yes" else "No",
                            label = "Available",
                            emoji = "🟢"
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    "Job Description",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    post.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 19.sp
                )

                if (worker?.experience?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "🛠️ ${worker.experience} years experience",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!worker?.about.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Experience / About",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        worker!!.about,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 19.sp
                    )
                }

                if (post.price.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "💰 ${post.price}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (post.availability.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "🕐 ${post.availability}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                if (worker?.address?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "📍 ${worker.address}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCall,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call Now")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    )
}