package com.beatdjam.qrcodereader.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.drawToBitmap
import com.beatdjam.qrcodereader.R
import com.beatdjam.qrcodereader.util.ImageUtil
import com.beatdjam.qrcodereader.util.ZXingUtil
import com.beatdjam.qrcodereader.util.ZXingUtil.RESULT_CAMERA
import com.beatdjam.qrcodereader.util.ZXingUtil.RESULT_PICK_IMAGE_FILE
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


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
            // QRコード保存のために保存用のPermissionを取得する
            when (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                true -> ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS
                )
                else -> {
                    savePicture()
                    Toast.makeText(this, "QRコードを保存しました", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == RESULT_PICK_IMAGE_FILE && resultCode == Activity.RESULT_OK -> {
                val uri = ImageUtil.getBitmapFromUri(this, data?.data)
                    ?.let { ZXingUtil.readQRCodeFromImage(it) }
                if (uri != null) return startResultActivity(uri)
            }

            // カメラからのQRコード読み取り後の処理
            requestCode == RESULT_CAMERA && resultCode == Activity.RESULT_OK -> {
                val uri = ZXingUtil.readQRCodeFromCamera(requestCode, resultCode, data)
                if (uri != null) return startResultActivity(uri)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            // 先ほどの独自定義したrequestCodeの結果確認
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) savePicture()
                else Toast.makeText(this, "ストレージへのアクセスが許可されていません", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startResultActivity(result: String) {
        Intent(applicationContext, ResultActivity::class.java).let {
            it.putExtra(Intent.EXTRA_TEXT, result)
            startActivity(it)
        }
    }


    private fun savePicture() {
        ImageUtil.saveBitmapImage(this.contentResolver, imageView2.drawToBitmap())
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

    companion object {
        private const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2000
    }
}