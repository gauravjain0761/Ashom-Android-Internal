package com.ashomapp.presentation.home.stockPrice.model

class StockHistoryModel : ArrayList<StockHistoryModelItem>()

data class StockHistoryModelItem(
    val Alias_Underlying_RIC: String,
    val Close_Ask: String,
    val Close_Bid: String,
    val Date_Time: String,
    val Domain: String,
    val GMT_Offset: String,
    val High: String,
    val High_Ask: String,
    val High_Bid: String,
    val Last_price: String,
    val Low: String,
    val Low_Ask: String,
    val Low_Bid: String,
    val No_Asks: String,
    val No_Bids: String,
    val No_Trades: String,
    val Open_Ask: String,
    val Open_Bid: String,
    val Open_price: String,
    val RIC: String,
    val Type_of: String,
    val Volume: String,
    val percentage_change_previous_entry: String,
    val price_difference_previous_entry: String
)