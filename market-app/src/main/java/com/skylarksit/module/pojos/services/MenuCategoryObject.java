package com.skylarksit.module.pojos.services;

import com.skylarksit.module.pojos.CollectionData;
import com.skylarksit.module.ui.model.IListItem;

import java.io.Serializable;
import java.util.List;

import androidx.annotation.NonNull;

import static com.skylarksit.module.ui.utils.HFRecyclerView.TYPE_SUB_HEADER;

public class MenuCategoryObject implements Serializable, IListItem, Comparable<MenuCategoryObject> {

    public String id;
    public List<MenuItemObject> items;
    public List<MenuCategoryObject> subcategories;
    public Boolean singleSelection;
    public String label;
    public Integer sortOrder;

    public String serviceSlug;
    public String description;
    public String imageUrl;
    public String thumbnailUrl;
    public String uidServiceProvider;
    public boolean showSubcategories = true;
    public boolean isSelected;
    public String selectedSubcategory;

    public CollectionData getCollectionData(CollectionData.Type type) {
        return new CollectionData(id, label, type);
    }

    public List<MenuItemObject> getItems() {
        return items;
    }

    public void setItems(List<MenuItemObject> items) {
        this.items = items;
    }

    public List<MenuCategoryObject> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<MenuCategoryObject> subcategories) {
        this.subcategories = subcategories;
    }

    public Boolean getSingleSelection() {
        if (singleSelection == null) return false;
        return singleSelection;
    }

    public void setSingleSelection(Boolean singleSelection) {
        this.singleSelection = singleSelection;
    }

    public String getLabel() {
        if (label == null) return "";
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getPrice() {
        return null;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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
        if (sortOrder == null) sortOrder = 100000;
        return sortOrder;
    }

    public Integer viewType = TYPE_SUB_HEADER;

    @Override
    public Integer getViewType() {
        return viewType;
    }

    @Override
    public int getIcon() {
        return 0;
    }

    @Override
    public String getAction() {
        int count = items!=null ? items.size() : 0;
        return String.valueOf(count);
    }

    @Override
    public boolean getColorizeIcon() {
        return false;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    @Override
    public int compareTo(@NonNull MenuCategoryObject categoryObject) {

        if (sortOrder == null || sortOrder == 0) sortOrder = 100000;
        if (categoryObject.sortOrder == null || categoryObject.sortOrder == 0) categoryObject.sortOrder = 100000;

        int result = this.sortOrder.compareTo(categoryObject.sortOrder);
        if (result == 0) {
            result = this.getLabel().compareTo(categoryObject.getLabel());
        }
        return result;
    }
}
