package me.chrislane.accudrop.network

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Handler
import android.os.Message
import android.util.Log
import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.service.LocationService
import java.util.*

class Peer2Peer(private val context: LocationService) : WifiP2pManager.ConnectionInfoListener, WifiP2pManager.DnsSdServiceResponseListener, WifiP2pManager.DnsSdTxtRecordListener, Handler.Callback {
    var receiver: BroadcastReceiver? = null
        private set
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    val intentFilter = IntentFilter()
    private var serviceRequest: WifiP2pDnsSdServiceRequest? = null
    private val handler = Handler(this)

    init {


        initialise()
        registerAndDiscover()
    }

    private fun initialise() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(context, context.mainLooper, null)

        val channel = channel
        receiver = BroadcastReceiver(manager, channel, this)
    }

    private fun registerAndDiscover() {
        val record = HashMap<String, String>()
        record[AVAILABLE] = "visible"

        val service = WifiP2pDnsSdServiceInfo.newInstance(
                INSTANCE_NAME, SERVICE_TYPE, record)
        manager.addLocalService(channel, service, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                Log.d(TAG, "Service added")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "Failed to add service")
            }
        })

        discoverService()

    }

    private fun discoverService() {
        manager.setDnsSdResponseListeners(channel, this, this)

        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance()
        manager.addServiceRequest(channel, serviceRequest,
                object : WifiP2pManager.ActionListener {

                    override fun onSuccess() {
                        Log.d(TAG, "Added service request")
                    }

                    override fun onFailure(reason: Int) {
                        Log.d(TAG, "Failed to add service request")
                    }
                })
        manager.discoverServices(channel, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                Log.d(TAG, "Service discovery started")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "Failed to start service discovery")
            }
        })
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo) {
        val handler: Thread
        val groupOwnerAddress = info.groupOwnerAddress.hostAddress

        if (info.isGroupOwner) {
            Log.d(TAG, "Device is group owner")

            handler = GroupOwnerHandler(this.handler)
            handler.start()
        } else {
            Log.d(TAG, "Device is a peer")

            handler = PeerHandler(this.handler, groupOwnerAddress)
            handler.start()
        }
    }

    override fun onDnsSdServiceAvailable(instanceName: String, registrationType: String, srcDevice: WifiP2pDevice) {
        // Check if the service is the same
        if (instanceName.equals(INSTANCE_NAME, ignoreCase = true)) {
            Log.d(TAG, "Service (" + instanceName + ") found on " + srcDevice.deviceName)

            val config = WifiP2pConfig()
            config.deviceAddress = srcDevice.deviceAddress
            config.wps.setup = WpsInfo.PBC

            if (serviceRequest != null) {
                manager.removeServiceRequest(channel, serviceRequest,
                        object : WifiP2pManager.ActionListener {

                            override fun onSuccess() {
                                Log.d(TAG, "Service request removed")
                            }

                            override fun onFailure(reason: Int) {
                                Log.d(TAG, "Failed to remove service request")
                            }
                        })

                manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d(TAG, "Connected to service")
                    }

                    override fun onFailure(reason: Int) {
                        Log.d(TAG, "Failed to connect to service")
                    }
                })
            }
        }
    }

    override fun onDnsSdTxtRecordAvailable(fullDomainName: String, txtRecordMap: Map<String, String>, srcDevice: WifiP2pDevice) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, srcDevice.deviceName + " is " + txtRecordMap[AVAILABLE])
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            COORD_MSG -> {
                val buffer = msg.obj as ByteArray
                val coordString = String(buffer, 0, msg.arg1)

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Location String: $coordString")
                }

                val strings = coordString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (strings.size == 3) {
                    val lat = java.lang.Double.valueOf(strings[0])
                    val lng = java.lang.Double.valueOf(strings[1])
                    val altitude = java.lang.Float.valueOf(strings[2])

                    context.checkProximity(lat, lng, altitude)
                }
            }
            COORD_SENDER -> context.setCoordSender(msg.obj as CoordSender)
        }

        return true
    }

    fun endConnection() {
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Group removed")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "Failed to remove group")
            }
        })

        manager.clearLocalServices(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Local services cleared")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "Failed to clear local services")
            }
        })
    }

    companion object {

        private val TAG = Peer2Peer::class.java.simpleName
        private const val AVAILABLE = "available"
        private const val INSTANCE_NAME = "_accudropproximity"
        private const val SERVICE_TYPE = "_presence._tcp"
        internal const val COORD_MSG = 1000
        internal const val COORD_SENDER = 1001
        internal const val SERVER_PORT = 8000
    }
}
