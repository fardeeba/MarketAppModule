package com.skylarksit.module.utils;

/**
 * Created by alexi on 10/26/15.
 *
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.skylarksit.module.R;
import com.skylarksit.module.ui.utils.LocalStorage;

public class SMSReceiver extends BroadcastReceiver
{
    public static SMSListener smsListener;

    public static void setSmsListener(SMSListener listener) {
        smsListener = listener;
    }

    public void onReceive(Context context, Intent intent)
    {
        if ( context != null && LocalStorage.instance().getBoolean("waitingForSms") && intent != null) {
            LocalStorage.instance().save(context,"waitingForSms", false);
            Bundle myBundle = intent.getExtras();
            SmsMessage[] messages;
            StringBuilder strMessage = new StringBuilder();

            if (myBundle != null) {
                Object[] pdus = (Object[]) myBundle.get("pdus");
                if (pdus == null || pdus.length == 0) {
                    return;
                }

                messages = new SmsMessage[pdus.length];

                for (int i = 0; i < messages.length; i++) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = myBundle.getString("format");
                        if (format == null) {
                            return;
                        }
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    } else {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    strMessage.append("SMS From: ").append(messages[i].getOriginatingAddress());
                    strMessage.append(" : ");
                    strMessage.append(messages[i].getMessageBody());
                    strMessage.append("\n");
                }

                Log.i("SMS", strMessage.toString());
                String sms = context.getString(R.string.eddress_ativation_code);
                if (strMessage.toString().contains(sms)) {
                    int index = strMessage.indexOf(sms);
                    String code = strMessage.substring(index + sms.length(), index + sms.length() + 4);
                    smsListener.callback(code);
                }
            }
        }
    }
}
