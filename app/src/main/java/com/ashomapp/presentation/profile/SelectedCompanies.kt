package com.ashomapp.presentation.profile

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.AshomDBHelper
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentSelectedCompaniesBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.presentation.home.CompanyAdapter
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.presentation.home.onCompanyClick
import com.ashomapp.utils.*
import com.bumptech.glide.Glide
import com.google.gson.Gson


class SelectedCompanies : Fragment(), onCompanyClick {
    private lateinit var mBinding: FragmentSelectedCompaniesBinding
    private lateinit var adapter: CompanyAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSelectedCompaniesBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding.mtoolbar.mainBack.setOnClickListener {
            ObjectAnimator.ofFloat(mBinding.mtoolbar.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        findNavController().navigate(R.id.settingFrag)
                    }
                }
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.settingFrag)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this.viewLifecycleOwner,
            onBackPressedCallback
        )
        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            HomeFlow.home_settingcurrentID = R.id.selectedCompanies
        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
            HomeFlow.financial_settingCurrentID = R.id.selectedCompanies
        } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
            HomeFlow.forum_settingcurrentID = R.id.selectedCompanies
        } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
            HomeFlow.news_settingcurrentID = R.id.selectedCompanies
        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
            HomeFlow.search_settingcurrentID = R.id.selectedCompanies
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
        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)

            findNavController().navigate(R.id.notificationFrag)
        }
        selectedCompaniesList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter = CompanyAdapter(it, this)
                mBinding.recylcerCompany.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }

        mBinding.mtoolbar.toolProfile.setOnClickListener {
            findNavController().navigateUp()
        }
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

        }
        mBinding.etCompanySearch.doOnTextChanged { text, start, before, count ->
            adapter.filter.filter(text)
        }
    }

    override fun onCompanyItemClick(companyDTO: CompanyDTO) {
        mBinding.etCompanySearch.setText("")
        mBinding.etCompanySearch.clearFocus()
        val companydetail = Gson().toJson(companyDTO)
        val args = bundleOf("company_detail" to companydetail)
        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            HomeFlow.home_selectedCompanytoComapnyDetail = args
        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
            HomeFlow.financial_selectedCompanytoComapnyDetail = args
        } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
            HomeFlow.forum_selectedCompanytoComapnyDetail = args
        } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
            HomeFlow.news_selectedCompanytoComapnyDetail = args
        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
            HomeFlow.search_selectedCompanytoComapnyDetail = args
        }
        findNavController().navigate(R.id.action_selectedCompanies_to_companyDetail, args)
    }

}