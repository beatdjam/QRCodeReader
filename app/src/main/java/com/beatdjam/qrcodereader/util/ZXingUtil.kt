package com.beatdjam.qrcodereader.util

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.util.AndroidRuntimeException
import com.beatdjam.qrcodereader.activity.MyCaptureActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.WriterException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder

object ZXingUtil {
    const val RESULT_PICK_IMAGE_FILE = 1000
    const val RESULT_CAMERA = 1001

    /**
     * QRCode読み取りカメラ起動
     */
    fun initiateScan(activity: Activity) =
        IntentIntegrator(activity).apply {
            captureActivity = MyCaptureActivity::class.java
            setPrompt("QRコードを読み取ってください")
            setBeepEnabled(false)
            setRequestCode(RESULT_CAMERA)
        }.initiateScan()

    /**
     * カメラ画像からQRCode読み取りを実行
     */
    fun readQRCodeFromCamera(resultCode: Int, intantData: Intent?) =
        IntentIntegrator.parseActivityResult(resultCode, intantData)?.contents

    /**
     * bitmapからQRCode読み取りを実行
     */
    fun readQRCodeFromImage(bitmap: Bitmap) = with(bitmap) {
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        MultiFormatReader().decode(binaryBitmap)?.text
    }

    /**
     * 文字列からQRCode生成
     */
    fun makeQRCode(contents: String): Bitmap = try {
        val size = 500
        BarcodeEncoder().encodeBitmap(
            contents,
            BarcodeFormat.QR_CODE,
            size,
            size,
            mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
        )
    } catch (e: WriterException) {
        throw AndroidRuntimeException("Barcode Error.", e)
    }
}