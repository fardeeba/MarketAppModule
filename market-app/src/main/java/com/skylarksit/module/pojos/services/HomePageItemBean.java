package com.skylarksit.module.pojos.services;

import com.skylarksit.module.pojos.CollectionData;
import com.skylarksit.module.ui.model.IListItem;
import com.skylarksit.module.ui.utils.HFRecyclerView;
import com.skylarksit.module.utils.Utilities;

import java.util.List;

public class HomePageItemBean implements IListItem{

    public String id;
    public String type;
    public String layout;
    public String itemId;
    public String label;
    public String imageUrl;
    public String thumbnailUrl;
    public String description;
    public List<String> storeGroupList;
    public String actionText;
    public Integer sortOrder;

    public MenuItemObject menuItemObject;
    public ServiceObject service;
    public MenuCategoryObject categoryObject;

    // TODO Remove all
    public String serviceSlug;
    public String serviceCategory;
    public String sku;
//    public String promoCode;
//    public String storeGroupList;
    public String actionLabel;
    public String bannerType;
    public String bannerUid;

    public Double price = 0d;
    public String currency = "USD";
    public Boolean hideDecimals;

    public String viewType;

    public CollectionData getCollectionData(){
        if (storeGroupList!=null && !storeGroupList.isEmpty()){
            return new CollectionData("stores_list",label, CollectionData.Type.STORES);
        }
        else if (categoryObject!=null){
            return categoryObject.getCollectionData(CollectionData.Type.HOME_PAGE);
        }
        return null;
    }


    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getPrice() {
        if (price==null || price == 0d || currency == null || currency.isEmpty() ) return null;
        return Utilities.getCurrencyFormatter(currency, hideDecimals, false).format(price);
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String getThumbnailUrl() {
        return thumbnailUrl != null ? thumbnailUrl : imageUrl;
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

        if (layout == null) return HFRecyclerView.TYPE_ITEM;

        switch (layout) {
            case "NO_SERVICES":
                return HFRecyclerView.TYPE_NO_SERVICES;
            case "order":
                return HFRecyclerView.TYPE_ORDER;
            case "FULL":
                return HFRecyclerView.TYPE_ITEM_FULL;
            default:
                return HFRecyclerView.TYPE_ITEM;
        }

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

}
