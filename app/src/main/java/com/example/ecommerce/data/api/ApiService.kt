package com.example.ecommerce.data.api

import com.example.ecommerce.data.apimodel.Product
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("products/categories")
    suspend fun getCategoryDI(): Response<List<String>>

    @GET("products")  // Ubah endpoint jika diperlukan
    suspend fun getProduct(): Response<Product>
}