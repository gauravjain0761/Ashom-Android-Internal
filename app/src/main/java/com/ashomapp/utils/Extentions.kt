package com.ashomapp.utils

import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.roundToInt


private const val SECOND_MILLIS = 1000
private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS

private const val WEEK_MILLIS = 24 * HOUR_MILLIS


fun getTimeAgo(time: Long, ctx: Context?): String? {
    var time = time
    if (time < 1000000000000L) {
        // if timestamp given in seconds, convert to millis
        time *= 1000
    }
    val now: Long = Date().time
    if (time > now || time <= 0) {
        return null
    }

    // TODO: localize
    val diff = now - time
    return if (diff < MINUTE_MILLIS) {
        "just now"
    } else if (diff < 2 * MINUTE_MILLIS) {
        "a minute ago"
    } else if (diff < 50 * MINUTE_MILLIS) {
        (diff / MINUTE_MILLIS).toString() + " minutes ago"
    } else if (diff < 90 * MINUTE_MILLIS) {
        "an hour ago"
    } else if (diff < 24 * HOUR_MILLIS) {
        (diff / HOUR_MILLIS).toString() + " hours ago"
    } else if (diff < 48 * HOUR_MILLIS) {
        "yesterday"
    } else {
        (diff / DAY_MILLIS).toString() + " days ago"
    }
}


fun getTimeAgo(date: Date?): String? {
    if (date == null) {
        return null
    }
    val time = date.time
    val curDate = Date()
    val now = curDate.time
    if (time > now || time <= 0) {
        return null
    }
    val timeAgo: String = when (val dim = getTimeDistanceInMinutes(time)) {
        0 -> {
            "less than a minute"

        }
        1 -> {
            "1 minute ago"
        }
        in 2..44 -> {
            "$dim minute ago"
        }
        in 45..89 -> {
            "1 hour ago"
        }
        in 90..1439 -> {
            "${(dim / 60).toFloat().roundToInt()} hours ago"
        }
        in 1440..2519 -> {
            "1 day ago"
        }
        in 2520..43199 -> {
            "${(dim / 1440).toFloat().roundToInt()} days ago"
        }
        in 43200..86399 -> {
            "1 month ago"

        }
        in 86400..525599 -> {
            "${(dim / 43200).toFloat().roundToInt()} months ago"
        }
        in 525600..655199 -> {
            "1 year ago"

        }
        in 655200..914399 -> {
            "over a year ago"
        }
        in 914400..1051199 -> {
            "2 years ago"

        }
        else -> {
            "${(dim / 525600).toFloat().roundToInt()} years ago"

        }
    }
    return timeAgo
}

private fun getTimeDistanceInMinutes(time: Long): Int {
    val timeDistance: Long = Date().time - time
    return (abs(timeDistance) / 1000 / 60).toFloat().roundToInt()
}

internal class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
    InputFilter {
    private val mPattern: Pattern
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        val matcher: Matcher = mPattern.matcher(dest)
        return if (!matcher.matches()) "" else null
    }

    init {
        mPattern=Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");


    }
}
