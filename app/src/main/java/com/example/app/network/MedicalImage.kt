package com.example.app.network

import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class MedicalImage(
    val name: String,
    @Json(name = "download_url") val imgSrcUrl: String
    ):Parcelable
