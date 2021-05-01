package com.skylarksit.module.pojos;

import com.skylarksit.module.pojos.services.PurchaseOrderObject;

import java.util.List;

public class PurchaseOrderResponseBean {

    public String message;
    public String description;
    public PurchaseOrderObject data;
    public List<MissingItem> missingItems;

}
