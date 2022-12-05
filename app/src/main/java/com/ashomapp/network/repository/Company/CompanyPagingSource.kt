package com.ashomapp.network.repository.Company

import android.util.Log
import androidx.paging.PagingSource
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.network.services.HomeServices
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException

class CompanyPagingSource (private val homeServices: HomeServices,
private  val country_name: String,
private val search : String) : PagingSource<Int, CompanyDTO>(){
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CompanyDTO> {
        val position = params.key ?: 1
        return try {

            val products = homeServices.getCompany(page_num = position.toString(), country_name = country_name,search = search)

            Log.d("PRODUCTLIST1", products.toString())
            LoadResult.Page(
                data = products.data,
                prevKey = if (position == 1) null else position - 1,
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

            PagingSource.LoadResult.Error(ex)
        }catch (ex : SocketTimeoutException){
            Log.d("ERROR1", ex.localizedMessage)

            PagingSource.LoadResult.Error(ex)
        }catch (ex : UnknownServiceException){
            Log.d("ERROR1", ex.localizedMessage)
            PagingSource.LoadResult.Error(ex)
        }catch (ex : IOException){
            Log.d("ERROR1", ex.localizedMessage)
            PagingSource.LoadResult.Error(ex)
        }
    }

}