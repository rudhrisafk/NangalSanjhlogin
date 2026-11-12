package com.nangalsanjh.app.navigation

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import com.google.firebase.firestore.FirebaseFirestore
import com.nangalsanjh.app.ui.screens.AdminPasscodeScreen
import com.nangalsanjh.app.ui.screens.HomeScreen
import com.nangalsanjh.app.ui.screens.LoginScreen
import com.nangalsanjh.app.ui.screens.OTPScreen
import com.nangalsanjh.app.ui.screens.ProfileSetupScreen
import com.nangalsanjh.app.ui.screens.RoleSelectionScreen
import com.nangalsanjh.app.ui.screens.SplashScreen
import com.nangalsanjh.app.ui.screens.WorkerCategoryScreen
import com.nangalsanjh.app.viewmodel.AuthViewModel
import com.nangalsanjh.app.viewmodel.ThemeViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object OTP : Screen("otp")
    object ProfileSetup : Screen("profile_setup")
    object RoleSelection : Screen("role_selection")
    object WorkerCategory : Screen("worker_category")
    object AdminPasscode : Screen("admin_passcode")
    object Home : Screen("home")
}

@Composable
fun NangalSevaNavHost(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val otpSent by authViewModel.otpSent.collectAsState()
    val verificationComplete by authViewModel.verificationComplete.collectAsState()
    val profileSaved by authViewModel.profileSaved.collectAsState()
    val lastPhone by authViewModel.lastPhone.collectAsState()
    val savedName by authViewModel.savedName.collectAsState()
    val savedRole by authViewModel.savedRole.collectAsState()

    var tempName by remember { mutableStateOf("") }
    var tempAddress by remember { mutableStateOf("") }
    var tempRole by remember { mutableStateOf("") }
    var isReturningUser by remember { mutableStateOf(false) }
    var existingBloodGroup by remember { mutableStateOf("") }

    // Login Persistence
    var startDestination by remember { mutableStateOf(Screen.Login.route) }
    // Splash animation plays while the auth check runs; both must finish before we continue
    var minSplashDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(650); minSplashDone = true }
    var isCheckingAuth by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val name = doc.getString("name") ?: ""
                        val role = doc.getString("role") ?: "seeker"
                        startDestination = "${Screen.Home.route}/$name/$role"
                    }
                    isCheckingAuth = false
                }
                .addOnFailureListener {
                    isCheckingAuth = false
                }
        } else {
            isCheckingAuth = false
        }
    }

    if (isCheckingAuth || !minSplashDone) {
        SplashScreen { }
        return
    }

    LaunchedEffect(otpSent) {
        if (otpSent) {
            navController.navigate("${Screen.OTP.route}/$lastPhone") {
                launchSingleTop = true
            }
            authViewModel.resetOtpSent()
        }
    }

    // ✅ Fix — Returning user check karo
    LaunchedEffect(verificationComplete) {
        if (verificationComplete) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            // Returning user — seedha home
                            val name = doc.getString("name") ?: ""
                            val role = doc.getString("role") ?: "seeker"
                            navController.navigate("${Screen.Home.route}/$name/$role") {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            // New user — profile setup
                            navController.navigate(Screen.ProfileSetup.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        authViewModel.resetState()
                    }
            }
        }
    }

    LaunchedEffect(profileSaved) {
        if (profileSaved) {
            navController.navigate("${Screen.Home.route}/$savedName/$savedRole") {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
            authViewModel.resetProfileSaved()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { phone ->
                    // Check returning user
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .whereEqualTo("phone", phone)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty) {
                                isReturningUser = true
                                existingBloodGroup =
                                    snapshot.documents[0].getString("bloodGroup") ?: ""
                            }
                        }
                    (context as? Activity)?.let { activity ->
                        authViewModel.sendOTP(phone, activity)
                    }
                },
                onBloodInfoSaved = { bloodGroup, age, consent ->
                    authViewModel.setBloodInfo(bloodGroup, age, consent)
                },
                isLoading = isLoading,
                error = error,
                isReturningUser = isReturningUser,
                existingBloodGroup = existingBloodGroup
            )
        }

        composable(
            route = "${Screen.OTP.route}/{phoneNumber}",
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OTPScreen(
                phoneNumber = phoneNumber,
                onVerifyClick = { otp -> authViewModel.verifyOTP(otp) },
                onResendClick = {
                    (context as? Activity)?.let { activity ->
                        authViewModel.resendOTP(phoneNumber, activity)
                    }
                },
                isLoading = isLoading,
                error = error,
            )
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onContinueClick = { name, address ->
                    tempName = name
                    tempAddress = address
                    navController.navigate(Screen.RoleSelection.route)
                },
                isLoading = isLoading,
                error = error,
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen { role ->
                tempRole = role
                when (role) {
                    "worker" -> navController.navigate(Screen.WorkerCategory.route)
                    "admin" -> navController.navigate(Screen.AdminPasscode.route)
                    else -> authViewModel.saveUserProfile(tempName, tempAddress, role)
                }
            }
        }

        composable(Screen.WorkerCategory.route) {
            WorkerCategoryScreen(
                onCategorySelected = { category, price, about, aadhar, experience ->
                    authViewModel.saveUserProfile(
                        tempName, tempAddress, tempRole, category, price, about, aadhar, experience
                    )
                },
                isLoading = isLoading
            )
        }

        composable(Screen.AdminPasscode.route) {
            AdminPasscodeScreen(
                onPasscodeVerified = {
                    authViewModel.saveUserProfile(tempName, tempAddress, tempRole)
                },
                isLoading = isLoading,
            )
        }

        composable(
            route = "${Screen.Home.route}/{userName}/{userRole}",
            arguments = listOf(
                navArgument("userName") { type = NavType.StringType },
                navArgument("userRole") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val userRole = backStackEntry.arguments?.getString("userRole") ?: "seeker"
            HomeScreen(
                userRole = userRole,
                userName = userName,
                themeViewModel = themeViewModel,
                onLogout = {
                    authViewModel.logout()
                    isReturningUser = false
                    existingBloodGroup = ""
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
    }
}