package com.skylarksit.module.libs

import android.content.Context

object SkylarksUtil {
    fun assetJSONFile(filename: String?, context: Context): String {
        return try {
            val manager = context.assets
            val file = manager.open(filename!!)
            val formArray = ByteArray(file.available())
            file.read(formArray)
            file.close()
            String(formArray)
        } catch (e: Exception) {
            ""
        }
    }
}
