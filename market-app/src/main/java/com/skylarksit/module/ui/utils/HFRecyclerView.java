package com.skylarksit.module.ui.utils;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skylarksit.module.ui.model.ServicesModel;

import java.util.List;

public abstract class HFRecyclerView<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 999;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_SERVICE = 13;
    public static final int TYPE_FOOTER = 888;
    public static final int TYPE_ITEM_FULL = 11;
    public static final int TYPE_SUB_HEADER = 12;


    public static final int TYPE_ORDER = 100;
    public static final int TYPE_NO_SERVICES = 200;
    public static final int TYPE_STORES_HORIZONTAL = 300;
    public static final int TYPE_STORES_VERTICAL = 400;
    public static final int TYPE_BLOCKS = 500;

    public static final int TYPE_GRID_2 = 502;
    public static final int TYPE_GRID_3 = 503;
    public static final int TYPE_GRID_4 = 504;

    public static final int TYPE_GRID_2_WHITE = 5021;
    public static final int TYPE_GRID_3_WHITE = 5031;
    public static final int TYPE_GRID_4_WHITE = 5041;

    public static final int TYPE_COLLECTIONS = 600;
    public static final int TYPE_PRODUCTS = 700;
    public static final int TYPE_BANNERS = 800;
    public static final Integer TYPE_FAVORITES = 900;

    public final Context context;

    protected ServicesModel model;

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public void setClickListener(ItemClickListener itemClickListener, int interval) {
        this.mClickListener = itemClickListener;
        this.mClickListener.setClickTimeInterval(interval);
    }


    protected List<T> data;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    private ItemClickListener mClickListener;

    protected boolean mWithHeader;
    protected boolean mWithFooter;

    public HFRecyclerView(Context context, List<T> data, boolean withHeader, boolean withFooter) {
        this.data = data;
        mWithHeader = withHeader;
        mWithFooter = withFooter;
        this.context = context;
        model = ServicesModel.instance(context);
    }

    //region Get View
    protected abstract RecyclerView.ViewHolder getItemView(LayoutInflater inflater, ViewGroup parent, int viewType);

    protected RecyclerView.ViewHolder getHeaderView(LayoutInflater inflater, ViewGroup parent, int viewType){return null;}

    protected RecyclerView.ViewHolder getFooterView(LayoutInflater inflater, ViewGroup parent, int viewType){return null;}
    //endregion

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_HEADER) {
            return getHeaderView(inflater, parent,viewType);
        } else if (viewType == TYPE_FOOTER) {
            return getFooterView(inflater, parent,viewType);
        }

        return getItemView(inflater, parent,viewType);

    }

    @Override
    public int getItemCount() {
        int itemCount = data.size();
        if (mWithHeader)
            itemCount++;
        if (mWithFooter)
            itemCount++;
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (mWithHeader && isPositionHeader(position))
            return TYPE_HEADER;
        if (mWithFooter && isPositionFooter(position))
            return TYPE_FOOTER;
        return TYPE_ITEM;
    }

    protected boolean isPositionHeader(int position) {
        return position == 0;
    }

    protected boolean isPositionFooter(int position) {
        return position == getItemCount() - 1;
    }

    public T getItem(int position) {

        if (data.size() == 0) return null;

        if (mWithHeader){
            int pos = position - 1;
            if (pos >= 0 && pos < data.size()){
                return data.get(pos);
            }
        }else{
            if (position >= 0 && position < data.size()){
                return data.get(position);
            }
        }
        return null;
    }

    public class ItemsHolder extends RecyclerView.ViewHolder{

        public ItemsHolder(View convertView){
            super(convertView);

            if (mClickListener!=null){
                convertView.setOnClickListener(view -> mClickListener.onItemClick(getAdapterPosition()));
            }

        }

    }
}
