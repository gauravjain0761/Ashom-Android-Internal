package com.ashomapp.presentation.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.databinding.ItemCustomYearTextBinding
import com.ashomapp.network.response.dashboard.YearsDTO

class AdaterYear(val list: List<YearsDTO>, val onYearClick: onYearClick) : RecyclerView.Adapter<AdaterYear.ViewHolder>() {
    class ViewHolder(val binding : ItemCustomYearTextBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(yearsDTO: YearsDTO, onYearClick: onYearClick) {
                  this.binding.year.text = "${yearsDTO.year}"
            Log.d("Value", yearsDTO.toString())
                   this.binding.year.setOnClickListener {
                       onYearClick.onYearItemClick(yearsDTO)
                   }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding  = ItemCustomYearTextBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(list[position], onYearClick)
    }

    override fun getItemCount(): Int {
        return  list.size
    }
}

interface  onYearClick{
   fun  onYearItemClick(yearsDTO: YearsDTO)
}