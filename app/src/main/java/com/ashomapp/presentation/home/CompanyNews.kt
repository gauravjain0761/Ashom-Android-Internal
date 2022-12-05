package com.ashomapp.presentation.home

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentCompanyNewsBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.presentation.news.NewsPagingAdapter
import com.ashomapp.presentation.news.NewsViewModel
import com.ashomapp.presentation.news.onNewItemClick
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.notificationcounter
import com.ashomapp.utils.setanimation
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter


class CompanyNews : Fragment(), onNewItemClick {

     private lateinit var mBinding : FragmentCompanyNewsBinding
    private val mNewsViewModel by activityViewModels<NewsViewModel>()
    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    private val  newsAdapter = NewsPagingAdapter(this)
    private var job: Job? = null
    private fun loadNews(country_name: String, companyname : String) {
        job?.cancel()

        job = lifecycleScope.launch {
            mNewsViewModel.getCompanyNews(country_name, companyname).collectLatest {
                newsAdapter.submitData(it)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentCompanyNewsBinding.inflate(layoutInflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (HomeFlow.profilefragenable){

        }
        else if (HomeFlow.sectionBottomID == R.id.homeFrag){
            HomeFlow.currentFragID = R.id.companyNews
        }else if (HomeFlow.sectionBottomID == R.id.countryList){
            HomeFlow.financialcurrentFragID = R.id.companyNews
        }else if (HomeFlow.sectionBottomID == R.id.searchFrag){
            HomeFlow.searchcurrentFragID = R.id.companyNews
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (HomeFlow.profilefragenable){
                    if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                        findNavController().navigate(R.id.companyDetail,  HomeFlow.home_selectedCompanytoComapnyDetail)
                    } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                        findNavController().navigate(R.id.companyDetail,  HomeFlow.financial_selectedCompanytoComapnyDetail)
                    } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                        findNavController().navigate(R.id.companyDetail,  HomeFlow.forum_selectedCompanytoComapnyDetail)
                    } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                        findNavController().navigate(R.id.companyDetail,  HomeFlow.news_selectedCompanytoComapnyDetail)
                    } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                        findNavController().navigate(R.id.companyDetail,  HomeFlow.search_selectedCompanytoComapnyDetail)
                    }
                }else if (HomeFlow.sectionBottomID == R.id.homeFrag){
                    findNavController().navigate(R.id.companyDetail,  HomeFlow.companydetailbundle)
                }else if (HomeFlow.sectionBottomID == R.id.searchFrag){
                    findNavController().navigate(R.id.companyDetail,  HomeFlow.searchCountrydetailbundle)
                }else if (HomeFlow.sectionBottomID == R.id.countryList){
                    findNavController().navigate(R.id.companyDetail,  HomeFlow.financialcompanydetailbundle)
                }
                else{
                    findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this.viewLifecycleOwner, onBackPressedCallback)

        mBinding.mtoolbar.mainBack.setOnClickListener {
            ObjectAnimator.ofFloat(mBinding.mtoolbar.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                       if (HomeFlow.profilefragenable){
                        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                            findNavController().navigate(R.id.companyDetail,  HomeFlow.home_selectedCompanytoComapnyDetail)
                        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                            findNavController().navigate(R.id.companyDetail,  HomeFlow.financial_selectedCompanytoComapnyDetail)
                        } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                            findNavController().navigate(R.id.companyDetail,  HomeFlow.forum_selectedCompanytoComapnyDetail)
                        } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                            findNavController().navigate(R.id.companyDetail,  HomeFlow.news_selectedCompanytoComapnyDetail)
                        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                            findNavController().navigate(R.id.companyDetail,  HomeFlow.search_selectedCompanytoComapnyDetail)
                        }
                    }else if (HomeFlow.sectionBottomID == R.id.homeFrag){
                            findNavController().navigate(R.id.companyDetail,  HomeFlow.companydetailbundle)
                        }else if (HomeFlow.sectionBottomID == R.id.searchFrag){
                            findNavController().navigate(R.id.companyDetail,  HomeFlow.searchCountrydetailbundle)
                        }else if (HomeFlow.sectionBottomID == R.id.countryList){
                            findNavController().navigate(R.id.companyDetail,  HomeFlow.financialcompanydetailbundle)
                        }
                        else{
                            findNavController().navigateUp()
                        }
                    }
                }
        }
        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext,it)

            findNavController().navigate(R.id.notificationFrag)
        }
        notificationcounter.observe(this.viewLifecycleOwner){
            if (it != 0){
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            }else{
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }
        mBinding.mtoolbar.icon.setOnClickListener {
            setanimation(it)
            (requireActivity() as MainActivity).gotoHome()
        }
        mBinding.companynewsRv.adapter = newsAdapter
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            findNavController().navigate(R.id.action_companyNews_to_settingFrag)
        }
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()){
            Glide.with(AshomAppApplication.instance.applicationContext).load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

        }

        val args = requireArguments().getString("company_news_detail")
        if (!args.isNullOrEmpty()){
            val companydetail = Gson().fromJson(args, CompanyDTO::class.java)
            loadNews(country_name = companydetail.Country, companyname = companydetail.SymbolTicker)
        }

        newsAdapter.apply {
            registerAdapterDataObserver(object  : RecyclerView.AdapterDataObserver(){
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    if (itemCount == 0 && positionStart == 0){
                        mBinding.noNewsFound.visibility = View.VISIBLE
                    }else{
                        mBinding.noNewsFound.visibility = View.GONE
                    }
                }
            })
        }

        VerticalOverScrollBounceEffectDecorator(RecyclerViewOverScrollDecorAdapter(mBinding.companynewsRv))

    }

    override fun onNavigatetoDetail(newsItemDTO: NewsItemDTO, position: Int) {
        val newstostring = Gson().toJson(newsItemDTO)
        val args = bundleOf("Newsdetail" to newstostring)
        if (HomeFlow.sectionBottomID == R.id.homeFrag){
            HomeFlow.hometonewsdetailbundle = args
            HomeFlow.home_companynew_detail = true
        }else if (HomeFlow.sectionBottomID == R.id.searchFrag){
            HomeFlow.hometonewsdetailbundle = args
            HomeFlow.search_companynew_detail = true
        }

        findNavController().navigate(R.id.newsDetail, args)
    }

    override fun onShare(newsItemDTO: NewsItemDTO, position: Int) {
        mHomeViewModel.recordEvent("news_share")
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "Hi, I am using Ashom.app. Feel free to download ( https://play.google.com/store/apps/details?id=com.ashomapp ) \n\nNews: \n${newsItemDTO.title}\n${newsItemDTO.link}")
        startActivity(Intent.createChooser(intent, "Share with:"))
    }

    override fun addToForum(newsItemDTO: NewsItemDTO, position: Int) {
        mHomeViewModel.recordEvent("create_news_forum")
        val companydetail = Gson().toJson(newsItemDTO)
        val args = bundleOf("news_item_to_forum" to companydetail)
         findNavController().navigate(R.id.composeFrag, args)
    }


}