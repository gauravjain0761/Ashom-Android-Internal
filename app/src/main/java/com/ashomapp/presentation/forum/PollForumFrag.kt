package com.ashomapp.presentation.forum

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.databinding.FragmentPollForumBinding
import com.ashomapp.presentation.auth.NetworkState
import com.ashomapp.utils.hideKeyboard
import com.ashomapp.utils.showKeyboard
import com.ashomapp.utils.temp_showToast
import com.bumptech.glide.Glide
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import android.graphics.drawable.TransitionDrawable
import androidx.activity.OnBackPressedCallback
import com.ashomapp.presentation.home.HomeFlow


class PollForumFrag : Fragment() {

    private lateinit var mBinding: FragmentPollForumBinding
    private val mForumViewmodel by activityViewModels<ForumViewModel>()
    val validitylist = listOf(
        "Select Validity",
        "1 day",
        "3 days",
        "5 days",
        "7 days"
    )
    private var navigate = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentPollForumBinding.inflate(layoutInflater, container, false).apply {
            viewmodel = mForumViewmodel
            yearselect = false
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindProgressButton(mBinding.postForum)
        mForumViewmodel.getUserdata()
        setEditTextAnimation()
        mForumViewmodel.profileImage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Glide.with(AshomAppApplication.instance.applicationContext).load(it)
                    .into(mBinding.forumProfilePic)
                mBinding.username.text = mForumViewmodel.username.value
            }
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mForumViewmodel.pollQuestion.value = ""
                mForumViewmodel.option1.value = ""
                mForumViewmodel.option2.value = ""
                mForumViewmodel.option3.value = ""
                mForumViewmodel.validity.value = ""
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this.viewLifecycleOwner, onBackPressedCallback)

        mBinding.forumAddCancel.setOnClickListener {
            ObjectAnimator.ofFloat(mBinding.mainBackView, "alpha", 0f, 1f, 0f)
                .apply {
                    duration = 100
                    start()
                    doOnEnd {
                        mForumViewmodel.pollQuestion.value = ""
                        mForumViewmodel.option1.value = ""
                        mForumViewmodel.option2.value = ""
                        mForumViewmodel.option3.value = ""
                        mForumViewmodel.validity.value = ""
                        findNavController().navigateUp()
                    }
                }
        }
        mBinding.addBtn.setOnClickListener {
            if (!mBinding.etOption3.isVisible) {
                mBinding.yearselect = true
                mBinding.addBtn.text = "Remove"
            } else {
                mBinding.yearselect = false
                mForumViewmodel.option3.value = ""
                mBinding.etOption3.setText("")
                mBinding.addBtn.text = "Add"
            }
        }
        et_text_counter()
        mForumViewmodel.postPollloading.observe(viewLifecycleOwner) { state ->

            when (state) {
                NetworkState.LOADING_STARTED -> {
                    mBinding.postForum.apply {
                        showProgress()
                        isClickable = false
                    }
                }
                NetworkState.SUCCESS -> {
                    mBinding.postForum.apply {
                        hideProgress("Post")
                        isClickable = true
                    }
                    mForumViewmodel.pollQuestion.value = ""
                    mForumViewmodel.option1.value = ""
                    mForumViewmodel.option2.value = ""
                    mForumViewmodel.option3.value = ""
                    mForumViewmodel.validity.value = ""
                    if (navigate) {
                        HomeFlow.reloadForum = true
                        navigate = false
                        findNavController().navigateUp()

                    }


                }

                else -> {}
            }

        }
        Validitydrowdown()

        mBinding.postForum.setOnClickListener {
            if (mForumViewmodel.pollQuestion.value.isNullOrEmpty()) {
                mBinding.etComposeForum.setError("Enter Title")
            } else if (mForumViewmodel.option1.value.isNullOrEmpty()) {
                mBinding.etOption1.setError("Enter option ")
            } else if (mForumViewmodel.option2.value.isNullOrEmpty()) {
                mBinding.etOption2.setError("Enter option")
            } else if (mForumViewmodel.validity.value.isNullOrEmpty()) {
                temp_showToast("Select validity")
            } else {
                navigate = true
                mForumViewmodel.submitPoll()
            }

        }
    }

    private fun et_text_counter() {
        mBinding.etComposeForum.requestFocus()
        showKeyboard(AshomAppApplication.instance.applicationContext, mBinding.etComposeForum)

        mBinding.etOption1.doOnTextChanged { text, start, before, count ->
            val remianingcount  = 25 - count
            mBinding.option1Counter.text = "$remianingcount/25"
        }
        mBinding.etOption2.doOnTextChanged { text, start, before, count ->
            val remianingcount  = 25 - count
            mBinding.option2Counter.text = "$remianingcount/25"
        }
        mBinding.etOption3.doOnTextChanged { text, start, before, count ->
            val remianingcount  = 25 - count
            mBinding.option3Counter.text = "$remianingcount/25"
        }
    }

    private fun setEditTextAnimation() {
        mBinding.etOption1.setOnFocusChangeListener { view, b ->
            if (b) {
                val background = mBinding.option1View2.background as TransitionDrawable

                background.startTransition(500)
            } else {
                val background = mBinding.option1View2.background as TransitionDrawable
                background.reverseTransition(500)
            }
        }
        mBinding.etOption2.setOnFocusChangeListener { view, b ->
            if (b) {
                val background = mBinding.option2View2.background as TransitionDrawable
                background.startTransition(500)
            } else {
                val background = mBinding.option2View2.background as TransitionDrawable
                background.reverseTransition(500)
            }
        }
        mBinding.etOption3.setOnFocusChangeListener { view, b ->
            if (b) {
                val background = mBinding.option3View2.background as TransitionDrawable
                background.startTransition(500)
            } else {
                val background = mBinding.option3View2.background as TransitionDrawable
                background.reverseTransition(500)
            }
        }
    }

    fun Validitydrowdown() {
        mBinding.validitySpinner.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                hideKeyboard(requireContext(), mBinding.validitySpinner)
                return false
            }
        })
        val specialisationSpinner = object : ArrayAdapter<String>(
            requireContext(), R.layout.item_spinner, validitylist

        ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val tv: TextView = super.getDropDownView(position, convertView, parent) as TextView
                // set item text size
                //tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,12F)
                // set selected item style
                if (position.toLong() == mBinding.validitySpinner.selectedItemPosition.toLong() && position != 0) {
                    tv.background = ColorDrawable(Color.parseColor("#72c4f1"))
                    tv.setTextColor(Color.parseColor("#ffffff"))

                } else {
                    tv.background = ColorDrawable(Color.parseColor("#ffffff"))
                    tv.setTextColor(Color.parseColor("#000000"))

                }
                return tv
            }
        }

        mBinding.validitySpinner.adapter = specialisationSpinner
        mBinding.validitySpinner.setSelection(validitylist.lastIndex)
        mBinding.validitySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    val conItem = specialisationSpinner.getItem(position)
                    val indexs = validitylist.indexOf(conItem)

                    if (indexs >= 1) {

                        mForumViewmodel.validity.value = "${conItem!!.filter { it.isDigit() }}"
                        //Toast.makeText(requireContext(), conItem!!.filter { it.isDigit() }+"," + indexs, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}


            }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard(AshomAppApplication.instance.applicationContext, mBinding.root)
    }

}