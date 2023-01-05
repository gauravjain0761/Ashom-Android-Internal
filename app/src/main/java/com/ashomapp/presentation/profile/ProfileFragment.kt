package com.ashomapp.presentation.profile

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.presentation.auth.AuthViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import android.widget.Toast

import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import androidx.activity.OnBackPressedCallback
import androidx.core.animation.doOnEnd
import androidx.navigation.fragment.findNavController
import com.ashomapp.MainActivity
import com.ashomapp.databinding.FragmentProfileBinding
import com.ashomapp.presentation.auth.NetworkState
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.utils.*
import com.bumptech.glide.Glide
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress


class ProfileFragment : Fragment() {
    private lateinit var mBinding: FragmentProfileBinding
    private val CAMERA_INTENT_CODE = 101
    private val GALLERY_INTENT_CODE = 100
    private val mProfileViewModel by activityViewModels<ProfileViewModel>()
    var profileUpdate_process = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentProfileBinding.inflate(layoutInflater, container, false).apply {
            lifecycleOwner = this@ProfileFragment
            viewmodel = mProfileViewModel
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindProgressButton(mBinding.updateSubmit)
        mBinding.mtoolbar.mainProfilePic.visibility = View.INVISIBLE

        if (HomeFlow.sectionBottomID == R.id.homeFrag){
            HomeFlow.home_settingcurrentID = R.id.profileFragment
        }else if (HomeFlow.sectionBottomID == R.id.countryList){
            HomeFlow.financial_settingCurrentID = R.id.profileFragment
        }else if (HomeFlow.sectionBottomID == R.id.forumFrag){
            HomeFlow.forum_settingcurrentID = R.id.profileFragment
        }else if (HomeFlow.sectionBottomID == R.id.newsFrag){
            HomeFlow.news_settingcurrentID = R.id.profileFragment
        }else if (HomeFlow.sectionBottomID == R.id.searchFrag){
            HomeFlow.search_settingcurrentID = R.id.profileFragment
        }
        /*notificationcounter.observe(this.viewLifecycleOwner) {
            if (it != 0) {
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            } else {
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }*/
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

        mBinding.mtoolbar.notificationBellIcon.visibility = View.GONE
       /* mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)

            findNavController().navigate(R.id.notificationFrag)
        }*/
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
        val user = SharedPrefrenceHelper.user
        mProfileViewModel.setProfileData(user)
        if (user.login_type.equals("normal")) {
            mBinding.updateProEmail.apply {
                isEnabled = true
                isClickable = true
                alpha = 1f


            }
        } else {
            mBinding.updateProEmail.apply {
                alpha = 0.7f
                isClickable = false
                isEnabled = false
                setOnKeyListener(null)
            }
        }
        Glide.with(AshomAppApplication.instance.applicationContext)
            .load(user.profile_pic)
            .into(mBinding.updateProIv)
        mBinding.updateSubmit.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            setanimation(it)
            submitdata()
        }
        mBinding.updateProChangePro.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            permission()
        }
        mBinding.updateProIv.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            setanimation(it)
            permission()
        }

        mProfileViewModel.updateprofileNetworkState.observe(viewLifecycleOwner) { state ->
            when (state) {
                NetworkState.LOADING_STARTED -> {
                    mBinding.updateSubmit.apply {
                        showProgress()
                        isClickable = false
                    }
                }
                NetworkState.LOADING_STOPPED -> {
                    mBinding.updateSubmit.apply {
                        hideProgress("Update")
                        isClickable = true
                    }
                }
                NetworkState.SUCCESS -> {
                    ApplyGTMEvent("view_update_profile","view_update_profile_count","GTM_view_update_profile")

                    mBinding.updateSubmit.apply {
                        hideProgress("Update")
                        isClickable = true
                    }
                    if (profileUpdate_process) {
                        profileUpdate_process = false
                        mBinding.updateProFirstname.clearFocus()
                        mBinding.updateProLastname.clearFocus()
                        mBinding.updateProEmail.clearFocus()
                        mBinding.updateProPhone.clearFocus()
                        successDialog(requireActivity(), "Success", "Profile Updated Successfully.")
                    }

                }
                NetworkState.FAILED -> {
                    mBinding.updateSubmit.apply {
                        hideProgress("Update")
                        isClickable = true
                    }
                    // temp_showToast("Something went wrong try again")
                }
            }

        }

    }

    private fun submitdata() {
        val firstname = mProfileViewModel.pro_firstname.value.toString().trim()
        val lastname = mProfileViewModel.pro_lastname.value.toString().trim()
        val phone = mProfileViewModel.pro_phone.value.toString().trim()
        val email = mProfileViewModel.pro_email.value.toString().trim()

        if (firstname.isNullOrEmpty()) {
            mBinding.updateProFirstname.setError("Enter first name")
        } else if (lastname.isNullOrEmpty()) {
            mBinding.updateProLastname.setError("Enter last name")
        } else if (email.isNullOrEmpty() || !isValidEmail(email)) {
            mBinding.updateProEmail.setError("Enter valid email address")
        } else {
            profileUpdate_process = true
            mProfileViewModel.updateprofile()

        }
    }

    private fun getCameraImage() {
        val cameraIntent = Intent("android.media.action.IMAGE_CAPTURE")
        startActivityForResult(cameraIntent, CAMERA_INTENT_CODE)
    }

    private fun getGalleryImage() {
        val gallery = Intent(Intent.ACTION_PICK).apply { this.type = "image/*" }
        startActivityForResult(gallery, GALLERY_INTENT_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_INTENT_CODE && data != null) {
            val uri = data.data
            uri ?: return
            mBinding.updateProIv.setImageURI(uri)
            SharedPrefrenceHelper.user.profile_pic = "$uri"
            requireActivity().contentResolver.openInputStream(uri)?.let { inputStream ->
                val name = FileUtils.getFile(requireContext(), uri).name
                val bytes = inputStream.readBytes()
                mProfileViewModel.profile_pic = bytes to name

            }

        } else if (requestCode == CAMERA_INTENT_CODE && data != null) {
            val uri = getImageUri(data.extras!!.get("data") as Bitmap)
            uri ?: return

            mBinding.updateProIv.setImageURI(uri)
            SharedPrefrenceHelper.user.profile_pic = "$uri"
            requireActivity().contentResolver.openInputStream(uri)?.let { inputStream ->
                val name = FileUtils.getFile(requireContext(), uri).name
                val bytes = inputStream.readBytes()

                mProfileViewModel.profile_pic = bytes to name


            }


        }
    }


    fun getImageUri(inImage: Bitmap): Uri? {
        val wrapper = ContextWrapper(AshomAppApplication.instance.applicationContext)
        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "P_${System.currentTimeMillis()}.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.fromFile(file)
    }

    private fun permission() {
        Dexter.withContext(AshomAppApplication.instance.applicationContext)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE

            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) { /* ... */
                    if (report.areAllPermissionsGranted()) {
                        //enable popup
                        imageselectDialog()
                    }
                    if (report.isAnyPermissionPermanentlyDenied()) {
                        // permission is denied permanently,
                        // we will show user a dialog message.
                        showRationalDialogForPermissions()
                    }


                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) { /* ... */
                    token?.continuePermissionRequest()
                    // showRationalDialogForPermissions()
                }
            }).check()
    }

    private fun showRationalDialogForPermissions() {
        val alertDialog: AlertDialog.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                AlertDialog.Builder(
                    requireContext(),
                    android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
                )
            } else {
                AlertDialog.Builder(
                    requireContext(),
                    R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
                )
            }
        // set alert dialog message text color
        alertDialog.setTitle("Need Permissions")
        val message =
            SpannableString("This app needs permission to use this feature. You can grant them in app settings.")
        message.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            message.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        alertDialog.setMessage(message)
        val gotosetting =
            SpannableString("GO TO SETTINGS")
        gotosetting.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            gotosetting.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        alertDialog.setPositiveButton(
            gotosetting
        ) { _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", "com.ashomapp", null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
        val gotocancel =
            SpannableString("CANCEL")
        gotocancel.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            gotocancel.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        alertDialog.setNegativeButton(
            "$gotocancel"
        ) { _, _ -> }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    private fun imageselectDialog() {
        // findNavController().navigate(R.id.otp_Fragment)
        val fonts = arrayOf(
            "Camera", "Gallery"
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            AlertDialog.Builder(
                requireActivity(),
                android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
            )
        } else {
            AlertDialog.Builder(
                requireActivity(),
                R.style.ThemeOverlay_MaterialComponents_Dialog_Alert
            )
        }
        builder.setTitle("Choose image from ? ")
        builder.setItems(fonts) { dialog, which ->
            if ("Camera" == fonts[which]) {
                getCameraImage()
                // Toast.makeText(AshomAppApplication.instance.applicationContext, "Your are selected camera", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else if ("Gallery" == fonts[which]) {
                getGalleryImage()
                //  Toast.makeText(AshomAppApplication.instance.applicationContext, "Your are selected galery", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            // the user clicked on colors[which]
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        builder.setCancelable(false)
        builder.show()
    }
}