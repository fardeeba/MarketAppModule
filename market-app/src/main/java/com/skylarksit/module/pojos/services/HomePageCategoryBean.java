package com.skylarksit.module.pojos.services;

import com.skylarksit.module.pojos.CollectionData;
import com.skylarksit.module.ui.model.IListItem;
import com.skylarksit.module.ui.model.IListItemGroup;
import com.skylarksit.module.ui.utils.HFRecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.skylarksit.module.ui.utils.HFRecyclerView.TYPE_BLOCKS;
import static com.skylarksit.module.ui.utils.HFRecyclerView.TYPE_GRID_2;
import static com.skylarksit.module.ui.utils.HFRecyclerView.TYPE_GRID_2_WHITE;
import static com.skylarksit.module.ui.utils.HFRecyclerView.TYPE_GRID_3;
import static com.skylarksit.module.ui.utils.HFRecyclerView.TYPE_GRID_3_WHITE;
import static com.skylarksit.module.ui.utils.HFRecyclerView.TYPE_GRID_4;
import static com.skylarksit.module.ui.utils.HFRecyclerView.TYPE_GRID_4_WHITE;

public class HomePageCategoryBean implements IListItemGroup,IListItem {

    public String id;
    public Integer sortOrder;
    public String label;
    public String description;
    public String imageUrl;
    public Type type;
    public Layout layout;

    public List<HomePageItemBean> items = new ArrayList<>();

    public boolean showActionText = true;
    public boolean showLabel = true;

    public List<IListItem> listItems;

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getPrice() {
        return null;
    }

    @Override
    public List<IListItem> getItems() {
        if (listItems == null) {
            listItems = new ArrayList<>(items);
        }
        return listItems;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String getThumbnailUrl() {
        return getImageUrl();
    }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public int getSortOrder() {
        return sortOrder;
    }

    @Override
    public Integer getViewType() {

        if (layout!=null){
            switch (layout){
                case GRID_2:
                    return TYPE_GRID_2;
                case GRID_3:
                    return TYPE_GRID_3;
                case GRID_4:
                    return TYPE_GRID_4;
                case GRID_2_WHITE:
                    return TYPE_GRID_2_WHITE;
                case GRID_3_WHITE:
                    return TYPE_GRID_3_WHITE;
                case GRID_4_WHITE:
                    return TYPE_GRID_4_WHITE;
            }
        }
        switch (type){
            case PRODUCTS: //Horizontal List of products
                return HFRecyclerView.TYPE_PRODUCTS;
            case BLOCKS:
            case COLLECTIONS:
                return HFRecyclerView.TYPE_BLOCKS;
            case STORES: //vertical List of stores
                if(Layout.VERTICAL.equals(layout)){
                    return HFRecyclerView.TYPE_STORES_VERTICAL;
                }else {
                    return HFRecyclerView.TYPE_STORES_HORIZONTAL;
                }
            case BANNERS:
                return HFRecyclerView.TYPE_BANNERS;
            case FAVORITES:
                return HFRecyclerView.TYPE_FAVORITES;
            case NO_SERVICES:
                return HFRecyclerView.TYPE_NO_SERVICES;
            default:
                return TYPE_BLOCKS;
        }
    }

    @Override
    public CollectionData getCollectionData() {
        return new CollectionData(id, label, CollectionData.Type.HOME_PAGE);
    }

    @Override
    public int getIcon() {
        return 0;
    }

    @Override
    public String getAction() {
        return null;
    }

    @Override
    public boolean getColorizeIcon() {
        return false;
    }

    public enum Type {
        PRODUCTS, BANNERS, STORES, BLOCKS, CATEGORIES, COLLECTIONS, FAVORITES, NO_SERVICES
    }

    public enum Layout {
        VERTICAL, HORIZONTAL, GRID_2, GRID_3, GRID_4,  GRID_2_WHITE, GRID_3_WHITE, GRID_4_WHITE
    }


}
