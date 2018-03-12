package me.chrislane.accudrop.network;

import android.os.Handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

class PeerHandler extends Thread {

    private final Handler handler;
    private final String groupOwnerAddress;

    PeerHandler(Handler handler, String groupOwnerAddress) {
        this.handler = handler;
        this.groupOwnerAddress = groupOwnerAddress;
    }

    @Override
    public void run() {
        super.run();

        Socket socket = new Socket();
        try {
            socket.setReuseAddress(true);
            socket.bind(null);
            socket.connect(new InetSocketAddress(groupOwnerAddress, Peer2Peer.SERVER_PORT),
                    5000);
            CoordSender coordSender = new CoordSender(socket, handler);
            new Thread(coordSender).start();
        } catch (IOException e) {
            e.printStackTrace();

            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
