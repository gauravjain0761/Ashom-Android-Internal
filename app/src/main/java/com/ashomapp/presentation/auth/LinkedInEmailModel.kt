package com.ashomapp.presentation.auth

import com.google.gson.annotations.SerializedName

data class LinkedInEmailModel(
    @SerializedName("elements")
    val elements: List<EmailHandle>
)

data class EmailHandle(
    @SerializedName("handle~")
    val handle: LinkedInEmail
)

data class LinkedInEmail(

    @SerializedName("emailAddress")
    val emailAddress: String
)