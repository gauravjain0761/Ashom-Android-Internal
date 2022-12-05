package com.dbvertex.myashomapp.network.response

import com.google.gson.annotations.SerializedName

data class loginDTO(

    @SerializedName("status")
    val status : Boolean,

    @SerializedName("message")
    val message : String,

    @SerializedName("token")
    val token : String,
)
