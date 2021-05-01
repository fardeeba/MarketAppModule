package com.skylarksit.module.pojos.services;

import com.skylarksit.module.utils.Utilities;

import java.text.NumberFormat;

public class ProductCustomizationItem implements IProductCustomizationItem {

    public ProductCustomizationItem(){}

    public String uid;
    public Boolean isSelected = false;
    public String groupType;
    public Double price = 0d;
    public String priceLabel;

    public String groupName;
    public String label;

    public ProductCustomizationItem(ProductCustomizationItem item) {
        this.uid = item.uid;
        this.isSelected = item.isSelected;
        this.groupType = item.groupType;
        this.groupName = item.groupName;
        this.price = item.price;
        this.priceLabel = item.priceLabel;
        this.label = item.label;
    }


    public String getPrice(String currency, Boolean hideDecimals) {

        NumberFormat formatter = Utilities.getCurrencyFormatter(currency, hideDecimals);
//        formatter.setMinimumFractionDigits(0);
        return "+"+formatter.format(price);
    }

    @Override
    public String getLabel() {
        return label;
    }
}
