package com.beatdjam.qrcodereader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.AndroidRuntimeException
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
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
            editText.setText(intentString)
            makeQRCode(editText.text.toString())
        }

        // QRコードスキャナ起動
        fab.setOnClickListener { IntentIntegrator(this).setBeepEnabled(false).initiateScan() }

        // ボタンを押してQRコード生成
        button.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) makeQRCode(editText.text.toString())
        }

        button2.setOnClickListener {
            val bitmap = imageView2.drawable.toBitmap()
            readQRCodeFromImage(bitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator
            .parseActivityResult(requestCode, resultCode, data)
            ?.contents

        when {
            result.isNullOrEmpty() -> super.onActivityResult(requestCode, resultCode, data)
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

    private fun makeQRCode(contents: String) = try {
        val size = 500
        val bitmap = BarcodeEncoder().encodeBitmap(
            contents,
            BarcodeFormat.QR_CODE,
            size,
            size,
            mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
        )
        imageView2.setImageBitmap(bitmap)
    } catch (e: WriterException) {
        throw AndroidRuntimeException("Barcode Error.", e)
    }

    private fun readQRCodeFromImage(bitmap: Bitmap) {
        val readString = with(bitmap) {
            val pixels = IntArray(width * height)
            getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                MultiFormatReader().decode(binaryBitmap)
            } catch (e: NotFoundException) {
                null
            } catch (e: ChecksumException) {
                null
            } catch (e: FormatException) {
                null
            }?.text
        }

        Toast.makeText(this, readString, Toast.LENGTH_SHORT).show()
    }
}
