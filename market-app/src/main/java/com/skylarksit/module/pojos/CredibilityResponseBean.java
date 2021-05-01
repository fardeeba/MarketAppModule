package com.skylarksit.module.pojos;

import com.skylarksit.module.pojos.services.CreditCardBean;
import com.skylarksit.module.pojos.services.GetLocationsResult;

import java.util.List;

public class CredibilityResponseBean extends ResponseBean {

	public List<AddressObject> userStapCodeReserveBean;
    public List<CreditCardBean> creditCards;

    public boolean forceUpdate;
    public String forceUpdateDesc;

    public String paymentGateway;
    public String checkoutProdKey;
    public boolean creditCardsEnabled;

    public String supportPhoneNumber;
    public String supportEmail;
    public String termsUrl;
    public String faqUrl;

    public Double amountInWallet;
    public String tenantCurrency;
    public String tenantCurrencySymbol;
    public Integer exchangeRate;

}
