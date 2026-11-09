// AdminNotificationViewModel.kt — NEW FILE

package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AdminNotification(
    val id: String = "",
    val type: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val timeStamp: Long = 0L
)

class AdminNotificationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _notifications = MutableStateFlow<List<AdminNotification>>(emptyList())
    val notifications: StateFlow<List<AdminNotification>> = _notifications

    init {
        db.collection("admin_notifications")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                _notifications.value = snapshot?.documents?.mapNotNull { doc ->
                    AdminNotification(
                        id = doc.id,
                        type = doc.getString("type") ?: "",
                        message = doc.getString("message") ?: "",
                        isRead = doc.getBoolean("isRead") ?: false,
                        timeStamp = doc.getLong("timeStamp") ?: 0L
                    )
                } ?: emptyList()
            }
    }

    fun markRead(id: String) {
        db.collection("admin_notifications").document(id).update("isRead", true)
    }
}