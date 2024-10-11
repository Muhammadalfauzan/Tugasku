package com.example.ecommerce.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.ecommerce.data.Repository
import com.example.ecommerce.data.apimodel.ProductItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    // Current amount of the product (quantity)
    private val _currentAmount = MutableLiveData(1)

    // Total price for the selected product based on quantity
    private val _totalPrice = MutableLiveData<Double>()

    // Selected item for which we're calculating price and quantity
    private val _selectedItem = MutableLiveData<ProductItem>()

    // Initialize the selected item with its price and calculate initial total price
    fun initSelectedItem(item: ProductItem) {
        _selectedItem.value = item
        _totalPrice.value = item.price // Initialize total price with the base price
    }
    fun clearTotalPrice(item: Double) {
        _totalPrice.value = item
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
            val totalPrice = selectedItem.price * currentAmount
            _totalPrice.value = totalPrice
        }
    }

    // Function to insert a cart item into the database
 /*   private suspend fun insertCartItem(cartItem: Cart) {
        try {
            repository.local.insertCart(cartItem)
            Log.d("DetailViewModel", "Item inserted into the database successfully.")
        } catch (e: Exception) {
            Log.e("DetailViewModel", "Failed to insert item: ${e.message}")
        }
    }*/
}


