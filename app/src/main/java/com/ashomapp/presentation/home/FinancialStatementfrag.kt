package com.ashomapp.presentation.home

import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ashomapp.R
import com.ashomapp.databinding.FragmentFinancialStatementfragBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.google.gson.Gson

import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.ItemYearListBinding
import com.ashomapp.databinding.SuscessDialogBinding
import com.ashomapp.network.response.dashboard.CompanyStatementsDTO
import com.ashomapp.network.response.dashboard.RemainingCompanyDTO
import com.ashomapp.network.response.dashboard.YearsDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.utils.*
import com.bumptech.glide.Glide
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class CustomFinancialDTO(
    val documenet_iamge: Int,
    val document_name: String,
    val document_description: String? = null
)


class FinancialStatementfrag : Fragment(), onYearClick, onDocumentClick {
    private lateinit var mBinding: FragmentFinancialStatementfragBinding
    private lateinit var mDocAdapter: FinancialDocumentAdapter

    var animatelist = false

    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    val listofinfodoc = listOf(
        "An income statement is a report that shows how much revenue a company earned over a specific time period (usually for a year or some portion of a year). An income statement also shows the costs and expenses associated with earning that revenue. The literal “bottom line” of the statement usually shows the company’s net earnings or losses. This tells you how much the company earned or lost over the period.Income statements also report earnings per share (or “EPS”). This calculation tells you how much money shareholders would receive if the company decided to distribute all of the net earnings for the period. (Companies almost never distribute all of their earnings. Usually they reinvest them in the business.)To understand how income statements are set up, think of them as a set of stairs. You start at the top with the total amount of sales made during the accounting period. Then you go down, one step at a time. At each step, you make a deduction for certain costs or other operating expenses associated with earning the revenue. At the bottom of the stairs, after deducting all of the expenses, you learn how much the company actually earned or lost during the accounting period. People often call this the bottom line.",
        "A balance sheet provides detailed information about a company’s assets, liabilities and shareholders’ equity.Assets are things that a company owns that have value. This typically means they can either be sold or used by the company to make products or provide services that can be sold. Assets include physical property, such as plants, trucks, equipment and inventory. It also includes things that can’t be touched but nevertheless exist and have value, such as trademarks and patents. And cash itself is an asset. So are investments a company makes.Liabilities are amounts of money that a company owes to others. This can include all kinds of obligations, like money borrowed from a bank to launch a new product, rent for use of a building, money owed to suppliers for materials, payroll a company owes to its employees, environmental cleanup costs, or taxes owed to the government. Liabilities also include obligations to provide goods or services to customers in the future.Shareholders’ equity is sometimes called capital or net worth. It’s the money that would be left if a company sold all of its assets and paid off all of its liabilities. This leftover money belongs to the shareholders, or the owners, of the company.",
        "The statement of changes in equity, sometimes called the “statement of changes in owners’ equity” or “statement of changes in shareholders’ equity,” primarily serves to report changes in the owners’ investment in the business over time. The basic components of owners’ equity are paid- in capital and retained earnings. Retained earnings include the cumulative amount of the company’s profits that have been retained in the company. In addition, non- controlling or minority interests and reserves that represent accumulated OCI items are included in equity. The latter items may be shown separately or included in retained earnings. Volkswagen includes reserves as components of retained earnings.The statement of changes in equity is organized to present, for each component of equity, the beginning balance, any increases during the period, any decreases during the period, and the ending balance. For paid- in capital, an example of an increase is a new issuance of equity and an example of a decrease is a repurchase of previously issued stock. For retained earnings, income (both net income as reported on the income statement and OCI) is the most common increase and a dividend payment is the most common decrease.",
        "Cash flow statements report a company’s inflows and outflows of cash. This is important because a company needs to have enough cash on hand to pay its expenses and purchase assets. While an income statement can tell you whether a company made a profit, a cash flow statement can tell you whether the company generated cash.A cash flow statement shows changes over time rather than absolute dollar amounts at a point in time. It uses and reorders the information from a company’s balance sheet and income statement.The bottom line of the cash flow statement shows the net increase or decrease in cash for the period. Generally, cash flow statements are divided into three main parts. Each part reviews the cash flow from one of three types of activities: (1) operating activities; (2) investing activities; and (3) financing activities.",
        "The statement of comprehensive income is one of the five financial statements required in a complete set of financial statements for distribution outside of a corporation.The statement of comprehensive income covers the same period of time as the income statement and consists of two major sections:Net income (or net earnings) from the company's income statement Other comprehensive income, which consists of positive and/or negative amounts for foreign currency translation and hedges, and a few other items. The totals from each of the above sections are summed and are presented as comprehensive income.(If a company does not have any item that meets the criteria of other comprehensive income, the statement of comprehensive income is not required.)For a company with an item that is considered to be other comprehensive income, the statement of comprehensive income is usually a separate financial statement that is presented immediately following the income statement. (However, a company has the option to combine the income statement and the statement of comprehensive income into one continuous financial statement.)The amount of net income will cause an increase in the stockholders' equity account Retained Earnings, while a loss will cause a decrease.The amount of other comprehensive income will cause an increase in the stockholders' equity account Accumulated Other Comprehensive Income (while a negative amount will cause a decrease in Accumulated Other Comprehensive Income).",
        "Also referred to as footnotes. These provide additional information pertaining to a company's operations and financial position and are considered to be an integral part of the financial statements. The notes are required by the full disclosure principle.",
        "An annual report is a financial summary of a company’s activities during the year along with management’s analysis of the company’s current financial position and future plans. Annual reports are prepared at the end of the fiscal year for external users to gain financial information about the inner workings of the company and what management plans to do in the future.",
        "Download the Audited Financial Statements"

    )

