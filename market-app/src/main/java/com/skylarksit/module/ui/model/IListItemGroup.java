package com.skylarksit.module.ui.model;

import com.skylarksit.module.pojos.CollectionData;

import java.util.List;

public interface IListItemGroup extends IListItem {
    String getLabel();
    List<IListItem> getItems();
    String getImageUrl();
    Integer getViewType();
    CollectionData getCollectionData();
}
