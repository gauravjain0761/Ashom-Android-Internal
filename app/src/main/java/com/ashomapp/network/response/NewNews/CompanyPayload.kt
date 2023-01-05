package com.ashomapp.network.response.NewNews


import com.google.gson.annotations.SerializedName

data class CompanyPayload(
    @SerializedName("Company_Name")
    var companyName: String,
    @SerializedName("company_status")
    var companyStatus: String,
    @SerializedName("Country")
    var country: String,
    @SerializedName("DelistingDate")
    var delistingDate: String,
    @SerializedName("exchanges")
    var exchanges: String,
    @SerializedName("id")
    var id: String,
    @SerializedName("image")
    var image: String,
    @SerializedName("industry")
    var industry: String,
    @SerializedName("Reference_No")
    var referenceNo: String,
    @SerializedName("SymbolTicker")
    var symbolTicker: String
)