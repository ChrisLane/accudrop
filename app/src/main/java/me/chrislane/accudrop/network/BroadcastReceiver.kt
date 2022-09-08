package me.chrislane.accudrop.network

import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDevice.*
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log


class BroadcastReceiver(private val manager: WifiP2pManager?, private val channel: WifiP2pManager.Channel,
                        private val base: Peer2Peer) : android.content.BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action) {
            val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "onReceive: P2P enabled")
            } else {
                Log.d(TAG, "onReceive: P2P disabled")
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action) {
            Log.d(TAG, "onReceive: peers changed")
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            Log.d(TAG, "onReceive: connection changed")
            if (manager == null) {
                return
            }

            val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)

            if (networkInfo!!.isConnected) {
                Log.d(TAG, "Connected and requesting connection info")
                manager.requestConnectionInfo(channel, base)
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            val device = intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)

            when (device!!.status) {
                CONNECTED -> Log.d(TAG, "Device status: connected")
                INVITED -> Log.d(TAG, "Device status: invited")
                FAILED -> Log.d(TAG, "Device status: failed")
                AVAILABLE -> Log.d(TAG, "Device status: available")
                UNAVAILABLE -> Log.d(TAG, "Device status: unavailable")
            }
        }
    }

    companion object {
        private val TAG = BroadcastReceiver::class.java.simpleName
    }
}
