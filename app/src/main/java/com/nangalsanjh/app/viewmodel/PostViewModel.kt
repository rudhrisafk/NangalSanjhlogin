package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Post(
    val id: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val userRole: String = "",
    val category: String = "",
    val description: String = "",
    val price: String = "",
    val availability: String = "",
    val timeStamp: Long = 0L,
    val replies: Int = 0,
    val uid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val declinedBy: List<String> = emptyList(),
    val status: String = "open",
    val assignedWorkerUid: String = "",
    val assignedWorkerName: String = "",
    val rated: Boolean = false,
    val feedbackRating: Float = 0f,
    val feedbackComment: String = "",
    val beforePhotoUrl: String = "",
    val afterPhotoUrl: String = "",
    val isOpenToAll: Boolean = false
)

class PostViewModel : ViewModel() {

    companion object {
        const val MAX_WORKER_POSTS = 5
        const val JOB_REQUEST_EXPIRY_MS = 48L * 60 * 60 * 1000
        const val WORKER_POST_EXPIRY_MS = 24L * 60 * 60 * 1000
        const val JOB_ACCEPT_WINDOW_MS = 10L * 60 * 1000
    }

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _postSuccess = MutableStateFlow(false)
    val postSuccess: StateFlow<Boolean> = _postSuccess

    private val _limitReached = MutableStateFlow(false)
    val limitReached: StateFlow<Boolean> = _limitReached

    init { fetchPosts() }

