package com.ashomapp.network.services

import com.ashomapp.network.response.ResponseDTO
import com.ashomapp.network.response.auth.UserData
import com.dbvertex.myashomapp.network.response.loginDTO
import com.google.gson.JsonObject
import com.squareup.okhttp.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface Authservices {

   // https://www.dbvertex.com/ashoms/api/webservice/user
    //first_name, last_name, email, mobile, password(optional in social login),
    // login_type (normal, linkedin, apple, google),
    // social_id(optional in normal), profile_pic(optional), device_id, country_code(optional)
    /*first_name, last_name, email, mobile, password(optional in social login),
    login_type (normal, linkedin, apple, google),
    social_id(optional in normal), profile_pic(optional), device_id, country_code(option)*/
    @FormUrlEncoded
    @POST("webservice/user")
    suspend fun registration(
        @Field("first_name") first_name : String,
        @Field("last_name") last_name : String,
        @Field("email")email : String,
        @Field("mobile")mobile : String,
        @Field("password")password : String,
        @Field("login_type")login_type : String,
        @Field("social_id")social_id : String,
        @Field("device_id")device_id : String,
        @Field("country_code")country_code : String,
        @Field("app_version")app_version : String
    ) : ResponseDTO

    @FormUrlEncoded
    @POST("webservice/login")
    suspend fun login(
        @Field("email")email : String,
        @Field("password")password : String,

    ): loginDTO

    @POST("webservice/userdata")
    @FormUrlEncoded
    suspend fun getUserdata(
        @Field("token") token : String
    ): UserData


    //https://ashom.app/api/webservice/verifyotp/{mobile}/{otp}
    @GET("webservice/verifyotp/{mobile}/{otp}")
    suspend fun verifyOTP(
        @Path("mobile") mobile : String,
        @Path("otp")otp : String,
        ): loginDTO
    @GET("webservice/resendotp/{mobile}")
    suspend fun ResendOTP(
        @Path("mobile")mobile : String,
    ): ResponseDTO

    //https://ashom.app/api/webservice/forgetpassword

    @FormUrlEncoded
    @POST("webservice/forgetpassword")
    suspend fun forgetpassword(
        @Field("email")email : String,
        ): ResponseDTO
}