package com.skylarksit.module.pojos.services;

public class OrderTrackingBean extends HomePageItemBean {

    public String companyName;
    public String status;
    public String orderUid;
    public String eta;
    public String etaMins;
    public Integer etaTime;
    public Boolean feedbackOrder;
    public String iconUrl;
    public String title = "Delivered in just 15 minutes!";
    public String subtitle = "Rate this order!";

    @Override
    public String getLabel() {
        return companyName;
    }

    @Override
    public String getDescription() {
        return status;
    }

    @Override
    public String getPrice() {
        return null;
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
        return 100;
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
