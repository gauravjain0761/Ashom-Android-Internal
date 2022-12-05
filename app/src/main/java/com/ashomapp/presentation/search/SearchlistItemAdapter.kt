package com.ashomapp.presentation.search

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemSearchListBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.ashomapp.presentation.home.onCompanyClick
import com.ashomapp.utils.hideKeyboard
import com.bumptech.glide.Glide
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class SearchlistItemAdapter(val list : List<CompanyDTO>,val onCompanyClick: onCompanyClick):
    RecyclerView.Adapter<SearchlistItemAdapter.ViewHolder>(), Filterable {
    var companyFilterList = ArrayList<CompanyDTO>()

    init {
        Log.d("filterdatatop", list.toString())
        companyFilterList = list as ArrayList<CompanyDTO>
    }
    class ViewHolder(val binding : ItemSearchListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bindview(searachDTO: CompanyDTO, onCompanyClick: onCompanyClick) {
                         //3 for image , 4 for companyname //0 for country name identifier
            if (searachDTO.Reference_No.equals("0")){
                binding.itemCountryName.text = "${searachDTO.Country} - ${searachDTO.SymbolTicker} | ${searachDTO.Company_Name}"

            }else{
                binding.itemCountryName.text = "${searachDTO.Country} - ${searachDTO.Reference_No} - ${searachDTO.SymbolTicker} | ${searachDTO.Company_Name}"

            }

            try {
                if (searachDTO.Country .equals("KSA")){
                    binding.itemCountIv.setImageResource(R.drawable.ksaflag)
                }else if (searachDTO.Country.equals("UAE")){
                    binding.itemCountIv.setImageResource(R.drawable.uae_flag)
                }else if (searachDTO.Country.equals("All")){
                    binding.itemCountIv.setImageResource(R.drawable.all_countries)
                }
                else{
                    Glide.with(AshomAppApplication.instance.applicationContext)
                        .load("https://countryflagsapi.com/png/${searachDTO.Country}")
                        .into(binding.itemCountIv)
                }
            }catch (e : Exception){
                Log.d("exception_country", e.localizedMessage)
            }

            binding.root.setOnClickListener {

                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                onCompanyClick.onCompanyItemClick(companyDTO = searachDTO)

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindview(companyFilterList[position], onCompanyClick)
    }

    override fun getItemCount(): Int {
        return  companyFilterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                Log.d("filterdata", charSearch.toString())
                if (charSearch.isEmpty()) {
                    companyFilterList = list as ArrayList<CompanyDTO>
                } else {
                    Log.d("filterdata", list.toString())

                    val resultList = ArrayList<CompanyDTO>()
                    for (row in list) {
                        Log.d("filterdata1", charSearch.toString() + "$row")

                        if (row.Country.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))||
                            row.Company_Name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))||
                            row.SymbolTicker.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))||
                            row.Reference_No!!.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))
                            ) {
                            resultList.add(row)
                            Log.d("filterdata132", charSearch.toString())

                        }
                    }
                    companyFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = companyFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                companyFilterList = results?.values as ArrayList<CompanyDTO>
                Log.d("filterdata1ds",  "$companyFilterList")
                notifyDataSetChanged()
            }

        }
    }


}
