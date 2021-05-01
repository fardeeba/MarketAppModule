package com.skylarksit.module.libs.alertdialog;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.skylarksit.module.R;
import com.skylarksit.module.pojos.services.ServiceObject;
import com.skylarksit.module.utils.Utilities;
import com.facebook.drawee.view.SimpleDraweeView;

public class ProviderDialog<Y> extends NokAlertDialogBuilder<ProviderDialog> {


    private SimpleDraweeView image;
    private ImageView etaImage;
    private TextView title;
    private TextView description;
    private TextView specialNotes;
    private TextView openingHours;

    public ProviderDialog(Context context, ServiceObject serviceObject){
        super(context);

        image.setImageURI(serviceObject.getImageUrl());
        description.setText(serviceObject.serviceDescription);
        title.setText(serviceObject.serviceName);
        openingHours.setText(serviceObject.getOpeningHours());

        if (!Utilities.isEmpty(serviceObject.specialNote)){
            specialNotes.setText(serviceObject.specialNote);
        }
        else{
            specialNotes.setVisibility(View.GONE);
        }

    }


    public interface PromoClickListener {
        void onClick(String promoCode);
    }

    {
        image = findView(R.id.image);
        etaImage = findView(R.id.etaImage);
        title = findView(R.id.title);
        description = findView(R.id.description);
        specialNotes = findView(R.id.specialNotes);
        openingHours = findView(R.id.openingHours);

        etaImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.buttonColor));
    }

    public ProviderDialog(Context context) {
        super(context);
    }

    public ProviderDialog setConfirmClickListener(final PromoClickListener confirmClickListener){
        if (confirmClickListener!=null)
        {
        }
        return this;
    }

    @Override
    protected int getLayout() {
        return R.layout.provider_popup;
    }
    

}
