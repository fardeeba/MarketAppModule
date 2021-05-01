package com.skylarksit.module.libs.alertdialog;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.skylarksit.module.R;
import com.skylarksit.module.ui.utils.OnOneOffClickListener;

public class PromoCodeLayout<Y> extends NokAlertDialogBuilder<PromoCodeLayout> {

    private final View confirmButton;
    private final EditText promoCodeText;

    public interface PromoClickListener {
        void onClick(String promoCode);
    }

    {
        promoCodeText = findView(R.id.promoCodeText);
        confirmButton = findView(R.id.confirmButton);
        View cancelButton = findView(R.id.cancelButton);
        cancelButton.setOnClickListener(new OnOneOffClickListener() {
            @Override
            public void onSingleClick(View v) {
                promoCodeText.setText("");
                hide();
            }
        });
    }

    public PromoCodeLayout(Context context) {
        super(context);
    }

    public PromoCodeLayout setConfirmClickListener(final PromoClickListener confirmClickListener){
        if (confirmClickListener!=null)
        {
            confirmButton.setOnClickListener(new OnOneOffClickListener() {
                @Override
                public void onSingleClick(View v) {
                    confirmClickListener.onClick(promoCodeText.getText().toString());
                }
            });
        }

        return this;
    }

    @Override
    protected int getLayout() {
        return R.layout.promo_popup;
    }


}
