package com.ashomapp.network.response.dashboard

import com.google.gson.annotations.SerializedName

data class RemainingCompanyDTO(
    @SerializedName("status")
    val status : Boolean,
    @SerializedName("message")
    val message : String,
    @SerializedName("last_report")
    val last_report : String,
    @SerializedName("visit_data")
    val visit_data : VisiteddataDTO,
    @SerializedName("visited_companies")
    val visited_companies : List<String>
)
