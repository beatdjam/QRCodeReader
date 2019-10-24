package com.beatdjam.qrcodereader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        when {
            result != null -> openDialog(result.contents)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun openDialog(contents: String) {
        AlertDialog.Builder(this)
            .setTitle("読み取ったURL")
            .setMessage(contents)
            .setPositiveButton("OK") { _, _ ->
                openBrowser(contents)
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun openBrowser(contents: String) {
        val uri = Uri.parse(contents)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
