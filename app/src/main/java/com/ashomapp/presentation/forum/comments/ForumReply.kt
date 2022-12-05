package com.ashomapp.presentation.forum.comments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentForumReplyBinding
import com.ashomapp.network.repository.ForumRepository
import com.ashomapp.network.response.dashboard.CommentDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.forum.ForumFrag
import com.ashomapp.presentation.forum.ForumViewModel
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.utils.getPrettyTime
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.showKeyboard
import com.ashomapp.utils.temp_showToast
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter

class ForumReply : Fragment(), onCommentClick {
    private lateinit var mBinding: FragmentForumReplyBinding
    private val commentReplies = MutableLiveData<List<CommentDTO>>()
    val mForumviewmodel: ForumViewModel by viewModels()
    private lateinit var mCommentAdapter: CommentAdapter
    var commentReplyID = ""
    var username = ""
    private val scrollBottom = MutableLiveData<Boolean>(false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentForumReplyBinding.inflate(layoutInflater, container, false).apply {
            viewmodel = mForumviewmodel
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        HomeFlow.forumCurrentID = R.id.forumReply
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!HomeFlow.forumreplyCommentBundle.isNullOrEmpty()) {
                    val args =
                        HomeFlow.forumreplyCommentBundle[HomeFlow.forumreplyCommentBundle.size - 1]


                    HomeFlow.forumreplyCommentBundle.removeAt(HomeFlow.forumreplyCommentBundle.size - 1)
                    findNavController().navigate(R.id.forumReply, args)

                } else {
                    findNavController().navigate(R.id.forumComments, HomeFlow.forumCommentBundle)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this.viewLifecycleOwner,
            onBackPressedCallback
        )


        mBinding.toolbar.mainBack.setOnClickListener {

            ObjectAnimator.ofFloat(mBinding.toolbar.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        if (!HomeFlow.forumreplyCommentBundle.isNullOrEmpty()) {
                            val args =
                                HomeFlow.forumreplyCommentBundle[HomeFlow.forumreplyCommentBundle.size - 1]
                            HomeFlow.forumreplyCommentBundle.removeAt(HomeFlow.forumreplyCommentBundle.size - 1)
                            findNavController().navigate(R.id.forumReply, args)

                        } else {
                            findNavController().navigate(
                                R.id.forumComments,
                                HomeFlow.forumCommentBundle
                            )
                        }
                    }
                }
        }
        mBinding.etComment.setText("${SharedPrefrenceHelper.user.first_name} ${SharedPrefrenceHelper.user.last_name}-")
        mBinding.toolbar.title.text = "COMMENTS"
        mBinding.toolbar.notificationBellIcon.visibility = View.GONE
        mBinding.toolbar.title.visibility = View.VISIBLE
        mBinding.toolbar.icon.visibility = View.INVISIBLE
        mBinding.toolbar.toolProfile.visibility = View.INVISIBLE
        val forumItem = requireArguments().getString("commentreply")
        if (!forumItem.isNullOrEmpty()) {
            val forumItemDto = Gson().fromJson(forumItem, CommentDTO::class.java)
            mForumviewmodel.forumID.value = ForumFrag.forumid
            //    commentReplyID = forumItemDto.id
            mForumviewmodel.reply_comment_id.value = forumItemDto.id

            getCommmentReplies()
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(forumItemDto.profile_pic).into(mBinding.forumContainer.itemForumProfile)
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.itemForumProfile)

            mBinding.forumContainer.itemForumName.text = "${forumItemDto.name}"
            mBinding.forumContainer.itemForumTime.text =
                getPrettyTime(forumItemDto.created, "yyyy-MM-dd HH:mm:ss")
            mBinding.forumContainer.itemForumDescription.text = forumItemDto.comment


        }

        commentReplies.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                mBinding.noCommentFound.visibility = View.GONE
                mCommentAdapter = CommentAdapter(it, this)
                mBinding.commentRv.adapter = mCommentAdapter
                VerticalOverScrollBounceEffectDecorator(RecyclerViewOverScrollDecorAdapter(mBinding.commentRv))

                mCommentAdapter.notifyDataSetChanged()
                if (scrollBottom.value == true) {
                    mBinding.commentRv.layoutManager?.scrollToPosition(it.size - 1)
                    mCommentAdapter.notifyDataSetChanged()
                    scrollBottom.value = false
                }

            } else {
                mBinding.noCommentFound.visibility = View.VISIBLE
                mCommentAdapter = CommentAdapter(it, this)
                mBinding.commentRv.adapter = mCommentAdapter
                mCommentAdapter.notifyDataSetChanged()

            }
        }
        mBinding.commentRv.setHasFixedSize(true)


        mBinding.sendComment.setOnClickListener {
            Log.d("commentid", mForumviewmodel.editCommentID.value.toString())
            if (mForumviewmodel.mcomment.value.isNullOrEmpty()) {
                mBinding.etComment.setError("Enter comment")
            } else {
                if (!commentReplyID.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        val result = ForumRepository.replyComment(
                            commentReplyID.toString(),
                            mForumviewmodel.mcomment.value.toString()
                        )
                        when (result) {
                            is ResultWrapper.Success -> {
                                temp_showToast("Comment reply added successfully..")
                                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                                mForumviewmodel.mcomment.value = " "
                                commentReplyID = ""
                                mBinding.etComment.setText("")
                                mBinding.etComment.clearFocus()
                                getCommmentReplies()
                                scrollBottom.value = true
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
                                temp_showToast("Comment successfully updated..")
                                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                                mForumviewmodel.mcomment.value = ""
                                mForumviewmodel.editCommentID.value = ""
                                mBinding.etComment.setText("")
                                mBinding.etComment.clearFocus()
                                getCommmentReplies()
                                scrollBottom.value = true
                            }
                            is ResultWrapper.Failure -> {

                            }
                        }
                    }
                } else {
                    lifecycleScope.launch {
                        val result = ForumRepository.replyComment(
                            mForumviewmodel.reply_comment_id.value.toString(),
                            mForumviewmodel.mcomment.value.toString()
                        )
                        when (result) {
                            is ResultWrapper.Success -> {
                                temp_showToast("Comment  reply successfully added..")
                                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                                mForumviewmodel.mcomment.value = ""
                                // mForumviewmodel.reply_comment_id.value = ""
                                mBinding.etComment.setText("Reply-")
                                mBinding.etComment.clearFocus()
                                getCommmentReplies()
                                scrollBottom.value = true
                            }
                            is ResultWrapper.Failure -> {

                            }
                        }
                    }
                }
            }
        }

    }

    private fun getCommmentReplies() {
        lifecycleScope.launch {
            val result =
                ForumRepository.getCommetnReplyComment(mForumviewmodel.reply_comment_id.value.toString())
            when (result) {
                is ResultWrapper.Success -> {
                    val list = mutableListOf<CommentDTO>()
                    list.addAll(result.response.map { it })
                    commentReplies.value = list
                }
                is ResultWrapper.Failure -> {
                    Log.d("error", result.errorMessage)
                }
            }
        }
    }

    override fun onReply(commentDTO: CommentDTO) {
        commentReplyID = commentDTO.id

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
        findNavController().navigate(R.id.forumReply, args)

    }

}