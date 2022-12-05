package com.ashomapp.presentation.forum.subscription


import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.anjlab.android.iab.v3.SkuDetails
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentSubscriptionDialogBinding
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.profile.SettingFrag
import com.ashomapp.utils.PLAYSTORE_LICENCE_KEY
import com.ashomapp.utils.formatDateFromDateString
import com.ashomapp.utils.getPrettyTime
import com.ashomapp.utils.successDialog
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.Year
import java.util.*

class SubscriptionDialog : DialogFragment(), BillingProcessor.IBillingHandler {

    private lateinit var mBinding: FragmentSubscriptionDialogBinding
    lateinit var billingClient: BillingClient
    private lateinit var bp: BillingProcessor
    private lateinit var purchaseInfo: PurchaseInfo

    var monthlyamt = ""
    var yearlyamt = ""
    val skulist = listOf("android.test.purchased")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinding = FragmentSubscriptionDialogBinding.inflate(layoutInflater, container, false).apply {
                frag = this@SubscriptionDialog
            }
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        bindProgressButton(mBinding.subcriptionMonthlyPurchase)
        bindProgressButton(mBinding.subcriptionYearlyPurchase)


        try {
            Log.d("Subscriptiontype", SharedPrefrenceHelper.user.subscription_type)
            val user = SharedPrefrenceHelper.user
            if (user.subscription_type == "Monthly") {
                mBinding.subcriptionFreePurchase.visibility = View.GONE
                mBinding.subcriptionMonthlyPurchase.visibility = View.GONE
            } else {
                mBinding.subcriptionFreePurchase.visibility = View.GONE
                mBinding.subcriptionMonthlyPurchase.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        /*  bp = BillingProcessor(AshomAppApplication.instance.applicationContext, "", this)
          bp.initialize()*/

        try {
            /* val args = arguments?.getString("where")
             if (!args.isNullOrEmpty()){
                 if (args.equals("profile")){
                     mBinding.home.root.visibility = View.GONE
                     mBinding.idProfile.root.visibility = View.VISIBLE
                 }else{
                     mBinding.home.root.visibility = View.VISIBLE
                     mBinding.idProfile.root.visibility = View.GONE
                 }
             }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ///IN app product
        /* val purchaseUpdateListener = PurchasesUpdatedListener{
                 billingResult, purchases ->
         }
         billingClient = BillingClient.newBuilder(AshomAppApplication.instance.applicationContext)
             .setListener(purchaseUpdateListener)
             .enablePendingPurchases().build()*/

        bp = BillingProcessor(requireActivity(),
            PLAYSTORE_LICENCE_KEY,
            this)
        bp.initialize()

        mBinding.subcriptionYearlyPurchase.setOnClickListener {
            // yearlysubscription()
            // onPurchasedClick(it)
            bp.subscribe(requireActivity(), "com.ashom.yearly.subscriptions")
        }
        mBinding.subcriptionMonthlyPurchase.setOnClickListener {
            bp.subscribe(requireActivity(), "com.ashom.monthlly.subscription")
        }

    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


    fun canceldialog() {
        mBinding.cancelDialog.setBackgroundColor(Color.parseColor("#80DDE6EB"))
        Handler().postDelayed({
            mBinding.cancelDialog.setBackgroundColor(Color.TRANSPARENT)
            findNavController().navigateUp()
        }, 50)
    }

    fun monthlysubscription() {
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, 2)
        val year = c.get(Calendar.YEAR)
        var month = c.get(Calendar.MONTH)

        val day = if (c.get(Calendar.DAY_OF_MONTH) > 9) {
            c.get(Calendar.DAY_OF_MONTH)
        } else {
            "0${c.get(Calendar.DAY_OF_MONTH)}"
        }
        val mmonth = if (month > 9) {
            month
        } else {
            "0${month}"
        }


        //    val date = formatDateFromDateString("YYYY-mm-dd","YYYY-MM-DD","$year-$month-$day")
        submitSubscription("$year-$mmonth-$day",
            "Monthly",
            "${monthlyamt.filter { it.isDigit() || it == '.' }}")
        Log.d("expire_date", "$year-$mmonth-$day")
    }

    fun freesubscription() {
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, 2)
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)

        val day = if (c.get(Calendar.DAY_OF_MONTH) > 9) {
            c.get(Calendar.DAY_OF_MONTH)
        } else {
            "0${c.get(Calendar.DAY_OF_MONTH)}"
        }
        val mmonth = if (month > 9) {
            month
        } else {
            "0${month}"
        }


        //    val date = formatDateFromDateString("YYYY-mm-dd","YYYY-MM-DD","$year-$month-$day")
        submitSubscription("$year-$mmonth-$day", "Free", "0")
        Log.d("expire_date", "$year-$mmonth-$day")
    }

