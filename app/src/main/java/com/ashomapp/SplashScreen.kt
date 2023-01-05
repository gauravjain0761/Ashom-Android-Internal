package com.ashomapp

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentSplashScreenBinding
import com.ashomapp.utils.temp_showToast
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class SplashScreen : Fragment() {
    private lateinit var mBinding: FragmentSplashScreenBinding
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


        val dshd = "INR 200.00".filter { it.isDigit() || it == '.' }

        Log.d("myvalue", dshd.toString())

       val isPlayStoreUpdated=appInstalledOrNot()
        if (isPlayStoreUpdated==false){
            temp_showToast("Please update play store app.")
        }else{
            Check_New_Version()
        }
       // oldFlow()
        try {
            val info: PackageInfo = requireContext().packageManager.getPackageInfo(
                "com.ashomapp",
                PackageManager.GET_SIGNATURES
            )
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

    private fun appInstalledOrNot(): Boolean {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse("market://search?q=foo")
        val pm = requireActivity().packageManager
        val list = pm.queryIntentActivities(intent, 0)
        return !list.isEmpty()
    }

    private fun Check_New_Version() {


        val appUpdateManager = AppUpdateManagerFactory.create(AshomAppApplication.instance.applicationContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {

               // update_app(requireContext(), requireActivity())
                AlertDialog.Builder(requireContext()).setTitle("Update Available!")
                    .setMessage("Please update your app and start again.")
                    .setPositiveButton("Update", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            //
                            //clearAppData()
                            val PACKAGE_NAME = activity!!.getPackageName();
                            try {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=$PACKAGE_NAME")
                                    )
                                )
                            } catch (e: ActivityNotFoundException) {

                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=${PACKAGE_NAME}")
                                    )
                                )
                            }
                        }

                    })
                    .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            p0!!.dismiss()
                            Toast.makeText(requireContext(),"Please update app and\n start again.",Toast.LENGTH_LONG).show()
                        }

                    }).show()


            } else {


                oldFlow()


            }
        }
    }

    private fun oldFlow() {
        Handler().postDelayed({
            val navOptions =
                NavOptions.Builder().setPopUpTo(R.id.splashScreen, true).build()
            Log.d("usertoken", SharedPrefrenceHelper.token.toString())


            if (SharedPrefrenceHelper.token.equals("0")) {
                findNavController().navigate(
                    R.id.action_splashScreen_to_loginFragment,
                    null,
                    navOptions
                )
            } else {
                if (!MainActivity.intentforNews.value.isNullOrEmpty()) {
                    val args = bundleOf("Newsdetail" to MainActivity.intentforNews.value.toString())
                    findNavController().navigate(R.id.newsDetail, args, navOptions)


                } else if (!MainActivity.intentforForum.value.isNullOrEmpty()) {
                    findNavController().navigate(R.id.forumFrag, null, navOptions)

                } else if (!MainActivity.intentforCompany.value.isNullOrEmpty()) {
                    val args =
                        bundleOf("company_finance_detail" to MainActivity.intentforCompany.value.toString())
                    findNavController().navigate(R.id.financialStatementfrag, args, navOptions)

                } else {
                    findNavController().navigate(R.id.homeFrag, null, navOptions)
                }

            }
        }, 1000)
    }

    private fun clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT) {
               val am =requireActivity().getSystemService(ACTIVITY_SERVICE) as ActivityManager
                  am.clearApplicationUserData() // note: it has a return value!
            } else {
                val packageName = activity!!.getPackageName();
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}