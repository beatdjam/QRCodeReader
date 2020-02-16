package com.beatdjam.qrcodereader.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.beatdjam.qrcodereader.R
import com.beatdjam.qrcodereader.util.ImageUtil
import com.beatdjam.qrcodereader.util.ZXingUtil
import com.beatdjam.qrcodereader.util.ZXingUtil.RESULT_CAMERA
import com.beatdjam.qrcodereader.util.ZXingUtil.RESULT_PICK_IMAGE_FILE
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // QRコード作成画面起動
        edit.setOnClickListener {
            startActivity(Intent(applicationContext, CreateActivity::class.java))
        }

        // QRコードスキャナ起動
        camera.setOnClickListener { ZXingUtil.initiateScan(this) }

        // 端末内画像読み込み
        gallery.setOnClickListener {
            startActivityForResult(ImageUtil.createGetDeviceImageIntent(), RESULT_PICK_IMAGE_FILE)
        }
    }

    // 画像読み込み後の処理を分岐
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == RESULT_PICK_IMAGE_FILE && resultCode == Activity.RESULT_OK -> {
                val uri = ImageUtil.getBitmapFromUri(this, data?.data)
                    ?.let { ZXingUtil.readQRCodeFromImage(it) }
                if (uri != null) return startResultActivity(uri)
            }

            // カメラからのQRコード読み取り後の処理
            requestCode == RESULT_CAMERA && resultCode == Activity.RESULT_OK -> {
                val uri = ZXingUtil.readQRCodeFromCamera(resultCode, data)
                if (uri != null) return startResultActivity(uri)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // 読み込み結果画面の起動
    private fun startResultActivity(result: String) {
        Intent(applicationContext, ResultActivity::class.java).let {
            it.putExtra(Intent.EXTRA_TEXT, result)
            startActivity(it)
        }
    }

}