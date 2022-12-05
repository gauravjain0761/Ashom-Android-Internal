package com.ashomapp.presentation.home.stockPrice.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class StockListModel : ArrayList<StockListModelItem>()

data class StockListModelItem(

    @SerializedName("Exchange Description")
    val Exchange_Description: String,
    val High: Double,
    val Identifier: String,
    val IdentifierType: String,
    val Industry: String?="",

    @SerializedName("Issuer Name")
    val Issuer_Name: String,

    val Last: Double,
    val Low: Double,
    val Open: Double,
    val RIC: String,

    @SerializedName("Reference Company")
    val Reference_Company: String,

    val SymbolTicker: String?="",

    @SerializedName("Trade Date")
    val Trade_Date: String,

    @SerializedName("Universal Close Price")
    val Universal_Close_Price: Double,

    val Volume: Int,
    val percentage_change: String,
    val price_difference: String
)