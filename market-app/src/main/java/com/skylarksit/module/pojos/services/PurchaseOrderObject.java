package com.skylarksit.module.pojos.services;

import android.net.Uri;

import com.skylarksit.module.pojos.AddressObject;
import com.skylarksit.module.ui.model.IListItem;
import com.skylarksit.module.ui.model.ServicesModel;
import com.skylarksit.module.utils.U;
import com.skylarksit.module.utils.Utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PurchaseOrderObject implements Serializable,IListItem {
    public String uid;

    public Long id;

    public String providerName;
    public String storeImageUrl;
    public String storeIconUrl;
    public String serviceSlug;
    public String servicePhoneNumber;
    public String deliveryEddressName;

    public String description;
    public String subtitle;

    public boolean canCancelOrder = false;

    public String serviceType;
    public String providerUid;
    public String currency;
    public Boolean hideDecimals;

    public Long creationDate;
    public String status;

    public Double deliveryLat;
    public Double deliveryLon;
    public Double pickupLat;
    public Double pickupLon;
    public String deliveryDetails;

    public Double collectionAmount;
    public Double total;
    public Double subTotal;
    public Double deductionFromWallet;
    public Double minimumChargeFee;
    public Double deliveryCharge;
    public Double deliveryVat;

    public Double vat;
    public Double tips;

    public Integer feedbackRating;
    public Integer thirdPartyFeedbackRating;

    public String eta;

    public String promoCode;
    public String promoCodeUid;
    public String discountType;
    public Double discountValue;
    public PromocodeApplication appliedOn;

    public AddressObject pickup;
    public AddressObject delivery;

    public String pickupEddress;

    public String deliveryEddress;
    public Long deliveryDate;

    public Long deliveryDateLong;
    public String deliveryRecipient;

    public String paymentMethod;

    public String notes;

    public String workerName;
    public String workerPhoneNumber;
    public String workerImageUrl;
    public String imageUrl;
    public String workerUid;
    public boolean fetchDriverLocation;
    public Integer statusTimerSecs;
    public Integer fetchDriverLocationTimerSecs;

    public List<PurchaseOrderItemObject> items = new ArrayList<>();
    public String statusLabel;
    public String creditCardNumber;

    public boolean showFeedback;
    public boolean showThirdPartyFeedback;

    public boolean hasPickup;
    public boolean hasReturn;
    public boolean hasDelivery;

    public boolean showEta;

    public String createdOnString;
    public Long createdOn;
    public Long completedOn;
    public Long pickedUpOn;
    public Long returnedOn;
    public Long confirmedOn;
    public Long deliveredOn;

    public void getDeliveryTime(){

    }

    // this is the title for Delivery fee. e.g. "Delivery Fee" or "Handling Fee"
    public String deliveryFeeLabel;

    // this is the value of delivery fee in readable format e.g. "PKR 12" or "USD 31.9"
    public String deliveryChargeLabel;
    public String minimumChargeLabel;
    public String vatLabel;
    public String totalPriceLabel;
    public String orderSubmittedTitle;
    public String orderSubmittedSubtitle;
    public String confirmedTitle;
    public String confirmedSubtitle;
    public String pickupTitle;
    public String pickupSubtitle;
    public String deliverTitle;
    public String deliverSubtitle;
    public String returnTitle;
    public String returnSubtitle;
    public String canceledTitle;
    public String canceledSubtitle;
    public Boolean pickupComplete;
    public Boolean pickupFailed;
    public Boolean deliveryComplete;
    public Boolean deliveryFailed;
    public Boolean returnComplete;
    public Boolean returnFailed;
    public Boolean confirmComplete;
    public String statusEnum;
    public String serviceTypeImage;
    public String countryIso;


    @Override
    public String getLabel() {
        if (ServicesModel.instance().singleStore && delivery!=null)
            return delivery.getLabel();
        else{
            return providerName;
        }
    }

    @Override
    public String getDescription() {
        return statusLabel;
    }

    @Override
    public String getPrice() {
        return null;
    }

    @Override
    public String getImageUrl() {
        if(imageUrl != null) return imageUrl;
        else if (storeIconUrl != null) return storeIconUrl;
        else if (storeImageUrl != null) return storeImageUrl;
        return serviceTypeImage;
    }

    @Override
    public String getThumbnailUrl() {
        return getImageUrl();
    }

    @Override
    public String getTag() {
        return createdOnString;
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

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public String getDeliveryFeeLabel() {
        if(Utilities.isEmpty(deliveryFeeLabel)){
            return "Delivery Fee";
        }

        return deliveryFeeLabel;
    }

    public void setDeliveryFeeLabel(String deliveryFeeLabel) {
        this.deliveryFeeLabel = deliveryFeeLabel;
    }

    public void setServiceTypeImage(String serviceTypeImage) {
        this.serviceTypeImage = serviceTypeImage;
    }
}
