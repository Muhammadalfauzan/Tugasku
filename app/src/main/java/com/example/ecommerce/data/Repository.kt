package com.example.ecommerce.data

import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class Repository @Inject constructor(
    remoteDataSource: RemoteData,  // DI untuk sumber data remote (API)
    localDataSource: LocalDataSource // DI untuk sumber data lokal (database)

) {
    // variabel untuk mengakses sumber data
    val remote = remoteDataSource
    val local = localDataSource


}