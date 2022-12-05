package com.ashomapp.presentation.home

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.network.repository.ForumRepository
import com.ashomapp.network.response.dashboard.*
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.home.stockPrice.model.StockHistoryModel
import com.ashomapp.presentation.home.stockPrice.model.StockListModel
import com.ashomapp.utils.SessionManager
import com.ashomapp.utils.SingleLiveEvent
import com.ashomapp.utils.temp_showToast
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.doctorsplaza.app.utils.Resource
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.util.*

class HomeViewModel : ViewModel() {

    val forumlist = MutableLiveData<List<ForumDTO>>()
    val countrylist = MutableLiveData<List<CountriesDTO>>()
    val companieslist = MutableLiveData<List<CompanyDTO>>()
    val Searchcompanieslist = MutableLiveData<List<CompanyDTO>>()
    val yearsDTO = MutableLiveData<List<YearsDTO>>()
    val CompanyDocumet = MutableLiveData<List<CompanyStatementsDTO>>()

    val companyID = MutableLiveData<String>()
    val myear = MutableLiveData<String>()
    val mPeriod = MutableLiveData("")

    val user_not_found = MutableLiveData(false)

    val stocksList = SingleLiveEvent<StockListModel>()

    val stockHistory = SingleLiveEvent<Resource<StockHistoryModel>>()

    val stockCompanyNews = SingleLiveEvent<NewsDTO>()

    val stockCompaniesList = SingleLiveEvent<List<CompanyDTO>>()

    fun recordEvent(event: String) {
        viewModelScope.launch {
            val result = HomeRepository.recordEvent(event)
            when (result) {
                is ResultWrapper.Success -> {}
                is ResultWrapper.Failure -> {}
            }
        }
    }

    fun getCompanyDocumetn() {
        viewModelScope.launch {
            val result =
                HomeRepository.getdoucment(companyID.value.toString(), myear.value.toString(),
                    mPeriod.value.toString())
            when (result) {
                is ResultWrapper.Success -> {
                    val list = mutableListOf<CompanyStatementsDTO>()
                    list.addAll(result.response.map { it })
                    CompanyDocumet.value = list
                }
                is ResultWrapper.Failure -> {

                }
            }
        }
    }

    fun getStockLists(requireContext: Context) {
        val sessionManager = SessionManager(requireContext)

        var errorCall = 0
        viewModelScope.launch {
            val result = HomeRepository.getStockList()

            when (result) {
                is ResultWrapper.Success -> {

                    stocksList.postValue(result.response)
                }

                is ResultWrapper.Failure -> {
                    if (sessionManager.stockListLocal.isEmpty() || sessionManager.stockListLocal == "null") {
                        Toast.makeText(requireContext,
                            "Something went wrong, Please try again later",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        val stockItem = Gson().fromJson(sessionManager.stockListLocal, StockListModel::class.java)
                        stocksList.postValue(stockItem)
                    }
                    errorCall += 1
                }
            }
        }
    }

    fun getStockCompanyNews(pageNo: Int, countryName: String, companyName: String) {
        viewModelScope.launch {
            when (val result =
                HomeRepository.getStockCompanyNews(pageNo, countryName, companyName)) {
                is ResultWrapper.Success -> {
                    stockCompanyNews.postValue(result.response)
                }
                is ResultWrapper.Failure -> {

                }
            }
        }
    }

    fun getStockCountryNews(pageNo: Int, countryName: String, companyName: String) {
        viewModelScope.launch {
            when (val result =
                HomeRepository.getStockCountryNews(pageNo, countryName, companyName)) {
                is ResultWrapper.Success -> {
                    stockCompanyNews.postValue(result.response)
                }
                is ResultWrapper.Failure -> {

                }
            }
        }
    }

    fun getStockHistory(
        days: String,
        weeks: String,
        months: String,
        year: String,
        spread: String,
        stockName: String,
    ) {
        viewModelScope.launch {



            stockHistory.postValue(Resource.Loading())
            try {
                val result = HomeRepository.getStockHistory(days, weeks, months, year, spread, stockName)
                when (result) {
                    is ResultWrapper.Success -> {
                        stockHistory.postValue(Resource.Success(result.response))

                    }
                    is ResultWrapper.Failure -> {
                        stockHistory.postValue(Resource.Error(result.errorMessage))
                    }
                }

            } catch (t: Throwable) {
                stockHistory.postValue(Resource.Error(checkThrowable(t), null))
            }
        }
    }


    fun getcompanyYear(companyID: String) {

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

    fun getForum() {
        viewModelScope.launch {
            val result = HomeRepository.getForum()
            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("forumdata", result.response.toString())
                    val list = mutableListOf<ForumDTO>()
                    list.addAll(result.response.map { it })
                    HomeFlow.mFforumlist.value = list
                }
                is ResultWrapper.Failure -> {
                    if (result.status_code == 404) {
                        try {
                            val josnobject = JSONObject(result.errorMessage.toString())

                            val message = "${josnobject.getString("message")}"
                            if (message.equals("User not found.")) {
                                user_not_found.value = true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    temp_showToast("${result.errorMessage}")
                }
            }
        }
    }

    fun updateshareprefrenceValue(usertoken: String) {
        SharedPrefrenceHelper.token = usertoken
        HomeFrag.userToken.value = usertoken
        HomeRepository.token = usertoken
        ForumRepository.mtoken = usertoken
    }

    fun getCountry(countrygrid: String? = null) {
        viewModelScope.launch {
            val result = HomeRepository.getCountry()
            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("forumdata", result.response.toString())

                    val list = mutableListOf<CountriesDTO>()

                    list.addAll(result.response.map { it })
                    countrylist.value = list
                }
                is ResultWrapper.Failure -> {
                    temp_showToast("${result.errorMessage}")
                }
            }
        }
    }

    fun getCompanyDataqItem(country_name: String, search: String) =
        HomeRepository.getPagingCompany(country_name = country_name, search = search)
            .cachedIn(viewModelScope)

    fun getCompany() {
        Log.d("android ", "testing android \n\n")
        viewModelScope.launch {
            val result = HomeRepository.getCompany()
            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("forumdata", result.response.toString())
                    val list = mutableListOf<CompanyDTO>()
                    list.addAll(result.response.map { it })
                    stockCompaniesList.postValue(list)
                    companieslist.value = list
                }
                is ResultWrapper.Failure -> {
                    temp_showToast("${result.errorMessage}")
                }
            }
        }
    }

    fun getSearchCompany(companyname: String, search: String) {
        Log.d("android ", "testing android \n\n")
        viewModelScope.launch {
            val result = HomeRepository.getSearchCompany(companyname, search)
            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("forumdata", result.response.toString())
                    val list = mutableListOf<CompanyDTO>()
                    list.addAll(result.response.map { it })

                    Searchcompanieslist.value = list
                }
                is ResultWrapper.Failure -> {
                    Log.d("error", result.errorMessage.toString())
                    temp_showToast("${result.errorMessage}")
                }
            }
        }
    }

    private fun checkThrowable(t: Throwable): String {
        return when (t) {
            is IOException ->
                "Network Failure"
            else -> "Conversion Error ${t.message}"

        }
    }
}