// WorkerViewModel.kt — FULL FILE (adds isBlocked filter)

package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class WorkerModel(
    val uid: String = "",
    val name: String = "",
    val category: String = "",
    val phone: String = "",
    val address: String = "",
    val isAvailable: Boolean = true,
    val rating: Float = 0f,
    val jobsDone: Int = 0,
    val price: String = "",
    val about: String = "",
    val aadhar: String = "",
    val reviewCount: Int = 0,
    val experience: String = "",
    val isBlocked: Boolean = false
)

class WorkerViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _workers = MutableStateFlow<List<WorkerModel>>(emptyList())
    val workers: StateFlow<List<WorkerModel>> = _workers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { fetchWorkers() }

    fun fetchWorkers() {
        _isLoading.value = true
        db.collection("users")
            .whereEqualTo("role", "worker")
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = e.localizedMessage
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    WorkerModel(
                        uid = doc.getString("uid") ?: "",
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        phone = doc.getString("phone") ?: "",
                        address = doc.getString("address") ?: "",
                        isAvailable = doc.getBoolean("isAvailable") ?: false,
                        rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                        jobsDone = (doc.getLong("jobsDone") ?: 0L).toInt(),
                        price = doc.getString("price") ?: "",
                        about = doc.getString("about") ?: "",
                        aadhar = doc.getString("aadhar") ?: "",
                        reviewCount = (doc.getLong("reviewCount") ?: 0L).toInt(),
                        experience = doc.getString("experience") ?: "",
                        isBlocked = doc.getBoolean("isBlocked") ?: false
                    )
                }?.filter { !it.isBlocked } ?: emptyList()
                _workers.value = list
            }
    }

    fun toggleAvailability(isAvailable: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("isAvailable", isAvailable)
    }
}