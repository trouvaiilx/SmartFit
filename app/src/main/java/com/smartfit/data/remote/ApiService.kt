// FILE: app/src/main/java/com/smartfit/data/remote/ApiService.kt

package com.smartfit.data.remote

import com.smartfit.data.remote.dto.SuggestionDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Retrofit API service for fetching workout suggestions from ExerciseDB API.
 * Uses the correct endpoint structure and response format.
 */
interface ApiService {
    @GET("exercises")
    suspend fun getExercises(
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): List<SuggestionDto>

    companion object {
        private const val BASE_URL = "https://exercisedb.p.rapidapi.com/"

        fun create(apiKey: String = ""): ApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val originalRequest = chain.request()

                    val request = if (apiKey.isNotEmpty()) {
                        originalRequest.newBuilder()
                            .addHeader("x-rapidapi-key", apiKey)
                            .addHeader("x-rapidapi-host", "exercisedb.p.rapidapi.com")
                            .build()
                    } else {
                        originalRequest
                    }

                    chain.proceed(request)
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}