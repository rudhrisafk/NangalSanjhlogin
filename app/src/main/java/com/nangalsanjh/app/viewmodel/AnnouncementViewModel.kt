// AnnouncementViewModel.kt — FULL FILE (removed bannerHeight/cellSpan, images stay as uploaded)

package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Announcement(
    val id: String = "",
    val type: String = "news",
    val title: String = "",
    val description: String = "",
    val contact: String = "",
    val bloodType: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val timeStamp: Long = 0L
)

class AnnouncementViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { fetchAnnouncements() }

    fun fetchAnnouncements() {
        _isLoading.value = true
        db.collection("announcements")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) return@addSnapshotListener
                _announcements.value = snapshot?.documents?.mapNotNull { doc ->
                    Announcement(
                        id = doc.id,
                        type = doc.getString("type") ?: "news",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        contact = doc.getString("contact") ?: "",
                        bloodType = doc.getString("bloodType") ?: "",
                        location = doc.getString("location") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        timeStamp = doc.getLong("timeStamp") ?: 0L
                    )
                } ?: emptyList()
            }
    }

    fun createAnnouncement(
        type: String,
        title: String,
        description: String,
        contact: String = "",
        bloodType: String = "",
        location: String = "",
        imageUrl: String = ""
    ) {
        db.collection("announcements").add(
            hashMapOf(
                "type" to type,
                "title" to title,
                "description" to description,
                "contact" to contact,
                "bloodType" to bloodType,
                "location" to location,
                "imageUrl" to imageUrl,
                "isActive" to true,
                "timeStamp" to System.currentTimeMillis()
            )
        )
    }

    fun updateAnnouncement(
        id: String,
        type: String,
        title: String,
        description: String,
        contact: String,
        bloodType: String,
        location: String,
        imageUrl: String
    ) {
        db.collection("announcements").document(id).update(
            mapOf(
                "type" to type,
                "title" to title,
                "description" to description,
                "contact" to contact,
                "bloodType" to bloodType,
                "location" to location,
                "imageUrl" to imageUrl
            )
        )
    }

    fun toggleActive(id: String, isActive: Boolean) {
        db.collection("announcements").document(id).update("isActive", isActive)
    }

    fun deleteAnnouncement(id: String) {
        db.collection("announcements").document(id).delete()
    }
}