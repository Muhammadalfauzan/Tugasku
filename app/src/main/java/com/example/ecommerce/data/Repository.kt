package com.example.ecommerce.data

import com.example.ecommerce.database.product.ProductItems
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class Repository @Inject constructor(
    remoteDataSource: RemoteData,
    localDataSource: LocalDataSource

) {
    val remote = remoteDataSource
    val local = localDataSource

    fun searchProductsByLabel(label: String): List<ProductItems> {
        return local.searchProductsByLabel(label)
    }
}