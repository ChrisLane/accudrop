package me.chrislane.accudrop.network

import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.net.Socket

class CoordSender internal constructor(private val socket: Socket?, private val handler: Handler) : Runnable {
    private var out: OutputStream? = null

    override fun run() {
        try {
            val `in` = socket!!.getInputStream()
            out = socket.getOutputStream()
            val buffer = ByteArray(1024)
            var bytes: Int
            handler.obtainMessage(Peer2Peer.COORD_SENDER, this).sendToTarget()

            while (true) {
                bytes = `in`.read(buffer)
                if (bytes == -1) {
                    break
                }
                handler.obtainMessage(Peer2Peer.COORD_MSG, bytes, -1, buffer)
                        .sendToTarget()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Exception in run: ", e)
        } finally {
            if (socket != null && socket.isConnected) {
                try {
                    socket.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun write(buffer: ByteArray) {
        // TODO: Send data as anything but strings...
        AsyncTask.execute {
            try {
                out?.write(buffer)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val TAG = CoordSender::class.java.simpleName
    }
}
