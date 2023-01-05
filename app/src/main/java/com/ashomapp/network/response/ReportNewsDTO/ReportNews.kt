package com.ashomapp.network.response.ReportNewsDTO

data class ReportNews(
    val company: List<String>,
    val country: List<String>,
    val created: String,
    val created_date: String,
    val date: String,
    val image_url: String,
    val link: String,
    val metadata: String,
    val source: String,
    val title: String,
    val type: String
)