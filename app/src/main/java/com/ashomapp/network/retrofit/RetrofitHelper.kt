package com.ashomapp.network.retrofit



import com.ashomapp.AshomAppApplication
import com.ashomapp.utils.API_URL
import com.ashomapp.utils.STOCK_API_URL
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private fun getCache(): Cache {
    val cacheSize = (10 * 1024 * 1024).toLong()
    val cacheDir = AshomAppApplication.instance.cacheDir
    return Cache(cacheDir, cacheSize)
}

private fun getOkHttpClient(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    //for performing request in parallel
    val dispatcher = Dispatcher().apply {
        maxRequests = 3
    }

    return OkHttpClient.Builder()
        .cache(getCache())
        .dispatcher(dispatcher)
        .connectTimeout(500, TimeUnit.SECONDS)
        .readTimeout(500, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .retryOnConnectionFailure(true)
        .addNetworkInterceptor(OnlineCachingInterceptor())
        .addInterceptor(OfflineCachingInterceptor())
        .addInterceptor(ApiKeyInterceptor())
        .build()
}

private fun getRetrofitBuilder(): Retrofit.Builder {
    return Retrofit.Builder().baseUrl(API_URL)
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
}

fun <T> getRetrofitService(clazz: Class<T>): T {
    return getRetrofitBuilder().build().create(clazz)
}

private fun getStockRetrofitBuilder(): Retrofit.Builder {
    return Retrofit.Builder().baseUrl(STOCK_API_URL)
        .client(getOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
}

fun <T> getStockRetrofitService(clazz: Class<T>): T {
    return getStockRetrofitBuilder().build().create(clazz)
}
