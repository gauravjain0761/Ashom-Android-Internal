package com.ashomapp.network.services

import com.ashomapp.network.response.ResponseDTO
import com.ashomapp.network.response.dashboard.CommentDTO
import com.ashomapp.network.response.dashboard.ForumDTO
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.http.*

interface ForumServices {

    //https://ashom.app/api/webservice/userdata
//https://ashom.app/api/webservice/forum token, content, content_image

    @POST("webservice/forum")
    @Multipart
    suspend fun submitComposeforum(
        @Part token :  MultipartBody.Part,
        @Part content :  MultipartBody.Part,
        @Part image_url :  MultipartBody.Part? = null,
        @Part content_image :  MultipartBody.Part? = null
   ) : ResponseDTO

    //https://ashom.app/api/webservice/updateforum/
    //https://ashom.app/api/webservice/postpoll
    //token, title, option1, option2, option3, validity (:All Fields are required and validity must be in numbers eg : 1day : 1)


    @POST("webservice/updateforum")
    @Multipart
    suspend fun updateComposeforum(
        @Part token :  MultipartBody.Part,
        @Part content :  MultipartBody.Part,
        @Part image_url :  MultipartBody.Part? = null,
        @Part content_image :  MultipartBody.Part? = null
    ) : ResponseDTO

    @POST("webservice/postpoll")
    @FormUrlEncoded
    suspend fun submitPollForum(
        @Field("token") token : String ,
        @Field("title") title : String ,
        @Field("option1") option1 : String ,
        @Field("option2") option2 : String ,
        @Field("option3") option3 : String ,
        @Field("validity") validity : String
    ) : ResponseDTO

    //https://ashom.app/api/webservice/sendvote
    //token, poll_id, selected_option  (Selected Option Must be only 1, 2, 3) All Fields are required
    @POST("webservice/sendvote")
    @FormUrlEncoded
    suspend fun sendvote(
        @Field("token") token : String ,
        @Field("poll_id") poll_id : String ,
        @Field("selected_option") selected_option : String
    ) : ResponseDTO


    @GET("webservice/comments/{forum_id}/{token}")
    suspend fun getComments(
        @Path("token") token: String,
        @Path("forum_id") forum_id : String
     ) : List<CommentDTO>

    //https://www.dbvertex.com/ashoms/api/webservice/comment
    //token, forum_id, comment
    @POST("webservice/comment")
    @FormUrlEncoded
    suspend fun postComment(
        @Field("token") token: String,
        @Field("forum_id") forum_id : String,
        @Field("comment") comment: String
    ) : ResponseDTO

    @POST("webservice/comment")
    @FormUrlEncoded
    suspend fun editComment(
        @Field("token") token: String,
        @Field("forum_id") forum_id : String,
        @Field("comment_id") comment_id : String,
        @Field("comment") comment: String
    ) : ResponseDTO
    //https://ashom.app/api/webservice/reply token, comment_id, comment, reply_id
    @POST("webservice/reply")
    @FormUrlEncoded
    suspend fun replyComment(
        @Field("token") token: String,
        @Field("comment_id") comment_id : String,
        @Field("comment") comment: String
    ) : ResponseDTO



//https://ashom.app/api/webservice/deletecomment/{comment_or_reply_id}
    @GET("webservice/deletecomment/{comment_id}")
    suspend fun deleteComment(
        @Path("comment_id") comment_id : String
    )
    //https://ashom.app/api/webservice/dislikeorundislike
    @POST("webservice/dislikeorundislike")
    @FormUrlEncoded
    suspend fun dislikeorundislike(
        @Field("token") token: String,
        @Field("forum_id") forum_id : String,
    ) : ResponseDTO


    //https://ashom.app/api/webservice/likeorunlike
    @POST("webservice/likeorunlike")
    @FormUrlEncoded
    suspend fun likeorunlike(
        @Field("token") token: String,
        @Field("forum_id") forum_id : String,
    ) : ResponseDTO

    //https://ashom.app/api/webservice/shareproject/{forum_id}
    @GET("webservice/shareproject/{forum_id}")
    suspend fun forumShare(
        @Path("forum_id") forum_id : String
    ) : ResponseDTO

    @GET("webservice/deleteforum/{forum_id}")
    suspend fun deleteForum(
        @Path("forum_id") forum_id : String
    ) : JsonObject

    //https://ashom.app/api/webservice/shareproject/{forum_id}
    @GET("webservice/commentreplies/{reply_id}/{token}")
    suspend fun commentreplieslist(
        @Path("token") token : String,
        @Path("reply_id") reply_id : String

    ) : List<CommentDTO>
}