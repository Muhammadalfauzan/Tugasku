package com.example.ecommerce.data.apimodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rating(
    val count: Int?,
    val rate: Double?
) : Parcelable