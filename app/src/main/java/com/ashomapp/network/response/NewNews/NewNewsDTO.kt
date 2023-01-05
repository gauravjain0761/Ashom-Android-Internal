package com.ashomapp.network.response.NewNews


import com.google.gson.annotations.SerializedName

data class NewNewsDTO(
    @SerializedName("company")
    var company: List<String>,
    @SerializedName("country")
    var country: List<String>,
    @SerializedName("created")
    var created: String,
    @SerializedName("created_date")
    var createdDate: String,
    @SerializedName("date")
    var date: String,
    @SerializedName("image_url")
    var imageUrl: String,
    @SerializedName("link")
    var link: String,
    @SerializedName("metadata")
    var metadata: String,
    @SerializedName("source")
    var source: String,
    @SerializedName("title")
    var title: String
)