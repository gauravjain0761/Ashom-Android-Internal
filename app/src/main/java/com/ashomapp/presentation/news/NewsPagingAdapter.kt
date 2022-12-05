package com.ashomapp.presentation.news

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ashomapp.network.response.dashboard.NewsItemDTO

class NewsPagingAdapter(
        private val onNewItemClick: onNewItemClick
) : PagingDataAdapter<NewsItemDTO, NewsViewHolder>(itemCallback) {
    companion object {
        val itemCallback = object : DiffUtil.ItemCallback<NewsItemDTO>() {
            override fun areItemsTheSame(oldDto: NewsItemDTO, newDto: NewsItemDTO): Boolean {
                return oldDto == newDto
            }

            override fun areContentsTheSame(oldDto: NewsItemDTO, newDto: NewsItemDTO): Boolean {
                return oldDto.title == newDto.title
            }
        }
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
             val news =getItem(position) ?:return
        holder.bind(news, onNewItemClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
           return NewsViewHolder.create(parent)
    }
}