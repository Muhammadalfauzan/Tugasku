package com.example.ecommerce.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.data.apimodel.Product
import com.example.ecommerce.data.Repository
import com.example.ecommerce.database.product.ProductItems
import com.example.ecommerce.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    /** RETROFIT **/
    var categoryResponse: MutableLiveData<NetworkResult<List<String>>> = MutableLiveData()
    var listMenuResponse: MutableLiveData<NetworkResult<Product>> = MutableLiveData()

    fun getCategory() = viewModelScope.launch {
        getCategorySafeCall()
    }

    private suspend fun getCategorySafeCall() {
        categoryResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.getCategoryMenu()
                categoryResponse.value = handleCategoryResponse(response)
            } catch (e: Exception) {
                categoryResponse.value = NetworkResult.Error("Error: ${e.message}")
            }
        } else {
            categoryResponse.value = NetworkResult.Error("No Internet Connection")
        }
    }

    private fun handleCategoryResponse(response: Response<List<String>>): NetworkResult<List<String>> {
        return when {
            response.message().toString().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                NetworkResult.Error("API Key Limited")
            }
            response.isSuccessful -> {
                val categories = response.body()
                if (categories != null) {
                    NetworkResult.Success(categories)
                } else {
                    NetworkResult.Error("Empty Response")
                }
            }
            else -> {
                NetworkResult.Error(response.message())
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    //==============================================MENU==============================================//
    fun getListMenu() = viewModelScope.launch {
        getListMenuSafeCall()
    }

    private suspend fun getListMenuSafeCall() {
        listMenuResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.getListproduct()
                listMenuResponse.value = handleListMenuResponse(response)

                val listMenu = listMenuResponse.value!!.data
                if (listMenu != null) {
                    offlineCacheMenu(listMenu)
                }
            } catch (e: Exception) {
                listMenuResponse.value = NetworkResult.Error("Error: $e")
            }
        } else {
            listMenuResponse.value = NetworkResult.Error("No Internet Connection")
        }
    }



    private fun handleListMenuResponse(response: Response<Product>): NetworkResult<Product> {
        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }

            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited")
            }

            response.isSuccessful -> {
                val listMenu = response.body()
                return NetworkResult.Success(listMenu!!)
            }

            else -> {
                return NetworkResult.Error(response.message())
            }
        }
    }

    /** Room Database**/
    val readProduct : LiveData<List<ProductItems>> = repository.local.readProduct().asLiveData()

    private fun insertProduct(product : ProductItems) =
        viewModelScope.launch(Dispatchers.IO) { repository.local.insertProduct(product) }


       private fun offlineCacheMenu(listMenu: Product) {
       val menu = ProductItems(listMenu)
       insertProduct(menu)
   }
}

