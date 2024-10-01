package com.example.ecommerce.di

import com.example.ecommerce.data.api.ApiService
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton



@Module
@InstallIn(SingletonComponent::class)

object NetworkModule {
    /** RETROFIT **/
    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        // CertificatePinner untuk SSL Pinning
        val certificatePinner = CertificatePinner.Builder()
            .add("fakestoreapi.com", "sha256/gmq/FyOqZeCKzhuonKNzGOtWWb8Xl2kGZoc1ptqDxME=")
            .add("fakestoreapi.com", "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=")
            .add("fakestoreapi.com", "sha256/mEflZT5enoR1FuXLgYYGqnVEoZvmf9c2bVBpiOjYQ0c=")
            .build()

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .certificatePinner(certificatePinner)
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /*@Singleton
    @Provides
    fun provideCertificate(context: Context): X509TrustManager {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificateInputStream: InputStream = context.assets.open("your_certificate.crt") // Sertakan file sertifikat di folder 'assets'
        val certificate = certificateFactory.generateCertificate(certificateInputStream) as X509Certificate
        certificateInputStream.close()

        // Memasukkan sertifikat ke KeyStore
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("server", certificate)

        // Gunakan KeyStore untuk TrustManager
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        val trustManagers = trustManagerFactory.trustManagers
        return trustManagers[0] as X509TrustManager
    }*/

    @Singleton
    @Provides
    fun provideConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Singleton
    @Provides
    fun provideRetrofitInstance(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://fakestoreapi.com/")
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    /** FIREBASE **/
    @Singleton
    @Provides
    fun providesFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}