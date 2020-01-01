package com.beatdjam.qrcodereader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.beatdjam.qrcodereader.ZXingUtil.RESULT_PICK_IMAGE_FILE
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // 外部から起動されて文字列が渡ってきていたらQR生成
        val intentString = intent.dataString ?: intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!intentString.isNullOrEmpty()) {
            val bitmap = ZXingUtil.makeQRCode(intentString)
            editText.setText(intentString)
            imageView2.setImageBitmap(bitmap)
        }

        // QRコードスキャナ起動
        fab.setOnClickListener { ZXingUtil.initiateScan(this) }
        // ボタンを押してQRコード生成
        button.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) ZXingUtil.makeQRCode(editText.text.toString())
        }

        // 端末内画像読み込み
        button2.setOnClickListener {
            startActivityForResult(ImageUtil.createGetDeviceImageIntent(), RESULT_PICK_IMAGE_FILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = getStringFromQRCode(requestCode, resultCode, data)
        when {
            result.isNullOrEmpty() -> super.onActivityResult(requestCode, resultCode, data)
            else -> {
                Intent(applicationContext, ResultActivity::class.java).let {
                    it.putExtra(Intent.EXTRA_TEXT, result)
                    startActivity(it)
                }
            }
        }
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
        else -> ZXingUtil.readQRCodeFromCamera(data)
    }

}