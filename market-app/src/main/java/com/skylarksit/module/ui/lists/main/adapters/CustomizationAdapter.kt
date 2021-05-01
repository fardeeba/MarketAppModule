@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.lists.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.R
import com.skylarksit.module.pojos.services.IProductCustomizationItem
import com.skylarksit.module.pojos.services.ProductCustomizationGroup
import com.skylarksit.module.pojos.services.ProductCustomizationItem
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.utils.HFRecyclerView
import java.util.*

class CustomizationAdapter(context: Context?, data: List<IProductCustomizationItem>, var serviceObject: ServiceObject) :
    HFRecyclerView<IProductCustomizationItem?>(context, data, false, false) {
    override fun getItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SUB_HEADER) {
            SectionViewHolder(
                inflater.inflate(
                    R.layout.product_item_header,
                    parent,
                    false
                )
            )
        } else ItemViewHolder(
            inflater.inflate(
                R.layout.product_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val item = getItem(position) as ProductCustomizationItem?
            if (item!!.price != null && item.price > 0) {
                holder.price.text = item.getPrice(serviceObject.currency.iso, serviceObject.currency.hideDecimals)
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
                holder.line.visibility = View.GONE
            } else if (topRound) {
                holder.line.visibility = View.GONE
            } else if (bottomRound) {
                holder.line.visibility = View.VISIBLE
            } else {
                holder.line.visibility = View.VISIBLE
            }
            if (item.groupType.equals("radio", ignoreCase = true)) {
                holder.radioButton.visibility = View.VISIBLE
                holder.checkBox.visibility = View.GONE
                holder.radioButton.text = item.label
                holder.radioButton.isChecked = item.isSelected
            } else {
                holder.checkBox.visibility = View.VISIBLE
                holder.radioButton.visibility = View.GONE
                holder.checkBox.text = item.label
                holder.checkBox.isChecked = item.isSelected
            }
        } else if (holder is SectionViewHolder) {
            val obj = getItem(position)
            holder.label.text = obj!!.label
        }
    }

    // stores and recycles views as they are scrolled off screen
    inner class SectionViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var label: TextView = itemView.findViewById(R.id.label)

    }

    // stores and recycles views as they are scrolled off screen
    inner class ItemViewHolder internal constructor(convertView: View) : RecyclerView.ViewHolder(convertView) {
        val radioButton: RadioButton = convertView.findViewById(R.id.radioButton)
        val checkBox: CheckBox = convertView.findViewById(R.id.checkbox)
        val line: View = convertView.findViewById(R.id.line)
        val price: TextView = convertView.findViewById(R.id.price)

        init {
            radioButton.setOnClickListener {
                val item = getItem(adapterPosition) as ProductCustomizationItem?
                if (item != null) {
                    item.isSelected = true
                    for (i in data) {
                        if (i is ProductCustomizationItem && item.groupName.equals(
                                i.groupName,
                                ignoreCase = true
                            ) && i.groupType.equals("radio", ignoreCase = true) && i != item
                        ) {
                            i.isSelected = false
                        }
                    }
                }
                //                notifyItemChanged(getAdapterPosition());
                notifyDataSetChanged()
            }
            checkBox.setOnClickListener {
                val item = getItem(adapterPosition) as ProductCustomizationItem?
                item!!.isSelected = !item.isSelected
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item is ProductCustomizationItem) TYPE_ITEM else TYPE_SUB_HEADER
    }

    init {
        val finalList: MutableList<IProductCustomizationItem> = ArrayList()
        for (item in data) {
            val group = item as ProductCustomizationGroup
            finalList.add(group)
            for (pi in group.items) {
                pi.groupName = group.label
                pi.groupType = group.groupType
                finalList.add(pi)
            }
        }
        super.data = finalList.toList()
    }
}
