package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserPoints(
    val points: Int = 0,
    val level: String = "Nagarik",
    val nextLevelPoints: Int = 100
)

class PointsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userPoints = MutableStateFlow(UserPoints())
    val userPoints: StateFlow<UserPoints> = _userPoints

    init { fetchPoints() }

    fun fetchPoints() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                val points = (doc?.getLong("points") ?: 0L).toInt()
                _userPoints.value = UserPoints(
                    points = points,
                    level = getLevel(points),
                    nextLevelPoints = getNextLevelPoints(points)
                )
            }
    }

    fun addPointsToUser(targetUid: String, pointsToAdd: Int, reason: String) {
        db.collection("users").document(targetUid)
            .get()
            .addOnSuccessListener { doc ->
                val currentPoints = (doc.getLong("points") ?: 0L).toInt()
                val newPoints = currentPoints + pointsToAdd

                db.collection("users").document(targetUid)
                    .update("points", newPoints)

                // History
                db.collection("points_history").add(
                    hashMapOf(
                        "uid" to targetUid,
                        "points" to pointsToAdd,
                        "reason" to reason,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
    }

    fun addPoints(reason: String) {
        val uid = auth.currentUser?.uid ?: return
        val pointsToAdd = when (reason) {
            "complaint" -> 10
            "booking" -> 15
            "resolved" -> 5
            else -> 5
        }

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val currentPoints = (doc.getLong("points") ?: 0L).toInt()
                val newPoints = currentPoints + pointsToAdd

                db.collection("users").document(uid)
                    .update("points", newPoints)

                // Save points history
                db.collection("points_history").add(
                    hashMapOf(
                        "uid" to uid,
                        "points" to pointsToAdd,
                        "reason" to reason,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }
    }

    private fun getLevel(points: Int): String = when {
        points >= 500 -> "🏆 Seva Samrat"
        points >= 300 -> "⭐ Seva Ratna"
        points >= 200 -> "🥈 Seva Mitra"
        points >= 100 -> "🥉 Seva Sevak"
        else -> "🌱 Nagarik"
    }

    private fun getNextLevelPoints(points: Int): Int = when {
        points >= 500 -> 500
        points >= 300 -> 500
        points >= 200 -> 300
        points >= 100 -> 200
        else -> 100
    }
}