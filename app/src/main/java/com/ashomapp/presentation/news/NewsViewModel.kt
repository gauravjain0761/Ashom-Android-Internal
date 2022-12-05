package com.ashomapp.presentation.news

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dbvertex.myashomapp.network.repository.HomeRepository

class NewsViewModel : ViewModel() {

 fun getNewsItem(country_name : String, search : String ? = null) = HomeRepository.getNews(country_name = country_name, search = search).cachedIn(viewModelScope)

 fun getCompanyNews(country_name: String, company_name :String) = HomeRepository
  .getCompanyNews(country_name, company_name).cachedIn(viewModelScope)
}