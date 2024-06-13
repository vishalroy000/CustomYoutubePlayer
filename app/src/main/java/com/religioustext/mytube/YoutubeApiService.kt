package com.religioustext.mytube

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeApiService {
    @GET("oembed")
    fun getVideoDetails(
        @Query("url") url: String,
        @Query("format") format: String = "json"
    ): Call<YoutubeVideoResponse>
}
