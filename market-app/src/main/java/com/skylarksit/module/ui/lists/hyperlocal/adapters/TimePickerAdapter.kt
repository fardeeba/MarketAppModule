package com.skylarksit.module.ui.lists.hyperlocal.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView

class TimePickerAdapter(context: Context?, var items: MutableList<DateTimeEntry>, clickListener: BtnClickListener?) :
    BaseAdapter() {

    interface BtnClickListener {
        fun onBtnClick(item: DateTimeEntry?)
    }

    private var clickListener: BtnClickListener? = null
    override fun getItem(position: Int): DateTimeEntry {
        return items[position]
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val viewHolder: ViewHolder = convertView.tag as ViewHolder
        val item = getItem(position)
        convertView.isEnabled = item.isActive
        convertView.alpha = if (item.isActive) 1f else 0.5f
        viewHolder.relativeLayout!!.tag = item
        viewHolder.label!!.text = item.label
        return convertView
    }

    inner class ViewHolder {
        var label: TextView? = null
        var relativeLayout: RelativeLayout? = null
    }

    init {
        this.clickListener = clickListener
    }
}
