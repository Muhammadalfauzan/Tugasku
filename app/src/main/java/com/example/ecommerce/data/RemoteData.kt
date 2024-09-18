package com.example.ecommerce.data

import com.example.ecommerce.api.ApiService
import retrofit2.Response
import javax.inject.Inject

class RemoteData @Inject constructor(
    private val apiService: ApiService,

    ) {

    suspend fun getCategoryMenu(): Response<CategoriesResponse> {
        return apiService.getCategoryDI()
    }
    suspend fun getListproduct(): Response<Product> {
        return apiService.getProduct()
    }
}