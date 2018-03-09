package me.chrislane.accudrop.network;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class CoordSender implements Runnable {

    private static final String TAG = CoordSender.class.getSimpleName();
    private final Socket socket;
    private final Handler handler;
    private OutputStream out;

    CoordSender(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            InputStream in = socket.getInputStream();
            out = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            handler.obtainMessage(Peer2Peer.COORD_SENDER, this).sendToTarget();

            while (true) {
                try {
                    bytes = in.read(buffer);
                    if (bytes == -1) {
                        break;
                    }

                    handler.obtainMessage(Peer2Peer.COORD_MSG, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Exception in loop: ", e);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception in run: ", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] buffer) {
        // TODO: Send data as anything but strings...
        AsyncTask.execute(() -> {
            try {
                out.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
