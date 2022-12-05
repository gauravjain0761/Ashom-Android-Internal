package com.ashomapp.presentation.forum.comments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.FragmentForumCommentsBinding
import com.ashomapp.network.repository.ForumRepository
import com.ashomapp.network.response.dashboard.CommentDTO
import com.ashomapp.network.response.dashboard.ForumDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.forum.ForumViewModel
import com.ashomapp.utils.getPrettyTime
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.temp_showToast
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.launch
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.presentation.forum.ForumFrag
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.presentation.home.HomeViewModel
import com.ashomapp.utils.showKeyboard
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter


class ForumComments : Fragment(), onCommentClick {
    private lateinit var mBinding: FragmentForumCommentsBinding
    val mForumviewmodel: ForumViewModel by viewModels()
    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    private lateinit var mCommentAdapter: CommentAdapter
    private var scrollBottom = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentForumCommentsBinding.inflate(layoutInflater, container, false).apply {
            viewmodel = mForumviewmodel
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        HomeFlow.forumCurrentID = R.id.forumComments
        mHomeViewModel.recordEvent("view_forum_detail_click")
        mBinding.toolbar.mainBack.setOnClickListener {
            ObjectAnimator.ofFloat(mBinding.toolbar.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        findNavController().navigate(R.id.forumFrag)
                    }
                }
        }
        mBinding.toolbar.notificationBellIcon.visibility = View.GONE

        mBinding.toolbar.title.text = "COMMENTS"
        mBinding.toolbar.title.visibility = View.VISIBLE
        mBinding.toolbar.icon.visibility = View.INVISIBLE
        mBinding.toolbar.toolProfile.visibility = View.INVISIBLE
        val forumItem = requireArguments().getString("forumdto")
        if (!forumItem.isNullOrEmpty()) {
            val forumItemDto = Gson().fromJson(forumItem, ForumDTO::class.java)
            mBinding.forumContainer.forumdto = forumItemDto
            mForumviewmodel.forumID.value = forumItemDto.id
            ForumFrag.forumid = forumItemDto.id
            mForumviewmodel.getComments()
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(forumItemDto.posted_by_profile).into(mBinding.forumContainer.itemForumProfile)
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.itemForumProfile)

            mBinding.forumContainer.itemForumName.text =
                "${forumItemDto.first_name} ${forumItemDto.last_name}"
            mBinding.forumContainer.itemForumTime.text =
                getPrettyTime(forumItemDto.created, "yyyy-MM-dd HH:mm:ss")
            if (!forumItemDto.content_image.isNullOrEmpty()) {
                mBinding.forumContainer.itemForumImageContainer.visibility = View.VISIBLE
                Glide.with(AshomAppApplication.instance.applicationContext)
                    .load(forumItemDto.content_image).into(mBinding.forumContainer.itemForumImage)

            } else {
                mBinding.forumContainer.itemForumImageContainer.visibility = View.GONE
            }
            if (forumItemDto.forum_type.equals("poll")) {
                mBinding.forumContainer.forumOptionContainer.visibility = View.GONE
                mBinding.forumContainer.option1.text = "${forumItemDto.option!!.option1}"
                mBinding.forumContainer.option2.text = "${forumItemDto.option!!.option2}"
                mBinding.forumContainer.option3.text = "${forumItemDto.option!!.option3}"

            } else {
                mBinding.forumContainer.forumOptionContainer.visibility = View.GONE
            }

