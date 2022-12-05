package com.ashomapp.presentation.home.stockPrice.adapter

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.R
import com.ashomapp.databinding.ItemStocksPricesBinding
import com.ashomapp.databinding.ItemStocksPricesGreyBinding
import com.ashomapp.databinding.ItemTickerCompanyNameBinding
import com.ashomapp.databinding.ItemTickerCompanyNameGreyBinding
import com.ashomapp.presentation.home.stockPrice.model.StockListModel
import com.ashomapp.presentation.home.stockPrice.model.StockListModelItem
import kotlinx.android.synthetic.main.item_stocks_prices.view.*
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow


class StockPriceAdapter @Inject constructor() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolderWhite(private val binding: ItemStocksPricesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StockListModelItem) {
            with(binding) {


                last.text = data.Last.toString()
                open.text = data.Open.toString()
                high.text = data.High.toString()
                low.text = data.Low.toString()
                close.text = data.Universal_Close_Price.toString()
                change.text = data.price_difference
                changePercent.text = data.percentage_change
                volume.text = getFormattedNumber(data.Volume.toLong())
                date.text = data.Trade_Date
                exchange.text = data.Exchange_Description

                if (data.Industry == null || data.Industry.toString()
                        .isEmpty() || data.Industry == "null"
                ) {
                    industry.text = "-"
                } else {
                    industry.text = data.Industry.toString()
                }



                if (data.price_difference == "NA") {
                    change.setTextColor(ContextCompat.getColor(context, R.color.textcolor))
                } else {
                    if (data.price_difference.toDouble() < 0) {
                        change.setTextColor(ContextCompat.getColor(context, R.color.shareqrcolor))
                    } else {
                        change.setTextColor(ContextCompat.getColor(context, R.color.graphGreen))
                    }
                }

                if (data.Universal_Close_Price < 0) {
                    close.setTextColor(ContextCompat.getColor(context, R.color.shareqrcolor))
                } else {
                    close.setTextColor(ContextCompat.getColor(context, R.color.graphGreen))
                }
                root.setOnClickListener {
                    stockClickListener?.let {
                        it(data)
                    }
                }
            }
        }
    }

    inner class ViewHolderGrey(private val binding: ItemStocksPricesGreyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StockListModelItem) {
            with(binding) {


                last.text = data.Last.toString()
                open.text = data.Open.toString()
                high.text = data.High.toString()
                low.text = data.Low.toString()
                close.text = data.Universal_Close_Price.toString()
                change.text = data.price_difference
                changePercent.text = data.percentage_change
                volume.text = getFormattedNumber(data.Volume.toLong())
                date.text = data.Trade_Date
                exchange.text = data.Exchange_Description

                if (data.Industry == null || data.Industry.toString()
                        .isEmpty() || data.Industry == "null"
                ) {
                    industry.text = "-"
                } else {
                    industry.text = data.Industry.toString()
                }


                if (data.price_difference == "NA") {
                    change.setTextColor(ContextCompat.getColor(context, R.color.textcolor))
                } else {
                    if (data.price_difference.toDouble() < 0) {
                        change.setTextColor(ContextCompat.getColor(context, R.color.shareqrcolor))
                    } else {
                        change.setTextColor(ContextCompat.getColor(context, R.color.graphGreen))
                    }
                }

                if (data.Universal_Close_Price < 0) {
                    close.setTextColor(ContextCompat.getColor(context, R.color.shareqrcolor))
                } else {
                    close.setTextColor(ContextCompat.getColor(context, R.color.graphGreen))
                }
                root.setOnClickListener {
                    stockClickListener?.let {
                        it(data)
                    }
                }
            }
        }
    }

    fun getFormattedNumber(count: Long): String {
        if (count < 1000) return "" + count
        val exp = (ln(count.toDouble()) / ln(1000.0)).toInt()
        return String.format("%.1f %c", count / 1000.0.pow(exp.toDouble()), "kMGTPE"[exp - 1])
    }


    private val diffUtil = object : DiffUtil.ItemCallback<StockListModelItem>() {
        override fun areItemsTheSame(
            oldItem: StockListModelItem,
            newItem: StockListModelItem,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: StockListModelItem,
            newItem: StockListModelItem,
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    private var stockClickListener: ((id: StockListModelItem) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        context = parent.context
        return when (viewType) {
            0 -> {
                ViewHolderWhite(
                    ItemStocksPricesBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ))
            }
            1 -> {
                ViewHolderGrey(
                    ItemStocksPricesGreyBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ))
            }
            else -> {
                ViewHolderWhite(
                    ItemStocksPricesBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ))
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val viewHolder: ViewHolderWhite = holder as ViewHolderWhite

                viewHolder.bind(differ.currentList[position])
            }
            1 -> {
                val viewHolder: ViewHolderGrey = holder as ViewHolderGrey

                viewHolder.bind(differ.currentList[position])
            }
        }


    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnStockClickListener(listener: (id: StockListModelItem) -> Unit) {
        stockClickListener = listener
    }


    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0)
            0
        else
            1
    }

}