package com.ashomapp.presentation.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.network.response.auth.UserDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.auth.NetworkState
import com.ashomapp.utils.temp_showToast
import com.dbvertex.myashomapp.network.repository.HomeRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    val pro_email = MutableLiveData<String>()
    val pro_firstname = MutableLiveData<String>()
    val pro_phone = MutableLiveData<String>()
    val pro_lastname = MutableLiveData<String>()
    var profile_pic: Pair<ByteArray, String>? = null

    val updateprofileNetworkState = MutableLiveData<NetworkState>()
    fun updateprofile(){
        updateprofileNetworkState.value = NetworkState.LOADING_STARTED
        viewModelScope.launch {
            val result = HomeRepository.updateUserPro(
            first_name = pro_firstname.value.toString(),
            last_name = pro_lastname.value.toString(),
            email = pro_email.value.toString(),
            mobile = pro_phone.value.toString(),
            profile_pic = profile_pic)
            updateprofileNetworkState.value = NetworkState.LOADING_STOPPED
            when(result){
                is ResultWrapper.Success ->{
                    SharedPrefrenceHelper.user = result.response.userdata
                    updateprofileNetworkState.value = NetworkState.SUCCESS
                  //  temp_showToast("Profile update successfully.")
                    Log.d("userupdate", result.response.toString())
                }
                is ResultWrapper.Failure ->{
                    Log.d("userupdatef", result.errorMessage.toString())
                   // temp_showToast("Something wrong.Try again later")
                    updateprofileNetworkState.value = NetworkState.FAILED
                }
            }


        }
    }



    fun setProfileData(user : UserDTO){
        pro_firstname.value = user.first_name
        pro_email.value = user.email
        pro_phone.value = user.mobile
        pro_lastname.value = user.last_name
    }
}