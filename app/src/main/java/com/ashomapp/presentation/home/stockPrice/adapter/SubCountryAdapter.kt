package com.ashomapp.presentation.home.stockPrice.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemCountryBinding
import com.ashomapp.databinding.ItemNewsBinding
import com.ashomapp.databinding.ItemStocksPricesBinding
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.presentation.home.Company_frag
import com.ashomapp.presentation.home.stockPrice.model.StockListModel
import com.ashomapp.presentation.home.stockPrice.model.StockListModelItem
import com.ashomapp.utils.getPrettyTime
import com.bumptech.glide.Glide
import java.lang.Exception
import javax.inject.Inject


class SubCountryAdapter @Inject constructor() : RecyclerView.Adapter<SubCountryAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var selectedPosition: Int? = null
    inner class ViewHolder(private val binding: ItemCountryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CountriesDTO) {
            with(binding) {
                itemCountryName.text = data.country
                try {
                    itemCountIv.setImageResource(R.drawable.uae_flag)
                  /*  when (data.country) {
                        "KSA" -> {
                            itemCountIv.setImageResource(R.drawable.ksaflag)
                        }
                        "UAE" -> {
                            itemCountIv.setImageResource(R.drawable.uae_flag)
                        }
                        "All Countries" -> {
                            itemCountIv.setImageResource(R.drawable.all_countries)
                        }
                        else -> {
                            Glide.with(AshomAppApplication.instance.applicationContext)
                                .load("https://countryflagsapi.com/png/${data.country}")
                                .into(itemCountIv)
                        }
                    }*/



                    if (selectedPosition==absoluteAdapterPosition){
                        itemCountryContainer.background.setColorFilter(Color.parseColor("#72c4f1"), PorterDuff.Mode.SRC_ATOP)
                    }else{
                        itemCountryContainer.background = ContextCompat.getDrawable(context,R.drawable.flag_bg)
                    }



                }catch (e : Exception){
                    Log.d("exception_country", e.localizedMessage)
                }

                root.setOnClickListener {
                    selectedPosition = absoluteAdapterPosition
                    subCountryClickListener?.let {
                        it(data)
                    }
                    notifyDataSetChanged()
                }

            }

        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<CountriesDTO>() {
        override fun areItemsTheSame(oldItem: CountriesDTO, newItem: CountriesDTO): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: CountriesDTO,
            newItem: CountriesDTO,
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)

    private var subCountryClickListener: ((id: CountriesDTO) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SubCountryAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemCountryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SubCountryAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnSubCountryClickListener(listener: (data: CountriesDTO) -> Unit) { subCountryClickListener = listener }


}