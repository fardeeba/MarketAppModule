package com.skylarksit.module.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.skylarksit.module.R
import com.skylarksit.module.utils.Utilities

class ClippedCurvedView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var paint: Paint? = null
    private var path: Path? = null
    fun init() {
        path = Path()
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.isDither = true
        paint!!.style = Paint.Style.FILL
        paint!!.color = ContextCompat.getColor(context, R.color.white)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val hPx = Utilities.dpToPx(20).toFloat()
        val width = width.toFloat()
        path!!.moveTo(0f, 0f)
        path!!.lineTo(0f, hPx)
        path!!.lineTo(width, hPx)
        path!!.lineTo(width, 0f)
        path!!.cubicTo(width / 2 + width / 4, hPx, width / 4, hPx, 0f, 0f)
        canvas.drawPath(path!!, paint!!)
    }

    init {
        init()
    }
}
