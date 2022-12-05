package com.ashomapp.network.retrofit



import com.ashomapp.utils.API_KEY
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds api key header in every request
 */
class ApiKeyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.header("x-api-key", API_KEY)
        return chain.proceed(requestBuilder.build())
    }
}
