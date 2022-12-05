package com.ashomapp.network.response.dashboard

import com.google.gson.annotations.SerializedName

data class CountriesDTO(
    @SerializedName("country")
    val country : String
)
