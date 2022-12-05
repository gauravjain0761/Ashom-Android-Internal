package com.ashomapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.utils.*
import java.lang.Exception
import java.util.concurrent.Executor

class AshomDBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY, " +
                COMPANY_ID + " TEXT, " +
                COMPANY_NAME + " TEXT," +
                SYMBOLTICKER + " TEXT," +
                COUNTRY + " TEXT," +
                COMPANY_IMAGE + " TEXT," +
                DELISTINGDATE + " TEXT," +
                COMPANY_STATUS + " TEXT," +
                COMPANY_EXCHANGE + " TEXT" + ")")

        db.execSQL(query)
    }

    fun deleteCompany() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM " + TABLE_NAME)
        db.close()
    }

    fun addCompany(companyDTO: CompanyDTO) {
        val values = ContentValues()
        values.put(COMPANY_ID, companyDTO.id)
        values.put(COMPANY_NAME, companyDTO.Company_Name)
        values.put(SYMBOLTICKER, companyDTO.SymbolTicker)
        values.put(COUNTRY, companyDTO.Country)
        values.put(COMPANY_IMAGE, companyDTO.image)
        values.put(DELISTINGDATE, companyDTO.DelistingDate)
        values.put(COMPANY_STATUS, companyDTO.company_status)
        values.put(COMPANY_EXCHANGE, companyDTO.exchanges)


        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()

    }

    fun getSearchCompanyList(list: List<String>): List<CompanyDTO> {
        val db = this.readableDatabase
        val companyArrayList: ArrayList<CompanyDTO> = ArrayList()
        var cursorCourses: Cursor? = null
        try {
            companyArrayList.clear()
            list.forEach {
                val adas = it.split("✂").toTypedArray()
              if (  adas[0].equals("CompanyName")){
                  cursorCourses = db.rawQuery(
                      "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID + "="
                              + adas[5], null
                  )
                  if (cursorCourses!!.moveToFirst()) {

                      do {
                          companyArrayList.add(
                              CompanyDTO(
                                  cursorCourses!!.getString(1),
                                  cursorCourses!!.getString(2),
                                  cursorCourses!!.getString(3),
                                  cursorCourses!!.getString(4),
                                  cursorCourses!!.getString(5),
                                  cursorCourses!!.getString(6),
                                  cursorCourses!!.getString(7)
                              )
                          )

                      } while (cursorCourses!!.moveToNext())
                  }

              }

            }
            cursorCourses!!.close()
            return companyArrayList
        } catch (e: Exception) {
        }
        return companyArrayList
    }

    fun getSelectedCompany(list: List<String>): List<String> {
        var selecteddata = ArrayList<String>()
        var cursor: Cursor? = null
        val db = this.readableDatabase
        try {
            list.forEach {
                cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID + "="
                            + it, null
                )
                if (cursor!!.moveToFirst()) {
                    do {
                        if (!selecteddata.contains(cursor!!.getString(2))) {
                            selecteddata.add(cursor!!.getString(2))
                        }
                    } while (cursor!!.moveToNext())
                }
            }
            cursor!!.close()
            return selecteddata
        } catch (e: Exception) {

        }
        return selecteddata
    }

    fun getSearchCompanyData(query: String, query2: String): List<CompanyDTO> {
        val db = this.readableDatabase
        val companyArrayList: ArrayList<CompanyDTO> = ArrayList()
        companyArrayList.clear()
        try {

            val cursorCourses = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_NAME + " like '%"
                        + query + "%' or " + COMPANY_NAME + " like '%"
                        + query2 + "%' or " + SYMBOLTICKER + " like '%"
                        + query + "%' or " + COUNTRY + " like '%"
                        + query + "%'", null
            )

            if (cursorCourses.moveToFirst()) {
                do {
                    companyArrayList.add(
                        CompanyDTO(
                            cursorCourses.getString(1),
                            cursorCourses.getString(2),
                            cursorCourses.getString(3),
                            cursorCourses.getString(4),
                            cursorCourses.getString(5),
                            cursorCourses.getString(6),
                        )
                    )
                } while (cursorCourses.moveToNext())
            }
            cursorCourses.close()
            return companyArrayList
        } catch (e: Exception) {
        }
        return companyArrayList
    }

    fun getSearchCompanyByCountryData(query: String): List<CompanyDTO> {
        val db = this.readableDatabase
        val companyArrayList: ArrayList<CompanyDTO> = ArrayList()
        companyArrayList.clear()
        try {

            val cursorCourses = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COMPANY_NAME=$query", null)

            if (cursorCourses.moveToFirst()) {
                do {
                    companyArrayList.add(
                        CompanyDTO(
                            cursorCourses.getString(1),
                            cursorCourses.getString(2),
                            cursorCourses.getString(3),
                            cursorCourses.getString(4),
                            cursorCourses.getString(5),
                            cursorCourses.getString(6),
                        )
                    )
                } while (cursorCourses.moveToNext())
            }
            cursorCourses.close()
            return companyArrayList
        } catch (e: Exception) {
        }
        return companyArrayList
    }

    fun getSingleCompany(query: String): CompanyDTO? {
        val db = this.readableDatabase
        var companyDTO : CompanyDTO? =  null
        try {

            val cursorCourses = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COMPANY_ID + "=" + query, null
            )

            if (cursorCourses.moveToFirst()) {
                do {
                 companyDTO = CompanyDTO(
                     cursorCourses.getString(1),
                     cursorCourses.getString(2),
                     cursorCourses.getString(3),
                     cursorCourses.getString(4),
                     cursorCourses.getString(5),
                     cursorCourses!!.getString(6),
                     cursorCourses!!.getString(7)
                    )
                } while (cursorCourses.moveToNext())
            }
            cursorCourses.close()
            return companyDTO
        } catch (e: Exception) {
        }
        return companyDTO
    }
    fun getSingleCompanyByName(query: String): CompanyDTO? {
        val db = this.readableDatabase
        var companyDTO : CompanyDTO? =  null
        try {

            val cursorCourses = db.rawQuery(
                    "SELECT * FROM $TABLE_NAME WHERE ${COMPANY_NAME.lowercase()} LIKE \"%$query%\" or ${SYMBOLTICKER.lowercase()} = \"%$query%\"", null
            )



            if (cursorCourses.moveToFirst()) {
                do {
                 companyDTO = CompanyDTO(
                     cursorCourses.getString(1),
                     cursorCourses.getString(2),
                     cursorCourses.getString(3),
                     cursorCourses.getString(4),
                     cursorCourses.getString(5),
                     cursorCourses!!.getString(6),
                     cursorCourses!!.getString(7)
                    )
                } while (cursorCourses.moveToNext())
            }
            cursorCourses.close()
            return companyDTO
        } catch (e: Exception) {
        }
        return companyDTO
    }

    fun getSingleCompanyBySymbol(query: String): CompanyDTO? {
        val db = this.readableDatabase
        var companyDTO : CompanyDTO? =  null
        try {

            val cursorCourses = db.rawQuery(
                    "SELECT * FROM $TABLE_NAME WHERE  ${SYMBOLTICKER.lowercase()} = $query", null
            )



            if (cursorCourses.moveToFirst()) {
                do {
                 companyDTO = CompanyDTO(
                     cursorCourses.getString(1),
                     cursorCourses.getString(2),
                     cursorCourses.getString(3),
                     cursorCourses.getString(4),
                     cursorCourses.getString(5),
                     cursorCourses!!.getString(6),
                     cursorCourses!!.getString(7)
                    )
                } while (cursorCourses.moveToNext())
            }
            cursorCourses.close()
            return companyDTO
        } catch (e: Exception) {
        }
        return companyDTO
    }

    fun getCompanyDataFromCOuntry(query: String): List<CompanyDTO> {
        val db = this.readableDatabase
        val companyArrayList: ArrayList<CompanyDTO> = ArrayList()
        companyArrayList.clear()
        try {
            val cursorCourses = db.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE $COUNTRY like '%$query%' or $COMPANY_EXCHANGE like '%$query%'", null
            )
            if (cursorCourses.moveToFirst()) {
                do {
                    companyArrayList.add(
                        CompanyDTO(
                            cursorCourses.getString(1),
                            cursorCourses.getString(2),
                            cursorCourses.getString(3),
                            cursorCourses.getString(4),
                            cursorCourses.getString(5),
                            cursorCourses!!.getString(6),
                            cursorCourses.getString(7)
                        )
                    )
                    Log.d("Companydbitemsddii", "${cursorCourses.getString(7)}")
                } while (cursorCourses.moveToNext())
            }
            cursorCourses.close()
            return companyArrayList
        } catch (e: Exception) {
        }
        return companyArrayList
    }
    fun getCompanyDataFromCountryList(query: String): List<CompanyDTO> {
        val db = this.readableDatabase
        val companyArrayList: ArrayList<CompanyDTO> = ArrayList()
        companyArrayList.clear()
        try {
            val cursorCourses = db.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE ${COUNTRY.lowercase()} = ${query.lowercase()}", null
            )
            if (cursorCourses.moveToFirst()) {
                do {
                    companyArrayList.add(
                        CompanyDTO(
                            cursorCourses.getString(1),
                            cursorCourses.getString(2),
                            cursorCourses.getString(3),
                            cursorCourses.getString(4),
                            cursorCourses.getString(5),
                            cursorCourses!!.getString(6),
                            cursorCourses!!.getString(7)
                        )
                    )
                    Log.d("Companydbitemsddii", "${cursorCourses.getString(7)}")
                } while (cursorCourses.moveToNext())
            }
            cursorCourses.close()
            return companyArrayList
        } catch (e: Exception) {
        }
        return companyArrayList
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

}