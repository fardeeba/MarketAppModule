package com.skylarksit.module.ui.utils

import android.content.Context
import com.skylarksit.module.ui.model.IListItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.utils.U
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.skylarksit.module.R

class ServicesListViewAdapter(
    context: Context?,
    data: List<IListItem?>?,
    withHeader: Boolean,
    withFooter: Boolean,
    viewType: Int
) : HFRecyclerView<IListItem?>(context, data, withHeader, withFooter) {
    private var itemViewType = 0
    override fun getItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        if (itemViewType == 1) {
            return ItemViewHolder(inflater.inflate(R.layout.service_cell_view, parent, false))
        } else if (itemViewType == TYPE_STORES_HORIZONTAL) {
            return if (parent.context.resources.getBoolean(R.bool.showFullWidthStores)) {
                ItemViewHolder(
                    inflater.inflate(
                        R.layout.home_page_store_item_full,
                        parent,
                        false
                    )
                )
            } else {
                ItemViewHolder(
                    inflater.inflate(
                        R.layout.home_page_store_item,
                        parent,
                        false
                    )
                )
            }
        }
        return null
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ItemViewHolder) {
            val obj = getItem(position)
            viewHolder.label.text = obj!!.label
            if (viewHolder.priceText != null) {
                val price = obj.price
                if (price != null) {
                    viewHolder.priceText!!.text = price
                    viewHolder.priceText!!.visibility = View.VISIBLE
                } else {
                    viewHolder.priceText!!.visibility = View.GONE
                }
            }
            viewHolder.image.setImageURI(U.parse(obj.imageUrl))
        }
    }

    private inner class ItemViewHolder(itemView: View) : ItemsHolder(itemView) {
        var label: TextView = itemView.findViewById(R.id.label)
        var priceText: TextView? = itemView.findViewById(R.id.priceText)
        var image: SimpleDraweeView = itemView.findViewById(R.id.image)
    }

    init {
        itemViewType = viewType
    }
}