            if (!forumItemDto.content.isNullOrEmpty()) {
                mBinding.forumContainer.itemForumDescription.text = forumItemDto.content

                mBinding.forumContainer.itemForumDescription.visibility = View.VISIBLE
            } else {
                mBinding.forumContainer.itemForumDescription.visibility = View.GONE
            }

        }





        Handler().postDelayed({
            mForumviewmodel.commentlist.observe(viewLifecycleOwner) {
                if (!it.isNullOrEmpty()) {
                    mBinding.noCommentFound.visibility = View.GONE

                    mCommentAdapter = CommentAdapter(it, this)
                    mCommentAdapter.notifyDataSetChanged()

                    Log.d("rv_postion", "${mCommentAdapter.list.size}, and ${it.size}")

                    if (scrollBottom) {
                        mBinding.commentScrollview.fullScroll(View.FOCUS_DOWN)
                        scrollBottom = false
                    }
                    mBinding.commentRv.adapter = mCommentAdapter
                    mBinding.commentRv.setHasFixedSize(true)
                    VerticalOverScrollBounceEffectDecorator(
                        RecyclerViewOverScrollDecorAdapter(
                            mBinding.commentRv
                        )
                    )


                } else {
                    mCommentAdapter = CommentAdapter(it, this)
                    mCommentAdapter.notifyDataSetChanged()
                    mBinding.commentRv.adapter = mCommentAdapter
                    mBinding.commentRv.setHasFixedSize(true)
                    mBinding.noCommentFound.visibility = View.VISIBLE
                    VerticalOverScrollBounceEffectDecorator(
                        RecyclerViewOverScrollDecorAdapter(
                            mBinding.commentRv
                        )
                    )

                }
            }

        }, 500)


        mBinding.sendComment.setOnClickListener {
            Log.d("commentid", mForumviewmodel.editCommentID.value.toString())
            if (mForumviewmodel.mcomment.value.isNullOrEmpty()) {
                mBinding.etComment.setError("Enter comment")
            } else {
                if (!mForumviewmodel.comment_id.value.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        val result = ForumRepository.replyComment(
                            mForumviewmodel.comment_id.value.toString(),
                            mForumviewmodel.mcomment.value.toString()
                        )
                        when (result) {
                            is ResultWrapper.Success -> {
                                temp_showToast("Reply posted successfully.")
                                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                                mForumviewmodel.mcomment.value = ""
                                mForumviewmodel.comment_id.value = ""
                                mBinding.etComment.setText("")
                                mBinding.etComment.clearFocus()
                                mForumviewmodel.getComments()

                            }
                            is ResultWrapper.Failure -> {

                            }
                        }
                    }
                } else if (!mForumviewmodel.editCommentID.value.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        val result = ForumRepository.editComment(
                            mForumviewmodel.forumID.value.toString(),
                            mForumviewmodel.editCommentID.value.toString(),
                            mForumviewmodel.mcomment.value.toString()
                        )
                        when (result) {
                            is ResultWrapper.Success -> {
                                temp_showToast("Comment updated successfully.")
                                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                                mForumviewmodel.mcomment.value = ""
                                mForumviewmodel.editCommentID.value = ""
                                mBinding.etComment.setText("")
                                mBinding.etComment.clearFocus()
                                mForumviewmodel.getComments()

                            }
                            is ResultWrapper.Failure -> {

                            }
                        }
                    }
                } else {

                    lifecycleScope.launch {
                        val result = ForumRepository.postComment(
                            mForumviewmodel.forumID.value.toString(),
                            mForumviewmodel.mcomment.value.toString()
                        )
                        when (result) {
                            is ResultWrapper.Success -> {
                                temp_showToast("Comment added successfully.")
                                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                                mForumviewmodel.mcomment.value = " "
                                mBinding.etComment.setText("")
                                mBinding.etComment.clearFocus()
                                mForumviewmodel.getComments()
                                scrollBottom = true
                            }
                            is ResultWrapper.Failure -> {

                            }
                        }
                    }
                }
            }
        }
    }

    override fun onReply(commentDTO: CommentDTO) {
        mForumviewmodel.comment_id.value = commentDTO.id
        mBinding.etComment.setText("Reply:-")
        mBinding.etComment.setSelection(mBinding.etComment.text.length)
        mBinding.etComment.requestFocus()
        showKeyboard(AshomAppApplication.instance.applicationContext, mBinding.etComment)

    }

    override fun onEditComment(commentDTO: CommentDTO) {

        mForumviewmodel.editCommentID.value = commentDTO.id
        mForumviewmodel.mcomment.value = commentDTO.comment
        mBinding.etComment.setText("${commentDTO.comment}")
        mBinding.etComment.requestFocus()
        mBinding.etComment.setSelection(mBinding.etComment.text.length)
        showKeyboard(AshomAppApplication.instance.applicationContext, mBinding.etComment)
    }

    override fun onDeleteComment(commentDTO: CommentDTO, position: Int) {

        lifecycleScope.launch {
            val result = ForumRepository.deleteComment(commentDTO.id)
            when (result) {
                is ResultWrapper.Success -> {
                    mForumviewmodel.getComments()

                }
                is ResultWrapper.Failure -> {

                }
            }
        }

    }

    override fun onMoreReply(commentDTO: CommentDTO) {
        val mforumdto = Gson().toJson(commentDTO)
        val args = bundleOf("commentreply" to mforumdto)

        HomeFlow.forumreplyCommentBundle.add(Bundle(args))
        findNavController().navigate(R.id.action_forumComments_to_forumReply, args)

    }

}