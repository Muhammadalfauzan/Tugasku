package com.example.ecommerce.data


import android.util.Log
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
    /** PRODUCT **/
    //Mengembalikan daftar produk (List<ProductItems>) dalam bentuk Flow yang dapat diobservasi secara reaktif.
    fun readProduct(): Flow<List<ProductItems>> {
        return productDao.readProduct()
    }
    // Menambahkan produk
    suspend fun insertProduct(product:ProductItems ){
        productDao.insertProduct(product)
    }

    // Mencari produk berdasarkan label yang diberikan dan mengembalikan daftar hasil pencarian produk
    fun searchProductsByLabel(label: String): List<ProductItems> {
        return productDao.searchProductsByLabel(label)
    }
    /** CART **/
    // Menamahkan item baru ke keranjang
    suspend fun insertCart(cartItem: Cart) {
        cartDao.insert(cartItem)
    }
    // Mengembalikan semua item keranjang dalam bentuk LiveData
    fun getAllCartItems(): LiveData<List<Cart>> {
        return cartDao.getAllCartItems()
    }

    //Menghapus item keranjang beradasarkan id
    suspend fun deleteById(cartId: Long) {
        return cartDao.deleteById(cartId)
    }

 /*   suspend fun deleteAllItems() {
        return cartDao.deleteAllItems()
    }*/

    // Memperbarui item keranjang
    suspend fun updateCart(cartItem: Cart) {
        return cartDao.update(cartItem)
    }

    // Mendapatkan item keranjang dari database berdasarkan id
   private suspend fun getCartItemById(cartId: Int): Cart? {
        return cartDao.getCartItemById(cartId)
    }


    // Menambahkan item baru ke keranjang dan memperbarui item yang sudah ada
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
            Log.d("LocalDataSource", "Inserted New Cart Item: ${newCartItem.title} - Quantity: ${newCartItem.quantity} - Total Price: ${newCartItem.totalPrice}")
        }
    }
}