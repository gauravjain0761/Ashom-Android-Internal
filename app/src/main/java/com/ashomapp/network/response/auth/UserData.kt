package com.ashomapp.network.response.auth

import com.google.gson.annotations.SerializedName

data class UserData(
    val userdata : UserDTO
)

data class UserDTO(
    @SerializedName("first_name")
    val first_name : String,
    @SerializedName("last_name")
    val last_name : String,
    @SerializedName("email")
    val email : String,
    @SerializedName("country_code")
    val country_code : String,
    @SerializedName("mobile")
    val mobile : String,
    @SerializedName("profile_pic")
    var profile_pic : String,
    @SerializedName("login_type")
    val login_type : String,
    @SerializedName("social_id")
    val social_id : String,
    @SerializedName("token")
    val token : String,
    @SerializedName("email_verified")
    val email_verified : String,
    @SerializedName("subscribed")
    val subscribed : String,
    @SerializedName("subscription_type")
    var subscription_type : String,
    @SerializedName("subscription_expiry_date")
    val subscription_expiry_date : String


)
