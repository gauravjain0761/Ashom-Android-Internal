package com.ashomapp.presentation.home

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemCountryBinding
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.bumptech.glide.Glide
import java.lang.Exception

class CountryAdapter(val list: List<CountriesDTO>, private val onCountryClick: onCountryClick) :
    RecyclerView.Adapter<CountryAdapter.ContryViewModel>() {

    private lateinit var context:Context
    private var uaeClicked: Boolean = false
    private var showSubCountries: Boolean = false

    class ContryViewModel(val mBinding: ItemCountryBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        fun ItemBind(countriesDTO: CountriesDTO, onCountryClick: onCountryClick) {
            mBinding.itemCountryName.text = "${countriesDTO.country}"
            try {
                if (countriesDTO.country.equals("KSA")) {
                    mBinding.itemCountIv.setImageResource(R.drawable.ksaflag)
                } else if (countriesDTO.country.equals("UAE")) {
                    mBinding.itemCountIv.setImageResource(R.drawable.uae_flag)
                } else if (countriesDTO.country.equals("All Countries")) {
                    mBinding.itemCountIv.setImageResource(R.drawable.all_countries)
                } else {
                    Glide.with(AshomAppApplication.instance.applicationContext)
                        .load("https://countryflagsapi.com/png/${countriesDTO.country}")
                        .into(mBinding.itemCountIv)
                }


            } catch (e: Exception) {
                Log.d("exception_country", e.localizedMessage)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContryViewModel {
        context = parent.context
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContryViewModel(binding)
    }

    private var subCountryClickListener: ((name: String) -> Unit)? = null

    override fun onBindViewHolder(holder: ContryViewModel, position: Int) {
        holder.ItemBind(list[position], onCountryClick)
        holder.mBinding.itemCountryContainer.background.colorFilter = null

        if (Company_frag.selectedItem == position) {
            holder.mBinding.itemCountryContainer.background.setColorFilter(Color.parseColor("#72c4f1"),
                PorterDuff.Mode.SRC_ATOP);
        }
        if (list[position].country == "UAE") {
            holder.mBinding.uaeSubCountry.isVisible = uaeClicked
        } else {
            holder.mBinding.uaeSubCountry.isVisible = false
        }

        holder.mBinding.dfm.background = ContextCompat.getDrawable(context,R.drawable.flag_bg)
        holder.mBinding.adx.background = ContextCompat.getDrawable(context,R.drawable.flag_bg)

        holder.mBinding.dfm.setOnClickListener {
            holder.mBinding.adx.background = ContextCompat.getDrawable(context,R.drawable.flag_bg)
            holder.mBinding.dfm.background.setColorFilter(Color.parseColor("#72c4f1"),PorterDuff.Mode.SRC_ATOP)
            subCountryClickListener?.let {
                it("DFM")
            }
        }

        holder.mBinding.adx.setOnClickListener {
            holder.mBinding.adx.background.setColorFilter(Color.parseColor("#72c4f1"),PorterDuff.Mode.SRC_ATOP)
            holder.mBinding.dfm.background = ContextCompat.getDrawable(context,R.drawable.flag_bg)
            subCountryClickListener?.let {
                it("ADX")
            }
        }

        holder.mBinding.itemCountryContainer.setOnClickListener {
            if (showSubCountries) {

                if (list[position].country == "UAE") {

                    uaeClicked = true
                    val previousItem = Company_frag.selectedItem
                    Company_frag.selectedItem = position
                    notifyItemChanged(previousItem)
                    notifyItemChanged(position)
                    onCountryClick.onCountryClick(list[position], it,position)
                    /*subCountryClickListener?.let {
                        it("DFM")
                    }*/


                } else {
                    uaeClicked = false
                    val previousItem = Company_frag.selectedItem
                    Company_frag.selectedItem = position
                    notifyItemChanged(previousItem)
                    notifyItemChanged(position)
                    onCountryClick.onCountryClick(list[position], it, position)
                }

            } else {
                uaeClicked = false
                val previousItem = Company_frag.selectedItem
                Company_frag.selectedItem = position
                notifyItemChanged(previousItem)
                notifyItemChanged(position)
                onCountryClick.onCountryClick(list[position], it, position)
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setSubCountryShow(showSubCountries: Boolean) {
        this.showSubCountries = showSubCountries
    }

    fun setOnSubCountryClickListener(listener: (data: String) -> Unit) {
        subCountryClickListener = listener
    }
}

interface onCountryClick {
    fun onCountryClick(countriesDTO: CountriesDTO, view: View, position: Int)
}