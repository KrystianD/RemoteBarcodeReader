package net.kdapps.remotebarcodereader

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class QRReaderViewZXing(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs), QRReaderView, ZXingScannerView.ResultHandler {
    private lateinit var mScannerView: ZXingScannerView

    private var handler: QRReaderViewHandler? = null

    override fun init() {
        mScannerView = ZXingScannerView(context)
        mScannerView.setResultHandler(this)

        addView(mScannerView)
    }

    override fun onResume() {
        mScannerView.startCamera()
    }

    override fun onPause() {
        mScannerView.stopCamera()
    }

    override fun setHandler(handler: QRReaderViewHandler) {
        this.handler = handler
    }

    override fun handleResult(rawResult: Result?) {
        rawResult?.let {
            handler?.onScan(it.text, it.barcodeFormat.name, it.rawBytes)
        }
        mScannerView.resumeCameraPreview(this)
    }
}