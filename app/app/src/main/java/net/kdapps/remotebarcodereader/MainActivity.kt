package net.kdapps.remotebarcodereader

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.ln


@Serializable
data class APIRequest(val cmd: String)

@Serializable
data class APIToastRequest(val text: String)

@Serializable
data class APIPlaySoundRequest(val name: String, val volume: Int = 100)

@Serializable
data class APISetTextRequest(val text: String)

@Serializable
data class APISetHtmlRequest(val html: String)

class MainActivity : AppCompatActivity(), QRReaderViewHandler {
    companion object {
        const val TAG = "RemoteBarcodeReader"
    }

    private val infoEl by lazy { findViewById<TextView>(R.id.info) }
    private val qrViewEl by lazy { findViewById<QRReaderViewCodeScanner>(R.id.scanner_view) }

    private lateinit var webSocketServer: MyWebSocketServer

    private var mediaPlayer: MediaPlayer? = null
    private var lastToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        qrViewEl.setHandler(this)

        if (hasRequiredPermissions()) {
            qrViewEl.init()
        } else {
            requestPermission()
        }

        webSocketServer = MyWebSocketServer(9999)
        webSocketServer.onMessageCb = {
            runOnUiThread {
                try {
                    handleApiRequest(it)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        qrViewEl.onResume()
        webSocketServer.onResume()
    }

    override fun onPause() {
        super.onPause()
        qrViewEl.onPause()
        webSocketServer.onPause()
    }

    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 0)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            0 -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    qrViewEl.init()
                } else {
                    finish()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onScan(text: String, format: String, rawData: ByteArray?) {
        try {
            val rawBytesB64 = rawData?.let { Base64.encodeToString(it, Base64.NO_WRAP) }

            Log.v(TAG, "onScan - text: $text, format: $format, rawData: $rawBytesB64")

            webSocketServer.broadcast(JsonObject(mapOf(
                    "text" to JsonPrimitive(text),
                    "format" to JsonPrimitive(format),
                    "raw_bytes" to JsonPrimitive(rawBytesB64),
            )).toString())
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun handleApiRequest(msg: String) {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val header = json.decodeFromString<APIRequest>(msg)

        when (header.cmd) {
            "show_toast" -> {
                val data = json.decodeFromString<APIToastRequest>(msg)

                lastToast?.cancel()
                lastToast = Toast.makeText(this, data.text, Toast.LENGTH_SHORT).also {
                    it.show()
                }
            }
            "play_sound" -> {
                val data = json.decodeFromString<APIPlaySoundRequest>(msg)

                playSound(data.name, data.volume)
            }
            "set_text" -> {
                val data = json.decodeFromString<APISetTextRequest>(msg)

                runOnUiThread {
                    infoEl.visibility = if (data.text.isNotEmpty()) View.VISIBLE else View.GONE
                    infoEl.text = data.text
                }
            }
            "set_html" -> {
                val data = json.decodeFromString<APISetHtmlRequest>(msg)

                infoEl.visibility = if (data.html.isNotEmpty()) View.VISIBLE else View.GONE
                infoEl.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(data.html, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(data.html)
                }
            }
        }
    }

    private fun playSound(name: String, volume: Int = 100) {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }

        val resId = resources.getIdentifier(if (name == "default") "c0" else name, "raw", packageName)
        if (resId == 0)
            return

        val maxVolume = 100f
        val volumeLog = (1.0 - (ln(maxVolume - volume.toDouble()) / ln(maxVolume)))

        mediaPlayer = MediaPlayer.create(this, resId).also {
            it.setVolume(volumeLog.toFloat(), volumeLog.toFloat())
            it.start()
        }
    }
}