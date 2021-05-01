package com.skylarksit.module.ui.model;


import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skylarksit.module.R;
import com.skylarksit.module.ui.utils.LocalStorage;
import com.skylarksit.module.utils.Utilities;
import com.fenchtose.tooltip.Tooltip;
import com.fenchtose.tooltip.TooltipAnimation;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class TooltipsManager {

    private static TooltipsManager mInstance = null;

    private Set<String> tooltips = new HashSet<>();
    private boolean initialized = false;

    private TooltipsManager(){}

    private class TooltipItem {

        private View anchor;
        private String text;
        private int position;
        private Context activity;
        private Integer resId;
        private int padding;
        private int viewDelay = 0;


        private TooltipItem(Context activity, View anchor, String text, int position, int resId, int padding, int viewDelay) {
            this.activity = activity;
            this.anchor = anchor;
            this.text = text;
            this.position = position;
            this.resId = resId;
            this.padding = padding;
            this.viewDelay = viewDelay;
        }
    }

    private Queue<TooltipItem> queue = new ArrayDeque<>();

    public void add(Context activity, @NonNull View anchor, @StringRes Integer resId,
                    @Tooltip.Position int position, int padding, int viewDelay) {

        if (!initialized){
            initialized = true;
            loadState(activity);
        }

        String res = String.valueOf(resId);

        if (tooltips.contains(res)){
            return;
        }

        String text = activity.getString(resId);
        if (text.isEmpty()) return;

        if (!queueContains(resId)){
            TooltipItem item = new TooltipItem(activity,anchor,text,position,resId,padding,viewDelay);
            queue.offer(item);
        }


    }

    private boolean queueContains(Integer resId){

        for (TooltipItem item:queue){
            if (item.resId.equals(resId)){
                return true;
            }
        }
        return false;
    }

    public void clear(){
        queue.clear();
    }

    private void loadState(Context context){
        String tooltipsString = LocalStorage.instance().getString("tooltips");
        if (tooltipsString != null){
            String[] sp = tooltipsString.split(",");
            tooltips = new HashSet<>(Arrays.asList(sp));
        }

    }

    private void saveSate(Context context){
        String tools = TextUtils.join(",",tooltips);
        LocalStorage.instance().save(context, "tooltips",tools);
    }

    public void clearTips(Context context) {
        tooltips.clear();
        queue.clear();
        LocalStorage.instance().save(context,"tooltips","");
    }

    public static TooltipsManager instance(){
        if(mInstance == null)
        {
            mInstance = new TooltipsManager();
        }
        return mInstance;
    }

    public void start(Context context) {
        displayNext(context);
    }

    private void displayNext(Context context) {
        if (queue.size()>0){

            TooltipItem item = queue.peek();
            if (item.viewDelay > 0){
                item.viewDelay--;
                return;
            }

            item = queue.poll();
            if (item!=null && item.resId!=null){
                showTooltip(item.activity, item.anchor,item.text, item.position,item.padding);
                tooltips.add(item.resId.toString());
                saveSate(context);
            }
        }
    }

    public void showTooltip(final Context activity, @NonNull View anchor, @StringRes int text,
                            @Tooltip.Position int position, int padding) {
        showTooltip(activity,anchor,activity.getText(text).toString(),position,padding);

    }

    public void showTooltip(final Context activity, @NonNull View anchor, String text,
                            @Tooltip.Position int position, int padding) {

        int tooltipColor = ContextCompat.getColor(activity, R.color.tooltipsBackground);

        RelativeLayout view = (RelativeLayout) LayoutInflater.from(activity).inflate(R.layout.tooltip_view,null);
        TextView textView = view.findViewById(R.id.text);
        textView.setText(text);

//        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) activity
//                .findViewById(android.R.id.content)).getChildAt(0);

        new Tooltip.Builder(activity)
                .anchor(anchor, position)
                .animate(new TooltipAnimation(TooltipAnimation.FADE, 100))
                .autoAdjust(true)
                .content(view)
                .withTip(new Tooltip.Tip(Utilities.dpToPx(12), Utilities.dpToPx(12), tooltipColor, Utilities.dpToPx(3)))
                .withPadding(Utilities.dpToPx(padding))
                .into(ServicesModel.instance().currentActivity.findViewById(android.R.id.content))
//                .into(viewGroup)
                .withListener(new Tooltip.Listener() {
                    @Override
                    public void onDismissed() {
//                        if (!activity.isFinishing())
//                            displayNext();
                    }
                })
                .show();
    }


}
