package com.ashomapp.presentation.forum

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentForumBinding
import com.ashomapp.databinding.SuscessDialogBinding
import com.ashomapp.network.repository.ForumRepository
import com.ashomapp.network.response.dashboard.ForumDTO
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.presentation.home.HomeViewModel
import com.ashomapp.utils.*
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ForumFrag : Fragment(), onForumClick {
    private lateinit var mBinding: FragmentForumBinding
    val homeviewmodel: HomeViewModel by viewModels()
    lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var Fadapter: ForumAdapter

    companion object {
        var forumid: String = ""
        var shareintentid: String = ""
        var scrollstatelistner = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentForumBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadingDialog()
        ApplyGTMEvent("forum_click", "forum click_count", "forum_click")


        HomeFlow.forumCurrentID = R.id.forumFrag
        mBinding.mtoolbar.mainBack.visibility = View.GONE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        MainActivity.intentforForum.value = ""
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            findNavController().navigate(R.id.action_forumFrag_to_settingFrag)
        }
        notificationcounter.observe(this.viewLifecycleOwner) {
            if (it != 0) {
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            } else {
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this.viewLifecycleOwner,
            onBackPressedCallback
        )
        mBinding.mtoolbar.icon.setOnClickListener {
            setanimation(it)
            if (HomeFlow.sectionBottomID == R.id.searchFrag && HomeFlow.searchcurrentFragID != R.id.searchFrag) {
                (requireActivity() as MainActivity).clicktoSearchFrag()
            } else if (HomeFlow.sectionBottomID == R.id.countryList && HomeFlow.financialcurrentFragID != R.id.countryList) {
                (requireActivity() as MainActivity).clicktoCountryList()
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag && HomeFlow.forumCurrentID != R.id.forumFrag) {
                (requireActivity() as MainActivity).clicktoForumTab()
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag && HomeFlow.newsFragCurrentID != R.id.newsFrag) {
                (requireActivity() as MainActivity).clicktoForumTab()
            } else {
                (requireActivity() as MainActivity).gotoHome()
            }
        }
        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            findNavController().navigate(R.id.notificationFrag)
        }
        if (HomeFlow.reloadForum) {
            homeviewmodel.getForum()
            HomeFlow.reloadForum = false
        }
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

        }
        /// getList()
        HomeFlow.mFforumlist.observe(viewLifecycleOwner) {
            Fadapter = ForumAdapter(it, this@ForumFrag)
            mBinding.recycleForum.adapter = Fadapter
            Fadapter.notifyDataSetChanged()
            mBinding.recycleForum.setHasFixedSize(true)
            mBinding.swipeForRefresh.isRefreshing = false

            if (Fadapter.itemCount >= scrollstatelistner) {
                if (scrollstatelistner == 0) {
                    mBinding.recycleForum.layoutManager?.scrollToPosition(scrollstatelistner)
                } else {
                    mBinding.recycleForum.layoutManager?.scrollToPosition(scrollstatelistner)

                }
            } else {
                scrollstatelistner = 0
            }
        }


        homeviewmodel.user_not_found.observe(this.viewLifecycleOwner) {
            if (it) {

                SharedPrefrenceHelper.devicetoken = "0"
                SharedPrefrenceHelper.token = "0"
                MainActivity.usertoken.value = ""
                val navOptions =
                    NavOptions.Builder().setPopUpTo(R.id.forumFrag, true).build()
                findNavController().navigate(R.id.loginFragment, null, navOptions)
                homeviewmodel.user_not_found.value = false

            }
        }
        mBinding.recycleForum.setHasFixedSize(true)
        //  VerticalOverScrollBounceEffectDecorator(RecyclerViewOverScrollDecorAdapter(mBinding.recycleForum))

        mBinding.addForumFab.setOnClickListener {
            create_forum()

        }

        mBinding.swipeForRefresh.setOnRefreshListener {
            homeviewmodel.getForum()
            scrollstatelistner = 0

        }

       //we can hide bell icon in case of small width
        // checkOverlap(mBinding.mtoolbar.icon, mBinding.mtoolbar.notificationBellIcon)

        mBinding.recycleForum.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                try {
                    var visibleChild: View = recyclerView.getChildAt(0)
                    val firstChild: Int = recyclerView.getChildAdapterPosition(visibleChild)

                    scrollstatelistner = firstChild
                } catch (e: Exception) {
                    Log.d("error", e.message.toString())
                }
            }
        })
    }

    private fun checkOverlap(view: View, view1: View) {
        if (view.isOverlap(view1)) {
        mBinding.mtoolbar.notificationBellIcon.apply {
            this.visibility=View.VISIBLE
        }
        }else{
            mBinding.mtoolbar.notificationBellIcon.apply {
                this.visibility=View.GONE
            }
        }
    }


    private fun create_forum() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomsheetview = LayoutInflater.from(requireContext())
            .inflate(
                R.layout.select_add_forum_type,
                view?.findViewById(R.id.bottom_sheet_forum)
            ) as LinearLayout
        val cv_compose_forum = bottomsheetview.findViewById<CardView>(R.id.forum_compose)
        val cv_poll_forum = bottomsheetview.findViewById<CardView>(R.id.forum_poll)
        bottomSheetDialog.setContentView(bottomsheetview)
        bottomSheetDialog.show()
        cv_compose_forum.setOnClickListener {
            ApplyGTMEvent(
                "created_compose_forum",
                "compose_forum_click_count",
                "created_compose_forum"
            )

            homeviewmodel.recordEvent("create_compose_forum")
            bottomSheetDialog.dismiss()
            findNavController().navigate(R.id.action_forumFrag_to_composeFrag)
        }
        cv_poll_forum.setOnClickListener {
            ApplyGTMEvent("created_poll_forum", "poll_forum_created_count", "created_poll_forum")

            homeviewmodel.recordEvent("create_poll_forum")
            bottomSheetDialog.dismiss()
            findNavController().navigate(R.id.action_forumFrag_to_pollForumFrag)
        }
    }

    override fun onOptionCLick(forumDTO: ForumDTO, option: String) {
        if (!forumDTO.voted) {
            lifecycleScope.launch {
                val result = ForumRepository.sendVote(forumDTO.id, option)
                when (result) {
                    is ResultWrapper.Success -> {
                        forumDTO.voted = true
                    }
                    is ResultWrapper.Failure -> {}
                }
            }
        } else {
            temp_showToast("Poll already voted by this user") //you  are already voted to this poll
        }
    }

    override fun onCommentClick(forumDTO: ForumDTO, position: Int) {
        val mforumdto = Gson().toJson(forumDTO)
        val args = bundleOf("forumdto" to mforumdto)
        scrollstatelistner = position
        HomeFlow.forumCommentBundle = args
        findNavController().navigate(R.id.action_forumFrag_to_forumComments, args)
    }

    override fun ondisLikeunDislike(forumDTO: ForumDTO) {
        lifecycleScope.launch {
            val result = ForumRepository.dislikeorundislike(forumDTO.id)
            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("dislikelike", result.response.toString())
                    if (result.response.message.equals("Forum UnDisliked Successfully.")) {
                        forumDTO.total_disliked = (forumDTO.total_disliked.toInt() - 1).toString()
                        forumDTO.disliked = false
                    } else {
                        forumDTO.total_disliked = (forumDTO.total_disliked.toInt() + 1).toString()
                        forumDTO.disliked = true
                        if (forumDTO.liked) {
                            forumDTO.liked = false
                            forumDTO.total_liked = (forumDTO.total_liked.toInt() - 1).toString()
                        }
                    }
                    Fadapter.notifyDataSetChanged()
                }
                is ResultWrapper.Failure -> {}
            }
        }
    }

    override fun onLikeunlike(forumDTO: ForumDTO) {
        lifecycleScope.launch {
            val result = ForumRepository.likeorunlike(forumDTO.id)
            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("dislikelike", result.response.toString())
                    if (result.response.message.equals("Forum Liked Successfully.")) {
                        forumDTO.total_liked = (forumDTO.total_liked.toInt() + 1).toString()
                        forumDTO.liked = true
                        if (forumDTO.disliked) {
                            forumDTO.disliked = false
                            forumDTO.total_disliked =
                                (forumDTO.total_disliked.toInt() - 1).toString()
                        }
                    } else {
                        forumDTO.total_liked = (forumDTO.total_liked.toInt() - 1).toString()
                        forumDTO.liked = false
                    }
                    Fadapter.notifyDataSetChanged()
                }
                is ResultWrapper.Failure -> {}
            }
        }
    }

    override fun onShareClick(forumDTO: ForumDTO, position: Int) {
        shareintentid = forumDTO.id
        Log.d("forum", "share")
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        scrollstatelistner = position
        val adas = forumDTO.content.split("@").toTypedArray()
        try {
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "Hi, I am using Ashom.app. Feel free to download ( https://play.google.com/store/apps/details?id=com.ashomapp )  \n\nNews:\n${adas[0].trim()} ${adas[1]}"
            )

        } catch (e: Exception) {
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "Hi, I am using Ashom.app. Feel free to download ( https://play.google.com/store/apps/details?id=com.ashomapp ) \n\nNews:\n${forumDTO.content}"
            )

        }
        startActivityForResult(Intent.createChooser(intent, "Share with:"), 110)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 110) {
            if (resultCode == RESULT_OK) {
                Log.d("sharingintent", "Sharing")
                lifecycleScope.launch {
                    val result = ForumRepository.forumShare(shareintentid)
                    when (result) {
                        is ResultWrapper.Success -> {
                            homeviewmodel.getForum()
                            Log.d("forums", result.response.message)
                        }
                        is ResultWrapper.Failure -> {
                            Log.d("forums", result.errorMessage)
                        }
                    }
                }
            } else {
                shareintentid = ""
            }
        }
    }

    override fun onForumDetail(forumDTO: ForumDTO, position: Int) {
        val adas = forumDTO.content.split("@").toTypedArray()
        val newsItemDTO = NewsItemDTO(
            metadata = "${forumDTO.metadata}",
            title = "${adas[0]}",
            image_url = "${forumDTO.content}",
            link = try {
                "${adas[1]}"
            } catch (e: Exception) {
                ""
            }
        )
        scrollstatelistner = position
        val newstostring = Gson().toJson(newsItemDTO)
        val args = bundleOf("Newsdetail" to newstostring)
        HomeFlow.forumtonewsDetailBundle = args
        findNavController().navigate(R.id.newsDetail, args)
    }

    override fun onEditForum(forumDTO: ForumDTO) {
        HomeFlow.reloadForum = true
        val companydetail = Gson().toJson(forumDTO)
        val args = bundleOf("edit_forum" to companydetail)
        findNavController().navigate(R.id.composeFrag, args)
    }

    override fun onDeleteForum(forumDTO: ForumDTO, position: Int) {
        deleteForumPopUp(forumDTO, position)
    }


    private fun deleteForumPopUp(forumDTO: ForumDTO, position: Int) {
        val Binding = SuscessDialogBinding.inflate(LayoutInflater.from(requireActivity()))
        val builder = Dialog(requireActivity())
        Binding.dialogTitle.text = "Alert!"
        Binding.dialogSubTitle.text = "Are you sure? You Want To Delete This Post. "
        Binding.dialogOk.text = "Confirm"
        Binding.dialogOk.setOnClickListener {
            Binding.dialogOk.setBackgroundColor(Color.parseColor("#BFDDED"))
            Handler().postDelayed({
                deleteForum(forumDTO, position)
                builder.dismiss()
                loadingEnableDisable.value = true

            }, 50)
        }
        Binding.dialogCancel.text = "Cancel"
        Binding.dialogCancel.setOnClickListener {
            Binding.dialogCancel.setBackgroundColor(Color.parseColor("#BFDDED"))
            Handler().postDelayed({
                builder.dismiss()
            }, 50)

        }
        builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
        builder.setContentView(Binding.root)
        builder.setCancelable(false)
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

    private fun deleteForum(forumDTO: ForumDTO, position: Int) {
        lifecycleScope.launch {
            val result = ForumRepository.forumDelete(forumDTO.id)
            when (result) {
                is ResultWrapper.Success -> {
                    loadingEnableDisable.value = false
                    temp_showToast("Forum deleted successfully.")
                    ApplyGTMEvent("deleted_forum", "deleted_forum_click_count", "deleted_forum")
                    Fadapter.notifyItemRemoved(position)
                    HomeFlow.reloadForum = true
                }
                is ResultWrapper.Failure -> {
                    loadingEnableDisable.value = false
                    temp_showToast("${result.errorMessage}")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

    }

}