    fun yearlysubscription() {
        val c = Calendar.getInstance()
        // c.add(Calendar.MONTH, 1)
        c.add(Calendar.YEAR, 1)

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
        val mmonth = if (month > 9) {
            month
        } else {
            "0${month}"
        }
        val day = if (c.get(Calendar.DAY_OF_MONTH) > 9) {
            c.get(Calendar.DAY_OF_MONTH)
        } else {
            "0${c.get(Calendar.DAY_OF_MONTH)}"
        }


        Log.d("expire_date", "$year-$mmonth-$day")

        submitSubscription("$year-$mmonth-$day",
            "Yearly",
            "${yearlyamt.filter { it.isDigit() || it == '.' }}")
    }

    fun onPurchasedClick(view: View) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skulist)
                        .setType(BillingClient.SkuType.INAPP)
                    billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailList ->

                        for (skudetaillist in skuDetailList!!) {
                            val flowpurchase = BillingFlowParams.newBuilder()
                                .setSkuDetails(skudetaillist)
                                .build()
                            val responsecode =
                                billingClient.launchBillingFlow(requireActivity(), flowpurchase)
                                    .responseCode
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {}

        })
    }

    private fun submitSubscription(expire_date: String, subscription_type: String, amount: String) {
        if (subscription_type.equals("Yearly")) {
            mBinding.subcriptionYearlyPurchase.apply {
                showProgress()
                isClickable = false
            }
        } else {
            mBinding.subcriptionMonthlyPurchase.apply {
                showProgress()
                isClickable = false
            }
        }
        lifecycleScope.launch {
            val result = HomeRepository.postSubscription(expire_date, subscription_type, amount)
            when (result) {
                is ResultWrapper.Success -> {
                    Log.d("subcription", "${result.response.toString()}")
                    if (result.response.status) {
                        successDialog(
                            requireActivity(),
                            "Success",
                            result.response.message.toString()
                        )
                    }
                    if (subscription_type.equals("Yearly")) {
                        SettingFrag.subscriptionType.value = "Yearly"
                        Log.d("subscriptiontypeyy", "${SettingFrag.subscriptionType.value}")
                        SharedPrefrenceHelper.user.subscription_type = "Yearly"
                        mBinding.subcriptionYearlyPurchase.apply {
                            hideProgress("Subscribe")
                            isClickable = true
                        }
                    } else if (subscription_type.equals("Monthly")) {
                        Log.d("subscriptiontypemm", "${SettingFrag.subscriptionType.value}")
                        SettingFrag.subscriptionType.value = "Monthly"
                        SharedPrefrenceHelper.user.subscription_type = "Monthly"
                        mBinding.subcriptionMonthlyPurchase.apply {
                            hideProgress("Subscribe")
                            isClickable = true
                        }
                    } else {
                        SettingFrag.subscriptionType.value = "Free"
                        SharedPrefrenceHelper.user.subscription_type = "Free"
                        mBinding.subcriptionMonthlyPurchase.apply {
                            hideProgress("Subscribe")
                            isClickable = true
                        }
                    }
                    findNavController().navigateUp()
                }
                is ResultWrapper.Failure -> {
                    Log.d("subcription", "${result.errorMessage.toString()}")

                    if (subscription_type.equals("Yearly")) {
                        mBinding.subcriptionYearlyPurchase.apply {
                            hideProgress("Subscribe")
                            isClickable = true
                        }
                    } else {
                        mBinding.subcriptionMonthlyPurchase.apply {
                            hideProgress("Subscribe")
                            isClickable = true
                        }
                    }
                }
            }
        }

    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        if (productId == "com.ashom.monthlly.subscription") {
            monthlysubscription()
        } else if (productId == "com.ashom.yearly.subscriptions") {
            yearlysubscription()
        }

    }

    override fun onPurchaseHistoryRestored() {}

    override fun onBillingError(errorCode: Int, error: Throwable?) {

    }

    override fun onBillingInitialized() {
        val subSku: MutableList<String> = ArrayList()
        subSku.add("com.ashom.yearly.subscriptions")
        subSku.add("com.ashom.monthlly.subscription")
        bp.getSubscriptionsListingDetailsAsync(subSku as ArrayList<String>?,
            object : BillingProcessor.ISkuDetailsResponseListener {
                override fun onSkuDetailsResponse(products: MutableList<SkuDetails>?) {
                    Log.d("subscriptionerr11", products!!.map { it.priceText }.toString())
                    try {
                        Log.d("subscriptionerr11", products!!.map { it.productId }.toString())

                        monthlyamt = "${products.map { it.priceText }[0]}"
                        yearlyamt = "${products.map { it.priceText }[1]}"
                        mBinding.subsMonthlyTimeperiod.text =
                            "${products.map { it.priceText }[0]}/ Monthly"
                        mBinding.subsYearlyTimeperiod.text =
                            "${products.map { it.priceText }[1]}/ Yearly"

                        val region = products.map { it.currency }
                        mBinding.subsFreeTimeperiod.text =
                            "${products.map { it.currency }[0]} 0/ Montly"
                        Log.d("currenyashom", region.toString())

                    } catch (e: Exception) {
                        Log.d("subscriptionerr", e.message.toString())
                    }
                }

                override fun onSkuDetailsError(error: String?) {
                    Log.d("subscriptionerr", error.toString())
                }

            })
    }


    override fun onDestroy() {
        super.onDestroy()
        if (bp != null) {
            bp.release()
        }
    }

}