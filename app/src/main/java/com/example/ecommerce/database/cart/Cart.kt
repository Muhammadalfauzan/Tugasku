package com.example.ecommerce.database.cart

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "cart_items")
data class Cart(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val category: String?,
    val description: String?,
    val image: String?,
    val price: Double?,
    val title: String?,
    var quantity: Int,
    var totalPrice : Double
) : Parcelable
