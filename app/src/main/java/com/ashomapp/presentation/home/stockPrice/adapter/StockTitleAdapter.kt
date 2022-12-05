package com.ashomapp.presentation.home.stockPrice.adapter

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.databinding.ItemTickerCompanyNameBinding
import com.ashomapp.databinding.ItemTickerCompanyNameGreyBinding
import com.ashomapp.presentation.home.stockPrice.model.StockListModelItem
import javax.inject.Inject


class StockTitleAdapter @Inject constructor() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var context: Context
    var stockSelectedPosition: Int? = null

    inner class ViewHolderWhite(private val binding: ItemTickerCompanyNameBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StockListModelItem) {
            with(binding) {
              /*  if (absoluteAdapterPosition % 2 == 0) {
                    bgView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    *//*if(absoluteAdapterPosition==stockSelectedPosition){
                        bgView.setBackgroundColor(Color.parseColor("#000030"))
                    }else{
                        bgView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))

                    }*//*
                } else {
                    bgView.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_300))
                    *//*if(absoluteAdapterPosition==stockSelectedPosition){
                        bgView.setBackgroundColor(Color.parseColor("#000030"))
                    }else{

                    bgView.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_300))
                    }*//*
                }*/

                val word: Spannable = SpannableString("${data.RIC}")
                word.setSpan(ForegroundColorSpan(Color.BLACK),
                    0,
                    word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                companyName.text = word.toString().uppercase()
                val wordTwo: Spannable = SpannableString(" - ${data.Issuer_Name.uppercase()}")
                wordTwo.setSpan(ForegroundColorSpan(Color.BLUE),
                    0,
                    wordTwo.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                companyName.append(wordTwo)



                root.setOnClickListener {
                    stockSelectedPosition = absoluteAdapterPosition
                    stockClickListener?.let {
                        it(data)
                    }
//                    notifyDataSetChanged()
                }
            }
        }
    }

    inner class ViewHolderGrey(private val binding: ItemTickerCompanyNameGreyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StockListModelItem) {
            with(binding) {


                val word: Spannable = SpannableString("${data.RIC}")
                word.setSpan(ForegroundColorSpan(Color.BLACK),
                    0,
                    word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                companyName.text = word.toString().uppercase()
                val wordTwo: Spannable = SpannableString(" - ${data.Issuer_Name.uppercase()}")
                wordTwo.setSpan(ForegroundColorSpan(Color.BLUE),
                    0,
                    wordTwo.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                companyName.append(wordTwo)



                root.setOnClickListener {
                    stockSelectedPosition = absoluteAdapterPosition
                    stockClickListener?.let {
                        it(data)
                    }
//                    notifyDataSetChanged()
                }
            }
        }
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
                    ItemTickerCompanyNameBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ))
            }
            1 -> {
                ViewHolderGrey(
                    ItemTickerCompanyNameGreyBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ))
            }
            else -> {
                ViewHolderWhite(
                    ItemTickerCompanyNameBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ))
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            0->{
                val viewHolder: ViewHolderWhite = holder as ViewHolderWhite
                viewHolder.bind(differ.currentList[position])
            }
            1->{
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
        return if (position % 2 == 0) {
            0
        } else {
            1
        }

    }

    /*  fun setStockList(stockListData: MutableList<StockListModelItem>){
          stockSelectedPosition = null
          stockListModel = stockListData
      }
      */

}