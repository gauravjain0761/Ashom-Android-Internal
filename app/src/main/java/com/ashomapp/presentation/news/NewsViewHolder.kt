package com.ashomapp.presentation.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemNewsBinding
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.utils.getPrettyTime
import com.bumptech.glide.Glide

class NewsViewHolder(private  val binding : ItemNewsBinding) :
  RecyclerView.ViewHolder(binding.root){
        fun bind(newsItemDTO: NewsItemDTO, onNewItemClick: onNewItemClick){
                  binding.itemNewsSource.text = "${newsItemDTO.source}"
            binding.itemNewsTitle.text = "${newsItemDTO.title}"
            /*
            for uto to current time zone

            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            df.setTimeZone(TimeZone.getTimeZone("UTC"))
            val date: Date = df.parse(newsItemDTO.created_date)
            df.setTimeZone(TimeZone.getDefault())
            val formattedDate: String = df.format(date)


            Log.d("datenewsitem", "${newsItemDTO.title}  \n $formattedDate")*/
            binding.itemNewsDate.text = getPrettyTime(newsItemDTO.created_date!!,"yyyy-MM-dd HH:mm:ss")
            binding.itemNewsTitle.setOnClickListener {
                onNewItemClick.onNavigatetoDetail(newsItemDTO, position)
            }
            binding.root.setOnClickListener {
                onNewItemClick.onNavigatetoDetail(newsItemDTO, position)
            }
            binding.itemNewsShareInForum.setOnClickListener {
               onNewItemClick.addToForum(newsItemDTO,position)
            }
            binding.itemNwesInShare.setOnClickListener {
                onNewItemClick.onShare(newsItemDTO, position)
            }
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(newsItemDTO.image_url)
                .placeholder(R.drawable.placeholder)
                .into(binding.itemNewsIv)
        }

    companion object {
        fun create(parent: ViewGroup): NewsViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemNewsBinding.inflate(layoutInflater, parent, false)
            return NewsViewHolder(binding)
        }
    }
}