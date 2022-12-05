package com.ashomapp.network.services


import com.ashomapp.network.response.NotificationDTO
import com.ashomapp.network.response.ResponseDTO
import com.ashomapp.network.response.SearachDTO
import com.ashomapp.network.response.auth.UserData
import com.ashomapp.network.response.dashboard.*
import com.ashomapp.presentation.home.stockPrice.model.StockHistoryModel
import com.ashomapp.presentation.home.stockPrice.model.StockListModel
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.http.*

interface HomeServices {
 /*   https://testnet.ashom.app/api/webservice/recordevent
    POST: token, event*/
    @POST("webservice/recordevent")
    @FormUrlEncoded
    suspend fun recordEvent(
            @Field("token") token : String,
            @Field("event") event : String
    ): JsonObject

    @GET("webservice/forum/{token}")
    suspend fun getForum(
        @Path("token") token : String
    ): List<ForumDTO>

    @GET("webservice/unreadnotification/{token}")
    suspend fun getNotificationCount(
        @Path("token") token : String
    ): Int

    @GET("webservice/selectedcompanies/{token}")
    suspend fun getSelectedCompanies(
        @Path("token") token : String
    ): List<CompanyDTO>
    @GET
    suspend fun getRemainingVisit(
        @Url url: String
    ): JsonObject

    //https://ashom.app/api/webservice/opencompany

    @POST("webservice/opencompany")
    @FormUrlEncoded
    suspend fun postOpenCompany(
        @Field("token") token: String,
        @Field("company_id") company_id: String,
        @Field("is_view") is_view : Int
    ) : ResponseDTO


   //https://ashom.app/api/webservice/subscription
    //token, expire_date (YYYY-MM-DD)  , subscription_type, amount
    //subscription_type -> Free , Monthly, Yearly

    @POST("webservice/subscription")
    @FormUrlEncoded
    suspend fun postsubscription(
        @Field("token") token : String,
        @Field("expire_date") expire_date : String,
        @Field("subscription_type") subscription_type : String,
        @Field("amount") amount : String
    ): ResponseDTO
//https://www.ashom.app/api/webservice/contactus email, name, subject, message (All Fields Required)
    @POST("webservice/contactus")
    @FormUrlEncoded
    suspend fun submitContactUs(
        @Field("email") email : String,
        @Field("name") name : String,
        @Field("subject") subject : String,
        @Field("message") message : String,
        @Field("mobile") phone : String
    )

    //https://ashom.app/api/webservice/usernotifications/{token}

    @GET("webservice/usernotifications/{token}")
    suspend fun getUserNotification(
        @Path("token") token : String
    ) : List<NotificationDTO>

    //https://ashom.app/api/webservice/updateuser
    //token, first_name, last_name, email, mobile (<--mandatory all), profile_pic (optional)


    //https://ashom.app/api/webservice/searchinsert token, searchstr
    @POST("webservice/searchinsert")
    @FormUrlEncoded
    suspend fun getSearchStrResult(
        @Field("token") token : String,
        @Field("searchstr") searchstr : String
    ) : List<SearachDTO>

    @GET("webservice/searches")
    suspend fun getSearchRecentlyview(
        @Query("token") token : String,
    ) : List<SearachDTO>

    @GET("webservice/searches")
    suspend fun getSearchMostView() : List<SearachDTO>

    @POST("webservice/updateuser")
    @Multipart
    suspend fun updateUserPro(
        @Part token :  MultipartBody.Part,
        @Part first_name :  MultipartBody.Part,
        @Part last_name :  MultipartBody.Part,
        @Part email :  MultipartBody.Part,
        @Part mobile :  MultipartBody.Part,
        @Part profile_pic :  MultipartBody.Part ? = null
    ) : UpdateProfileDTO
    //https://ashom.app/api/webservice/updateDeviceToken
    // token, device_token
    @POST("webservice/updateDeviceToken")
    @FormUrlEncoded
    suspend fun updateToken(
        @Field("token") token : String,
        @Field("device_token") device_token : String
    ): ResponseDTO

    //https://ashom.app/api/webservice/changepassword
    //token, old_password, new_password, confirm_password
    @POST("webservice/changepassword")
    @FormUrlEncoded
    suspend fun changepassword(
        @Field("token") token : String,
        @Field("old_password") old_password : String,
        @Field("new_password") new_password : String,
        @Field("confirm_password") confirm_password : String
    ): ResponseDTO

    //https://ashom.app/api/webservice/signout/{token}
    @GET("webservice/signout/{token}")
    suspend fun userLogout(
        @Path("token") token : String
    ) : ResponseDTO


    @POST("webservice/userdata")
    @FormUrlEncoded
    suspend fun getUserdata(
        @Field("token") token : String
    ): UserData

    @GET("webservice/getyears/{company_id}")
    suspend fun getFinancialYear(
        @Path("company_id") company_id : String
    ): List<YearsDTO>

    //https://ashom.app/api/webservice/documents/{company_id}/{year}/{period}

    @GET("webservice/documents/{company_id}/{year}/{period}")
    suspend fun getDocuments(
        @Path("company_id")company_id: String,
        @Path("year")year: String,
        @Path("period")period: String
    ) : List<CompanyStatementsDTO>


    @GET("stock/getall")
    suspend fun getStockList() : StockListModel



    @GET("get/history")
    suspend fun getStockHistory(
        @Query("days") days: String,
        @Query("weeks") weeks: String,
        @Query("months") months: String,
        @Query("years") year: String,
        @Query("spread") spread: String,
        @Query("stockname") stockName: String,
    ) : StockHistoryModel

    @GET("webservice/countries")
    suspend fun getCountry(
    ): List<CountriesDTO>



   // https://www.dbvertex.com/ashoms/api/webservice/companies/KSA
    @GET("webservice/companies/{country_name}/{search}/{page}")
    suspend fun getCompany(
         @Path("country_name") country_name : String,
         @Path("search") search: String,
         @Path("page") page_num  : String

    ) :CompanyRes

    @GET("webservice/companies")
    suspend fun getAllCompany( ) :List<CompanyDTO>

    @GET("webservice/lastcompanyid")
    suspend fun getlastCompanyID( ) :String

    @GET("webservice/companies/{country_name}/{search}")
    suspend fun getSearchCompany(
        @Path("country_name") country_name : String,
        @Path("search") search: String
    ) :List<CompanyDTO>

    @POST("webservice/financialapi")
    @FormUrlEncoded
    suspend fun getNews(
        @Field("page") page_num  : String,
        @Field("countryname") country_name: String,
        @Field("search") search : String ? = null
    ): NewsDTO

    @GET("webservice/financialapi/{page_num}/{country_name}/{company_name}")
    suspend fun getCompanyNews(
        @Path("page_num") page_num  : String,
        @Path("country_name") country_name: String,
        @Path("company_name") company_name: String
    ): NewsDTO


}