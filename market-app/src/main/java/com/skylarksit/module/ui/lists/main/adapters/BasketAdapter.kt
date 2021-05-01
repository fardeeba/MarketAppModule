@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.lists.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.skylarksit.module.R
import com.skylarksit.module.ui.model.BasketItem
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.model.IProductItem
import com.skylarksit.module.ui.model.SectionHeaderItem
import com.skylarksit.module.ui.utils.HFRecyclerView
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.Utilities

class BasketAdapter(context: Context?, data: List<IProductItem?>?) :
    HFRecyclerView<IProductItem?>(context, data, false, false) {
    override fun getItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SUB_HEADER) {
            SectionViewHolder(
                inflater.inflate(
                    R.layout.list_section_header,
                    parent,
                    false
                )
            )
        } else ItemViewHolder(
            inflater.inflate(
                R.layout.basket_row_item,
                parent,
                false
            )
        )
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
            if (item.qty != null) {
                holder.qty.visibility = View.VISIBLE
                holder.qty.text = item.qty
            } else {
                holder.qty.visibility = View.GONE
            }
            if (item.imageUrl != null) {
                holder.icon.setImageURI(item.imageUrl)
                holder.icon.visibility = View.VISIBLE
            } else {
                holder.icon.visibility = View.GONE
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
                    Utilities.dpToPx(6),
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
                    Utilities.dpToPx(6),
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
                holder.layout.background =
                    ContextCompat.getDrawable(context, R.color.white)
                (holder.layout.layoutParams as RelativeLayout.LayoutParams).setMargins(
                    Utilities.dpToPx(24),
                    0,
                    Utilities.dpToPx(24),
                    0
                )
                holder.line.visibility = View.VISIBLE
            }

//            if (actionButtonListener!=null && item.getAction() != null){
//                viewHolder.actionButton.setVisibility(View.VISIBLE);
//                viewHolder.subtitle.setPadding(0,0,Utilities.dpToPx(80),0);
//                viewHolder.actionButton.setText(item.getAction());
//            }
//            else{
//                viewHolder.subtitle.setPadding(0,0,0,0);
//                viewHolder.actionButton.setVisibility(View.GONE);
//            }
        } else if (holder is SectionViewHolder) {
            val obj: IListItem? = getItem(position)
            holder.label.text = obj!!.label
            if (obj.tag != null && obj.tag.isNotEmpty()) {
                holder.actionText.visibility = View.VISIBLE
                holder.actionText.text = obj.tag
            } else {
                holder.actionText.visibility = View.GONE
            }
        }
    }

    // stores and recycles views as they are scrolled off screen
    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var label: TextView = itemView.findViewById(R.id.label)
        var actionText: TextView = itemView.findViewById(R.id.actionText)

        init {
            actionText.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View) {
                    val item: IListItem? = getItem(adapterPosition)
                    if (item is SectionHeaderItem && item.clickListener != null) item.clickListener.onSingleClick(v)
                }
            })
        }
    }

    // stores and recycles views as they are scrolled off screen
    inner class ItemViewHolder internal constructor(convertView: View) : ItemsHolder(convertView) {
        val label: TextView = convertView.findViewById(R.id.label)
        val price: TextView = convertView.findViewById(R.id.priceText)
        val qty: TextView = convertView.findViewById(R.id.itemCounterText)

        //        private TextView actionButton;
        var icon: SimpleDraweeView = convertView.findViewById(R.id.icon)
        val layout: RelativeLayout = convertView.findViewById(R.id.layout)
        val line: View = convertView.findViewById(R.id.line)
    }

    //
    //    // allows clicks events to be caught
    //    public void setActionButton(ItemClickListener itemClickListener) {
    //        this.actionButtonListener = itemClickListener;
    //    }
    override fun getItemViewType(position: Int): Int {
        if (mWithHeader && isPositionHeader(position)) return TYPE_HEADER
        if (mWithFooter && isPositionFooter(position)) return TYPE_FOOTER
        when (getItem(position)!!.viewType) {
            TYPE_SUB_HEADER -> return TYPE_SUB_HEADER
        }
        return TYPE_ITEM
    }
}