    fun fetchPosts() {
        _isLoading.value = true
        db.collection("posts")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = e.localizedMessage
                    return@addSnapshotListener
                }
                val postList = snapshot?.documents?.mapNotNull { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val declined = doc.get("declinedBy") as? List<String> ?: emptyList()
                    Post(
                        id = doc.id,
                        userName = doc.getString("userName") ?: "",
                        userPhone = doc.getString("userPhone") ?: "",
                        userRole = doc.getString("userRole") ?: "",
                        category = doc.getString("category") ?: "",
                        description = doc.getString("description") ?: "",
                        price = doc.getString("price") ?: "",
                        availability = doc.getString("availability") ?: "",
                        timeStamp = doc.getLong("timeStamp") ?: 0L,
                        replies = (doc.getLong("replies") ?: 0L).toInt(),
                        uid = doc.getString("uid") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        address = doc.getString("address") ?: "",
                        declinedBy = declined,
                        status = doc.getString("status") ?: "open",
                        assignedWorkerUid = doc.getString("assignedWorkerUid") ?: "",
                        assignedWorkerName = doc.getString("assignedWorkerName") ?: "",
                        rated = doc.getBoolean("rated") ?: false,
                        feedbackRating = (doc.getDouble("feedbackRating") ?: 0.0).toFloat(),
                        feedbackComment = doc.getString("feedbackComment") ?: "",
                        beforePhotoUrl = doc.getString("beforePhotoUrl") ?: "",
                        afterPhotoUrl = doc.getString("afterPhotoUrl") ?: "",
                        isOpenToAll = doc.getBoolean("isOpenToAll") ?: false
                    )
                } ?: emptyList()
                _posts.value = postList
            }
    }

    fun createWorkerPost(
        userName: String,
        category: String,
        description: String,
        price: String,
        availability: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val phone = auth.currentUser?.phoneNumber ?: ""

        val activeCount = _posts.value.count { it.uid == uid && it.userRole == "worker" }
        if (activeCount >= MAX_WORKER_POSTS) {
            _limitReached.value = true
            return
        }

        _isLoading.value = true
        _error.value = null

        val post = hashMapOf(
            "userName" to userName,
            "userPhone" to phone,
            "userRole" to "worker",
            "category" to category,
            "description" to description,
            "price" to price,
            "availability" to availability,
            "timeStamp" to System.currentTimeMillis(),
            "replies" to 0,
            "uid" to uid,
            "latitude" to 0.0,
            "longitude" to 0.0,
            "address" to "",
            "declinedBy" to emptyList<String>(),
            "status" to "open",
            "assignedWorkerUid" to "",
            "assignedWorkerName" to "",
            "rated" to false,
            "feedbackRating" to 0.0,
            "feedbackComment" to "",
            "beforePhotoUrl" to "",
            "afterPhotoUrl" to "",
            "isOpenToAll" to false
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener {
                _isLoading.value = false
                _postSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage ?: "Failed to post"
            }
    }

    fun editWorkerPost(
        postId: String,
        category: String,
        description: String,
        price: String,
        availability: String
    ) {
        _isLoading.value = true
        db.collection("posts").document(postId)
            .update(
                mapOf(
                    "category" to category,
                    "description" to description,
                    "price" to price,
                    "availability" to availability
                )
            )
            .addOnSuccessListener {
                _isLoading.value = false
                _postSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage ?: "Failed to update"
            }
    }

    fun deletePost(postId: String) {
        db.collection("posts").document(postId).delete()
    }

    fun createJobRequest(
        userName: String,
        category: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String,
        isOpenToAll: Boolean = false
    ) {
        val uid = auth.currentUser?.uid ?: return
        val phone = auth.currentUser?.phoneNumber ?: ""

        _isLoading.value = true
        _error.value = null

        val post = hashMapOf(
            "userName" to userName,
            "userPhone" to phone,
            "userRole" to "seeker",
            "category" to category,
            "description" to description,
            "price" to "",
            "availability" to "",
            "timeStamp" to System.currentTimeMillis(),
            "replies" to 0,
            "uid" to uid,
            "latitude" to latitude,
            "longitude" to longitude,
            "address" to address,
            "declinedBy" to emptyList<String>(),
            "status" to "open",
            "assignedWorkerUid" to "",
            "assignedWorkerName" to "",
            "rated" to false,
            "feedbackRating" to 0.0,
            "feedbackComment" to "",
            "beforePhotoUrl" to "",
            "afterPhotoUrl" to "",
            "isOpenToAll" to isOpenToAll
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener {
                _isLoading.value = false
                _postSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage ?: "Failed to post"
            }
    }

    fun declinePost(postId: String, workerUid: String) {
        db.collection("posts").document(postId)
            .update("declinedBy", FieldValue.arrayUnion(workerUid))
    }

    fun acceptPost(postId: String, workerUid: String, workerName: String) {
        db.collection("posts").document(postId)
            .update(
                mapOf(
                    "status" to "assigned",
                    "assignedWorkerUid" to workerUid,
                    "assignedWorkerName" to workerName
                )
            )
    }

    fun completeJob(postId: String, beforePhotoUrl: String = "", afterPhotoUrl: String = "") {
        val updates = mutableMapOf<String, Any>("status" to "completed")
        if (beforePhotoUrl.isNotEmpty()) updates["beforePhotoUrl"] = beforePhotoUrl
        if (afterPhotoUrl.isNotEmpty()) updates["afterPhotoUrl"] = afterPhotoUrl
        db.collection("posts").document(postId).update(updates)
    }

    fun submitFeedback(postId: String, workerUid: String, rating: Float, comment: String) {
        val userRef = db.collection("users").document(workerUid)
        userRef.get().addOnSuccessListener { doc ->
            val oldRating = doc.getDouble("rating") ?: 0.0
            val oldCount = doc.getLong("reviewCount") ?: 0L
            val oldJobsDone = doc.getLong("jobsDone") ?: 0L
            val newCount = oldCount + 1
            val newRating = ((oldRating * oldCount) + rating) / newCount

            userRef.update(
                mapOf(
                    "rating" to newRating,
                    "reviewCount" to newCount,
                    "jobsDone" to oldJobsDone + 1
                )
            )

            db.collection("posts").document(postId).update(
                mapOf(
                    "rated" to true,
                    "feedbackRating" to rating,
                    "feedbackComment" to comment
                )
            )
        }
    }

    fun cleanupExpiredJobRequests(uid: String) {
        if (uid.isEmpty()) return
        val cutoff = System.currentTimeMillis() - JOB_REQUEST_EXPIRY_MS
        db.collection("posts")
            .whereEqualTo("uid", uid)
            .whereEqualTo("userRole", "seeker")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    val ts = doc.getLong("timeStamp") ?: 0L
                    if (ts < cutoff) doc.reference.delete()
                }
            }
    }

    fun cleanupExpiredWorkerPosts(uid: String) {
        if (uid.isEmpty()) return
        val cutoff = System.currentTimeMillis() - WORKER_POST_EXPIRY_MS
        db.collection("posts")
            .whereEqualTo("uid", uid)
            .whereEqualTo("userRole", "worker")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    val ts = doc.getLong("timeStamp") ?: 0L
                    if (ts < cutoff) doc.reference.delete()
                }
            }
    }

    fun cleanupExpiredAcceptWindow() {
        val cutoff = System.currentTimeMillis() - JOB_ACCEPT_WINDOW_MS
        db.collection("posts")
            .whereEqualTo("userRole", "seeker")
            .whereEqualTo("status", "open")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    val ts = doc.getLong("timeStamp") ?: 0L
                    if (ts < cutoff) doc.reference.update("status", "expired")
                }
            }
    }

    fun resetPostSuccess() {
        _postSuccess.value = false
    }

    fun resetLimitReached() {
        _limitReached.value = false
    }
}