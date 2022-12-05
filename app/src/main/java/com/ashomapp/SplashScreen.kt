package com.ashomapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentSplashScreenBinding
import com.ashomapp.presentation.home.HomeViewModel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class SplashScreen : Fragment() {
         private lateinit var mBinding : FragmentSplashScreenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentSplashScreenBinding.inflate(layoutInflater, container, false)

        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
             requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)


        val dshd = "INR 200.00".filter { it.isDigit()  || it == '.' }

        Log.d("myvalue", dshd.toString())

        Handler().postDelayed({
            val navOptions =
                NavOptions.Builder().setPopUpTo(R.id.splashScreen, true).build()
            Log.d("usertoken", SharedPrefrenceHelper.token.toString())

         if (SharedPrefrenceHelper.token.equals("0")){
             findNavController().navigate(R.id.action_splashScreen_to_loginFragment, null, navOptions)
         }else{
             if (!MainActivity.intentforNews.value.isNullOrEmpty()){
                 val args = bundleOf("Newsdetail" to MainActivity.intentforNews.value.toString())
                 findNavController().navigate(R.id.newsDetail, args, navOptions)


             }else if (!MainActivity.intentforForum.value.isNullOrEmpty()){
                 findNavController().navigate(R.id.forumFrag, null, navOptions)

             }else if (!MainActivity.intentforCompany.value.isNullOrEmpty()){
                 val args = bundleOf("company_finance_detail" to MainActivity.intentforCompany.value.toString())
                 findNavController().navigate(R.id.financialStatementfrag, args, navOptions)

             }else{
                 findNavController().navigate(R.id.homeFrag, null, navOptions)
             }

         }
        }, 1000) // 3000 is the delayed time in milliseconds.

        try {
            val info : PackageInfo = requireContext().packageManager.getPackageInfo("com.ashomapp", PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            Log.d("Key", e.message.toString())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            Log.d("Key1", e.message.toString())
        }
    }






}