package com.ashomapp.presentation.home

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.core.animation.doOnEnd
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.database.SharedPrefrenceHelper
import com.ashomapp.databinding.FragmentCompanyDocumentViewBinding
import com.ashomapp.network.response.dashboard.CompanyStatementsDTO
import com.ashomapp.network.response.dashboard.RemainingCompanyDTO
import com.ashomapp.utils.ApplyGTMEvent
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.notificationcounter
import com.ashomapp.utils.setanimation
import com.bumptech.glide.Glide
import com.github.barteksc.pdfviewer.PDFView
import com.google.gson.Gson
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class CompanyDocumentView : Fragment() {
    private lateinit var mBinding: FragmentCompanyDocumentViewBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentCompanyDocumentViewBinding.inflate(layoutInflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mBinding.mtoolbar.mainBack.setOnClickListener {
            ObjectAnimator.ofFloat(mBinding.mtoolbar.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        findNavController().navigateUp()
                    }
                }
        }
        mBinding.mtoolbar.notificationBellIcon.setOnClickListener {
            hideKeyboard(AshomAppApplication.instance.applicationContext, it)

            findNavController().navigate(R.id.notificationFrag)
        }
        notificationcounter.observe(this.viewLifecycleOwner) {
            if (it != 0) {
                mBinding.mtoolbar.iconBadges.text = it.toString()
                mBinding.mtoolbar.iconBadges.visibility = View.VISIBLE
            } else {
                mBinding.mtoolbar.iconBadges.visibility = View.GONE
            }
        }
        mBinding.mtoolbar.icon.setOnClickListener {
            setanimation(it)
            if (HomeFlow.sectionBottomID == R.id.searchFrag && HomeFlow.searchcurrentFragID != R.id.searchFrag) {
                (requireActivity() as MainActivity).clicktoSearchFrag()
            } else if (HomeFlow.sectionBottomID == R.id.countryList && HomeFlow.financialcurrentFragID != R.id.countryList) {
                (requireActivity() as MainActivity).clicktoCountryList()
            } else if (HomeFlow.sectionBottomID == R.id.forumFrag && HomeFlow.forumCurrentID != R.id.forumFrag) {
                (requireActivity() as MainActivity).clicktoForumTab()
            } else if (HomeFlow.sectionBottomID == R.id.newsFrag && HomeFlow.newsFragCurrentID != R.id.newsFrag) {
                (requireActivity() as MainActivity).clicktoForumTab()
            } else {
                (requireActivity() as MainActivity).gotoHome()
            }
        }
        mBinding.mtoolbar.toolProfile.setOnClickListener {
            findNavController().navigate(R.id.action_companyDocumentView_to_settingFrag)
        }
        if (!SharedPrefrenceHelper.user.profile_pic.isNullOrEmpty()) {
            Glide.with(AshomAppApplication.instance.applicationContext)
                .load(SharedPrefrenceHelper.user.profile_pic).into(mBinding.mtoolbar.mainProfilePic)

        }
        val args = requireArguments().getString("document_link")
        val webView = mBinding.doucmentWebview
        webView.webViewClient = WebViewClient()
        Log.d("url", "https://docs.google.com/gview?url=$args&embedded=true#:0.page.20")
        if (!args.isNullOrEmpty()) {
            val newsitem = Gson().fromJson(args, CompanyStatementsDTO::class.java)
            mBinding.documentViewTitle.text = "${newsitem.document_name}"
          //by nishant 4-10-22
            if(newsitem.document_name.equals("Financial Report")){
                ApplyGTMEvent("company_report","company_report_count","company_report")
            }

            RetrivePDFfromUrl(mBinding.idPDFView).execute(newsitem.document_link)
            Log.d(
                "url",
                "https://docs.google.com/gview?url=${newsitem.document_link}&embedded=true#:0.page.20"
            )
            //     webView.loadUrl("https://docs.google.com/gview?url=${newsitem.document_link}&embedded=true#:0.page.20")

            // this will enable the javascript settings
            //      webView.settings.javaScriptEnabled = true

            // if you want to enable zoom feature
            //      webView.settings.setSupportZoom(true)

        }
    }

    internal class RetrivePDFfromUrl(pdfView: PDFView) : AsyncTask<String, Void, InputStream>() {
        val innerTextView: PDFView? = pdfView
        override fun doInBackground(vararg strings: String?): InputStream? {
            var inputStream: InputStream? = null
            try {
                val url = URL(strings.get(0))
                // below is the step where we are
                // creating our connection.
                val urlConnection: HttpURLConnection = url.openConnection() as HttpsURLConnection
                if (urlConnection.getResponseCode() === 200) {
                    // response is success.
                    // we are getting input stream from url
                    // and storing it in our variable.
                    inputStream = BufferedInputStream(urlConnection.getInputStream())
                }
            } catch (e: IOException) {
                // this is the method
                // to handle errors.
                e.printStackTrace()
                return null
            }
            return inputStream!!
        }

        override fun onPostExecute(inputStream: InputStream?) {
            innerTextView?.fromStream(inputStream)?.load();
        }

    }

}