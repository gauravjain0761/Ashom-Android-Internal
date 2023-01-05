package com.ashomapp.presentation.home

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemCountryBinding
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.bumptech.glide.Glide
import java.lang.Exception

class CountryAdapter(val list : List<CountriesDTO>, val onCountryClick: onCountryClick) :
RecyclerView.Adapter<CountryAdapter.ContryViewModel>(){

    class ContryViewModel(val mBinding : ItemCountryBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun ItemBind(countriesDTO: CountriesDTO, onCountryClick: onCountryClick) {
            mBinding.itemCountryName.text = "${countriesDTO.country}"
           try {
               if (countriesDTO.country.equals("KSA")){
                   mBinding.itemCountIv.setImageResource(R.drawable.ksaflag)
               }else if (countriesDTO.country.equals("UAE")){
                   mBinding.itemCountIv.setImageResource(R.drawable.uae_flag)
               }else if (countriesDTO.country.equals("All Countries")){
                   mBinding.itemCountIv.setImageResource(R.drawable.all_countries)
               }
               else{
               Glide.with(AshomAppApplication.instance.applicationContext)
                   .load("https://countryflagsapi.com/png/${countriesDTO.country}")
                   .into(mBinding.itemCountIv)
               }
           }catch (e : Exception){
               Log.d("exception_country", e.localizedMessage)
           }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContryViewModel {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent ,false)
        return  ContryViewModel(binding)
    }

    override fun onBindViewHolder(holder: ContryViewModel, position: Int) {
        holder.ItemBind(list[position], onCountryClick)
        holder.mBinding.itemCountryContainer.getBackground().setColorFilter(null);

        if (Company_frag.selectedItem ==position){
            holder.mBinding.itemCountryContainer.getBackground().setColorFilter(Color.parseColor("#72c4f1"), PorterDuff.Mode.SRC_ATOP);
        }
        holder.mBinding.itemCountryContainer.setOnClickListener {
            val previousItem = Company_frag.selectedItem
            Company_frag.selectedItem = position
            notifyItemChanged(previousItem)
            notifyItemChanged(position)
            onCountryClick.onCountryClick(list[position])
        }

    }

    override fun getItemCount(): Int {
        return  list.size
    }
}

interface  onCountryClick{
    fun onCountryClick(countriesDTO: CountriesDTO)
}