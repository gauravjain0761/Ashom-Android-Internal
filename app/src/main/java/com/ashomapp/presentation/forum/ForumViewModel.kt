package com.ashomapp.presentation.forum

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashomapp.network.repository.ForumRepository
import com.ashomapp.network.response.dashboard.CommentDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.auth.NetworkState
import com.ashomapp.utils.temp_showToast
import com.dbvertex.myashomapp.network.repository.HomeRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class ForumViewModel : ViewModel() {
    val composemsg = MutableLiveData<String>()
    val composemsglink = MutableLiveData<String>()
    var compose_image: Pair<ByteArray, String>? = null
    val username = MutableLiveData<String>()
    val profileImage = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()

    //Poll
    val pollQuestion = MutableLiveData<String>()
    val option1 = MutableLiveData<String>()
    val option2 = MutableLiveData<String>()
    val option3 = MutableLiveData<String>("")
    val validity = MutableLiveData<String>()


    val comment_id = MutableLiveData<String>()
    val reply_comment_id = MutableLiveData<String>()

    val mcomment = MutableLiveData<String>()
    val forumID = MutableLiveData<String>()
    val editCommentID = MutableLiveData<String>()

    val ComposeforumID = MutableLiveData<String>()
    val commentlist = MutableLiveData<List<CommentDTO>>()
    val postPollloading = MutableLiveData<NetworkState>()
    val loading = MutableLiveData<NetworkState>()
    fun getUserdata() {
        viewModelScope.launch {
            val result = HomeRepository.getUserData()
            when (result) {
                is ResultWrapper.Success -> {
                    username.value =
                        "${result.response.userdata.first_name} ${result.response.userdata.last_name}"
                    profileImage.value = "${result.response.userdata.profile_pic}"
                }
                is ResultWrapper.Failure -> {

                }
            }
        }
    }

    fun submitCompose() {
        loading.value = NetworkState.LOADING_STARTED
        val compomsefinalmsg =
            if (composemsglink.value.isNullOrEmpty()) composemsg.value.toString() else "${composemsg.value.toString()}@${composemsglink.value.toString()}"
        viewModelScope.launch {
            val result = if (!imageUrl.value.isNullOrEmpty()) {
                ForumRepository.submitComposeForum(
                    content = compomsefinalmsg.toString(),
                    image_url = imageUrl.value.toString(),
                    content_image = compose_image
                )
            } else {
                ForumRepository.submitComposeForum(
                    content = compomsefinalmsg.toString(),
                    content_image = compose_image
                )
            }
            loading.value = NetworkState.LOADING_STOPPED
            when (result) {
                is ResultWrapper.Success -> {
                    loading.value = NetworkState.SUCCESS
                    temp_showToast("Forum added successfully")
                }
                is ResultWrapper.Failure -> {
                    loading.value = NetworkState.FAILED
                }
            }
        }
    }


    fun UpdateCompose() {
        loading.value = NetworkState.LOADING_STARTED
        val compomsefinalmsg =
            if (composemsglink.value.isNullOrEmpty()) composemsg.value.toString() else "${composemsg.value.toString()}@${composemsglink.value.toString()}"
        viewModelScope.launch {
            val result = if (!imageUrl.value.isNullOrEmpty()) {
                ForumRepository.updateComposeForum(
                    forum_id = ComposeforumID.value.toString(),
                    content = compomsefinalmsg.toString(),
                    image_url = imageUrl.value.toString(),
                    content_image = compose_image
                )
            } else {
                ForumRepository.updateComposeForum(
                    forum_id = ComposeforumID.value.toString(),
                    content = compomsefinalmsg.toString(),
                    content_image = compose_image
                )
            }
            loading.value = NetworkState.LOADING_STOPPED
            when (result) {
                is ResultWrapper.Success -> {
                    loading.value = NetworkState.SUCCESS
                    temp_showToast("Forum Updated successfuly.")
                }
                is ResultWrapper.Failure -> {
                    loading.value = NetworkState.FAILED
                    temp_showToast(result.errorMessage)
                }
            }
        }
    }


    fun submitPoll() {
        postPollloading.value = NetworkState.LOADING_STARTED
        viewModelScope.launch {
            val result = ForumRepository.submitPollForum(
                pollQuestion.value.toString(),
                option1.value.toString(),
                option2.value.toString(),
                option3.value.toString(),
                validity.value.toString()
            )
            postPollloading.value = NetworkState.LOADING_STOPPED
            when (result) {
                is ResultWrapper.Success -> {
                    temp_showToast("Forum added successfull")
                    postPollloading.value = NetworkState.SUCCESS
                }
                is ResultWrapper.Failure -> {
                    Log.d("Error", result.errorMessage)
                    postPollloading.value = NetworkState.FAILED
                }
            }
        }
    }

    fun getComments() {
        viewModelScope.launch {
            val result = ForumRepository.getComments(forumID.value.toString())
            when (result) {
                is ResultWrapper.Success -> {
                    val list = mutableListOf<CommentDTO>()
                    list.addAll(result.response.map { it })
                    commentlist.value = list
                }
                is ResultWrapper.Failure -> { }
            }
        }
    }
}