package com.ashomapp.presentation.search

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.AshomDBHelper
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentSearchBinding
import com.ashomapp.network.response.SearachDTO
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.home.CompanyAdapter
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.presentation.home.HomeViewModel
import com.ashomapp.presentation.home.onCompanyClick
import com.ashomapp.utils.ApplyGTMEvent
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.notificationcounter
import com.ashomapp.utils.setanimation
import com.bumptech.glide.Glide
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class SearchFrag : Fragment(), onCompanyClick {

    private lateinit var mBinding: FragmentSearchBinding
    private val mSearchViewModel by activityViewModels<SearchViewModel>()
    private lateinit var mAdapter: SearchlistItemAdapter
    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    var search_itemclick = false
    val db = AshomDBHelper(AshomAppApplication.instance.applicationContext, null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("searchtab", "onCreate")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSearchBinding.inflate(layoutInflater, container, false).apply {
            lifecycleOwner = this@SearchFrag
        }
        return mBinding.root
    }

     companion object {
         val searchMostViewResult = MutableLiveData<List<CompanyDTO>>()
         val searchRecentlyViewResult = MutableLiveData<List<CompanyDTO>>()
        var  reloadlist = true
         var scrollchange = 0
         var scrolly = 0
     }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ApplyGTMEvent("search_click","search_click_count","search_click")

        HomeFlow.searchcurrentFragID = R.id.searchFrag
        mBinding.mtoolbar.mainBack.visibility = View.GONE
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            findNavController().navigate(R.id.action_searchFrag_to_settingFrag)
        }
        mBinding.mtoolbar.icon.setOnClickListener {
            setanimation(it)
            (requireActivity() as MainActivity).gotoHome()
        }
        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)

            findNavController().navigate(R.id.notificationFrag)
        }

        Handler().postDelayed({
            mBinding.newstedScrollSearch.scrollY = scrolly
            mBinding.mostviewRv.visibility = View.VISIBLE
            mBinding.viewagainRv.visibility = View.VISIBLE
            mBinding.viewagainTv.visibility = View.VISIBLE
            mBinding.mostviewTv.visibility = View.VISIBLE
        },-30)
        notificationcounter.observe(this.viewLifecycleOwner) {
            if (it != 0) {
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            } else {
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                requireActivity().finish()
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
        mBinding.searchRv.visibility = View.GONE
        mSearchViewModel.Searchcompanieslist.observe(viewLifecycleOwner) {
            mAdapter = SearchlistItemAdapter(it, this@SearchFrag)
            mBinding.searchRv.adapter = mAdapter
            mAdapter.notifyDataSetChanged()

        }
        mBinding.newstedScrollSearch.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener{
            override fun onScrollChange(
                v: NestedScrollView?,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
               scrollchange = scrollX
                scrolly = scrollY

                Log.d("scrollviwesearch", "x : $scrollchange, y : $scrolly")
            }

        })

        if (reloadlist){
            Handler().postDelayed({
                GlobalScope.launch(Dispatchers.IO) {
                    async {
                        getRecentView()
                    }.await()
                    async {
                        getMostview()
                        reloadlist = false
                    }.await()

                }
            }, 550)
        }else{

        }
        searchRecentlyViewResult.observe(this.viewLifecycleOwner){
            val adpter = CompanyAdapter(it, this@SearchFrag)
            mBinding.viewagainRv.adapter = adpter
            mBinding.viewagainRv.hasFixedSize()
            val layoutManager2 =
                GridLayoutManager(AshomAppApplication.instance.applicationContext, 2)
            mBinding.viewagainRv.layoutManager = layoutManager2
            adpter.notifyDataSetChanged()


        }
        searchMostViewResult.observe(this.viewLifecycleOwner){
            val adpter = CompanyAdapter(it, this@SearchFrag)
            mBinding.mostviewRv.adapter = adpter
            mBinding.mostviewRv.hasFixedSize()
            val layoutManager2 =
                GridLayoutManager(AshomAppApplication.instance.applicationContext, 2)
            mBinding.mostviewRv.layoutManager = layoutManager2
            adpter.notifyDataSetChanged()



        }
        mBinding.etSearch.doOnTextChanged { text, start, before, count ->
            if (text?.length != 0) {
                // mSearchViewModel.getSearchStr(text.toString())
                mBinding.searchRv.visibility = View.VISIBLE
                search_itemclick = true
                MainActivity.bottomhideunhide.value = true

                mSearchViewModel.getSearchCompany("0", text.toString())
                mBinding.homeSearchIcon.setImageResource(R.drawable.ic_close_search)
            } else {
                mBinding.homeSearchIcon.setImageResource(R.drawable.search_icon)
                mBinding.searchRv.visibility = View.GONE
                MainActivity.bottomhideunhide.value = false

                search_itemclick = false
            }
        }
        mBinding.root.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            mBinding.etSearch.clearFocus()
            MainActivity.bottomhideunhide.value = false
            mBinding.etSearch.setText("")


        }
        mBinding.homeSearchIcon.setOnClickListener {
            if (mBinding.etSearch.text.length > 0) {
                mBinding.etSearch.setText("")
                mBinding.etSearch.clearFocus()
                MainActivity.bottomhideunhide.value = false

                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            }
        }
        /*  Handler().postDelayed({
         mSearchViewModel.getSearchRecentlyView()
            mSearchViewModel.getSearchMostview()

              mSearchViewModel.searchRecentlyViewResult.observe(requireParentFragment().viewLifecycleOwner){
                  if (!it.isNullOrEmpty()){
                      val list = mutableListOf<CompanyDTO>()
                      it.forEach { item ->
                          val adas = item.search.split("✂").toTypedArray()
                          if (adas[0].equals("CompanyName")){
                              list.add(
                                  CompanyDTO(
                                      "${adas[5]}",
                                      "${adas[4]}",
                                      "${adas[2]}",
                                      "${adas[1]}",
                                      "${adas[3]}"
                                  ))
                          }
                      }
                      val adpter = CompanyAdapter(list, this)
                      mBinding.viewagainRv.adapter = adpter
                      mBinding.viewagainRv.hasFixedSize()
                      val layoutManager2 =
                          GridLayoutManager(AshomAppApplication.instance.applicationContext, 2)
                      mBinding.viewagainRv.layoutManager = layoutManager2
                      adpter.notifyDataSetChanged()
                      mBinding.viewagainRv.startLayoutAnimation()
                      Handler().postDelayed({
                          mBinding.viewagainRv.clearAnimation()
                      }, 1000)

                  }
              }

              mSearchViewModel.searchMostViewResult.observe(requireParentFragment().viewLifecycleOwner){
                  if (!it.isNullOrEmpty()){
                      val list = mutableListOf<CompanyDTO>()
                      it.forEach { item ->
                          val adas = item.search.split("✂").toTypedArray()
                          if (adas[0].equals("CompanyName")){
                              list.add(
                                  CompanyDTO(
                                      "${adas[5]}",
                                      "${adas[4]}",
                                      "${adas[2]}",
                                      "${adas[1]}",
                                      "${adas[3]}"
                                  ))
                          }
                      }

                      val adpter =  CompanyAdapter(list, this)
                      mBinding.mostviewRv.adapter =adpter
                      mBinding.mostviewRv.hasFixedSize()
                      val layoutManager2 =
                          GridLayoutManager(AshomAppApplication.instance.applicationContext, 2)
                      mBinding.mostviewRv.layoutManager = layoutManager2
                      adpter.notifyDataSetChanged()
                      mBinding.mostviewRv.startLayoutAnimation()
                      Handler().postDelayed({
                          mBinding.mostviewRv.clearAnimation()
                      }, 500)

                  }
              }
          },550)
    */


    }

    override fun onCompanyItemClick(companyDTO: CompanyDTO) {
        if (mBinding.etSearch.isFocused && mBinding.etSearch.text.isNullOrEmpty()) {
            mBinding.etSearch.setText("")
            mBinding.etSearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
        } else {

            mHomeViewModel.recordEvent("search_company")
            if (search_itemclick) {
                mSearchViewModel.getSearchStr("CompanyName✂${companyDTO.Country}✂${companyDTO.SymbolTicker}✂${companyDTO.image}✂${companyDTO.Company_Name}✂${companyDTO.id}")
                SearchFrag.reloadlist = true
            }

            HomeFlow.companydetailanimation = true
            mBinding.etSearch.setText("")
            mBinding.etSearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)

            val dbcompanyDTO  = db.getSingleCompany(companyDTO.id)
            val companydetail = Gson().toJson(dbcompanyDTO)
            Log.d("dbvcompany", dbcompanyDTO.toString())

            val args = bundleOf("company_detail" to companydetail)
            HomeFlow.searchCountrydetailbundle = args
            findNavController().navigate(R.id.action_searchFrag_to_companyDetail, args)
        }
    }

    private fun getRecentView() {

        lifecycleScope.launch {
            val result = HomeRepository.getrecentlyView()
            when (result) {
                is ResultWrapper.Success -> {
                    val mlist = mutableListOf<SearachDTO>()
                    mlist.addAll(result.response.map { it })
                    if (!mlist.isNullOrEmpty()) {
                        val list = mutableListOf<CompanyDTO>()
                        db.getSearchCompanyList(mlist.map { it.search }).forEach {
                            list.add(it)
                        }

                       /* mlist.forEach { item ->
                            val adas = item.search.split("✂").toTypedArray()
                            if (adas[0].equals("CompanyName")) {
                                list.add(
                                    CompanyDTO(
                                        "${adas[5]}",
                                        "${adas[4]}",
                                        "${adas[2]}",
                                        "${adas[1]}",
                                        "${adas[3]}"
                                    )
                                )
                            }
                        }*/
                       searchRecentlyViewResult.value = list

                    }
                }
                is ResultWrapper.Failure -> {

                }
            }
        }
    }

    private fun getMostview() {
        lifecycleScope.launch {
            val result = HomeRepository.getSearchMostView()
            when (result) {
                is ResultWrapper.Success -> {
                    val mlist = mutableListOf<SearachDTO>()
                    mlist.addAll(result.response.map { it })
                    Log.d("mostviewed", mlist.toString())
                    if (!mlist.isNullOrEmpty()) {
                        val list = mutableListOf<CompanyDTO>()
                        db.getSearchCompanyList(mlist.map { it.search }).forEach {
                            list.add(it)
                        }
                       /* mlist.forEach { item ->
                            val adas = item.search.split("✂").toTypedArray()
                            if (adas[0].equals("CompanyName")) {
                                list.add(
                                    CompanyDTO(
                                        "${adas[5]}",
                                        "${adas[4]}",
                                        "${adas[2]}",
                                        "${adas[1]}",
                                        "${adas[3]}"
                                    )
                                )
                            }
                        }*/

                      searchMostViewResult.value = list

                    }

                }
                is ResultWrapper.Failure -> {

                }
            }
        }
    }





}
