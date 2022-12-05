package com.ashomapp.presentation.forum.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.databinding.ItemCommentsBinding
import com.ashomapp.network.response.dashboard.CommentDTO
import com.ashomapp.utils.getPrettyTime
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(
    val list: List<CommentDTO>,
    val onCommentClick: onCommentClick
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    class ViewHolder(val mBinding: ItemCommentsBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun bindview(commentDTO: CommentDTO, onCommentClick: onCommentClick) {
            Glide.with(AshomAppApplication.instance.applicationContext).load(commentDTO.profile_pic)
                .into(mBinding.commentPro)
            mBinding.commentUsername.text = "${commentDTO.name}"
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            df.setTimeZone(TimeZone.getTimeZone("UTC"))
            val date: Date = df.parse(commentDTO.created)
            df.setTimeZone(TimeZone.getDefault())
            val formattedDate: String = df.format(date)
            mBinding.commentDate.text = getPrettyTime(formattedDate, "yyyy-MM-dd HH:mm:ss")
            mBinding.commentComment.text = "${commentDTO.comment}"
            mBinding.editComment.setOnClickListener {
                onCommentClick.onEditComment(commentDTO)
            }
            if (commentDTO.isMine.equals("0")) {
                mBinding.deleteComment.visibility = View.GONE
                mBinding.editComment.visibility = View.GONE
            } else {
                mBinding.deleteComment.visibility = View.VISIBLE
                mBinding.editComment.visibility = View.VISIBLE
            }
            mBinding.reply.setOnClickListener {
                onCommentClick.onReply(commentDTO)
            }
            mBinding.deleteComment.setOnClickListener {
                onCommentClick.onDeleteComment(commentDTO, position)
            }

            if (commentDTO.in_reply) {
                mBinding.commentReplyRv.visibility = View.VISIBLE
                val adapter = CommentAdapter(commentDTO.replies, onCommentClick)
                mBinding.commentReplyRv.adapter = adapter
                adapter.notifyDataSetChanged()
            } else {
                mBinding.commentReplyRv.visibility = View.GONE
            }

            if (commentDTO.in_reply && commentDTO.replies.isNullOrEmpty()) {
                mBinding.moreReply.visibility = View.VISIBLE
                mBinding.moreReply.setOnClickListener {
                    onCommentClick.onMoreReply(commentDTO)
                }
            } else {
                mBinding.moreReply.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding =
            ItemCommentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(mBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindview(list[position], onCommentClick)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

interface onCommentClick {
    fun onReply(commentDTO: CommentDTO)

    fun onEditComment(commentDTO: CommentDTO)

    fun onDeleteComment(commentDTO: CommentDTO, position: Int)

    fun onMoreReply(commentDTO: CommentDTO)
}