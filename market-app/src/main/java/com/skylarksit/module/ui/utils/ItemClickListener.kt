package com.skylarksit.module.ui.utils

@Suppress("UNCHECKED_CAST")
abstract class ItemClickListener<T> {
    private val adapter: HFRecyclerView<*>
    private var mLastClickTime = 0L
    private var clickTimeInterval: Long = 200
    fun setClickTimeInterval(interval: Long) {
        clickTimeInterval = interval
    }

    constructor(adapter: HFRecyclerView<*>) {
        this.adapter = adapter
    }

    constructor(adapter: HFRecyclerView<*>, clickTimeInterval: Int) {
        this.clickTimeInterval = clickTimeInterval.toLong()
        this.adapter = adapter
    }

    abstract fun onClick(item: T, position: Int)
    protected fun onClick(): Boolean {
        return false
    }

    fun onItemClick(position: Int) {
        if (clickTimeInterval == 0L) {
            if (!onClick()) {
                val m = adapter.getItem(position) as T
                onClick(m, position)
            }
            return
        }
        val now = System.currentTimeMillis()
        var canClick = true
        if (mLastClickTime != 0L) {
            canClick = now - mLastClickTime >= clickTimeInterval
        }
        if (canClick) {
            if (!onClick()) {
                val m = adapter.getItem(position) as T
                onClick(m, position)
            }
            mLastClickTime = System.currentTimeMillis()
        }
    }
}
