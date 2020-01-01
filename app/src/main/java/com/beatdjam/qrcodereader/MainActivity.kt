package com.beatdjam.qrcodereader

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
            val bitmap = ZXingModel.makeQRCode(intentString)
            editText.setText(intentString)
            imageView2.setImageBitmap(bitmap)
        }

        // QRコードスキャナ起動
        fab.setOnClickListener { ZXingModel.initiateScan(this) }

        // ボタンを押してQRコード生成
        button.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) ZXingModel.makeQRCode(editText.text.toString())
        }

        // TODO リファクタ
        button2.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).also {
                it.addCategory(Intent.CATEGORY_OPENABLE)
                it.type = "image/*"
            }
            startActivityForResult(intent, RESULT_PICK_IMAGEFILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intantData: Intent?) {
        val result = when {
            // ローカル画像読み取りの場合
            requestCode == RESULT_PICK_IMAGEFILE && resultCode == Activity.RESULT_OK -> {
                getBitmapFromUri(intantData?.data)?.let { ZXingModel.readQRCodeFromImage(it) }
            }
            // カメラからのQRコード読み取り後の処理
            else -> ZXingModel.readQRCodeFromCamera(requestCode, resultCode, intantData)
        }

        when {
            result.isNullOrEmpty() -> super.onActivityResult(requestCode, resultCode, intantData)
            URLUtil.isValidUrl(result) -> dialogAction(
                result,
                "読み取りURLをブラウザで開きますか？",
                "開く",
                ::openBrowser
            )
            else -> dialogAction(
                result,
                "読み取りテキストをクリップボードにコピーしますか？",
                "コピー",
                ::copyToClipBoard
            )
        }
    }

    private fun getBitmapFromUri(uri: Uri?) = when {
        uri != null -> contentResolver
            .openFileDescriptor(uri, "r")
            ?.use { BitmapFactory.decodeFileDescriptor(it.fileDescriptor) }
        else -> null
    }

    private fun dialogAction(
        contents: String,
        title: String,
        positiveText: String,
        positiveEvent: (String) -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(contents)
            .setPositiveButton(positiveText) { _, _ -> positiveEvent(contents) }
            .setNegativeButton("閉じる", null)
            .show()
    }

    private fun openBrowser(contents: String) =
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(contents)))

    private fun copyToClipBoard(contents: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", contents)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
    }
}