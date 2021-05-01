package com.skylarksit.module.ui.lists.items;

import com.skylarksit.module.R;
import com.skylarksit.module.pojos.AddressObject;
import com.skylarksit.module.ui.model.IListItem;

public class SearchResultItem implements IListItem {

    public String label;
    public String description;
    public int icon;
    public String imageUrl;
    public String type;
    public Object data;
    public String actionButtonLabel;
    public String tag;
    public String uid;
    public boolean colorizeIcon;

    public SearchResultItem() {

    }

    public SearchResultItem(AddressObject addressObject) {

        this.label = addressObject.getLabel();
        this.description = addressObject.getDescription();
        this.type = "EDDRESS";
        this.data = addressObject;

        icon = R.drawable.pin_dark;

    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
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
    public String getThumbnailUrl() {
        return imageUrl;
    }

    @Override
    public String getTag() {
        return tag;
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
        return icon;
    }

    @Override
    public String getAction() {
        return actionButtonLabel;
    }

    @Override
    public boolean getColorizeIcon() {
        return colorizeIcon;
    }


}
