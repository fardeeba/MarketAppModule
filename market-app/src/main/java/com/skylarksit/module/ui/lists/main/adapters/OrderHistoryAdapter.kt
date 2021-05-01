package com.skylarksit.module.ui.lists.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.R
import com.skylarksit.module.pojos.services.PurchaseOrderObject
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.utils.HFRecyclerView
import com.skylarksit.module.utils.TimestampUtils
import com.skylarksit.module.utils.Utilities
import java.util.concurrent.TimeUnit

class OrderHistoryAdapter(context: Context?, data: List<PurchaseOrderObject?>?) :
    HFRecyclerView<PurchaseOrderObject?>(context, data, false, false) {
    override fun getItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(inflater.inflate(R.layout.order_history_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val item = getItem(position)
            holder.title.text = item!!.label
            holder.subtitle.text = item.createdOnString
            holder.deliveryTimeLabel.text = item.statusLabel
            holder.deliveryTimeText.text = ""
            if ("DELIVERED".equals(item.status, ignoreCase = true)) {
                holder.deliveryTimeText.setTextColor(ContextCompat.getColor(context, R.color.secondaryTextColor))
                var service = model.servicesMap[item.providerUid]
                if (service == null) service = model.servicesMap[item.serviceSlug]
                var slow: Int? = null
                var fast: Int? = null
                if (service != null) {
                    slow = service.defaultEtaMins * 2
                    fast = (service.defaultEtaMins / 1.5).toInt()
                }
                if (slow == null) slow = 120
                if (fast == null) fast = (slow / 1.5).toInt()
                if (item.completedOn != null && item.createdOn != null) {
                    holder.deliveryTimeText.text = TimestampUtils.getElapsedTime(item.createdOn, item.completedOn)
                    val diff = TimeUnit.MILLISECONDS.toMinutes(item.completedOn - item.createdOn)
                    when {
                        diff > slow -> {
                            holder.deliveryTimeText.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.secondaryTextColor
                                )
                            )
                        }
                        diff <= fast -> {
                            holder.deliveryTimeText.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.thirdTextColor
                                )
                            )
                        }
                        else -> {
                            holder.deliveryTimeText.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.thirdTextColor
                                )
                            )
                        }
                    }
                }
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
                    Utilities.dpToPx(24),
                    Utilities.dpToPx(1),
                    Utilities.dpToPx(24),
                    Utilities.dpToPx(2)
                )
                holder.line.visibility = View.GONE
            } else if (topRound) {
                holder.layout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.rounded_corners_white_10_top
                )
                (holder.layout.layoutParams as RelativeLayout.LayoutParams).setMargins(
                    Utilities.dpToPx(24),
                    Utilities.dpToPx(1),
                    Utilities.dpToPx(24),
                    0
                )
                holder.line.visibility = View.GONE
            } else if (bottomRound) {
                holder.layout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.rounded_corners_white_10_bottom
                )
                (holder.layout.layoutParams as RelativeLayout.LayoutParams).setMargins(
                    Utilities.dpToPx(24),
                    0,
                    Utilities.dpToPx(24),
                    Utilities.dpToPx(2)
                )
                holder.line.visibility = View.VISIBLE
            } else {
                holder.layout.background = ContextCompat.getDrawable(context, R.color.white)
                (holder.layout.layoutParams as RelativeLayout.LayoutParams).setMargins(
                    Utilities.dpToPx(24),
                    0,
                    Utilities.dpToPx(24),
                    0
                )
                holder.line.visibility = View.VISIBLE
            }
        }
    }

    // stores and recycles views as they are scrolled off screen
    inner class ItemViewHolder internal constructor(convertView: View) : ItemsHolder(convertView) {
        val title: TextView = convertView.findViewById(R.id.title)
        val subtitle: TextView = convertView.findViewById(R.id.subtitle)
        val deliveryTimeText: TextView = convertView.findViewById(R.id.deliveryTimeText)
        val deliveryTimeLabel: TextView = convertView.findViewById(R.id.deliveryTimeLabel)
        val layout: RelativeLayout = convertView.findViewById(R.id.layout)
        val line: View = convertView.findViewById(R.id.line)

    }

    override fun getItemViewType(position: Int): Int {
        if (mWithHeader && isPositionHeader(position)) return TYPE_HEADER
        if (mWithFooter && isPositionFooter(position)) return TYPE_FOOTER
        val item: IListItem? = getItem(position)
        if (item != null && item.viewType != null) {
            if (item.viewType == TYPE_SUB_HEADER) {
                return TYPE_SUB_HEADER
            }
        }
        return TYPE_ITEM
    }
}
