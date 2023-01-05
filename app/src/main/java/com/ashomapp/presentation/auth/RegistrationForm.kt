package com.ashomapp.presentation.auth

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
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
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentRegistrationFormBinding
import com.ashomapp.network.repository.AuthRepository
import com.ashomapp.network.repository.ForumRepository
import com.ashomapp.network.response.Countrycodelist
import com.ashomapp.network.retrofit.ResultWrapper
import com.ashomapp.presentation.home.HomeFrag
import com.ashomapp.utils.*
import com.bumptech.glide.Glide
import com.dbvertex.myashomapp.network.repository.HomeRepository
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.squareup.moshi.Json
import kotlinx.coroutines.withContext

import org.json.JSONObject
import org.json.JSONTokener
import java.io.OutputStreamWriter
import java.lang.IllegalStateException
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection


class RegistrationForm : Fragment(), onCountryCodeClick {
    private lateinit var mBinding: FragmentRegistrationFormBinding
    private val mAuthViewModel by activityViewModels<AuthViewModel>()
    private lateinit var bottomSheet: BottomSheetDialog
    private val countryCodeList = ArrayList<Countrycodelist>()

    lateinit var linkedinAuthURLFull: String
    lateinit var linkedIndialog: BottomSheetDialog
    lateinit var linkedinCode: String

    val RC_SIGN_IN = 111
    var navigate = false
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    val logintoRegTransitonBool = MutableLiveData<Boolean>(false)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentRegistrationFormBinding.inflate(layoutInflater, container, false).apply {
            lifecycleOwner = this@RegistrationForm
            viewmodel = mAuthViewModel
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadingDialog()
        bindProgressButton(mBinding.loginSubmit)


        Glide.with(AshomAppApplication.instance.applicationContext)
            .load(R.raw.progress_gif)

            .into(mBinding.progressImg)

        mBinding.signupFormCon.setOnClickListener {
            hideKeyboard(requireContext(),it)
        }
        setetanimaton()
        setupGoogleLogin()
        countryCodeList.clear()
        countrycodelist.forEach {
            countryCodeList.add(it)
            if (it.country_code.equals("971")) {
                Glide.with(AshomAppApplication.instance.applicationContext)
                    .load("https://countryflagsapi.com/png/${it.country_name}")
                    .into(mBinding.countFlag)
                mBinding.countCode.text = "+${it.country_code}"
                mAuthViewModel.country_code.value = "${it.country_code}"
            }
        }
        try {
            MainActivity.layoutheight.observe(viewLifecycleOwner) { heightDifference ->
                if (heightDifference > 300) {
                    val h2 = mBinding.loginScrool.height + 280
                    Log.d("Keyboardsize", "$heightDifference , $h2")
                    mBinding.loginviewdsd.minimumHeight = h2
                } else {
                    val h2 = mBinding.loginScrool.height - 280
                    Log.d("Keyboardsize", "$heightDifference, $h2")
                    mBinding.loginviewdsd.minimumHeight = h2
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        mAuthViewModel.email.value = ""
        mAuthViewModel.password.value = ""
        mBinding.loginSubmit.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            setanimation(it)
            mAuthViewModel.social_id.value = ""
            mAuthViewModel.logintype.value = "normal"
            registration()


        }

        mBinding.loginToSignup.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)
            setanimation(it)
            val navOptions =
                NavOptions.Builder().setPopUpTo(R.id.registrationForm, true).build()
            findNavController().navigate(
                R.id.action_registrationForm_to_loginFragment,
                null,
                navOptions
            )


        }

        /*mAuthViewModel.country_code.value = "${mBinding.ccp.selectedCountryCodeAsInt}"
       mBinding.ccp.setOnCountryChangeListener(object : CountryCodePicker.OnCountryChangeListener{
           override fun onCountrySelected() {
              // temp_showToast(" fsdf  ${mBinding.ccp.selectedCountryCodeAsInt}")

               mAuthViewModel.country_code.value = "${mBinding.ccp.selectedCountryCodeAsInt}"
           }

       })*/
        mBinding.ccp.setOnClickListener {
            countrycodeBottomSheet()
        }

        val state = "linkedin" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

        linkedinAuthURLFull =
            AUTHURL + "?response_type=code&client_id=" + LINKEDN_APIKEY + "&scope=" + SCOPE + "&state=" + state + "&redirect_uri=" + LINKEDN_REDIRECTURI

        mBinding.signupwithlinkdn.setOnClickListener {
            loadingEnableDisable.value = true
            setanimation(it)
            mAuthViewModel.logintype.value = "linkedin"
            setupLinkedinWebviewDialog(linkedinAuthURLFull)
            mAuthViewModel.phone.value = ""
        }

        mBinding.googleSigin.setOnClickListener {
            loadingEnableDisable.value = true
            setanimation(it)
            mAuthViewModel.logintype.value = "google"
            signWithGoogleIn()
            mAuthViewModel.phone.value = ""
        }



        mAuthViewModel.registerNetworkState.observe(viewLifecycleOwner) { state ->
            when (state) {
                NetworkState.LOADING_STARTED -> {
                    hideKeyboard(requireContext(), mBinding.loginSubmit)
                    loadingEnableDisable.value = true
                    mBinding.progressImg.visibility = View.VISIBLE
                    mBinding.loginSubmit.apply {
                        showProgress()
                        isEnabled = false
                    }

                }
                NetworkState.LOADING_STOPPED -> {
                    mBinding.progressImg.visibility = View.GONE
                    loadingEnableDisable.value = false
                    mBinding.loginSubmit.apply {
                        hideProgress("SIGN UP")
                        isEnabled = true
                    }

                }
                NetworkState.SUCCESS -> {
                    mBinding.progressImg.visibility = View.GONE
                    mBinding.loginSubmit.apply {
                        hideProgress("SIGN UP")
                        isEnabled = true
                    }
                    // findNavController().navigate(R.id.otp_Fragment)

                    if (mAuthViewModel.logintype.value.equals("normal")) {
                        if (navigate) {
                            navigate = false
                            val navOptions =
                                NavOptions.Builder().setPopUpTo(R.id.registrationForm, true).build()
                            findNavController().navigate(
                                R.id.action_registrationForm_to_otpFragment,
                                null,
                                navOptions
                            )
                            mAuthViewModel.loginNetworkState.removeObservers(viewLifecycleOwner)

                        }
                    }


                }

                NetworkState.FAILED -> {
                    SharedPrefrenceHelper.token = ""
                    loadingEnableDisable.value = false
                    mBinding.loginSubmit.apply {
                        hideProgress("SIGN UP")
                        isEnabled = true
                    }
                    mAuthViewModel.loginError.observe(viewLifecycleOwner) {
                        if (!it.isNullOrEmpty()) {
                            successDialog(requireActivity(), "Alert!", it)
                            mAuthViewModel.loginError.value = ""
                        }
                    }

                }
            }
        }

    }


