package com.skylarksit.module.ui.model;


public class BasketItem implements IProductItem {

    public static final Integer CHARGES = 51;
    public static final Integer PRODUCT = 52;
    public static final Integer TOTAL = 53;
    public static final Integer SPECIAL = 54;
    public String price;
    public String label;
    public Integer type;

    public BasketItem(String label, String price, Integer type){
        this.price = price;
        this.type = type;
        this.label = label;
    }

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
        return price;
    }

    @Override
    public String getImageUrl() {
        return null;
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
        return 0;
    }

    @Override
    public Integer getViewType() {
        return type;
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

    @Override
    public String getQty() {
        return null;
    }

    @Override
    public String getItemPrice() {
        return price;
    }
}
