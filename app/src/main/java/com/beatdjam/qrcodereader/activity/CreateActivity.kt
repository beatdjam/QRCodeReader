package com.beatdjam.qrcodereader.activity

import android.Manifest
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
import kotlinx.android.synthetic.main.activity_create.*

class CreateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        // 外部から起動されて文字列が渡ってきていたらQR生成
        val intentString = intent.dataString ?: intent.getStringExtra(Intent.EXTRA_TEXT)
        if (!intentString.isNullOrEmpty()) setQRCodeBitmapByText(intentString)

        // ボタンを押してQRコード生成
        imageButton2.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) setQRCodeBitmapByText(text)
        }


        imageButton.setOnClickListener {
            // QRコード保存のために保存用のPermissionを取得する
            when (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                true -> ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS
                )
                else -> {
                    savePicture()

                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) savePicture()
                else Toast.makeText(this, "ストレージへのアクセスが許可されていません", Toast.LENGTH_SHORT).show()
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
        imageButton.visibility = View.VISIBLE
    }

    private fun savePicture() {
        ImageUtil.saveBitmapImage(this.contentResolver, imageView2.drawToBitmap())
        Toast.makeText(this, "QRコードを保存しました", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2000
    }
}
