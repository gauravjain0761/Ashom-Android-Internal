package com.ashomapp.presentation.home.country

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
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentCountryListBinding
import com.ashomapp.network.response.dashboard.CountriesDTO
import com.ashomapp.presentation.home.*
import com.ashomapp.presentation.home.HomeMainContainer.Companion.hometoCompanyBundle
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.notificationcounter
import com.ashomapp.utils.setanimation
import com.bumptech.glide.Glide
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter

class CountryList : Fragment(), onCountryGridClick {
    private lateinit var mBinding: FragmentCountryListBinding
    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    private lateinit var adapter: CountryGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentCountryListBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            HomeFlow.currentFragID = R.id.countryList
        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
            HomeFlow.financialcurrentFragID = R.id.countryList
        }
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
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    findNavController().navigate(R.id.homeFrag)
                } else {
                    requireActivity().finish()
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
                            findNavController().navigate(R.id.homeFrag)
                        } else {
                            requireActivity().finish()
                        }
                    }
                }
        }
        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)

            findNavController().navigate(R.id.notificationFrag)
        }
        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            mBinding.mtoolbar.mainBack.visibility = View.VISIBLE
        } else {
            mBinding.mtoolbar.mainBack.visibility = View.GONE
        }
        mBinding.mtoolbar.icon.setOnClickListener {
            setanimation(it)
            if (HomeFlow.sectionBottomID == R.id.countryList && HomeFlow.financialcurrentFragID != R.id.countryList){
                (requireActivity() as MainActivity).clicktoCountryList()
            }else{
                (requireActivity() as MainActivity).gotoHome()
            }
        }
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            findNavController().navigate(R.id.action_countryList_to_settingFrag)
        }
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

        }

        /* mHomeViewModel.countrylist.observe(viewLifecycleOwner){
             if (!it.isNullOrEmpty()  ){

             }
         }*/
        adapter = CountryGridAdapter(MainActivity.countrylist, this)
        mBinding.countryListRv.adapter = adapter
        adapter.notifyDataSetChanged()
        mBinding.countryListRv.setHasFixedSize(true)
        Log.d("listloading", "${MainActivity.animationBoolewan.value}")

        if (HomeFlow.countryAnimation) {
            mBinding.countryListRv.clearAnimation()
            HomeFlow.countryAnimation = false
        } else if (MainActivity.financialtab == true) {
            mBinding.countryListRv.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                AshomAppApplication.instance.applicationContext,
                R.anim.recycler_layout_animation
            )
            Log.d(
                "listloading1",
                "${MainActivity.animationBoolewan.value}, ${MainActivity.financiallistanimation}"
            )

        } else if (MainActivity.animationBoolewan.value == true) {
            mBinding.countryListRv.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                AshomAppApplication.instance.applicationContext,
                R.anim.recycler_layout_animation
            )
            MainActivity.animationBoolewan.value = false
        } else {
            Log.d(
                "listloading2",
                "${MainActivity.animationBoolewan.value}, ${MainActivity.financiallistanimation}"
            )
            mBinding.countryListRv.clearAnimation()
        }

        mBinding.countryListSearch.doOnTextChanged { text, start, before, count ->
            if (text?.length != 0) {
                adapter.filter.filter(text)
                adapter.notifyDataSetChanged()
                mBinding.homeSearchIcon.setImageResource(R.drawable.ic_close_search)
            } else {
                adapter.filter.filter(text)
                adapter.notifyDataSetChanged()
                mBinding.homeSearchIcon.setImageResource(R.drawable.search_icon)
            }
        }
        mBinding.root.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            mBinding.countryListSearch.clearFocus()

        }
        mBinding.homeSearchIcon.setOnClickListener {
            if (mBinding.countryListSearch.text.length > 0) {
                mBinding.countryListSearch.setText("")
                mBinding.countryListSearch.clearFocus()
                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            }
        }
        //   OverScrollDecoratorHelper.setUpOverScroll(mBinding.countryListRv, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        VerticalOverScrollBounceEffectDecorator(RecyclerViewOverScrollDecorAdapter(mBinding.countryListRv))
    }

    override fun onCountryClick(countriesDTO: CountriesDTO) {
        if (mBinding.countryListSearch.isFocused && mBinding.countryListSearch.text.isNullOrEmpty()) {
            mBinding.countryListSearch.clearFocus()
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
            mBinding.countryListSearch.setText("")
        } else {
            hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)


            Handler().postDelayed({
                val argument = bundleOf("country_name" to countriesDTO.country)

                findNavController().navigate(R.id.action_countryList_to_company_frag, argument)

            }, 100)

        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.countryListRv.clearAnimation()
    }

    override fun onResume() {
        super.onResume()

    }

}