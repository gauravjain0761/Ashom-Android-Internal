package com.ashomapp.network.retrofit

import android.util.Log
import androidx.databinding.ktx.BuildConfig
import com.ashomapp.utils.NOT_FOUND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


suspend fun <T> safelyCallApi(apiCall: suspend () -> T): ResultWrapper<out T> {
    return withContext(Dispatchers.IO) {
        try {
            val result = apiCall()

            ResultWrapper.Success(result)


        } catch (throwable: Throwable) {

            val message = when (throwable) {
                is IOException,
                is UnknownHostException -> "Error Connecting to server."
                is SocketTimeoutException -> "Error while connecting to internet."
                is HttpException ->
                    when (throwable.code()) {
                        408 -> "Request Timed Out.\nPlease Check your internet Connection"
                        400 -> throwable.response()!!.errorBody()!!.charStream().readText()
                        500 -> "A Server Error Occurred.\nTry restarting app.\nIf problem persists contact us via email."
                        504 -> "Not connected to Internet"
                        404 -> throwable.response()!!.errorBody()!!.charStream().readText() //do not change this constant
                        401 -> throwable.response()!!.errorBody()!!.charStream().readText()
                        403 -> throwable.response()!!.errorBody()!!.charStream().readText()
                        else -> "An Unexpected Error occurred while completing request."
                    }
                else -> "An Unexpected Error occurred."
            }
            val statuscode = when(throwable){
                is HttpException -> throwable.code()
                else -> 0
            }

            AshomLog(throwable.message, throwable)
            ResultWrapper.Failure(message,statuscode )

        }
    }


}

fun AshomLog(message: String?, throwable: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        Log.d("Ashom.applog", message ?: "an unknown error occurred", throwable)
    }
}