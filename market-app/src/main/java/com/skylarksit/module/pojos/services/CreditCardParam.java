package com.skylarksit.module.pojos.services;

import com.skylarksit.module.pojos.IJsonParam;

public class CreditCardParam implements IJsonParam {
	
	public CreditCardParam() {
		
	}

    public CreditCardParam(String uid,String email) {
        this.uid = uid;
        this.email = email;
    }

    // set only for Paystack
    public String cardNumber;
	public String cvv;

	public Integer expiryMonth;
	public Integer expiryYear;
	public String name;

	public String email;
	public String token;
	
	public String uid;
	
}
