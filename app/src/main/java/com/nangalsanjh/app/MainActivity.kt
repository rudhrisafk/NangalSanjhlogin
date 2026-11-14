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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.messaging.FirebaseMessaging
import com.nangalsanjh.app.navigation.NangalSevaNavHost
import com.nangalsanjh.app.ui.screens.LocalAppLanguage
import com.nangalsanjh.app.viewmodel.AppLanguage
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
            val language by themeViewModel.language.collectAsState()
            val textScale by themeViewModel.textScale.collectAsState()
            remember {
                themeViewModel.setTextScale(
                    getSharedPreferences("nangal_prefs", MODE_PRIVATE).getFloat("text_scale", 1.0f)
                )
                true
            }
            LaunchedEffect(textScale) {
                getSharedPreferences("nangal_prefs", MODE_PRIVATE)
                    .edit().putFloat("text_scale", textScale).apply()
            }

            // Load saved language once, before first frame
            remember {
                val saved = getSharedPreferences("nangal_prefs", MODE_PRIVATE)
                    .getString("app_language", AppLanguage.ENGLISH.name)
                runCatching { AppLanguage.valueOf(saved ?: "") }
                    .getOrNull()?.let { themeViewModel.setLanguage(it) }
                true
            }
            // Save whenever it changes
            LaunchedEffect(language) {
                getSharedPreferences("nangal_prefs", MODE_PRIVATE)
                    .edit().putString("app_language", language.name).apply()
            }

            NangalSevaTheme(isDark = theme == AppTheme.DARK) {
                val baseDensity = androidx.compose.ui.platform.LocalDensity.current
                CompositionLocalProvider(
                    LocalAppLanguage provides language,
                    androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(
                        baseDensity.density, baseDensity.fontScale * textScale
                    )
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        NangalSevaNavHost(themeViewModel = themeViewModel)
                    }
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