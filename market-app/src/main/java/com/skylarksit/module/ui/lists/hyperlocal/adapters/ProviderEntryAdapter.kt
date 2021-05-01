package com.skylarksit.module.ui.lists.hyperlocal.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.skylarksit.module.R
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.utils.HFRecyclerView
import com.skylarksit.module.utils.U
import com.skylarksit.module.utils.Utilities

class ProviderEntryAdapter(activity: Context?, data: List<IListItem>) :
    HFRecyclerView<IListItem?>(activity, data, false, false) {
    override fun getItemView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(inflater.inflate(R.layout.provider_cell_view, parent, false))
    }

    @Suppress("DEPRECATION")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ItemViewHolder
        val item = getItem(position) as ServiceObject?
        viewHolder.serviceDescription.text = item!!.serviceDescription
        viewHolder.etaLabel.visibility = View.VISIBLE
        viewHolder.etaImage.visibility = View.VISIBLE
        if (!item.isClosed()) {
            viewHolder.etaLabel.text = item.etaText
        } else {
            viewHolder.etaLabel.text = context.getString(R.string.closed)
        }
        if (item.deliveryCharge != null && item.deliveryCharge > 0) {
            viewHolder.deliveryPriceLabel.text = Utilities.formatCurrency(
                item.deliveryCharge,
                item.currency.iso,
                item.currency.hideDecimals
            ) + " " + context.resources.getString(R.string.status_delivery)
        } else {
            viewHolder.deliveryPriceImage.visibility = View.GONE
            viewHolder.deliveryPriceLabel.visibility = View.GONE
        }
        if (!Utilities.isEmpty(item.priceTagLabel)) {
            viewHolder.priceTagLabel.text = item.priceTagLabel
        } else {
            viewHolder.priceTagImage.visibility = View.GONE
            viewHolder.priceTagLabel.visibility = View.GONE
        }
        //        viewHolder.serviceTextDesc.setText(item.getOpeningHours());
        viewHolder.serviceImage.setImageURI(U.parse(item.getBackgroundImageUrl()))
    }

    private inner class ItemViewHolder(convertView: View) : ItemsHolder(convertView) {
        var serviceImage: SimpleDraweeView = convertView.findViewById(R.id.serviceImage)
        var serviceDescription: TextView = convertView.findViewById(R.id.serviceDescription)
        var etaLabel: TextView = convertView.findViewById(R.id.etaLabel)
        var priceTagLabel: TextView = convertView.findViewById(R.id.priceTagTextView)
        var deliveryPriceLabel: TextView = convertView.findViewById(R.id.deliveryPriceLabel)
        var etaImage: ImageView = convertView.findViewById(R.id.etaImage)
        var deliveryPriceImage: ImageView = convertView.findViewById(R.id.deliveryPriceImage)
        var priceTagImage: ImageView = convertView.findViewById(R.id.priceImage)

    }
}
