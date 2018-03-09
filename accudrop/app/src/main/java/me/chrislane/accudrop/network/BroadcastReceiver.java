package me.chrislane.accudrop.network;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import static android.net.wifi.p2p.WifiP2pDevice.AVAILABLE;
import static android.net.wifi.p2p.WifiP2pDevice.CONNECTED;
import static android.net.wifi.p2p.WifiP2pDevice.FAILED;
import static android.net.wifi.p2p.WifiP2pDevice.INVITED;
import static android.net.wifi.p2p.WifiP2pDevice.UNAVAILABLE;


public class BroadcastReceiver extends android.content.BroadcastReceiver {

    private static final String TAG = BroadcastReceiver.class.getSimpleName();
    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private final Peer2Peer base;

    public BroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                             Peer2Peer base) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.base = base;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "onReceive: P2P enabled");
            } else {
                Log.d(TAG, "onReceive: P2P disabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "onReceive: peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "onReceive: connection changed");
            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                Log.d(TAG, "Connected and requesting connection info");
                manager.requestConnectionInfo(channel, base);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            switch (device.status) {
                case CONNECTED:
                    Log.d(TAG, "Device status: connected");
                    break;
                case INVITED:
                    Log.d(TAG, "Device status: invited");
                    break;
                case FAILED:
                    Log.d(TAG, "Device status: failed");
                    break;
                case AVAILABLE:
                    Log.d(TAG, "Device status: available");
                    break;
                case UNAVAILABLE:
                    Log.d(TAG, "Device status: unavailable");
                    break;
            }
        }
    }
}
