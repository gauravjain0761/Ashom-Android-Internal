package com.ashomapp.presentation.home

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.ashomapp.R
import com.ashomapp.network.response.dashboard.ForumDTO

class HomeFlow {

    companion object{
        var HomeFlowEnable = true

        var countryAnimation = false

        var companydetailanimation = true

        var home_to_news = false


        var scanMe = false

        var home_companynew_detail = false
        var search_companynew_detail = false

        val mFforumlist  = MutableLiveData<List<ForumDTO>>()
        var reloadForum = false

        var profilefragenable = false

        var notificationfragenable = false

        var sectionBottomID : Int = R.id.homeFrag

        var currentFragID : Int = R.id.homeFrag

        //for_home
        var companydetailbundle = Bundle()
        var financialstatementbundle = Bundle()
        var HomecompanyNameBundle = "0"
        var  homecompanynewsBundle  =Bundle()
        var  hometonewsdetailbundle  =Bundle()

        var  searchtonewsdetailbundle  =Bundle()

        var pro_mHomePeriodselection = 0
        var pro_mHomePeriodselectionval = "Annual"
        var pro_mHomeSelectedYear = ""

        var homecompanyrecyclervisibleitem = 0

        var hometonotification = false
        var hometoNotification_to_news_Bundle  = Bundle()
        var hometoNotification_to_Financailstatement_Bundle  = Bundle()
        var hometoNotificationCurrentFragID =  R.id.notificationFrag


        var home_to_profile = false
        var home_settingcurrentID = R.id.settingFrag
        var home_selectedCompanytoComapnyDetail = Bundle()
        var home_selectedComapnytoFinancialStatement = Bundle()

        var companydetailfromsearch = false


        //for_search
        var searchCountrydetailbundle = Bundle()
        var searchFinanancialbundle = Bundle()
        var searchcurrentFragID  : Int = R.id.searchFrag
        var  seacrhcompanynewsBundle  =Bundle()

        var search_to_notification = false
        var searchtoNotification_to_news_Bundle  = Bundle()
        var searchtoNotification_to_Financailstatement_Bundle  = Bundle()
        var searchtoNotificationCurrentFragID =  R.id.notificationFrag

        var search_to_profile = false
        var search_settingcurrentID = R.id.settingFrag

        var search_selectedCompanytoComapnyDetail = Bundle()
        var search_selectedComapnytoFinancialStatement = Bundle()


        //for_financial
        var financialcurrentFragID  : Int = R.id.searchFrag
        var financialcompanydetailbundle = Bundle()
        var Financial_financialstatementbundle = Bundle()
        var FinancialcompanyNameBundle = "0"
        var financialcompanynewsBundle = Bundle()
        var countrylist_to_notification = false

        var financialtoNotification_to_news_Bundle  = Bundle()
        var financialtoNotificationCurrentFragID =  R.id.notificationFrag
        var financialtoNotification_to_Financailstatement_Bundle  = Bundle()
        var financial_to_profile = false
        var financialcompanyrecyclervisibleitem = 0

        var financial_settingCurrentID = R.id.settingFrag
        var financial_selectedCompanytoComapnyDetail = Bundle()
        var financial_selectedComapnytoFinancialStatement = Bundle()

        //for Forum
        var forumCurrentID : Int = R.id.forumFrag
        var forumCommentBundle = Bundle()
        var forumreplyCommentBundle = ArrayList<Bundle>()
        var forumtonewsDetailBundle = Bundle()

        var fourm_to_notification = false
        var forumtoNotification_to_news_Bundle  = Bundle()
        var forumtoNotification_to_Financailstatement_Bundle  = Bundle()
        var forumtoNotificationCurrentFragID =  R.id.notificationFrag

        var forum_to_profile = false

        var forum_settingcurrentID = R.id.settingFrag
        var forum_selectedCompanytoComapnyDetail = Bundle()
        var forum_selectedComapnytoFinancialStatement = Bundle()



        //for news
        var newsFragCurrentID : Int = R.id.newsFrag
        var newDetailBundle = Bundle()

        var news_to_notification = false
        var newstoNotification_to_news_Bundle  = Bundle()
        var newstoNotification_to_Financailstatement_Bundle  = Bundle()
        var newstoNotificationCurrentFragID =  R.id.notificationFrag

        var news_to_profile = false

        var news_settingcurrentID = R.id.settingFrag
        var news_selectedCompanytoComapnyDetail = Bundle()
        var news_selectedComapnytoFinancialStatement = Bundle()



        //For notification to financial statement
        var not_mHomePeriodselection = 0
        var not_mHomePeriodselectionval = "Annual"
        var not_mHomeSelectedYear = ""

        var not_mNewsPeriodselection = 0
        var not_mNewsPeriodselectionval = "Annual"
        var not_mNewsSelectedYear = ""


        var not_mFinancialPeriodselection = 0
        var not_mFinancialPeriodselectionval = "Annual"
        var not_mFinancialSelectedYear = ""

        var not_mForumPeriodselection = 0
        var not_mForumPeriodselectionval = "Annual"
        var not_mForumSelectedYear = ""

        var not_mSearchPeriodselection = 0
        var not_mSearchPeriodselectionval = "Annual"
        var not_mSearchSelectedYear = ""

    }
}