package com.skylarksit.module

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class ForegroundBackgroundListener : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {

    }

}
