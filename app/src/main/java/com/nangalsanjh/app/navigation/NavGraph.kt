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
import com.google.firebase.firestore.FirebaseFirestore
import com.nangalsanjh.app.screens.*
import com.nangalsanjh.app.viewmodel.AuthViewModel
import com.nangalsanjh.app.viewmodel.ProfileViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object OTP : Screen("otp")
    object ProfileSetup : Screen("profile_setup")
    object RoleSelection : Screen("role_selection")
    object WorkerCategory : Screen("worker_category")
    object AdminPasscode : Screen("admin_passcode")
    object Home : Screen("home")
    object AddWorkerProfile : Screen("add_worker_profile")
}

@Composable
fun NangalSevaNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val context = LocalContext.current

    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val otpSent by authViewModel.otpSent.collectAsState()
    val verificationComplete by authViewModel.verificationComplete.collectAsState()
    val profileSaved by authViewModel.profileSaved.collectAsState()
    val lastPhone by authViewModel.lastPhone.collectAsState()
    val savedName by authViewModel.savedName.collectAsState()
    val savedRole by authViewModel.savedRole.collectAsState()

    // ProfileViewModel states
    val activeRole by profileViewModel.activeRole.collectAsState()
    val navigateToWorkerSetup by profileViewModel.navigateToWorkerSetup.collectAsState()

    var tempName by remember { mutableStateOf("") }
    var tempAddress by remember { mutableStateOf("") }
    var tempRole by remember { mutableStateOf("") }

    var startDestination by remember { mutableStateOf(Screen.Splash.route) }
    var isCheckingAuth by remember { mutableStateOf(true) }
    var isReturningUser by remember { mutableStateOf(false) }
    var existingBloodGroup by remember { mutableStateOf("") }

    // Login persistence
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
                        val role = doc.getString("activeRole") ?: doc.getString("role") ?: "seeker"
                        startDestination = "${Screen.Home.route}/$name/$role"
                    }
                    isCheckingAuth = false
                }
                .addOnFailureListener { isCheckingAuth = false }
        } else {
            isCheckingAuth = false
        }
    }

    if (isCheckingAuth) {
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
                            val name = doc.getString("name") ?: ""
                            val role = doc.getString("activeRole") ?: doc.getString("role") ?: "seeker"
                            navController.navigate("${Screen.Home.route}/$name/$role") {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
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

    // Role switch hone par navigate
    LaunchedEffect(activeRole) {
        if (activeRole.isNotEmpty()) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val homeRoute = "${Screen.Home.route}/{userName}/{userRole}"
            if (currentRoute == homeRoute) {
                val name = navController.currentBackStackEntry?.arguments?.getString("userName") ?: ""
                navController.navigate("${Screen.Home.route}/$name/$activeRole") {
                    popUpTo(homeRoute) { inclusive = true }
                }
            }
        }
    }

    // Seeker ne worker profile add karna click kiya
    LaunchedEffect(navigateToWorkerSetup) {
        if (navigateToWorkerSetup) {
            navController.navigate(Screen.AddWorkerProfile.route)
            profileViewModel.resetWorkerSetupTrigger()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Login.route) {
            var isCheckingPhone by remember { mutableStateOf(false) }
            var lastCheckedPhone by remember { mutableStateOf("") }

            // Phone number ke 10 digit complete hote hi background me check kar lo
            // (button dabane se pehle hi pata chal jaye returning user hai ya naya)
            fun checkPhoneIfNeeded(phone: String) {
                if (phone.length == 13 && phone != lastCheckedPhone) { // "+91" + 10 digits
                    lastCheckedPhone = phone
                    isCheckingPhone = true
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .whereEqualTo("phone", phone)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty) {
                                isReturningUser = true
                                existingBloodGroup = snapshot.documents[0].getString("bloodGroup") ?: ""
                            } else {
                                isReturningUser = false
                                existingBloodGroup = ""
                            }
                            isCheckingPhone = false
                        }
                        .addOnFailureListener {
                            isCheckingPhone = false
                        }
                }
            }

            LoginScreen(
                onPhoneChanged = { phone ->
                    checkPhoneIfNeeded(phone)
                },
                onLoginClick = { phone ->
                    // Agar check abhi tak nahi hua (jaldi click ho gaya), to pehle confirm karo
                    if (phone != lastCheckedPhone) {
                        isCheckingPhone = true
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .whereEqualTo("phone", phone)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                if (!snapshot.isEmpty) {
                                    isReturningUser = true
                                    existingBloodGroup = snapshot.documents[0].getString("bloodGroup") ?: ""
                                } else {
                                    isReturningUser = false
                                    existingBloodGroup = ""
                                }
                                isCheckingPhone = false
                                if (context is Activity) {
                                    authViewModel.sendOTP(phone, context)
                                }
                            }
                            .addOnFailureListener {
                                isCheckingPhone = false
                                if (context is Activity) {
                                    authViewModel.sendOTP(phone, context)
                                }
                            }
                    } else {
                        // Check pehle se ho chuka hai, seedha OTP bhej do
                        if (context is Activity) {
                            authViewModel.sendOTP(phone, context)
                        }
                    }
                },
                onBloodInfoSaved = { bloodGroup, age, consent ->
                    authViewModel.setBloodInfo(bloodGroup, age, consent)
                },
                isLoading = isLoading || isCheckingPhone,
                error = error,
                isReturningUser = isReturningUser,
                existingBloodGroup = existingBloodGroup
            )
        }

        composable(
            route = "${Screen.OTP.route}/{phoneNumber}",
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OTPScreen(
                phoneNumber = phoneNumber,
                onVerifyClick = { otp -> authViewModel.verifyOTP(otp) },
                onResendClick = {
                    if (context is Activity) authViewModel.resendOTP(phoneNumber, context)
                },
                isLoading = isLoading,
                error = error
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
                error = error
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    tempRole = role
                    when (role) {
                        "worker" -> navController.navigate(Screen.WorkerCategory.route)
                        "admin" -> navController.navigate(Screen.AdminPasscode.route)
                        else -> authViewModel.saveUserProfile(tempName, tempAddress, role)
                    }
                }
            )
        }

        composable(Screen.WorkerCategory.route) {
            WorkerCategoryScreen(
                onCategorySelected = { category, price, about, aadhar ->
                    authViewModel.saveUserProfile(
                        tempName, tempAddress, tempRole,
                        category, price, about, aadhar
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
                isLoading = isLoading
            )
        }

        // Seeker ka worker profile add flow
        composable(Screen.AddWorkerProfile.route) {
            WorkerCategoryScreen(
                onCategorySelected = { category, price, about, aadhar ->
                    profileViewModel.addWorkerProfile(category, price, about, aadhar)
                    navController.popBackStack()
                },
                isLoading = isLoading
            )
        }

        composable(
            route = "${Screen.Home.route}/{userName}/{userRole}",
            arguments = listOf(
                navArgument("userName") { type = NavType.StringType },
                navArgument("userRole") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val userRole = backStackEntry.arguments?.getString("userRole") ?: "seeker"
            HomeScreen(
                userRole = userRole,
                userName = userName,
                onLogout = {
                    authViewModel.logout()
                    isReturningUser = false
                    existingBloodGroup = ""
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onRoleSwitched = { newRole ->
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .update("activeRole", newRole)
                            .addOnSuccessListener {
                                navController.navigate("${Screen.Home.route}/$userName/$newRole") {
                                    popUpTo("${Screen.Home.route}/{userName}/{userRole}") { inclusive = true }
                                }
                            }
                            .addOnFailureListener {
                                navController.navigate("${Screen.Home.route}/$userName/$newRole") {
                                    popUpTo("${Screen.Home.route}/{userName}/{userRole}") { inclusive = true }
                                }
                            }
                    }
                }
            )
        }
    }
}