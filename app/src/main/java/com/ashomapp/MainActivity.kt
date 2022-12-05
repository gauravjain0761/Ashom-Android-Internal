package com.ashomapp

import android.animation.ObjectAnimator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ashomapp.databinding.ActivityMainBinding

import android.view.animation.OvershootInterpolator
import android.widget.Switch
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd

import androidx.core.animation.doOnStart
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.ashomapp.database.AshomDBHelper
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.network.response.NOtificationDataNews
import com.ashomapp.network.response.NotificationData
import com.ashomapp.network.response.NotifiicationFirebaseMetaData
import com.ashomapp.network.response.NotifiicationMetaData
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.forum.ForumFragContainer
import com.ashomapp.presentation.home.FinancialStatementfrag
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.presentation.home.HomeViewModel
import com.ashomapp.utils.*
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.Exception


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private val mHomeViewModel: HomeViewModel by viewModels()
    var destination_Id: Int = 0
    val db = AshomDBHelper(AshomAppApplication.instance.applicationContext, null)
    val botttombaranimationdur: Long = 400
    var lastCompanyID: String = "0"
    private lateinit var reviewmanager: ReviewManager

    companion object {
        var financialtab: Boolean = false
        var financiallistanimation: Int = 0
        var usertoken = MutableLiveData<String>()
        var layoutheight = MutableLiveData<Int>(0)
        val countrylist = ArrayList<CountriesDTO>()
        val companiesList = ArrayList<CompanyDTO>()
        val intentforNews = MutableLiveData<String>()
        val intentforForum = MutableLiveData<String>()
        val intentforCompany = MutableLiveData<String>()


        var animationBoolewan = MutableLiveData<Boolean>(false)

        var enablehome = false

        val bottomhideunhide = MutableLiveData<Boolean>(false)


    }


    override fun onStart() {
        super.onStart()
        animationBoolewan.value = true

        Log.d("listloading", "${animationBoolewan.value}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        reviewsetUp()
        getCountry()
        getCompanies()

        getLastCompanyID()

        //This condition only for one time company load when application intall
        val list = db.getCompanyDataFromCOuntry("")


        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                try {
                    val token = SharedPrefrenceHelper.token
                    Log.d("error", token.toString())
                    if (!token.equals("0")) {
                        getNotificationCount()
                    }
                } catch (e: Exception) {
                    Log.d("error", e.localizedMessage)
                }

                handler.postDelayed(this, 3000)
            }
        }
        runnable.run()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        mNavController = navHostFragment.navController
        mNavController.addOnDestinationChangedListener { _, destination, _ ->
            animateNavigationBar(destination)
            if (destination.id.equals(R.id.loginFragment) || destination.id.equals(R.id.contactUs)) {
                getKeyboardHeight()
            }
            destination_Id = destination.id
            bottomhideunhide.observe(this) {
                if (it) {
                    binding.bottomAppBar.visibility = View.GONE
                } else {
                    if (destination_Id == R.id.splashScreen||destination_Id == R.id.fullStockGraphFragment) {
                        binding.bottomAppBar.visibility = View.GONE

                    } else {
                        binding.bottomAppBar.visibility = View.VISIBLE
                    }
                }
            }
            binding.bottomAppBar.isVisible = destination.id in setOf(
                R.id.homeFrag,
                R.id.newsFrag,
                R.id.forumFrag,
                R.id.newsDetail,
                R.id.countryList,
                R.id.searchFrag,
                R.id.changePassword,
                R.id.settingFrag,
                R.id.profileFragment,
                R.id.privacy_policy,
                R.id.company_frag,
                R.id.companyDocumentView,
                R.id.companyDetail,
                R.id.financialStatementfrag,
                R.id.companyNews,
                R.id.companyDocumentView,
                R.id.subscriptionDialog,
                R.id.selectedCompanies,
                R.id.notificationFrag,
                R.id.forumComments,
                R.id.forumReply,
                R.id.stockPriceFragment,
                R.id.stockDetailsFragment,

            )
        }
        bottomAppbar()
        //For notification
        ///Firstly check enablehome bolean
        try {
            val intent = intent.extras
            val type = intent?.getString("type").toString()

            Log.d("notintent", " Notificationdata : ${intent.toString()}")
            if (type.equals("News")) {

                val notification = intent?.getString("metadata").toString()
                val newsitem = Gson().fromJson(
                    notification,
                    NotifiicationFirebaseMetaData::class.java
                )
                if (newsitem.type.equals("News")) {
                    HomeFlow.notificationfragenable = true
                    val Snewsitem = Gson().fromJson(
                        newsitem.data.toString(),
                        NOtificationDataNews::class.java
                    )
                    val newitemdto = NewsItemDTO(
                        Snewsitem.title,
                        Snewsitem.date,
                        Snewsitem.image_url,
                        Snewsitem.source,
                        Snewsitem.link
                    )

                    val newstostring = Gson().toJson(newitemdto)
                    intentforNews.value = newstostring

                    binding.mainReader2.visibility = View.VISIBLE

                    binding.mainHome2.visibility = View.INVISIBLE
                    focus(binding.mainReader2)
                    focus(binding.mainHome2)
                }
            } else if (type.equals("Forum")) {
                mHomeViewModel.getForum()
                intentforForum.value = "Forum"
                animateonlyForum()
            } else if (type.equals("Financial Report")) {
                val notification = intent?.getString("metadata")
                HomeFlow.notificationfragenable = true
                animateonlyhome()
                if (!notification.isNullOrEmpty()) {
                    val notificationq = notification.replace("[", "")

                    HomeFlow.notificationfragenable = true
                    Log.d("notintent", "${notification[0]}")

                    val newsitem = Gson().fromJson(
                        notificationq.replace("]", ""),
                        NotificationData::class.java
                    )
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
                    intentforCompany.value = companydetail1


                }
                Log.d("notintent", "Array metadata : $notification")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("notintent", " Notificationdata : ${e.message}")
        }
    }

    private fun getLastCompanyID() {
        lifecycleScope.launch {
            val result = HomeRepository.getlastcompanyid()
            when (result) {
                is ResultWrapper.Success -> {

                    Log.d("fetchcompany", "Yes ${result.response} , ${SharedPrefrenceHelper.last_company_id}")
                    if (!SharedPrefrenceHelper.last_company_id.equals(result.response)) {

                        lastCompanyID = result.response
                        Log.d("fetchcompany", "Yes 2")
                        getCompanies()
                    }
                }
                is ResultWrapper.Failure -> {
                }
            }
        }
    }

    private fun reviewsetUp() {

        reviewmanager = ReviewManagerFactory.create(this)
        val request = reviewmanager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("reviewdetail", "${task.result}")
                val reviewInfo = task.result
                val flow: Task<Void> = reviewmanager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { task1 ->
                    Log.d("reviewdetail2", "${task1.isSuccessful}")
                }
            } else {
                Log.d("reviewdetailsssssssds", "${task}")

            }
        }
    }


    private fun bottomAppbar() {

        binding.mainHome.setOnClickListener {
            if (destination_Id != R.id.homeFrag) {
                if (HomeFlow.sectionBottomID != R.id.homeFrag) {
                    gotoHome()
                } else {
                    HomeFlow.hometonotification = false
                    HomeFlow.home_to_profile = false
                    mNavController.navigate(R.id.homeFrag)
                }
            }
        }
        binding.mainSetting.setOnClickListener {
            if (destination_Id != R.id.countryList || HomeFlow.sectionBottomID != R.id.countryList) {
                if (HomeFlow.sectionBottomID != R.id.countryList) {
                    clicktoCountryList()
                } else {
                    HomeFlow.countrylist_to_notification = false
                    HomeFlow.financial_to_profile = false
                    mNavController.navigate(R.id.countryList)
                }
            }
        }
        binding.mainForum.setOnClickListener {
            if (destination_Id != R.id.forumFrag) {
                if (HomeFlow.sectionBottomID != R.id.forumFrag) {
                    clicktoForumTab()
                } else {
                    HomeFlow.fourm_to_notification = false
                    HomeFlow.forum_to_profile = false
                    mNavController.navigate(R.id.forumFrag)
                }
            }
        }
        binding.mainReader.setOnClickListener {
            if (destination_Id != R.id.newsFrag || HomeFlow.sectionBottomID != R.id.newsFrag) {
                Log.d("newtab", "1")
                if (HomeFlow.sectionBottomID != R.id.newsFrag) {
                    gotoNewsTab()
                } else {
                    HomeFlow.news_to_notification = false
                    HomeFlow.news_to_profile = false
                    HomeFlow.newsFragCurrentID = R.id.newsFrag
                    mNavController.navigate(R.id.newsFrag)
                }
            }
            Log.d("newtab", "0")
        }
        binding.mainSearch.setOnClickListener {
            if (destination_Id != R.id.searchFrag) {
                if (HomeFlow.sectionBottomID != R.id.searchFrag) {
                    clicktoSearchFrag()
                } else {
                    HomeFlow.search_to_notification = false
                    HomeFlow.search_to_profile = false
                    mNavController.navigate(R.id.searchFrag)
                }
            }
        }
    }

    fun gotoHome() {
        if (HomeFlow.sectionBottomID != R.id.homeFrag || HomeFlow.profilefragenable == true
            || HomeFlow.notificationfragenable == true
        ) {
            HomeFlow.sectionBottomID = R.id.homeFrag
            HomeFlow.profilefragenable = false
            HomeFlow.notificationfragenable = false

            // not pro with - out stack

            /*  if (HomeFlow.currentFragID == R.id.countryList) {
                  mNavController.navigate(R.id.countryList)
                  animateonlyhome()
              } else if (HomeFlow.currentFragID == R.id.company_frag) {
                  Log.d("homesection", "Home ${HomeFlow.HomecompanyNameBundle}")
                  val argument = bundleOf("country_name" to HomeFlow.HomecompanyNameBundle)

                  mNavController.navigate(R.id.company_frag, argument)
                  animateonlyhome()
              } else if (HomeFlow.currentFragID == R.id.companyDetail) {
                  mNavController.navigate(R.id.companyDetail, HomeFlow.companydetailbundle)
                  animateonlyhome()
              } else if (HomeFlow.currentFragID == R.id.companyNews) {
                  mNavController.navigate(R.id.companyNews, HomeFlow.homecompanynewsBundle)
                  animateonlyhome()
              } else if (HomeFlow.currentFragID == R.id.newsFrag) {
                  mNavController.navigate(R.id.newsFrag)
                  HomeFlow.home_to_news = true
                  animateonlyhome()
              } else if (HomeFlow.currentFragID == R.id.newsDetail) {
                  HomeFlow.home_to_news = true
                  Log.d("newtab", "Hometodetail ${HomeFlow.hometonewsdetailbundle}")
                  mNavController.navigate(R.id.newsDetail, HomeFlow.hometonewsdetailbundle)
                  animateonlyhome()
              } else if (HomeFlow.currentFragID == R.id.financialStatementfrag) {
                  mNavController.navigate(
                      R.id.financialStatementfrag,
                      HomeFlow.financialstatementbundle
                  )
                  animateonlyhome()
              } else {
                  mNavController.navigate(R.id.homeFrag)
              }
  */
            // not pro with stack


            if (HomeFlow.hometonotification == true) {

                if (HomeFlow.hometoNotificationCurrentFragID == R.id.newsDetail) {
                    HomeFlow.notificationfragenable = true
                    mNavController.navigate(
                        R.id.newsDetail,
                        HomeFlow.hometoNotification_to_news_Bundle
                    )
                    animateonlyhome()
                } else if (HomeFlow.hometoNotificationCurrentFragID == R.id.financialStatementfrag) {
                    HomeFlow.notificationfragenable = true

                    FinancialStatementfrag.mNotificationPeriodselection =
                        HomeFlow.not_mHomePeriodselection
                    FinancialStatementfrag.mNotificationPeriodselectionval =
                        HomeFlow.not_mHomePeriodselectionval
                    FinancialStatementfrag.mNotificationSelectedYear =
                        HomeFlow.not_mHomeSelectedYear

                    mNavController.navigate(
                        R.id.financialStatementfrag,
                        HomeFlow.hometoNotification_to_news_Bundle
                    )
                    animateonlyhome()
                } else {
                    HomeFlow.hometonotification = false
                    mNavController.navigate(R.id.notificationFrag)
                    animateonlyhome()
                }

            } else if (HomeFlow.home_to_profile == true) {
                if (HomeFlow.home_settingcurrentID == R.id.profileFragment) {
                    mNavController.navigate(R.id.profileFragment)
                    animateonlyhome()
                } else if (HomeFlow.home_settingcurrentID == PRIVACY_POLICY) {
                    val args = bundleOf("ppurl" to PRIVACY_POLICY_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    animateonlyhome()
                } else if (HomeFlow.home_settingcurrentID == ABOUT_US) {
                    val args = bundleOf("ppurl" to ABOUT_US_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    animateonlyhome()
                } else if (HomeFlow.home_settingcurrentID == TERMS_CONDITION) {
                    val args = bundleOf("ppurl" to TERMS_CONDITION_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    animateonlyhome()
                } else if (HomeFlow.home_settingcurrentID == R.id.changePassword) {
                    mNavController.navigate(R.id.changePassword)
                    animateonlyhome()
                } else if (HomeFlow.home_settingcurrentID == R.id.contactUs) {
                    mNavController.navigate(R.id.contactUs)
                    animateonlyhome()
                } else {
                    HomeFlow.home_to_profile = false
                    mNavController.navigate(R.id.settingFrag)
                    animateonlyhome()
                }
            } else {
                if (HomeFlow.currentFragID == R.id.countryList) {
                    mNavController.navigate(R.id.countryList)
                    animateonlyhome()
                } else if (HomeFlow.currentFragID == R.id.company_frag) {
                    Log.d("homesection", "Home ${HomeFlow.HomecompanyNameBundle}")
                    val argument = bundleOf("country_name" to HomeFlow.HomecompanyNameBundle)

                    mNavController.navigate(R.id.company_frag, argument)
                    animateonlyhome()
                } else if (HomeFlow.currentFragID == R.id.companyDetail) {
                    mNavController.navigate(R.id.companyDetail, HomeFlow.companydetailbundle)
                    animateonlyhome()
                } else if (HomeFlow.currentFragID == R.id.companyNews) {
                    mNavController.navigate(R.id.companyNews, HomeFlow.homecompanynewsBundle)
                    animateonlyhome()
                } else if (HomeFlow.currentFragID == R.id.newsFrag) {
                    mNavController.navigate(R.id.newsFrag)
                    HomeFlow.home_to_news = true
                    animateonlyhome()
                } else if (HomeFlow.currentFragID == R.id.newsDetail) {
                    HomeFlow.home_to_news = true
                    Log.d("newtab", "Hometodetail ${HomeFlow.hometonewsdetailbundle}")
                    mNavController.navigate(R.id.newsDetail, HomeFlow.hometonewsdetailbundle)
                    animateonlyhome()
                } else if (HomeFlow.currentFragID == R.id.financialStatementfrag) {
                    mNavController.navigate(
                        R.id.financialStatementfrag,
                        HomeFlow.financialstatementbundle
                    )
                    animateonlyhome()
                } else {
                    mNavController.navigate(R.id.homeFrag)
                }
            }
        } else {
            HomeFlow.sectionBottomID = R.id.homeFrag
            HomeFlow.homecompanyrecyclervisibleitem = 0
            mNavController.navigate(R.id.homeFrag)
        }
    }

    fun gotoNewsTab() {
        if (HomeFlow.sectionBottomID != R.id.newsFrag || HomeFlow.profilefragenable == true
            || HomeFlow.notificationfragenable == true || HomeFlow.home_to_news == false
        ) {
            HomeFlow.profilefragenable = false
            HomeFlow.notificationfragenable = false

            // not pro without stack

            /* if (HomeFlow.sectionBottomID != R.id.newsFrag || destination_Id != R.id.newsFrag) {
                 if (HomeFlow.newsFragCurrentID == R.id.newsDetail) {
                     Log.d("newtab", "32 ${HomeFlow.newsFragCurrentID}")
                     HomeFlow.sectionBottomID = R.id.newsFrag
                     mNavController.navigate(R.id.newsDetail, HomeFlow.newDetailBundle)
                     animateonltNews()
                 } else {
                     HomeFlow.sectionBottomID = R.id.newsFrag
                     Log.d("newtab", "31 ${HomeFlow.newsFragCurrentID}")
                     mNavController.navigate(R.id.newsFrag)
                     animateonltNews()
                 }
             }*/

            // not pro with stack

            if (HomeFlow.news_to_notification == true) {
                HomeFlow.sectionBottomID = R.id.newsFrag
                Log.d("newtab", "4")
                if (HomeFlow.newstoNotificationCurrentFragID == R.id.newsDetail) {
                    HomeFlow.notificationfragenable = true
                    mNavController.navigate(
                        R.id.newsDetail,
                        HomeFlow.newstoNotification_to_news_Bundle
                    )
                    animateonltNews()
                } else if (HomeFlow.newstoNotificationCurrentFragID == R.id.financialStatementfrag) {
                    HomeFlow.notificationfragenable = true

                    FinancialStatementfrag.mNotificationPeriodselection =
                        HomeFlow.not_mNewsPeriodselection
                    FinancialStatementfrag.mNotificationPeriodselectionval =
                        HomeFlow.not_mNewsPeriodselectionval
                    FinancialStatementfrag.mNotificationSelectedYear =
                        HomeFlow.not_mNewsSelectedYear

                    mNavController.navigate(
                        R.id.financialStatementfrag,
                        HomeFlow.newstoNotification_to_Financailstatement_Bundle
                    )
                    animateonltNews()
                } else {
                    mNavController.navigate(R.id.notificationFrag)
                    animateonltNews()
                }

            } else if (HomeFlow.news_to_profile == true) {
                if (HomeFlow.news_settingcurrentID == R.id.profileFragment) {
                    HomeFlow.sectionBottomID = R.id.newsFrag
                    mNavController.navigate(R.id.profileFragment)
                    animateonltNews()
                } else if (HomeFlow.news_settingcurrentID == PRIVACY_POLICY) {
                    HomeFlow.sectionBottomID = R.id.newsFrag
                    val args = bundleOf("ppurl" to PRIVACY_POLICY_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    animateonltNews()
                } else if (HomeFlow.news_settingcurrentID == ABOUT_US) {
                    HomeFlow.sectionBottomID = R.id.newsFrag
                    val args = bundleOf("ppurl" to ABOUT_US_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    animateonltNews()
                } else if (HomeFlow.news_settingcurrentID == TERMS_CONDITION) {
                    HomeFlow.sectionBottomID = R.id.newsFrag
                    val args = bundleOf("ppurl" to TERMS_CONDITION_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    animateonltNews()
                } else if (HomeFlow.news_settingcurrentID == R.id.changePassword) {
                    HomeFlow.sectionBottomID = R.id.newsFrag
                    mNavController.navigate(R.id.changePassword)
                    animateonltNews()
                } else if (HomeFlow.news_settingcurrentID == R.id.contactUs) {
                    HomeFlow.sectionBottomID = R.id.newsFrag
                    mNavController.navigate(R.id.contactUs)
                    animateonltNews()
                } else {
                    Log.d("newtab", "4")
                    HomeFlow.sectionBottomID = R.id.newsFrag
                    mNavController.navigate(R.id.settingFrag)
                    animateonltNews()
                }
            } else {
                Log.d("newtab", "5")
                if (HomeFlow.sectionBottomID != R.id.newsFrag || destination_Id != R.id.newsFrag) {
                    if (HomeFlow.newsFragCurrentID == R.id.newsDetail) {
                        Log.d("newtab", "32 ${HomeFlow.newsFragCurrentID}")
                        HomeFlow.sectionBottomID = R.id.newsFrag
                        mNavController.navigate(R.id.newsDetail, HomeFlow.newDetailBundle)
                        animateonltNews()
                    } else {
                        HomeFlow.sectionBottomID = R.id.newsFrag
                        Log.d("newtab", "31 ${HomeFlow.newsFragCurrentID}")
                        mNavController.navigate(R.id.newsFrag)
                        animateonltNews()
                    }
                }
            }
        } else {
            HomeFlow.newsFragCurrentID = R.id.newsFrag
            mNavController.navigate(R.id.newsFrag)
        }
    }

    fun clicktoForumTab() {
        if (HomeFlow.sectionBottomID != R.id.forumFrag || HomeFlow.profilefragenable == true || HomeFlow.notificationfragenable == true) {
            HomeFlow.sectionBottomID = R.id.forumFrag
            HomeFlow.profilefragenable = false
            HomeFlow.notificationfragenable = false
            HomeFlow.home_to_news = false

            //  pro with stack

            /*if (HomeFlow.forumCurrentID == R.id.newsDetail) {
                mNavController.navigate(R.id.newsDetail, HomeFlow.forumtonewsDetailBundle)
                animateonlyForum()
            } else if (HomeFlow.forumCurrentID == R.id.forumComments) {
                mNavController.navigate(R.id.forumComments, HomeFlow.forumCommentBundle)
                animateonlyForum()
            } else if (HomeFlow.forumCurrentID == R.id.forumReply) {
                if (!HomeFlow.forumreplyCommentBundle.isNullOrEmpty()) {
                    val args =
                        HomeFlow.forumreplyCommentBundle[HomeFlow.forumreplyCommentBundle.size - 1]
                    mNavController.navigate(R.id.forumReply, args)
                    animateonlyForum()
                } else {
                    mNavController.navigate(R.id.forumComments, HomeFlow.forumCommentBundle)
                }
            } else {
                mNavController.navigate(R.id.forumFrag)
            }
*/
            // not pro with stack
            if (HomeFlow.fourm_to_notification == true) {

                if (HomeFlow.forumtoNotificationCurrentFragID == R.id.newsDetail) {
                    HomeFlow.notificationfragenable = true
                    mNavController.navigate(
                        R.id.newsDetail,
                        HomeFlow.forumtoNotification_to_news_Bundle
                    )
                    animateonlyForum()
                } else if (HomeFlow.forumtoNotificationCurrentFragID == R.id.financialStatementfrag) {
                    HomeFlow.notificationfragenable = true

                    FinancialStatementfrag.mNotificationPeriodselection =
                        HomeFlow.not_mForumPeriodselection
                    FinancialStatementfrag.mNotificationPeriodselectionval =
                        HomeFlow.not_mForumPeriodselectionval
                    FinancialStatementfrag.mNotificationSelectedYear =
                        HomeFlow.not_mForumSelectedYear

                    mNavController.navigate(
                        R.id.financialStatementfrag,
                        HomeFlow.forumtoNotification_to_Financailstatement_Bundle
                    )
                    animateonlyForum()
                } else {
                    HomeFlow.fourm_to_notification = false
                    mNavController.navigate(R.id.notificationFrag)
                    animateonlyForum()
                }


            } else if (HomeFlow.forum_to_profile == true) {
                if (HomeFlow.forum_settingcurrentID == R.id.profileFragment) {
                    mNavController.navigate(R.id.profileFragment)
                    animateonlyForum()
                } else if (HomeFlow.forum_settingcurrentID == PRIVACY_POLICY) {
                    val args = bundleOf("ppurl" to PRIVACY_POLICY_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    animateonlyForum()
                } else if (HomeFlow.forum_settingcurrentID == ABOUT_US) {
                    val args = bundleOf("ppurl" to ABOUT_US_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    animateonlyForum()
                } else if (HomeFlow.forum_settingcurrentID == TERMS_CONDITION) {
                    val args = bundleOf("ppurl" to TERMS_CONDITION_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    animateonlyForum()
                } else if (HomeFlow.forum_settingcurrentID == R.id.changePassword) {
                    mNavController.navigate(R.id.changePassword)
                    animateonlyForum()
                } else if (HomeFlow.forum_settingcurrentID == R.id.contactUs) {
                    mNavController.navigate(R.id.contactUs)
                    animateonlyForum()
                } else {
                    HomeFlow.forum_to_profile = false
                    mNavController.navigate(R.id.settingFrag)
                    animateonlyForum()
                }
            } else {
                if (HomeFlow.forumCurrentID == R.id.newsDetail) {
                    mNavController.navigate(R.id.newsDetail, HomeFlow.forumtonewsDetailBundle)
                    animateonlyForum()
                } else if (HomeFlow.forumCurrentID == R.id.forumComments) {
                    mNavController.navigate(R.id.forumComments, HomeFlow.forumCommentBundle)
                    animateonlyForum()
                } else if (HomeFlow.forumCurrentID == R.id.forumReply) {
                    if (!HomeFlow.forumreplyCommentBundle.isNullOrEmpty()) {
                        val args =
                            HomeFlow.forumreplyCommentBundle[HomeFlow.forumreplyCommentBundle.size - 1]
                        mNavController.navigate(R.id.forumReply, args)
                        animateonlyForum()
                    } else {
                        mNavController.navigate(R.id.forumComments, HomeFlow.forumCommentBundle)
                    }
                } else {
                    mNavController.navigate(R.id.forumFrag)
                }
            }
        } else {
            mNavController.navigate(R.id.forumFrag)
        }

    }

    fun clicktoCountryList() {
        if (HomeFlow.sectionBottomID != R.id.countryList || HomeFlow.profilefragenable == true || HomeFlow.notificationfragenable == true) {
            HomeFlow.profilefragenable = false
            HomeFlow.notificationfragenable = false

            //without - not pro stack

            /* if (destination_Id != R.id.countryList || HomeFlow.currentFragID == R.id.countryList && HomeFlow.sectionBottomID != R.id.countryList) {
                 HomeFlow.sectionBottomID = R.id.countryList
                 financialtab = false
                 if (HomeFlow.financialcurrentFragID == R.id.company_frag) {
                     val argument =
                         bundleOf("country_name" to HomeFlow.FinancialcompanyNameBundle)
                     Log.d("homesection", "Home ${HomeFlow.FinancialcompanyNameBundle}")
                     mNavController.navigate(R.id.company_frag, argument)
                     animateonlyCOuntrylist()
                 } else if (HomeFlow.financialcurrentFragID == R.id.companyDetail) {
                     mNavController.navigate(
                         R.id.companyDetail,
                         HomeFlow.financialcompanydetailbundle
                     )
                     animateonlyCOuntrylist()
                 } else if (HomeFlow.financialcurrentFragID == R.id.financialStatementfrag) {
                     mNavController.navigate(
                         R.id.financialStatementfrag,
                         HomeFlow.Financial_financialstatementbundle
                     )
                     animateonlyCOuntrylist()
                 } else if (HomeFlow.financialcurrentFragID == R.id.companyNews) {
                     mNavController.navigate(
                         R.id.companyNews,
                         HomeFlow.financialcompanynewsBundle
                     )
                     animateonlyCOuntrylist()
                 } else {
                     mNavController.navigate(R.id.countryList)
                 }
             }*/

            // not pro with stack

            if (HomeFlow.countrylist_to_notification == true) {
                HomeFlow.sectionBottomID = R.id.countryList

                if (HomeFlow.financialtoNotificationCurrentFragID == R.id.newsDetail) {
                    HomeFlow.notificationfragenable = true
                    mNavController.navigate(
                        R.id.newsDetail,
                        HomeFlow.financialtoNotification_to_news_Bundle
                    )
                    animateonlyCOuntrylist()
                } else if (HomeFlow.financialtoNotificationCurrentFragID == R.id.financialStatementfrag) {
                    HomeFlow.notificationfragenable = true

                    FinancialStatementfrag.mNotificationPeriodselection =
                        HomeFlow.not_mFinancialPeriodselection
                    FinancialStatementfrag.mNotificationPeriodselectionval =
                        HomeFlow.not_mFinancialPeriodselectionval
                    FinancialStatementfrag.mNotificationSelectedYear =
                        HomeFlow.not_mFinancialSelectedYear

                    mNavController.navigate(
                        R.id.financialStatementfrag,
                        HomeFlow.financialtoNotification_to_Financailstatement_Bundle
                    )
                    animateonlyCOuntrylist()
                } else {
                    HomeFlow.countrylist_to_notification = false
                    mNavController.navigate(R.id.notificationFrag)
                    animateonlyCOuntrylist()
                }


            } else if (HomeFlow.financial_to_profile == true) {
                if (HomeFlow.financial_settingCurrentID == R.id.profileFragment) {
                    HomeFlow.sectionBottomID = R.id.countryList
                    mNavController.navigate(R.id.profileFragment)
                    animateonlyCOuntrylist()
                } else if (HomeFlow.financial_settingCurrentID == PRIVACY_POLICY) {
                    val args = bundleOf("ppurl" to PRIVACY_POLICY_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    HomeFlow.sectionBottomID = R.id.countryList
                    animateonlyCOuntrylist()
                } else if (HomeFlow.financial_settingCurrentID == ABOUT_US) {
                    val args = bundleOf("ppurl" to ABOUT_US_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    HomeFlow.sectionBottomID = R.id.countryList
                    animateonlyCOuntrylist()
                } else if (HomeFlow.financial_settingCurrentID == TERMS_CONDITION) {
                    val args = bundleOf("ppurl" to TERMS_CONDITION_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    HomeFlow.sectionBottomID = R.id.countryList
                    animateonlyCOuntrylist()
                } else if (HomeFlow.financial_settingCurrentID == R.id.changePassword) {
                    mNavController.navigate(R.id.changePassword)
                    HomeFlow.sectionBottomID = R.id.countryList
                    animateonlyCOuntrylist()
                } else if (HomeFlow.financial_settingCurrentID == R.id.contactUs) {
                    mNavController.navigate(R.id.contactUs)
                    HomeFlow.sectionBottomID = R.id.countryList
                    animateonlyCOuntrylist()
                } else {
                    HomeFlow.sectionBottomID = R.id.countryList
                    HomeFlow.financial_to_profile = false
                    mNavController.navigate(R.id.settingFrag)
                    animateonlyCOuntrylist()
                }
            } else {
                if (destination_Id != R.id.countryList || HomeFlow.currentFragID == R.id.countryList && HomeFlow.sectionBottomID != R.id.countryList) {
                    HomeFlow.sectionBottomID = R.id.countryList
                    financialtab = false
                    if (HomeFlow.financialcurrentFragID == R.id.company_frag) {
                        val argument =
                            bundleOf("country_name" to HomeFlow.FinancialcompanyNameBundle)
                        Log.d("homesection", "Home ${HomeFlow.FinancialcompanyNameBundle}")
                        mNavController.navigate(R.id.company_frag, argument)
                        animateonlyCOuntrylist()
                    } else if (HomeFlow.financialcurrentFragID == R.id.companyDetail) {
                        mNavController.navigate(
                            R.id.companyDetail,
                            HomeFlow.financialcompanydetailbundle
                        )
                        animateonlyCOuntrylist()
                    } else if (HomeFlow.financialcurrentFragID == R.id.financialStatementfrag) {
                        mNavController.navigate(
                            R.id.financialStatementfrag,
                            HomeFlow.Financial_financialstatementbundle
                        )
                        animateonlyCOuntrylist()
                    } else if (HomeFlow.financialcurrentFragID == R.id.companyNews) {
                        mNavController.navigate(
                            R.id.companyNews,
                            HomeFlow.financialcompanynewsBundle
                        )
                        animateonlyCOuntrylist()
                    } else {
                        mNavController.navigate(R.id.countryList)
                    }
                }
            }
        } else {
            HomeFlow.financialcompanyrecyclervisibleitem = 0
            mNavController.navigate(R.id.countryList)
        }

    }

    fun clicktoSearchFrag() {
        if (HomeFlow.sectionBottomID != R.id.searchFrag || HomeFlow.profilefragenable == true || HomeFlow.notificationfragenable == true) {

            HomeFlow.profilefragenable = false
            HomeFlow.notificationfragenable = false
            /// without - not_profile_stack

            /* if (destination_Id != R.id.searchFrag) {
                 HomeFlow.sectionBottomID = R.id.searchFrag
                 if (HomeFlow.searchcurrentFragID == R.id.companyDetail) {
                     mNavController.navigate(
                         R.id.companyDetail,
                         HomeFlow.searchCountrydetailbundle
                     )
                     animateonlySearch()
                 } else if (HomeFlow.searchcurrentFragID == R.id.financialStatementfrag) {
                     mNavController.navigate(
                         R.id.financialStatementfrag,
                         HomeFlow.searchFinanancialbundle
                     )
                     animateonlySearch()
                 } else if (HomeFlow.searchcurrentFragID == R.id.companyNews) {
                     mNavController.navigate(R.id.companyNews, HomeFlow.seacrhcompanynewsBundle)
                     animateonlySearch()
                 } else {
                     mNavController.navigate(R.id.searchFrag)
                 }
             }*/

            ///not_profile_stack

            if (HomeFlow.search_to_notification == true) {

                HomeFlow.sectionBottomID = R.id.searchFrag
                if (HomeFlow.searchtoNotificationCurrentFragID == R.id.newsDetail) {
                    HomeFlow.notificationfragenable = true
                    mNavController.navigate(
                        R.id.newsDetail,
                        HomeFlow.searchtoNotification_to_news_Bundle
                    )
                    animateonlySearch()
                } else if (HomeFlow.searchtoNotificationCurrentFragID == R.id.financialStatementfrag) {
                    HomeFlow.notificationfragenable = true
                    FinancialStatementfrag.mNotificationPeriodselection =
                        HomeFlow.not_mSearchPeriodselection
                    FinancialStatementfrag.mNotificationPeriodselectionval =
                        HomeFlow.not_mSearchPeriodselectionval
                    FinancialStatementfrag.mNotificationSelectedYear =
                        HomeFlow.not_mSearchSelectedYear
                    mNavController.navigate(
                        R.id.financialStatementfrag,
                        HomeFlow.searchtoNotification_to_Financailstatement_Bundle
                    )
                    animateonlySearch()
                } else {
                    HomeFlow.search_to_notification = false
                    mNavController.navigate(R.id.notificationFrag)
                    animateonlySearch()
                }


            } else if (HomeFlow.search_to_profile == true) {
                if (HomeFlow.search_settingcurrentID == R.id.profileFragment) {
                    mNavController.navigate(
                        R.id.profileFragment
                    )
                    HomeFlow.sectionBottomID = R.id.searchFrag
                    animateonlySearch()
                } else if (HomeFlow.search_settingcurrentID == PRIVACY_POLICY) {
                    val args = bundleOf("ppurl" to PRIVACY_POLICY_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    HomeFlow.sectionBottomID = R.id.searchFrag
                    animateonlySearch()
                } else if (HomeFlow.search_settingcurrentID == ABOUT_US) {
                    val args = bundleOf("ppurl" to ABOUT_US_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    HomeFlow.sectionBottomID = R.id.searchFrag
                    animateonlySearch()
                } else if (HomeFlow.search_settingcurrentID == TERMS_CONDITION) {
                    val args = bundleOf("ppurl" to TERMS_CONDITION_URL)
                    mNavController.navigate(R.id.privacy_policy, args)
                    HomeFlow.sectionBottomID = R.id.searchFrag
                    animateonlySearch()
                } else if (HomeFlow.search_settingcurrentID == R.id.changePassword) {
                    mNavController.navigate(R.id.changePassword)
                    HomeFlow.sectionBottomID = R.id.searchFrag
                    animateonlySearch()
                } else if (HomeFlow.search_settingcurrentID == R.id.contactUs) {
                    mNavController.navigate(R.id.contactUs)
                    HomeFlow.sectionBottomID = R.id.searchFrag
                    animateonlySearch()
                } else {
                    HomeFlow.search_to_profile = false
                    mNavController.navigate(
                        R.id.settingFrag
                    )
                    HomeFlow.sectionBottomID = R.id.searchFrag
                    animateonlySearch()
                }
            } else {
                if (destination_Id != R.id.searchFrag) {
                    HomeFlow.sectionBottomID = R.id.searchFrag
                    if (HomeFlow.searchcurrentFragID == R.id.companyDetail) {
                        mNavController.navigate(
                            R.id.companyDetail,
                            HomeFlow.searchCountrydetailbundle
                        )
                        animateonlySearch()
                    } else if (HomeFlow.searchcurrentFragID == R.id.financialStatementfrag) {
                        mNavController.navigate(
                            R.id.financialStatementfrag,
                            HomeFlow.searchFinanancialbundle
                        )
                        animateonlySearch()
                    } else if (HomeFlow.searchcurrentFragID == R.id.companyNews) {
                        mNavController.navigate(R.id.companyNews, HomeFlow.seacrhcompanynewsBundle)
                        animateonlySearch()
                    } else {
                        mNavController.navigate(R.id.searchFrag)
                    }
                }
            }
        } else {
            mNavController.navigate(R.id.searchFrag)
        }

    }


    fun animateonlyhome() {
        binding.mainHome2.visibility = View.VISIBLE
        focus(binding.mainHome2)

        val screenwidth = (binding.mainHome.width.toFloat() / 2) - binding.mainHome2.width / 2

        binding.homeCvIcon.setImageResource(R.drawable.home_tb_fill)
        binding.homeCvIcon.apply {
            setPadding(9)
        }
        /* ObjectAnimator.ofFloat(
             binding.mainHome2,
             "translationX",
             0f
         ).apply {
             duration = 650
             interpolator = OvershootInterpolator()
             start()
         }*/
        binding.mainHome2.animate().x(0f + screenwidth)
            .setDuration(botttombaranimationdur)
            .start()

        unfocus(binding.mainSetting2)
        unfocus(binding.mainSearch2)
        unfocus(binding.mainReader2)
        unfocus(binding.mainForum2)

        tvunfocus(binding.mainForum4)
        tvfocus(binding.mainHome4)
        tvunfocus(binding.mainSetting4)
        tvunfocus(binding.mainReader4)
        tvunfocus(binding.mainSearch4)

        binding.mainForum3.visibility = View.VISIBLE
        binding.mainHome3.visibility = View.INVISIBLE
        binding.mainSetting3.visibility = View.VISIBLE
        binding.mainReader3.visibility = View.VISIBLE
        binding.mainSearch3.visibility = View.VISIBLE

    }

    private fun animateonlyCOuntrylist() {
        val screenwidth = (binding.mainHome.width.toFloat() / 2) - binding.mainHome2.width / 2
        binding.mainHome2.visibility = View.VISIBLE
        if (!financialtab) {
            binding.homeCvIcon.setImageResource(R.drawable.companyanalysis)
            val density = applicationContext.getResources().getDisplayMetrics().density
            val paddingPixel = 7 * density
            binding.homeCvIcon.setPadding(paddingPixel.toInt())
            /* ObjectAnimator.ofFloat(
                 binding.mainHome2,
                 "translationX",
                 binding.mainHome.width.toFloat()
             ).apply {
                 duration = 650
                 interpolator = OvershootInterpolator()
                 start()
             }*/
            binding.mainHome2.animate().x(binding.mainHome.width.toFloat() + screenwidth)
                .setDuration(botttombaranimationdur)
                .start()
            //  focus(binding.mainHome2)

            tvunfocus(binding.mainForum4)
            tvunfocus(binding.mainHome4)
            tvfocus(binding.mainSetting4)
            tvunfocus(binding.mainReader4)
            tvunfocus(binding.mainSearch4)

            binding.mainForum3.visibility = View.VISIBLE
            binding.mainSetting3.visibility = View.INVISIBLE
            binding.mainHome3.visibility = View.VISIBLE
            binding.mainReader3.visibility = View.VISIBLE
            binding.mainSearch3.visibility = View.VISIBLE
        }
    }

    private fun animateonlySearch() {
        val screenwidth = (binding.mainHome.width.toFloat() / 2) - binding.mainHome2.width / 2
        binding.mainHome2.visibility = View.VISIBLE
        binding.homeCvIcon.setImageResource(R.drawable.search_icon_2)
        val density = applicationContext.getResources().getDisplayMetrics().density
        val paddingPixel = 5 * density
        binding.homeCvIcon.setPadding(paddingPixel.toInt())
        /* ObjectAnimator.ofFloat(
             binding.mainHome2,
             "translationX",
             binding.mainHome.width.toFloat() * 4
         ).apply {
             duration = 650
             interpolator = OvershootInterpolator()
             start()

         }*/
        binding.mainHome2.animate().x(binding.mainHome.width.toFloat() * 4 + screenwidth)
            .setDuration(botttombaranimationdur)
            .start()
        //   focus(binding.mainHome2)
        tvunfocus(binding.mainForum4)
        tvunfocus(binding.mainHome4)
        tvunfocus(binding.mainSetting4)
        tvunfocus(binding.mainReader4)
        tvfocus(binding.mainSearch4)

        binding.mainForum3.visibility = View.VISIBLE
        binding.mainSearch3.visibility = View.INVISIBLE
        binding.mainHome3.visibility = View.VISIBLE
        binding.mainReader3.visibility = View.VISIBLE
        binding.mainSetting3.visibility = View.VISIBLE
    }

    fun animateonlyForum() {
        val screenwidth = (binding.mainHome.width.toFloat() / 2) - binding.mainHome2.width / 2
        binding.mainHome2.visibility = View.VISIBLE
        binding.homeCvIcon.setImageResource(R.drawable.forum)
        val density = applicationContext.getResources().getDisplayMetrics().density
        val paddingPixel = 5 * density
        binding.homeCvIcon.setPadding(paddingPixel.toInt())
        if (!intentforForum.value.isNullOrEmpty()) {
            binding.mainForum2.visibility = View.VISIBLE
            binding.mainHome2.visibility = View.GONE
            focus(binding.mainForum2)
        } else {
            binding.mainForum2.visibility = View.GONE
            binding.mainHome2.visibility = View.VISIBLE
            focus(binding.mainForum2)

        }
        Handler()
            .postDelayed({
                /*  ObjectAnimator.ofFloat(
                      binding.mainHome2,
                      "translationX",
                      binding.mainForum.width.toFloat() * 2
                  ).apply {
                      duration = 650
                      interpolator = OvershootInterpolator()
                      start()
                  }*/

            }, 50)
        binding.mainHome2.animate().x(binding.mainHome.width.toFloat() * 2 + screenwidth)
            .setDuration(botttombaranimationdur)
            .start()

        tvfocus(binding.mainForum4)
        tvunfocus(binding.mainHome4)
        tvunfocus(binding.mainSetting4)
        tvunfocus(binding.mainReader4)
        tvunfocus(binding.mainSearch4)

        binding.mainSetting3.visibility = View.VISIBLE
        binding.mainForum3.visibility = View.INVISIBLE
        binding.mainHome3.visibility = View.VISIBLE
        binding.mainReader3.visibility = View.VISIBLE
        binding.mainSearch3.visibility = View.VISIBLE
    }

    fun animateonltNews() {
        val screenwidth = (binding.mainHome.width.toFloat() / 2) - binding.mainHome2.width / 2
        binding.mainHome2.visibility = View.VISIBLE
        binding.homeCvIcon.setImageResource(R.drawable.news_fill)
        val density = applicationContext.getResources().getDisplayMetrics().density
        val paddingPixel = 4 * density
        binding.homeCvIcon.setPadding(paddingPixel.toInt())
        /* ObjectAnimator.ofFloat(
             binding.mainHome2,
             "translationX",
             binding.mainHome.width.toFloat() * 3
         ).apply {
             duration = 650
             interpolator = OvershootInterpolator()
             start()
         }*/
        tvunfocus(binding.mainForum4)
        tvunfocus(binding.mainHome4)
        tvunfocus(binding.mainSetting4)
        tvfocus(binding.mainReader4)
        tvunfocus(binding.mainSearch4)

        binding.mainForum3.visibility = View.VISIBLE
        binding.mainReader3.visibility = View.INVISIBLE
        binding.mainHome3.visibility = View.VISIBLE
        binding.mainSetting3.visibility = View.VISIBLE
        binding.mainSearch3.visibility = View.VISIBLE
        binding.mainHome2.animate().x(binding.mainHome.width.toFloat() * 3 + screenwidth)
            .setDuration(botttombaranimationdur)
            .start()
        //   focus(binding.mainHome2)


    }

    private fun animateNavigationBar(destination: NavDestination) {
        val screenwidth = (binding.mainHome.width.toFloat() / 2) - binding.mainHome2.width / 2
        when (destination.id) {
            R.id.countryList -> {
                binding.mainHome2.visibility = View.VISIBLE
                if (HomeFlow.sectionBottomID == R.id.countryList) {
                    binding.homeCvIcon.setImageResource(R.drawable.companyanalysis)
                    val density = applicationContext.getResources().getDisplayMetrics().density
                    val paddingPixel = 7 * density
                    binding.homeCvIcon.setPadding(paddingPixel.toInt())

                    /* ObjectAnimator.ofFloat(
                         binding.mainHome2,
                         "translationX",
                         binding.mainHome.width.toFloat()
                     ).apply {
                         duration = 650
                         interpolator = OvershootInterpolator()
                         start()
                     }*/
                    binding.mainHome2.animate().x(binding.mainHome.width.toFloat() + screenwidth)
                        .setDuration(botttombaranimationdur)
                        .start()
                    //  focus(binding.mainHome2)

                    tvunfocus(binding.mainForum4)
                    tvunfocus(binding.mainHome4)
                    tvfocus(binding.mainSetting4)
                    tvunfocus(binding.mainReader4)
                    tvunfocus(binding.mainSearch4)

                    binding.mainForum3.visibility = View.VISIBLE
                    binding.mainSetting3.visibility = View.INVISIBLE
                    binding.mainHome3.visibility = View.VISIBLE
                    binding.mainReader3.visibility = View.VISIBLE
                    binding.mainSearch3.visibility = View.VISIBLE
                }
            }
            R.id.forumFrag -> {
                binding.mainHome2.visibility = View.VISIBLE
                binding.homeCvIcon.setImageResource(R.drawable.forum)
                val density = applicationContext.getResources().getDisplayMetrics().density
                val paddingPixel = 5 * density
                binding.homeCvIcon.setPadding(paddingPixel.toInt())
                if (!intentforForum.value.isNullOrEmpty()) {
                    binding.mainForum2.visibility = View.VISIBLE
                    binding.mainHome2.visibility = View.GONE
                    focus(binding.mainForum2)
                } else {
                    binding.mainForum2.visibility = View.GONE
                    binding.mainHome2.visibility = View.VISIBLE
                    focus(binding.mainForum2)

                }
                Handler()
                    .postDelayed({
                        /* ObjectAnimator.ofFloat(
                             binding.mainHome2,
                             "translationX",
                             binding.mainForum.width.toFloat() * 2
                         ).apply {
                             duration = 650
                             interpolator = OvershootInterpolator()
                             start()
                         }*/


                    }, 50)
                binding.mainHome2.animate().x((binding.mainHome.width.toFloat() * 2) + screenwidth)
                    .setDuration(botttombaranimationdur)
                    .start()

                tvfocus(binding.mainForum4)
                tvunfocus(binding.mainHome4)
                tvunfocus(binding.mainSetting4)
                tvunfocus(binding.mainReader4)
                tvunfocus(binding.mainSearch4)

                binding.mainSetting3.visibility = View.VISIBLE
                binding.mainForum3.visibility = View.INVISIBLE
                binding.mainHome3.visibility = View.VISIBLE
                binding.mainReader3.visibility = View.VISIBLE
                binding.mainSearch3.visibility = View.VISIBLE
                ///textview
            }
            R.id.newsFrag -> {
                if (!HomeFlow.home_to_news) {
                    binding.mainHome2.visibility = View.VISIBLE

                    binding.homeCvIcon.setImageResource(R.drawable.news_fill)
                    val density = applicationContext.getResources().getDisplayMetrics().density
                    val paddingPixel = 4 * density
                    binding.homeCvIcon.setPadding(paddingPixel.toInt())
                    /* ObjectAnimator.ofFloat(
                         binding.mainHome2,
                         "translationX",
                         binding.mainHome.width.toFloat() * 3
                     ).apply {
                         duration = 650
                         interpolator = OvershootInterpolator()
                         start()
                     }*/
                    //   focus(binding.mainHome2)
                    binding.mainHome2.animate()
                        .x((binding.mainHome.width.toFloat() * 3) + screenwidth)
                        .setDuration(botttombaranimationdur)
                        .start()

                    tvunfocus(binding.mainForum4)
                    tvunfocus(binding.mainHome4)
                    tvunfocus(binding.mainSetting4)
                    tvfocus(binding.mainReader4)
                    tvunfocus(binding.mainSearch4)

                    binding.mainForum3.visibility = View.VISIBLE
                    binding.mainReader3.visibility = View.INVISIBLE
                    binding.mainHome3.visibility = View.VISIBLE
                    binding.mainSetting3.visibility = View.VISIBLE
                    binding.mainSearch3.visibility = View.VISIBLE
                }
            }
            R.id.searchFrag -> {
                binding.mainHome2.visibility = View.VISIBLE
                binding.homeCvIcon.setImageResource(R.drawable.search_icon_2)
                val density = applicationContext.getResources().getDisplayMetrics().density
                val paddingPixel = 5 * density
                binding.homeCvIcon.setPadding(paddingPixel.toInt())
                /* ObjectAnimator.ofFloat(
                     binding.mainHome2,
                     "translationX",
                     binding.mainHome.width.toFloat() * 4
                 ).apply {
                     duration = 650
                     interpolator = OvershootInterpolator()
                     start()

                 }*/

                binding.mainHome2.animate().x((binding.mainHome.width.toFloat() * 4) + screenwidth)
                    .setDuration(botttombaranimationdur)
                    .start()

                //   focus(binding.mainHome2)
                tvunfocus(binding.mainForum4)
                tvunfocus(binding.mainHome4)
                tvunfocus(binding.mainSetting4)
                tvunfocus(binding.mainReader4)
                tvfocus(binding.mainSearch4)

                binding.mainForum3.visibility = View.VISIBLE
                binding.mainSearch3.visibility = View.INVISIBLE
                binding.mainHome3.visibility = View.VISIBLE
                binding.mainReader3.visibility = View.VISIBLE
                binding.mainSetting3.visibility = View.VISIBLE
            }
            R.id.newsDetail -> {
                //    binding.mainHome2.visibility = View.VISIBLE
                if (!intentforNews.value.isNullOrEmpty()) {

                    binding.homeCvIcon.setImageResource(R.drawable.news_fill)
                    val density = applicationContext.getResources().getDisplayMetrics().density
                    val paddingPixel = 4 * density
                    binding.homeCvIcon.setPadding(paddingPixel.toInt())
                    binding.mainHome2.animate()
                        .x((binding.mainHome.width.toFloat() * 3) + screenwidth)
                        .setDuration(botttombaranimationdur)
                        .start()

                    tvunfocus(binding.mainForum4)
                    tvunfocus(binding.mainHome4)
                    tvunfocus(binding.mainSetting4)
                    tvfocus(binding.mainReader4)
                    tvunfocus(binding.mainSearch4)

                    binding.mainForum3.visibility = View.VISIBLE
                    binding.mainReader3.visibility = View.INVISIBLE
                    binding.mainHome3.visibility = View.VISIBLE
                    binding.mainSetting3.visibility = View.VISIBLE
                    binding.mainSearch3.visibility = View.VISIBLE

                }
            }
            R.id.homeFrag -> {
                binding.mainHome2.visibility = View.VISIBLE
                binding.homeCvIcon.setImageResource(R.drawable.home_tb_fill)
                binding.homeCvIcon.apply {
                    setPadding(9)
                }
                /*ObjectAnimator.ofFloat(
                    binding.mainHome2,
                    "translationX",
                    0f
                ).apply {
                    duration = 650
                    interpolator = OvershootInterpolator()
                    start()
                }*/


                focus(binding.mainHome2)
                unfocus(binding.mainSetting2)
                unfocus(binding.mainSearch2)
                unfocus(binding.mainReader2)
                unfocus(binding.mainForum2)

                tvunfocus(binding.mainForum4)
                tvfocus(binding.mainHome4)
                tvunfocus(binding.mainSetting4)
                tvunfocus(binding.mainReader4)
                tvunfocus(binding.mainSearch4)

                binding.mainForum3.visibility = View.VISIBLE
                binding.mainHome3.visibility = View.INVISIBLE
                binding.mainSetting3.visibility = View.VISIBLE
                binding.mainReader3.visibility = View.VISIBLE
                binding.mainSearch3.visibility = View.VISIBLE

                if (screenwidth.equals(0f)) {
                    Handler().postDelayed({
                        val screenwidth =
                            (binding.mainHome.width.toFloat() / 2) - binding.mainHome2.width / 2
                        Log.d("screenwidth", "$screenwidth")
                        binding.mainHome2.animate().x(screenwidth)
                            .setDuration(botttombaranimationdur)

                            .start()
                    }, 100)
                } else {
                    binding.mainHome2.animate().x(screenwidth)
                        .setDuration(botttombaranimationdur)

                        .start()
                }
            }

            else -> {
                if (enablehome) {
                    focus(binding.mainHome2)
                    binding.mainHome2.visibility = View.VISIBLE
                }
                if (!intentforCompany.value.isNullOrEmpty()) {
                    tvunfocus(binding.mainForum4)
                    tvfocus(binding.mainHome4)
                    tvunfocus(binding.mainSetting4)
                    tvunfocus(binding.mainReader4)
                    tvunfocus(binding.mainSearch4)

                    binding.mainForum3.visibility = View.VISIBLE
                    binding.mainHome3.visibility = View.INVISIBLE
                    binding.mainSetting3.visibility = View.VISIBLE
                    binding.mainReader3.visibility = View.VISIBLE
                    binding.mainSearch3.visibility = View.VISIBLE

                }
                Log.d("fjkf", "fdjf")
            }
        }
    }

    fun getKeyboardHeight() {
        var height = 0
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val r = Rect()
                val rootview = window.decorView
                rootview.getWindowVisibleDisplayFrame(r)
                val screenHeight = rootview.height
                layoutheight.value = screenHeight - (r.bottom - r.top)
                Log.d("Keyboardsize", "${layoutheight.value}")
            }

        })

    }

    fun getCountry() {
        countrylist.clear()
        lifecycleScope.launch {
            val result = HomeRepository.getCountry()
            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("forumdata", result.response.toString())

                    val list = mutableListOf<CountriesDTO>()

                    list.addAll(result.response.map { it })
                    list.forEach {
                        countrylist.add(it)
                    }
                }
                is ResultWrapper.Failure -> {
                    temp_showToast("${result.errorMessage}")
                }
            }
        }
    }


    fun getCompanies() {
        companiesList.clear()
        db.deleteCompany()
        Log.d("fetchcompany", "Yes1")
        lifecycleScope.launch {
            val result = HomeRepository.getSearchCompany("0", "0")
            when (result) {
                is ResultWrapper.Success -> {
                    val companylist = mutableListOf<CompanyDTO>()
                    SharedPrefrenceHelper.last_company_id = lastCompanyID
                    companylist.addAll(result.response.map { it })
                    companiesList.addAll(companylist)

                    companiesList.forEach {
                        Log.d("Companydbitems11", "${it.toString()}")
                        db.addCompany(it)
                    }
                }
                is ResultWrapper.Failure -> {
                    Log.d("Errorcompany", result.errorMessage.toString())
                }
            }
        }
    }


    private fun getNotificationCount() {
        lifecycleScope.launch {
            val token = SharedPrefrenceHelper.token.toString()
            val result = HomeRepository.getNotificationcount(token)
            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("notification_count", result.response.toString() + ", $token")
                    notificationcounter.value = result.response
                }
                is ResultWrapper.Failure -> {
                    Log.d("error", result.errorMessage)
                }
            }
        }
    }
}
