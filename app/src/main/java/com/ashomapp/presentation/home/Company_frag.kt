package com.ashomapp.presentation.home

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentCompanyFragBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.ashomapp.presentation.home.country.CompanyPagingAdapter
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter
import androidx.recyclerview.widget.SimpleItemAnimator

import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import com.ashomapp.MainActivity
import com.ashomapp.database.AshomDBHelper
import jp.wasabeef.recyclerview.animators.*
import java.lang.Exception
import android.os.Parcelable
import com.ashomapp.presentation.search.SearchFrag
import com.ashomapp.presentation.search.SearchViewModel
import com.ashomapp.utils.*


class Company_frag : Fragment(), onCountryClick, onCompanyClick {

    private lateinit var mBinding: FragmentCompanyFragBinding
    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    private val adapter = CompanyPagingAdapter(this)
    private lateinit var countryAdapter: CountryAdapter
    private lateinit var companyAdapter: CompanyAdapter
    private var job: Job? = null
    private val mSearchViewModel by activityViewModels<SearchViewModel>()
    private var bottomtotopbool = false
    var search_itemclick = false
    val db = AshomDBHelper(AshomAppApplication.instance.applicationContext, null)

    private fun loadNews(country_name: String, search: String) {
        job?.cancel()
        job = lifecycleScope.launch {
            /* mHomeViewModel.getCompanyDataqItem(country_name, search = search).collectLatest {
                 adapter.submitData(it)
             }*/

            val list = db.getCompanyDataFromCOuntry(country_name)

            Log.d("Companydbitemsdd", "${list.toString()}")
            companyAdapter = CompanyAdapter(list, this@Company_frag)
            Handler().postDelayed({
                mBinding.recylcerCompany.adapter = companyAdapter
                val layoutManager2 =
                    GridLayoutManager(AshomAppApplication.instance.applicationContext, 2)

                mBinding.recylcerCompany.layoutManager = layoutManager2
                bottomtotopbool = false
                mBinding.bottomtotopiv.rotation = 0f
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    mBinding.recylcerCompany.layoutManager?.scrollToPosition(HomeFlow.homecompanyrecyclervisibleitem + 1)
                    Log.d("stateuser", "home ${HomeFlow.homecompanyrecyclervisibleitem}")
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    mBinding.recylcerCompany.layoutManager?.scrollToPosition(HomeFlow.financialcompanyrecyclervisibleitem + 1)
                    Log.d("stateuser", "financial")
                }
                Log.d(
                    "companyfragss",
                    list.size.toString() + "," + companyAdapter.itemCount.toString()
                )
            }, 300)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentCompanyFragBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    companion object {
        val rv_subcategory_position = MutableLiveData<String>()
        var selectedItem = 0
        var country_name: String = "0"

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        ApplyGTMEvent("company_list_view","company_list_view_count","company_list_view")

        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            HomeFlow.currentFragID = R.id.company_frag
        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
            HomeFlow.financialcurrentFragID = R.id.company_frag
        }

        notificationcounter.observe(this.viewLifecycleOwner) {
            if (it != 0) {
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            } else {
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }
        mHomeViewModel.getCountry()
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    findNavController().navigate(R.id.action_company_frag_to_countryList)
                    HomeFlow.countryAnimation = true
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    findNavController().navigate(R.id.action_company_frag_to_countryList)
                    HomeFlow.countryAnimation = true
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
                        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                            findNavController().navigate(R.id.action_company_frag_to_countryList)
                            HomeFlow.countryAnimation = true

                        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                            findNavController().navigate(R.id.action_company_frag_to_countryList)
                            HomeFlow.countryAnimation = true
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
            if (HomeFlow.sectionBottomID == R.id.countryList && HomeFlow.financialcurrentFragID != R.id.countryList) {
                (requireActivity() as MainActivity).clicktoCountryList()
            } else {
                (requireActivity() as MainActivity).gotoHome()
            }
        }
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            findNavController().navigate(R.id.action_company_frag_to_settingFrag)
        }
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

        }
        try {

        } catch (e: Exception) {
            e.printStackTrace()
        }
        val args = requireArguments().getString("country_name").toString()
        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            Log.d("homesection", "Home")
            HomeFlow.HomecompanyNameBundle = "${args}"
        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
            Log.d("homesection", "Countrylist")
            HomeFlow.FinancialcompanyNameBundle = "${args}"
        }
        if (!args.isNullOrEmpty()) {
            countryAdapter = CountryAdapter(companyCountrylist, this)
            mBinding.countryRv.adapter = countryAdapter
            if (args.isNullOrEmpty()) {
                //  mHomeViewModel.getCompany("")
                selectedItem = 0
            } else {

                if (args.equals("KSA")) {
                    mHomeViewModel.recordEvent("view_ksa_company")
                } else if (args.equals("Kuwait")) {
                    mHomeViewModel.recordEvent("view_kuwait_company")
                } else if (args.equals("Qatar")) {
                    mHomeViewModel.recordEvent("view_qatar_company")
                } else if (args.equals("Oman")) {
                    mHomeViewModel.recordEvent("view_oman_company")
                } else if (args.equals("UAE")) {
                    mHomeViewModel.recordEvent("view_uae_company")
                } else if (args.equals("Bahrain")) {
                    mHomeViewModel.recordEvent("view_bahrain_company")
                }

                if (country_name.equals("0")) {

                    Handler()
                        .postDelayed({
                            loadNews(args, "0")
                        }, 100)
                    country_name = args
                    companyCountrylist.forEachIndexed { index, countriesDTO ->
                        if (countriesDTO.country.equals(country_name)) {
                            selectedItem = index
                            mBinding.countryRv.scrollToPosition(index)
                        }
                    }
                    if (country_name.equals("UAE") || country_name.equals("ADX") || country_name.equals(
                            "DFM"
                        )
                    ) {
                        mBinding.extraFilter.visibility = View.VISIBLE

                        if (args.equals("ADX")) {
                            selectedItem = -1
                            adapter.notifyDataSetChanged()
                            mBinding.itemCountryUaeAdx.getBackground().setColorFilter(
                                Color.parseColor("#72c4f1"),
                                PorterDuff.Mode.SRC_ATOP
                            );
                            mBinding.itemUaeDfm.getBackground().setColorFilter(null);
                        } else if (args.equals("DFM")) {
                            selectedItem = -1
                            adapter.notifyDataSetChanged()
                            mBinding.itemUaeDfm.getBackground().setColorFilter(
                                Color.parseColor("#72c4f1"),
                                PorterDuff.Mode.SRC_ATOP
                            );
                            mBinding.itemCountryUaeAdx.getBackground().setColorFilter(null);
                        }
                        mBinding.countryRv.scrollToPosition(4)
                    } else {
                        mBinding.extraFilter.visibility = View.GONE
                    }
                } else {
                    Handler()
                        .postDelayed({
                            loadNews(args, "0")
                        }, 100)
                    companyCountrylist.forEachIndexed { index, countriesDTO ->
                        if (countriesDTO.country.equals(args)) {

                            selectedItem = index
                            mBinding.countryRv.scrollToPosition(index)
                        }
                    }
                    if (args.equals("UAE") || args.equals("ADX") || args.equals("DFM")) {
                        mBinding.extraFilter.visibility = View.VISIBLE
                        // selectedItem = 4
                        if (args.equals("ADX")) {
                            selectedItem = -1
                            adapter.notifyDataSetChanged()
                            mBinding.itemCountryUaeAdx.getBackground().setColorFilter(
                                Color.parseColor("#72c4f1"),
                                PorterDuff.Mode.SRC_ATOP
                            );
                            mBinding.itemUaeDfm.getBackground().setColorFilter(null);
                        } else if (args.equals("DFM")) {
                            selectedItem = -1
                            adapter.notifyDataSetChanged()
                            mBinding.itemUaeDfm.getBackground().setColorFilter(
                                Color.parseColor("#72c4f1"),
                                PorterDuff.Mode.SRC_ATOP
                            );
                            mBinding.itemCountryUaeAdx.getBackground().setColorFilter(null);
                        }
                        mBinding.countryRv.scrollToPosition(4)

                    } else {
                        mBinding.extraFilter.visibility = View.GONE
                    }


                }
            }
        }

        /*  mHomeViewModel.companieslist.observe(viewLifecycleOwner) {
              adapter = CompanyAdapter(it, this)

              adapter.notifyDataSetChanged()
          }*/

        mBinding.itemCountryUaeAdx.setOnClickListener {
            mBinding.itemCountryUaeAdx.getBackground()
                .setColorFilter(Color.parseColor("#72c4f1"), PorterDuff.Mode.SRC_ATOP);
            selectedItem = -1
            countryAdapter.notifyDataSetChanged()
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.homecompanyrecyclervisibleitem = 0
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.financialcompanyrecyclervisibleitem = 0
            }
            mBinding.itemUaeDfm.getBackground().setColorFilter(null);
            country_name = "ADX"
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.HomecompanyNameBundle = "ADX"
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.FinancialcompanyNameBundle = "ADX"
            }
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            mBinding.etCompanySearch.clearFocus()
            mBinding.etCompanySearch.setText("")

            loadNews(country_name, "0")

        }
        mBinding.itemUaeDfm.setOnClickListener {
            mBinding.itemUaeDfm.getBackground()
                .setColorFilter(Color.parseColor("#72c4f1"), PorterDuff.Mode.SRC_ATOP);
            mBinding.itemCountryUaeAdx.getBackground().setColorFilter(null);
            selectedItem = -1
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.homecompanyrecyclervisibleitem = 0
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.financialcompanyrecyclervisibleitem = 0
            }
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            mBinding.etCompanySearch.clearFocus()
            mBinding.etCompanySearch.setText("")
            countryAdapter.notifyDataSetChanged()
            country_name = "DFM"
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.HomecompanyNameBundle = "DFM"
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.FinancialcompanyNameBundle = "DFM"
            }
            loadNews(country_name, "0")

        }
        mBinding.root.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            mBinding.etCompanySearch.clearFocus()

        }
        mBinding.etCompanySearch.doOnTextChanged { text, start, before, count ->
            Log.d("searchet", text?.length.toString())
            if (text?.length != 0) {
                //  loadNews(country_name, text.toString())
                companyAdapter.filter.filter(text.toString())
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.homecompanyrecyclervisibleitem = 0
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.financialcompanyrecyclervisibleitem = 0
                }


                mBinding.homeSearchIcon.setImageResource(R.drawable.ic_close_search)
            } else {
                // loadNews(country_name, "0")

                companyAdapter.filter.filter(text.toString())
                /* if (HomeFlow.sectionBottomID == R.id.homeFrag){
                     HomeFlow.homecompanyrecyclervisibleitem = 0
                 }else if (HomeFlow.sectionBottomID ==R.id.countryList){
                     HomeFlow.financialcompanyrecyclervisibleitem = 0
                 }*/
                mBinding.homeSearchIcon.setImageResource(R.drawable.search_icon)
            }
        }
        mBinding.homeSearchIcon.setOnClickListener {
            if (mBinding.etCompanySearch.text.length > 0) {
                mBinding.etCompanySearch.setText("")
                mBinding.etCompanySearch.clearFocus()
                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            }
        }

        mBinding.recylcerCompany.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                var visibleChild: View = recyclerView.getChildAt(0)
                val firstChild: Int = recyclerView.getChildAdapterPosition(visibleChild)

                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.homecompanyrecyclervisibleitem = firstChild
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.financialcompanyrecyclervisibleitem = firstChild
                }
                if (firstChild <= 0) {
                    bottomtotopbool = false
                    mBinding.bottomtotopiv.rotation = 0f
                } else {
                    bottomtotopbool = true
                    mBinding.bottomtotopiv.rotation = 180f
                }
            }
        })


        mBinding.bottomToTop.setOnClickListener {
            if (companyAdapter.itemCount > 0) {
                if (bottomtotopbool) {
                    mBinding.recylcerCompany.scrollToPosition(0)
                    bottomtotopbool = false
                    mBinding.bottomtotopiv.rotation = 0f
                } else {
                    mBinding.recylcerCompany.scrollToPosition(companyAdapter.itemCount - 1)
                    bottomtotopbool = true
                    mBinding.bottomtotopiv.rotation = 180f
                }
            }
        }


        /*Handler().postDelayed({
            mBinding.recylcerCompany.adapter = adapter
            val layoutManager2 =
                GridLayoutManager(AshomAppApplication.instance.applicationContext, 2)

            mBinding.recylcerCompany.layoutManager = layoutManager2

           mBinding.recylcerCompany.itemAnimator = ScaleInBottomAnimator()
            mBinding.recylcerCompany.itemAnimator?.apply {
                addDuration = 100
                removeDuration = 0
                moveDuration = 100
                changeDuration = 0
            }
            mBinding.recylcerCompany.startLayoutAnimation()


        }, 800)*/

        //  OverScrollDecoratorHelper.setUpOverScroll(mBinding.recylcerCompany, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        VerticalOverScrollBounceEffectDecorator(RecyclerViewOverScrollDecorAdapter(mBinding.recylcerCompany))

        Handler().postDelayed({
            mBinding.recylcerCompany.clearAnimation()

        }, 850)

    }

    override fun onCountryClick(countriesDTO: CountriesDTO) {
        if (mBinding.etCompanySearch.isFocused && mBinding.etCompanySearch.text.isNullOrEmpty()) {
            mBinding.etCompanySearch.setText("")
            mBinding.etCompanySearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)

        } else {

            mBinding.etCompanySearch.setText("")
            mBinding.etCompanySearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
            onCOuntryRecord(countriesDTO.country)
            mBinding.itemUaeDfm.getBackground().setColorFilter(null);
            mBinding.itemCountryUaeAdx.getBackground().setColorFilter(null);
            if (countriesDTO.country.equals("All Countries")) {
                //   mHomeViewModel.getCompany("")
                country_name = "0"
                loadNews(country_name, "0")
            } else {
                //mHomeViewModel.getCompany(countriesDTO.country)
                country_name = "${countriesDTO.country}"

                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    Log.d("homesection", "Home")
                    HomeFlow.HomecompanyNameBundle = "${countriesDTO.country}"
                    HomeFlow.homecompanyrecyclervisibleitem = 0
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    Log.d("homesection", "Countrylist")
                    HomeFlow.financialcompanyrecyclervisibleitem = 0
                    HomeFlow.FinancialcompanyNameBundle = "${countriesDTO.country}"
                }
                loadNews(country_name, "0")
            }
            if (countriesDTO.country.equals("UAE")) {
                mBinding.extraFilter.visibility = View.VISIBLE
            } else {
                mBinding.extraFilter.visibility = View.GONE
            }
            val layoutManager2 =
                GridLayoutManager(AshomAppApplication.instance.applicationContext, 2)
            mBinding.recylcerCompany.layoutManager = layoutManager2
        }
    }

    private fun onCOuntryRecord(country: String) {
        if (country.equals("KSA")) {
            mHomeViewModel.recordEvent("view_ksa_company")
        } else if (country.equals("Kuwait")) {
            mHomeViewModel.recordEvent("view_kuwait_company")
        } else if (country.equals("Qatar")) {
            mHomeViewModel.recordEvent("view_qatar_company")
        } else if (country.equals("Oman")) {
            mHomeViewModel.recordEvent("view_oman_company")
        } else if (country.equals("UAE")) {
            mHomeViewModel.recordEvent("view_uae_company")
        } else if (country.equals("Bahrain")) {
            mHomeViewModel.recordEvent("view_bahrain_company")
        }

    }

    override fun onCompanyItemClick(companyDTO: CompanyDTO) {
        if (mBinding.etCompanySearch.isFocused && mBinding.etCompanySearch.text.isNullOrEmpty()) {
            mBinding.etCompanySearch.setText("")
            mBinding.etCompanySearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)

        } else {

            recordCompany(companyDTO.Country, companyDTO.SymbolTicker)
            mSearchViewModel.getSearchStr("CompanyName✂${companyDTO.Country}✂${companyDTO.SymbolTicker}✂${companyDTO.image}✂${companyDTO.Company_Name}✂${companyDTO.id}")
            mHomeViewModel.recordEvent("company_page_view")
            SearchFrag.reloadlist = true
            HomeFlow.companydetailanimation = true
            mBinding.etCompanySearch.setText("")
            mBinding.etCompanySearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
            val companydetail = Gson().toJson(companyDTO)
            val args = bundleOf("company_detail" to companydetail)
            if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                HomeFlow.currentFragID = R.id.companyDetail
                HomeFlow.companydetailbundle = args
            } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                HomeFlow.financialcurrentFragID = R.id.companyDetail
                HomeFlow.financialcompanydetailbundle = args
            }
            findNavController().navigate(R.id.action_company_frag_to_companyDetail, args)
        }
    }

    private fun recordCompany(country: String, symbolTicker: String) {
        if (country.equals("KSA")) {
            mHomeViewModel.recordEvent("view_ksa_$symbolTicker")
        } else if (country.equals("Kuwait")) {
            mHomeViewModel.recordEvent("view_kuwait_$symbolTicker")
        } else if (country.equals("Qatar")) {
            mHomeViewModel.recordEvent("view_qatar_$symbolTicker")
        } else if (country.equals("Oman")) {
            mHomeViewModel.recordEvent("view_oman_$symbolTicker")
        } else if (country.equals("UAE")) {
            mHomeViewModel.recordEvent("view_uae_$symbolTicker")
        } else if (country.equals("Bahrain")) {
            mHomeViewModel.recordEvent("view_bahrain_$symbolTicker")
        }
    }

    fun RecyclerView.disableItemAnimator() {
        (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = true

    }


}