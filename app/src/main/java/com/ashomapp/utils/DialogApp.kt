package com.ashomapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.LoadingDialogBinding
import com.ashomapp.databinding.SuscessDialogBinding

fun successDialog(activity: Activity, title: String, mmessage: String) {
    // findNavController().navigate(R.id.otp_Fragment)
    val mBinding = SuscessDialogBinding.inflate(LayoutInflater.from(activity))
    val builder = Dialog(activity)


    mBinding.dialogTitle.text = "$title"
    mBinding.dialogSubTitle.text = "$mmessage"
    mBinding.dialogOk.setOnClickListener {
        mBinding.dialogOk.setBackgroundColor(Color.parseColor("#BFDDED"))
        Handler().postDelayed({
            mBinding.dialogOk.setBackgroundColor(Color.WHITE)
            builder.dismiss()
        }, 50)

    }

    mBinding.dialogCancel.visibility = View.GONE
    mBinding.seperationView.visibility = View.GONE

    builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
    builder.setContentView(mBinding.root)
    builder.setCancelable(false)

    builder.setCanceledOnTouchOutside(false)
    builder.show()
}

val loadingEnableDisable = MutableLiveData<Boolean>(false)
fun Fragment.loadingDialog() {
    // findNavController().navigate(R.id.otp_Fragment)
    val mBinding = LoadingDialogBinding.inflate(LayoutInflater.from(requireActivity()))
    val builder = Dialog(requireActivity())
    loadImageFromUrl(mBinding.progressLoadingImg, 0)
    loadingEnableDisable.observe(viewLifecycleOwner) {
        if (it) {
            builder.show()

        } else {
            builder.dismiss()
        }
    }
    builder.window?.setBackgroundDrawableResource(android.R.color.transparent)
    builder.setContentView(mBinding.root)
    builder.setCancelable(false)
    builder.setCanceledOnTouchOutside(false)
    builder.show()
}
/*val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
    AlertDialog.Builder( activity,android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
} else {
    AlertDialog.Builder( activity, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
}

builder.setTitle("$title")
val message =
    SpannableString("$mmessage")
message.setSpan(
    ForegroundColorSpan(Color.BLACK),
    0,
    message.length,
    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
)
val m_yes =
    SpannableString("OK")
m_yes.setSpan(
    ForegroundColorSpan(Color.BLACK),
    0,
    m_yes.length,
    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
)
builder.setMessage("$message")
builder.setPositiveButton(m_yes){dialog, which ->
    dialog.cancel()
}


// Set other dialog properties
val alertDialog: AlertDialog = builder.create()
alertDialog.setCancelable(false)
alertDialog.show()*/
