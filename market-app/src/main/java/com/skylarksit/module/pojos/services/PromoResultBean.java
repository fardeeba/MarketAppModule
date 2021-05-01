package com.skylarksit.module.pojos.services;

import com.skylarksit.module.pojos.ResponseBean;
import com.skylarksit.module.utils.Utilities;

public class PromoResultBean extends ResponseBean {
	
	public String uid;
	public String discountType;
	public Double discountValue;
    public String currency = "USD";
    public Boolean hideDecimals;
    public String serviceUid;
    public String promoCode;
    public PromocodeApplication appliedOn;

    public String getDiscountLabel(){

        if (discountValue == null || discountType == null){
            return "N/A";
        }

        if (discountType.equals("VALUE")){
            return Utilities.formatCurrency(discountValue,currency, hideDecimals);
        }
        else{
            return discountValue.intValue() + "%";
        }

    }

}
