package com.example.ecommerce.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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


    // Menggunakan MediatorLiveData untuk memantau jumlah total item di keranjang
    private val _cartItemCount = MediatorLiveData<Int>()
    val cartItemCount: LiveData<Int> = _cartItemCount

    // LiveData untuk total harga di keranjang
    val totalPrice: LiveData<Double> = calculateTotalPrice()

    // Inisialisasi view model untuk perubahan badge di cartfragment
    init {
        _cartItemCount.addSource(allCartItems) { cartItems ->
            val itemCount = cartItems.sumOf { it.quantity }
            Log.d("CartViewModel", "Total item count calculated: $itemCount")
            _cartItemCount.value = itemCount
        }
    }

    fun addToCart(cartItem: Cart) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.addOrUpdateCartItem(cartItem)
            Log.d("CartViewModel", "Item added to cart: ${cartItem.title}")
            updateCartItemCount() // Perbarui jumlah item di keranjang setelah perubahan data
        }
    }

    fun deleteCartItemById(cartId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteById(cartId)
            Log.d("CartViewModel", "Item deleted from cart: $cartId")
            updateCartItemCount()
        }
    }

    fun updateCart(cart: Cart) {
        viewModelScope.launch(Dispatchers.IO) {
            if (cart.price != null) {
                cart.totalPrice = cart.price * cart.quantity
            } else {
                // Tampilkan pesan error atau lakukan sesuatu jika price null
                Log.e("CartViewModel", "Price is null for item: ${cart.title}")
            }
            repository.local.updateCart(cart)
            Log.d("CartViewModel", "Item updated in cart: ${cart.title}")
            updateCartItemCount()
        }
    }

    private fun calculateTotalPrice(): LiveData<Double> {
        return allCartItems.map { cartItems ->
            cartItems.sumOf { (it.price ?: 0.0) * it.quantity }
        }
    }


    private fun updateCartItemCount() {
        viewModelScope.launch(Dispatchers.Main) {
            allCartItems.value?.let { cartItems ->
                val itemCount = cartItems.sumOf { it.quantity }
                Log.d("CartViewModel", "Total item count updated: $itemCount")
                _cartItemCount.value = itemCount
            }
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
