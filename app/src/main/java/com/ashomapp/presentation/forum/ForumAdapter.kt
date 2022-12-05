package com.ashomapp.presentation.forum

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.ItemForumBinding
import com.ashomapp.network.response.dashboard.ForumDTO
import com.ashomapp.utils.capitalizeString
import com.ashomapp.utils.getPrettyTime
import com.ashomapp.utils.getPrettyTimeForForum
import com.ashomapp.utils.setanimation
import com.bumptech.glide.Glide
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ForumAdapter(val list: List<ForumDTO>, val onForumClick: onForumClick) :
    RecyclerView.Adapter<ForumAdapter.Forumviewholder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Forumviewholder {
        val binding = ItemForumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Forumviewholder(binding)
    }

    override fun onBindViewHolder(holder: Forumviewholder, position: Int) {
        holder.bind(list[position], onForumClick)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class Forumviewholder(val binding: ItemForumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forumDTO: ForumDTO, onForumClick: onForumClick) {
            binding.forumdto = forumDTO

            binding.itemForumName.text = "${forumDTO.posted_by_name}"
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            df.setTimeZone(TimeZone.getTimeZone("UTC"))
            val date: Date = df.parse(forumDTO.created)
            df.setTimeZone(TimeZone.getDefault())
            val formattedDate: String = df.format(date)
            binding.itemForumTime.text = getPrettyTimeForForum(formattedDate, "yyyy-MM-dd HH:mm:ss")
            Log.d("forumdate", forumDTO.created.toString())
            binding.deleteForum.setOnClickListener {
                setanimation(it)
                onForumClick.onDeleteForum(forumDTO, position)
            }
            binding.editForum.setOnClickListener {
                setanimation(it)
                onForumClick.onEditForum(forumDTO)
            }

            if (forumDTO.isMine && !forumDTO.forum_type.equals("poll")) {
                binding.editForum.visibility = View.VISIBLE
            } else {
                binding.editForum.visibility = View.GONE
            }
            if (forumDTO.isMine){
                binding.deleteForum.visibility = View.VISIBLE
            } else {
                binding.deleteForum.visibility = View.GONE
            }

            if (!forumDTO.content_image.isNullOrEmpty()) {
                binding.itemForumImageContainer.visibility = View.VISIBLE
                Glide.with(AshomAppApplication.instance.applicationContext)
                    .load(forumDTO.content_image)
                    .placeholder(R.drawable.placeholder)
                    .into(binding.itemForumImage)
            } else {
                binding.itemForumImageContainer.visibility = View.GONE
            }
            binding.itemForumDescription.setOnClickListener {
                val adas = forumDTO.content.split("@").toTypedArray()
                if (adas.size > 0) {
                    try {
                        val item = adas[1]
                        if (!item.isNullOrEmpty()) {
                            onForumClick.onForumDetail(forumDTO, position)
                        }
                    } catch (e: Exception) {
                        Log.d("error", "something wrong")
                    }
                }
            }
            binding.itemForumImageContainer.setOnClickListener {
                val adas = forumDTO.content.split("@").toTypedArray()
                if (adas.size > 0) {
                    try {
                        val item = adas[1]
                        if (!item.isNullOrEmpty()) {
                            onForumClick.onForumDetail(forumDTO, position)
                        }
                    } catch (e: Exception) {
                        Log.d("error", "something wrong")
                    }

                }
            }
            binding.commentContainer.setOnClickListener {
                setanimation(it)

                onForumClick.onCommentClick(forumDTO, position)
            }
            binding.like.setOnClickListener {
                setanimation(it)
                onForumClick.onLikeunlike(forumDTO)
            }
            binding.dislike.setOnClickListener {
                setanimation(it)
                onForumClick.ondisLikeunDislike(forumDTO)
            }
            binding.forumShare.setOnClickListener {
                setanimation(it)
                onForumClick.onShareClick(forumDTO, position)
            }
            if (forumDTO.forum_type.equals("poll")) {
                binding.forumOptionContainer.visibility = View.VISIBLE
                binding.editForum.visibility = View.GONE
                binding.option1.text = "${forumDTO.option!!.option1}"
                binding.option2.text = "${forumDTO.option!!.option2}"
                if (!forumDTO.option.option3.isNullOrEmpty()) {
                    binding.option3.text = "${forumDTO.option!!.option3}"
                    binding.option3.visibility = View.VISIBLE
                } else {
                    binding.option3.visibility = View.GONE
                }
                binding.option1.setOnClickListener {
                    if (!forumDTO.voted) {
                        binding.selectedoption = 1
                    }
                    onForumClick.onOptionCLick(forumDTO, "1")

                }

                binding.option2.setOnClickListener {
                    if (!forumDTO.voted) {
                        binding.selectedoption = 2
                    }
                    onForumClick.onOptionCLick(forumDTO, "2")
                }
                binding.option3.setOnClickListener {
                    if (!forumDTO.voted) {
                        binding.selectedoption = 3
                    }
                    onForumClick.onOptionCLick(forumDTO, "3")
                }
            } else {
                binding.forumOptionContainer.visibility = View.GONE
            }

            if (!forumDTO.content.isNullOrEmpty()) {
                val adas = forumDTO.content.split("@").toTypedArray()
                Log.d("forumsize", adas.size.toString())
                if (adas.size > 0) {

                    if (adas[0].substring(0, 1).equals("\n")) {
                        binding.itemForumDescription.text = capitalizeString("${adas[0].replace("\n", "")}")
                    } else {
                        binding.itemForumDescription.text = capitalizeString("${adas[0].replace("\n\n", " ")}")
                    }
                    try {
                        binding.itemForumLink.text = "${adas[1]}"
                        binding.itemForumLink.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        binding.itemForumLink.visibility = View.GONE
                        Log.d("error", "something wrong")
                    }

                } else {
                    binding.itemForumDescription.text = "${forumDTO.content}"
                    binding.itemForumLink.visibility = View.GONE
                }
                binding.itemForumDescription.visibility = View.VISIBLE

            } else {
                binding.itemForumDescription.visibility = View.GONE
            }
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(forumDTO.posted_by_profile).into(binding.itemForumProfile)
        }

    }
}

