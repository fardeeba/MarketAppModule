package com.skylarksit.module.ui.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.skylarksit.module.R;
import com.skylarksit.module.ui.model.IListItem;
import com.skylarksit.module.utils.U;
import com.skylarksit.module.utils.Utilities;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

public class HomePageBlockAdapter extends HFRecyclerView<IListItem> {

    private int parentType;

    public HomePageBlockAdapter(Context context, List<IListItem> data, boolean withHeader, boolean withFooter, int parentType) {
        super(context, data, withHeader, withFooter);
        this.parentType = parentType;
    }

    @Override
    protected RecyclerView.ViewHolder getItemView(LayoutInflater inflater, ViewGroup parent, int viewType) {
        if (parentType == TYPE_GRID_2_WHITE || parentType == TYPE_GRID_3_WHITE || parentType == TYPE_GRID_4_WHITE) {
            return new ItemViewHolder(inflater.inflate(R.layout.home_page_block_item_white, parent, false));
        }
        if (parent.getContext().getResources().getBoolean(R.bool.showFullWidthItem)) {
            return new ItemViewHolder(inflater.inflate(R.layout.home_page_block_item_full_centeral_text, parent, false));
        } else {
            return new ItemViewHolder(inflater.inflate(R.layout.home_page_block_item_full, parent, false));
        }
    }

    @Override
    protected RecyclerView.ViewHolder getHeaderView(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected RecyclerView.ViewHolder getFooterView(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof ItemViewHolder) {

            IListItem obj = getItem(position);

            ItemViewHolder holder = (ItemViewHolder) viewHolder;

            if (!Utilities.isEmpty(obj.getLabel())) {
                holder.label.setText(model.homePageAlignCenter ? obj.getLabel().toUpperCase() : obj.getLabel());
            } else {
                holder.label.setText("");
            }

            if (holder.description != null) {

                if (Utilities.isEmpty(obj.getDescription())) {
                    holder.description.setVisibility(View.GONE);
                } else {
                    holder.description.setVisibility(View.VISIBLE);
                    holder.description.setText(model.homePageAlignCenter ? obj.getDescription().toUpperCase() : obj.getDescription());
                }
            }

            holder.image.setImageURI(U.parse(obj.getImageUrl()));

        }

    }

    private class ItemViewHolder extends ItemsHolder {
        TextView label;
        TextView description;
        RelativeLayout textLayout;
        SimpleDraweeView image;

        ItemViewHolder(final View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            textLayout = itemView.findViewById(R.id.textLayout);
            description = itemView.findViewById(R.id.description);
            image = itemView.findViewById(R.id.image);

//            if (model.homePageAlignCenter){
//                description.setGravity(Gravity.CENTER_HORIZONTAL);
//                label.setGravity(Gravity.CENTER_HORIZONTAL);
//                RelativeLayout.LayoutParams layoutParams =
//                        (RelativeLayout.LayoutParams)textLayout.getLayoutParams();
//                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//                textLayout.setLayoutParams(layoutParams);
//            }
//            else{
//                RelativeLayout.LayoutParams layoutParams =
//                        (RelativeLayout.LayoutParams)textLayout.getLayoutParams();
//                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//                textLayout.setLayoutParams(layoutParams);
//            }
        }

    }

    @Override
    public int getItemViewType(int position) {

        IListItem item = getItem(position);

        Integer viewType = item.getViewType();

       /* if (position == 0 && context.getResources().getBoolean(R.bool.showFullWidthItem)) {
            return TYPE_ITEM_FULL;
        }*/

        if (viewType != null && viewType > 0)
            return viewType;

        return TYPE_ITEM;
    }


}
