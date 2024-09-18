package com.example.ecommerce.api

import com.example.ecommerce.data.CategoriesResponse
import com.example.ecommerce.data.Product
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("products/categories")
    suspend fun getCategoryDI(): Response<List<String>>

    @GET("products")  // Ubah endpoint jika diperlukan
    suspend fun getProduct(): Response<Product>
}