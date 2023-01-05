package com.ashomapp.presentation.auth

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.Color
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
import android.view.WindowManager
import androidx.core.animation.doOnEnd
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentForgetPasswordBinding
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.isValidEmail
import com.ashomapp.utils.setanimation
import com.ashomapp.utils.successDialog
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import kotlinx.coroutines.launch


class ForgetPassword : Fragment() {
    private lateinit var mBinding: FragmentForgetPasswordBinding
    private val mAuthViewModel by activityViewModels<AuthViewModel>()
    private var navigate = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentForgetPasswordBinding.inflate(layoutInflater, container, false).apply {
            lifecycleOwner = this@ForgetPassword
            viewmodel = mAuthViewModel
            loading = true
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        bindProgressButton(mBinding.loginSubmit)
        setetanimaton()

        mBinding.loginSubmit.setOnClickListener {
            navigate = true
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            setanimation(it)
            userForgetpass()

        }
        mAuthViewModel.email.value = ""
        mBinding.mainBack.setOnClickListener {
            ObjectAnimator.ofFloat(mBinding.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        findNavController().navigateUp()
                    }
                }
        }
        mAuthViewModel.forgetpassNetworkState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                NetworkState.LOADING_STARTED -> {
                    hideKeyboard(requireContext(), mBinding.loginSubmit)
                    mBinding.loading = false
                    mBinding.loginSubmit.apply {
                        showProgress()
                        isEnabled = false
                    }

                }
                NetworkState.LOADING_STOPPED -> {
                    mBinding.loading = true
                    mBinding.loginSubmit.apply {
                        hideProgress("Continue")
                        isEnabled = true
                    }

                }
                NetworkState.SUCCESS -> {
                    mBinding.loading = true
                    mBinding.loginSubmit.apply {
                        hideProgress("Continue")
                        isEnabled = true
                    }
                    mAuthViewModel.email.value = ""
                    if (navigate) {
                        // findNavController().navigate(R.id.otp_Fragment)
                        navigate = false
                        val builder = AlertDialog.Builder(
                            requireActivity(),
                            android.R.style.Theme_DeviceDefault_Light_Dialog
                        )
                        builder.setTitle("Information")
                        builder.setMessage("Password has been sent on your registered email.\nPlease check and login again.")
                        val m_yes =
                            SpannableString("OK")
                        m_yes.setSpan(
                            ForegroundColorSpan(Color.BLACK),
                            0,
                            m_yes.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        builder.setPositiveButton(m_yes) { dialog, which ->
                            val navOptions =
                                NavOptions.Builder().setPopUpTo(R.id.forgetPassword, true).build()
                            findNavController().navigate(
                                R.id.action_forgetPassword_to_loginFragment,
                                null,
                                navOptions
                            )
                            mAuthViewModel.forgetpassNetworkState.removeObservers(viewLifecycleOwner)
                        }
                        // Set other dialog properties
                        val alertDialog: AlertDialog = builder.create()
                        alertDialog.setCancelable(false)
                        alertDialog.show()
                    }


                }

                NetworkState.FAILED -> {
                    mBinding.loading = true
                    mBinding.loginSubmit.apply {
                        hideProgress("Continue")
                        isEnabled = true
                    }

                    mAuthViewModel.email.value = ""
                    mAuthViewModel.loginError.observe(viewLifecycleOwner) {
                        if (!it.isNullOrEmpty()) {
                            successDialog(requireActivity(), "Alert!", it)
                            mAuthViewModel.loginError.value = ""
                        }
                    }

                }
            }
        })


    }

    private fun setetanimaton() {
        mBinding.loginEmail.setOnFocusChangeListener { view, b ->
            if (b) {
                mBinding.loginEmailView.setBackgroundColor(resources.getColor(R.color.app_color))
            } else {
                mBinding.loginEmailView.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }

    }

    private fun userForgetpass() {
        val email = mAuthViewModel.email.value

        if (email.isNullOrEmpty() || !isValidEmail(email)) {
            mBinding.loginEmail.setError("Enter valid email")
        } else {
            mAuthViewModel.forgetPassword()
        }


    }


}