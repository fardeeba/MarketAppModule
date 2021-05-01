package com.skylarksit.module.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class MLinearLayoutManger(context: Context?) : LinearLayoutManager(context) {
    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     */
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}
