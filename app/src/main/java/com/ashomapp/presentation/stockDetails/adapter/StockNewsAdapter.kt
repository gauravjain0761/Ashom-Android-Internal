package com.ashomapp.presentation.stockDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemNewsBinding
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.presentation.home.stockPrice.model.StockListModelItem
import com.ashomapp.utils.getTimeAgo
import com.bumptech.glide.Glide
import com.snov.timeagolibrary.PrettyTimeAgo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class StockNewsAdapter @Inject constructor() : RecyclerView.Adapter<StockNewsAdapter.ViewHolder>() {
    private lateinit var context: Context
    lateinit var stockListModel: MutableList<StockListModelItem>
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    inner class ViewHolder(private val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NewsItemDTO) {
            with(binding) {
                binding.itemNewsSource.text = "${data.source}"
                binding.itemNewsTitle.text = "${data.title}"
//                binding.itemNewsDate.text = getPrettyTimeForForum(data.created_date!!,"yyyy-MM-dd HH:mm:ss")
                binding.itemNewsDate.text = getTimeAgo(dateFormat.parse(data.created_date!!))

                binding.itemNewsTitle.setOnClickListener {
                    stockNewsTitleClickListener?.let {
                        it(data)
                    }
//                    onNewItemClick.onNavigatetoDetail(data, position)
                }

                binding.newsCard.setOnClickListener {
                    stockNewsTitleClickListener?.let {
                        it(data)
                    }
//                    onNewItemClick.onNavigatetoDetail(data, position)
                }

                root.setOnClickListener {
                    stockNewsClickListener?.let {
                        it(data)
                    }
                }

                itemNewsShareInForum.setOnClickListener {
                    stockNewsShareInForumClickListener?.let {
                        it(data)
                    }

//                    onNewItemClick.addToForum(data,position)
                }

                itemNwesInShare.setOnClickListener {
                    stockNewsShareClickListener?.let {
                        it(data)
                    }
//                    onNewItemClick.onShare(data, position)
                }

                Glide.with(AshomAppApplication.instance.applicationContext)
                    .load(data.image_url)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.itemNewsIv)
            }
        }
    }

    fun  getPrettyFormat(createdDate: String, format: String):String{
        try {
            val dateInMillis = PrettyTimeAgo.timestampToMilli(context, createdDate, format)
            return PrettyTimeAgo.getTimeAgo(dateInMillis)
        } catch (e: ParseException) {
            // Do error handling here
            e.printStackTrace()
        }
        return ""
    }
    private val diffUtil = object : DiffUtil.ItemCallback<NewsItemDTO>() {
        override fun areItemsTheSame(oldItem: NewsItemDTO, newItem: NewsItemDTO): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: NewsItemDTO,
            newItem: NewsItemDTO,
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)

    private var stockNewsClickListener: ((id: NewsItemDTO) -> Unit)? = null
    private var stockNewsTitleClickListener: ((data: NewsItemDTO) -> Unit)? = null
    private var stockNewsShareClickListener: ((data: NewsItemDTO) -> Unit)? = null
    private var stockNewsShareInForumClickListener: ((data: NewsItemDTO) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): StockNewsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemNewsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StockNewsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setOnStockNewsClickListener(listener: (data: NewsItemDTO) -> Unit) { stockNewsClickListener = listener }

    fun setOnNewTitleClickListener(listener: (data: NewsItemDTO) -> Unit) { stockNewsTitleClickListener = listener }

    fun setOnStockNewsShareInForumClickListener(listener: (id: NewsItemDTO) -> Unit) { stockNewsShareInForumClickListener = listener }
    fun setOnStockNewsShareClickListener(listener: (id: NewsItemDTO) -> Unit) { stockNewsShareClickListener = listener }



}