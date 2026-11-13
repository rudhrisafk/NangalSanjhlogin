package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.scale
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
    isProfileSetup: Boolean = true,
) {
    var activeRole by remember { mutableStateOf(userRole) }
    var selectedTab by remember { mutableStateOf(0) }
    var showAIAssistant by remember { mutableStateOf(false) }
    var showEmergency by remember { mutableStateOf(false) }
    var showBloodDonors by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var showProfileSetupPopup by remember(isProfileSetup) { mutableStateOf(!isProfileSetup) }

    val theme by themeViewModel.theme.collectAsState()
    val language by themeViewModel.language.collectAsState()

    LaunchedEffect(activeRole) {
        selectedTab = 0
    }

    if (showAIAssistant) {
        AIAssistantDialog { showAIAssistant = false }
        return
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
                        Column {
                            Text(
                                "Nangal Seva",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    brush = flowingTitleBrush(MaterialTheme.colorScheme.onBackground)
                                ),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "ਨੰਗਲ ਸੇਵਾ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    if (activeRole == "worker" || activeRole == "admin") {
                        IconButton(onClick = { showEmergency = true }) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFB3101B).copy(alpha = 0.12f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Warning, contentDescription = "Emergency", tint = Color(0xFFB3101B), modifier = Modifier.size(18.dp))
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
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    when (activeRole) {
                                        "worker" -> Icons.Default.Build
                                        "admin" -> Icons.Default.AdminPanelSettings
                                        else -> Icons.Default.AccountCircle
                                    },
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
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
                                            color = Color(0xFFB3101B),
                                            modifier = Modifier.size(48.dp).pulseRing(Color(0xFFB3101B))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(18.dp))
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
                                    onClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        showEmergency = true
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color.Transparent
                                    )
                                )
                            } else {
                                val bounce by androidx.compose.animation.core.animateFloatAsState(
                                    targetValue = if (selectedTab == index) 1.2f else 1f,
                                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.35f, stiffness = 380f),
                                    label = "navBounce"
                                )
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.size(22.dp).scale(bounce)
                                        )
                                    },
                                    label = {
                                        Text(
                                            item.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    },
                                    selected = selectedTab == index,
                                    onClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        selectedTab = index
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
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
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(52.dp)
                        .spinningRing(listOf(Color(0xFF3A7BFF), Color(0xFF8E5BFF), Color(0xFF19C3B1)))
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Open Nangal AI assistant",
                        modifier = Modifier.size(24.dp).scale(breathing())
                    )
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
                "seeker" -> SeekerContent(selectedTab, userName, onLogout, onNavigate = { if (it == 2) showEmergency = true else selectedTab = it })
                "worker" -> WorkerContent(selectedTab, userName, onLogout)
                "admin" -> AdminContent(selectedTab, userName, onLogout)
                else -> SeekerContent(selectedTab, userName, onLogout, onNavigate = { if (it == 2) showEmergency = true else selectedTab = it })
            }
        }
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
                Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showProfile = false }) {
                            Icon(
                                Icons.Default.ArrowBack,
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            onClick = { themeViewModel.setTheme(AppTheme.DARK) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = if (theme == AppTheme.DARK)
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                "Dark",
                                fontSize = 13.sp,
                                color = if (theme == AppTheme.DARK)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        OutlinedButton(
                            onClick = { themeViewModel.setTheme(AppTheme.LIGHT) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = if (theme == AppTheme.LIGHT)
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                "Light",
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
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                        )
                ) {
                    if (showBloodDonors) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showBloodDonors = false }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            IconButton(onClick = {
                                showEmergency = false
                                showBloodDonors = false
                            }) {
                                Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        BloodDonorsScreen(userRole = activeRole)
                    } else {
                        EmergencyScreen(
                            onBloodDonors = { showBloodDonors = true },
                            onClose = { showEmergency = false }
                        )
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
        NavItem("Dashboard", Icons.Default.Dashboard),
        NavItem("Issues", Icons.Default.Report),
        NavItem("Resolved", Icons.Default.CheckCircle),
        NavItem("Reports", Icons.Default.Flag),
        NavItem("Notices", Icons.Default.Campaign),
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
fun SeekerContent(tab: Int, userName: String, onLogout: () -> Unit, onNavigate: (Int) -> Unit = {}) {
    when (tab) {
        0 -> CommunityFeedScreen(userName = userName, userRole = "seeker", onNavigate = onNavigate)
        1 -> ServicesScreen()
        3 -> ComplaintsScreen(userRole = "seeker", userName = userName)
        4 -> MapScreen(userRole = "seeker")
        else -> CommunityFeedScreen(userName = userName, userRole = "seeker", onNavigate = onNavigate)
    }
}

@Composable
fun WorkerContent(tab: Int, userName: String, onLogout: () -> Unit) {
    when (tab) {
        0 -> CommunityFeedScreen(userName = userName, userRole = "worker")
        1 -> MapScreen(userRole = "worker")
        else -> CommunityFeedScreen(userName = userName, userRole = "worker")
    }
}

@Composable
fun AdminContent(tab: Int, userName: String, onLogout: () -> Unit) {
    when (tab) {
        0 -> AdminDashboardScreen()
        1 -> ComplaintsScreen(userRole = "admin", userName = userName)
        2 -> ResolvedComplaintsScreen()
        3 -> AdminReportsScreen()
        4 -> AdminAnnouncementsScreen()
        5 -> MapScreen(userRole = "admin")
        else -> AdminDashboardScreen()
    }
}