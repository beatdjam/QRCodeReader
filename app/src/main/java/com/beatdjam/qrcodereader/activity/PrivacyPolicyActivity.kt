package com.beatdjam.qrcodereader.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.beatdjam.qrcodereader.R
import kotlinx.android.synthetic.main.activity_notice.*

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
        textView2.text = """
                    『QRCodeUtil』のプライバシーポリシー
                    ・画像/メディア/ファイル（USB ストレージのコンテンツの読み取り、USB ストレージのコンテンツの変更または削除）
                        作成した画像をストレージに保存するために利用しています
                    ・ストレージ（USB ストレージのコンテンツの読み取り、USB ストレージのコンテンツの変更または削除）
                        撮影した画像をストレージに保存するために利用しています
                    ・カメラ（画像と動画の撮影）
                        写真の撮影・プレビューに利用しています
                        撮影された画像は内蔵ストレージや SD カードに保存されます
                """.trimIndent()
    }
}
