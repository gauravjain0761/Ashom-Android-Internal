package com.ashomapp.network.response.dashboard

import com.google.gson.annotations.SerializedName

data class CompanyStatementsDTO(
    @SerializedName("document_name")
    val document_name : String,

    @SerializedName("document_link")
    val document_link : String,

    val document_infor : String ? = null
)