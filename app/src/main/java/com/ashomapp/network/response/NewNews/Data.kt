package com.ashomapp.network.response.NewNews


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("Company_payload")
    var companyPayload: CompanyPayload,
    @SerializedName("period")
    var period: String,
    @SerializedName("year")
    var year: String
)