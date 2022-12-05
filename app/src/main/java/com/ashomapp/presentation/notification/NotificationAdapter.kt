package com.ashomapp.presentation.notification

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemNotificationBinding
import com.ashomapp.network.response.NOtificationDataNews
import com.ashomapp.network.response.NotificationDTO
import com.ashomapp.network.response.NotificationData
import com.ashomapp.utils.getPrettyTime
import com.bumptech.glide.Glide
import com.google.gson.Gson
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    val list: List<NotificationDTO>,
    val onNotificationClick: onNotificationClick? = null
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(notificationDTO: NotificationDTO, onNotificationClick: onNotificationClick?) {

            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            df.setTimeZone(TimeZone.getTimeZone("UTC"))
            val date: Date = df.parse(notificationDTO.created_at)
            df.setTimeZone(TimeZone.getDefault())
            val formattedDate: String = df.format(date)
            binding.notificationDate.text =
                getPrettyTime(formattedDate, "yyyy-MM-dd HH:mm:ss")
            binding.notificationMainTitle.text = "${notificationDTO.message}"
            try {
                binding.root.setOnClickListener {
                    onNotificationClick?.onNotificationClick(notificationDTO)
                }
                if (notificationDTO.metadata.type.equals("Financial Report")) {
                    val newsitem = Gson().fromJson(
                        notificationDTO.metadata.data[0].toString(),
                        NotificationData::class.java
                    )
                    Log.d("Companynot", newsitem.toString())
                    Log.d("Companynot2", notificationDTO.metadata.data[0].toString())
                    if (newsitem.companypayload.Country.equals("KSA")) {
                        binding.notificationFlag.setImageResource(R.drawable.ksaflag)
                    } else if (newsitem.companypayload.Country.equals("UAE")) {
                        binding.notificationFlag.setImageResource(R.drawable.uae_flag)
                    } else if (newsitem.companypayload.Country.equals("All Countries")) {
                        binding.notificationFlag.setImageResource(R.drawable.all_countries)
                    } else {
                        Glide.with(AshomAppApplication.instance.applicationContext)
                            .load("https://countryflagsapi.com/png/${newsitem.companypayload.Country}")
                            .into(binding.notificationFlag)
                    }
                    binding.notificationFlag.visibility = View.VISIBLE
                    Glide.with(AshomAppApplication.instance.applicationContext)
                        .load("${newsitem.companypayload.image}")
                        .placeholder(R.drawable.placeholder)
                        .into(binding.notificationIcon)
                    binding.notificationIcon.setColorFilter(null)

                } else if (notificationDTO.metadata.type.equals("News")) {
                    val newsitem = Gson().fromJson(
                        notificationDTO.metadata.data[0].toString(),
                        NOtificationDataNews::class.java
                    )
                    binding.notificationFlag.visibility = View.GONE
                  //  binding.notificationIcon.setImageResource()
                    Glide.with(AshomAppApplication.instance.applicationContext)

                        .load("${newsitem.image_url}")
                        .placeholder(R.drawable.placeholder)
                        .into(binding.notificationIcon)
                    binding.notificationIcon.setColorFilter(null)

                    Log.d("Companynot", newsitem.toString())
                } else {
                    binding.notificationFlag.visibility = View.GONE
                    binding.notificationIcon.setImageResource(R.drawable.forum)
                    binding.notificationIcon.setColorFilter(
                        ContextCompat.getColor(
                            AshomAppApplication.instance.applicationContext,
                            R.color.black
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    );
                }

            } catch (e: Exception) {
                Log.d("exception_country", e.localizedMessage)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(list[position], onNotificationClick)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}