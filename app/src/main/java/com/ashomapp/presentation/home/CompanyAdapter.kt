package com.ashomapp.presentation.home


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemCompanyBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.bumptech.glide.Glide
import java.util.*
import kotlin.collections.ArrayList
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.AlphaAnimation





class CompanyAdapter(val list: List<CompanyDTO>, val onCompanyClick: onCompanyClick)
    :  RecyclerView.Adapter<CompanyAdapter.Companyviewholder>(), Filterable {
    var countryFilterList = ArrayList<CompanyDTO>()

    init {
        countryFilterList = list as ArrayList<CompanyDTO>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Companyviewholder {
        val binding = ItemCompanyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Companyviewholder(binding)
    }

    override fun onBindViewHolder(holder: Companyviewholder, position: Int) {
        holder.bind(countryFilterList[position],onCompanyClick)

    }

    override fun getItemCount(): Int {
        return countryFilterList.size
    }

    class Companyviewholder(val binding : ItemCompanyBinding):RecyclerView.ViewHolder(binding.root) {
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

    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                Log.d("filterdata", charSearch.toString())
                if (charSearch.isEmpty()) {
                    countryFilterList = list as ArrayList<CompanyDTO>
                } else {
                    val resultList = ArrayList<CompanyDTO>()
                    for (row in list) {
                        Log.d("filterdata1", charSearch.toString() + "$row")

                        if (row.Country.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))||
                            row.Company_Name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))||
                            row.SymbolTicker.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(row)
                            Log.d("filterdata132", charSearch.toString())

                        }
                    }
                    countryFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = countryFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                countryFilterList = results?.values as ArrayList<CompanyDTO>
                Log.d("filterdata1ds",  "$countryFilterList")
                notifyDataSetChanged()
            }

        }
    }
}
interface onCompanyClick{
    fun onCompanyItemClick(companyDTO: CompanyDTO)
}