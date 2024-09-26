package com.example.ecommerce.data


import androidx.lifecycle.LiveData
import com.example.ecommerce.database.cart.Cart
import com.example.ecommerce.database.cart.CartDao
import com.example.ecommerce.database.product.ProductDao
import com.example.ecommerce.database.product.ProductItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val productDao: ProductDao,
    private val cartDao: CartDao
) {
    /** MENU **/
    fun readProduct(): Flow<List<ProductItems>> {
        return productDao.readProduct()
    }

    suspend fun insertProduct(product:ProductItems ){
        productDao.insertProduct(product)
    }


    fun searchProductsByLabel(label: String): List<ProductItems> {
        return productDao.searchProductsByLabel(label)
    }
    /** CART **/

    suspend fun insertCart(cartItem: Cart) {
        cartDao.insert(cartItem)
    }

    fun getAllCartItems(): LiveData<List<Cart>> {
        return cartDao.getAllCartItems()
    }

    suspend fun deleteById(cartId: Long) {
        return cartDao.deleteById(cartId)
    }

    suspend fun deleteAllItems() {
        return cartDao.deleteAllItems()
    }

    suspend fun updateCart(cartItem: Cart) {
        return cartDao.update(cartItem)
    }


    suspend fun getCartItemById(cartId: Int): Cart? {
        return cartDao.getCartItemById(cartId)
    }



    suspend fun addOrUpdateCartItem(cartItem: Cart) {
        val existingCartItem = getCartItemById(cartItem.id)

        if (existingCartItem != null) {
            // Update quantity dan total price jika item sudah ada di keranjang
            val updatedCartItem = existingCartItem.copy(
                quantity = existingCartItem.quantity + cartItem.quantity,
                totalPrice = (existingCartItem.price ?: 0.0) * (existingCartItem.quantity + cartItem.quantity)  // Gunakan Double
            )
            updateCart(updatedCartItem)
        } else {
            // Jika item baru, masukkan ke keranjang dengan perhitungan total price
            val newCartItem = cartItem.copy(
                totalPrice = (cartItem.price ?: 0.0) * cartItem.quantity  // Hitung total price saat insert
            )
            insertCart(newCartItem)
        }
    }
}