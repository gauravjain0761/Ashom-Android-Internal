package com.ashomapp.network.response.dashboard

import com.google.gson.annotations.SerializedName

data class NewsDTO (
    @SerializedName("data")
         val data : List<NewsItemDTO>
        )
data class NewsItemDTO(
    @SerializedName("title")
    val title  : String,
    @SerializedName("date")
    val date  : String ?= null,
    @SerializedName("image_url")
    val image_url  : String,
    @SerializedName("source")
    val source  : String?=null,
    @SerializedName("link")
    val link  : String ,
    @SerializedName("metadata")
    var metadata: String,
    @SerializedName("created")
    val created  : String ?= null,
    @SerializedName("country")
    val country  : List<String>?= null,
    @SerializedName("company")
    val company  : List<String>?= null,
    @SerializedName("created_date")
    val created_date  : String?= null








///created_date

)