package com.skylarksit.module.pojos.services;

import com.skylarksit.module.ui.model.ILabelItem;

public class GetLocationsResult implements ILabelItem {

    public GetLocationsResult(String label, Double lat, Double lon) {
        this.label = label;
        this.lat = lat;
        this.lon = lon;
    }

    public String label;
    public Double lat;
    public Double lon;
    public String uid;

    @Override
    public String toString() {
        return label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
