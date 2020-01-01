package com.beatdjam.qrcodereader

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri

object ImageUtil {
    /**
     * 画像選択Intentから渡されたURIを用いて、Bitmap取得を行う
     */
    fun getBitmapFromUri(context: Context, uri: Uri?) = when {
        uri != null -> context.contentResolver
            .openFileDescriptor(uri, "r")
            ?.use { BitmapFactory.decodeFileDescriptor(it.fileDescriptor) }
        else -> null
    }

    /**
     * 端末内画像取得用のActivityを起動するIntentを作成
     */
    fun createGetDeviceImageIntent() = Intent(Intent.ACTION_OPEN_DOCUMENT).also {
        it.addCategory(Intent.CATEGORY_OPENABLE)
        it.type = "image/*"
    }

}