package com.ashomapp.utils

import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF


class CustomMarkerView(context: Context?, layoutResource: Int) : MarkerView(context, layoutResource) {

}

class YourMarkerView(context: Context?, layoutResource: Int) :
    MarkerView(context, layoutResource) {



    override fun refreshContent(e: Entry?, highlight: Highlight?) {
//            super.refreshContent(e, highlight)
    }

    private var mOffset: MPPointF? = null
    override fun getOffset(): MPPointF {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = MPPointF((-(width / 2)).toFloat(), (-(height/2)).toFloat())
        }
        return mOffset!!
    }
}