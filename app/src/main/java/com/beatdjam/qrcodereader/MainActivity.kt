package com.beatdjam.qrcodereader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            IntentIntegrator(this).initiateScan()
        }
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
            .setTitle("読み取りURL")
            .setMessage(url)
            .setPositiveButton("はい") { _, _ -> openBrowser(url) }
            .setNegativeButton("いいえ", null)
            .show()
    }

    private fun openDialogForOther(contents: String) {
        AlertDialog.Builder(this)
            .setTitle("読み取りテキスト")
            .setMessage(contents)
            .setNegativeButton("閉じる", null)
            .show()
    }


    private fun openBrowser(contents: String) {
        val uri = Uri.parse(contents)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
