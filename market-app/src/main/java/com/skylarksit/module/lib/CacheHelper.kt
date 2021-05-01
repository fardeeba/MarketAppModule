package com.skylarksit.module.lib

import android.content.Context
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception

object CacheHelper {
    fun save(context: Context, location: String, `object`: Any?) {
        try {
            context.openFileOutput("$location.dat", Context.MODE_PRIVATE).use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(`object`)
                    fos.close()
                }
            }
        } catch (ignored: Exception) {
        }
    }

    fun read(context: Context, location: String): Any? {
        try {
            context.openFileInput("$location.dat")
                .use { fis -> ObjectInputStream(fis).use { ois -> return ois.readObject() } }
        } catch (ignored: Exception) {
        }
        return null
    }

    fun delete(context: Context, file: String) {
        context.deleteFile("$file.dat")
    }
}
