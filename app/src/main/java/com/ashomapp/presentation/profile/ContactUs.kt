package com.ashomapp.presentation.profile

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.databinding.FragmentContactUsBinding
import com.ashomapp.network.response.Countrycodelist
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.auth.CountryCodeAdapter
import com.ashomapp.presentation.auth.onCountryCodeClick
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.utils.*
import com.bumptech.glide.Glide
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.lang.IllegalStateException


class ContactUs : Fragment(), onCountryCodeClick {
    private lateinit var mBinding: FragmentContactUsBinding
    private lateinit var bottomSheet: BottomSheetDialog
    private val countryCodeList = ArrayList<Countrycodelist>()
    var countryCode = "971"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentContactUsBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindProgressButton(mBinding.loginSubmit)
        setetanimaton()
        if (HomeFlow.sectionBottomID == R.id.homeFrag){
            HomeFlow.home_settingcurrentID = R.id.contactUs
        }else if (HomeFlow.sectionBottomID == R.id.countryList){
            HomeFlow.financial_settingCurrentID = R.id.contactUs
        }else if (HomeFlow.sectionBottomID == R.id.forumFrag){
            HomeFlow.forum_settingcurrentID = R.id.contactUs
        }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
            HomeFlow.news_settingcurrentID = R.id.contactUs
        }else if (HomeFlow.sectionBottomID == R.id.searchFrag){
            HomeFlow.search_settingcurrentID = R.id.contactUs
        }
        mBinding.mtoolbar.toolProfile.visibility = View.INVISIBLE
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
        countryCodeList.clear()
        countrycodelist.forEach {
            countryCodeList.add(it)
            if (it.country_code.equals("971")) {
                Glide.with(AshomAppApplication.instance.applicationContext)
                    .load("https://countryflagsapi.com/png/${it.country_name}")
                    .into(mBinding.countFlag)
                mBinding.countCode.text = "+${it.country_code}"

            }
        }
        mBinding.ccp.setOnClickListener {
            countrycodeBottomSheet()
        }
        try {
            MainActivity.layoutheight.observe(viewLifecycleOwner) { heightDifference ->
                if (heightDifference > 300) {
                    val h2 = mBinding.loginScrool.height + 300
                    Log.d("Keyboardsize", "$heightDifference , $h2")
                    mBinding.loginviewdsd.minimumHeight = h2
                } else {
                    val h2 = mBinding.loginScrool.height - 300
                    Log.d("Keyboardsize", "$heightDifference, $h2")
                    mBinding.loginviewdsd.minimumHeight = h2
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

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

        mBinding.loginSubmit.setOnClickListener {
            val fullname = mBinding.contactusFullname.text.toString().trim()
            val email = mBinding.contactUsEmail.text.toString().trim()
            val subject = mBinding.contactUsSubject.text.toString().trim()
            val message = mBinding.contactUsMessage.text.toString().trim()
            val phone = mBinding.loginPhone.text.toString().trim()

            if (fullname.isNullOrEmpty()) {
                mBinding.contactusFullname.setError("Enter full name")
            } else if (email.isNullOrEmpty() || !isValidEmail(email)) {
                mBinding.contactUsEmail.setError("Enter valid email")
            } else if (subject.isNullOrEmpty()) {
                mBinding.contactUsSubject.setError("Enter subject")
            } else if (message.isNullOrEmpty()) {
                mBinding.contactUsMessage.setError("Enter message")
            } else if (phone.isNullOrEmpty()) {
                mBinding.loginPhone.setError("Enter phone number")

            } else {
                hideKeyboard(AshomAppApplication.instance.applicationContext, it)
                setanimation(it)
                mBinding.loginSubmit.apply {
                    showProgress()
                    isClickable = false
                }
                submitdata(fullname, email, subject, message, phone)
            }
        }
    }

    private fun setetanimaton() {
        mBinding.contactusFullname.setOnFocusChangeListener { view, b ->
            if (b) {
                mBinding.contactusFullnameView.setBackgroundColor(resources.getColor(R.color.app_color))
            } else {
                mBinding.contactusFullnameView.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }
        mBinding.contactUsEmail.setOnFocusChangeListener { view, b ->
            if (b) {
                mBinding.contactUsEmailView.setBackgroundColor(resources.getColor(R.color.app_color))
            } else {
                mBinding.contactUsEmailView.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }
        mBinding.contactUsSubject.setOnFocusChangeListener { view, b ->
            if (b) {
                mBinding.contactUsSubjectView.setBackgroundColor(resources.getColor(R.color.grey))
            } else {
                mBinding.contactUsSubjectView.setBackgroundColor(resources.getColor(R.color.btn_color))
            }
        }
        mBinding.contactUsMessage.setOnFocusChangeListener { view, b ->
            if (b) {
                mBinding.contactUsMessageView.setBackgroundColor(resources.getColor(R.color.grey))
            } else {
                mBinding.contactUsMessageView.setBackgroundColor(resources.getColor(R.color.btn_color))
            }
        }
        mBinding.loginPhone.setOnFocusChangeListener { view, b ->
            if (b) {
                mBinding.loginPhoneView.setBackgroundColor(resources.getColor(R.color.grey))
            } else {
                mBinding.loginPhoneView.setBackgroundColor(resources.getColor(R.color.btn_color))
            }
        }
    }

    private fun submitdata(
        name: String,
        email: String,
        subject: String,
        message: String,
        phone: String
    ) {
        lifecycleScope.launch {
            val result = HomeRepository.submitContactUs(
                name,
                email,
                subject,
                message,
                "+$countryCode$phone "
            )
            when (result) {
                is ResultWrapper.Success -> {
                    mBinding.loginSubmit.apply {
                        hideProgress("SUBMIT")
                        isClickable = true
                    }
                    mBinding.contactusFullname.setText("")
                    mBinding.contactUsEmail.setText("")
                    mBinding.contactUsSubject.setText("")
                    mBinding.contactUsMessage.setText("")
                    mBinding.contactusFullname.clearFocus()
                    mBinding.contactUsEmail.clearFocus()
                    mBinding.contactUsSubject.clearFocus()
                    mBinding.contactUsMessage.clearFocus()


                    contactusSuccessDialog()
                }
                is ResultWrapper.Failure -> {
                    mBinding.loginSubmit.apply {
                        hideProgress("SUBMIT")
                        isClickable = true
                    }
                    temp_showToast("Some thing went wrong try again...")
                }
            }
        }
    }

    private fun countrycodeBottomSheet() {
        bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.countrycode_bottosheet_layout, null)
        val btnClose = view.findViewById<ImageView>(R.id.country_code_cancel)
        val viewCountryrcode = view.findViewById<CardView>(R.id.country_code_view)
        val recyclerview = view.findViewById<RecyclerView>(R.id.countrycodeList_rv)
        val et_search = view.findViewById<EditText>(R.id.countrycode_list_search)

        val adapter = CountryCodeAdapter(countryCodeList, this)
        et_search.doAfterTextChanged {
            adapter.filter.filter(it.toString())
        }
        btnClose.setOnClickListener {
            ObjectAnimator.ofFloat(viewCountryrcode, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        bottomSheet.dismiss()
                    }
                }
        }

        recyclerview.adapter = adapter
        adapter.notifyDataSetChanged()

        bottomSheet.setContentView(view)

        bottomSheet.show()
    }

    override fun onCountryCodeClick(countrycodelist: Countrycodelist) {
        bottomSheet.dismiss()
        mBinding.countFlag
        //  mAuthViewModel.country_code.value = "${countrycodelist.country_code}"
        countryCode = countrycodelist.country_code
        if (countrycodelist.country_name.equals("Bahamas")) {
            mBinding.countFlag.setImageResource(R.drawable.bahamas)
        } else if (countrycodelist.country_name.equals("British Indian Ocean Territory")) {
            mBinding.countFlag.setImageResource(R.drawable.british_indian_ocean_territory)
        } else if (countrycodelist.country_name.equals("Cape Verde")) {
            mBinding.countFlag.setImageResource(R.drawable.cape_verde)
        } else if (countrycodelist.country_name.equals("Cayman Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_cayman_islands)
        } else if (countrycodelist.country_name.equals("Central African Republic")) {
            mBinding.countFlag.setImageResource(R.drawable.central_africal_republic)
        } else if (countrycodelist.country_name.equals("Cocos (Keeling) Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.coco_island)
        } else if (countrycodelist.country_name.equals("Comoros")) {
            mBinding.countFlag.setImageResource(R.drawable.comoros)
        } else if (countrycodelist.country_name.equals("Congo, Democratic Republic of the")) {
            mBinding.countFlag.setImageResource(R.drawable.congo)
        } else if (countrycodelist.country_name.equals("Cook Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_cook_islands)
        } else if (countrycodelist.country_name.equals("Curacao")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_curacao)
        } else if (countrycodelist.country_name.equals("Czech Republic")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_czech_republic)
        } else if (countrycodelist.country_name.equals("Falkland Islands (Malvinas)")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_falkland_islands)
        }//Falkland Islands (Malvinas)  Czech Republic
        else if (countrycodelist.country_name.equals("Faroe Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_faroe_islands)
        }//Falkland Islands (Malvinas)
        else if (countrycodelist.country_name.equals("Holy See (Vatican City State)")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_vatican_city)
        }//Falkland Islands (Malvinas)
        else if (countrycodelist.country_name.equals("Macedonia")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_macedonia)
        }//Marshall Islands
        else if (countrycodelist.country_name.equals("Marshall Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_marshall_islands)
        } else if (countrycodelist.country_name.equals("Netherlands Antilles")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_netherlands_antilles)
        } else if (countrycodelist.country_name.equals("Northern Mariana Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_northern_mariana_islands)
        } else if (countrycodelist.country_name.equals("Palestinian Territory, Occupied")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_palestine)
        } else if (countrycodelist.country_name.equals("Saint Vincent and the Grenadines")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_saint_vicent_and_the_grenadines)
        }//Swaziland Turks and Caicos Islands
        else if (countrycodelist.country_name.equals("Swaziland")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_swaziland)
        } else if (countrycodelist.country_name.equals("Virgin Islands, US")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_us_virgin_islands)
        } else if (countrycodelist.country_name.equals("Turks and Caicos Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_turks_and_caicos_islands)
        } else {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load("https://countryflagsapi.com/png/${countrycodelist.country_name}")
                .into(mBinding.countFlag)
        }

        mBinding.countCode.text = "+${countrycodelist.country_code}"
        Log.d("countrycode", "Selected Country : ${countrycodelist.country_code}")
    }

    private fun contactusSuccessDialog() {
        // findNavController().navigate(R.id.otp_Fragment)
        successDialog(
            requireActivity(),
            "ASHOM.APP",
            "Thank you for contacting ASHOM.APP. We'll get back to you shortly"
        )

    }
}