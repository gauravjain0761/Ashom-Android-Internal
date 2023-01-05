package com.ashomapp.network.response.NewNews


import com.google.gson.annotations.SerializedName

data class componyMetaDataDTO(
    @SerializedName("data")
    var data: List<Data>,
    @SerializedName("type")
    var type: String
)