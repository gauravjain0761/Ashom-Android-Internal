package com.ashomapp.presentation.home.country

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemCountryBinding
import com.ashomapp.databinding.ItemCountryListBinding
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class CountryGridAdapter(
    private var list: List<CountriesDTO>,
    val onCountryClick: onCountryGridClick
) :
    RecyclerView.Adapter<CountryGridAdapter.ContryViewModel>(), Filterable {
    var countryFilterList = ArrayList<CountriesDTO>()

    init {
        countryFilterList = list as ArrayList<CountriesDTO>
    }

    class ContryViewModel(val mBinding: ItemCountryListBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        fun ItemBind(countriesDTO: CountriesDTO, onCountryClick: onCountryGridClick) {
            mBinding.itemCountryListName.text = "${countriesDTO.country}"
            mBinding.itemCountryListMain.setOnClickListener {
                onCountryClick.onCountryClick(countriesDTO)
            }
            try {
                if (countriesDTO.country.equals("KSA")) {
                    mBinding.countryFlag.setImageResource(R.drawable.ksaflag)
                } else if (countriesDTO.country.equals("UAE")) {
                    mBinding.countryFlag.setImageResource(R.drawable.uae_flag)
                } else if (countriesDTO.country.equals("All Countries")) {
                    mBinding.countryFlag.setImageResource(R.drawable.all_countries)
                    mBinding.root.visibility = View.GONE
                } else {
                    Glide.with(AshomAppApplication.instance.applicationContext)
                        .load("https://countryflagsapi.com/png/${countriesDTO.country}")
                        .placeholder(R.drawable.placeholder)
                        .into(mBinding.countryFlag)
                }

            } catch (e: Exception) {
                Log.d("exception_country", e.localizedMessage)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContryViewModel {
        val binding =
            ItemCountryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContryViewModel(binding)
    }

    override fun onBindViewHolder(holder: ContryViewModel, position: Int) {
        holder.ItemBind(countryFilterList[position], onCountryClick)


    }

    override fun getItemCount(): Int {
        return countryFilterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                Log.d("filterdata", charSearch.toString())
                if (charSearch.isEmpty()) {
                    countryFilterList = list as ArrayList<CountriesDTO>
                } else {
                    val resultList = ArrayList<CountriesDTO>()
                    for (row in list) {
                        Log.d("filterdata1", charSearch.toString() + "$row")

                        if (row.country.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT)) ||
                            row.country.uppercase(Locale.ROOT)
                                .contains(charSearch.uppercase(Locale.ROOT))
                        ) {
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
                countryFilterList = results?.values as ArrayList<CountriesDTO>
                Log.d("filterdata1ds", "$countryFilterList")
                notifyDataSetChanged()
            }

        }
    }


}

interface onCountryGridClick {
    fun onCountryClick(countriesDTO: CountriesDTO)
}