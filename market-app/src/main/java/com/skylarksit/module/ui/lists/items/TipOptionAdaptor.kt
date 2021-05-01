@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.lists.items

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.skylarksit.module.R
import com.skylarksit.module.ui.model.TipOption
import com.skylarksit.module.ui.utils.HFRecyclerView
import com.skylarksit.module.ui.utils.IClickListener

class TipOptionAdaptor(
    context: Context?,
    data: List<TipOption?>?,
    private val clickListener: IClickListener<TipOption>
) : HFRecyclerView<TipOption?>(context, data, false, false) {
    override fun getItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TipOptionViewHolder(inflater.inflate(R.layout.tip_option_cell, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TipOptionViewHolder) {
            val tipOption = getItem(position)
            holder.tipButton.text = tipOption!!.tipLabel
            if (tipOption.isSelected) {
                selectTipButton(holder.tipButton)
            } else {
                unSelectTipButton(holder.tipButton)
            }
        }
    }

    // stores and recycles views as they are scrolled off screen
    inner class TipOptionViewHolder internal constructor(convertView: View) : RecyclerView.ViewHolder(convertView) {
        val tipButton: Button = convertView.findViewById(R.id.tipOptionButton)

        init {
            tipButton.setOnClickListener {
                val tipOption = getItem(adapterPosition)
                if (tipButton.isSelected) {
                    model.tip = 0.0
                    for (tip in data) {
                        if (tip?.tipValue == tipOption!!.tipValue) {
                            tip.isSelected = false
                        }
                    }
                } else {
                    model.tip = tipOption!!.tipValue
                    for (tip in data) {
                        tip?.isSelected = tip?.tipValue == tipOption.tipValue
                    }
                }
                notifyDataSetChanged()
                clickListener.click(tipOption)
            }
        }
    }

    private fun selectTipButton(tipButton: Button) {
        tipButton.background =
            context.resources.getDrawable(R.drawable.rounded_corners_button_secondary)
        tipButton.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        tipButton.setTextColor(ContextCompat.getColor(context, R.color.white))
        tipButton.isSelected = true
    }

    private fun unSelectTipButton(tipButton: Button) {
        tipButton.background = context.resources.getDrawable(R.drawable.rounded_corners_white_10)
        tipButton.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        tipButton.setTextColor(ContextCompat.getColor(context, R.color.black))
        tipButton.isSelected = false
    }
}
