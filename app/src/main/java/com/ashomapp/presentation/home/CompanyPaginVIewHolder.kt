package com.ashomapp.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemCompanyBinding
import com.ashomapp.databinding.ItemNewsBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.presentation.news.NewsViewHolder
import com.bumptech.glide.Glide

class CompanyPaginVIewHolder(val binding : ItemCompanyBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(companyDTO: CompanyDTO, onCompanyClick: onCompanyClick) {

        Glide.with(AshomAppApplication.instance.applicationContext)
            .load(companyDTO.image).placeholder(R.drawable.placeholder)
            .into(binding.itemCompanyImg)
        binding.itemCompanyName.text = "${companyDTO.Company_Name}"
        binding.itemCompanySymbolTicker.text ="${companyDTO.SymbolTicker}"
        binding.itemCompanyImg.setOnClickListener {
            onCompanyClick.onCompanyItemClick(companyDTO)
        }

    }
    companion object {
        fun create(parent: ViewGroup): CompanyPaginVIewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemCompanyBinding.inflate(layoutInflater, parent, false)
            return CompanyPaginVIewHolder(binding)
        }
    }

}