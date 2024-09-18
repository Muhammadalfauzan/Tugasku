package com.example.ecommerce.database

import androidx.room.TypeConverter
import com.example.ecommerce.data.apimodel.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProductTypeConverter{

    @TypeConverter
    fun fromProduct(product: Product?): String? {
        return Gson().toJson(product)
    }

    @TypeConverter
    fun toProduct(productString: String?): Product? {
        return Gson().fromJson(productString, object : TypeToken<Product>() {}.type)
    }
}
