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
import com.skylarksit.module.pojos.services.MenuCategoryObject
import com.skylarksit.module.ui.utils.HFRecyclerView

class SubcategoriesAdapter(context: Context?, data: List<MenuCategoryObject?>?) :
    HFRecyclerView<MenuCategoryObject?>(context, data, false, false) {
    override fun getItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(inflater.inflate(R.layout.subcategories_item, parent, false))
    }

    override fun getHeaderView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return null
    }

    override fun getFooterView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val item = getItem(position)
            holder.title.text = item!!.getLabel()
            if (item.isSelected) {
                holder.title.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.background.background =
                    context.getDrawable(R.drawable.subcategories_background_selected)
            } else {
                holder.title.setTextColor(ContextCompat.getColor(context, R.color.secondaryTextColor))
                holder.background.background = context.getDrawable(R.drawable.subcategories_background)
            }
        }
    }

    inner class ItemViewHolder internal constructor(convertView: View) : ItemsHolder(convertView) {
        val title: TextView = convertView.findViewById(R.id.title)
        val background: RelativeLayout = convertView.findViewById(R.id.background)

    }
}
