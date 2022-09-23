package com.ebenezer.gana.newsapp.di

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.ebenezer.gana.newsapp.network.InternetConnectivityChecker
import com.ebenezer.gana.newsapp.network.api.NewsApi
import com.ebenezer.gana.newsapp.util.Constants.Companion.API_KEY
import com.ebenezer.gana.newsapp.util.Constants.Companion.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkingModule {

    @Provides
    fun provideFactory(): Converter.Factory {
        return GsonConverterFactory.create()
    }

    @Provides
    fun provideClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(buildQueryParameterInterceptor())
            .build()

    fun buildQueryParameterInterceptor() = Interceptor { chain ->
        val originalRequest = chain.request()
        val url = originalRequest.url.newBuilder()
            .addQueryParameter("apiKey", API_KEY)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(url)
            .build()
        chain.proceed(newRequest)
    }

    @Provides
    fun buildRetrofit(client: OkHttpClient, factory: Converter.Factory): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(factory)
            .build()

    }

    @Provides
    fun buildApiService(retrofit: Retrofit): NewsApi =
        retrofit.create(NewsApi::class.java)


    @RequiresApi(Build.VERSION_CODES.M)
    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService(
            ConnectivityManager::class.java
        )

    @Provides
    @Singleton
    fun provideInternetConnectivityChecker(connectivityManager: ConnectivityManager) =
        InternetConnectivityChecker(connectivityManager)

}