package com.skylarksit.module.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.skylarksit.module.R
import com.skylarksit.module.utils.Utilities

class CurvedView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var paint: Paint? = null
    private var path: Path? = null
    var shader: LinearGradient? = null
    fun init() {
        path = Path()
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.isDither = true
        paint!!.style = Paint.Style.FILL
        paint!!.setShadowLayer(12f, 0f, 0f, Color.GRAY)

        // Important for certain APIs
        setLayerType(LAYER_TYPE_SOFTWARE, paint)
        setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (shader == null) {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f, ContextCompat.getColor(
                    context, R.color.headerColorLight
                ), ContextCompat.getColor(
                    context, R.color.headerColorDark
                ), Shader.TileMode.CLAMP
            )
            paint!!.shader = shader
        }
        val hPx = Utilities.dpToPx(10)
        val width = width.toFloat()
        val height = (height - hPx).toFloat()
        path!!.moveTo(0f, 0f)
        path!!.lineTo(0f, height)
        path!!.cubicTo(
            width / 4, height + hPx,
            width / 2 + width / 4, height + hPx,
            width, height
        )
        path!!.lineTo(width, 0f)
        path!!.lineTo(0f, 0f)
        canvas.drawPath(path!!, paint!!)
    }

    init {
        init()
    }
}
