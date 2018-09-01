package me.chrislane.accudrop.network

import android.os.Handler

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

internal class PeerHandler(private val handler: Handler, private val groupOwnerAddress: String) : Thread() {

    override fun run() {
        super.run()

        val socket = Socket()
        try {
            socket.reuseAddress = true
            socket.bind(null)
            socket.connect(InetSocketAddress(groupOwnerAddress, Peer2Peer.SERVER_PORT),
                    5000)
            val coordSender = CoordSender(socket, handler)
            Thread(coordSender).start()
        } catch (e: IOException) {
            e.printStackTrace()

            if (socket.isConnected) {
                try {
                    socket.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }

            }
        }

    }
}
