package net.kdapps.remotebarcodereader

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import com.budiyev.android.codescanner.*
import kotlin.math.roundToInt


class QRReaderViewCodeScanner(context: Context, attrs: AttributeSet?) :
        FrameLayout(context, attrs), QRReaderView {

    private lateinit var codeScanner: CodeScanner
    private lateinit var codeScannerView: CodeScannerView

    private var handler: QRReaderViewHandler? = null

    override fun init() {
        codeScannerView = CodeScannerView(context)
        codeScanner = CodeScanner(context, codeScannerView)

        codeScannerView.isAutoFocusButtonVisible = true
        codeScannerView.flashButtonColor = 0xffffffff.toInt()
        codeScannerView.isFlashButtonVisible = true
        codeScannerView.frameAspectRatioHeight = 1f
        codeScannerView.frameAspectRatioWidth = 1f
        codeScannerView.frameColor = 0xffffffff.toInt()
        codeScannerView.frameCornersRadius = dp(0)
        codeScannerView.frameCornersSize = dp(50)
        codeScannerView.frameSize = 0.75f
        codeScannerView.frameThickness = dp(2)
        codeScannerView.maskColor = 0x77000000

        addView(codeScannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.CONTINUOUS
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            handler?.onScan(it.text, it.barcodeFormat.name, it.rawBytes)
        }
        codeScanner.errorCallback = ErrorCallback.SUPPRESS
    }

    override fun onResume() {
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
    }

    override fun setHandler(handler: QRReaderViewHandler) {
        this.handler = handler
    }

    private fun dp(x: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x.toFloat(), resources.displayMetrics).roundToInt()
    }
}