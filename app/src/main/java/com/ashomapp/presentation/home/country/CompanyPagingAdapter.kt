package com.ashomapp.presentation.home.country

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.presentation.home.CompanyPaginVIewHolder
import com.ashomapp.presentation.home.onCompanyClick
import com.ashomapp.presentation.news.NewsViewHolder
import com.ashomapp.presentation.news.onNewItemClick

class CompanyPagingAdapter(private val onNewItemClick: onCompanyClick) :
    PagingDataAdapter<CompanyDTO, CompanyPaginVIewHolder>(itemCallback) {
    companion object {
        val itemCallback = object : DiffUtil.ItemCallback<CompanyDTO>() {
            override fun areItemsTheSame(oldDto: CompanyDTO, newDto: CompanyDTO): Boolean {
                return oldDto == newDto
            }

            override fun areContentsTheSame(oldDto: CompanyDTO, newDto: CompanyDTO): Boolean {
                return oldDto.id == newDto.id
            }
        }
    }

    override fun onBindViewHolder(holder: CompanyPaginVIewHolder, position: Int) {
        val news = getItem(position) ?: return
        holder.bind(news, onNewItemClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyPaginVIewHolder {
        return CompanyPaginVIewHolder.create(parent)
    }
}