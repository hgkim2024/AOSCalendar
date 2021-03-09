package com.asusoft.calendar.util.`object`

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.orhanobut.logger.Logger
import kotlin.math.abs

class LongPressChecker(context: Context?) {

    interface OnLongPressListener {
        fun onLongPressed(x: Float, y: Float)
    }

    private val mHandler = Handler()
    private val mLongPressCheckRunnable = LongPressCheckRunnable()
    private val mLongPressTimeout: Int
    private val mScaledTouchSlope: Int
    private var mTargetView: View? = null
    private var mOnLongPressListener: OnLongPressListener? = null
    private var mLongPressed = false
    private var mLastX = 0f
    private var mLastY = 0f

    init {
        if (Looper.myLooper() != Looper.getMainLooper()) throw RuntimeException()
        mLongPressTimeout = ViewConfiguration.getLongPressTimeout()
        mScaledTouchSlope = ViewConfiguration.get(context).scaledTouchSlop
    }

    fun setOnLongPressListener(listener: OnLongPressListener?) {
        mOnLongPressListener = listener
    }

    fun deliverMotionEvent(v: View?, event: MotionEvent) {
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                mTargetView = v
                mLastX = event.x
                mLastY = event.y
                startTimeout()
            }

            MotionEvent.ACTION_MOVE -> {
                val x: Float = event.x
                val y: Float = event.y
                if (
                    abs(x - mLastX) > mScaledTouchSlope
                    || abs(y - mLastY) > mScaledTouchSlope
                ) {
                    stopTimeout()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopTimeout()
            }
        }
    }

    private fun startTimeout() {
        Logger.d("startTimeout")
        mLongPressed = false
        mHandler.postDelayed(mLongPressCheckRunnable, mLongPressTimeout.toLong())
    }

    private fun stopTimeout() {
        Logger.d("stopTimeout")
        if (!mLongPressed) mHandler.removeCallbacks(mLongPressCheckRunnable)
    }

    private inner class LongPressCheckRunnable : Runnable {
        override fun run() {
            Logger.d("LongPressCheckRunnable run")
            mLongPressed = true
            if (mOnLongPressListener != null) {
                mTargetView!!.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                mOnLongPressListener!!.onLongPressed(mLastX, mLastY)
            }
        }
    }

}