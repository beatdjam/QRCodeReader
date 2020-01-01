package com.beatdjam.qrcodereader

import android.content.Intent

object ImageUtil {
    /**
     * 端末内画像取得用のActivityを起動するIntentを作成
     */
    fun createIntentOfGetDeviceImage() = Intent(Intent.ACTION_OPEN_DOCUMENT).also {
        it.addCategory(Intent.CATEGORY_OPENABLE)
        it.type = "image/*"
    }

}