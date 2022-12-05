package com.ashomapp.utils

import android.content.Context
import android.content.SharedPreferences


class SessionManager(mcxt: Context) {

    companion object {
        val PREF_NAME = "Ashom"
        val PREF_GENERAL = "PREF_GENERAL"
        const val KEY_STOCK_LIST = "stockList"

    }


    var generalEditor: SharedPreferences.Editor
    var generalPref: SharedPreferences

    private var PRIVATE_MODE = 0

    init {


        generalPref = mcxt.getSharedPreferences(PREF_GENERAL, PRIVATE_MODE)
        generalEditor = generalPref.edit()

    }


    var stockListLocal: String
        get() = generalPref.getString(KEY_STOCK_LIST, "").toString()
        set(stockList) {
            generalEditor.putString(KEY_STOCK_LIST, stockList)
            generalEditor.commit()
        }



}