package com.ashomapp.network.response

import com.google.gson.annotations.SerializedName

data class ResponseDTO(
    @SerializedName("status")
    val status : Boolean,

    @SerializedName("message")
    val message : String
)
