package com.beatdjam.qrcodereader.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

object ImageUtil {
    private const val DIRECTORY_NAME = "com.beatdjam.qrcodereader"
    private const val FILE_EXTENSION = ".jpg"

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

    /**
     * 与えられたBitmapをAndroidのバージョンに合わせて保存する
     */
    fun saveBitmapImage(contentResolver: ContentResolver, bitmap: Bitmap, fileName: String) {
        // ギャラリーへの反映時に必要な情報を格納している値です
        // AndroidQ以降とそれより前で設定可能なKeyが異なるため、
        // ここでは共通部分のみ生成してif文内でそれぞれに必要な値を追加しています
        val contentValues = createContentValues(fileName)
        when {
            // AndroidQ用の処理
            Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT -> {
                // 画像の保存先を指定
                contentValues.apply {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/${DIRECTORY_NAME}"
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                contentResolver.run {
                    val uri = insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        ?: return

                    // MediaStoreの登録を行ってUriを取得し、該当Uriへ画像を書き出す
                    openOutputStream(uri).use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }

                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    update(uri, contentValues, null, null)
                }
            }
            // それ以前用の処理
            // Qより前のOS用の処理のため一部メソッドがDeprecatedになっているが利用している
            else -> contentResolver.run {
                // FIXME 権限周り調整
                // 書き出し用のディレクトリを作成
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    DIRECTORY_NAME
                )
                if (!directory.exists()) directory.mkdirs()

                // 書き出し用のファイルを作成
                val file = File(directory, "$fileName${FILE_EXTENSION}")
                // Bitmapをファイルに書き出し
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

                contentValues.put(MediaStore.Images.Media.DATA, file.absolutePath)
                insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            }
        }
    }

    /**
     * [Build.VERSION_CODES.Q]以上とそれより前で設定できる値が違うため、ここでは共通部分のみ設定
     */
    private fun createContentValues(name: String) = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$name${FILE_EXTENSION}")
        put(MediaStore.Images.Media.TITLE, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1_000)
    }
}