package com.beatdjam.qrcodereader

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.beatdjam.qrcodereader.ZXingUtil.RESULT_PICK_IMAGE_FILE
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // 外部から起動されて文字列が渡ってきていたらQR生成
        val intentString = intent.dataString ?: intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!intentString.isNullOrEmpty()) setQRCodeBitmapByText(intentString)

        // ボタンを押してQRコード生成
        create_qr_code.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) setQRCodeBitmapByText(text)
        }

        // QRコードスキャナ起動
        launch_camera.setOnClickListener { ZXingUtil.initiateScan(this) }

        // 端末内画像読み込み
        load_from_local_image.setOnClickListener {
            startActivityForResult(ImageUtil.createGetDeviceImageIntent(), RESULT_PICK_IMAGE_FILE)
        }
        button.setOnClickListener {
            ScreenshotUtil.take(this.contentResolver, imageView2.drawToBitmap(), "hoge")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = getStringFromQRCode(requestCode, resultCode, data)
        when {
            result.isNullOrEmpty() -> super.onActivityResult(requestCode, resultCode, data)
            else -> {
                // 読み取りに成功した文字列が存在すれば読み取り結果画面に遷移
                Intent(applicationContext, ResultActivity::class.java).let {
                    it.putExtra(Intent.EXTRA_TEXT, result)
                    startActivity(it)
                }
            }
        }
    }

    /**
     * 文字列からQRコードを生成してImageViewに設定
     */
    private fun setQRCodeBitmapByText(text: String) {
        val bitmap = ZXingUtil.makeQRCode(text)
        editText.setText(text)
        imageView2.setImageBitmap(bitmap)
        imageView2.visibility = View.VISIBLE
    }

    /**
     * QRコードから文字列を取得
     */
    private fun getStringFromQRCode(requestCode: Int, resultCode: Int, data: Intent?) = when {
        // ローカル画像読み取りの場合
        requestCode == RESULT_PICK_IMAGE_FILE && resultCode == Activity.RESULT_OK -> {
            ImageUtil.getBitmapFromUri(this, data?.data)
                ?.let { ZXingUtil.readQRCodeFromImage(it) }
        }
        // カメラからのQRコード読み取り後の処理
        else -> ZXingUtil.readQRCodeFromCamera(requestCode, resultCode, data)
    }

}

object ScreenshotUtil {
    private const val DIRECTORY_NAME = "com.beatdjam.qrcodereader"
    private const val FILE_EXTENSION = ".jpg"

    fun take(contentResolver: ContentResolver, bitmap: Bitmap, fileName: String) {
        // ギャラリーへの反映時に必要な情報を格納している値です
        // AndroidQ以降とそれより前で設定可能なKeyが異なるため、
        // ここでは共通部分のみ生成してif文内でそれぞれに必要な値を追加しています
        val contentValues = createContentValues(fileName)
        val uri: Uri
        when {
            VERSION_CODES.Q <= VERSION.SDK_INT -> {
                contentValues.apply {
                    // 画像の保存先を指定します（AndroidQより前と指定方法が異なります）
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/$DIRECTORY_NAME"
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                contentResolver.run {
                    uri = insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        ?: return

                    // AndroidQ以降では画像の書き出し前にギャラリーへの登録(正確に言うとMediaStoreへの登録)を
                    // 済ませてから該当Uriに画像を書き出します（処理の順番がAndroidQより前と異なります）
                    openOutputStream(uri).use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }

                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    update(uri, contentValues, null, null)
                }
            }
            else -> {
                // AndroidQより前の処理では一部メソッドがDeprecatedになっていますが、
                // Q以降の処理に互換性がないためSDK_INTによって処理を分岐させています

                // スクリーンショット画像書き出し用のディレクトリ・ファイルを準備
                val directory = File(
                    // ファイルの書き出し先はいくつか候補がありますが、
                    // アプリを削除してもファイルが消えない、外部アプリからアクセス可能という理由から、
                    // getExternalStoragePublicDirectoryを選択しています
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    DIRECTORY_NAME
                )
                if (!directory.exists()) directory.mkdirs()
                val file = File(directory, "$fileName$FILE_EXTENSION")

                // Bitmapをファイルに書き出します
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

                // 書き出した画像をギャラリーに反映させています
                contentResolver.run {
                    contentValues.put(MediaStore.Images.Media.DATA, file.absolutePath)
                    uri = insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        ?: return
                }
            }
        }
    }

    /**
     * [Build.VERSION_CODES.Q]以上とそれより前で設定できる値が違うため、ここでは共通部分のみ設定
     */
    private fun createContentValues(name: String) = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$name$FILE_EXTENSION")
        put(MediaStore.Images.Media.TITLE, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1_000)
    }
}