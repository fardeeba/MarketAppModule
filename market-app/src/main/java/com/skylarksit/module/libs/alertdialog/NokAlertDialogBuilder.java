package com.skylarksit.module.libs.alertdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.skylarksit.module.R;

import org.codehaus.jackson.util.VersionUtil;

public abstract class NokAlertDialogBuilder<T extends NokAlertDialogBuilder> {

    private Dialog dialog;
    private View dialogView;

    protected TextView tvTitle;
    private TextView tvMessage;
    private Context context;
    private int titleColor;

    public void setTitleColor(@ColorRes  int titleColor){
        this.titleColor = titleColor;
    }

    public interface NokAlertClickListener {
        void onClick(NokAlertDialog sweetAlertDialog);
    }

    public NokAlertDialogBuilder(Context context) {
        createDialog(new AlertDialog.Builder(context));
        setContext(context);
    }

    public void createDialog(AlertDialog.Builder dialogBuilder) {
        dialogView = LayoutInflater.from(dialogBuilder.getContext()).inflate(getLayout(), null);

        dialog = dialogBuilder.setView(dialogView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setDimAmount(0.75f);
        }

        tvTitle = findView(R.id.dialog_title);

        if (titleColor!=0){
            tvTitle.setTextColor(titleColor);
        }

        tvMessage = findView(R.id.dialog_message);
    }

    protected abstract int getLayout();

    public T setTitle(@StringRes int title) {
        return setTitle(string(title));
    }

    public T setTitle(CharSequence title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        }

        return (T) this;
    }

    public T setMessage(@StringRes int message) {
        return setMessage(string(message));
    }

    public T setMessage(CharSequence message) {
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }

        return (T) this;
    }

    public Dialog show() {
        try {
            if (dialog == null) return null;

            if (dialog.isShowing()) dialog.hide();

            if (context instanceof Activity) {
                if (!((Activity) context).isFinishing()) {
                    dialog.show();
                }
            } else {
                dialog.show();
            }

            if (dialog.getWindow()!=null)
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        } catch (Exception e) {
            Log.e("Alert Dialog", e.getLocalizedMessage());
        }

        return dialog;
    }

    public void hide() {

        if (context == null || (context instanceof Activity && ((Activity) context).isFinishing())) return;

        if (isShowing()){
            try {
                dialog.dismiss();
                dialog.hide();
            } catch (Exception e) {
                Log.d("[AwSDialog : dismiss]", " Error removing dialog");
            }
        }

    }

    public void destroy(){
        try {
            dialog.dismiss();
            dialog.hide();
        } catch (Exception e) {
            Log.d("[AwSDialog : dismiss]", " Error removing dialog");
        }
    }

    public boolean isShowing(){
        return dialog!=null && dialog.isShowing();
    }

    public T setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
        return (T) this;
    }

    protected String string(@StringRes int res) {
        return dialogView.getContext().getString(res);
    }

    protected <ViewClass extends View> ViewClass findView(int id) {
        return (ViewClass) dialogView.findViewById(id);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
