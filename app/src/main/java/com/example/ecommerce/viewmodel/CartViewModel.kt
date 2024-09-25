package com.example.ecommerce.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.data.Repository
import com.example.ecommerce.database.cart.Cart
import com.example.ecommerce.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    // LiveData untuk semua item di keranjang
    val allCartItems: LiveData<List<Cart>> = repository.local.getAllCartItems()

    private val _cartItemLiveData = MutableLiveData<Cart>()

    // LiveData untuk total harga, sekarang dengan tipe Double
    var totalPrice: LiveData<Double> = calculateTotalPrice()

    private val _orderPlacedLiveData = MutableLiveData<NetworkResult<Boolean>>()
    val orderPlacedLiveData: LiveData<NetworkResult<Boolean>> = _orderPlacedLiveData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Fungsi untuk menghapus item dari keranjang berdasarkan ID
    fun deleteCartItemById(cartId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteById(cartId)
        }
    }

    // Fungsi untuk menambah atau memperbarui item di keranjang
    fun addToCart(cartItem: Cart) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.addOrUpdateCartItem(cartItem)
        }
    }

    // Fungsi untuk menghapus semua item di keranjang
    private fun deleteAllItems() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteAllItems()
        }
    }

    // Fungsi untuk memperbarui item di keranjang
    fun updateCart(cart: Cart) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.updateCart(cart)
            _cartItemLiveData.postValue(cart)
        }
    }

    // Fungsi untuk menghitung total harga, sekarang tipe data adalah Double
    private fun calculateTotalPrice(): LiveData<Double> {
        return repository.local.getAllCartItems().map { cartItems ->
            var total = 0.0
            for (cartItem in cartItems) {
                val itemTotalPrice = (cartItem.price ?: 0.0) * cartItem.quantity
                Log.d("CartViewModel", "Item: ${cartItem.title}, Quantity: ${cartItem.quantity}, Price: ${cartItem.price}, Item Total: $itemTotalPrice")
                total += itemTotalPrice
            }
            Log.d("CartViewModel", "Total price calculated: $total")
            total
        }
    }


    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

}