package com.skylarksit.module.pojos;

public class MarketStoreGroup {

    public String id;
    public String label;
    public Integer sortOrder = 0;

    public Integer getSortOrder() {
        return sortOrder == null ? 0 : sortOrder;

    }
}