    private fun setetanimaton() {
        val binding = mBinding
        binding.loginEmail.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.loginEmailView.setBackgroundColor(resources.getColor(R.color.app_color))
            } else {
                binding.loginEmailView.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }
        binding.loginPassword.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.loginPasswordView.setBackgroundColor(resources.getColor(R.color.app_color))
            } else {
                binding.loginPasswordView.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }

        binding.loginFirstName.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.loginFirstnameView.setBackgroundColor(resources.getColor(R.color.app_color))
            } else {
                binding.loginFirstnameView.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }
        binding.loginLastname.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.loginLastnameView.setBackgroundColor(resources.getColor(R.color.app_color))
            } else {
                binding.loginLastnameView.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }
        binding.loginPhone.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.loginPhoneView.setBackgroundColor(resources.getColor(R.color.app_color))
            } else {
                binding.loginPhoneView.setBackgroundColor(resources.getColor(R.color.grey))
            }
        }


    }

    private fun registration() {
        val fisrtname = mBinding.loginFirstName.text
        val lastname = mBinding.loginLastname.text
        val emailorphone = mBinding.loginEmail.text
        val password = mBinding.loginPassword.text
        val phone = mBinding.loginPhone.text

        if (fisrtname.isNullOrEmpty()) {
            mBinding.loginFirstnameView.setBackgroundColor(resources.getColor(R.color.red))

            mBinding.loginFirstName.setError("Enter first name")
        } else if (lastname.isNullOrEmpty()) {
            mBinding.loginLastnameView.setBackgroundColor(resources.getColor(R.color.red))
            mBinding.loginLastname.setError("Enter last name")

        } else if (emailorphone.isNullOrEmpty() || !isValidEmail(emailorphone.toString())) {
            mBinding.loginEmailView.setBackgroundColor(resources.getColor(R.color.red))
            mBinding.loginEmail.setError("Enter valid email")

        } else if (phone.isNullOrEmpty()) {
            mBinding.loginPhoneView.setBackgroundColor(resources.getColor(R.color.red))
            mBinding.loginPhone.setError("Enter phone no.")
        } else if (mAuthViewModel.country_code.value.isNullOrEmpty()) {
            mBinding.loginPhoneView.setBackgroundColor(resources.getColor(R.color.red))
            mBinding.loginPhone.setError("Select country code")
        } else if (password.isNullOrEmpty()) {
            mBinding.loginPasswordView.setBackgroundColor(resources.getColor(R.color.red))
            mBinding.loginPassword.setError("Enter password")

        } else if (mAuthViewModel.country_code.value.isNullOrEmpty()) {
            temp_showToast("select country code")
        } else {
            loadingEnableDisable.value = true
            mAuthViewModel.email.value = emailorphone.toString()
            mAuthViewModel.phone.value = phone.toString()
            mAuthViewModel.firstname.value = fisrtname.toString()
            mAuthViewModel.lastname.value = lastname.toString()
            mAuthViewModel.password.value = password.toString()
            navigate = true
            Handler().postDelayed({
                mAuthViewModel.registered()
            }, 1000)
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
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, sho updateUI(account)w authenticated UI.
              if (account != null){
                  if (!account.givenName.isNullOrEmpty() && !account.familyName.isNullOrEmpty()){
                      mAuthViewModel.firstname.value = "${account.givenName}"
                      mAuthViewModel.lastname.value = "${account.familyName}"
                      mAuthViewModel.email.value = "${account.email}"
                      mAuthViewModel.social_id.value = "${account.id}"
                      SharedPrefrenceHelper.token = "${account.id}"
                      MainActivity.usertoken.value = "${account.id}"
                      HomeRepository.token = "${account.id}"
                      ForumRepository.mtoken = "${account.id}"
                      HomeFrag.userToken.value = "${account.id}"
                      Handler().postDelayed({
                          LoginWithSocial(account.id.toString())
                      }, 100)

                  }else{
                      temp_showToast("Something went wrong. Please try again later.")
                      loadingEnableDisable.value = false
                  }
              }
              else{
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
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Googlecred", "signInResult:failed code=" + e.statusCode)

        }
    }

    private fun countrycodeBottomSheet() {
        bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.countrycode_bottosheet_layout, null)
        val btnClose = view.findViewById<ImageView>(R.id.country_code_cancel)
        val viewCountryrcode = view.findViewById<CardView>(R.id.country_code_view)
        val recyclerview = view.findViewById<RecyclerView>(R.id.countrycodeList_rv)
        val et_search = view.findViewById<EditText>(R.id.countrycode_list_search)

        val adapter = CountryCodeAdapter(countryCodeList, this)
        et_search.doAfterTextChanged {
            adapter.filter.filter(it.toString())
        }
        btnClose.setOnClickListener {
            ObjectAnimator.ofFloat(viewCountryrcode, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        bottomSheet.dismiss()
                    }
                }
        }

        recyclerview.adapter = adapter
        adapter.notifyDataSetChanged()

        bottomSheet.setContentView(view)

        bottomSheet.show()
    }

    override fun onCountryCodeClick(countrycodelist: Countrycodelist) {
        bottomSheet.dismiss()
        mBinding.countFlag
        mAuthViewModel.country_code.value = "${countrycodelist.country_code}"
        if (countrycodelist.country_name.equals("Bahamas")) {
            mBinding.countFlag.setImageResource(R.drawable.bahamas)
        } else if (countrycodelist.country_name.equals("British Indian Ocean Territory")) {
            mBinding.countFlag.setImageResource(R.drawable.british_indian_ocean_territory)
        } else if (countrycodelist.country_name.equals("Cape Verde")) {
            mBinding.countFlag.setImageResource(R.drawable.cape_verde)
        } else if (countrycodelist.country_name.equals("Cayman Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_cayman_islands)
        } else if (countrycodelist.country_name.equals("Central African Republic")) {
            mBinding.countFlag.setImageResource(R.drawable.central_africal_republic)
        } else if (countrycodelist.country_name.equals("Cocos (Keeling) Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.coco_island)
        } else if (countrycodelist.country_name.equals("Comoros")) {
            mBinding.countFlag.setImageResource(R.drawable.comoros)
        } else if (countrycodelist.country_name.equals("Congo, Democratic Republic of the")) {
            mBinding.countFlag.setImageResource(R.drawable.congo)
        } else if (countrycodelist.country_name.equals("Cook Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_cook_islands)
        } else if (countrycodelist.country_name.equals("Curacao")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_curacao)
        } else if (countrycodelist.country_name.equals("Czech Republic")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_czech_republic)
        } else if (countrycodelist.country_name.equals("Falkland Islands (Malvinas)")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_falkland_islands)
        }//Falkland Islands (Malvinas)  Czech Republic
        else if (countrycodelist.country_name.equals("Faroe Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_faroe_islands)
        }//Falkland Islands (Malvinas)
        else if (countrycodelist.country_name.equals("Holy See (Vatican City State)")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_vatican_city)
        }//Falkland Islands (Malvinas)
        else if (countrycodelist.country_name.equals("Macedonia")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_macedonia)
        }//Marshall Islands
        else if (countrycodelist.country_name.equals("Marshall Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_marshall_islands)
        } else if (countrycodelist.country_name.equals("Netherlands Antilles")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_netherlands_antilles)
        } else if (countrycodelist.country_name.equals("Northern Mariana Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_northern_mariana_islands)
        } else if (countrycodelist.country_name.equals("Palestinian Territory, Occupied")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_palestine)
        } else if (countrycodelist.country_name.equals("Saint Vincent and the Grenadines")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_saint_vicent_and_the_grenadines)
        }//Swaziland Turks and Caicos Islands
        else if (countrycodelist.country_name.equals("Swaziland")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_swaziland)
        } else if (countrycodelist.country_name.equals("Virgin Islands, US")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_us_virgin_islands)
        } else if (countrycodelist.country_name.equals("Turks and Caicos Islands")) {
            mBinding.countFlag.setImageResource(R.drawable.flag_turks_and_caicos_islands)
        } else {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load("https://countryflagsapi.com/png/${countrycodelist.country_name}")
                .into(mBinding.countFlag)
        }

        mBinding.countCode.text = "+${countrycodelist.country_code}"
        Log.d("countrycode", "Selected Country : ${countrycodelist.country_code}")
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupLinkedinWebviewDialog(url: String) {
        linkedIndialog = BottomSheetDialog(requireActivity(), R.style.CustomBottomSheetDialog)
        val webView = WebView(requireActivity())
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient = LinkedInWebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
        linkedIndialog.setContentView(webView)
        linkedIndialog.show()
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
                fetchlinkedInUserProfile(accessToken)
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
                fetchLinkedInEmailAddress(token,linkedinprofiledata.id)


            }
        }
    }

    fun fetchLinkedInEmailAddress(token: String, id: String) {
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

                SharedPrefrenceHelper.token = token
                HomeFrag.userToken.value = token
                HomeRepository.token = token
                ForumRepository.mtoken = token
                val linkedinprofiledata = Gson().fromJson(response, LinkedInEmailModel::class.java)
                val linkedinEmail = linkedinprofiledata.elements[0].handle.emailAddress
                mAuthViewModel.email.value = "$linkedinEmail"

                LoginWithSocial(id)

                Log.d("LinkedIn Email: ", linkedinEmail)
            }
        }
    }

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
                                R.id.action_registrationForm_to_homeFrag,
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
                                R.id.action_registrationForm_to_homeFrag,
                                args,
                                navOptions
                            )
                        }, 1000)


                    }
                }
                is ResultWrapper.Failure -> {

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