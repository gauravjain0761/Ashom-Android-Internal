package com.ashomapp.presentation.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.AshomDBHelper
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentHomeBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.home.country.CountryList
import com.ashomapp.presentation.search.SearchViewModel
import com.ashomapp.presentation.search.SearchlistItemAdapter
import com.ashomapp.utils.*
import com.bumptech.glide.Glide
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import java.lang.Exception
import android.R.string.no
import com.android.billingclient.api.*
import com.ashomapp.presentation.notification.NotificationServices
import com.ashomapp.presentation.search.SearchFrag


class HomeFrag : Fragment(), onCompanyClick {
         private lateinit var mBinding : FragmentHomeBinding
    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    private val mSearchViewModel by activityViewModels<SearchViewModel>()
    var search_itemclick = false

    val db = AshomDBHelper(AshomAppApplication.instance.applicationContext, null)
    companion object{
        val  userToken =MutableLiveData<String>("")
    }
    private lateinit var mAdapter : SearchlistItemAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
       // loginDialog()
        notificationcounter.observe(this.viewLifecycleOwner){
            if (it != 0){
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            }else{
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }
        mHomeViewModel.getForum()


        HomeFlow.currentFragID = R.id.homeFrag

        mBinding.root.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            mBinding.homeSearch.setText("")
            mBinding.homeSearch.clearFocus()
            MainActivity.bottomhideunhide.value = false


        }
          hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this.viewLifecycleOwner, onBackPressedCallback)
        try {
           val argument = arguments?.getString("logincome")
           if (!argument.isNullOrEmpty()){
               loginDialog()

               this.arguments?.clear()
           }
       }catch (e :Exception){
           Log.d("Error", e.localizedMessage)
       }
        userToken.observe(viewLifecycleOwner){
            if (!it.isNullOrEmpty()){
                SharedPrefrenceHelper.token = it
                getUserData()
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val currentToken = it.result
                currentToken?.let { token ->
                    val myToken = SharedPrefrenceHelper.devicetoken
                    if (!token.equals(myToken)){
                         Updatetoken(token)
                    }
                    Log.d("token", token)
                }
            }
        }
         mBinding.mtoolbar.mainBack.visibility = View.GONE
        mBinding.cvFinancialReport.setOnClickListener {
            if (mBinding.homeSearch.isFocused && mBinding.homeSearch.text.isNullOrEmpty()){
                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                mBinding.homeSearch.clearFocus()
            }else{
                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                MainActivity.financialtab = true
                mBinding.homeSearch.clearFocus()
                MainActivity.financiallistanimation = MainActivity.financiallistanimation+1
                Handler().postDelayed({
                   findNavController().navigate(R.id.action_homeFrag_to_countryList)
                },100)
            }
        }

        mBinding.cvStockPrices.setOnClickListener {
            if (mBinding.homeSearch.isFocused && mBinding.homeSearch.text.isNullOrEmpty()){
                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                mBinding.homeSearch.clearFocus()
            }else{
                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                MainActivity.financialtab = true
                mBinding.homeSearch.clearFocus()
                MainActivity.financiallistanimation = MainActivity.financiallistanimation+1
                findNavController().navigate(R.id.action_homeFrag_to_stockprice)
            }
        }


        mBinding.mtoolbar.toolProfile.setOnClickListener {

            hideKeyboard(AshomAppApplication.instance.applicationContext,it)
            mBinding.homeSearch.clearFocus()
            Handler().postDelayed({

             findNavController().navigate(R.id.action_homeFrag_to_settingFrag)

         },100)
        }

        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {

            hideKeyboard(AshomAppApplication.instance.applicationContext,it)
            mBinding.homeSearch.clearFocus()
            Handler().postDelayed({
                findNavController().navigate(R.id.notificationFrag)
            },100)
        }

        mBinding.cvNewClick.setOnClickListener {
           if (mBinding.homeSearch.isFocused && mBinding.homeSearch.text.isNullOrEmpty()){
               hideKeyboard(AshomAppApplication.instance.applicationContext,it)
               mBinding.homeSearch.clearFocus()
           }else{
               HomeFlow.home_to_news = true
               hideKeyboard(AshomAppApplication.instance.applicationContext,it)
               mBinding.homeSearch.clearFocus()
               mHomeViewModel.recordEvent("view_countries_news")
               Handler().postDelayed({

                   findNavController().navigate(R.id.action_homeFrag_to_newsFrag)

               },100)
           }
        }
        mBinding.searchRv.visibility = View.GONE

       mHomeViewModel.Searchcompanieslist.observe(this.viewLifecycleOwner){
           mAdapter = SearchlistItemAdapter(it, this)
           mBinding.searchRv.adapter =mAdapter
           mAdapter.notifyDataSetChanged()
       }

        mBinding.homeSearch.doOnTextChanged { text, start, before, count ->
            if (text?.length != 0 ){
                // mSearchViewModel.getSearchStr(text.toString())
                search_itemclick = true
                 MainActivity.bottomhideunhide.value = true
                mBinding.searchRv.visibility = View.VISIBLE
                mHomeViewModel.getSearchCompany("0", text.toString())
                mBinding.homeSearchIcon.setImageResource(R.drawable.ic_close_search)


            }else{
                MainActivity.bottomhideunhide.value = false

                search_itemclick = false
                mBinding.searchRv.visibility = View.GONE
                mBinding.homeSearchIcon.setImageResource(R.drawable.search_icon)


            }
        }
        mBinding.homeSearchIcon.setOnClickListener {
            if (mBinding.homeSearch.text.length > 0){
                mBinding.homeSearch.setText("")
                mBinding.homeSearch.clearFocus()
                MainActivity.bottomhideunhide.value = false

                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            }
        }

    }



    private fun Updatetoken(token: String) {
          lifecycleScope.launch {
              val result = HomeRepository.updateToken(token)
              when(result){
                  is ResultWrapper.Success ->{
                      SharedPrefrenceHelper.devicetoken = token
                      try {
                          val user = SharedPrefrenceHelper.user
                          if (!user.subscription_type.equals("Yearly")){
                              val args = bundleOf("where" to "home")
                              findNavController().navigate(R.id.subscriptionDialog, args)
                          }
                      }catch (e :Exception){
                          e.printStackTrace()
                      }
                  }
                  is ResultWrapper.Failure ->{
                      Log.d("errorupdatingtoken", result.errorMessage)
                  }
              }
          }
    }

    private fun getUserData(){
        lifecycleScope.launch {
            val result = HomeRepository.getUserData(SharedPrefrenceHelper.token.toString())
            when(result){
                is ResultWrapper.Success ->{
                    //username.value = "${result.response.userdata.first_name} ${result.response.userdata.last_name}"
                  //  profileImage.value = "${result.response.userdata.profile_pic}"
                    Glide.with(AshomAppApplication.instance.applicationContext).load(result.response.userdata.profile_pic).into(mBinding.mtoolbar.mainProfilePic)
                    SharedPrefrenceHelper.user = result.response.userdata
                }
                is ResultWrapper.Failure ->{
                    Log.d("usererrordd", "${result.errorMessage}")
                }
            }
        }
    }

    private fun loginDialog(){
        successDialog(requireActivity(), "Success","User Login Successfully.")
    }

    override fun onCompanyItemClick(companyDTO: CompanyDTO) {
        if (search_itemclick){
            mSearchViewModel.getSearchStr("CompanyName✂${companyDTO.Country}✂${companyDTO.SymbolTicker}✂${companyDTO.image}✂${companyDTO.Company_Name}✂${companyDTO.id}")
            SearchFrag.reloadlist = true
        }

        mHomeViewModel.recordEvent("search_company")
        mBinding.homeSearch.setText("")
        mBinding.homeSearch.clearFocus()
        hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)

        val dbcompanyDTO  = db.getSingleCompany(companyDTO.id)
        val companydetail = Gson().toJson(dbcompanyDTO)
        val args = bundleOf("company_detail" to companydetail)
        HomeFlow.companydetailfromsearch = true
        HomeFlow.companydetailanimation = true

       Handler().postDelayed({
              /* val fragment = CompanyDetail()
           fragment.arguments = args
           val activity = requireActivity() as MainActivity
           val fragManager = activity.supportFragmentManager
           fragManager.beginTransaction()
               .add(R.id.home_frameLayout,fragment)
               .commit()*/

           HomeFlow.companydetailbundle = args
      findNavController().navigate(R.id.action_homeFrag_to_companyDetail, args)

       },100)
    }

    override fun onPause() {
        super.onPause()

        Log.d("databaseho", "Database Closed")
    }

    override fun onResume() {
        super.onResume()
      try {
          if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()){
              Glide.with(AshomAppApplication.instance.applicationContext).load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

          }
      }catch (e :Exception){
          Log.d("data", "df")
      }
    }





}


