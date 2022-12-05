package com.ashomapp.presentation.forum

import com.ashomapp.network.response.dashboard.ForumDTO

interface onForumClick {

    fun onOptionCLick(forumDTO: ForumDTO, option : String)

    fun onCommentClick(forumDTO: ForumDTO, position: Int)

    fun ondisLikeunDislike(forumDTO: ForumDTO)

    fun onLikeunlike(forumDTO: ForumDTO)

    fun onShareClick(forumDTO: ForumDTO, position: Int)

    fun onForumDetail(forumDTO: ForumDTO, position: Int)

    fun onEditForum(forumDTO: ForumDTO)

    fun onDeleteForum(forumDTO: ForumDTO, position: Int)


}