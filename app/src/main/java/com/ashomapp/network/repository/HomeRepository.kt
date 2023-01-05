package com.dbvertex.myashomapp.network.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.network.repository.Company.CompanyPagingSource
import com.ashomapp.network.repository.ForumRepository
import com.ashomapp.network.repository.news.NewsPaginSource
import com.ashomapp.network.response.auth.UserDTO
import com.ashomapp.network.retrofit.getRetrofitService
import com.ashomapp.network.retrofit.safelyCallApi
import com.ashomapp.network.services.HomeServices
import com.ashomapp.utils.API_URL
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception


object HomeRepository {
    private val services = getRetrofitService(HomeServices::class.java)
    private const val NEWS_PAGE_SIZE = 1
    var token = SharedPrefrenceHelper.token.toString()

    suspend fun recordEvent(event : String) = safelyCallApi {
        services.recordEvent(SharedPrefrenceHelper.token.toString(), event)
    }
    suspend fun getForum() = safelyCallApi {
         services.getForum(SharedPrefrenceHelper.token.toString())
    }

    suspend fun getNotificationcount(token : String) = safelyCallApi {
        services.getNotificationCount(token)
    }

    suspend fun getNotification(token : String) = safelyCallApi {
        services.getUserNotification(token)
    }
    suspend fun getSelectedComapnies() = safelyCallApi {
        services.getSelectedCompanies(SharedPrefrenceHelper.token.toString())
    }
    suspend fun getRemainingVisit(companyid: String) = safelyCallApi {
        services.getRemainingVisit("$API_URL/webservice/remainvisits?token=${SharedPrefrenceHelper.token.toString()}&company_id=$companyid")
    }

    suspend fun postOpenCompany(companyid: String , is_view : Int) = safelyCallApi {
        services.postOpenCompany(SharedPrefrenceHelper.token.toString(), companyid, is_view)
    }

    suspend fun postSubscription(expire_data : String, subscription_type : String , amount : String)
    = safelyCallApi {
        services.postsubscription(SharedPrefrenceHelper.token.toString(),expire_data, subscription_type, amount)
    }
    suspend fun submitContactUs(name : String, email : String, subject : String , message : String, phone : String) = safelyCallApi {
        services.submitContactUs(email, name, subject, message, phone = phone)
    }
    suspend fun getSearchStrResult(str : String) = safelyCallApi {
        services.getSearchStrResult(SharedPrefrenceHelper.token.toString(), str)
    }

    suspend fun getSearchMostView() = safelyCallApi {
        services.getSearchMostView()
    }
    suspend fun getrecentlyView() = safelyCallApi {
        services.getSearchRecentlyview(SharedPrefrenceHelper.token.toString())
    }

    //  //token, first_name, last_name, email, mobile (<--mandatory all), profile_pic (optional)
    suspend fun updateUserPro(first_name : String,last_name : String,email : String,mobile : String,
                              profile_pic : Pair<ByteArray, String>? = null) = safelyCallApi {
        try {
            val imageRequestBody = profile_pic!!.first.toRequestBody("image/*".toMediaType())
            val mprofileImage = imageRequestBody?.let {
                MultipartBody.Part.createFormData("profile_pic", profile_pic.second,
                    it
                )
            }
            val token = MultipartBody.Part.createFormData("token", SharedPrefrenceHelper.token.toString())
            val mfirst_name = MultipartBody.Part.createFormData("first_name", first_name.toString())
            val mlast_name = MultipartBody.Part.createFormData("last_name", last_name.toString())
            val memail = MultipartBody.Part.createFormData("email", email.toString())
            val mmobile = MultipartBody.Part.createFormData("mobile", mobile.toString())
           services.updateUserPro(token, mfirst_name,mlast_name,memail, mmobile, mprofileImage)


        }catch (e : Exception){
            val token = MultipartBody.Part.createFormData("token", SharedPrefrenceHelper.token.toString())
            val first_name = MultipartBody.Part.createFormData("first_name", first_name.toString())
            val last_name = MultipartBody.Part.createFormData("last_name", last_name.toString())
            val email = MultipartBody.Part.createFormData("email", email.toString())
            val mobile = MultipartBody.Part.createFormData("mobile", mobile.toString())
            services.updateUserPro(token, first_name,last_name,email, mobile)
        }
    }

    suspend fun changePassword(
        oldpass : String,
        newpass :String,
        confirmpass : String
    ) = safelyCallApi {
        services.changepassword(SharedPrefrenceHelper.token.toString(), oldpass, newpass,confirmpass)
    }

     suspend fun UserLogout() = safelyCallApi {
         services.userLogout(SharedPrefrenceHelper.token.toString())
     }
    suspend fun updateToken(device_token : String) = safelyCallApi {
        services.updateToken(SharedPrefrenceHelper.token.toString(), device_token)
    }

    suspend fun getUserData(mtoken : String ? = null) = safelyCallApi {
      val  result =   if (!mtoken.isNullOrEmpty()){
             services.getUserdata(mtoken!!)
         }else{
             services.getUserdata(SharedPrefrenceHelper.token.toString())
         }
        SharedPrefrenceHelper.user = result.userdata
        result
    }
    suspend fun getFinanacialsYears (companyid : String) = safelyCallApi {
        services.getFinancialYear(companyid)
    }

    suspend fun getdoucment(companyid: String, year : String, period : String) = safelyCallApi {
             services.getDocuments(companyid, year, period)
    }
    suspend fun getCountry () = safelyCallApi {
        services.getCountry()
    }

    suspend fun getlastcompanyid() = safelyCallApi {
        services.getlastCompanyID()
    }

    suspend fun getCompany () = safelyCallApi{
       services.getAllCompany()
    }
    suspend fun getSearchCompany (country : String, search: String) = safelyCallApi{
        services.getSearchCompany(country, search = search)
    }
    fun getPagingCompany(country_name : String ,search : String) = Pager(
        config = PagingConfig(
            pageSize = NEWS_PAGE_SIZE,
            enablePlaceholders = true
        ),
        pagingSourceFactory ={
            CompanyPagingSource(services, country_name, search = search)
        }
    ).flow
     fun getNews(country_name : String ,search : String ? = null) = Pager(
        config = PagingConfig(
           pageSize = NEWS_PAGE_SIZE,
            enablePlaceholders = true
        ),
        pagingSourceFactory ={
               NewsPaginSource(services, country_name, search = search)
        }
    ).flow

    fun getCompanyNews(country_name : String, company_name : String) = Pager(
        config = PagingConfig(
            pageSize = NEWS_PAGE_SIZE,
            enablePlaceholders = true
        ),
        pagingSourceFactory ={
            NewsPaginSource(services, country_name, company_name)
        }
    ).flow
}