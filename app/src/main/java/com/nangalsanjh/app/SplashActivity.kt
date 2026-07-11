package com.nangalsanjh.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout

class SplashActivity : AppCompatActivity() {

    private var isNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val motionLayout = findViewById<MotionLayout>(R.id.motionLayout)

        // Ensure transition starts if autoTransition doesn't trigger
        motionLayout.post {
            motionLayout.transitionToEnd()
        }

        motionLayout.setTransitionListener(
            object : MotionLayout.TransitionListener {
                override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {}
                override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
                override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {}

                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    if (currentId == R.id.end) {
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                navigateToMain()
                            },
                            500,
                        )
                    }
                }
            },
        )

        // Safety fallback: Navigate to MainActivity after 4 seconds regardless of animation state
        Handler(Looper.getMainLooper()).postDelayed(
            {
                navigateToMain()
            },
            4000,
        )
    }

    private fun navigateToMain() {
        if (isNavigated || isFinishing || isDestroyed) return
        isNavigated = true

        startActivity(Intent(this, MainActivity::class.java))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                android.R.anim.fade_in,
                android.R.anim.fade_out,
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finish()
    }
}