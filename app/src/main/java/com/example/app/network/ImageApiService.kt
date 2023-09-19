package com.example.app.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val URL = "https://api.github.com/repos/PiotrZawalski/KotlinMedicalApp/contents/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(URL)
    .build()

interface ImageApiService {
    @GET("images")
    suspend fun getPhotos(): List<MedicalImage>
}

object ImageApi {
    val retrofitService: ImageApiService by lazy { retrofit.create(ImageApiService::class.java) }
}