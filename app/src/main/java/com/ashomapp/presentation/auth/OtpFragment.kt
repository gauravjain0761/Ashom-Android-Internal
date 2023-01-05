package com.ashomapp.presentation.auth

import `in`.aabhasjindal.otptextview.OTPListener
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isNotEmpty
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentOtpBinding
import com.ashomapp.network.repository.AuthRepository
import com.ashomapp.network.repository.ForumRepository
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.home.HomeViewModel
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.setanimation
import com.ashomapp.utils.successDialog
import com.ashomapp.utils.temp_showToast
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.poovam.pinedittextfield.PinField
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception
import java.text.DecimalFormat


class OtpFragment : Fragment() {
    private lateinit var mBinding: FragmentOtpBinding
    private val motp = MutableLiveData<String>()
    private val mAuthViewModel by activityViewModels<AuthViewModel>()
    private val mHomeViewModel by activityViewModels<HomeViewModel>()
    private val countdowntimeEnable = MutableLiveData<Boolean>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentOtpBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindProgressButton(mBinding.btnVerify)

        mBinding.otpView.onTextCompleteListener = object : PinField.OnTextCompleteListener {
            override fun onTextComplete(enteredText: String): Boolean {
                motp.value = enteredText
                return true
            }

        }
        countdowntimeEnable.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.otpResend.isClickable = false
                val timer = object : CountDownTimer(120000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val f = DecimalFormat("00")
                        val hour = millisUntilFinished / 3600000 % 24
                        val min = millisUntilFinished / 60000 % 60
                        val sec = millisUntilFinished / 1000 % 60

                        mBinding.otpResend.text = f.format(min) + ":" + f.format(sec)
                        countdowntimeEnable.value = false
                    }

                    override fun onFinish() {
                        mBinding.otpResend.isClickable = true
                        mBinding.otpResend.text = "Resend OTP"
                    }
                }
                timer.start()
            } else {

            }
        }

        try {
            val args = requireArguments().getString("resendotp")
            if (!args.isNullOrEmpty()) {
                if (!mAuthViewModel.phone.value.isNullOrEmpty()) {
                    resendCode()
                }
            }
        } catch (e: Exception) {
            Log.d("error", e.message.toString())
        }
        mBinding.otpResend.setOnClickListener {

            if (!mAuthViewModel.phone.value.isNullOrEmpty()) {
                resendCode()
            }

        }

        mAuthViewModel.phone.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                mBinding.otpSubTitle.text =
                    "Enter the OTP sent to your mobile number +${mAuthViewModel.country_code.value}-${mAuthViewModel.phone.value} and email ${mAuthViewModel.email.value}"
            }
        }
        mBinding.btnVerify.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            setanimation(it)

            if (!motp.value.isNullOrEmpty()) {
                mBinding.btnVerify.apply {
                    showProgress()
                    isClickable = false
                }
                Verfiy_Code()
            }
        }
    }

    fun Verfiy_Code() {
        lifecycleScope.launch {
            val result = AuthRepository.verify_OTP(
                "${mAuthViewModel.phone.value}",
                motp.value.toString()
            )
            when (result) {
                is ResultWrapper.Success -> {

                    if (result.response.status) {
                        mHomeViewModel.updateshareprefrenceValue(result.response.token)
                        Handler().postDelayed({
                            mBinding.btnVerify.apply {
                                hideProgress("VERIFY")
                                isClickable = true
                            }
                            Log.d("usertoken", SharedPrefrenceHelper.token.toString())
                            val navOptions =
                                NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
                            val args = Bundle()
                            args.putString("logincome", "login")
                            findNavController().navigate(
                                R.id.action_otpFragment_to_homeFrag,
                                args,
                                navOptions
                            )

                        }, 1500)

                    } else {
                        temp_showToast("Some thing went wrong")
                    }
                }
                is ResultWrapper.Failure -> {
                    mBinding.btnVerify.apply {
                        hideProgress("VERIFY")
                        isClickable = true
                    }
                    val obj=JSONObject("${result.errorMessage}")
                    val msg=obj.getString("message")
                    if (msg!=null){
                        temp_showToast("${msg}")
                    }else{
                        temp_showToast("Something went wrong.")
                    }

                }
            }
        }
    }


    fun resendCode() {
        lifecycleScope.launch {
            val result = AuthRepository.resendOTP("${mAuthViewModel.phone.value}")
            countdowntimeEnable.value = true
            when (result) {
                is ResultWrapper.Success -> {
                    successDialog(requireActivity(), "Success", "${result.response.message}")
                }
                is ResultWrapper.Failure -> {

                    if (result.status_code == 400) {
                        try {
                            val josnobject = JSONObject(result.errorMessage.toString())
                            val message = josnobject.getString("message")
                            Log.d("otp_unver", message.toString())
                            successDialog(requireActivity(), "Alert!", "${message}")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                        Log.d("otp", result.errorMessage)
                    }


                }
            }
        }
    }

}