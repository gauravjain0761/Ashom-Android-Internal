package com.ashomapp.presentation.profile

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentSettingBinding
import com.ashomapp.databinding.LayoutScanMeBinding
import com.ashomapp.network.retrofit.ResultWrapper
import com.bumptech.glide.Glide
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import android.app.Activity
import android.app.Dialog
import android.content.Context

import android.util.DisplayMetrics

import com.google.android.material.bottomsheet.BottomSheetBehavior


import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.ashomapp.R
import android.content.Intent
import android.net.Uri
import android.widget.Toast

import android.graphics.Bitmap.CompressFormat

import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.ashomapp.MainActivity
import com.ashomapp.database.AshomDBHelper
import com.ashomapp.databinding.SuscessDialogBinding
import com.ashomapp.network.response.auth.UserDTO
import com.ashomapp.network.response.auth.UserData
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.RemainingCompanyDTO
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.utils.*
import com.google.gson.Gson
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.ActivityNotFoundException





class SettingFrag : Fragment() {
    private lateinit var mBinding: FragmentSettingBinding
    private lateinit var drawble: BitmapDrawable
    private lateinit var bitmap: Bitmap


    companion object {
        val list = ArrayList<String>()
        val subscriptionType = MutableLiveData<String>("Free")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSettingBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        HomeFlow.profilefragenable = true
        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
            HomeFlow.home_to_profile = true
            HomeFlow.home_settingcurrentID = R.id.settingFrag
        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
            HomeFlow.financial_to_profile = true
            HomeFlow.financial_settingCurrentID = R.id.settingFrag
        } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
            HomeFlow.forum_to_profile = true
            HomeFlow.forum_settingcurrentID = R.id.settingFrag
        } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
            HomeFlow.news_to_profile = true
            HomeFlow.news_settingcurrentID = R.id.settingFrag
        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
            HomeFlow.search_to_profile = true
            HomeFlow.search_settingcurrentID = R.id.settingFrag
        }


        if (HomeFlow.scanMe == true){
            ScanmeBottommsheet()
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
                        if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                            HomeFlow.home_to_profile = false
                            (requireActivity() as MainActivity).gotoHome()
                        } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                            HomeFlow.financial_to_profile = false
                            (requireActivity() as MainActivity).clicktoCountryList()
                        } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                            HomeFlow.forum_to_profile = false
                            (requireActivity() as MainActivity).clicktoForumTab()
                        } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                            HomeFlow.search_to_profile = false
                            (requireActivity() as MainActivity).clicktoSearchFrag()
                        } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                            HomeFlow.news_to_profile = false
                            (requireActivity() as MainActivity).gotoNewsTab()
                        }
                    }
                }
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (HomeFlow.sectionBottomID == R.id.homeFrag) {
                    HomeFlow.home_to_profile = false
                    (requireActivity() as MainActivity).gotoHome()
                } else if (HomeFlow.sectionBottomID == R.id.countryList) {
                    HomeFlow.financial_to_profile = false
                    (requireActivity() as MainActivity).clicktoCountryList()
                } else if (HomeFlow.sectionBottomID == R.id.forumFrag) {
                    HomeFlow.forum_to_profile = false
                    (requireActivity() as MainActivity).clicktoForumTab()
                } else if (HomeFlow.sectionBottomID == R.id.searchFrag) {
                    HomeFlow.search_to_profile = false
                    (requireActivity() as MainActivity).clicktoSearchFrag()
                } else if (HomeFlow.sectionBottomID == R.id.newsFrag) {
                    HomeFlow.news_to_profile = false
                    (requireActivity() as MainActivity).gotoNewsTab()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this.viewLifecycleOwner,
            onBackPressedCallback
        )
        mBinding.mtoolbar.toolProfile.visibility = View.VISIBLE
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

        }
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            setanimation(it)
            findNavController().navigate(R.id.action_setting_to_setting)

        }
        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)

            findNavController().navigate(R.id.notificationFrag)
        }

        mBinding.notificationContainer.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            findNavController().navigate(R.id.notificationFrag)
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

        getUserdata()
        selectedCompaniesList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                it.forEachIndexed { index, companyDTO ->
                    if (index < 3) {
                        if (index == 0) {
                            mBinding.selectedCompaniesSubtitle.text = "${companyDTO.Company_Name}"
                        } else {
                            mBinding.selectedCompaniesSubtitle.append(",${companyDTO.Company_Name}")
                        }
                    } else {
                        return@forEachIndexed
                    }

                }
            }else{
                mBinding.selectedCompaniesSubtitle.text = ""
            }
        }

        mBinding.settingVersionCode.text =
            "Version ${fetchVersionCode().version_name}(1)"

        subscriptionType.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                mBinding.subscriptionFree.text = "${it}"
                Log.d("subscriptiontype", "${subscriptionType.value}")
                if (it.equals("Yearly")) {
                    mBinding.subscriptionUpgrade.visibility = View.GONE
                    mBinding.subscriptionDeadline.visibility = View.VISIBLE
                    mBinding.premium.setImageResource(R.drawable.gold_ashom)
                } else if (it.equals("Monthly")) {
                    mBinding.subscriptionUpgrade.visibility = View.VISIBLE
                    mBinding.subscriptionDeadline.visibility = View.VISIBLE
                    mBinding.premium.setImageResource(R.drawable.bronz_ashom)
                }else if (it.equals("Quarterly")) {
                    mBinding.subscriptionUpgrade.visibility = View.VISIBLE
                    mBinding.subscriptionDeadline.visibility = View.VISIBLE
                    mBinding.premium.setImageResource(R.drawable.bronz_ashom)
                }else {
                    mBinding.subscriptionUpgrade.visibility = View.VISIBLE
                    mBinding.subscriptionDeadline.visibility = View.GONE
                    mBinding.premium.setImageResource(R.drawable.silver_ashom)

                }
            } else {
                mBinding.subscriptionFree.text = ""
                mBinding.subscriptionDeadline.visibility = View.GONE
                mBinding.premium.setImageResource(R.drawable.silver_ashom)

            }
        }

        Log.d("user", "this is user data" + SharedPrefrenceHelper.user.toString())

        onClickEvent()
    }

    private fun getSelectedCompanies() {
        lifecycleScope.launch {
            val result = HomeRepository.getSelectedComapnies()
            when (result) {
                is ResultWrapper.Success -> {
                    val list = mutableListOf<CompanyDTO>()
                    list.addAll(result.response.map { it })

                    selectedCompaniesList.value = list
                }
                is ResultWrapper.Failure -> {
                    Log.d("selectedcomerror", result.errorMessage)
                }
            }
        }
    }

    private fun getUserdata() {
        val user = SharedPrefrenceHelper.user
        if (!user.profile_pic.isNullOrEmpty()) {
            Log.d("userdata", user.toString())
            Glide.with(AshomAppApplication.instance.applicationContext).load(user.profile_pic)
                .into(mBinding.settingProfilePic)
            mBinding.profileUsername.text = "${user.first_name} ${user.last_name}"
            mBinding.profileUseremail.text = "${user.email}"
            subscriptionType.value = "${user.subscription_type}"


            if (user.subscription_type.equals("Yearly")) {
                mBinding.subscriptionUpgrade.visibility = View.GONE
                mBinding.selectedCompaniesSubtitle.visibility = View.GONE
            } else {
                mBinding.subscriptionUpgrade.visibility = View.VISIBLE
                mBinding.selectedCompaniesSubtitle.visibility = View.VISIBLE
            }


            if (user.login_type.equals("normal")) {
                mBinding.changePasswordContainer.visibility = View.VISIBLE
                mBinding.changePassview.visibility = View.VISIBLE
            } else {
                mBinding.changePasswordContainer.visibility = View.GONE
                mBinding.changePassview.visibility = View.GONE
            }
            if (!user.subscription_expiry_date.isNullOrEmpty()) {
                mBinding.subscriptionDeadline.visibility = View.VISIBLE
                val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                df.setTimeZone(TimeZone.getTimeZone("UTC"))
                val date: Date = df.parse(user.subscription_expiry_date)
                df.setTimeZone(TimeZone.getDefault())
                val formattedDate: String = df.format(date)
                val simpleDateFormat = SimpleDateFormat("dd MMM yyyy")
                val dateTime = simpleDateFormat.format(date)
                mBinding.subscriptionDeadline.text = "EXPIRES ON: ${
                   dateTime
                }"
            }else{
                mBinding.subscriptionDeadline.visibility = View.GONE
            }


        }
    }


    private fun onClickEvent() {
        mBinding.profileView.setOnClickListener {
            findNavController().navigate(R.id.action_settingFrag_to_profileFragment)
        }
        mBinding.tAndCContainer.setOnClickListener {
            if (HomeFlow.sectionBottomID == R.id.homeFrag){
                HomeFlow.home_settingcurrentID = TERMS_CONDITION
            }else if (HomeFlow.sectionBottomID == R.id.countryList){
                HomeFlow.financial_settingCurrentID =TERMS_CONDITION
            }else if (HomeFlow.sectionBottomID == R.id.forumFrag){
                HomeFlow.forum_settingcurrentID = TERMS_CONDITION
            }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
                HomeFlow.news_settingcurrentID = TERMS_CONDITION
            }else if (HomeFlow.sectionBottomID == R.id.searchFrag){
                HomeFlow.search_settingcurrentID = TERMS_CONDITION
            }
            ApplyGTMEvent("view_tnc","view_tnc_count","view_tnc")

            val args = bundleOf("ppurl" to TERMS_CONDITION_URL)
            findNavController().navigate(R.id.action_settingFrag_to_privacy_policy, args)
        }
        mBinding.privacypolicyContainer.setOnClickListener {
            if (HomeFlow.sectionBottomID == R.id.homeFrag){
                HomeFlow.home_settingcurrentID = PRIVACY_POLICY
            }else if (HomeFlow.sectionBottomID == R.id.countryList){
                HomeFlow.financial_settingCurrentID =PRIVACY_POLICY
            }else if (HomeFlow.sectionBottomID == R.id.forumFrag){
                HomeFlow.forum_settingcurrentID = PRIVACY_POLICY
            }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
                HomeFlow.news_settingcurrentID = PRIVACY_POLICY
            }else if (HomeFlow.sectionBottomID == R.id.searchFrag){
                HomeFlow.search_settingcurrentID = PRIVACY_POLICY
            }
            ApplyGTMEvent("view_privacy_policy","view_privacy_policy_count","view_privacy_policy")

            val args = bundleOf("ppurl" to PRIVACY_POLICY_URL)
            findNavController().navigate(R.id.action_settingFrag_to_privacy_policy, args)

        }
        mBinding.aboutusContainer.setOnClickListener {
            // findNavController().navigate(R.id.)
            if (HomeFlow.sectionBottomID == R.id.homeFrag){
                HomeFlow.home_settingcurrentID = ABOUT_US
            }else if (HomeFlow.sectionBottomID == R.id.countryList){
                HomeFlow.financial_settingCurrentID =ABOUT_US
            }else if (HomeFlow.sectionBottomID == R.id.forumFrag){
                HomeFlow.forum_settingcurrentID = ABOUT_US
            }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
                HomeFlow.news_settingcurrentID = ABOUT_US
            }else if (HomeFlow.sectionBottomID == R.id.searchFrag){
                HomeFlow.search_settingcurrentID = ABOUT_US
            }
            ApplyGTMEvent("about_us_app","about_us_count","about_us_app_view")

            val args = bundleOf("ppurl" to ABOUT_US_URL)
            findNavController().navigate(R.id.action_settingFrag_to_privacy_policy, args)
        }
        mBinding.changePasswordContainer.setOnClickListener {
            // findNavController().navigate(R.id.)
            ApplyGTMEvent("change_password","change_password_count","change_password")

            findNavController().navigate(R.id.action_settingFrag_to_changePassword)
        }
        mBinding.rateUsContainer.setOnClickListener {
            // findNavController().navigate(R.id.)
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.ashomapp")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.ashomapp")
                    )
                )
            }
        }
        mBinding.contactusContainer.setOnClickListener {
            findNavController().navigate(R.id.action_settingFrag_to_contactUs)

        }
        mBinding.subscriptionUpgrade.setOnClickListener {
            ApplyGTMEvent("view_subscription","view_subscription_count","view_subscription")

            val args = bundleOf("where" to "profile")
            findNavController().navigate(R.id.subscriptionDialog, args)

        }
        mBinding.selectedCompaniesContainer.setOnClickListener {
            ApplyGTMEvent("view_selected_companies","view_selected_companies_count","view_selected_companies")

            findNavController().navigate(R.id.action_settingFrag_to_selectedCompanies)
        }
        mBinding.restorePurchaseContainer.setOnClickListener {
            // findNavController().navigate(R.id.)
            temp_showToast("Restore purchase")
        }
        mBinding.inviteFriendContainer.setOnClickListener {
            // findNavController().navigate(R.id.)

            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=com.ashomapp"
            )
            startActivity(Intent.createChooser(intent, "Share with:"))
        }
        mBinding.scanmeContainer.setOnClickListener {
            // findNavController().navigate(R.id.)
            HomeFlow.scanMe = true
            ScanmeBottommsheet()

        }
        mBinding.logoutContainer.setOnClickListener {
            // findNavController().navigate(R.id.)
            UserlogoutDialog()
        }

    }

    private fun UserlogoutDialog() {
        // findNavController().navigate(R.id.otp_Fragment)
        val mBinding = SuscessDialogBinding.inflate(LayoutInflater.from(requireActivity()))
        val builder = Dialog(requireActivity())
        mBinding.dialogTitle.text = "Logout"
        mBinding.dialogSubTitle.text = "Are you sure you want to logout? "
        mBinding.dialogOk.text = "OK"
        mBinding.dialogOk.setOnClickListener {
            mBinding.dialogOk.setBackgroundColor(Color.parseColor("#BFDDED"))

            Handler().postDelayed({
                mBinding.dialogCancel.setBackgroundColor(Color.TRANSPARENT)
                builder.dismiss()
                logoutprocess(builder)
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

    private fun logoutprocess(dialog: DialogInterface?) {

        lifecycleScope.launch {
            val result = HomeRepository.UserLogout()
            when (result) {
                is ResultWrapper.Success -> {
                    SharedPrefrenceHelper.devicetoken = "0"
                    SharedPrefrenceHelper.token = "0"
                    MainActivity.usertoken.value = ""
                    selectedCompaniesList.value = emptyList()
                    val navOptions =
                        NavOptions.Builder().setPopUpTo(R.id.settingFrag, true).build()
                    findNavController().navigate(R.id.loginFragment, null, navOptions)

                }
                is ResultWrapper.Failure -> {
                    SharedPrefrenceHelper.devicetoken = "0"
                    SharedPrefrenceHelper.token = "0"
                    MainActivity.usertoken.value = ""
                    val navOptions =
                        NavOptions.Builder().setPopUpTo(R.id.settingFrag, true).build()
                    findNavController().navigate(R.id.loginFragment, null, navOptions)
                }
            }
        }
    }

    private fun ScanmeBottommsheet() {
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.layout_scan_me, null)
        bottomSheet.setCancelable(false)
        val btnClose = view.findViewById<ImageView>(R.id.cancel_scanme)
        val shareqrcode = view.findViewById<CardView>(R.id.cv_share_qr)
        val shareqrlink = view.findViewById<CardView>(R.id.cv_sharelink)
        val qrcode = view.findViewById<ImageView>(R.id.share_qr_code)
        shareqrcode.setOnClickListener {

            //Share image using intent to another app
            val bitmapDrawable: BitmapDrawable = qrcode.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            shareImageandText(bitmap)
        }

        shareqrlink.setOnClickListener {
            bottomSheet.dismiss()
            HomeFlow.scanMe = false
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=com.ashomapp"
            )
            startActivity(Intent.createChooser(intent, "Share with:"))
        }
        btnClose.setOnClickListener {
            // on below line we are calling a dismiss
            // method to close our dialog.
            bottomSheet.dismiss()
            HomeFlow.scanMe = false
        }

        bottomSheet.setContentView(view)
        bottomSheet.setOnShowListener { dialoginterface ->

            setupFullHeight(bottomSheet, requireActivity())

        }
        bottomSheet.show()

    }


    //share image using intent
    private fun shareImageandText(bitmap: Bitmap) {
        val uri: Uri = getmageToShare(bitmap)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra(Intent.EXTRA_SUBJECT, "ASHOM.APP")
        intent.type = "image/png"
        startActivity(Intent.createChooser(intent, "Share Via"))
    }

    // Retrieving the url to share
    private fun getmageToShare(bitmap: Bitmap): Uri {
        val imagefolder = File(requireActivity().cacheDir, "images");
        var uri: Uri? = null;
        try {
            imagefolder.mkdirs();
            val file = File(imagefolder, "app_qrcode.png");
            val outputStream = FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(
                AshomAppApplication.instance.applicationContext,
                "com.ashomapp.fileprovider",
                file
            );
        } catch (e: Exception) {
            Toast.makeText(
                AshomAppApplication.instance.applicationContext,
                "" + e.message,
                Toast.LENGTH_LONG
            ).show();
        }
        return uri!!;
    }

    private fun getRemainingVisit() {
        /* lifecycleScope.launch {
             val result = HomeRepository.getRemainingVisit()
             when(result){
                 is ResultWrapper.Success ->{
                     list.clear()
                     val newsitem = Gson().fromJson(result.response.toString(), RemainingCompanyDTO::class.java)
                     Log.d("remainingvisit", "${newsitem}, ${result.response.toString()}")
                       if (!newsitem.visited_companies.isNullOrEmpty()){
                           list.addAll(newsitem.visited_companies.map { it })
                           val db = AshomDBHelper(AshomAppApplication.instance.applicationContext, null)
                           val dsdata = db.getSelectedCompany(newsitem.visited_companies)
                           Log.d("selectedcompanies", dsdata.joinToString(","))

                           mBinding.selectedCompaniesSubtitle.text = "${dsdata.joinToString(",")}"
                       }else{
                           list.clear()
                       }
                 }
                 is ResultWrapper.Failure ->{
                     Log.d("remainingvisit", result.errorMessage)
                 }
             }
         }*/
    }

    override fun onResume() {
        super.onResume()
        getSelectedCompanies()
        getSSSUserData()

    }

    private fun getSSSUserData() {
        lifecycleScope.launch {
            val result = HomeRepository.getUserData(SharedPrefrenceHelper.token.toString())
            when (result) {
                is ResultWrapper.Success -> {
                    //username.value = "${result.response.userdata.first_name} ${result.response.userdata.last_name}"
                    //  profileImage.value = "${result.response.userdata.profile_pic}"
                    Glide.with(AshomAppApplication.instance.applicationContext)
                        .load(result.response.userdata.profile_pic)
                        .into(mBinding.mtoolbar.mainProfilePic)
                    SharedPrefrenceHelper.user = UserDTO(
                        result.response.userdata.first_name,
                        result.response.userdata.last_name,
                        result.response.userdata.email,
                        result.response.userdata.country_code,
                        result.response.userdata.mobile,
                        result.response.userdata.profile_pic,
                        result.response.userdata.login_type,
                        result.response.userdata.social_id,
                        result.response.userdata.token,
                        result.response.userdata.email_verified,
                        result.response.userdata.subscribed,
                        result.response.userdata.subscription_type,
                        result.response.userdata.subscription_expiry_date,

                        )
                    if (!result.response.userdata.subscription_expiry_date.isNullOrEmpty()) {
                        mBinding.subscriptionDeadline.visibility = View.VISIBLE
                        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                        df.setTimeZone(TimeZone.getTimeZone("UTC"))
                        val date: Date = df.parse( result.response.userdata.subscription_expiry_date)
                        df.setTimeZone(TimeZone.getDefault())
                        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy")
                        val dateTime = simpleDateFormat.format(date)

                        mBinding.subscriptionDeadline.text = "EXPIRES ON: ${
                           dateTime
                        }"

                    }else{
                        mBinding.subscriptionDeadline.visibility = View.GONE
                    }
                    subscriptionType.value = result.response.userdata.subscription_type

                }
                is ResultWrapper.Failure -> {

                }
            }
        }
    }


}