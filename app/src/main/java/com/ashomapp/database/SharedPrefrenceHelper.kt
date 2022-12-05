package com.ashomapp.database

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.ashomapp.AshomAppApplication
import com.ashomapp.network.response.auth.UserDTO
import com.ashomapp.network.response.dashboard.LoginData
import com.google.gson.Gson
import java.lang.Exception

object SharedPrefrenceHelper {

    private const val TOKEN = "TOKEN"
    private const val DEVICE_TOKEN = "DEVICE_TOKEN"
    private val sharedPrefs: SharedPreferences
    private const val USER = "USER"
    private const val LOGIN_INFO = "LOGIN_INFO"
    private const val BADGES_COUNT = "BADGES_COUNT"
    private const val LAST_COMPANY_ID = "LAST_COMPANY_ID"
    init {
        val context = AshomAppApplication.instance
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    var token : String?
        get() {
            return sharedPrefs.getString(TOKEN, "0")
        }
        set(value) {
            sharedPrefs.edit{
                putString(TOKEN,value)
            }
        }
    var notification_badges : Int?
        get() {
            return sharedPrefs.getInt(BADGES_COUNT, 0)
        }
        set(value) {
            sharedPrefs.edit{
                putInt(BADGES_COUNT,value!!)
            }
        }
    var last_company_id : String?
        get() {
            return sharedPrefs.getString(LAST_COMPANY_ID, "0")
        }
        set(value) {
            sharedPrefs.edit{
                putString(LAST_COMPANY_ID,value!!)
            }
        }
    var devicetoken : String?
        get() {
            return sharedPrefs.getString(DEVICE_TOKEN, "0")
        }
        set(value) {
            sharedPrefs.edit{
                putString(DEVICE_TOKEN,value)
            }
        }

    var loginINFO: LoginData
        set(value) = sharedPrefs.edit {
            val userJson = Gson().toJson(value)
            putString(LOGIN_INFO, userJson)

            Log.d("Users_", userJson.toString())
        }
        get() {
            val userString = sharedPrefs.getString(LOGIN_INFO, null) ?: throw Exception("User not Found")
            Log.d("Users_", userString)



            return  Gson().fromJson(userString, LoginData::class.java)
        }

    var user: UserDTO
        set(value) = sharedPrefs.edit {
            val userJson = Gson().toJson(value)
            putString(USER, userJson)
            Log.d("Users_", userJson.toString())
        }
        get() {
            val userString = sharedPrefs.getString(USER, null) ?: throw Exception("User not Found")
            Log.d("Users_", userString)



            return  Gson().fromJson(userString, UserDTO::class.java)
        }
}
