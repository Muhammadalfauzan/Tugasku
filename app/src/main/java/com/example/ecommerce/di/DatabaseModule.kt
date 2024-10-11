package com.example.ecommerce.di

import android.content.Context
import androidx.room.Room
import com.example.ecommerce.database.cart.CartDatabase
import com.example.ecommerce.database.product.ProductDatabase
import com.example.ecommerce.utils.SharedPreferencesUser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton


@Module
@InstallIn (SingletonComponent::class)
object DatabaseModule {

/** CART DATABASE **/
    @Singleton
    @Provides
    fun provideCartDatabase(
        @ApplicationContext context: Context
    ): CartDatabase {
        val passphrase : ByteArray = SQLiteDatabase.getBytes("ecommerce-app-key".toCharArray())
        // Membuat factory untuk enkripsi database menggunakan passphrase
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
        context,
        CartDatabase::class.java,
        "cart_database"
    )   .openHelperFactory(factory)  // mengatur enkripsi database
        .fallbackToDestructiveMigration()
        .build()
    }

    // Menyediakan instance dari cartDao untuk operasi CRUD pada tabel cart
    @Singleton
    @Provides
    fun provideCartDao(cartDatabase: CartDatabase) = cartDatabase.cartDao()

    /** MENU DATABASE **/
    @Singleton
    @Provides
    fun provideProductDatabase(
        // Menggunakan Room untuk membangun instance dari ProductDatabase dengan nama "product_database"
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        ProductDatabase::class.java,
        "product_database"
    ).build()
    // Menyediakan instance dari cartDao untuk operasi CRUD pada tabel product
    @Singleton
    @Provides
    fun provideMenuDao(database: ProductDatabase) = database.productDao()

    // Menyediakan instance dari sharedpreferencesUser
    @Provides
    @Singleton
    fun provideSharedPreferencesUser(@ApplicationContext context: Context): SharedPreferencesUser {
        return SharedPreferencesUser(context) // Membuat objek SharedPreferencesUser dengan context aplikasi
    }
}
