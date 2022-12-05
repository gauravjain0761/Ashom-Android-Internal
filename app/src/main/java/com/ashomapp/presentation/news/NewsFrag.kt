package com.ashomapp.presentation.news

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentNewsBinding
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.ashomapp.network.response.dashboard.NewsItemDTO

import com.ashomapp.presentation.home.*
import com.ashomapp.presentation.home.Company_frag.Companion.selectedItem
import com.ashomapp.utils.*
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter

class NewsFrag : Fragment(), onNewItemClick, onCountryClick {

    private lateinit var mBinding: FragmentNewsBinding
    private val mNewsViewModel by activityViewModels<NewsViewModel>()
    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    private val newsAdapter = NewsPagingAdapter(this)
    private lateinit var CountryAdapter: CountryAdapter
    private var job: Job? = null
    private fun loadNews(country_name: String, search: String? = null) {
        job?.cancel()
        job = lifecycleScope.launch {
            mNewsViewModel.getNewsItem(country_name, search = search).collectLatest {
                newsAdapter.submitData(it)

            }
        }
    }

    companion object {
        var country_name: String = ""
        var nscrollstatelistner = 0

        var country_name_home : String = ""
        var country_name_news : String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (country_name.isNullOrEmpty()) {
            loadNews("")
            mHomeViewModel.getCountry()
        } else {
            if (HomeFlow.sectionBottomID == R.id.homeFrag){
                loadNews(country_name_home)
            }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
                loadNews(country_name_news)
            }


        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentNewsBinding.inflate(layoutInflater, container, false).apply {
            if (country_name.isNullOrEmpty()) {
                isSelected = true
            }
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if ( HomeFlow.sectionBottomID == R.id.newsFrag){
            HomeFlow.newsFragCurrentID = R.id.newsFrag
            country_name = country_name_news
            mHomeViewModel.getCountry()
            mBinding.mtoolbar.mainBack.visibility = View.GONE
        }else if (HomeFlow.sectionBottomID == R.id.homeFrag){
            HomeFlow.currentFragID = R.id.newsFrag
            HomeFlow.home_to_news = true
            country_name = country_name_home
            mHomeViewModel.getCountry()
            mBinding.mtoolbar.mainBack.visibility = View.VISIBLE
        }

      //  loadingDialog()  //for loading dialog
        loadingEnableDisable.value = false
         mBinding.mtoolbar.mainBack.setOnClickListener {
             ObjectAnimator.ofFloat(mBinding.mtoolbar.mainBackView, "alpha", 0f, 1f, 0f)
                 .apply {
                     duration = 100
                     start()
                     doOnEnd {
                         if (HomeFlow.sectionBottomID == R.id.homeFrag){
                             findNavController().navigate(R.id.homeFrag)
                         }

                     }
                 }
         }
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)

            findNavController().navigate(R.id.action_newsFrag_to_settingFrag)
        }

        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)

            findNavController().navigate(R.id.notificationFrag)
        }
        notificationcounter.observe(this.viewLifecycleOwner) {
            if (it != 0) {
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            } else {
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }
        mBinding.mtoolbar.icon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)

            setanimation(it)
            (requireActivity() as MainActivity).gotoHome()
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
                if (HomeFlow.sectionBottomID == R.id.homeFrag){
                    findNavController().navigate(R.id.homeFrag)
                }else{
                    requireActivity().finish()
                }

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this.viewLifecycleOwner,
            onBackPressedCallback
        )
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

        }

        if (country_name.isNullOrEmpty()) {
            mBinding.allNews.getBackground()
                .setColorFilter(Color.parseColor("#72c4f1"), PorterDuff.Mode.SRC_ATOP);
            Company_frag.selectedItem = -1
        }

        mBinding.newRv.adapter = newsAdapter

        mHomeViewModel.countrylist.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                CountryAdapter = CountryAdapter(it, this)
                mBinding.countryRv.adapter = CountryAdapter
                it.forEachIndexed { index, countriesDTO ->
                    if (countriesDTO.country.equals(country_name)){
                        Company_frag.selectedItem = index
                        CountryAdapter.notifyDataSetChanged()
                        mBinding.countryRv.scrollToPosition(index)
                    }
                }
                //   Company_frag.selectedItem = -1
            }
        }
        VerticalOverScrollBounceEffectDecorator(RecyclerViewOverScrollDecorAdapter(mBinding.newRv))

        mBinding.allNews.setOnClickListener {
            if (mBinding.etNewsSearch.isFocused && mBinding.etNewsSearch.text.isNullOrEmpty()) {
                mBinding.etNewsSearch.clearFocus()
                hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
            } else {
                mBinding.allNews.getBackground()
                    .setColorFilter(Color.parseColor("#72c4f1"), PorterDuff.Mode.SRC_ATOP);
                loadNews("")
                nscrollstatelistner = 0
                country_name = ""
                selectedItem = -1
                CountryAdapter.notifyDataSetChanged()
            }
        }
        newsAdapter.apply {
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    /*Handler().postDelayed({
                        loadingEnableDisable.value = false
                    }, 550)*/
                    if (newsAdapter.itemCount >= nscrollstatelistner) {
                        if (nscrollstatelistner == 0) {
                            mBinding.newRv.layoutManager?.scrollToPosition(nscrollstatelistner)
                        } else {
                            mBinding.newRv.layoutManager?.scrollToPosition(nscrollstatelistner)
                        }

                    } else {
                        nscrollstatelistner = 0
                    }
                }
            })

        }
        mBinding.root.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            mBinding.etNewsSearch.clearFocus()

        }
        mBinding.etNewsSearch.doOnTextChanged { text, start, before, count ->
            if (text?.length != 0) {
                loadNews(country_name, text.toString())
                nscrollstatelistner = 0
                mBinding.homeSearchIcon.setImageResource(R.drawable.ic_close_search)
            } else {
                loadNews(country_name)
                // nscrollstatelistner = 0
                mBinding.homeSearchIcon.setImageResource(R.drawable.search_icon)
            }
        }

        mBinding.newRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
               try {
                   var visibleChild: View = recyclerView.getChildAt(0)
                   val firstChild: Int = recyclerView.getChildAdapterPosition(visibleChild)
                   nscrollstatelistner = firstChild
               }catch (e :Exception){
                   e.printStackTrace()
               }
            }
        })
        mBinding.homeSearchIcon.setOnClickListener {

            if (mBinding.etNewsSearch.text.length > 0) {
                mBinding.etNewsSearch.setText("")
                mBinding.etNewsSearch.clearFocus()
                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            }
        }


    }

    override fun onNavigatetoDetail(newsItemDTO: NewsItemDTO, position: Int) {
        // temp_showToast("navigate to detail")
        if (mBinding.etNewsSearch.isFocused && mBinding.etNewsSearch.text.isNullOrEmpty()) {
            mBinding.etNewsSearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
        } else {
            nscrollstatelistner = position
            mBinding.etNewsSearch.setText("")
            mBinding.etNewsSearch.clearFocus()
            val newstostring = Gson().toJson(newsItemDTO)
            val args = bundleOf("Newsdetail" to newstostring)
             if (HomeFlow.sectionBottomID == R.id.newsFrag){
                 HomeFlow.newDetailBundle = args
             }else if (HomeFlow.sectionBottomID == R.id.homeFrag){
                 HomeFlow.hometonewsdetailbundle = args
             }

            findNavController().navigate(R.id.newsDetail, args)
        }
    }

    override fun onShare(newsItemDTO: NewsItemDTO, position: Int) {
        if (mBinding.etNewsSearch.isFocused && mBinding.etNewsSearch.text.isNullOrEmpty()) {
            mBinding.etNewsSearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)

        } else {

            mHomeViewModel.recordEvent("news_share")
            mBinding.etNewsSearch.setText("")
            mBinding.etNewsSearch.clearFocus()
            nscrollstatelistner = position
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "Hi, I am using Ashom.app. Feel free to download ( https://play.google.com/store/apps/details?id=com.ashomapp ) \n\nNews: \n${newsItemDTO.title}\n${newsItemDTO.link}"
            )
            startActivity(Intent.createChooser(intent, "Share with:"))
        }
    }

    override fun addToForum(newsItemDTO: NewsItemDTO, position: Int) {
        if (mBinding.etNewsSearch.isFocused && mBinding.etNewsSearch.text.isNullOrEmpty()) {
            mBinding.etNewsSearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
        } else {
            mHomeViewModel.recordEvent("create_news_forum")
            mBinding.etNewsSearch.setText("")
            mBinding.etNewsSearch.clearFocus()
            nscrollstatelistner = position
            val companydetail = Gson().toJson(newsItemDTO)
            val args = bundleOf("news_item_to_forum" to companydetail)

            findNavController().navigate(R.id.composeFrag, args)
        }
    }

    override fun onCountryClick(countriesDTO: CountriesDTO, view: View, position: Int) {
        if (mBinding.etNewsSearch.isFocused && mBinding.etNewsSearch.text.isNullOrEmpty()) {
            mBinding.etNewsSearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
        } else {
            mBinding.etNewsSearch.setText("")
            mBinding.allNews.getBackground().setColorFilter(null);
            mBinding.etNewsSearch.clearFocus()
            nscrollstatelistner = 0

            onNewsCountryClick(countriesDTO.country)
            if (countriesDTO.country.equals("All Countries")) {
                loadNews("")
                country_name = ""
                if (HomeFlow.sectionBottomID == R.id.homeFrag){
                    country_name_home = ""
                }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
                    country_name_news = ""
                }
            } else {
                if (HomeFlow.sectionBottomID == R.id.homeFrag){
                    country_name_home = countriesDTO.country
                }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
                    country_name_news = countriesDTO.country
                }

                loadNews(countriesDTO.country)
                country_name = countriesDTO.country
            }
        }
    }

    private fun onNewsCountryClick(country: String) {
        if (country.equals("KSA")) {
            mHomeViewModel.recordEvent("view_ksa_news")
        } else if (country.equals("Kuwait")) {
            mHomeViewModel.recordEvent("view_kuwait_news")
        } else if (country.equals("Qatar")) {
            mHomeViewModel.recordEvent("view_qatar_news")
        } else if (country.equals("Oman")) {
            mHomeViewModel.recordEvent("view_oman_news")
        } else if (country.equals("UAE")) {
            mHomeViewModel.recordEvent("view_uae_news")
        } else if (country.equals("Bahrain")) {
            mHomeViewModel.recordEvent("view_bahrain_news")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!country_name.isNullOrEmpty()) {
            CountryAdapter.notifyDataSetChanged()

        }
    }

    override fun onPause() {
        super.onPause()
    }

}