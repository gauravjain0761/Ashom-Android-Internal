package com.ashomapp.presentation.stockDetails.model

class CountryModel : ArrayList<CountryModelItem>()

data class CountryModelItem(
    val country: String,
    val selected: Boolean = false

)