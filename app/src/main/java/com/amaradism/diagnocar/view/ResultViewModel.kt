package com.amaradism.diagnocar.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amaradism.diagnocar.data.ArticlesItem
import com.amaradism.diagnocar.data.NewsAPIorgResponse
import com.amaradism.diagnocar.data.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultViewModel : ViewModel() {
    private val _articlesData = MutableLiveData<List<ArticlesItem>>()
    val articlesData: LiveData<List<ArticlesItem>> get() = _articlesData

    private val _errorMessage: MutableLiveData<String?> = MutableLiveData()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        getArticles()
    }

    private fun getArticles() {
        _isLoading.value = true
        viewModelScope.launch {
            val client = ApiConfig.getApiService().getArticles()
            client.enqueue(object : Callback<NewsAPIorgResponse> {
                override fun onResponse(
                    call: Call<NewsAPIorgResponse>,
                    response: Response<NewsAPIorgResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val articleData =
                            response.body()?.articles?.filter { it.url != "https://removed.com" }
                                ?: emptyList()

                        if (articleData.isNotEmpty()) {
                            Log.d("MyViewModel", "Data received: $articleData")
                            _articlesData.value = articleData
                            _errorMessage.value = null
                        } else {
                            _errorMessage.value = "No data available."
                            Log.e("MyViewModel", "No data received")
                        }
                    } else {
                        _errorMessage.value = "Failed to receive data."
                        Log.e("MyViewModel", "onFailure: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<NewsAPIorgResponse>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "Anda sedang dalam mode offline."
                    Log.e("MyViewModel", "Network error: ${t.message}")
                }

            })
        }
    }
}