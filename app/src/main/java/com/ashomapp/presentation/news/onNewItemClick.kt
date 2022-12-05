package com.ashomapp.presentation.news

import com.ashomapp.network.response.dashboard.NewsItemDTO

interface onNewItemClick {
    fun onNavigatetoDetail(newsItemDTO: NewsItemDTO, position: Int)
    fun onShare(newsItemDTO: NewsItemDTO, position: Int)
    fun addToForum(newsItemDTO: NewsItemDTO, position: Int)
}