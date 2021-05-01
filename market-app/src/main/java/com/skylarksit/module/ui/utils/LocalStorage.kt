package com.skylarksit.module.ui.utils

import android.content.Context
import android.content.SharedPreferences
import com.skylarksit.module.ui.model.ServicesModel

class LocalStorage {
    private fun getPref(context: Context): SharedPreferences {
        return context.getSharedPreferences("skylarks_preferences", Context.MODE_PRIVATE)
    }

    private fun getEditor(context: Context): SharedPreferences.Editor {
        return context.getSharedPreferences("skylarks_preferences", Context.MODE_PRIVATE).edit()
    }

    fun getString(key: String?): String {
        return getPref(ServicesModel.instance().appContext).getString(key, "")!!
    }

    fun getBoolean(key: String?): Boolean {
        return getPref(ServicesModel.instance().appContext).getBoolean(key, false)
    }

    fun getInt(context: Context, key: String?): Int {
        return getPref(context).getInt(key, -1)
    }

    fun getLong(context: Context, key: String?): Long {
        return getPref(context).getLong(key, -1L)
    }

    fun getFloat(context: Context, key: String?): Float {
        return getPref(context).getFloat(key, -1f)
    }

    fun clear(context: Context) {
        getEditor(context).clear().apply()
    }

    fun clear(context: Context, key: String?) {
        getEditor(context).remove(key).apply()
    }

    fun save(context: Context, key: String?, value: Any?) {
        when (value) {
            is String -> getEditor(context).putString(key, value as String?)
                .apply()
            is Boolean -> getEditor(context).putBoolean(key, (value as Boolean?)!!)
                .apply()
            is Float -> getEditor(context).putFloat(key, (value as Float?)!!)
                .apply()
            is Long -> getEditor(context).putLong(key, (value as Long?)!!)
                .apply()
            is Int -> getEditor(context).putInt(key, (value as Int?)!!).apply()
        }
    }

    companion object {
        private var instance: LocalStorage? = null

        @JvmStatic
        fun instance(): LocalStorage {
            if (instance == null) instance = LocalStorage()
            return instance!!
        }
    }
}
