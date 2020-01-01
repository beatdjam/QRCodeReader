package com.beatdjam.qrcodereader

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
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val extraText = intent.getStringExtra(Intent.EXTRA_TEXT)
        textView.text = extraText
        if (URLUtil.isValidUrl(extraText)) imageButton2.visibility = View.VISIBLE
        imageButton.setOnClickListener { copyToClipBoard(extraText) }
        imageButton2.setOnClickListener { openBrowser(extraText) }
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
