package com.skylarksit.module.libs.alertdialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.skylarksit.module.R;
import com.skylarksit.module.ui.utils.OnOneOffClickListener;

public class NokAlertDialog extends NokAlertDialogBuilder<NokAlertDialog> {

    private Integer type = -1;
    private Button confirmButton;
    private TextView cancelButton;

    public static final Integer SUCCESS = 1;
    public static final Integer WARNING = 2;
    public static final Integer ERROR = 4;

    {
        confirmButton = findView(R.id.confirmButton);
        cancelButton = findView(R.id.cancelButton);

        if (cancelButton!=null)

            cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });
    }

    @Override
    public Dialog show() {

        if (ERROR.equals(type)){
            confirmButton.setVisibility(View.GONE);
            setCancelText(getContext().getString(R.string.dismiss));
        }

        return super.show();
    }

    public NokAlertDialog(Context context, Integer type){
        super(context);
        this.type = type;

    }

    public NokAlertDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayout() {
        return R.layout.nok_alert_layout;
    }

    public NokAlertDialog setConfirmText(String confirmText) {
        confirmButton.setText(confirmText);
        return this;
    }

    public NokAlertDialog setCancelText(String cancelText) {
        cancelButton.setText(cancelText);
        return this;
    }

    public NokAlertDialog showCancelButton(boolean showCancelButton) {
        cancelButton.setVisibility(showCancelButton ? View.VISIBLE:View.GONE);
        return this;
    }

    public void changeAlertType(Integer alertType) {
    }

    public NokAlertDialog setConfirmClickListener(final NokAlertClickListener confirmClickListener){
        if (confirmClickListener!=null)
        {
            confirmButton.setOnClickListener(new OnOneOffClickListener() {
                @Override
                public void onSingleClick(View v) {
                    confirmClickListener.onClick(NokAlertDialog.this);
                    hide();
                }
            });
        }

        return this;
    }

    public NokAlertDialog setCancelClickListener(final NokAlertClickListener cancelClickListener){
        if (cancelClickListener!=null)
        {
            cancelButton.setOnClickListener(new OnOneOffClickListener() {
                @Override
                public void onSingleClick(View v) {
                    cancelClickListener.onClick(NokAlertDialog.this);
                    hide();
                }
            });
        }
        return this;
    }

    public void dismiss(){
        hide();
    }
}
