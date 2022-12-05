package com.ashomapp.presentation.forum

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.ashomapp.MainActivity
import com.ashomapp.R


class ForumFragContainer : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         loadFragment(ForumFrag())
    }



    fun loadFragment(fragment : Fragment){

        val fragManager = supportFragmentManager
        fragManager.beginTransaction()
            .add(R.id.forum_frameLayout, fragment)
            .commit()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }


}