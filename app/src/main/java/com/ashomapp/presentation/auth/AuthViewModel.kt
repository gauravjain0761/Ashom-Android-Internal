package com.ashomapp.presentation.auth

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashomapp.network.repository.AuthRepository
import com.ashomapp.network.response.auth.UnAuthorisedDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.utils.temp_showToast
import com.google.gson.Gson
import kotlinx.coroutines.launch
import android.widget.Toast
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.presentation.home.HomeFrag
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

import retrofit.Result.response
import java.io.IOException
import org.json.JSONException

import retrofit.Result.response

import org.json.JSONObject





enum class NetworkState {
    LOADING_STARTED, LOADING_STOPPED, SUCCESS, FAILED, UN_AUTHORIZED
}
class AuthViewModel : ViewModel() {
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>("")

    val firstname = MutableLiveData<String>()
    val phone = MutableLiveData<String>("")
    val lastname = MutableLiveData<String>()
    val mtoken = MutableLiveData<String>()
    val country_code = MutableLiveData<String>("971")
    val logintype = MutableLiveData<String>("normal")
    val social_id  = MutableLiveData<String>("")

    val otp_verification_code = MutableLiveData<String>()

    val registerNetworkState = MutableLiveData<NetworkState>()
    val loginNetworkState = MutableLiveData<NetworkState>()
    val forgetpassNetworkState = MutableLiveData<NetworkState>()
    val otp_verified = MutableLiveData<Boolean>(false)

    val loginError = MutableLiveData<String>()
    fun registered(){
        registerNetworkState.value = NetworkState.LOADING_STARTED
        viewModelScope.launch {
            val result = AuthRepository.Registration(firstname.value.toString(), lastname.value.toString(),
                email.value.toString(), "${phone.value}", password.value.toString(),
                logintype.value.toString(), social_id = social_id.value.toString(), countrycode = country_code.value.toString())
            registerNetworkState.value = NetworkState.LOADING_STOPPED
            when(result){
                is ResultWrapper.Success ->{
                   if (result.response.status){

                       registerNetworkState.value = NetworkState.SUCCESS

                   }else{
                       registerNetworkState.value = NetworkState.FAILED
                   }
                    Log.d("responsereg", result.response.toString())
                   // temp_showToast("${result.response}")
                }
                is ResultWrapper.Failure ->{
                    registerNetworkState.value = NetworkState.FAILED
                    try {
                        val josnobject = JSONObject(result.errorMessage.toString())

                           val phoneError =  "${josnobject.getString("mobile_error")}"
                        val emailError =  "${josnobject.getString("email_error")}"
                         // temp_showToast("$phoneError  $emailError")
                            if (!phoneError.isNullOrEmpty()){
                                loginError.value = phoneError
                            }else if (!emailError.isNullOrEmpty()){
                                loginError.value = emailError
                            }
                        Log.d("responseregjson", josnobject.toString() + "${josnobject.getString("message")}")
                    }catch (e : Exception){
                        Log.d("responseregjsonerror", e.localizedMessage.toString())
                        loginNetworkState.value = NetworkState.FAILED

                    }
                }
            }
        }
    }

    fun  userlogin(){
        loginNetworkState.value = NetworkState.LOADING_STARTED
        viewModelScope.launch {
            val result = AuthRepository.userlogin(email.value.toString(), password.value.toString())
            loginNetworkState.value = NetworkState.LOADING_STOPPED
            when(result){
                is ResultWrapper.Success ->{

                       if (result.response.status){
                           otp_verified.value = true
                           HomeFrag.userToken.value = result.response.token
                           SharedPrefrenceHelper.token = result.response.token
                           loginNetworkState.value = NetworkState.SUCCESS

                       }else{
                           loginNetworkState.value = NetworkState.FAILED
                       }
                    Log.d("responsereg", result.response.toString())
                    //  temp_showToast("${result.response}")
                }

                is ResultWrapper.Failure ->{
                     try {
                            val josnobject = JSONObject(result.errorMessage.toString())

                            country_code.value = "${josnobject.getString("country_code")}"
                            phone.value = "${josnobject.getString("mobile")}"
                            email.value = "${josnobject.getString("user_email")}"
                         Log.d("responseregjson", josnobject.toString() + "${josnobject.getString("message")}")

                         loginNetworkState.value = NetworkState.UN_AUTHORIZED
                        }catch (e : Exception){
                            Log.d("responseregjsonerror","${result.errorMessage}"+ e.localizedMessage.toString())
                           try {
                               val josnobject = JSONObject(result.errorMessage.toString())
                               val error = josnobject.getString("login_error")
                               val email_error = josnobject.getString("email_error")
                               val password_error = josnobject.getString("password_error")
                                //   temp_showToast("${(error)}.")
                               if (!error.isNullOrEmpty()){
                                   loginError.value = error
                               }else if (!email_error.isNullOrEmpty()){
                                   loginError.value = email_error
                               }else if (!password_error.isNullOrEmpty()){
                                   loginError.value = password_error
                               }
                               loginNetworkState.value = NetworkState.FAILED
                           }catch (e : Exception){
                               if (result.status_code == 400){
                                   val josnobject = JSONObject(result.errorMessage.toString())
                                   val error = josnobject.getString("message")
                                   loginError.value = error
                               }
                               else{
                                   loginError.value = e.message
                               }
                              // temp_showToast("Something went wrong. ${e.message.toString()}")
                               loginNetworkState.value = NetworkState.FAILED
                           }


                        }


                    }

            }
        }
    }


    fun  forgetPassword(){
        forgetpassNetworkState.value = NetworkState.LOADING_STARTED
        viewModelScope.launch {
            val result = AuthRepository.forgetPassword(email.value.toString())
            forgetpassNetworkState.value = NetworkState.LOADING_STOPPED
            when(result){
                is ResultWrapper.Success ->{
                    forgetpassNetworkState.value = NetworkState.SUCCESS
                    Log.d("responsereg", result.response.toString())
                    //  temp_showToast("${result.response}")
                }
                is ResultWrapper.Failure ->{
                    forgetpassNetworkState.value = NetworkState.FAILED
                    Log.d("responsereg", result.errorMessage.toString())
                    if (result.status_code == 404){
                        try {

                            val josnobject = JSONObject(result.errorMessage.toString())
                            val email_error = josnobject.getString("email_error")
                            if (!email_error.isNullOrEmpty()){
                                loginError.value = email_error
                            }
                        }catch (e :Exception){
                            Log.d("responsereg", e.toString())
                            temp_showToast("${result.errorMessage}")
                        }
                    }else{
                        temp_showToast("${result.errorMessage}")
                    }

                }
            }
        }
    }

     fun  updateshareprefrenceValue(usertoken  : String){
         SharedPrefrenceHelper.token = usertoken
     }


}