    companion object {
        var lockunlock = 0
        var remaining = -1
        var upgradelaertdilaog = 0
        var mperiodselection = 0

        var mHomePeriodselection = 0
        var mHomePeriodselectionval = "Annual"
        var mHomeSelectedYear = ""

        var homesetyear = 0
        var countrylistsetYear = 0
        var searchsetYear = 0


        //changed by nj-4-10-22
        var fromnewssection=0

        var mFinancialPeriodselection = 0
        var mFinancialPeriodselectionval = "Annual"
        var mFinancialSelectedYear = ""


        //changed by nj-4-10-22
        var mNewsPeriodselection = 0
        var mNewsPeriodselectionval = "Annual"
        var mNewsSelectedYear = ""




        var mNotificationPeriodselection = 0
        var mNotificationPeriodselectionval = "Annual"
        var mNotificationSelectedYear = ""

        var mSearchPeriodselection = 0
        var mSearchPeriodselectionval = "Annual"
        var mSearchSelectedYear = ""


        var listanimation = true

        var COMPANY_SYMBOL = ""
    }

    //var  list = mutableListOf<String>()
    val list = listOf(
        CustomFinancialDTO(
            R.drawable.incomestatement,
            "Income Statement",
            listofinfodoc[0]
        ),
        CustomFinancialDTO(
            R.drawable.balance_sheet, "Balance Sheet",
            listofinfodoc[1]
        ),
        CustomFinancialDTO(
            R.drawable.equity_statements, "Equity Statement",
            listofinfodoc[2].toString()
        ),
        CustomFinancialDTO(
            R.drawable.cash_flow, "Cash Flow Statement",
            listofinfodoc[3]
        ),
        CustomFinancialDTO(
            R.drawable.comprehensive_income, "Comprehensive Income Statement",
            listofinfodoc[4]
        ),
        CustomFinancialDTO(
            R.drawable.notes, "Notes",
            listofinfodoc[5]
        ),
        CustomFinancialDTO(
            R.drawable.annual_reports, "Annual Report",
            listofinfodoc[6]
        ),
        CustomFinancialDTO(
            R.drawable.financial_report, "Financial Report",
            listofinfodoc[7]
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animatelist = true

        val args = requireArguments().getString("company_finance_detail")
        if (!args.isNullOrEmpty()) {

            val companydetail = Gson().fromJson(args, CompanyDTO::class.java)
            postOpenCompany(companydetail.id)
            COMPANY_SYMBOL = companydetail.SymbolTicker
                mHomeViewModel.companyID.value = "${companydetail.id}"
            mHomeViewModel.getcompanyYear(companydetail.id)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            FragmentFinancialStatementfragBinding.inflate(layoutInflater, container, false).apply {
                yearselect = false
                periodselection = mperiodselection
            }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        ApplyGTMEvent("financial_Statement_view","financial_Statement_view_count","financial_Statement_view")



        MainActivity.intentforCompany.value = ""
        notificationcounter.observe(this.viewLifecycleOwner) {
            if (it != 0) {
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            } else {
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }
        if (HomeFlow.notificationfragenable) {
            mperiodselection = mNotificationPeriodselection
            mHomeViewModel.mPeriod.value = mNotificationPeriodselectionval
            mBinding.periodselection = mperiodselection
            mHomeViewModel.myear.value = mNotificationSelectedYear
            mBinding.yearTv.setText(mNotificationSelectedYear)
            mHomeViewModel.getCompanyDocumetn()
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.hometonotification = true
                HomeFlow.hometoNotificationCurrentFragID = R.id.financialStatementfrag
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.countrylist_to_notification = true
                HomeFlow.financialtoNotificationCurrentFragID = R.id.financialStatementfrag
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                HomeFlow.fourm_to_notification = true
                HomeFlow.forumtoNotificationCurrentFragID = R.id.financialStatementfrag
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                HomeFlow.news_to_notification = true
                HomeFlow.newstoNotificationCurrentFragID = R.id.financialStatementfrag
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                HomeFlow.search_to_notification = true
                HomeFlow.searchtoNotificationCurrentFragID = R.id.financialStatementfrag
            }
        }

        //changed by nj-4-10-22
        else if (HomeFlow.newsfragenable) {
            mperiodselection = HomeFlow.pro_mHomePeriodselection
            mHomeViewModel.mPeriod.value = HomeFlow.pro_mHomePeriodselectionval
            mBinding.periodselection = mperiodselection
            mHomeViewModel.myear.value = HomeFlow.pro_mHomeSelectedYear
            mBinding.yearTv.setText(HomeFlow.pro_mHomeSelectedYear)
            mHomeViewModel.getCompanyDocumetn()

            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.hometonotification = true
                HomeFlow.hometoNotificationCurrentFragID = R.id.financialStatementfrag
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.countrylist_to_notification = true
                HomeFlow.financialtoNotificationCurrentFragID = R.id.financialStatementfrag
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                HomeFlow.fourm_to_notification = true
                HomeFlow.forumtoNotificationCurrentFragID = R.id.financialStatementfrag
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                HomeFlow.news_to_notification = true
                HomeFlow.newstoNotificationCurrentFragID = R.id.financialStatementfrag
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                HomeFlow.search_to_notification = true
                HomeFlow.searchtoNotificationCurrentFragID = R.id.financialStatementfrag
            }
        }

//



        else if (HomeFlow.profilefragenable) {
            mperiodselection = HomeFlow.pro_mHomePeriodselection
            mHomeViewModel.mPeriod.value = HomeFlow.pro_mHomePeriodselectionval
            mBinding.periodselection = mperiodselection
            mHomeViewModel.myear.value = HomeFlow.pro_mHomeSelectedYear
            mBinding.yearTv.setText(HomeFlow.pro_mHomeSelectedYear)
            mHomeViewModel.getCompanyDocumetn()
        } else if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            mperiodselection = mHomePeriodselection
            mHomeViewModel.mPeriod.value = mHomePeriodselectionval
            mBinding.periodselection = mperiodselection
            mHomeViewModel.myear.value = mHomeSelectedYear
            mBinding.yearTv.setText(mHomeSelectedYear)
            mHomeViewModel.getCompanyDocumetn()

            HomeFlow.currentFragID = R.id.financialStatementfrag
        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
            mperiodselection = mSearchPeriodselection
            mHomeViewModel.mPeriod.value = mSearchPeriodselectionval
            mBinding.periodselection = mperiodselection
            mHomeViewModel.myear.value = mSearchSelectedYear
            mBinding.yearTv.setText(mSearchSelectedYear)
            mHomeViewModel.getCompanyDocumetn()

            HomeFlow.searchcurrentFragID = R.id.financialStatementfrag
        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
            mperiodselection = mFinancialPeriodselection
            mHomeViewModel.mPeriod.value = mFinancialPeriodselectionval
            mBinding.periodselection = mperiodselection
            mHomeViewModel.myear.value = mFinancialSelectedYear
            mBinding.yearTv.setText(mFinancialSelectedYear)
            mHomeViewModel.getCompanyDocumetn()


            HomeFlow.financialcurrentFragID = R.id.financialStatementfrag
        }


        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mperiodselection = 0
                mHomeViewModel.mPeriod.value = "Annual"
                mHomeViewModel.myear.value = ""

                mHomeViewModel.CompanyDocumet.value = emptyList()
                if (HomeFlow.notificationfragenable) {
                    mNotificationPeriodselection = 0
                    mNotificationPeriodselectionval = "Annual"
                    mNotificationSelectedYear = ""

                    //changed by nj-4-10-22
                    if(fromnewssection==1){
                        findNavController().navigate(R.id.newsFrag)
                        fromnewssection=0
                        HomeFlow.newsfragenable = false
                    }else{
                        findNavController().navigate(R.id.notificationFrag)
                    }
                    //
                   // findNavController().navigate(R.id.notificationFrag)
                    HomeFlow.notificationfragenable = false
                } else if (HomeFlow.profilefragenable) {
                    if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                        findNavController().navigate(
                            R.id.action_financialStatementfrag_to_companyDetail,
                            HomeFlow.home_selectedCompanytoComapnyDetail
                        )
                    } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                        findNavController().navigate(
                            R.id.action_financialStatementfrag_to_companyDetail,
                            HomeFlow.financial_selectedCompanytoComapnyDetail
                        )
                    } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                        findNavController().navigate(
                            R.id.action_financialStatementfrag_to_companyDetail,
                            HomeFlow.forum_selectedCompanytoComapnyDetail
                        )
                    } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                        findNavController().navigate(
                            R.id.action_financialStatementfrag_to_companyDetail,
                            HomeFlow.news_selectedCompanytoComapnyDetail
                        )
                    } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                        findNavController().navigate(
                            R.id.action_financialStatementfrag_to_companyDetail,
                            HomeFlow.search_selectedCompanytoComapnyDetail
                        )
                    }
                } else if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    findNavController().navigate(
                        R.id.action_financialStatementfrag_to_companyDetail,
                        HomeFlow.companydetailbundle
                    )
                    mHomePeriodselection = 0
                    mHomePeriodselectionval = "Annual"
                    mHomeSelectedYear = ""
                    homesetyear = -1
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    mSearchPeriodselection = 0
                    mSearchPeriodselectionval = "Annual"
                    mSearchSelectedYear = ""
                    searchsetYear = -1

                    findNavController().navigate(
                        R.id.companyDetail,
                        HomeFlow.searchCountrydetailbundle
                    )
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    mFinancialPeriodselection = 0
                    mFinancialPeriodselectionval = "Annual"
                    mFinancialSelectedYear = ""
                    countrylistsetYear = -1
                    findNavController().navigate(
                        R.id.companyDetail,
                        HomeFlow.financialcompanydetailbundle
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

        mBinding.mtoolbar.mainBack.setOnClickListener {
            ObjectAnimator.ofFloat(mBinding.mtoolbar.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        mperiodselection = 0
                        mHomeViewModel.mPeriod.value = "Annual"
                        mHomeViewModel.myear.value = ""

                        mHomeViewModel.CompanyDocumet.value = emptyList()
                        if (HomeFlow.notificationfragenable) {
                            mNotificationPeriodselection = 0
                            mNotificationPeriodselectionval = "Annual"
                            mNotificationSelectedYear = ""

                            //changed by nj-4-10-22
                            if(fromnewssection==1){
                                findNavController().navigate(R.id.newsFrag)
                                fromnewssection=0
                                HomeFlow.newsfragenable = false
                            }else{
                                findNavController().navigate(R.id.notificationFrag)
                            }
                            //
                           // findNavController().navigate(R.id.notificationFrag)
                            HomeFlow.notificationfragenable = false
                        }else if(HomeFlow.newsfragenable){
                            mNewsPeriodselection = 0
                            mNewsPeriodselectionval = "Annual"
                            mNewsSelectedYear = ""
                            findNavController().navigate(R.id.newsFrag)
                            HomeFlow.newsfragenable = false
                        }



                        else if (HomeFlow.profilefragenable) {
                            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                                findNavController().navigate(
                                    R.id.action_financialStatementfrag_to_companyDetail,
                                    HomeFlow.home_selectedCompanytoComapnyDetail
                                )
                            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                                findNavController().navigate(
                                    R.id.action_financialStatementfrag_to_companyDetail,
                                    HomeFlow.financial_selectedCompanytoComapnyDetail
                                )
                            } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                                findNavController().navigate(
                                    R.id.action_financialStatementfrag_to_companyDetail,
                                    HomeFlow.forum_selectedCompanytoComapnyDetail
                                )
                            } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                                findNavController().navigate(
                                    R.id.action_financialStatementfrag_to_companyDetail,
                                    HomeFlow.news_selectedCompanytoComapnyDetail
                                )
                            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                                findNavController().navigate(
                                    R.id.action_financialStatementfrag_to_companyDetail,
                                    HomeFlow.search_selectedCompanytoComapnyDetail
                                )
                            }
                        } else if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                            mHomePeriodselection = 0
                            mHomePeriodselectionval = "Annual"
                            mHomeSelectedYear = ""
                            homesetyear = -1
                            findNavController().navigate(
                                R.id.action_financialStatementfrag_to_companyDetail,
                                HomeFlow.companydetailbundle
                            )
                        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                            mSearchPeriodselection = 0
                            mSearchPeriodselectionval = "Annual"
                            mSearchSelectedYear = ""
                            searchsetYear = -1
                            findNavController().navigate(
                                R.id.action_financialStatementfrag_to_companyDetail,
                                HomeFlow.searchCountrydetailbundle
                            )
                        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                            mFinancialPeriodselection = 0
                            mFinancialPeriodselectionval = "Annual"
                            mFinancialSelectedYear = ""
                            countrylistsetYear = -1
                            findNavController().navigate(
                                R.id.action_financialStatementfrag_to_companyDetail,
                                HomeFlow.financialcompanydetailbundle
                            )
                        } else {
                            findNavController().navigateUp()
                        }
                    }
                }
        }
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
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag && HomeFlow.forumCurrentID != R.id.forumFrag) {
                (requireActivity() as MainActivity).clicktoForumTab()
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag && HomeFlow.newsFragCurrentID != R.id.newsFrag) {
                (requireActivity() as MainActivity).clicktoForumTab()
            } else {
                (requireActivity() as MainActivity).gotoHome()
            }
        }

        mBinding.mtoolbar.toolProfile.setOnClickListener {

            findNavController().navigate(R.id.action_financialStatementfrag_to_settingFrag)
        }
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

            Log.d("subscriptiontype", "${SharedPrefrenceHelper.user.subscription_type}")
        }
        mDocAdapter =
            FinancialDocumentAdapter(list = list, mList = emptyList(), onDocumentClick = this)
        mBinding.statementList.adapter = mDocAdapter
        mDocAdapter.notifyDataSetChanged()
        Handler().postDelayed({
            if (mHomeViewModel.mPeriod.value.isNullOrEmpty()) {
                mBinding.statementList.startLayoutAnimation()
                animatelist = false

            } else {
                mBinding.statementList.clearAnimation()
            }
        }, 100)



        mHomeViewModel.CompanyDocumet.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                val adapter = FinancialDocumentAdapter(list, it, this)
                mBinding.statementList.adapter = adapter
                adapter.notifyDataSetChanged()
            } else {
                val adapter = FinancialDocumentAdapter(list, emptyList(), this)
                mBinding.statementList.adapter = adapter
                adapter.notifyDataSetChanged()
            }

        }

        mHomeViewModel.yearsDTO.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                val adaterYear = AdaterYear(it as ArrayList<YearsDTO>, this)
                mBinding.yearSelect.yearList.adapter = adaterYear
                adaterYear.notifyDataSetChanged()
                /*if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    if (homesetyear == -1) {
                        mHomeSelectedYear = "${it[0].year}"
                        mHomeViewModel.myear.value = "${it[0].year}"
                        homesetyear = 0
                    }
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {

                    if (countrylistsetYear == -1) {
                        mHomeSelectedYear = "${it[0].year}"
                        mHomeViewModel.myear.value = "${it[0].year}"
                        mFinancialSelectedYear = "${it[0].year}"
                        countrylistsetYear = 0
                    }
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    if (searchsetYear == -1) {
                        mHomeSelectedYear = "${it[0].year}"
                        mHomeViewModel.myear.value = "${it[0].year}"
                        mSearchSelectedYear = "${it[0].year}"
                        searchsetYear = 0
                    }
                }*/

                mHomeViewModel.getCompanyDocumetn()


            }

        }
        mHomeViewModel.myear.observe(this.viewLifecycleOwner) {
            mBinding.yearTv.text = "${it}"
        }
        mBinding.yearSelectionView.setOnClickListener {
            if (mBinding.yearselect == true) {
                mBinding.yearselect = false
            }
        }
        mBinding.timePeriodYear.setOnClickListener {

            if (mBinding.yearselect == true) {
                mBinding.yearselect = false
            } else {
                mBinding.yearselect = true

            }

        }
        mBinding.timePeriodAnnual.setOnClickListener {

            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                mHomePeriodselection = 0
                mHomePeriodselectionval = "Annual"
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                mFinancialPeriodselection = 0
                mFinancialPeriodselectionval = "Annual"
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                mSearchPeriodselection = 0
                mSearchPeriodselectionval = "Annual"
            }
            if (HomeFlow.notificationfragenable) {
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.not_mHomePeriodselection = 0
                    HomeFlow.not_mHomePeriodselectionval = "Annual"
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.not_mFinancialPeriodselection = 0
                    HomeFlow.not_mFinancialPeriodselectionval = "Annual"
                } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                    HomeFlow.not_mForumPeriodselection = 0
                    HomeFlow.not_mForumPeriodselectionval = "Annual"
                } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                    HomeFlow.not_mForumPeriodselection = 0
                    HomeFlow.not_mForumPeriodselectionval = "Annual"
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    HomeFlow.not_mSearchPeriodselection = 0
                    HomeFlow.not_mSearchPeriodselectionval = "Annual"
                }
            }

            mperiodselection = 0
            mBinding.periodselection = mperiodselection
            mHomeViewModel.mPeriod.value = "Annual"
            mHomeViewModel.getCompanyDocumetn()
        }
        mBinding.firstQtr.setOnClickListener {
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                mHomePeriodselection = 1
                mHomePeriodselectionval = "q1"
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                mFinancialPeriodselection = 1
                mFinancialPeriodselectionval = "q1"
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                mSearchPeriodselection = 1
                mSearchPeriodselectionval = "q1"
            }
            if (HomeFlow.notificationfragenable) {
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.not_mHomePeriodselection = 1
                    HomeFlow.not_mHomePeriodselectionval = "q1"
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.not_mFinancialPeriodselection = 1
                    HomeFlow.not_mFinancialPeriodselectionval = "q1"
                } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                    HomeFlow.not_mForumPeriodselection = 1
                    HomeFlow.not_mForumPeriodselectionval = "q1"
                } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                    HomeFlow.not_mForumPeriodselection = 1
                    HomeFlow.not_mForumPeriodselectionval = "q1"
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    HomeFlow.not_mSearchPeriodselection = 1
                    HomeFlow.not_mSearchPeriodselectionval = "q1"
                }
            }
            mperiodselection = 1
            mBinding.periodselection = mperiodselection
            mHomeViewModel.mPeriod.value = "q1"
            mHomeViewModel.getCompanyDocumetn()
        }
        mBinding.secondQtr.setOnClickListener {
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                mHomePeriodselection = 2
                mHomePeriodselectionval = "q2"
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                mFinancialPeriodselection = 2
                mFinancialPeriodselectionval = "q2"
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                mSearchPeriodselection = 2
                mSearchPeriodselectionval = "q2"
            }

            if (HomeFlow.notificationfragenable) {
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.not_mHomePeriodselection = 2
                    HomeFlow.not_mHomePeriodselectionval = "q2"
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.not_mFinancialPeriodselection = 2
                    HomeFlow.not_mFinancialPeriodselectionval = "q2"
                } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                    HomeFlow.not_mForumPeriodselection = 2
                    HomeFlow.not_mForumPeriodselectionval = "q2"
                } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                    HomeFlow.not_mForumPeriodselection = 2
                    HomeFlow.not_mForumPeriodselectionval = "q2"
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    HomeFlow.not_mSearchPeriodselection = 2
                    HomeFlow.not_mSearchPeriodselectionval = "q2"
                }
            }
            mperiodselection = 2
            mBinding.periodselection = mperiodselection
            mHomeViewModel.mPeriod.value = "q2"
            mHomeViewModel.getCompanyDocumetn()
        }
        mBinding.thirdQtr.setOnClickListener {
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                mHomePeriodselection = 3
                mHomePeriodselectionval = "q3"
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                mFinancialPeriodselection = 3
                mFinancialPeriodselectionval = "q3"
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                mSearchPeriodselection = 3
                mSearchPeriodselectionval = "q3"
            }
            if (HomeFlow.notificationfragenable) {
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.not_mHomePeriodselection = 3
                    HomeFlow.not_mHomePeriodselectionval = "q3"
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.not_mFinancialPeriodselection = 3
                    HomeFlow.not_mFinancialPeriodselectionval = "q3"
                } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                    HomeFlow.not_mForumPeriodselection = 3
                    HomeFlow.not_mForumPeriodselectionval = "q3"
                } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                    HomeFlow.not_mForumPeriodselection = 3
                    HomeFlow.not_mForumPeriodselectionval = "q3"
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    HomeFlow.not_mSearchPeriodselection = 3
                    HomeFlow.not_mSearchPeriodselectionval = "q3"
                }
            }
            mperiodselection = 3
            mBinding.periodselection = mperiodselection
            mHomeViewModel.mPeriod.value = "q3"
            mHomeViewModel.getCompanyDocumetn()
        }
        mBinding.fourthQtr.setOnClickListener {
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                mHomePeriodselection = 4
                mHomePeriodselectionval = "q4"
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                mFinancialPeriodselection = 4
                mFinancialPeriodselectionval = "q4"
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                mSearchPeriodselection = 4
                mSearchPeriodselectionval = "q4"
            }

            if (HomeFlow.notificationfragenable) {
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.not_mHomePeriodselection = 4
                    HomeFlow.not_mHomePeriodselectionval = "q4"
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.not_mFinancialPeriodselection = 4
                    HomeFlow.not_mFinancialPeriodselectionval = "q4"
                } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                    HomeFlow.not_mForumPeriodselection = 4
                    HomeFlow.not_mForumPeriodselectionval = "q4"
                } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                    HomeFlow.not_mForumPeriodselection = 4
                    HomeFlow.not_mForumPeriodselectionval = "q4"
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    HomeFlow.not_mSearchPeriodselection = 4
                    HomeFlow.not_mSearchPeriodselectionval = "q4"
                }
            }
            mperiodselection = 4
            mBinding.periodselection = mperiodselection
            mHomeViewModel.mPeriod.value = "q4"
            mHomeViewModel.getCompanyDocumetn()
        }

    }


    override fun onYearItemClick(yearsDTO: YearsDTO) {
        mBinding.yearTv.text = "${yearsDTO.year}"
        mHomeViewModel.myear.value = "${yearsDTO.year}"
        mHomeViewModel.getCompanyDocumetn()
        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            mHomeSelectedYear = "${yearsDTO.year}"
        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
            mFinancialSelectedYear = "${yearsDTO.year}"
        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
            mSearchSelectedYear = "${yearsDTO.year}"
        }

        if (HomeFlow.notificationfragenable) {
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.not_mHomeSelectedYear = "${yearsDTO.year}"
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.not_mFinancialSelectedYear = "${yearsDTO.year}"
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                HomeFlow.not_mForumSelectedYear = "${yearsDTO.year}"
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                HomeFlow.not_mNewsSelectedYear = "${yearsDTO.year}"
            } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                HomeFlow.not_mSearchSelectedYear = "${yearsDTO.year}"
            }
        }
        mBinding.yearselect = false
    }

    override fun onDocumentClick(companyStatementsDTO: CompanyStatementsDTO) {

        recordView(companyStatementsDTO.document_name)
        if (mBinding.yearselect == true) {
            mBinding.yearselect = false
        }
        if (lockunlock == 1) {

            lifecycleScope.launch {
                val result =
                    HomeRepository.postOpenCompany(mHomeViewModel.companyID.value.toString(), 1)
                when (result) {
                    is ResultWrapper.Success -> {
                        if (result.response.status) {
                            val companydetail = Gson().toJson(companyStatementsDTO)

                            val navargs = bundleOf("document_link" to companydetail)
                            findNavController().navigate(
                                R.id.action_financialStatementfrag_to_companyDocumentView,
                                navargs
                            )
                        }
                    }
                    is ResultWrapper.Failure -> {
                        successDialog(
                            requireActivity(),
                            "ALert!",
                            "Please upgrade your subscription firstly."
                        )

                        Log.d("open_companyerr", result.errorMessage.toString())
                    }
                }
            }

        } else {
            SubscriptionlimitDialog()
        }
        // temp_showToast("${companyStatementsDTO.document_name}")
    }

    private fun recordView(documentName: String) {
        /*    "Income Statement",
              "Balance Sheet",
              "Equity Statement",
              "Cash Flow Statement",
              "Comprehensive Income Statement",
              "Notes",
              "Annual Report",
              "Financial Report",*/
         if (documentName.equals("Income Statement")){
              mHomeViewModel.recordEvent("company_income_statement_${COMPANY_SYMBOL}_view")
        }
        else if (documentName.equals("Balance Sheet")){
             mHomeViewModel.recordEvent("company_balance_sheet_${COMPANY_SYMBOL}_view")
        }
         else if (documentName.equals("Equity Statement")){
             mHomeViewModel.recordEvent("company_equity_statement_${COMPANY_SYMBOL}_view")
        }else if (documentName.equals("Cash Flow Statement")) {
             mHomeViewModel.recordEvent("company_cashflow_statement_${COMPANY_SYMBOL}_view")
        }
         else if (documentName.equals("Comprehensive Statement")) {
             mHomeViewModel.recordEvent("company_comprehensive_statement_${COMPANY_SYMBOL}_view")
        }
         else if (documentName.equals("Notes")) {
             mHomeViewModel.recordEvent("company_notes_${COMPANY_SYMBOL}_view")
        }
          else if (documentName.equals("Annual Report")){
             mHomeViewModel.recordEvent("company_annual_report_${COMPANY_SYMBOL}_view")
        }
       else if (documentName.equals( "Financial Report" )) {

             mHomeViewModel.recordEvent("company_financial_report_${COMPANY_SYMBOL}_view")
        }



    }

    override fun onInfoClick(companyStatementsDTO: CompanyStatementsDTO) {
        if (mBinding.yearselect == true) {
            mBinding.yearselect = false
        }
        documentDescription(
            companyStatementsDTO.document_name,
            companyStatementsDTO.document_infor!!
        )
    }

    override fun onPause() {
        mBinding.statementList.clearAnimation()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }


    private fun documentDescription(mtitle: String, mdescription: String) {
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_document_info, null)
        val btnClose = view.findViewById<ImageView>(R.id.document_iv)
        val title = view.findViewById<TextView>(R.id.document_title)
        val description = view.findViewById<TextView>(R.id.document_description)

        title.text = "$mtitle"
        description.text = "$mdescription"
        btnClose.setOnClickListener {
            // on below line we are calling a dismiss
            // method to close our dialog.
            bottomSheet.dismiss()
        }


        bottomSheet.setContentView(view)


        bottomSheet.show()

    }


    private fun postOpenCompany(company_id: String) {

        lifecycleScope.launch {
            val result = HomeRepository.postOpenCompany(company_id, 0)
            when (result) {
                is ResultWrapper.Success -> {
                    if (result.response.status) {
                        lockunlock = 1
                        mDocAdapter.notifyDataSetChanged()
                    }
                    Log.d("open_companydto", result.response.toString())
                }
                is ResultWrapper.Failure -> {
                    if (result.status_code == 403) {
                        lockunlock = 0
                        mDocAdapter.notifyDataSetChanged()
                        upgradelaertdilaog = 1
                    }
                    Log.d("open_companyerr", result.errorMessage.toString())
                }
            }
        }

    }

    private fun SubscriptionlimitDialog() {
        // findNavController().navigate(R.id.otp_Fragment)
        val mBinding = SuscessDialogBinding.inflate(LayoutInflater.from(requireActivity()))
        val builder = Dialog(requireActivity())
        mBinding.dialogTitle.text = "Alert!"
        mBinding.dialogSubTitle.text =
            if (SharedPrefrenceHelper.user.subscription_type.equals("Monthly")) {
                "Company limit over for Monthly plan "
            } else {
                "Company limit over for Free plan "
            }
        mBinding.dialogOk.text = "Upgrade"
        mBinding.dialogOk.setOnClickListener {
            mBinding.dialogOk.setBackgroundColor(Color.parseColor("#BFDDED"))

            Handler().postDelayed({
                mBinding.dialogCancel.setBackgroundColor(Color.TRANSPARENT)
                builder.dismiss()
                findNavController().navigate(R.id.subscriptionDialog)
            }, 50)
        }
        mBinding.dialogCancel.text = "Cancel"
        mBinding.dialogCancel.setOnClickListener {
            mBinding.dialogCancel.setBackgroundColor(Color.parseColor("#BFDDED"))
            Handler().postDelayed({
                mBinding.dialogCancel.setBackgroundColor(Color.TRANSPARENT)
                builder.dismiss()
            }, 50)

        }
        builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
        builder.setContentView(mBinding.root)
        builder.setCancelable(false)
        builder.setCanceledOnTouchOutside(false)
        builder.show()


    }

}