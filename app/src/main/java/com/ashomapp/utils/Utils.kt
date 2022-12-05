package com.ashomapp.utils

import android.accounts.Account
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Handler
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import com.ashomapp.AshomAppApplication
import com.ashomapp.R
import com.ashomapp.network.response.AppDetailDTO
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.people.v1.PeopleService
import com.snov.timeagolibrary.PrettyTimeAgo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


fun setanimation(view : View){
    val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f)
    val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f)
    val rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 0f, 0f)
    AnimatorSet().apply {
        duration = 1000
        interpolator = OvershootInterpolator()
        playTogether(scaleXAnimator, scaleYAnimator, rotationAnimator)
        start()
    }
}
fun focus(view : View){
    val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1.2f)
    val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1.2f)
    val rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 0f, 0f)

    AnimatorSet().apply {
        duration = 1000
        interpolator = OvershootInterpolator()
        playTogether(scaleXAnimator, scaleYAnimator, rotationAnimator)
        start()
    }

}

fun unfocus(view : View){
    val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0f, 0f, 0f)
    val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0f, 0f, 0f)
    val rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 0f, 0f)
    AnimatorSet().apply {
        duration = 1500
        interpolator = OvershootInterpolator()
        playTogether(scaleXAnimator, scaleYAnimator, rotationAnimator)
        start()
    }
}

fun tvfocus(view : View){
    view.visibility = View.VISIBLE
    val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f, 1f)
    val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f, 1f)
    val rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 0f, 0f)

    AnimatorSet().apply {
        duration = 400
        interpolator = OvershootInterpolator()
        playTogether(scaleXAnimator, scaleYAnimator, rotationAnimator)
        start()
    }

    val animation = AnimationUtils.loadAnimation(
        AshomAppApplication.instance.applicationContext, R.anim.slide_up)

    view.startAnimation(animation)


}

fun tvunfocus(view : View){
   /* val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0f, 0f, 0f)
    val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0f, 0f, 0f)
    val rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 0f, 0f)
    AnimatorSet().apply {
        duration = 1000
        interpolator = OvershootInterpolator()
        playTogether(scaleXAnimator, scaleYAnimator, rotationAnimator)
        start()
    }*/
    val animation = AnimationUtils.loadAnimation(
        AshomAppApplication.instance.applicationContext, R.anim.bottom_slide_down)

     view.startAnimation(animation)
      view.visibility = View.INVISIBLE
}

 fun temp_showToast(text: String) {
    Toast.makeText(
        AshomAppApplication.instance,
        text,
        Toast.LENGTH_LONG
    ).show()
}


fun isValidEmail(email: String): Boolean {
    return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun getPrettyTime(
    oldTime: String,
    timeformat: String,
    flags: Int = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_ABBREV_MONTH,
): String {
    //ex. 2020-12-07 13:00:07 UTC
    if (oldTime.isEmpty()) {
        return "Just Now"
    }
    val date = SimpleDateFormat(timeformat, Locale.getDefault()).parse(oldTime)
    val iSTAddition = 19800000 //+ 5:30 hours in millis



    return DateUtils.getRelativeTimeSpanString(
        date.time ,
        System.currentTimeMillis(),
        0,
        flags
    ).toString()


}
fun getPrettyTimeForForum(
    oldTime: String,
    timeformat: String,
    flags: Int = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_ABBREV_WEEKDAY,
): String {
    //ex. 2020-12-07 13:00:07 UTC
    if (oldTime.isEmpty()) {
        return "Just Now"
    }
    val date = SimpleDateFormat(timeformat, Locale.getDefault()).parse(oldTime)




    return DateUtils.getRelativeTimeSpanString(
        date.time ,
        System.currentTimeMillis(),
        0,
        DateUtils.FORMAT_SHOW_YEAR
    ).toString()
}


@Throws(ParseException::class)
fun formatDateFromDateString(
    inputDateFormat: String?, outputDateFormat: String?,
    inputDate: String?,
): String? {
    Log.d("expire_date_u", "$inputDate")
    val mParsedDate: Date
    val mOutputDateString: String
    val mInputDateFormat = SimpleDateFormat(inputDateFormat, Locale.getDefault())
    val mOutputDateFormat = SimpleDateFormat(outputDateFormat, Locale.getDefault())
    mParsedDate = mInputDateFormat.parse(inputDate)
    mOutputDateString = mOutputDateFormat.format(mParsedDate)
    Log.d("expire_date_u", "$mOutputDateString, $mInputDateFormat, $mOutputDateFormat")
    return mOutputDateString
}
fun getSignInOptions(): GoogleSignInOptions =
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestIdToken(WEB_CLIENT_ID)
        .build()

/**
 *Creates instance of People Service
 *
 * PeopleService can be used for retrieving logged in user's data from the Google People Api.
 */
fun getPeopleService(context: Context, email: String): PeopleService {
    val googleCredential = GoogleAccountCredential.usingOAuth2(
        context, listOf(Scopes.PROFILE, DOB_SCOPE)
    ).also {
        it.selectedAccount = Account(email, "com.google")
    }
    val httpTransport = AndroidHttp.newCompatibleTransport()
    val jsonFactory = JacksonFactory.getDefaultInstance()
    return PeopleService.Builder(
        httpTransport,
        jsonFactory,
        googleCredential
    ).setApplicationName("Ashom.app")
        .build()
}

 fun setupFullHeight(bottomSheetDialog: BottomSheetDialog, context: Context) {
    val bottomSheet =
        bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
    val behavior = BottomSheetBehavior.from(bottomSheet!!)
    val layoutParams = bottomSheet.layoutParams
    val windowHeight = getWindowHeight(context)
    if (layoutParams != null) {
        layoutParams.height = windowHeight - 50
    }
    bottomSheet.layoutParams = layoutParams
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
}

 fun getWindowHeight(context: Context): Int {
    // Calculate window height for fullscreen use
    val displayMetrics = DisplayMetrics()
    (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

fun fetchVersionCode() : AppDetailDTO{
    var dto = AppDetailDTO("", 1,"")
    try {
        val pInfo: PackageInfo =
            AshomAppApplication.instance.applicationContext.getPackageManager().getPackageInfo(AshomAppApplication.instance.applicationContext.getPackageName(), 0)
        val version = pInfo.versionName
        dto =  AppDetailDTO(pInfo.versionName, pInfo.versionCode, pInfo.packageName)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return  dto
}
fun loginViewAnimation(view : View, mRelativeLayout  : Float, duration: Long ){

    view.animate()
        .translationY(-mRelativeLayout)
        .alpha(0.0f)
        .setDuration(duration)

    Handler().postDelayed({
      view.visibility = View.GONE
    }, 800)


}
fun RegViewAnimation(view : View,duration: Long){
    view.visibility = View.VISIBLE
    view.animate()
        .translationY(view.scaleY)
        .alpha(1f)
        .setDuration(duration)

}

@BindingAdapter("image_url")
fun loadImageFromUrl(imageView: ImageView, url: Int) {
    Glide.with(imageView).load(R.raw.progress_gif).into(imageView)
}

fun capitalizeString(str: String): String {
    var retStr = str
    try { // We can face index out of bound exception if the string is null
        retStr = str.substring(0, 1).toUpperCase() + str.substring(1)
    } catch (e: Exception) {
    }
    return retStr
}





data class ChartData(
    val xValue: Float,
    val yValue: Float,
)


