package com.ashomapp.network.repository

import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.network.retrofit.getRetrofitService
import com.ashomapp.network.retrofit.safelyCallApi
import com.ashomapp.network.services.ForumServices
import com.ashomapp.network.services.HomeServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception

object ForumRepository {
    private val services = getRetrofitService(ForumServices::class.java)
     var mtoken = SharedPrefrenceHelper.token.toString()

    suspend fun submitComposeForum(content : String, image_url : String?= null, content_image : Pair<ByteArray, String>? = null) = safelyCallApi {
       try {
           val imageRequestBody = content_image!!.first.toRequestBody("image/*".toMediaType())
           val mcontent_image = imageRequestBody?.let {
               MultipartBody.Part.createFormData("content_image", content_image.second,
                   it
               )
           }
           val token = MultipartBody.Part.createFormData("token", SharedPrefrenceHelper.token.toString())
           val mcontent = MultipartBody.Part.createFormData("content", content.toString())
           services.submitComposeforum(token, mcontent, mcontent_image!!)
       }catch (e : Exception){
           val token = MultipartBody.Part.createFormData("token", SharedPrefrenceHelper.token.toString())
           val mcontent = MultipartBody.Part.createFormData("content", content.toString())
           val content_image = MultipartBody.Part.createFormData("content_image", image_url.toString())
          if (image_url.isNullOrEmpty()){
              services.submitComposeforum(token, mcontent)
          }else{
              services.submitComposeforum(token, mcontent,content_image)
          }
       }
    }

    suspend fun updateComposeForum(forum_id : String, content : String, image_url : String?= null, content_image : Pair<ByteArray, String>? = null) = safelyCallApi {
        try {
            val imageRequestBody = content_image!!.first.toRequestBody("image/*".toMediaType())
            val mcontent_image = imageRequestBody?.let {
                MultipartBody.Part.createFormData("content_image", content_image.second,
                    it
                )
            }
                val token = MultipartBody.Part.createFormData("forum_id", forum_id)
            val mcontent = MultipartBody.Part.createFormData("content", content.toString())
            services.updateComposeforum(token, mcontent, mcontent_image!!)
        }catch (e : Exception){
            val token = MultipartBody.Part.createFormData("forum_id", forum_id)
            val mcontent = MultipartBody.Part.createFormData("content", content.toString())
            val content_image = MultipartBody.Part.createFormData("content_image", image_url.toString())
            if (image_url.isNullOrEmpty()){
                services.updateComposeforum(token, mcontent)
            }else{
                services.updateComposeforum(token, mcontent,content_image)
            }
        }
    }

    suspend fun submitPollForum(title : String, option1 : String, option2 : String, option3 : String, validity : String)= safelyCallApi {
        services.submitPollForum(SharedPrefrenceHelper.token.toString(), title, option1, option2, option3, validity)
    }

    suspend fun sendVote(pollid : String, selectedoption : String) = safelyCallApi {
        services.sendvote(SharedPrefrenceHelper.token.toString(), pollid, selectedoption)
    }

    suspend fun getComments(forum_id : String)= safelyCallApi {
        services.getComments(SharedPrefrenceHelper.token.toString(), forum_id)
    }

    suspend fun getCommetnReplyComment(comment_Id: String) = safelyCallApi {
        services.commentreplieslist(token = SharedPrefrenceHelper.token.toString(), comment_Id)
    }
    suspend fun postComment(forum_id: String, comment :String) = safelyCallApi {
        services.postComment(SharedPrefrenceHelper.token.toString(), forum_id, comment)
    }
    suspend fun editComment(forumid: String,comment_id: String, comment :String) = safelyCallApi {
        services.editComment(SharedPrefrenceHelper.token.toString(),forumid, comment_id, comment)
    }

    suspend fun deleteComment(comment_Id : String) = safelyCallApi {
        services.deleteComment(comment_Id)
    }

    suspend fun dislikeorundislike(forumid : String) = safelyCallApi {
        services.dislikeorundislike(token = SharedPrefrenceHelper.token.toString(), forum_id = forumid)
    }

    suspend fun likeorunlike(forumid : String) = safelyCallApi {
        services.likeorunlike(token = SharedPrefrenceHelper.token.toString(), forum_id = forumid)
    }

    suspend fun forumShare(forumid : String) = safelyCallApi {
        services.forumShare(forum_id = forumid)
    }


    suspend fun forumDelete(forumid : String) = safelyCallApi {
        services.deleteForum(forum_id = forumid)
    }

    //token, comment_id, comment, reply_id

    suspend fun replyComment(comment_Id: String, comment: String) = safelyCallApi {
        services.replyComment(SharedPrefrenceHelper.token.toString(), comment_Id, comment)
    }
}