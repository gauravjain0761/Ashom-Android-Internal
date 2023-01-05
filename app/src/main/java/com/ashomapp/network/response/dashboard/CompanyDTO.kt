package com.ashomapp.network.response.dashboard
import com.google.gson.annotations.SerializedName
data class CompanyDTO(

    @SerializedName("id")
    val id : String,

    @SerializedName("Company_Name")
    val Company_Name : String,


    @SerializedName("SymbolTicker")
    val SymbolTicker : String,

    @SerializedName("Country")
    val Country : String,


    @SerializedName("image")
    val image : String,

    @SerializedName("DelistingDate")
    val DelistingDate : String  ? = null,

    @SerializedName("company_status")
    val company_status : String ? = "",

     @SerializedName("lastReport")
     val lastReport : String ? = null,

    @SerializedName("exchanges")
    val exchanges : String ? = "",

    @SerializedName("Reference_No")
    val Reference_No : String ? = "",

    )
