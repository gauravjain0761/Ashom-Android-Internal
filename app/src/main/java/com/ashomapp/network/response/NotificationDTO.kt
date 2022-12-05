package com.ashomapp.network.response

import com.ashomapp.network.response.dashboard.CompanyDTO
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import org.json.JSONObject

data class NotificationDTO(

        @SerializedName("message")
        val message : String,
        @SerializedName("metadata")
        val metadata : NotifiicationMetaData,

        @SerializedName("created_at")
        val created_at  : String
)
data class NotifiicationMetaData(
    @SerializedName("type")
    val type : String,
    @SerializedName("data")
    val data : JsonArray

)
data class NotifiicationFirebaseMetaData(
    @SerializedName("type")
    val type : String,
    @SerializedName("data")
    val data : JsonObject

)

data class NotificationData(
    @SerializedName("Company_payload")
     val companypayload : CompanyDTO,
    @SerializedName("year")
    val year : String,
    @SerializedName("period")
    val period : String,
)
data class  NOtificationDataNews(
    @SerializedName("id")
    val id : String,
    @SerializedName("title")
    val title : String,
    @SerializedName("date")
    val date : String,
    @SerializedName("image_url")
    val image_url : String,
    @SerializedName("source")
    val source : String,
    @SerializedName("link")
    val link : String,
    @SerializedName("m_countries")
    val m_countries : String,
    @SerializedName("m_companies")
    val m_companies : String,
    @SerializedName("created")
    val created : String
)
