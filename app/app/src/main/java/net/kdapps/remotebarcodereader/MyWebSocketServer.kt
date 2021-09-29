package net.kdapps.remotebarcodereader

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class MyWebSocketServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {
    var onMessageCb: ((message: String) -> Unit)? = null

    init {
        isReuseAddr = true
        connectionLostTimeout = 100
    }

    fun onResume() {
        run {
            start()
        }
    }

    fun onPause() {
        stop()
    }

    override fun onStart() {
    }

    override fun onError(conn: WebSocket, ex: Exception) {
        ex.printStackTrace()
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
    }

    override fun onMessage(conn: WebSocket, message: String) {
        onMessageCb?.invoke(message)
    }

    override fun onMessage(conn: WebSocket, message: ByteBuffer) {
    }
}