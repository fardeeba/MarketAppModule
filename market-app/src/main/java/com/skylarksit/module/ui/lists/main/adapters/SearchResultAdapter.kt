@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.lists.main.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.skylarksit.module.R
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.model.SectionHeaderItem
import com.skylarksit.module.ui.utils.HFRecyclerView
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.Utilities

class SearchResultAdapter : HFRecyclerView<IListItem?> {
    private var actionButtonListener: ItemClickListener<*>? = null
    private var itemResource = R.layout.list_result_item
    private var hideImages = false

    constructor(context: Context?, data: List<IListItem?>?, withHeader: Boolean, withFooter: Boolean) : super(
        context,
        data,
        withHeader,
        withFooter
    )

    constructor(
        context: Context?,
        data: List<IListItem?>?,
        withHeader: Boolean,
        withFooter: Boolean,
        @LayoutRes item_resource: Int
    ) : super(context, data, withHeader, withFooter) {
        this.itemResource = item_resource
    }

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
            inflater.inflate(itemResource, parent, false)
        )
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
            holder.title.text = item!!.label
            if (holder.subtitle != null) {
                if (item.description != null && item.description.isNotEmpty()) {
                    holder.subtitle.text = item.description
                    holder.subtitle.visibility = View.VISIBLE
                } else {
                    holder.subtitle.visibility = View.GONE
                }
            }
            if (holder.icon != null) holder.icon!!.visibility = View.GONE
            if (holder.image != null) holder.image!!.visibility = View.GONE
            if (!hideImages) {
                if (item.imageUrl != null && holder.icon != null) {
                    holder.icon!!.visibility = View.VISIBLE
                    holder.icon!!.setImageURI(item.imageUrl)
                } else if (item.icon != 0 && holder.image != null) {
                    holder.image!!.visibility = View.VISIBLE
                    holder.image!!.setImageResource(item.icon)
                    if (item.colorizeIcon) {
                        holder.image!!.setColorFilter(
                            ContextCompat.getColor(context, R.color.secondaryColor),
                            PorterDuff.Mode.SRC_ATOP
                        )
                    }
                }
            }
            if (holder.tag != null) {
                if (item.tag != null && item.tag.isNotEmpty()) {
                    holder.tag.visibility = View.VISIBLE
                    holder.tag.text = item.tag
                } else {
                    holder.tag.visibility = View.GONE
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
            if (holder.actionButton != null) {
                if (actionButtonListener != null && item.action != null) {
                    holder.actionButton.visibility = View.VISIBLE
                    //viewHolder.subtitle.setPadding(0,0, Utilities.dpToPx(80),0);
                    holder.actionButton.text = item.action
                } else {
                    //viewHolder.subtitle.setPadding(0,0,0,0);
                    holder.actionButton.visibility = View.GONE
                }
            }
        } else if (holder is SectionViewHolder) {
            val obj = getItem(position)
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
                    val item = getItem(adapterPosition)
                    if (item is SectionHeaderItem && item.clickListener != null) item.clickListener.onSingleClick(v)
                }
            })
        }
    }

    // stores and recycles views as they are scrolled off screen
    inner class ItemViewHolder internal constructor(convertView: View) : ItemsHolder(convertView) {
        val title: TextView = convertView.findViewById(R.id.title)
        val subtitle: TextView? = convertView.findViewById(R.id.subtitle)
        val actionButton: TextView? = convertView.findViewById(R.id.actionButton)
        val tag: TextView? = convertView.findViewById(R.id.tag)
        var icon: SimpleDraweeView? = convertView.findViewById(R.id.icon)
        var image: ImageView? = convertView.findViewById(R.id.image)
        val layout: RelativeLayout = convertView.findViewById(R.id.layout)
        val line: View = convertView.findViewById(R.id.line)

        init {
            if (actionButtonListener != null) {
                actionButton!!.setOnClickListener {
                    actionButtonListener!!.onItemClick(
                        adapterPosition
                    )
                }
            }
        }
    }

    // allows clicks events to be caught
    fun setActionButton(itemClickListener: ItemClickListener<*>?) {
        actionButtonListener = itemClickListener
    }

    override fun getItemViewType(position: Int): Int {
        if (mWithHeader && isPositionHeader(position)) return TYPE_HEADER
        if (mWithFooter && isPositionFooter(position)) return TYPE_FOOTER
        val item = getItem(position)
        if (item != null && item.viewType != null) {
            when (item.viewType) {
                TYPE_SUB_HEADER -> return TYPE_SUB_HEADER
            }
        }
        return TYPE_ITEM
    }
}
