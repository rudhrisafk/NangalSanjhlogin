// FavoriteViewModel.kt — NEW FILE

package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class FavoriteWorker(
    val workerUid: String = "",
    val workerName: String = "",
    val category: String = "",
    val phone: String = ""
)

class FavoriteViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _favorites = MutableStateFlow<List<FavoriteWorker>>(emptyList())
    val favorites: StateFlow<List<FavoriteWorker>> = _favorites

    init { fetchFavorites() }

    fun fetchFavorites() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("favorites")
            .addSnapshotListener { snapshot, _ ->
                _favorites.value = snapshot?.documents?.mapNotNull { doc ->
                    FavoriteWorker(
                        workerUid = doc.id,
                        workerName = doc.getString("workerName") ?: "",
                        category = doc.getString("category") ?: "",
                        phone = doc.getString("phone") ?: ""
                    )
                } ?: emptyList()
            }
    }

    fun addFavorite(workerUid: String, workerName: String, category: String, phone: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("favorites").document(workerUid)
            .set(hashMapOf("workerName" to workerName, "category" to category, "phone" to phone))
    }

    fun removeFavorite(workerUid: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("favorites").document(workerUid).delete()
    }
}