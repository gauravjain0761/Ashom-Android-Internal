package com.ashomapp.presentation.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.ashomapp.MainActivity
import com.ashomapp.R
import com.ashomapp.presentation.home.country.CountryList
import com.ashomapp.presentation.news.NewsFrag

class HomeMainContainer : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    companion object{
        val replacefragment = MutableLiveData<Int>(0)
        var hometoCompanyBundle = Bundle()
        var currentFrag : Fragment = HomeFrag()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_main_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }





    override fun onPause() {
        super.onPause()
    }






}