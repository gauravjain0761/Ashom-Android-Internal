package com.ashomapp.presentation.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashomapp.network.response.SearachDTO
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.utils.temp_showToast
import com.dbvertex.myashomapp.network.repository.HomeRepository
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    val searchStrResult = MutableLiveData<List<SearachDTO>>()
    val searchMostViewResult = MutableLiveData<List<SearachDTO>>()
    val searchRecentlyViewResult = MutableLiveData<List<SearachDTO>>()
    val Searchcompanieslist  = MutableLiveData<List<CompanyDTO>>()
    fun getSearchStr(str : String){
        viewModelScope.launch {
            val result = HomeRepository.getSearchStrResult(str)
            when(result){
                is ResultWrapper.Success ->{
                    val list = mutableListOf<SearachDTO>()
                    list.addAll(result.response.map { it })
                    searchStrResult.value = list
                    Log.d("adddedtosearch", "${result.response.toString()}")
                }
                is ResultWrapper.Failure ->{
                    Log.d("adddedtosearch", "${result.errorMessage}")
                }
            }
        }
    }
    fun getSearchMostview(){
        viewModelScope.launch {
            val result = HomeRepository.getSearchMostView()
            when(result){
                is ResultWrapper.Success ->{
                    val list = mutableListOf<SearachDTO>()
                    list.addAll(result.response.map { it })
                    searchMostViewResult.value = list
                }
                is ResultWrapper.Failure ->{

                }
            }
        }
    }
    fun getSearchRecentlyView(){
        viewModelScope.launch {
            val result = HomeRepository.getrecentlyView()
            when(result){
                is ResultWrapper.Success ->{
                    val list = mutableListOf<SearachDTO>()
                    list.addAll(result.response.map { it })
                    searchRecentlyViewResult.value = list
                }
                is ResultWrapper.Failure ->{

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