package com.example.ecommerce.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.data.Repository
import com.example.ecommerce.data.apimodel.ProductItem
import com.example.ecommerce.database.cart.Cart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    // Current amount of the product (quantity)
    private val _currentAmount = MutableLiveData(1)
    val currentAmount: LiveData<Int> = _currentAmount

    // Total price for the selected product based on quantity
    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> = _totalPrice

    // Selected item for which we're calculating price and quantity
    private val _selectedItem = MutableLiveData<ProductItem>()

    // Initialize the selected item with its price and calculate initial total price
    fun initSelectedItem(item: ProductItem) {
        _selectedItem.value = item
        _totalPrice.value = item.price.toDouble() // Initialize total price with the base price
    }

    // Update quantity and recalculate total price based on the new quantity
    fun setCurrentAmount(amount: Int) {
        _currentAmount.value = amount
        updateTotalPrice() // Recalculate the total price based on the new quantity
    }

    // Recalculate total price using the original price and the current quantity
    private fun updateTotalPrice() {
        val currentAmount = _currentAmount.value ?: 1
        val selectedItem = _selectedItem.value
        if (selectedItem != null) {
            // totalPrice is recalculated as price * currentAmount, but price itself remains unchanged
            val totalPrice = selectedItem.price.toDouble() * currentAmount
            _totalPrice.value = totalPrice
        }
    }

    // Function to insert a cart item into the database
    private suspend fun insertCartItem(cartItem: Cart) {
        try {
            repository.local.insertCart(cartItem)
            Log.d("DetailViewModel", "Item inserted into the database successfully.")
        } catch (e: Exception) {
            Log.e("DetailViewModel", "Failed to insert item: ${e.message}")
        }
    }

/*    // Add selected item to the cart
    fun addToCart() = viewModelScope.launch {
        val selectedItem = _selectedItem.value

        if (selectedItem == null) {
            Log.e("DetailViewModel", "Selected item is null!")
            return@launch
        }

        val cartItem = totalPrice.value?.let { totalPriceValue ->
            currentAmount.value?.let { quantityValue ->
                Cart(
                    id = selectedItem.id,  // Assuming ProductItem has an ID field
                    image = selectedItem.image,
                    title = selectedItem.title,
                    description = selectedItem.description,
                    category = selectedItem.category,
                    price = selectedItem.price.toDouble(), // Using original price, not totalPrice
                    quantity = quantityValue,
                    totalPrice = totalPriceValue.toInt()
                )
            }
        }

        if (cartItem != null) {
            try {
                insertCartItem(cartItem)
                Log.d("DetailViewModel", "Cart item successfully inserted into the database!")
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error inserting cart item: ${e.message}")
            }
        } else {
            Log.e("DetailViewModel", "Cart item creation failed!")
        }
    }*/
}


