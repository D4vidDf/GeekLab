package com.daviddf.geeklab.ui.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddf.geeklab.Experiments
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NewsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _news = MutableStateFlow<List<Experiments>>(emptyList())
    val news: StateFlow<List<Experiments>> = _news.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var lastVisible: DocumentSnapshot? = null
    private var isLastPage = false
    private val PAGE_SIZE = 10L

    init {
        loadMoreNews()
    }

    fun loadMoreNews() {
        if (_isLoading.value || isLastPage) return
        fetchNews()
    }

    fun refreshNews() {
        viewModelScope.launch {
            _isLoading.value = true
            val startTime = System.currentTimeMillis()
            
            lastVisible = null
            isLastPage = false
            _news.value = emptyList()
            
            fetchNewsInternal()
            
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingTime = 2000L - elapsedTime
            if (remainingTime > 0) {
                delay(remainingTime)
            }
            _isLoading.value = false
        }
    }

    private fun fetchNews() {
        viewModelScope.launch {
            _isLoading.value = true
            fetchNewsInternal()
            _isLoading.value = false
        }
    }

    private suspend fun fetchNewsInternal() {
        _error.value = null
        try {
            var query = db.collection("experiments")
                .orderBy("Fecha", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE)

            lastVisible?.let {
                query = query.startAfter(it)
            }

            val snapshot = query.get().await()
            val newsList = snapshot.toObjects(Experiments::class.java)

            if (newsList.isNotEmpty()) {
                _news.value = if (lastVisible == null) newsList else _news.value + newsList
                lastVisible = snapshot.documents[snapshot.size() - 1]
            }

            if (newsList.size < PAGE_SIZE) {
                isLastPage = true
            }
            Log.d("NewsViewModel", "Loaded ${newsList.size} items. Total: ${_news.value.size}")
        } catch (e: Exception) {
            Log.e("NewsViewModel", "Error fetching news", e)
            _error.value = e.message ?: "Unknown error"
        }
    }
}
