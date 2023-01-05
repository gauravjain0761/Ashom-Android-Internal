package com.ashomapp.presentation.notification

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentNotificationBinding
import com.ashomapp.network.response.NOtificationDataNews
import com.ashomapp.network.response.NotificationDTO
import com.ashomapp.network.response.NotificationData
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.home.FinancialStatementfrag
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.utils.ApplyGTMEvent
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.notificationcounter
import com.ashomapp.utils.setanimation
import com.bumptech.glide.Glide
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter


class NotificationFrag : Fragment(), onNotificationClick {
    private lateinit var mBinding: FragmentNotificationBinding
    val notificationlist = MutableLiveData<List<NotificationDTO>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentNotificationBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ApplyGTMEvent("notification_click","notification_click_count","notification_click")


        HomeFlow.notificationfragenable = true


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
        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            HomeFlow.hometonotification = true
            HomeFlow.hometoNotificationCurrentFragID = R.id.notificationFrag
        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
            HomeFlow.countrylist_to_notification = true
            HomeFlow.financialtoNotificationCurrentFragID = R.id.notificationFrag
        } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
            HomeFlow.fourm_to_notification = true
            HomeFlow.forumtoNotificationCurrentFragID = R.id.notificationFrag
        } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
            HomeFlow.news_to_notification = true
            HomeFlow.newstoNotificationCurrentFragID = R.id.notificationFrag
        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
            HomeFlow.search_to_notification = true
            HomeFlow.searchtoNotificationCurrentFragID = R.id.notificationFrag
        }

        mBinding.mtoolbar.mainBack.setOnClickListener {
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.hometonotification = false
                (requireActivity() as MainActivity).gotoHome()
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.countrylist_to_notification = false
                (requireActivity() as MainActivity).clicktoCountryList()
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                HomeFlow.fourm_to_notification = false
                (requireActivity() as MainActivity).clicktoForumTab()
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                HomeFlow.search_to_notification = false
                (requireActivity() as MainActivity).clicktoSearchFrag()
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                HomeFlow.news_to_notification = false
                (requireActivity() as MainActivity).gotoNewsTab()
            }
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.hometonotification = false
                    (requireActivity() as MainActivity).gotoHome()
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.countrylist_to_notification = false
                    (requireActivity() as MainActivity).clicktoCountryList()
                } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                    HomeFlow.fourm_to_notification = false
                    (requireActivity() as MainActivity).clicktoForumTab()
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    HomeFlow.search_to_notification = false
                    (requireActivity() as MainActivity).clicktoSearchFrag()
                } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                    HomeFlow.news_to_notification = false
                    (requireActivity() as MainActivity).gotoNewsTab()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this.viewLifecycleOwner,
            onBackPressedCallback
        )
        mBinding.mtoolbar.icon.setOnClickListener {
            setanimation(it)
            if (HomeFlow.sectionBottomID == R.id.searchFrag && HomeFlow.searchcurrentFragID != R.id.searchFrag
                || HomeFlow.search_to_profile || HomeFlow.search_to_notification) {
                HomeFlow.search_to_profile = false
                HomeFlow.search_to_notification = false
                (requireActivity() as MainActivity).clicktoSearchFrag()
            } else if (HomeFlow.sectionBottomID == R.id.countryList && HomeFlow.financialcurrentFragID != R.id.countryList
                || HomeFlow.financial_to_profile || HomeFlow.countrylist_to_notification) {
                HomeFlow.financial_to_profile = false
                HomeFlow.countrylist_to_notification = false
                (requireActivity() as MainActivity).clicktoCountryList()
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag && HomeFlow.forumCurrentID != R.id.forumFrag
                || HomeFlow.forum_to_profile || HomeFlow.fourm_to_notification) {
                HomeFlow.forum_to_profile = false
                HomeFlow.fourm_to_notification = false
                (requireActivity() as MainActivity).clicktoForumTab()
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag && HomeFlow.newsFragCurrentID != R.id.newsFrag
                || HomeFlow.news_to_profile || HomeFlow.news_to_notification) {
                HomeFlow.news_to_profile = false
                HomeFlow.news_to_notification = false
                (requireActivity() as MainActivity).gotoNewsTab()
            } else {
                HomeFlow.home_to_profile = false
                HomeFlow.hometonotification = false
                (requireActivity() as MainActivity).gotoHome()
            }


        }
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            findNavController().navigate(R.id.action_notificationFrag_to_profileFragment)
        }
        getNotification()
        notificationlist.observe(this.viewLifecycleOwner) {
            val adapter = NotificationAdapter(it, this)
            mBinding.notificationRv.adapter = adapter
            adapter.notifyDataSetChanged()
        }
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)
        }
        VerticalOverScrollBounceEffectDecorator(RecyclerViewOverScrollDecorAdapter(mBinding.notificationRv))

    }

    private fun getNotification() {
        lifecycleScope.launch {
            val result = HomeRepository.getNotification(SharedPrefrenceHelper.token.toString())
            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("notificationlog", result.response.toString())
                    val list = mutableListOf<NotificationDTO>()
                    list.addAll(result.response.map { it })
                    notificationlist.value = list
                }
                is ResultWrapper.Failure -> {
                    Log.d("notificationlog", result.errorMessage)
                }
            }
        }
    }

    override fun onNotificationClick(notificationDTO: NotificationDTO) {
        HomeFlow.notificationfragenable = true


        if (notificationDTO.metadata.type.equals("Financial Report")) {
            val newsitem = Gson().fromJson(
                notificationDTO.metadata.data[0].toString(),
                NotificationData::class.java
            )

            Log.d("notificationcom", newsitem.toString())

            if (newsitem.period.equals("Annual")) {
                FinancialStatementfrag.mNotificationPeriodselection = 0
            } else if (newsitem.period.equals("Q1")) {
                FinancialStatementfrag.mNotificationPeriodselection = 1
            } else if (newsitem.period.equals("Q2")) {
                FinancialStatementfrag.mNotificationPeriodselection = 2
            } else if (newsitem.period.equals("Q3")) {
                FinancialStatementfrag.mNotificationPeriodselection = 3
            } else if (newsitem.period.equals("Q4")) {
                FinancialStatementfrag.mNotificationPeriodselection = 4
            }

            FinancialStatementfrag.mNotificationPeriodselectionval = newsitem.period.trim()
            FinancialStatementfrag.mNotificationSelectedYear = newsitem.year.trim()
            val companydetail1 = Gson().toJson(newsitem.companypayload)
            val args = bundleOf("company_finance_detail" to companydetail1)

            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.hometoNotification_to_Financailstatement_Bundle = args
                if (newsitem.period.equals("Annual")) {
                    HomeFlow.not_mHomePeriodselection = 0
                } else if (newsitem.period.equals("Q1")) {
                    HomeFlow.not_mHomePeriodselection = 1
                } else if (newsitem.period.equals("Q2")) {
                    HomeFlow.not_mHomePeriodselection = 2
                } else if (newsitem.period.equals("Q3")) {
                    HomeFlow.not_mHomePeriodselection = 3
                } else if (newsitem.period.equals("Q4")) {
                    HomeFlow.not_mHomePeriodselection = 4
                }

                HomeFlow.not_mHomePeriodselectionval = newsitem.period.trim()
                HomeFlow.not_mHomeSelectedYear = newsitem.year.trim()

            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.financialtoNotification_to_Financailstatement_Bundle = args
                if (newsitem.period.equals("Annual")) {
                    HomeFlow.not_mFinancialPeriodselection = 0
                } else if (newsitem.period.equals("Q1")) {
                    HomeFlow.not_mFinancialPeriodselection = 1
                } else if (newsitem.period.equals("Q2")) {
                    HomeFlow.not_mFinancialPeriodselection = 2
                } else if (newsitem.period.equals("Q3")) {
                    HomeFlow.not_mFinancialPeriodselection = 3
                } else if (newsitem.period.equals("Q4")) {
                    HomeFlow.not_mFinancialPeriodselection = 4
                }

                HomeFlow.not_mFinancialPeriodselectionval = newsitem.period.trim()
                HomeFlow.not_mFinancialSelectedYear = newsitem.year.trim()
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                HomeFlow.forumtoNotification_to_Financailstatement_Bundle = args
                if (newsitem.period.equals("Annual")) {
                    HomeFlow.not_mForumPeriodselection = 0
                } else if (newsitem.period.equals("Q1")) {
                    HomeFlow.not_mForumPeriodselection = 1
                } else if (newsitem.period.equals("Q2")) {
                    HomeFlow.not_mForumPeriodselection = 2
                } else if (newsitem.period.equals("Q3")) {
                    HomeFlow.not_mForumPeriodselection = 3
                } else if (newsitem.period.equals("Q4")) {
                    HomeFlow.not_mForumPeriodselection = 4
                }

                HomeFlow.not_mForumPeriodselectionval = newsitem.period.trim()
                HomeFlow.not_mForumSelectedYear = newsitem.year.trim()
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                HomeFlow.newstoNotification_to_Financailstatement_Bundle = args
                if (newsitem.period.equals("Annual")) {
                    HomeFlow.not_mNewsPeriodselection = 0
                } else if (newsitem.period.equals("Q1")) {
                    HomeFlow.not_mNewsPeriodselection = 1
                } else if (newsitem.period.equals("Q2")) {
                    HomeFlow.not_mNewsPeriodselection = 2
                } else if (newsitem.period.equals("Q3")) {
                    HomeFlow.not_mNewsPeriodselection = 3
                } else if (newsitem.period.equals("Q4")) {
                    HomeFlow.not_mNewsPeriodselection = 4
                }

                HomeFlow.not_mNewsPeriodselectionval = newsitem.period.trim()
                HomeFlow.not_mNewsSelectedYear = newsitem.year.trim()
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                if (newsitem.period.equals("Annual")) {
                    HomeFlow.not_mSearchPeriodselection = 0
                } else if (newsitem.period.equals("Q1")) {
                    HomeFlow.not_mSearchPeriodselection = 1
                } else if (newsitem.period.equals("Q2")) {
                    HomeFlow.not_mSearchPeriodselection = 2
                } else if (newsitem.period.equals("Q3")) {
                    HomeFlow.not_mSearchPeriodselection = 3
                } else if (newsitem.period.equals("Q4")) {
                    HomeFlow.not_mSearchPeriodselection = 4
                }

                HomeFlow.not_mSearchPeriodselectionval = newsitem.period.trim()
                HomeFlow.not_mSearchSelectedYear = newsitem.year.trim()
                HomeFlow.searchtoNotification_to_Financailstatement_Bundle = args
            }
            //  (requireActivity() as MainActivity).animateonlyhome()

            findNavController().navigate(
                R.id.action_notificationFrag_to_financialStatementfrag,
                args
            )
        } else if (notificationDTO.metadata.type.equals("News")) {
            val newsitem = Gson().fromJson(
                notificationDTO.metadata.data[0].toString(),
                NOtificationDataNews::class.java
            )
            val newitemdto = NewsItemDTO(
                newsitem.title,
                newsitem.date,
                newsitem.image_url,
                newsitem.source,
                newsitem.link,
                newsitem.metadata
            )
            //  (requireActivity() as MainActivity).animateonltNews()

            val newstostring = Gson().toJson(newitemdto)
            val args = bundleOf("Newsdetail" to newstostring)
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.hometoNotification_to_news_Bundle = args
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.financialtoNotification_to_news_Bundle = args
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                HomeFlow.forumtoNotification_to_news_Bundle = args
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                HomeFlow.newstoNotification_to_news_Bundle = args
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                HomeFlow.searchtoNotification_to_news_Bundle = args
            }


            findNavController().navigate(R.id.action_notificationFrag_to_newsDetail, args)
        } else {
            (requireActivity() as MainActivity).animateonlyForum()
            findNavController().navigate(R.id.action_notificationFrag_to_forumFrag)
        }

    }


}