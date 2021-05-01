package com.skylarksit.module.pojos;

import android.content.Context;

import com.skylarksit.module.R;
import com.skylarksit.module.pojos.services.CreditCardParam;
import com.skylarksit.module.ui.model.IListItem;

public class PaymentOption implements IListItem {

    public String uid;
    public String name;
    public String paymentMethod;
    public CreditCardParam creditCardParam;
    public boolean isSelected;

    public PaymentOption(String paymentMethod, String label, CreditCardParam creditCardParam) {
        this.name = label;
        this.paymentMethod = paymentMethod;
        this.creditCardParam = creditCardParam;
    }

    public String getName(){

        if (paymentMethod!=null){
            switch (paymentMethod) {
                case "CASH":
                    return context.getString(R.string.cash);
                case "POS":
                    return context.getString(R.string.posMachine);
                case "ADDNEW":
                    return context.getString(R.string.payment);
                case "CREDIT":
                    return name.replace("MASTERCARD", "MC").replace("****", "**");
            }
        }
        return name;

    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public String getDescription() {
        if (paymentMethod!=null){
            switch (paymentMethod) {
                case "CASH":
                    return context.getString(R.string.pay_with_cash_on_delivery);
                case "POS":
                    return context.getString(R.string.pay_with_card);
                case "ADDNEW":
                    return context.getString(R.string.save_your_credit_or_debit);
                case "CREDIT":
                    return context.getString(R.string.user_this_card_to_pay);
            }
        }

        return null;
    }

    private Context context;
    public void setContext(Context context){
        this.context = context;
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
        return getImageUrl();
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

        if (paymentMethod!=null){
            switch (paymentMethod) {
                case "CASH":
                    return R.drawable.cash;
                case "POS":
                    return R.drawable.pos;
                case "CREDIT":
                case "ADDNEW":
                    return R.drawable.creditcard;
            }
        }
        return 0;

    }

    @Override
    public String getAction() {
        return null;
    }

    @Override
    public boolean getColorizeIcon() {
        return true;
    }

    public boolean isCash() {
        return paymentMethod.equalsIgnoreCase("Cash");
    }
}
