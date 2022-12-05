package com.ashomapp.presentation.news

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentNewsDetailBinding
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.presentation.home.HomeViewModel
import com.ashomapp.utils.*
import com.ashomapp.utils.NewLodingDialog.hidenewloading
import com.ashomapp.utils.NewLodingDialog.shownewsloading
import com.bumptech.glide.Glide
import com.google.gson.Gson


class NewsDetail : Fragment() {

     private lateinit var mBinding : FragmentNewsDetailBinding
       var loadingDialog = false
    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        mBinding = FragmentNewsDetailBinding.inflate(layoutInflater, container, false).apply {
            loading = loadingDialog
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
         MainActivity.intentforNews.value = ""
          mBinding.loading = true
        mHomeViewModel.recordEvent("view_news_detail_click")
        loadImageFromUrl(mBinding.loadingDialogdd.progressLoadingImg, 0)
        if (HomeFlow.notificationfragenable == true){
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.hometonotification = true
                HomeFlow.hometoNotificationCurrentFragID = R.id.newsDetail
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.countrylist_to_notification = true
                HomeFlow.financialtoNotificationCurrentFragID = R.id.newsDetail
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                HomeFlow.fourm_to_notification = true
                HomeFlow.forumtoNotificationCurrentFragID = R.id.newsDetail
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                HomeFlow.news_to_notification = true
                HomeFlow.newstoNotificationCurrentFragID = R.id.newsDetail
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                HomeFlow.search_to_notification = true
                HomeFlow.searchtoNotificationCurrentFragID = R.id.newsDetail
            }
        }else if (HomeFlow.profilefragenable){

        }
        else if (HomeFlow.sectionBottomID == R.id.forumFrag){
            HomeFlow.forumCurrentID = R.id.newsDetail
        }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
            HomeFlow.newsFragCurrentID = R.id.newsDetail
        }else if (HomeFlow.sectionBottomID == R.id.homeFrag){
            HomeFlow.currentFragID = R.id.newsDetail
        }
        notificationcounter.observe(this.viewLifecycleOwner){
            if (it != 0){
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            }else{
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (HomeFlow.notificationfragenable){
                    findNavController().navigate(R.id.notificationFrag)
                    HomeFlow.notificationfragenable = false
                }else if (HomeFlow.home_companynew_detail == true){
                    findNavController().navigate(R.id.companyNews, HomeFlow.homecompanynewsBundle)
                    HomeFlow.home_companynew_detail = false
                }else if (HomeFlow.search_companynew_detail == true){
                    findNavController().navigate(R.id.companyNews, HomeFlow.seacrhcompanynewsBundle)
                    HomeFlow.search_companynew_detail = false
                }else if (HomeFlow.sectionBottomID == R.id.forumFrag){
                    findNavController().navigate(R.id.forumFrag)
                }else if (HomeFlow.sectionBottomID == R.id.homeFrag && !HomeFlow.home_to_news){
                       findNavController().navigate(R.id.companyDetail, HomeFlow.companydetailbundle)
                }else if (HomeFlow.sectionBottomID == R.id.countryList){
                    findNavController().navigate(R.id.companyDetail, HomeFlow.financialcompanydetailbundle)
                }else if (HomeFlow.sectionBottomID == R.id.searchFrag){
                    findNavController().navigate(R.id.companyDetail, HomeFlow.searchCountrydetailbundle)
                }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
                    findNavController().navigate(R.id.newsFrag)
                }else if (HomeFlow.sectionBottomID == R.id.homeFrag){
                    findNavController().navigate(R.id.newsFrag)
                }

                else{
                    findNavController().navigateUp()
                }

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


        mBinding.mtoolbar.mainBack.setOnClickListener {
            ObjectAnimator.ofFloat(mBinding.mtoolbar.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        if (HomeFlow.notificationfragenable){
                            findNavController().navigate(R.id.notificationFrag)
                            HomeFlow.notificationfragenable = false
                        }
                        else if (HomeFlow.home_companynew_detail == true){
                            findNavController().navigate(R.id.companyNews, HomeFlow.homecompanynewsBundle)
                            HomeFlow.home_companynew_detail = false
                        }else if (HomeFlow.search_companynew_detail == true){
                            findNavController().navigate(R.id.companyNews, HomeFlow.seacrhcompanynewsBundle)
                            HomeFlow.search_companynew_detail = false
                        }else if (HomeFlow.sectionBottomID == R.id.forumFrag){
                            findNavController().navigate(R.id.forumFrag)
                        }else if (HomeFlow.sectionBottomID == R.id.homeFrag && !HomeFlow.home_to_news){
                            findNavController().navigate(R.id.companyDetail, HomeFlow.companydetailbundle)
                        }else if (HomeFlow.sectionBottomID == R.id.countryList){
                            findNavController().navigate(R.id.companyDetail, HomeFlow.financialcompanydetailbundle)
                        }
                        else if (HomeFlow.sectionBottomID == R.id.searchFrag){
                            findNavController().navigate(R.id.companyDetail, HomeFlow.searchCountrydetailbundle)
                        }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
                            findNavController().navigate(R.id.newsFrag)
                        }else if (HomeFlow.sectionBottomID == R.id.homeFrag){
                            findNavController().navigate(R.id.newsFrag)
                        }
                        else{
                            findNavController().navigateUp()
                        }

                    }
                }
        }
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            findNavController().navigate(R.id.action_newsDetail_to_settingFrag)
        }
        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext,it)

            findNavController().navigate(R.id.notificationFrag)
        }
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()){
            Glide.with(AshomAppApplication.instance.applicationContext).load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

        }


   val webView = mBinding.newsWebview
        val webViewClient: WebViewClient = object: WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                mBinding.loading = true
                Handler().postDelayed({
                    mBinding.loading = false
                },1500)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                mBinding.loading = false
                super.onPageFinished(view, url)
            }
        }

        webView.webViewClient = webViewClient
        webView.setBackgroundResource(R.drawable.page_bg);
        val newItem = requireArguments().getString("Newsdetail")
        if (!newItem.isNullOrEmpty()){
            // this will load the url of the website
            val newItemDto = Gson().fromJson(newItem, NewsItemDTO::class.java)

            mBinding.itemNwesInShare.setOnClickListener {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.type = "text/plain"
                mHomeViewModel.recordEvent("news_share")
                    intent.putExtra(Intent.EXTRA_TEXT, "Hi, I am using Ashom.app. Feel free to download ( https://play.google.com/store/apps/details?id=com.ashomapp ) \n\nNews: \n${newItemDto.title}\n${newItemDto.link}")
                    startActivity(Intent.createChooser(intent, "Share with:"))
                }
            mBinding.itemNewsShareInForum.setOnClickListener {
                val companydetail = Gson().toJson(newItemDto)
                val args = bundleOf("news_item_to_forum" to companydetail)
                mHomeViewModel.recordEvent("create_news_forum")
                findNavController().navigate(R.id.composeFrag, args)
            }
            webView.loadUrl(newItemDto.link)

            // this will enable the javascript settings
            webView.settings.javaScriptEnabled = true

            // if you want to enable zoom feature
            webView.settings.setSupportZoom(true)
        }

    }



}