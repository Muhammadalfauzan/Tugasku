package com.example.ecommerce.di

import android.content.Context
import androidx.room.Room
import com.example.ecommerce.database.cart.CartDatabase
import com.example.ecommerce.database.product.ProductDatabase
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
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
        context,
        CartDatabase::class.java,
        "cart_database"
    )   .openHelperFactory(factory)
        .fallbackToDestructiveMigration()
        .build()
    }


    @Singleton
    @Provides
    fun provideCartDao(cartDatabase: CartDatabase) = cartDatabase.cartDao()

    /** MENU DATABASE **/
    @Singleton
    @Provides
    fun provideProductDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        ProductDatabase::class.java,
        "product_database"
    ).build()

    @Singleton
    @Provides
    fun provideMenuDao(database: ProductDatabase) = database.productDao()
}
