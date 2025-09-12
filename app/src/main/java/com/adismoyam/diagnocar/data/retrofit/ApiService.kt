package com.adismoyam.diagnocar.data.retrofit

import com.adismoyam.diagnocar.BuildConfig
import com.adismoyam.diagnocar.data.NewsAPIorgResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("everything")
    fun getArticles(
        @Query("q") query: String = "automotive",
        @Query("language") language: String = "en",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 25,
        @Query("apiKey") apiKey: String = BuildConfig.NEWSAPI_KEY
    ): Call<NewsAPIorgResponse>
}
