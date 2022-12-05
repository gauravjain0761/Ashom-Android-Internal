package com.ashomapp.utils


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import androidx.annotation.Nullable
import androidx.core.view.*

/**
 * Workaround for
 * http://stackoverflow.com/questions/42783030/nestedscrollview-fling-stopping-bug-on-nougat-api-25
 * https://code.google.com/p/android/issues/detail?id=246551
 *
 *
 * Should be used only for API 21+. On API <21 It won't properly work as a nested scrolling child.
 */
class FlingableNestedScrollView : ScrollView, NestedScrollingChild, NestedScrollingParent,
    ScrollingView {
    private var parentHelper: NestedScrollingParentHelper? = null
    private var childHelper: NestedScrollingChildHelper? = null
    private var onScrollChangeListener: OnScrollChangeListener? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
    }

    // Measurement algorithm is taken from NestedScrollView
    override fun measureChild(
        child: View,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int,
    ) {
        val lp = child.layoutParams
        val childWidthMeasureSpec: Int
        val childHeightMeasureSpec: Int
        childWidthMeasureSpec =
            getChildMeasureSpec(parentWidthMeasureSpec, paddingLeft + paddingRight, lp.width)
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun measureChildWithMargins(
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int,
    ) {
        val lp = child.layoutParams as MarginLayoutParams
        val padding = paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin + widthUsed
        val childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, padding, lp.width)
        val childHeightMeasureSpec =
            MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED)
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    // NestedScrollingChild
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper!!.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper!!.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return childHelper!!.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        childHelper!!.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return childHelper!!.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, offsetInWindow: IntArray?,
    ): Boolean {
        return childHelper!!.dispatchNestedScroll(dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow)
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
    ): Boolean {
        return childHelper!!.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean,
    ): Boolean {
        return childHelper!!.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper!!.dispatchNestedPreFling(velocityX, velocityY)
    }

    // NestedScrollingParent
    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        parentHelper!!.onNestedScrollAccepted(child, target, nestedScrollAxes)
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
    }

    override fun onStopNestedScroll(target: View) {
        parentHelper!!.onStopNestedScroll(target)
        stopNestedScroll()
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        dispatchNestedPreScroll(dx, dy, consumed, null)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun getNestedScrollAxes(): Int {
        return parentHelper!!.nestedScrollAxes
    }

    override fun getTopFadingEdgeStrength(): Float {
        return super.getTopFadingEdgeStrength()
    }

    override fun getBottomFadingEdgeStrength(): Float {
        return super.getBottomFadingEdgeStrength()
    }

    //ScrollingView
    override fun computeVerticalScrollRange(): Int {
        return super.computeVerticalScrollRange()
    }

    override fun computeVerticalScrollOffset(): Int {
        return Math.max(0, super.computeVerticalScrollOffset())
    }

    override fun computeVerticalScrollExtent(): Int {
        return super.computeVerticalScrollExtent()
    }

    override fun computeHorizontalScrollRange(): Int {
        return super.computeHorizontalScrollRange()
    }

    override fun computeHorizontalScrollOffset(): Int {
        return super.computeHorizontalScrollOffset()
    }

    override fun computeHorizontalScrollExtent(): Int {
        return super.computeHorizontalScrollExtent()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (onScrollChangeListener != null) {
            onScrollChangeListener!!.onScrollChange(this, l, t, oldl, oldt)
        }
    }

    /**
     * Register a callback to be invoked when the scroll X or Y positions of
     * this view change.
     *
     * This version of the method works on all versions of Android, back to API v4.
     *
     * @param listener The listener to notify when the scroll X or Y position changes.
     * @see android.view.View.getScrollX
     * @see android.view.View.getScrollY
     */
    fun setOnScrollChangeListener(@Nullable listener: OnScrollChangeListener?) {
        onScrollChangeListener = listener
    }

    /**
     * Interface definition for a callback to be invoked when the scroll
     * X or Y positions of a view change.
     *
     *
     *
     * This version of the interface works on all versions of Android, back to API v4.
     *
     * @see .setOnScrollChangeListener
     */
    interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param v          The view whose scroll position has changed.
         * @param scrollX    Current horizontal scroll origin.
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        fun onScrollChange(
            v: FlingableNestedScrollView?,
            scrollX: Int,
            scrollY: Int,
            oldScrollX: Int,
            oldScrollY: Int,
        )
    }

    init {
        parentHelper = NestedScrollingParentHelper(this)
        childHelper = NestedScrollingChildHelper(this)
        isNestedScrollingEnabled = true
    }
}