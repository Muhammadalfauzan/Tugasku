package com.example.ecommerce.data

import com.example.ecommerce.data.api.ApiService
import com.example.ecommerce.data.apimodel.CategoriesResponse
import com.example.ecommerce.data.apimodel.Product
import retrofit2.Response
import javax.inject.Inject

class RemoteData @Inject constructor(
    private val apiService: ApiService,

    ) {
    // Fungsi ini mengakses endpoint API untuk mendapatkan kategori menu
    suspend fun getCategoryMenu(): Response<CategoriesResponse> {
        return apiService.getCategoryDI()
    }
    // Fungsi ini mengakses endpoint API untuk mendapatkan daftar produk
    suspend fun getListproduct(): Response<Product> {
        return apiService.getProduct()
    }
}