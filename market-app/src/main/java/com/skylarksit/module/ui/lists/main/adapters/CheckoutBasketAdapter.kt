package com.skylarksit.module.ui.lists.main.adapters

import android.content.Context
import com.skylarksit.module.ui.model.IProductItem
import com.skylarksit.module.ui.utils.HFRecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.ui.model.BasketItem
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.skylarksit.module.R
import com.skylarksit.module.utils.Utilities

class CheckoutBasketAdapter(context: Context?, data: List<IProductItem?>?) :
    HFRecyclerView<IProductItem?>(context, data, false, false) {
    private val margins = 0
    override fun getItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(inflater.inflate(R.layout.checkout_row_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val item = getItem(position)
            holder.label.text = item!!.label
            holder.label.setTextColor(ContextCompat.getColor(context, R.color.textColor))
            if (item.viewType == BasketItem.SPECIAL) {
                holder.label.setTextColor(ContextCompat.getColor(context, R.color.redColor))
            }
            if (item.itemPrice != null) {
                holder.price.text = item.itemPrice
                holder.price.visibility = View.VISIBLE
            } else {
                holder.price.visibility = View.GONE
            }
            var topRound = false
            var bottomRound = false
            if (position == itemCount - 1 || position + 1 < itemCount && getItemViewType(position + 1) == TYPE_SUB_HEADER) {
                bottomRound = true
            }
            if (position == 0 || position - 1 >= 0 && getItemViewType(position - 1) == TYPE_SUB_HEADER) {
                topRound = true
            }
            if (topRound && bottomRound) {
                holder.layout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.rounded_corners_white_10
                )
                (holder.layout.layoutParams as RelativeLayout.LayoutParams).setMargins(
                    Utilities.dpToPx(margins),
                    Utilities.dpToPx(2),
                    Utilities.dpToPx(margins),
                    Utilities.dpToPx(2)
                )
                holder.line.visibility = View.GONE
            } else if (topRound) {
                holder.layout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.rounded_corners_white_10_top
                )
                (holder.layout.layoutParams as RelativeLayout.LayoutParams).setMargins(
                    Utilities.dpToPx(margins),
                    Utilities.dpToPx(2),
                    Utilities.dpToPx(margins),
                    0
                )
                holder.line.visibility = View.GONE
            } else if (bottomRound) {
                holder.layout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.rounded_corners_white_10_bottom
                )
                (holder.layout.layoutParams as RelativeLayout.LayoutParams).setMargins(
                    Utilities.dpToPx(margins),
                    0,
                    Utilities.dpToPx(margins),
                    Utilities.dpToPx(2)
                )
                holder.line.visibility = View.VISIBLE
            } else {
                holder.layout.background = ContextCompat.getDrawable(context, R.color.white)
                (holder.layout.layoutParams as RelativeLayout.LayoutParams).setMargins(
                    Utilities.dpToPx(margins),
                    0,
                    Utilities.dpToPx(margins),
                    0
                )
                holder.line.visibility = View.VISIBLE
            }
        }
    }

    // stores and recycles views as they are scrolled off screen
    inner class ItemViewHolder internal constructor(convertView: View) : ItemsHolder(convertView) {
        val label: TextView = convertView.findViewById(R.id.label)
        val price: TextView = convertView.findViewById(R.id.priceText)
        val layout: RelativeLayout = convertView.findViewById(R.id.layout)
        val line: View = convertView.findViewById(R.id.line)

    }

    override fun getItemViewType(position: Int): Int {
        if (mWithHeader && isPositionHeader(position)) return TYPE_HEADER
        if (mWithFooter && isPositionFooter(position)) return TYPE_FOOTER
        when (getItem(position)!!.viewType) {
            TYPE_SUB_HEADER -> return TYPE_SUB_HEADER
        }
        return TYPE_ITEM
    }
}
