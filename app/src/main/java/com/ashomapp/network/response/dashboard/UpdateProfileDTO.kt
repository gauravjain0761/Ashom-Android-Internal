package com.ashomapp.network.response.dashboard

import com.ashomapp.network.response.auth.UserDTO
import com.google.gson.annotations.SerializedName

data class UpdateProfileDTO(
    @SerializedName("status")
    val status : Boolean,

    @SerializedName("message")
    val message : String,

    @SerializedName("userdata")
    val userdata : UserDTO
)
