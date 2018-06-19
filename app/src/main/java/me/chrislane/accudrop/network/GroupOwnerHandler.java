package me.chrislane.accudrop.network;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class GroupOwnerHandler extends Thread {

    private static final String TAG = GroupOwnerHandler.class.getSimpleName();
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            5, 15, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());
    private ServerSocket socket = null;
    private Handler handler;

    GroupOwnerHandler(Handler handler) {
        try {
            socket = new ServerSocket();
            socket.setReuseAddress(true);
            AsyncTask.execute(() -> {
                try {
                    socket.bind(new InetSocketAddress(Peer2Peer.SERVER_PORT));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            this.handler = handler;
            Log.d(TAG, "Socket created");
        } catch (IOException e) {
            e.printStackTrace();
            threadPool.shutdown();
            Log.e(TAG, "Failed to create socket: ", e);
        }
    }

    @Override
    public void run() {
        super.run();

        while (true) {
            try {
                if (socket.isBound()) {
                    Socket newSocket = socket.accept();
                    threadPool.execute(new CoordSender(newSocket, handler));
                }
            } catch (IOException e) {
                e.printStackTrace();

                threadPool.shutdown();
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                break;
            }
        }
    }
}
