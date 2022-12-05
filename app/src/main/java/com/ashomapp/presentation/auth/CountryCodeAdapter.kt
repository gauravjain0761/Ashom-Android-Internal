package com.ashomapp.presentation.auth

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemCountryCodeListBinding
import com.ashomapp.network.response.Countrycodelist
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.bumptech.glide.Glide
import java.util.*
import kotlin.collections.ArrayList

class CountryCodeAdapter(
    val list: ArrayList<Countrycodelist>,
    val onCountryCodeClick: onCountryCodeClick
) : RecyclerView.Adapter<CountryCodeAdapter.CCViewHolder>(), Filterable {
    var countryFilterList = ArrayList<Countrycodelist>()

    init {
        countryFilterList = list
    }

    class CCViewHolder(val binding: ItemCountryCodeListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(countrycodelist: Countrycodelist, onCountryCodeClick: onCountryCodeClick) {
            binding.countryCodeCountryName.text = "${countrycodelist.country_name}"
            binding.countryCodeCode.text = "+${countrycodelist.country_code}"
            if (countrycodelist.country_name.equals("Bahamas")) {
                binding.countryCodeFlag.setImageResource(R.drawable.bahamas)
            } else if (countrycodelist.country_name.equals("British Indian Ocean Territory")) {
                binding.countryCodeFlag.setImageResource(R.drawable.british_indian_ocean_territory)
            } else if (countrycodelist.country_name.equals("Cape Verde")) {
                binding.countryCodeFlag.setImageResource(R.drawable.cape_verde)
            } else if (countrycodelist.country_name.equals("Cayman Islands")) {
                Glide.with(AshomAppApplication.instance.applicationContext)
                    .load("https://countryflagsapi.com/png/the Cayman Islands")
                    .into(binding.countryCodeFlag)
            } else if (countrycodelist.country_name.equals("Central African Republic")) {
                binding.countryCodeFlag.setImageResource(R.drawable.central_africal_republic)
            } else if (countrycodelist.country_name.equals("Cocos (Keeling) Islands")) {
                binding.countryCodeFlag.setImageResource(R.drawable.coco_island)
            } else if (countrycodelist.country_name.equals("Comoros")) {
                binding.countryCodeFlag.setImageResource(R.drawable.comoros)
            } else if (countrycodelist.country_name.equals("Congo, Democratic Republic of the")) {
                binding.countryCodeFlag.setImageResource(R.drawable.congo)
            } else if (countrycodelist.country_name.equals("Cook Islands")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_cook_islands)
            } else if (countrycodelist.country_name.equals("Curacao")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_curacao)
            } else if (countrycodelist.country_name.equals("Czech Republic")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_czech_republic)
            } else if (countrycodelist.country_name.equals("Falkland Islands (Malvinas)")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_falkland_islands)
            }//Falkland Islands (Malvinas)  Czech Republic
            else if (countrycodelist.country_name.equals("Faroe Islands")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_faroe_islands)
            }//Falkland Islands (Malvinas)
            else if (countrycodelist.country_name.equals("Holy See (Vatican City State)")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_vatican_city)
            }//Falkland Islands (Malvinas)
            else if (countrycodelist.country_name.equals("Macedonia")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_macedonia)
            }//Marshall Islands
            else if (countrycodelist.country_name.equals("Marshall Islands")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_marshall_islands)
            } else if (countrycodelist.country_name.equals("Netherlands Antilles")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_netherlands_antilles)
            } else if (countrycodelist.country_name.equals("Northern Mariana Islands")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_northern_mariana_islands)
            } else if (countrycodelist.country_name.equals("Palestinian Territory, Occupied")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_palestine)
            } else if (countrycodelist.country_name.equals("Saint Vincent and the Grenadines")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_saint_vicent_and_the_grenadines)
            }//Swaziland Turks and Caicos Islands
            else if (countrycodelist.country_name.equals("Swaziland")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_swaziland)
            } else if (countrycodelist.country_name.equals("Virgin Islands, US")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_us_virgin_islands)
            } else if (countrycodelist.country_name.equals("Turks and Caicos Islands")) {
                binding.countryCodeFlag.setImageResource(R.drawable.flag_turks_and_caicos_islands)
            } else {
                Glide.with(AshomAppApplication.instance.applicationContext)
                    .load("https://countryflagsapi.com/png/${countrycodelist.country_name}")
                    .into(binding.countryCodeFlag)
            }

            binding.root.setOnClickListener {
                onCountryCodeClick.onCountryCodeClick(countrycodelist)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CCViewHolder {
        val binding =
            ItemCountryCodeListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CCViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CCViewHolder, position: Int) {
        holder.bind(countryFilterList[position], onCountryCodeClick)
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
                    countryFilterList = list as ArrayList<Countrycodelist>
                } else {
                    val resultList = ArrayList<Countrycodelist>()
                    for (row in list) {
                        Log.d("filterdata1", charSearch.toString() + "$row")

                        if (row.country_name.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT)) ||
                            row.country_name.uppercase(Locale.ROOT)
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
                countryFilterList = results?.values as ArrayList<Countrycodelist>
                Log.d("filterdata1ds", "$countryFilterList")
                notifyDataSetChanged()
            }

        }
    }
}