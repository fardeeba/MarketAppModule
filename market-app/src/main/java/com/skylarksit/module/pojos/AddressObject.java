package com.skylarksit.module.pojos;

import android.location.Location;

import com.skylarksit.module.R;
import com.skylarksit.module.ui.model.ILabelItem;
import com.skylarksit.module.ui.model.IListItem;
import com.skylarksit.module.utils.Utilities;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AddressObject implements Serializable, Cloneable, ILabelItem, IListItem {

    public String building;
    public String countryCode; //iso
    public String countryName;
    public String locality;
    public String city;
    public String unit;

    @SerializedName("additionalInfo")
    public String notes;
    public String code;
    public Double lat;
    public Double lon;
    public String uid;
    public String phoneNumber;

    public boolean returnToMainView = false;

    public AddressObject() {
    }

    public AddressObject(Address data) {
        this.code = data.getEddressCode();

        if (data.getCoordinates() != null) {
            lat = data.getCoordinates().lat;
            lon = data.getCoordinates().lon;
        }
        city = data.getCity();
        building = data.getBuilding();
        locality = data.getLocality();
        notes = data.getNotes();
        unit = data.getUnit();

        if (data.getCountry() != null) {
            countryCode = data.getCountry().getIso();
        }
    }

    public AddressObject(AddressObject addressObject) {
        this.update(addressObject);
    }

    public AddressObject clone() {
        return new AddressObject(this);
    }

    @Override
    public String getLabel() {
        return locality;
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        if (Utilities.notEmpty(building)) {
            sb.append(building);
        }
        if (Utilities.notEmpty(unit)) {
            sb.append(unit);
        }
        return sb.toString();
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
        return null;
    }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public int getSortOrder() {
        return 0;
    }

    @Override
    public Integer getViewType() {
        return null;
    }

    @Override
    public int getIcon() {
        return R.drawable.pin_light;
    }

    @Override
    public String getAction() {
        return null;
    }

    @Override
    public boolean getColorizeIcon() {
        return false;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lon);
    }

    public Location getLocation() {
        Location loc1 = new Location("");
        loc1.setLatitude(lat);
        loc1.setLongitude(lon);
        return loc1;

    }

    public Boolean isPinned() {
        return lat != null && lat != 0 && lon != null && lon != 0;
    }

    public void update(AddressObject addressObject) {
        this.unit = addressObject.unit;
        this.building = addressObject.building;
        this.code = addressObject.code;
        this.countryCode = addressObject.countryCode;
        this.countryName = addressObject.countryName;
        this.city = addressObject.city;
        this.lat = addressObject.lat;
        this.lon = addressObject.lon;
        this.locality = addressObject.locality;
        this.notes = addressObject.notes;
        this.uid = addressObject.uid;
        this.phoneNumber = addressObject.phoneNumber;
    }

    public String validate() {
        if (building == null) return "City is required";
        if (locality == null) return "Address is required";
        return null;
    }

    public String getUid() {
        if (uid == null)
            return code;

        return uid;
    }
}
