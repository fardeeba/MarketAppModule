package com.skylarksit.module.ui.utils

import android.view.View

abstract class OnOneOffClickListener : View.OnClickListener {
    abstract fun onSingleClick(v: View)
    override fun onClick(v: View) {
        val now = System.currentTimeMillis()
        if (mLastClickTime == 0L || now - mLastClickTime >= CLICK_TIME_INTERVAL) {
            onSingleClick(v)
            mLastClickTime = System.currentTimeMillis()
        }
    }

    companion object {
        private var mLastClickTime = 0L
        private const val CLICK_TIME_INTERVAL: Long = 500
    }
}
