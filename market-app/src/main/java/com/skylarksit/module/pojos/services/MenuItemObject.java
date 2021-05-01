package com.skylarksit.module.pojos.services;

import com.skylarksit.module.ui.model.IProductItem;
import com.skylarksit.module.ui.model.ServicesModel;
import com.skylarksit.module.ui.utils.HFRecyclerView;
import com.skylarksit.module.utils.Utilities;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MenuItemObject implements IProductItem, Serializable, Comparable<MenuItemObject> {

    public List<ProductCustomizationGroup> customizationItems;

    public String id; //This is the MONGO ID of market product item
    public String idProductString; //This is the MONGO ID for product item sent with collections
    public String label;
    public String searchLabel;
    public String itemViewType = "normal";
    public boolean neverRecommend = false;
    public Integer recommendationLevel = 0;

    //    public String currency;
//    public Boolean hideDecimals;
    public boolean forcePopup = true;
    public ServiceObject service;
    public String brandName;
    public boolean isFavorite;
    public boolean isVariablePrice;

    public String category;
    public String subcategory;

    public String description;
    public String watermarkUrl;
    public String promoTag;
    public String specialNote;
    public String searchKeywords;
    public Double price;
    public String imageUrl;
    public String thumbnailUrl;
    public String uid;
    public String sku;
    public String productSku;
    public String discountLabel;
    public Double strikedPrice;
    public Integer maxQty = 100;
    public Integer minQty = 0;
    public boolean outOfStock = false;
    public String unit = "unit";
    public List<String> tags;
    public List<String> recommendationTags;

    public Integer qtyPerUnit = 1;
    public String serviceSlug;
    public String thirdPartyUid;

    public Integer itemsOrdered = 0;
    public boolean showInventoryCount = false;

    public Integer sortOrder = Integer.MAX_VALUE;
    public String groupId;

    public String getThumbnailUrl(){
        if (thumbnailUrl != null) return thumbnailUrl;
        return imageUrl;
    }

    public MenuItemObject() {
    }

    public boolean isOutOfStock() {
        if (outOfStock) return true;
        if (service != null && service.hasInventory && ServicesModel.instance().inventory!= null && ServicesModel.instance().inventory.size()>0){
            Integer qty = ServicesModel.instance().inventory.get(id);
            return (qty == null || qty<=0);
        }
        return false;
    }

    public int getMaxQty() {
        if (maxQty == null) maxQty = 100;
        return maxQty;
    }

    public int getStock() {
        Integer stock = null;
        if (service.hasInventory && ServicesModel.instance().inventory!= null && ServicesModel.instance().inventory.size()>0){
            stock = ServicesModel.instance().inventory.get(id);
        }
        if (stock!=null){
            return stock;
        }
        return Integer.MAX_VALUE;
    }

    public MenuItemObject(MenuItemObject item) {

        this.label = item.label;
        this.searchLabel = item.searchLabel;
        this.itemViewType = item.itemViewType;
        this.id = item.id;
        this.idProductString = item.idProductString;
        this.tags = item.tags;
        this.thirdPartyUid = item.thirdPartyUid;
        this.forcePopup = item.forcePopup;
        this.service = item.service;
        this.brandName = item.brandName;
        this.isFavorite = item.isFavorite;
        this.subcategory = item.subcategory;
        this.category = item.category;
        this.recommendationTags = item.recommendationTags;
        this.recommendationLevel = item.recommendationLevel;
        this.description = item.description;
        this.specialNote = item.specialNote;
        this.price = item.price;
        this.imageUrl = item.imageUrl;
        this.sku = item.sku;
        this.uid = item.uid;
        this.discountLabel = item.discountLabel;
        this.strikedPrice = item.strikedPrice;
        this.maxQty = item.maxQty;
        this.minQty = item.minQty;
        this.outOfStock = item.outOfStock;
        this.unit = item.unit;
        this.qtyPerUnit = item.qtyPerUnit;
        this.serviceSlug = item.serviceSlug;
        this.itemsOrdered = item.itemsOrdered;
        this.sortOrder = item.sortOrder;
        this.promoTag = item.promoTag;
        this.groupId = item.groupId;
        this.watermarkUrl = item.watermarkUrl;


        if (item.customizationItems != null) {
            this.customizationItems = new ArrayList<>();
            for (ProductCustomizationGroup group : item.customizationItems) {
                this.customizationItems.add(new ProductCustomizationGroup(group));
            }
        }

    }

    public String getSearchLabel() {
        if (searchLabel == null) {
            searchLabel = Utilities.removeDiacriticalMarks(label).toLowerCase();
        }
        return searchLabel;
    }


    public boolean isItemSelected() {
        return itemsOrdered > 0;
    }

    public String itemsCount() {

        if (unit == null || unit.equalsIgnoreCase("unit")) {
            return itemsOrdered.intValue() + "x";
        } else {
            return itemsOrdered.intValue() * qtyPerUnit + unit;
        }

    }
    public String itemsCountSimple() {

        if (unit == null || unit.equalsIgnoreCase("unit")) {
            return String.valueOf(itemsOrdered.intValue());
        } else {
            return itemsOrdered.intValue() * qtyPerUnit + unit;
        }

    }

    public String itemsCount(Integer qty) {

        if (unit == null || unit.equalsIgnoreCase("unit")) {
            return String.valueOf(qty);
        } else {
            return qty * qtyPerUnit + unit;
        }

    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public Double getTotalPrice() {

        Double total = price;
        if (customizationItems != null) {
            for (IProductCustomizationItem pg : customizationItems) {
                for (ProductCustomizationItem pi : ((ProductCustomizationGroup) pg).items) {
                    if (pi.isSelected && pi.price != null && pi.price > 0) {
                        total += pi.price;
                    }
                }
            }
        }
        return total;
    }

    @Override
    public String getPrice() {
        if (price == null) price = 0d;
        return Utilities.getCurrencyFormatter(service.currency).format(price);
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
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

        switch (itemViewType) {
            case "full":
                return HFRecyclerView.TYPE_ITEM_FULL;
            case "subHeader":
                return HFRecyclerView.TYPE_SUB_HEADER;
        }
        return 0;
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

    public String getStrikedPriceLabel() {
        if (strikedPrice == null) return null;
        return Utilities.getCurrencyFormatter(service.currency, false).format(strikedPrice);
    }

    public boolean hasCustomizations() {
        return customizationItems != null && customizationItems.size() > 0;
    }

    public void setItems(Integer qty) {
        itemsOrdered = qty;
    }


    public boolean displayPopup() {
        return !isSingleSelection() && forcePopup;
    }

    public String getUnitLabel() {
        if (unit != null && !unit.equalsIgnoreCase("unit"))
            return "(" + qtyPerUnit + unit + ")";
        return null;
    }

    public boolean isSingleSelection() {
        return false;

    }

    @Override
    public String getQty() {
        return itemsCount();
    }

    @Override
    public String getItemPrice() {
        return Utilities.getCurrencyFormatter(service.currency, false).format(getTotalPrice() * itemsOrdered);
    }


    public boolean tagsContain(String searchString) {
        return tags!=null && tags.contains(searchString);
    }

    @Override
    public int compareTo(@NotNull MenuItemObject object2) {

        if (sortOrder == null || sortOrder == 0) sortOrder = 1000000;
        if (object2.sortOrder == null || object2.sortOrder == 0) object2.sortOrder = 1000000;

        int result = this.sortOrder.compareTo(object2.sortOrder);
        if (result == 0 && this.getLabel() != null && object2.getLabel()!=null) {
            result = this.getLabel().compareTo(object2.getLabel());
        }
        return result;
    }
}
