package me.chrislane.accudrop.network;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import me.chrislane.accudrop.service.LocationService;

public class Peer2Peer implements WifiP2pManager.ConnectionInfoListener,
        WifiP2pManager.DnsSdServiceResponseListener,
        WifiP2pManager.DnsSdTxtRecordListener,
        Handler.Callback {

    private static final String TAG = Peer2Peer.class.getSimpleName();
    private static final String AVAILABLE = "available";
    private static final String INSTANCE_NAME = "_accudropproximity";
    private static final String SERVICE_TYPE = "_presence._tcp";
    static final int COORD_MSG = 1000;
    static final int COORD_SENDER = 1001;
    static final int SERVER_PORT = 8000;
    private final LocationService context;
    private BroadcastReceiver receiver;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private Handler handler = new Handler(this);

    public Peer2Peer(LocationService context) {
        this.context = context;


        initialise();
        registerAndDiscover();
    }

    private void initialise() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, context.getMainLooper(), null);

        receiver = new BroadcastReceiver(manager, channel, this);
    }

    private void registerAndDiscover() {
        Map<String, String> record = new HashMap<>();
        record.put(AVAILABLE, "visible");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                INSTANCE_NAME, SERVICE_TYPE, record);
        manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Service added");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Failed to add service");
            }
        });

        discoverService();

    }

    private void discoverService() {
        manager.setDnsSdResponseListeners(channel, this, this);

        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Added service request");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "Failed to add service request");
                    }
                });
        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Failed to start service discovery");
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Thread handler;
        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

        if (info.isGroupOwner) {
            Log.d(TAG, "Device is group owner");

            handler = new GroupOwnerHandler(this.handler);
            handler.start();
        } else {
            Log.d(TAG, "Device is a peer");

            handler = new PeerHandler(this.handler, groupOwnerAddress);
            handler.start();
        }
    }

    @Override
    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
        // Check if the service is the same
        if (instanceName.equalsIgnoreCase(INSTANCE_NAME)) {
            Log.d(TAG, "Service (" + instanceName + ") found on " + srcDevice.deviceName);

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = srcDevice.deviceAddress;
            config.wps.setup = WpsInfo.PBC;

            if (serviceRequest != null) {
                manager.removeServiceRequest(channel, serviceRequest,
                        new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Service request removed");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "Failed to remove service request");
                            }
                        });

                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Connected to service");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "Failed to connect to service");
                    }
                });
            }
        }
    }

    @Override
    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
        Log.d(TAG, srcDevice.deviceName + " is " + txtRecordMap.get(AVAILABLE));
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case COORD_MSG:
                byte[] buffer = (byte[]) msg.obj;
                String coordString = new String(buffer, 0, msg.arg1);
                Log.d(TAG, "Coord String: " + coordString);
                break;
            case COORD_SENDER:
                context.setCoordSender((CoordSender) msg.obj);
        }

        return true;
    }

    public IntentFilter getIntentFilter() {
        return intentFilter;
    }

    public BroadcastReceiver getReceiver() {
        return receiver;
    }


}
