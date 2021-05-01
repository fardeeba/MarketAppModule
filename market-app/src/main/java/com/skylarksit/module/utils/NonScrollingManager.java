package com.skylarksit.module.utils;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;

public class NonScrollingManager extends LinearLayoutManager {
    public NonScrollingManager(Context context) {
        super(context);

    }

    // it will always pass false to RecyclerView when calling "canScrollVertically()" method.
    @Override
    public boolean canScrollVertically() {
        return false;
    }
}
