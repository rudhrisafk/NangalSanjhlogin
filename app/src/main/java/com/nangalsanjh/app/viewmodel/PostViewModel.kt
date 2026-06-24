package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Post(
    val id: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val userRole: String = "",
    val category: String = "",
    val description: String = "",
    val timeStamp: Long = 0L,
    val replies: Int = 0
)

class PostViewModel : ViewModel() {

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

    init {
        fetchPosts()
    }

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
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                _posts.value = postList
            }
    }

    fun createPost(
        userName: String,
        category: String,
        description: String
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
            "timeStamp" to System.currentTimeMillis(),
            "replies" to 0,
            "uid" to uid
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

    fun resetPostSuccess() {
        _postSuccess.value = false
    }
}