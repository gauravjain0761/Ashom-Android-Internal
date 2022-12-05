package com.ashomapp.network.retrofit



import com.ashomapp.AshomAppApplication
import okhttp3.Interceptor
import okhttp3.Response

class OfflineCachingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val isConnected = AshomAppApplication.instance.isConnectedToInternet()
        if (!isConnected) {
            val maxStale = 60 * 60 * 24 * 10 // Offline cache available for 10 days
            request = request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                .removeHeader("Pragma")
                .build()
        }
        return chain.proceed(request)
    }
}
