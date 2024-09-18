package com.example.ecommerce.data

import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class Repository @Inject constructor(
    remoteDataSource: RemoteData,
    localDataSource: LocalDataSource

) {
    val remote = remoteDataSource
    val local = localDataSource

}