package me.chrislane.accudrop.network

import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class GroupOwnerHandler(handler: Handler) : Thread() {
    private val threadPool = ThreadPoolExecutor(
            5, 15, 10, TimeUnit.SECONDS,
            LinkedBlockingQueue())
    private var socket: ServerSocket? = null
    private lateinit var handler: Handler

    init {
        try {
            socket = ServerSocket()
            socket!!.reuseAddress = true
            AsyncTask.execute {
                try {
                    socket!!.bind(InetSocketAddress(Peer2Peer.SERVER_PORT))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            this.handler = handler
            Log.d(TAG, "Socket created")
        } catch (e: IOException) {
            e.printStackTrace()
            threadPool.shutdown()
            Log.e(TAG, "Failed to create socket: ", e)
        }

    }

    override fun run() {
        super.run()

        while (true) {
            try {
                if (socket!!.isBound) {
                    val newSocket = socket!!.accept()
                    threadPool.execute(CoordSender(newSocket, handler))
                }
            } catch (e: IOException) {
                e.printStackTrace()

                threadPool.shutdown()
                if (socket != null && !socket!!.isClosed) {
                    try {
                        socket!!.close()
                    } catch (e1: IOException) {
                        e1.printStackTrace()
                    }

                }
                break
            }

        }
    }

    companion object {

        private val TAG = GroupOwnerHandler::class.java.simpleName
    }
}
