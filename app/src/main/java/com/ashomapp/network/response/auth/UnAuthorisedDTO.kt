package com.ashomapp.network.response.auth

import com.google.gson.annotations.SerializedName

data class UnAuthorisedDTO(
    //{"status":false,"message":"Please! Verify OTP Before Login","mobile":"7974732267","otp_verfied":false,
    // "country_code":"91","user_email":"dbvertexsharma@gmail.com"}

    @SerializedName("status")
val status : Boolean,
    @SerializedName("message")
val message : String,
    @SerializedName("mobile")
    val mobile : String,
    @SerializedName("otp_verfied")
    val otp_verfied : String,
    @SerializedName("country_code")
    val country_code : String,
    @SerializedName("user_email")
    val user_email : String,

)
