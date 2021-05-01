package com.skylarksit.module.ui.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skylarksit.module.R;
import com.skylarksit.module.pojos.services.MenuItemObject;
import com.skylarksit.module.ui.activities.hyperlocal.FinalGridActivity;
import com.skylarksit.module.ui.model.IListItem;
import com.skylarksit.module.utils.U;
import com.skylarksit.module.utils.Utilities;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class  ItemsGridViewAdapter<T extends IListItem> extends HFRecyclerView<T> {

    private ItemClickListener serviceClickButtonListener;
    private ItemClickListener quickAddListener;
    private ItemClickListener quickRemoveListener;
    private ItemClickListener itemClickListener;
    private String listType = "grid";
    private boolean isHorizontalScroll;
    private boolean hideImages = false;

    public void setHideImages(boolean hide){
        this.hideImages = hide;
    }

    public FinalGridActivity activity;

    public ItemsGridViewAdapter(Context context, List<T> data, boolean isHorizontalScroll) {
        super(context,data, false, false);
        init(data,isHorizontalScroll, "grid");
    }

    public void init(List<T> data, boolean isHorizontalScroll, String listType){
        this.listType = listType;
        this.isHorizontalScroll = isHorizontalScroll;
        setData(data);
    }

    public ItemsGridViewAdapter(Context context, List<T> data, String listType, boolean withHeader, boolean withFooter) {
        super(context,data, withHeader, withFooter);
        init(data, false, listType);
    }

    @Override
    protected RecyclerView.ViewHolder getItemView(LayoutInflater inflater, ViewGroup parent, int viewType) {

        if ("basket".equals(listType)){
            return new ListItemViewHolder(inflater.inflate(R.layout.items_gridview_basket, parent, false));
        }
        if (viewType == TYPE_ITEM || viewType == TYPE_ITEM_FULL) {

            if ("list".equalsIgnoreCase(listType)) {
                return new ListItemViewHolder(inflater.inflate(R.layout.items_gridview_list_cell, parent, false));
            } else {
                return new ItemViewHolder(inflater.inflate(R.layout.items_gridview_cell, parent, false));
            }

        } else if (viewType == TYPE_SUB_HEADER) {
            if ("list".equalsIgnoreCase(listType)) {
                return new SectionViewHolder(inflater.inflate(R.layout.items_gridview_list_subheader, parent, false));
            } else {
                return new SectionViewHolder(inflater.inflate(R.layout.items_gridview_subheader, parent, false));
            }
        }
        else if (viewType == TYPE_SERVICE){
            return new ServiceItemHolder(inflater.inflate(R.layout.search_service_item, parent, false));
        }

        return null;

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if (position < 0) return;

        if (getItemCount()==0) return;

        if (viewHolder instanceof ItemViewHolder) {

            MenuItemObject obj = (MenuItemObject) getItem(position);

            ItemViewHolder holder = (ItemViewHolder) viewHolder;

            holder.label.setText(obj.label);
            if(obj.service!=null){
                holder.priceText.setText(Utilities.getCurrencyFormatter(obj.service.currency,false).format(obj.price));
            }
            holder.image.setImageURI(U.parse(obj.getThumbnailUrl()));


            if (holder.watermark!=null){
                if (Utilities.notEmpty(obj.watermarkUrl)){
                    holder.watermark.setImageURI(obj.watermarkUrl);
                    holder.watermark.setVisibility(View.VISIBLE);
                }
                else{
                    holder.watermark.setVisibility(View.GONE);
                }
            }

            if (holder.quickAddLabel!=null){
                holder.quickAddLabel.setText(obj.isOutOfStock() ? R.string.out_of_stock : R.string.add);

                if (!"list".equalsIgnoreCase(listType)) {
                    holder.quickAddLabel.setTextColor(obj.isOutOfStock() ? ContextCompat.getColor(context,R.color.secondaryColor) : ContextCompat.getColor(context,R.color.secondaryColor));
                }
                else{
                    holder.quickAddLabel.setTextColor(obj.isOutOfStock() ? ContextCompat.getColor(context,R.color.color_light_grey) : ContextCompat.getColor(context,R.color.secondaryColor));
                }

                holder.quickAddText.setEnabled(!obj.isOutOfStock());

                if (!"list".equalsIgnoreCase(listType)) {
                    holder.quickAddLabel.setBackground(ContextCompat.getDrawable(context, obj.isOutOfStock() ? R.drawable.add_item_background_soldout : R.drawable.add_item_background));
                }
//                else{
//                    holder.quickAddText.setAlpha(obj.isOutOfStock() ? 0.5f : 1f);
//                }
            }

            if (holder.quickAddText!=null) {
                if (obj.isItemSelected())
                    holder.quickAddText.setVisibility(View.GONE);
                else{
                    holder.quickAddText.setVisibility(View.VISIBLE);
                }
            }

//            if(context.getResources().getBoolean(R.bool.localize_app)){
//                holder.itemCounterText.setBackgroundResource(R.drawable.item_counter_top_ar);
//                holder.minusImage.setBackgroundResource(R.drawable.counter_button_remove_ar);
//            }

            if (obj.isItemSelected()) {
                holder.itemCounterText.setVisibility(View.VISIBLE);
                holder.itemCounterText.setText(obj.itemsCountSimple());

//                if (holder.minus!=null) {
//                    holder.minus.setVisibility(View.VISIBLE);
//                }
                if (holder.addRemove!=null){
                    holder.quickAddText.setVisibility(View.GONE);
                    holder.addRemove.setVisibility(View.VISIBLE);
                }
            } else {
                holder.itemCounterText.setVisibility(View.GONE);
//                if (holder.minus!=null)
//                    holder.minus.setVisibility(View.GONE);
                if (holder.addRemove!=null){
                    holder.addRemove.setVisibility(View.GONE);
                }
            }

            /*if (holder.description!=null){
                if (obj.description != null && !obj.description.isEmpty()) {
                    holder.description.setVisibility(View.VISIBLE);
                    holder.description.setText(obj.description);
                } else {
                    holder.description.setVisibility(View.GONE);
                }
            }*/

            if (holder.addRemove!= null && holder.plusHitArea!=null){
                holder.plusHitArea.setVisibility(holder.addRemove.getVisibility());
                holder.minusHitArea.setVisibility(holder.addRemove.getVisibility());
            }

            if (holder.discountText!=null){
                if (!Utilities.isEmpty(obj.discountLabel)) {
                    holder.discountText.setVisibility(View.VISIBLE);
                    holder.discountText.setText(obj.discountLabel);

                } else if (!Utilities.isEmpty(obj.promoTag)){
                    holder.discountText.setVisibility(View.VISIBLE);
                    holder.discountText.setText(obj.promoTag);
                }
                else{
                    holder.discountText.setVisibility(View.GONE);
                }
            }

            if (obj.getUnitLabel()!=null) {
                holder.unitTypeText.setVisibility(View.VISIBLE);
                holder.unitTypeText.setText(obj.getUnitLabel());
            } else {
                holder.unitTypeText.setVisibility(View.GONE);
            }

            if (holder instanceof ListItemViewHolder){

                ListItemViewHolder iHolder = (ListItemViewHolder) holder;

                boolean topRound = false;
                boolean bottomRound = false;

                if (position == getItemCount()-1 || position+1 < getItemCount() && getItemViewType(position+1) == TYPE_SUB_HEADER){
                    bottomRound = true;
                }

                if (position == 0 || getItemViewType(position - 1) == TYPE_SUB_HEADER){
                    topRound = true;
                }

                if (topRound && bottomRound){
                    iHolder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_corners_white_10));
                    ((RelativeLayout.LayoutParams)iHolder.layout.getLayoutParams()).setMargins(Utilities.dpToPx(1), Utilities.dpToPx(1), Utilities.dpToPx(1), Utilities.dpToPx(1));
                    iHolder.line.setVisibility(View.GONE);
                }
                else if (topRound){
                    iHolder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_corners_white_10_top));
                    ((RelativeLayout.LayoutParams)iHolder.layout.getLayoutParams()).setMargins(Utilities.dpToPx(1), Utilities.dpToPx(1), Utilities.dpToPx(1),0);
                    iHolder.line.setVisibility(View.GONE);
                }
                else if (bottomRound){
                    iHolder.layout.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_corners_white_10_bottom));
                    ((RelativeLayout.LayoutParams)iHolder.layout.getLayoutParams()).setMargins(Utilities.dpToPx(1),0, Utilities.dpToPx(1), Utilities.dpToPx(2));
                    iHolder.line.setVisibility(View.VISIBLE);
                }
                else {
                    iHolder.layout.setBackground(ContextCompat.getDrawable(context, R.color.white));
                    ((RelativeLayout.LayoutParams)iHolder.layout.getLayoutParams()).setMargins(Utilities.dpToPx(1),0, Utilities.dpToPx(1),0);
                    iHolder.line.setVisibility(View.VISIBLE);
                }
            }

            holder.image.setVisibility(hideImages && "list".equalsIgnoreCase(listType) ? View.GONE : View.VISIBLE);


        } else if (viewHolder instanceof SectionViewHolder) {
            SectionViewHolder holder = (SectionViewHolder) viewHolder;
            IListItem listItem = getItem(position);
            if (listItem!=null){
                holder.label.setText(listItem.getLabel());
            }
            else{
                holder.label.setText("");
            }
        }
        else if (viewHolder instanceof ServiceItemHolder) {
            ServiceItemHolder holder = (ServiceItemHolder) viewHolder;
            IListItem obj = getItem(position);
            holder.image.setImageURI(U.parse(obj.getImageUrl()));
        }


    }

    public class SectionViewHolder<Y> extends RecyclerView.ViewHolder {
        public TextView label;
        public RelativeLayout background;

        SectionViewHolder(final View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            background = itemView.findViewById(R.id.background);

            if(background != null) {
                if (context.getResources().getBoolean(R.bool.showItemBox)) {
                    background.setBackground(null);
                } else {
                    background.setBackground(context.getDrawable(R.color.white));
                    background.setPadding(Utilities.dpToPx(12), 0, Utilities.dpToPx(12), 0);
                }
            }

        }

    }

    public class ServiceItemHolder<Y> extends RecyclerView.ViewHolder {
        public SimpleDraweeView image;

        ServiceItemHolder(final View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            itemView.setOnClickListener(view -> serviceClickButtonListener.onItemClick(getAdapterPosition()));
        }

    }

    // stores and recycles views as they are scrolled off screen
    class ItemViewHolder<Y> extends ItemsHolder {
        //        ImageView infoButton;
        TextView label;
        TextView description;
        TextView priceText;
        SimpleDraweeView image;
        SimpleDraweeView watermark;
        TextView itemCounterText;
        TextView unitTypeText;
        TextView discountText;
        LinearLayout textBox;
        View quickAddText;
        View imageBg;
        TextView quickAddLabel;
//        View minus;
        View minusImage;
        View addRemove;
        View plusHitArea;
        View minusHitArea;

        ItemViewHolder(final View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            description = itemView.findViewById(R.id.description);
            priceText = itemView.findViewById(R.id.priceText);
            discountText = itemView.findViewById(R.id.discountText);
            unitTypeText = itemView.findViewById(R.id.unitType);
            quickAddLabel = itemView.findViewById(R.id.quickAddLabel);
            imageBg = itemView.findViewById(R.id.imageBg);
            image = itemView.findViewById(R.id.image);
            watermark = itemView.findViewById(R.id.watermark);
            itemCounterText = itemView.findViewById(R.id.itemCounterText);
            textBox = itemView.findViewById(R.id.textBox);
            quickAddText = itemView.findViewById(R.id.quickAddText);
//            minus = itemView.findViewById(R.id.minus);
            minusImage = itemView.findViewById(R.id.minusImage);
            addRemove = itemView.findViewById(R.id.addRemove);
            plusHitArea = itemView.findViewById(R.id.plusHitArea);
            minusHitArea = itemView.findViewById(R.id.minusHitArea);

            if (itemClickListener!=null){
                if ("list".equalsIgnoreCase(listType) || "basket".equalsIgnoreCase(listType)){
                    itemView.setOnClickListener(new OnOneOffClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            itemClickListener.onItemClick(getAdapterPosition());
                        }
                    });
                }
                else {
                    image.setOnClickListener(new OnOneOffClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            itemClickListener.onItemClick(getAdapterPosition());
                        }
                    });
                }
            }

            if (plusHitArea!=null){
                plusHitArea.setOnClickListener(view -> {
                    if (quickAddListener!=null) {
                        quickAddListener.setClickTimeInterval(0);
                        quickAddListener.onItemClick(getAdapterPosition());
                    }
                });
            }
            if (minusHitArea!=null){
                minusHitArea.setOnClickListener(view -> {
                    if (quickRemoveListener!=null) {
                        quickRemoveListener.setClickTimeInterval(0);
                        quickRemoveListener.onItemClick(getAdapterPosition());
                    }
                });
            }

            if (isHorizontalScroll)
                itemView.getLayoutParams().width = Utilities.dpToPx(120);
            if (quickAddText!=null){
                quickAddText.setOnClickListener(view -> {
                    if (quickAddListener!=null) {
                        quickAddListener.setClickTimeInterval(0);
                        quickAddListener.onItemClick(getAdapterPosition());
                    }
                });
            }

        }

    }

    class ListItemViewHolder<Y> extends ItemViewHolder {

        RelativeLayout layout;
        View line;

        ListItemViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            line = itemView.findViewById(R.id.line);

        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    public void setQuickRemoveListener(ItemClickListener itemClickListener) {
        this.quickRemoveListener = itemClickListener;
    }

    public void setQuickAddListener(ItemClickListener itemClickListener) {
        this.quickAddListener = itemClickListener;
    }

    public void setServiceClickButtonListener(ItemClickListener itemClickListener) {
        this.serviceClickButtonListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {

        if (mWithHeader && isPositionHeader(position))
            return TYPE_HEADER;
        if (mWithFooter && isPositionFooter(position))
            return TYPE_FOOTER;

        IListItem item = getItem(position);
        if (item != null){
            int viewType = item.getViewType();
            if (viewType>0)
                return viewType;

            return TYPE_ITEM;
        }

        return TYPE_HEADER;

    }

}
