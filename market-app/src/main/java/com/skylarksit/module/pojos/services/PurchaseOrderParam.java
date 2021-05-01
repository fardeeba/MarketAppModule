package com.skylarksit.module.pojos.services;

import com.skylarksit.module.pojos.IJsonParam;

import java.util.List;

public class PurchaseOrderParam implements IJsonParam {

	public CreditCardParam creditCardParam;
	
	public String orderUid;
	public String serviceUid;
	public String serviceName;
	
	public String currencySymbol;

	public Double deliveryLat;
	public Double deliveryLon;
	public Double pickupLat;
	public Double pickupLon;
	public int promiseTime;

	public Double cashCollection;
	public Double total;
	public Double subTotal;
	public Double deductionFromWallet;
	public Double minimumChargeFee;
	public Double deliveryCharge;
	public Double deliveryVat;
	public Double tips;
	public Double vat;

	public String discountType;
	public Double discountValue;
	
	public String pickupTempName;
	public String pickupTempNumber;
	
	public String pickupEddress;
	public String pickupDate;
	public String pickupTime;
	
	public String deliveryEddress;
	public String deliveryTempName;
	public String deliveryTempNumber;
	
	public String deliveryDate;
	public String deliveryTime;
	public String deliveryRecipient;
	
	public String textOrder;
	public String voiceOrderUrl;

	public String email;
	public String notes;
	public String promoCode;
	public Boolean hasPricing;
	
	public List<PurchaseOrderItemObject> items;
	public String customerIp;
    public String paymentOptionUid;
	public Double deliveryDistanceKm;
	public String storeId;
	public String category;
}
