package com.ashomapp.presentation.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.network.repository.ForumRepository
import com.ashomapp.network.response.dashboard.*
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.auth.NetworkState
import com.ashomapp.utils.temp_showToast
import com.dbvertex.myashomapp.network.repository.HomeRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeViewModel : ViewModel() {

    val forumlist  = MutableLiveData<List<ForumDTO>>()
    val countrylist  = MutableLiveData<List<CountriesDTO>>()
    val companieslist  = MutableLiveData<List<CompanyDTO>>()
    val Searchcompanieslist  = MutableLiveData<List<CompanyDTO>>()
    val yearsDTO  = MutableLiveData<List<YearsDTO>>()
    val CompanyDocumet  = MutableLiveData<List<CompanyStatementsDTO>>()

    val companyID  = MutableLiveData<String>()
    val myear = MutableLiveData<String>()
    val mPeriod = MutableLiveData<String>("")

    val user_not_found = MutableLiveData<Boolean>(false)

    fun recordEvent(event : String) {
        viewModelScope.launch {
            val result = HomeRepository.recordEvent(event)
            when(result){
                is ResultWrapper.Success ->{}
                is ResultWrapper.Failure ->{}
            }
        }
    }

    fun getCompanyDocumetn(){
        viewModelScope.launch {
            val result = HomeRepository.getdoucment(companyID.value.toString(), myear.value.toString(),
                mPeriod.value.toString())
            when(result){
                is ResultWrapper.Success ->{
                    val list = mutableListOf<CompanyStatementsDTO>()
                    list.addAll(result.response.map { it })
                    CompanyDocumet.value = list
                }
                is ResultWrapper.Failure ->{

                }
            }
        }
    }


    fun getcompanyYear(companyID : String) {

        viewModelScope.launch {
            val result = HomeRepository.getFinanacialsYears(companyID)
            when (result) {
                is ResultWrapper.Success -> {
                    val list = mutableListOf<YearsDTO>()
                 list.addAll(result.response.map { it })
                    yearsDTO.value = list
                }
                is ResultWrapper.Failure -> {

                }
            }
        }
    }
    fun getForum(){
        viewModelScope.launch {
            val result = HomeRepository.getForum()
            when(result){
                is ResultWrapper.Success ->{

                    Log.d("forumdata", result.response.toString())
                    val list  = mutableListOf<ForumDTO>()
                    list.addAll(result.response.map { it })
                   HomeFlow.mFforumlist.value = list
                }
                is ResultWrapper.Failure ->{

                    if(result.status_code == 404){
                        try {
                            val josnobject = JSONObject(result.errorMessage.toString())

                            val message =  "${josnobject.getString("message")}"
                            if (message.equals("User not found.")){
                                user_not_found.value = true
                            }
                        }catch (e :Exception){
                            e.printStackTrace()
                        }
                    }
                    //when api key not found!!!!
                    try {


                        val josnobject = JSONObject(result.errorMessage.toString())

                        val message = "${josnobject!!.getString("error")}"
                        if (message != null && message.equals("Invalid API key ")) {
                            temp_showToast(message)

                        } else if (message != "Invalid API key " && message != null) {
                            temp_showToast("${message}")

                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }


                }
            }
        }
    }

    fun  updateshareprefrenceValue(usertoken  : String){
        SharedPrefrenceHelper.token = usertoken
        HomeFrag.userToken.value = usertoken
        HomeRepository.token = usertoken
        ForumRepository.mtoken = usertoken
    }
    fun getCountry( countrygrid : String ? = null){
        viewModelScope.launch {
            val result = HomeRepository.getCountry()
            when(result){
                is ResultWrapper.Success ->{
                    Log.d("forumdata", result.response.toString())

                    val list  = mutableListOf<CountriesDTO>()

                    list.addAll(result.response.map { it })
                    countrylist.value = list
                }
                is ResultWrapper.Failure ->{
                    temp_showToast("${result.errorMessage}")
                }
            }
        }
    }

    fun getCompanyDataqItem(country_name : String, search : String ) = HomeRepository.getPagingCompany(country_name = country_name, search = search).cachedIn(viewModelScope)

    fun getCompany(){
        Log.d("android ", "testing android \n\n")
        viewModelScope.launch {
            val result = HomeRepository.getCompany()
            when(result){
                is ResultWrapper.Success ->{
                    Log.d("forumdata", result.response.toString())
                       val list = mutableListOf<CompanyDTO>()
                   list.addAll(result.response.map { it })

                    companieslist.value = list
                }
                is ResultWrapper.Failure ->{
                    temp_showToast("${result.errorMessage}")
                }
            }
        }
    }
    fun getSearchCompany(companyname : String, search : String){
        Log.d("android ", "testing android \n\n")
        viewModelScope.launch {
            val result = HomeRepository.getSearchCompany(companyname,search)
            when(result){
                is ResultWrapper.Success ->{
                    Log.d("forumdata", result.response.toString())
                    val list = mutableListOf<CompanyDTO>()
                    list.addAll(result.response.map { it })

                    Searchcompanieslist.value = list
                }
                is ResultWrapper.Failure ->{
                    Log.d("error", result.errorMessage.toString())
                    temp_showToast("${result.errorMessage}")
                }
            }
        }
    }
}