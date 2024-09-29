package com.example.ecommerce.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.data.apimodel.Product
import com.example.ecommerce.data.Repository
import com.example.ecommerce.data.apimodel.ProductItem
import com.example.ecommerce.data.apimodel.Rating
import com.example.ecommerce.database.product.ProductItems
import com.example.ecommerce.utils.NetworkResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
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

    /** Database Room **/
    val readProduct: LiveData<List<ProductItems>> = repository.local.readProduct().asLiveData()

    /** ML Kit Search Labeling **/
    var searchResultResponse: MutableLiveData<List<ProductItem>> = MutableLiveData()


    /** Variabel Penanda untuk Mengecek Status Data **/
    private var hasFetchedCategories = false
    private var hasFetchedProducts = false

    /** Mengambil Kategori dari API **/
    fun getCategory() = viewModelScope.launch {
        // Cek apakah sudah fetch data kategori sebelumnya
        if (!hasFetchedCategories) {
            getCategorySafeCall()
            hasFetchedCategories = true
        }
    }

    /** Mengambil Daftar Produk dari API **/
    fun getListMenu() = viewModelScope.launch {
        // Cek apakah sudah fetch data produk sebelumnya
        if (!hasFetchedProducts) {
            getListMenuSafeCall()
            hasFetchedProducts = true
        }
    }

   /* *//** Mengambil Kategori dari API **//*
    fun getCategory() = viewModelScope.launch {
        getCategorySafeCall()
    }*/

    /** Menganalisis Gambar dengan ML Kit **/
    fun analyzeImageWithMLKit(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        labeler.process(image)
            .addOnSuccessListener { labels ->

                val labelList = labels.map { it.text }
                Log.d("ImageLabeling", "Labels received: $labelList")
                searchProductsByLabels(labelList)


            }
            .addOnFailureListener {
                Log.e("ImageLabeling", "Failed to label image")
            }
    }

    fun searchProductsByLabels(labels: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val filteredProducts = mutableListOf<ProductItem>()
            labels.forEach { label ->
                Log.d("SearchLabel", "Searching products for label: $label")

                val products = repository.local.searchProductsByLabel(label)
                Log.d("SearchLabel", "Found products for label '$label': ${products.map { it.title }}")

                filteredProducts.addAll(products.map { convertToProductItem(it) })
            }

            Log.d("SearchResult", "Total filtered products: ${filteredProducts.size}")
            searchResultResponse.postValue(filteredProducts)
        }
    }


    /** Konversi Produk dari API ke Room Model **/
    private fun convertToProductItem(productItems: ProductItems): ProductItem {
        val rating = Rating(
            count = productItems.ratingCount,
            rate = productItems.ratingRate
        )

        return ProductItem(
            id = productItems.id,
            title = productItems.title,
            description = productItems.description,
            category = productItems.category,
            image = productItems.image,
            price = productItems.price,
            rating = rating
        )
    }
    fun getConvertedProductItems(): LiveData<List<ProductItem>> {
        return readProduct.map { productItemsList ->
            productItemsList.map { productItems ->
                convertToProductItem(productItems)
            }
        }
    }
    /** Mendapatkan Kategori secara Aman **/
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

    /** Memeriksa Koneksi Internet **/
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    //============================================== MENU ==============================================//
   /* fun getListMenu() = viewModelScope.launch {
        getListMenuSafeCall()
    }
*/
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
        return when {
            response.message().toString().contains("timeout") -> {
                NetworkResult.Error("Timeout")
            }

            response.code() == 402 -> {
                NetworkResult.Error("API Key Limited")
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


    private fun insertProduct(product: ProductItems) =
        viewModelScope.launch(Dispatchers.IO) { repository.local.insertProduct(product) }

    /** Cache Produk Secara Offline **/
    private fun offlineCacheMenu(listMenu: Product) {
        listMenu.forEach {
            val menu = ProductItems(
                id = it.id,
                title = it.title,
                description = it.description,
                category = it.category,
                image = it.image,
                price = it.price,
                ratingRate = it.rating.rate,
                ratingCount = it.rating.count
            )
            insertProduct(menu)
        }
    }
}