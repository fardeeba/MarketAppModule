package com.skylarksit.module.pojos.services;

import android.net.Uri;

import com.skylarksit.module.ui.model.IProductItem;
import com.skylarksit.module.utils.Utilities;

import java.io.Serializable;
import java.util.List;

public class PurchaseOrderItemObject implements Serializable,IProductItem {

    public Double quantity = 0d;
    public String label;
    public String category;
    public String subCategory;
    public String imageUrl;
    public String thumbnailUrl;
    public String description;
    public String uid;
    public String uidProduct;
    public String sku;
    public String currency;
    public Boolean hideDecimals;
    public Double itemPrice;
    public Double totalPrice;
    public Double discountValue;
    public String discountType;
    public String legacyDescription;
    public String legacyId;
    public Integer qtyPerUnit = 1;
    public String unit = "unit";
    public String totalPriceLabel;
    public boolean isVariablePrice;

    public List<ProductCustomizationGroup> customizationItems;
    public String marketProductItemId;

    public String itemsCount() {

        if (unit == null || unit.equalsIgnoreCase("unit")){
            return quantity.intValue() + "x";
        }
        else{
            return quantity.intValue() * qtyPerUnit + unit;
        }

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
        return null;
    }


    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
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
        return 0;
    }

    @Override
    public Integer getViewType() {
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

    @Override
    public String getQty() {
        return itemsCount();
    }

    @Override
    public String getItemPrice() {
        return Utilities.getCurrencyFormatter(currency, hideDecimals, true).format(itemPrice * quantity);
    }
}
