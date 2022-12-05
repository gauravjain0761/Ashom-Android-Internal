package com.ashomapp.network.repository


import com.ashomapp.BuildConfig
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.network.response.auth.UserDTO
import com.ashomapp.network.retrofit.getRetrofitService
import com.ashomapp.network.retrofit.safelyCallApi
import com.ashomapp.network.services.Authservices
import com.ashomapp.presentation.home.HomeFrag
import com.ashomapp.utils.UniqueDeviceId
import com.dbvertex.myashomapp.network.repository.HomeRepository

object AuthRepository {
    private val authservices = getRetrofitService(Authservices::class.java)


    suspend fun getUserData(token  : String) = safelyCallApi {
        val user =     authservices.getUserdata(token = token)
        SharedPrefrenceHelper.user =    user.userdata

        user

    }
    suspend fun Registration(
       first_name : String,
       last_name : String,
       email : String,
       mobile : String,
       password : String,
       login_type : String,
       social_id : String,
       countrycode : String

    ) = safelyCallApi {
        authservices.registration(
            first_name, last_name, email, mobile, password, login_type, social_id, UniqueDeviceId.getUniqueId(),
            countrycode, BuildConfig.VERSION_CODE.toString()
        )


    }

    suspend fun userlogin(
        email: String,
        password: String
    ) = safelyCallApi {
        authservices.login(email, password)

    }

    suspend fun verify_OTP(
        mobile: String,
        otp_code : String
    ) = safelyCallApi {
      val result =   authservices.verifyOTP(mobile,otp_code)
        SharedPrefrenceHelper.token = result.token

        result
    }

    suspend fun resendOTP(
        mobile : String
    ) = safelyCallApi {
        authservices.ResendOTP(mobile)
    }

    suspend fun forgetPassword(
        email: String
    ) = safelyCallApi {
        authservices.forgetpassword(email)
    }
}