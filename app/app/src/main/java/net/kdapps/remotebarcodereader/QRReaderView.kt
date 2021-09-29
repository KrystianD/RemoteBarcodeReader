package net.kdapps.remotebarcodereader

interface QRReaderViewHandler {
    fun onScan(text: String, format: String, rawData: ByteArray?)
}

interface QRReaderView {
    fun init()
    fun onResume()
    fun onPause()

    fun setHandler(handler: QRReaderViewHandler)
}