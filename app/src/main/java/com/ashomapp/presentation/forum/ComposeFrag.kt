package com.ashomapp.presentation.forum

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.animation.doOnEnd
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.FragmentComposeBinding
import com.ashomapp.network.response.dashboard.CompanyDTO
import com.ashomapp.network.response.dashboard.ForumDTO
import com.ashomapp.network.response.dashboard.NewsItemDTO
import com.ashomapp.presentation.auth.NetworkState
import com.ashomapp.presentation.home.HomeFlow
import com.ashomapp.utils.ApplyGTMEvent
import com.ashomapp.utils.FileUtils
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.showKeyboard
import com.bumptech.glide.Glide
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.lang.Exception


class ComposeFrag : Fragment() {
    private lateinit var mBinding: FragmentComposeBinding
    private val GALLERY_INTENT_CODE = 100
    private val mForumViewmodel by activityViewModels<ForumViewModel>()
    private var navigate = false
    private var isImageselection = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentComposeBinding.inflate(layoutInflater, container, false).apply {
            viewmodel = mForumViewmodel
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindProgressButton(mBinding.postForum)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mBinding.forumAddCancel.setOnClickListener {
            mBinding.etComposeForum.setText("")
            ObjectAnimator.ofFloat(mBinding.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        findNavController().navigateUp()
                    }
                }
        }
        mBinding.etComposeForum.requestFocus()
        showKeyboard(AshomAppApplication.instance.applicationContext, mBinding.etComposeForum)
        try {
            val args = requireArguments().getString("news_item_to_forum")
            Log.d("newsssitem", args.toString())
            if (!args.isNullOrEmpty()) {
                val newsitem = Gson().fromJson(args, NewsItemDTO::class.java)
                Glide.with(AshomAppApplication.instance.applicationContext).load(newsitem.image_url)
                    .into(mBinding.composeImage)
                mForumViewmodel.imageUrl.value = newsitem.image_url
                mForumViewmodel.composemsg.value = "\n\n\n\n\n${newsitem.title}"
                mForumViewmodel.composemsglink.value = "${newsitem.link}"
            } else {
                val margs = requireArguments().getString("edit_forum")
                Log.d("newsssitem", margs.toString())
                if (!margs.isNullOrEmpty()) {
                    val newsitem = Gson().fromJson(margs, ForumDTO::class.java)
                    Glide.with(AshomAppApplication.instance.applicationContext)
                        .load(newsitem.content_image).into(mBinding.composeImage)
                    mForumViewmodel.ComposeforumID.value = newsitem.id
                    mForumViewmodel.imageUrl.value = newsitem.content_image
                    mForumViewmodel.composemsg.value = "${newsitem.content}"
                    mBinding.etComposeForum.setText("${newsitem.content}")

                } else {
                    mBinding.etComposeForum.setText("")
                    mForumViewmodel.imageUrl.value = ""
                    mForumViewmodel.composemsg.value = ""
                    mForumViewmodel.composemsglink.value = ""
                    mForumViewmodel.compose_image = null
                }
            }
        } catch (e: Exception) {
            mForumViewmodel.imageUrl.value = ""
            mForumViewmodel.composemsg.value = ""
            mForumViewmodel.composemsglink.value = ""
            mForumViewmodel.compose_image = null
            Log.d("Error", "error")
            mBinding.etComposeForum.setText("")
        }
        mForumViewmodel.getUserdata()
        mForumViewmodel.profileImage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Glide.with(AshomAppApplication.instance.applicationContext).load(it)
                    .into(mBinding.forumProfilePic)
                mBinding.username.text = mForumViewmodel.username.value
            }
        }
        mForumViewmodel.loading.observe(viewLifecycleOwner) { state ->
            when (state) {
                NetworkState.LOADING_STARTED -> {
                    mBinding.postForum.apply {
                        showProgress()
                        isClickable = false
                    }
                }
                NetworkState.LOADING_STOPPED -> {
                    mBinding.postForum.apply {
                        hideProgress("Post")
                        isClickable = true
                    }
                }
                NetworkState.SUCCESS -> {
                    ApplyGTMEvent("posted_compose_forum","posted_compose_forum_count","posted_compose_forum")

                    mBinding.postForum.apply {
                        hideProgress("Post")
                        isClickable = true
                    }
                    if (navigate) {
                        navigate = false
                        HomeFlow.reloadForum = true
                        mForumViewmodel.composemsg.value = ""
                        mForumViewmodel.loading.removeObservers(this)
                        findNavController().navigate(R.id.forumFrag)
                    }
                }
                NetworkState.FAILED -> {
                    mForumViewmodel.loading.removeObservers(this)
                    mBinding.postForum.apply {
                        hideProgress("Post")
                        isClickable = true
                    }
                }
            }
        }
        mBinding.postForum.setOnClickListener {
            if (!mForumViewmodel.ComposeforumID.value.isNullOrEmpty()) {
                mForumViewmodel.UpdateCompose()
                navigate = true
            } else {
                mForumViewmodel.submitCompose()
                navigate = true
            }
        }
        mBinding.forumAddImage.setOnClickListener {
            isImageselection = true
            permission()
        }
    }

    private fun getGalleryImage() {
        val gallery = Intent(Intent.ACTION_PICK).apply { this.type = "image/*" }
        startActivityForResult(gallery, GALLERY_INTENT_CODE)
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
                        getGalleryImage()
                    } else {
                        showRationalDialogForPermissions()
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) { /* ... */
                    token?.continuePermissionRequest()
                    showRationalDialogForPermissions()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_INTENT_CODE && data != null) {
            val uri = data.data
            uri ?: return
            mBinding.composeImage.setImageURI(uri)
            isImageselection = false
            requireActivity().contentResolver.openInputStream(uri)?.let { inputStream ->
                val name = FileUtils.getFile(requireContext(), uri).name
                val bytes = inputStream.readBytes()
                mForumViewmodel.compose_image = bytes to name
            }
        }
    }
    override fun onPause() {
        super.onPause()
        hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
    }
}