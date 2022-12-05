package com.ashomapp.network.response.dashboard

import com.google.gson.annotations.SerializedName

data class VisiteddataDTO(
    @SerializedName("max_companies")
    val max_companies : String,

    @SerializedName("visited_companies")
    val visited_companies : String,

    @SerializedName("remaining_visits")
    val remaining_visits : String

)
