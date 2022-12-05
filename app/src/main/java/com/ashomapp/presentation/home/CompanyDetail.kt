package com.ashomapp.presentation.home

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentCompanyDetailBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.RemainingCompanyDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.utils.*
import com.bumptech.glide.Glide
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch


import android.view.animation.Animation

import android.R.string.no
import android.text.Html
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions


class CompanyDetail : Fragment() {

    private lateinit var mBinding: FragmentCompanyDetailBinding
    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentCompanyDetailBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadingDialog()  //for loading dialog


        if (HomeFlow.profilefragenable) {
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.home_settingcurrentID = R.id.companyDetail
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.financial_settingCurrentID = R.id.companyDetail
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                HomeFlow.forum_settingcurrentID = R.id.companyDetail
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                HomeFlow.news_settingcurrentID = R.id.companyDetail
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                HomeFlow.search_settingcurrentID = R.id.companyDetail
            }
        } else if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            HomeFlow.currentFragID = R.id.companyDetail
        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
            HomeFlow.searchcurrentFragID = R.id.companyDetail
        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
            HomeFlow.financialcurrentFragID = R.id.companyDetail
        }

        notificationcounter.observe(this.viewLifecycleOwner) {
            if (it != 0) {
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            } else {
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }

        mBinding.mtoolbar.mainBack.setOnClickListener {
            ObjectAnimator.ofFloat(mBinding.mtoolbar.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        if (HomeFlow.profilefragenable) {
                            findNavController().navigate(R.id.selectedCompanies)
                        } else if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                            if (HomeFlow.companydetailfromsearch) {
                                HomeFlow.companydetailfromsearch = false
                                findNavController().navigate(R.id.homeFrag)
                            } else {
                                val argument =
                                    bundleOf("country_name" to HomeFlow.HomecompanyNameBundle)
                                findNavController().navigate(
                                    R.id.action_companyDetail_to_company_frag,
                                    argument
                                )
                            }
                        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                            findNavController().navigate(R.id.searchFrag)
                        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                            val argument =
                                bundleOf("country_name" to HomeFlow.FinancialcompanyNameBundle)

                            findNavController().navigate(
                                R.id.action_companyDetail_to_company_frag,
                                argument
                            )
                        } else {
                            findNavController().navigateUp()
                        }
                    }
                }
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {


                if (HomeFlow.profilefragenable) {
                    findNavController().navigate(R.id.selectedCompanies)
                } else if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    if (HomeFlow.companydetailfromsearch) {
                        HomeFlow.companydetailfromsearch = false
                        findNavController().navigate(R.id.homeFrag)
                    } else {
                        val argument = bundleOf("country_name" to HomeFlow.HomecompanyNameBundle)
                        findNavController().navigate(
                            R.id.action_companyDetail_to_company_frag,
                            argument
                        )
                    }
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    findNavController().navigate(R.id.searchFrag)
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    val argument = bundleOf("country_name" to HomeFlow.FinancialcompanyNameBundle)
                    findNavController().navigate(
                        R.id.action_companyDetail_to_company_frag,
                        argument
                    )
                } else {
                    findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this.viewLifecycleOwner,
            onBackPressedCallback
        )

        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)

            findNavController().navigate(R.id.notificationFrag)
        }
        mBinding.mtoolbar.icon.setOnClickListener {
            setanimation(it)
            if (HomeFlow.sectionBottomID == R.id.searchFrag && HomeFlow.searchcurrentFragID != R.id.searchFrag) {
                (requireActivity() as MainActivity).clicktoSearchFrag()
            } else if (HomeFlow.sectionBottomID == R.id.countryList && HomeFlow.financialcurrentFragID != R.id.countryList) {
                (requireActivity() as MainActivity).clicktoCountryList()
            } else {
                (requireActivity() as MainActivity).gotoHome()
            }
        }
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            findNavController().navigate(R.id.action_companyDetail_to_settingFrag)
        }
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)
            if (SharedPrefrenceHelper.user.subscription_type.equals("Yearly")) {
                mBinding.remainingSubscription.visibility = View.GONE
            } else {
                mBinding.remainingSubscription.visibility = View.VISIBLE
            }
        }

        val args = requireArguments().getString("company_detail")
        if (!args.isNullOrEmpty()) {
            val companydetail = Gson().fromJson(args, CompanyDTO::class.java)
            Glide.with(AshomAppApplication.instance.applicationContext).load(companydetail.image).into(mBinding.companyDetailComImage)
            mBinding.companyName.text = "${companydetail.Company_Name}"
            mBinding.countryName.text = "${companydetail.Country}"
            getRemainingVisit(companydetail.SymbolTicker)
            if (companydetail.company_status.isNullOrEmpty() || companydetail.company_status != "Suspended") {
                if (!companydetail.DelistingDate.isNullOrEmpty()) {
                    mBinding.delisteddate.text = "DELISTED ON ${companydetail.DelistingDate}"
                }
            } else {
                mBinding.delisteddate.text = "${companydetail.company_status}  ⛔  "
            }

            if (companydetail.Country.equals("KSA")) {
                mBinding.countryFlag.setImageResource(R.drawable.ksaflag)
            } else if (companydetail.Country.equals("UAE")) {
                mBinding.countryFlag.setImageResource(R.drawable.uae_flag)
            } else if (companydetail.Country.equals("All")) {
                mBinding.countryFlag.setImageResource(R.drawable.all_countries)
            } else {
                Glide.with(AshomAppApplication.instance.applicationContext)
                    .load("https://countryflagsapi.com/png/${companydetail.Country}")
                    .into(mBinding.countryFlag)
            }

            mBinding.latestreliselistdate.setOnClickListener {
                setanimation(it)
                val companydetail1 = Gson().toJson(companydetail)
                mHomeViewModel.recordEvent("click_company_financial_statements")
                val args = bundleOf("company_finance_detail" to companydetail1)

                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.financialstatementbundle = args
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    HomeFlow.searchFinanancialbundle = args
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.Financial_financialstatementbundle = args
                }
                findNavController().navigate(
                    R.id.action_companyDetail_to_financialStatementfrag,
                    args
                )

            }

            mBinding.cvFinancialStatement.setOnClickListener {
                //  Log.d("is_view", postOpenCompany(companydetail.id).toString())
                //    loadingEnableDisable.value = true
                mHomeViewModel.recordEvent("click_company_financial_statements")
                val companydetail1 = Gson().toJson(companydetail)
                val args = bundleOf("company_finance_detail" to companydetail1)

                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.financialstatementbundle = args
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    HomeFlow.searchFinanancialbundle = args
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.Financial_financialstatementbundle = args
                }
                findNavController().navigate(
                    R.id.action_companyDetail_to_financialStatementfrag,
                    args
                )


            }

            mBinding.cvCompanyNews.setOnClickListener {
                mHomeViewModel.recordEvent("click_company_news")
                mHomeViewModel.recordEvent("view_${companydetail.Country.lowercase()}_${companydetail.SymbolTicker}_news")
                val companydetail1 = Gson().toJson(companydetail)
                val args = bundleOf("company_news_detail" to companydetail1)

                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.homecompanynewsBundle = args
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    HomeFlow.seacrhcompanynewsBundle = args
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.financialcompanynewsBundle = args
                }
                findNavController().navigate(R.id.companyNews, args)
            }
        }


        if (HomeFlow.companydetailanimation) {
            val animation = AnimationUtils.loadAnimation(
                AshomAppApplication.instance.applicationContext, R.anim.slide_up
            )
            mBinding.containerDetail.startAnimation(animation)
            HomeFlow.companydetailanimation = false

        }

    }


    private fun getRemainingVisit(id: String) {
        lifecycleScope.launch {
            val result = HomeRepository.getRemainingVisit(id)
            when (result) {
                is ResultWrapper.Success -> {
                    val newsitem =
                        Gson().fromJson(result.response.toString(), RemainingCompanyDTO::class.java)
                    Log.d("remainingvisit", "${newsitem}, ${result.response.toString()}")
                    if (newsitem.status) {
                        if (!newsitem.last_report.isNullOrEmpty()) {
                            mBinding.latestreliselistdate.text =
                                Html.fromHtml("<u> Last Released Report: ${newsitem.last_report} </u>")

                            val strsarray = newsitem.last_report.split("-")
                            Log.d("stringsplit", strsarray.toString())


                            if (HomeFlow.profilefragenable) {
                                if (strsarray[0].equals("Annual ")) {
                                    HomeFlow.pro_mHomePeriodselection = 0
                                } else if (strsarray[0].equals("Q1 ")) {
                                    HomeFlow.pro_mHomePeriodselection = 1
                                } else if (strsarray[0].equals("Q2 ")) {
                                    HomeFlow.pro_mHomePeriodselection = 2
                                } else if (strsarray[0].equals("Q3 ")) {
                                    HomeFlow.pro_mHomePeriodselection = 3
                                } else if (strsarray[0].equals("Q4 ")) {
                                    HomeFlow.pro_mHomePeriodselection = 4
                                }

                                HomeFlow.pro_mHomePeriodselectionval = strsarray[0].trim()
                                HomeFlow.pro_mHomeSelectedYear = strsarray[1].trim()

                            } else if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                                if (strsarray[0].equals("Annual ")) {
                                    FinancialStatementfrag.mHomePeriodselection = 0
                                } else if (strsarray[0].equals("Q1 ")) {
                                    FinancialStatementfrag.mHomePeriodselection = 1
                                } else if (strsarray[0].equals("Q2 ")) {
                                    FinancialStatementfrag.mHomePeriodselection = 2
                                } else if (strsarray[0].equals("Q3 ")) {
                                    FinancialStatementfrag.mHomePeriodselection = 3
                                } else if (strsarray[0].equals("Q4 ")) {
                                    FinancialStatementfrag.mHomePeriodselection = 4
                                }

                                FinancialStatementfrag.mHomePeriodselectionval = strsarray[0].trim()
                                FinancialStatementfrag.mHomeSelectedYear = strsarray[1].trim()

                            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                                if (strsarray[0].equals("Annual ")) {
                                    FinancialStatementfrag.mFinancialPeriodselection = 0
                                } else if (strsarray[0].equals("Q1 ")) {
                                    FinancialStatementfrag.mFinancialPeriodselection = 1
                                } else if (strsarray[0].equals("Q2 ")) {
                                    FinancialStatementfrag.mFinancialPeriodselection = 2
                                } else if (strsarray[0].equals("Q3 ")) {
                                    FinancialStatementfrag.mFinancialPeriodselection = 3
                                } else if (strsarray[0].equals("Q4 ")) {
                                    FinancialStatementfrag.mFinancialPeriodselection = 4
                                }

                                FinancialStatementfrag.mFinancialPeriodselectionval =
                                    strsarray[0].trim()
                                FinancialStatementfrag.mFinancialSelectedYear = strsarray[1].trim()

                            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                                if (strsarray[0].equals("Annual ")) {
                                    FinancialStatementfrag.mSearchPeriodselection = 0
                                } else if (strsarray[0].equals("Q1 ")) {
                                    FinancialStatementfrag.mSearchPeriodselection = 1
                                } else if (strsarray[0].equals("Q2 ")) {
                                    FinancialStatementfrag.mSearchPeriodselection = 2
                                } else if (strsarray[0].equals("Q3 ")) {
                                    FinancialStatementfrag.mSearchPeriodselection = 3
                                } else if (strsarray[0].equals("Q4 ")) {
                                    FinancialStatementfrag.mSearchPeriodselection = 4
                                }

                                FinancialStatementfrag.mSearchPeriodselectionval =
                                    strsarray[0].trim()
                                FinancialStatementfrag.mSearchSelectedYear = strsarray[1].trim()

                            }
                        }

                        if (newsitem.visit_data.remaining_visits.isNullOrEmpty()) {
                            mBinding.remainingSubscription.text = ""

                        } else {
                            FinancialStatementfrag.remaining =
                                newsitem.visit_data.remaining_visits.toInt()
                            mBinding.remainingSubscription.text =
                                "Remaining Count: ${newsitem.visit_data.remaining_visits} of ${newsitem.visit_data.max_companies}"


                        }
                        if (newsitem.visit_data.remaining_visits.equals("0") && newsitem.visit_data.max_companies.toInt() > 0) {
                            FinancialStatementfrag.lockunlock = 1

                        } else if (newsitem.visit_data.remaining_visits.toInt() > 0) {
                            FinancialStatementfrag.lockunlock = 1
                        } else {
                            FinancialStatementfrag.lockunlock = 0
                        }
                    } else {
                        SharedPrefrenceHelper.devicetoken = "0"
                        SharedPrefrenceHelper.token = "0"
                        MainActivity.usertoken.value = ""
                        val navOptions =
                            NavOptions.Builder().setPopUpTo(R.id.companyDetail, true).build()
                        findNavController().navigate(R.id.loginFragment, null, navOptions)
                    }
                }
                is ResultWrapper.Failure -> {
                    Log.d("remainingvisit", result.errorMessage)
                }
            }
        }
    }


}