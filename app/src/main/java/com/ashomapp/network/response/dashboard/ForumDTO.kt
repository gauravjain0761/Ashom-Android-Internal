package com.ashomapp.network.response.dashboard

import com.google.gson.annotations.SerializedName

data class ForumDTO(

    @SerializedName("metadata")
    var metadata: String,
         @SerializedName("id")
         val id : String,

         @SerializedName("content")
         val content : String,

         @SerializedName("content_image")
         val content_image : String,

         @SerializedName("share_count")
         var share_count : String,

         @SerializedName("forum_type")
         val forum_type  : String,

         @SerializedName("validity")
         val validity : String,

         @SerializedName("options")
         val option : forumOptions? = null,

         @SerializedName("created")
         val created : String,

         @SerializedName("token")
         val token : String,

         @SerializedName("first_name")
         val first_name : String,

         @SerializedName("last_name")
         val last_name : String,

         @SerializedName("email")
         val email : String,

         @SerializedName("country_code")
         val country_code : String,

         @SerializedName("mobile")
         val mobile : String,

         @SerializedName("password")
         val password : String,

         @SerializedName("profile_pic")
         val profile_pic : String,

         @SerializedName("login_type")
         val login_type : String,

         @SerializedName("social_id")
         val social_id : String,

         @SerializedName("device_id")
         val device_id : String,

         @SerializedName("voted")
         var voted : Boolean,


         @SerializedName("posted_by_name")
         val posted_by_name : String,

         @SerializedName("posted_by_profile")
         val posted_by_profile : String,

         @SerializedName("liked")
         var liked : Boolean,

         @SerializedName("total_liked")
         var total_liked : String,

         @SerializedName("disliked")
         var disliked : Boolean,

         @SerializedName("total_disliked")
         var total_disliked : String,

         @SerializedName("total_comments")
         val total_comments : String,

         @SerializedName("isMine")
         val isMine : Boolean,

         )

data class forumOptions(
    @SerializedName("option1")
    val option1 : String,
    @SerializedName("option2")
    val option2 : String,
    @SerializedName("option3")
    val option3 : String,
    @SerializedName("total_option1_voters")
    val total_option1_voters : String,
    @SerializedName("total_option2_voters")
    val total_option2_voters : String,
    @SerializedName("total_option3_voters")
    val total_option3_voters : String,
    @SerializedName("percentage_option1_voters")
    val percentage_option1_voters : String,
    @SerializedName("percentange_option2_voters")
    val percentange_option2_voters : String,
    @SerializedName("percentage_option3_voters")
    val percentage_option3_voters : String


)