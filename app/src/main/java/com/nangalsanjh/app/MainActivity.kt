package com.nangalsanjh.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.messaging.FirebaseMessaging
import com.nangalsanjh.app.navigation.NangalSevaNavHost
import com.nangalsanjh.app.viewmodel.AppTheme
import com.nangalsanjh.app.theme.NangalSevaTheme
import com.nangalsanjh.app.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ CRITICAL — enables edge-to-edge + proper inset handling
        enableEdgeToEdge()

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        askNotificationPermission()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) saveTokenToFirestore(task.result)
        }

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val theme by themeViewModel.theme.collectAsState()

            NangalSevaTheme(isDark = theme == AppTheme.DARK) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NangalSevaNavHost(themeViewModel = themeViewModel)
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = com.google.firebase.auth.FirebaseAuth
            .getInstance().currentUser?.uid ?: return
        com.google.firebase.firestore.FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }
}