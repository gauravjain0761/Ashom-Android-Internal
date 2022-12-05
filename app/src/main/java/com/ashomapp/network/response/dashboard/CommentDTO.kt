package com.ashomapp.network.response.dashboard

import com.google.gson.annotations.SerializedName

data class CommentDTO(
    @SerializedName("id")
    val id : String,
    @SerializedName("name")
    val name : String,
    @SerializedName("profile_pic")
    val profile_pic : String,
    @SerializedName("comment")
    val comment : String,
    @SerializedName("replied_to")
    val replied_to : String,
    @SerializedName("created")
    val created : String,
    @SerializedName("isMine")
    val isMine : String,
    @SerializedName("in_reply")
    val in_reply : Boolean,
    @SerializedName("replies")
    val replies  : List<CommentDTO>


)
