package com.skylarksit.module.ui.model;


public interface IListItem {

    String getLabel();
    String getDescription();
    String getPrice();
    String getImageUrl();
    String getThumbnailUrl();
    String getTag();
    int getSortOrder();
    Integer getViewType();
    int getIcon();
    String getAction();
    boolean getColorizeIcon();
}
