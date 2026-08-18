package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nangalsanjh.app.R
import com.nangalsanjh.app.viewmodel.AppLanguage
import com.nangalsanjh.app.viewmodel.AppTheme
import com.nangalsanjh.app.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRole: String,
    userName: String,
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit = {},
    onRoleSwitch: ((String) -> Unit)? = null,
    isProfileSetup: Boolean = true
) {
    var activeRole by remember { mutableStateOf(userRole) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAIAssistant by remember { mutableStateOf(false) }
    var showEmergency by remember { mutableStateOf(false) }
    var showBloodDonors by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }

    var showProfileSetupPopup by remember(isProfileSetup) { mutableStateOf(!isProfileSetup) }

    val theme by themeViewModel.theme.collectAsState()
    val language by themeViewModel.language.collectAsState()

    LaunchedEffect(activeRole) {
        selectedTab = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.nangal_seva_logo),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Nangal Seva",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (activeRole == "worker" || activeRole == "admin") {
                        IconButton(onClick = { showEmergency = true }) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFB71C1C).copy(alpha = 0.12f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🚨", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            Icons.Default.Settings,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { showProfile = true }) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    when (activeRole) {
                                        "worker" -> "👷"
                                        "admin" -> "🏛️"
                                        else -> "👤"
                                    },
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        getNavItems(activeRole).forEachIndexed { index, item ->
                            if (item.isSOS) {
                                NavigationBarItem(
                                    icon = {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFB71C1C),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Text("🚨", fontSize = 16.sp)
                                                    Text(
                                                        "SOS",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White,
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    label = {},
                                    selected = false,
                                    onClick = { showEmergency = true },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color.Transparent
                                    )
                                )
                            } else {
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            item.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (activeRole == "seeker") {
                FloatingActionButton(
                    onClick = { showAIAssistant = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp)
                ) {
                    Text("🤖", fontSize = 20.sp)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeRole) {
                "seeker" -> SeekerContent(selectedTab, userName, onLogout)
                "worker" -> WorkerContent(selectedTab, userName, onLogout)
                "admin" -> AdminContent(selectedTab, userName, onLogout)
                else -> SeekerContent(selectedTab, userName, onLogout)
            }
        }
    }

    if (showAIAssistant) {
        AIAssistantDialog { showAIAssistant = false }
    }

    if (showProfileSetupPopup) {
        AlertDialog(
            onDismissRequest = { showProfileSetupPopup = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👤", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Your Profile", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Your profile isn't set up yet. Complete it to get the most out of Nangal Seva.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showProfileSetupPopup = false
                    showProfile = true
                }) { Text("Set Up Now") }
            },
            dismissButton = {
                TextButton(onClick = { showProfileSetupPopup = false }) { Text("Later") }
            }
        )
    }

    if (showProfile) {
        Dialog(
            onDismissRequest = { showProfile = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showProfile = false }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            "Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    ProfileScreen(
                        userName = userName,
                        userRole = activeRole,
                        onLogout = {
                            showProfile = false
                            onLogout()
                        },
                        onRoleSwitch = { newRole ->
                            activeRole = newRole
                            selectedTab = 0
                            showProfile = false
                            onRoleSwitch?.invoke(newRole)
                        }
                    )
                }
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Settings,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settings", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Theme",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (theme != AppTheme.DARK) themeViewModel.toggleTheme() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = if (theme == AppTheme.DARK)
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                "🌙 Dark",
                                fontSize = 13.sp,
                                color = if (theme == AppTheme.DARK)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        OutlinedButton(
                            onClick = { if (theme != AppTheme.LIGHT) themeViewModel.toggleTheme() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = if (theme == AppTheme.LIGHT)
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                "☀️ Light",
                                fontSize = 13.sp,
                                color = if (theme == AppTheme.LIGHT)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Language / ਭਾਸ਼ਾ / भाषा",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            AppLanguage.ENGLISH to "🇬🇧 English",
                            AppLanguage.HINDI to "🇮🇳 हिंदी",
                            AppLanguage.PUNJABI to "🌾 ਪੰਜਾਬੀ"
                        ).forEach { (lang, label) ->
                            OutlinedButton(
                                onClick = { themeViewModel.setLanguage(lang) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = if (language == lang)
                                    ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    )
                                else ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text(
                                    label,
                                    fontSize = 14.sp,
                                    color = if (language == lang)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) { Text("Done") }
            }
        )
    }

    if (showEmergency) {
        Dialog(
            onDismissRequest = {
                showEmergency = false
                showBloodDonors = false
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showBloodDonors) {
                            IconButton(onClick = { showBloodDonors = false }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack, null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                        IconButton(onClick = {
                            showEmergency = false
                            showBloodDonors = false
                        }) {
                            Icon(
                                Icons.Default.Close, null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (showBloodDonors) {
                        BloodDonorsScreen(userRole = activeRole)
                    } else {
                        EmergencyScreen(onBloodDonors = { showBloodDonors = true })
                    }
                }
            }
        }
    }
}

data class NavItem(
    val label: String,
    val icon: ImageVector = Icons.Default.Home,
    val isSOS: Boolean = false
)

fun getNavItems(role: String): List<NavItem> = when (role) {
    "worker" -> listOf(
        NavItem("Feed", Icons.Default.Home),
        NavItem("Map", Icons.Default.Map)
    )
    "admin" -> listOf(
        NavItem("Complaints", Icons.Default.Report),
        NavItem("Resolved", Icons.Default.CheckCircle),
        NavItem("Announcements", Icons.Default.Campaign),
        NavItem("Map", Icons.Default.Map)
    )
    else -> listOf(
        NavItem("Feed", Icons.Default.Home),
        NavItem("Services", Icons.Default.Build),
        NavItem("SOS", isSOS = true),
        NavItem("Complaints", Icons.Default.Report),
        NavItem("Map", Icons.Default.Map)
    )
}

@Composable
fun SeekerContent(tab: Int, userName: String, onLogout: () -> Unit = {}) {
    when (tab) {
        0 -> CommunityFeedScreen(userName = userName, userRole = "seeker")
        1 -> ServicesScreen()
        3 -> ComplaintsScreen(userRole = "seeker", userName = userName)
        4 -> MapScreen(userRole = "seeker")
        else -> CommunityFeedScreen(userName = userName, userRole = "seeker")
    }
}

@Composable
fun WorkerContent(tab: Int, userName: String, onLogout: () -> Unit = {}) {
    when (tab) {
        0 -> CommunityFeedScreen(userName = userName, userRole = "worker")
        1 -> MapScreen(userRole = "worker")
        else -> CommunityFeedScreen(userName = userName, userRole = "worker")
    }
}

@Composable
fun AdminContent(tab: Int, userName: String, onLogout: () -> Unit = {}) {
    when (tab) {
        0 -> ComplaintsScreen(userRole = "admin", userName = userName)
        1 -> ResolvedComplaintsScreen()
        2 -> AdminAnnouncementsScreen()
        3 -> MapScreen(userRole = "admin")
        else -> ComplaintsScreen(userRole = "admin", userName = userName)
    }
}