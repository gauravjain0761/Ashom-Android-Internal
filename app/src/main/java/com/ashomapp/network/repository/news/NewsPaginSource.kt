package com.ashomapp.network.repository.news

import android.util.Log
import androidx.paging.PagingSource
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.network.services.HomeServices
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException

class NewsPaginSource(private val homeServices: HomeServices,
                      private  val country_name: String,
                      private val company_name : String ? = null,
                      private val search : String ? = null) : PagingSource<Int, NewsItemDTO>(){
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NewsItemDTO> {
        val position = params.key ?: 0
        return try {

            val products = if (company_name.isNullOrEmpty()){
                homeServices.getNews(page_num = position.toString(), country_name = country_name,search = search)
            }else{
                homeServices.getCompanyNews(page_num = position.toString(), country_name = country_name,company_name = company_name)
            }

            Log.d("PRODUCTLIST1", products.toString())
            LoadResult.Page(
                data = products.data,
                prevKey = if (position == 0) null else position - 1,
                nextKey = if (products.data.isEmpty()) null else position + 1
            )
        } catch (ex: JsonSyntaxException) {
           Log.d("Errorpaging", ex.localizedMessage)

            LoadResult.Error(ex)
        } catch (ex: HttpException) {
            if (ex.code() == 404) {

                LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (position == 0) null else position - 1,
                    nextKey = null
                )
            } else {
                LoadResult.Error(ex)
            }
        }catch (ex : UnknownHostException){
            Log.d("ERROR1", ex.localizedMessage)
            Log.d("Errorpaging", ex.localizedMessage)
            LoadResult.Error(ex)
        } catch (ex: Exception) {
            Log.d("ERROR1", ex.localizedMessage)

            LoadResult.Error(ex)
        }catch (ex : SocketTimeoutException){
            Log.d("ERROR1", ex.localizedMessage)

            LoadResult.Error(ex)
        }catch (ex : UnknownServiceException){
            Log.d("ERROR1", ex.localizedMessage)
            LoadResult.Error(ex)
        }catch (ex : IOException){
            Log.d("ERROR1", ex.localizedMessage)
            LoadResult.Error(ex)
        }
    }

}