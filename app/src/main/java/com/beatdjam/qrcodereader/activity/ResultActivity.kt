package com.beatdjam.qrcodereader.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beatdjam.qrcodereader.R
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // QRコードから読み取った文字列を取り出す
        val extraText = intent.getStringExtra(Intent.EXTRA_TEXT)
        textView.text = extraText
        // Urlとして認識可能な文字列であれば外部アプリで開くボタンを表示
        if (URLUtil.isValidUrl(extraText)) imageButton2.visibility = View.VISIBLE
        imageButton.setOnClickListener { copyToClipBoard(extraText) }
        imageButton2.setOnClickListener { startDefaultIntent(extraText) }
    }


    /**
     * 文字列を端末規定のアプリケーションで開く
     */
    private fun startDefaultIntent(contents: String) =
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(contents)))

    /**
     * クリップボードにコピー
     */
    private fun copyToClipBoard(contents: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", contents)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show()
    }
}
