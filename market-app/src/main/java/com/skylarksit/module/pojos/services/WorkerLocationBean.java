package com.skylarksit.module.pojos.services;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.core.content.ContextCompat;

import com.skylarksit.module.R;
import com.skylarksit.module.utils.ImageHelper;
import com.skylarksit.module.utils.Utilities;
import com.google.android.gms.maps.model.LatLng;

public class WorkerLocationBean {

	public Double lat;
	public Double lon;
    public String vehicleType;

    private LatLng latLon;

    public LatLng getLatLon() {
        if (latLon == null && lat !=null && lon!=null){
            latLon = new LatLng(lat,lon);
        }
        return latLon;
    }

    public Bitmap getVehicleIcon(Context context){
        Bitmap icon = Utilities.getBitmap(context, R.drawable.motorcycle,Utilities.dpToPx(30));
        return ImageHelper.changeBitmapColor(icon, ContextCompat.getColor(context, R.color.secondaryColor));
//        switch (vehicleType){
//            case "MOTORCYCLE":
//                return Utilities.getBitmap(context,R.drawable.motorcycle,Utilities.dpToPx(30));
//            case "CAR":
//                return Utilities.getBitmap(context,R.drawable.car,Utilities.dpToPx(30));
//            case "TRUCK":
//                return Utilities.getBitmap(context,R.drawable.truck,Utilities.dpToPx(30));
//            case "BICYCLE":
//                return Utilities.getBitmap(context,R.drawable.bicycle,Utilities.dpToPx(30));
//            case "RUNNER":
//                break;
//        }
//        return null;
    }


	public WorkerLocationBean() {
}
}
