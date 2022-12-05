package com.ashomapp.network.response

import com.google.gson.annotations.SerializedName

data class SearachDTO (
    @SerializedName("searchstr")
    val search : String
    )