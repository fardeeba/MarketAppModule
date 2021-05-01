package com.skylarksit.module.ui.model;

import android.net.Uri;

import com.skylarksit.module.ui.utils.HFRecyclerView;
import com.skylarksit.module.ui.utils.OnOneOffClickListener;

public class SectionHeaderItem implements IListItem {

    private String label;
    private String actionText;
    public OnOneOffClickListener clickListener;

    public SectionHeaderItem(String label,String actionText){
        this.label = label;
        this.actionText = actionText;
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

    @Override
    public String getImageUrl() {
        return null;
    }

    @Override
    public String getThumbnailUrl() {
        return getImageUrl();
    }

    @Override
    public String getTag() {
        return actionText;
    }

    @Override
    public int getSortOrder() {
        return 0;
    }

    @Override
    public Integer getViewType() {
        return HFRecyclerView.TYPE_SUB_HEADER;
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
