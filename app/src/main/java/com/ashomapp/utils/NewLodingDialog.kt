package com.ashomapp.utils

import android.app.Dialog
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.ashomapp.databinding.LoadingDialogBinding

object NewLodingDialog {
    var builder : Dialog? = null //obj

    fun Fragment.shownewsloading(){
        val mBinding = LoadingDialogBinding.inflate(LayoutInflater.from(requireActivity()))
        builder = Dialog(requireActivity())
        loadImageFromUrl(mBinding.progressLoadingImg, 0)
        builder!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        builder!!.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

        builder!!.setContentView(mBinding.root)
        builder!!.show()
    }

    fun Fragment.hidenewloading(){
        try {
            if (builder != null) {
                builder!!.dismiss()
            }
        } catch (e: Exception) {
        }
    }
}