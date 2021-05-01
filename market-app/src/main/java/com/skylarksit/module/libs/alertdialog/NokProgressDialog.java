package com.skylarksit.module.libs.alertdialog;

import android.content.Context;

import com.skylarksit.module.R;

public class NokProgressDialog extends NokAlertDialogBuilder<NokProgressDialog> {

    public NokProgressDialog(Context context) {
        super(context);

        setCancelable(false);
    }

    @Override
    protected int getLayout() {
        return R.layout.nok_alert_progress_layout;
    }


}
