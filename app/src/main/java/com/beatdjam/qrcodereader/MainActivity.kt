package com.beatdjam.qrcodereader

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    private val RESULT_PICK_IMAGEFILE = 1000

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
            startActivityForResult(ImageUtil.createIntentOfGetDeviceImage(), RESULT_PICK_IMAGEFILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = when {
            // ローカル画像読み取りの場合
            requestCode == RESULT_PICK_IMAGEFILE && resultCode == Activity.RESULT_OK -> {
                getBitmapFromUri(data?.data)?.let { ZXingUtil.readQRCodeFromImage(it) }
            }
            // カメラからのQRコード読み取り後の処理
            else -> ZXingUtil.readQRCodeFromCamera(requestCode, resultCode, data)
        }

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
     * 画像選択Intentから渡されたURIを用いて、Bitmap取得を行う
     */
    private fun getBitmapFromUri(uri: Uri?) = when {
        uri != null -> contentResolver
            .openFileDescriptor(uri, "r")
            ?.use { BitmapFactory.decodeFileDescriptor(it.fileDescriptor) }
        else -> null
    }
}