package com.ashomapp.network.retrofit

sealed class ResultWrapper<T> {
    class Success<T>(val response: T) : ResultWrapper<T>()
    class Failure(val errorMessage: String, val status_code : Int ? = null ) : ResultWrapper<Nothing>()
}