package com.skylarksit.module.ui.components

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.view.LayoutInflater
import com.skylarksit.module.ui.model.IProductItem
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.skylarksit.module.R
import com.skylarksit.module.ui.model.BasketItem

class BasketItemView : RelativeLayout {
    var mInflater: LayoutInflater
    var item: IProductItem? = null

    constructor(context: Context?, item: IProductItem?) : super(context) {
        mInflater = LayoutInflater.from(context)
        this.item = item
        init()
    }

    constructor(context: Context?) : super(context) {
        mInflater = LayoutInflater.from(context)
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        mInflater = LayoutInflater.from(context)
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mInflater = LayoutInflater.from(context)
        init()
    }

    private fun init() {
        val convertView = mInflater.inflate(R.layout.basket_row_item, this, true)
        val label = convertView.findViewById<TextView>(R.id.label)
        val qty = convertView.findViewById<TextView>(R.id.itemCounterText)
        val price = convertView.findViewById<TextView>(R.id.priceText)
        val icon: SimpleDraweeView = convertView.findViewById(R.id.icon)
        label.text = item!!.label
        label.setTextColor(ContextCompat.getColor(context, R.color.textColor))
        if (item!!.viewType == BasketItem.SPECIAL) {
            label.setTextColor(ContextCompat.getColor(context, R.color.redColor))
        }
        if (item!!.itemPrice != null) {
            price.text = item!!.itemPrice
            price.visibility = VISIBLE
        } else {
            price.visibility = GONE
        }
        if (item!!.qty != null) {
            qty.visibility = VISIBLE
            qty.text = item!!.qty
        } else {
            qty.visibility = GONE
        }
        if (item!!.imageUrl != null) {
            icon.setImageURI(item!!.imageUrl)
            icon.visibility = VISIBLE
        } else {
            icon.visibility = GONE
        }
    }
}
