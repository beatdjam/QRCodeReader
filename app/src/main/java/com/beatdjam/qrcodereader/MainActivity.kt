package com.beatdjam.qrcodereader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.beatdjam.qrcodereader.util.ImageUtil
import com.beatdjam.qrcodereader.util.ZXingUtil
import com.beatdjam.qrcodereader.util.ZXingUtil.RESULT_PICK_IMAGE_FILE
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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
            // 現在時刻をファイル名にする
            val fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            ImageUtil.saveBitmapImage(this.contentResolver, imageView2.drawToBitmap(), fileName)
            Toast.makeText(this, "QRコードを保存しました", Toast.LENGTH_SHORT).show()
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