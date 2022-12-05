package com.ashomapp.presentation.auth

import com.google.gson.annotations.SerializedName

data class LinkedInProfileModel(
    @SerializedName("firstName")
    val firstname: Firstname,

    @SerializedName("lastName")
    val lastname: Firstname,

    @SerializedName("id")
    val id: String
)

data class Firstname(
    @SerializedName("localized")
    val localized: Localised
)

data class Localised(

    @SerializedName("en_US")
    val enUS: String
)