package com.ashomapp.presentation.auth

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentLoginBinding
import com.ashomapp.network.repository.AuthRepository
import com.ashomapp.network.repository.ForumRepository
import com.ashomapp.network.response.dashboard.LoginData
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.utils.*
import com.bumptech.glide.Glide
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.JSONTokener
import java.io.OutputStreamWriter
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import android.view.ViewTreeObserver

import android.R.string.no
import android.content.Context
import android.graphics.Rect
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.NestedScrollView
import com.ashomapp.presentation.home.HomeFrag
import java.lang.IllegalStateException


class LoginFragment : Fragment() {

    private lateinit var mBinding: FragmentLoginBinding
    var progressBar: ProgressDialog? = null
    private var navigate = false
    private val mAuthViewModel by activityViewModels<AuthViewModel>()
    lateinit var linkedinAuthURLFull: String
    lateinit var linkedIndialog: BottomSheetDialog
    lateinit var linkedinCode: String
    val RC_SIGN_IN = 111
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentLoginBinding.inflate(layoutInflater, container, false).apply {
            lifecycleOwner = this@LoginFragment
            viewmodel = mAuthViewModel

        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        bindProgressButton(mBinding.loginSubmit)
        loadingDialog()  //for loading dialog
        progressBar = ProgressDialog(requireContext())
        setetanimaton()
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button event
                requireActivity().finish()
            }
        }

        try {
            MainActivity.layoutheight.observe(viewLifecycleOwner) { heightDifference ->
                if (heightDifference > 300) {
                    val h2 = mBinding.loginScrool.height + 500
                    Log.d("Keyboardsize", "$heightDifference , $h2")
                    mBinding.loginviewdsd.minimumHeight = h2
                } else {
                    val h2 = mBinding.loginScrool.height - 500
                    Log.d("Keyboardsize", "$heightDifference, $h2")
                    mBinding.loginviewdsd.minimumHeight = h2
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        setupGoogleLogin()
        requireActivity().onBackPressedDispatcher.addCallback(
            this.viewLifecycleOwner,
            onBackPressedCallback
        )

        try {
            val user = SharedPrefrenceHelper.loginINFO
            if (!user.email.isNullOrEmpty() && !user.password.isNullOrEmpty()) {
                mBinding.loginEmail.setText("${user.email}")
                mBinding.loginPassword.setText("${user.password}")
                mBinding.remingMeBtn.isChecked = true
            }
        } catch (e: Exception) {
            Log.d("error", e.localizedMessage)
        }
        mBinding.loginSubmit.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            setanimation(it)
            mAuthViewModel.logintype.value = "normal"
            //added by nj
            ApplyGTMEvent("login_click","login_click_count","login_click")
            userlogin()

        }
        mBinding.loginToSignup.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            setanimation(it)

            findNavController().navigate(R.id.action_loginFragment_to_registrationForm)
        }
        setupGoogleLogin()
        val state = "linkedin" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        linkedinAuthURLFull =
            AUTHURL + "?response_type=code&client_id=" + LINKEDN_APIKEY + "&scope=" + SCOPE + "&state=" + state + "&redirect_uri=" + LINKEDN_REDIRECTURI


        mBinding.loginWithLinkedIn.setOnClickListener {
            mAuthViewModel.logintype.value = "linkedin"
            setanimation(it)
            loadingEnableDisable.value = true
            mAuthViewModel.phone.value = ""
            setupLinkedinWebviewDialog(linkedinAuthURLFull)
        }
        mBinding.loginWithGoogle.setOnClickListener {
            mAuthViewModel.logintype.value = "google"
            loadingEnableDisable.value = true
            mAuthViewModel.phone.value = ""
            setanimation(it)
            signWithGoogleIn()

        }

        Glide.with(AshomAppApplication.instance.applicationContext)
            .load(R.raw.progress_gif)

            .into(mBinding.progressImg)
        mBinding.loginForgetpassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgetPassword)
        }

        mAuthViewModel.loginNetworkState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                NetworkState.LOADING_STARTED -> {
                    hideKeyboard(requireContext(), mBinding.loginSubmit)
                    mBinding.progressImg.visibility = View.VISIBLE
                    mBinding.loginSubmit.apply {
                        showProgress()
                        isEnabled = false
                    }

                }
                NetworkState.LOADING_STOPPED -> {
                    progressBar!!.dismiss()
                    mBinding.progressImg.visibility = View.GONE
                    mBinding.loginSubmit.apply {
                        hideProgress("Login")
                        isEnabled = true
                    }
                    loadingEnableDisable.value = false

                }
                NetworkState.SUCCESS -> {
                    mBinding.progressImg.visibility = View.GONE
                    mBinding.loginSubmit.apply {
                        hideProgress("Login")
                        isEnabled = true
                    }
                    // findNavController().navigate(R.id.otp_Fragment)
                    if (navigate) {
                        mAuthViewModel.loginNetworkState.removeObservers(viewLifecycleOwner)
                        if (mAuthViewModel.otp_verified.value == true) {
                            Handler().postDelayed({
                                val args = Bundle()
                                args.putString("logincome", "login")

                                val navOptions =
                                    NavOptions.Builder().setPopUpTo(R.id.loginFragment, true)
                                        .build()
                                findNavController().navigate(
                                    R.id.action_loginFragment_to_homeFrag,
                                    args,
                                    navOptions
                                )
                            }, 1000)
                            navigate = false
                        }
                    }

                }

                NetworkState.UN_AUTHORIZED -> {
                    mBinding.loginSubmit.apply {
                        hideProgress("Login")
                        isEnabled = true
                    }
                    if (navigate) {

                        val args = bundleOf("resendotp" to "resend")
                        findNavController().navigate(R.id.action_loginFragment_to_otpFragment, args)
                        navigate = false
                    }

                }
                NetworkState.FAILED -> {
                    mBinding.loginSubmit.apply {
                        hideProgress("Login")
                        isEnabled = true
                    }
                    mAuthViewModel.loginError.observe(viewLifecycleOwner) {
                        if (!it.isNullOrEmpty()) {
                            successDialog(requireActivity(), "Alert!", it)
                            mAuthViewModel.loginError.value = ""
                        }
                    }
                    loadingEnableDisable.value = false

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
        mBinding.loginPassword.setOnFocusChangeListener { view, b ->
            if (b) {
                mBinding.loginPasswordView.setBackgroundColor(resources.getColor(R.color.app_color))
            } else {
                mBinding.loginPasswordView.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }
        mBinding.loginSubmit.setOnFocusChangeListener { view, b ->

            if (b) {
                mBinding.loginSubmit.setBackgroundColor(resources.getColor(R.color.grey))
            } else {
                mBinding.loginSubmit.setBackgroundColor(resources.getColor(R.color.btn_color))
            }
        }
    }

    private fun userlogin() {
        val email = mBinding.loginEmail.text.toString()
        val password = mBinding.loginPassword.text.toString()

        if (email.isNullOrEmpty()) {
            mBinding.loginEmail.setError("Enter email")
        } else if (password.isNullOrEmpty()) {
            mBinding.loginPassword.setError("Enter password")
        } else {
            if (mBinding.remingMeBtn.isChecked) {
                SharedPrefrenceHelper.loginINFO = LoginData(
                    email,
                    password
                )
            } else {
                SharedPrefrenceHelper.loginINFO = LoginData(
                    "",
                    ""
                )
            }
            navigate = true
            mAuthViewModel.email.value = email
            mAuthViewModel.password.value = password
            Handler().postDelayed({
                mAuthViewModel.userlogin()
            }, 1000)
            loadingEnableDisable.value = true
        }
    }

    //LinkedIn Login start

    @SuppressLint("SetJavaScriptEnabled")
    fun setupLinkedinWebviewDialog(url: String) {
        linkedIndialog =
            BottomSheetDialog(requireActivity() as MainActivity, R.style.CustomBottomSheetDialog)
        val webView = WebView(requireActivity() as MainActivity)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient = LinkedInWebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
        linkedIndialog.setContentView(webView)
        linkedIndialog.show()

        loadingEnableDisable.value = false
    }

    @Suppress("OverridingDeprecatedMember")
    inner class LinkedInWebViewClient : WebViewClient() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (request?.url.toString().startsWith(LINKEDN_REDIRECTURI)) {
                handleUrl(request?.url.toString())

                // Close the dialog after getting the authorization code
                if (request?.url.toString().contains("?code=")) {
                    linkedIndialog.dismiss()
                }
                return true
            }
            return false
        }

        // For API 19 and below
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith(LINKEDN_REDIRECTURI)) {
                handleUrl(url)

                // Close the dialog after getting the authorization code
                if (url.contains("?code=")) {
                    linkedIndialog.dismiss()
                }
                return true
            }
            return false
        }

        // Check webview url for access token code or error
        private fun handleUrl(url: String) {
            val uri = Uri.parse(url)

            if (url.contains("code")) {
                linkedinCode = uri.getQueryParameter("code") ?: ""
                linkedInRequestForAccessToken()
            } else if (url.contains("error")) {
                val error = uri.getQueryParameter("error") ?: ""
                Log.e("Error: ", error)
            }
        }
    }

    fun linkedInRequestForAccessToken() {
        GlobalScope.launch(Dispatchers.Default) {
            val grantType = "authorization_code"
            val postParams =
                "grant_type=" + grantType + "&code=" + linkedinCode + "&redirect_uri=" + LINKEDN_REDIRECTURI + "&client_id=" + LINKEDN_APIKEY + "&client_secret=" + LINKEDN_SECRETKEY
            val url = URL(TOKENURL)
            val httpsURLConnection =
                withContext(Dispatchers.IO) { url.openConnection() as HttpsURLConnection }
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.setRequestProperty(
                "Content-Type",
                "application/x-www-form-urlencoded"
            )
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true
            val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
            withContext(Dispatchers.IO) {
                outputStreamWriter.write(postParams)
                outputStreamWriter.flush()
            }
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8
            val jsonObject = JSONTokener(response).nextValue() as JSONObject

            val accessToken = jsonObject.getString("access_token") //The access token
            Log.d("accessToken is: ", accessToken)

            val expiresIn = jsonObject.getInt("expires_in") //When the access token expires
            Log.d("expires in: ", expiresIn.toString())


            withContext(Dispatchers.Main) {
                // Get user's id, first name, last name, profile pic url
                fetchlinkedInUserProfile(token = accessToken)
            }
        }
    }

    fun fetchlinkedInUserProfile(token: String) {
        GlobalScope.launch(Dispatchers.Default) {
            val tokenURLFull =
                "https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))&oauth2_access_token=$token"
            val url = URL(tokenURLFull)
            val httpsURLConnection =
                withContext(Dispatchers.IO) { url.openConnection() as HttpsURLConnection }
            httpsURLConnection.requestMethod = "GET"
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = false
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8

            withContext(Dispatchers.Main) {
                val linkedinprofiledata =
                    Gson().fromJson(response, LinkedInProfileModel::class.java)
                Log.d("LinkedIn Access Token: ", token)
                Log.d(
                    "LinkedIn res",
                    "Firstname : ${linkedinprofiledata.firstname} , Lastname : ${linkedinprofiledata.lastname}, ID : ${linkedinprofiledata.id}"
                )
                mAuthViewModel.social_id.value = "${linkedinprofiledata.id.toString()}"
                mAuthViewModel.firstname.value =
                    linkedinprofiledata.firstname.localized.enUS.toString()
                mAuthViewModel.lastname.value =
                    linkedinprofiledata.lastname.localized.enUS.toString()
                fetchLinkedInEmailAddress(token, linkedinprofiledata.id)


            }
        }
    }

    fun fetchLinkedInEmailAddress(token: String, id  :String ) {
        val tokenURLFull =
            "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))&oauth2_access_token=$token"
        GlobalScope.launch(Dispatchers.Default) {
            val url = URL(tokenURLFull)
            val httpsURLConnection =
                withContext(Dispatchers.IO) { url.openConnection() as HttpsURLConnection }
            httpsURLConnection.requestMethod = "GET"
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = false
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8
            Log.d("LinkedInemail", response.toString())

            withContext(Dispatchers.Main) {
                // LinkedIn Email
                val linkedinprofiledata = Gson().fromJson(response, LinkedInEmailModel::class.java)
                val linkedinEmail = linkedinprofiledata.elements[0].handle.emailAddress
                mAuthViewModel.email.value = "$linkedinEmail"
                LoginWithSocial(id)
                Log.d("LinkedIn Email: ", linkedinEmail)
            }
        }
    }

    private fun setupGoogleLogin() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestId()
            .requestProfile()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        val account =
            GoogleSignIn.getLastSignedInAccount(AshomAppApplication.instance.applicationContext)
        try {
            if (!account!!.displayName.isNullOrEmpty()) {
                mGoogleSignInClient.signOut()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun signWithGoogleIn() {
        val signInIntent = mGoogleSignInClient.signInIntent

        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else {
            loadingEnableDisable.value = false
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, sho updateUI(account)w authenticated UI.
            /*mAuthViewModel.firstname.value = "${account.givenName}"
            mAuthViewModel.lastname.value = "${account.familyName}"
            mAuthViewModel.email.value = "${account.email}"
            mAuthViewModel.social_id.value = "${account.id}"*/

            if (account != null) {

                if (!account.givenName.isNullOrEmpty() || !account.familyName.isNullOrEmpty()){
                    mAuthViewModel.firstname.value = "${account.givenName}"
                    mAuthViewModel.lastname.value = "${account.familyName}"
                    mAuthViewModel.email.value = "${account.email}"
                    mAuthViewModel.social_id.value = "${account.id}"
                    Handler().postDelayed({
                        LoginWithSocial(account.id.toString())
                    },100)
                }else{
                    temp_showToast("Something went wrong. Please try again later.")
                    loadingEnableDisable.value = false
                }

            } else {
                loadingEnableDisable.value = false
            }


            Log.d(
                "Googlecred", "Account detail ${account.displayName}, " +
                        "${account.email} ," +
                        " ${account.familyName} ," +
                        " ${account.givenName}"
            )
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            loadingEnableDisable.value = false
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Googlecred", "signInResult:failed code=" + e.statusCode)

        }
    }

    //****** LinkedIn Login End

    private fun LoginWithSocial(token: String) {
        loadingEnableDisable.value = true
        lifecycleScope.launch {
            val result = AuthRepository.getUserData(token)
            when (result) {
                is ResultWrapper.Success -> {
                    if (!result.response.userdata.token.isNullOrEmpty()) {
                        SharedPrefrenceHelper.token = token
                        HomeRepository.token = token
                        ForumRepository.mtoken = token
                        HomeFrag.userToken.value = token
                        Handler().postDelayed({

                            loadingEnableDisable.value = false

                            Log.d("usertoken", "${SharedPrefrenceHelper.token}")
                            val args = Bundle()
                            args.putString("logincome", "login")

                            val navOptions =
                                NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
                            findNavController().navigate(
                                R.id.action_loginFragment_to_homeFrag,
                                args,
                                navOptions
                            )
                        }, 1000)
                    }
                }
                is ResultWrapper.Failure -> {
                    if (result.status_code == 404) {
                        // temp_showToast("User not found.")
                        loadingEnableDisable.value = true

                        RegisterUser(token)
                    }
                    Log.d("errror", result.errorMessage)
                }
            }
        }
    }

    private fun RegisterUser(token: String) {

        lifecycleScope.launch {
            val result = AuthRepository.Registration(
                mAuthViewModel.firstname.value.toString(),
                mAuthViewModel.lastname.value.toString(),
                mAuthViewModel.email.value.toString(),
                "${mAuthViewModel.phone.value}",
                mAuthViewModel.password.value.toString(),
                mAuthViewModel.logintype.value.toString(),
                social_id = mAuthViewModel.social_id.value.toString(),
                countrycode = mAuthViewModel.country_code.value.toString()
            )
            when (result) {
                is ResultWrapper.Success -> {
                    if (result.response.status) {

                        SharedPrefrenceHelper.token = token
                        HomeRepository.token = token
                        ForumRepository.mtoken = token
                        HomeFrag.userToken.value = token
                        Handler().postDelayed({
                            loadingEnableDisable.value = false
                            val args = Bundle()
                            args.putString("logincome", "login")


                            val navOptions =
                                NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
                            findNavController().navigate(
                                R.id.action_loginFragment_to_homeFrag,
                                args,
                                navOptions
                            )
                        }, 1000)


                    }
                }
                is ResultWrapper.Failure -> {
                    Handler().postDelayed({
                        loadingEnableDisable.value = false

                    }, 300)
                    SharedPrefrenceHelper.token = ""
                    try {
                        mGoogleSignInClient.signOut()
                    } catch (e: Exception) {
                        Log.d("error", "eror")
                    }
                    Handler().postDelayed({
                        loadingEnableDisable.value = false

                    }, 300)
                    try {
                        val josnobject = JSONObject(result.errorMessage.toString())

                        val countrycode = "+${josnobject.getString("country_code")}"
                        val phone = "${josnobject.getString("mobile")}"
                        val email = "${josnobject.getString("user_email")}"
                        Log.d(
                            "responseregjson",
                            josnobject.toString() + "${josnobject.getString("message")}"
                        )


                    } catch (e: Exception) {
                        Log.d(
                            "responseregjsonerror",
                            "${result.errorMessage}" + e.localizedMessage.toString()
                        )
                        try {
                            val josnobject = JSONObject(result.errorMessage.toString())
                            val error = josnobject.getString("login_type_error")
                            val email_error = josnobject.getString("email_error")
                            val password_error = josnobject.getString("password_error")
                            //   temp_showToast("${(error)}.")
                            if (!error.isNullOrEmpty()) {
                                successDialog(requireActivity(), "Alert!", error)

                            } else if (!email_error.isNullOrEmpty()) {
                                successDialog(requireActivity(), "Alert!", email_error)
                            } else if (!password_error.isNullOrEmpty()) {
                                successDialog(requireActivity(), "Alert!", password_error)
                            }
                        } catch (e: Exception) {
                            temp_showToast("Something went wrong. ${e.message.toString()}")

                        }


                    }


                }
            }

        }

    }


}