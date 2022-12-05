package com.ashomapp.network.response.dashboard

import com.google.gson.annotations.SerializedName

data class CompanyRes(
    @SerializedName("data")
    val data : List<CompanyDTO>
)
