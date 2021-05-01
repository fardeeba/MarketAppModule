package com.skylarksit.module.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Environment
import com.facebook.imagepipeline.request.ImageRequestBuilder
import java.io.File

object ImageHelper {

    private var uniqueName = "skylarksIt"
    private fun getDiskCacheDir(context: Context?): File? {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        if (context != null && context.externalCacheDir != null) {
            val cachePath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() ||
                !Environment.isExternalStorageRemovable()
            ) context.externalCacheDir!!.path else context.cacheDir.path
            return File(cachePath + File.separator + uniqueName)
        }
        return null
    }

    @JvmStatic
    fun deleteCache(context: Context?) {
        try {
            val dir = getDiskCacheDir(context)
            if (dir != null && dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()!!
            for (aChildren in children) {
                val success = deleteDir(File(dir, aChildren))
                if (!success) {
                    return false
                }
            }
            return dir.delete()
        }
        return false
    }

    fun getDrawable(drawable: Int): Uri {
        return ImageRequestBuilder.newBuilderWithResourceId(drawable).build().sourceUri
    }

    @JvmStatic
    fun changeBitmapColor(sourceBitmap: Bitmap, color: Int): Bitmap {
        val resultBitmap = Bitmap.createBitmap(
            sourceBitmap, 0, 0,
            sourceBitmap.width - 1, sourceBitmap.height - 1
        )
        val p = Paint()
        val filter: ColorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        p.colorFilter = filter
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(resultBitmap, 0f, 0f, p)
        return resultBitmap
    }
}
