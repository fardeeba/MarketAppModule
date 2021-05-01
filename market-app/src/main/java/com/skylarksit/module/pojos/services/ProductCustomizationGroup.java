package com.skylarksit.module.pojos.services;

import java.util.ArrayList;
import java.util.List;

public class ProductCustomizationGroup implements IProductCustomizationItem {

    public ProductCustomizationGroup(){}

    public String label;
    public String groupType = "Radio";
    public List<ProductCustomizationItem> items;

    public ProductCustomizationGroup(ProductCustomizationGroup group) {
        this.label = group.label;
        this.groupType = group.groupType;

        if(group.items!=null){
            this.items = new ArrayList<>();
            for (ProductCustomizationItem item:group.items){
                this.items.add(new ProductCustomizationItem(item));
            }
        }

    }

    @Override
    public String getLabel() {
        return label;
    }
}
