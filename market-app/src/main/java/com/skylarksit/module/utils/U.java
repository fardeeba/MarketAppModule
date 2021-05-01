package com.skylarksit.module.utils;

import android.net.Uri;

import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class U {

    public static Uri parse(String uri){
        if (uri!=null && !uri.trim().isEmpty())
            return Uri.parse(uri);
        return null;
    }

    public static Uri parse(int drawable){
        return ImageRequestBuilder.newBuilderWithResourceId(drawable).build().getSourceUri();
    }


}
