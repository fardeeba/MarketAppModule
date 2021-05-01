package com.skylarksit.module.pojos;

import java.io.Serializable;

/**
 * Created by alexi on 10/12/15.
 *
 */
public class LocationObject implements Serializable {
    private String name;
    private Double lat;
    private Double lon;
    private String placeId;
    private String description;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
