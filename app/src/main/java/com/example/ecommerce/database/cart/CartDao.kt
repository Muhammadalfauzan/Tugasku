package com.example.ecommerce.database.cart

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CartDao {

    @Insert(onConflict =  OnConflictStrategy.IGNORE)
    suspend fun insert(cartItem : Cart)

    @Query("DELETE FROM cart_items")
    suspend fun deleteAllItems()

    @Query("SELECT * FROM cart_items")
    fun  getAllCartItems(): LiveData<List<Cart>>

    @Query("DELETE FROM CART_ITEMS WHERE id = :cartId")
    suspend fun deleteById(cartId:Long)

    @Update
    suspend fun update(cartItem: Cart)

    @Query("SELECT * FROM cart_items WHERE id = :cartId LIMIT 1")
    suspend fun getCartItemById(cartId: Int): Cart?
}
