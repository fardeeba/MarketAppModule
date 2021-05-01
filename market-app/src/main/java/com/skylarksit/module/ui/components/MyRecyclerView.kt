package com.skylarksit.module.ui.components

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.utils.Utilities

class MyRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val spec: Int = MeasureSpec.makeMeasureSpec((Utilities.screenHeight * 0.6).toInt(), MeasureSpec.AT_MOST)
        super.onMeasure(widthSpec, spec)
    }
}
