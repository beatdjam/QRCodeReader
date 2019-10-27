package com.beatdjam.qrcodereader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.AndroidRuntimeException
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { IntentIntegrator(this).initiateScan() }
        button.setOnClickListener { makeQRCode(editText.text.toString()) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator
            .parseActivityResult(requestCode, resultCode, data)
            ?.contents
        // resultが空なら何もせずに終了
        if (result.isNullOrEmpty()) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        // URLの場合のみWeb遷移用ダイアログを開く
        if (URLUtil.isValidUrl(result)) openDialogForUrl(Uri.parse(result).toString())
        else openDialogForOther(result)
    }

    private fun openDialogForUrl(url: String) {
        AlertDialog.Builder(this)
            .setTitle("読み取りURLをブラウザで開きますか？")
            .setMessage(url)
            .setPositiveButton("開く") { _, _ -> openBrowser(url) }
            .setNegativeButton("閉じる", null)
            .show()
    }

    private fun openDialogForOther(contents: String) {
        AlertDialog.Builder(this)
            .setTitle("読み取りテキストをクリップボードにコピーしますか？")
            .setMessage(contents)
            .setPositiveButton("コピー") { _, _ -> copyToClipBoard(contents) }
            .setNegativeButton("閉じる", null)
            .show()
    }


    private fun openBrowser(contents: String) {
        val uri = Uri.parse(contents)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun copyToClipBoard(contents: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", contents)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
    }

    private fun makeQRCode(contents: String) {
        val size = 500
        try {
            //QRコードをBitmapで作成
            val bitmap = BarcodeEncoder().encodeBitmap(contents, BarcodeFormat.QR_CODE, size, size)
            //作成したQRコードを画面上に配置
            imageView2.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            throw AndroidRuntimeException("Barcode Error.", e)
        }
    }
}
