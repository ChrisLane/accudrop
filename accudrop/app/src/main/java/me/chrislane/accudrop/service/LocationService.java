package me.chrislane.accudrop.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import me.chrislane.accudrop.R;
import me.chrislane.accudrop.listener.ReadingListener;
import me.chrislane.accudrop.network.BroadcastReceiver;
import me.chrislane.accudrop.network.CoordSender;
import me.chrislane.accudrop.network.Peer2Peer;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;

public class LocationService extends Service {

    private static final String TAG = LocationService.class.getSimpleName();
    private static final float NO_VALUE = 1337;
    private static final String CHANNEL_ID = "AccuDrop";
    private static final int FOREGROUND_ID = 1237;
    private PressureViewModel pressureViewModel;
    private GnssViewModel gnssViewModel;
    private ReadingListener readingListener;
    private BroadcastReceiver receiver;
    private CoordSender coordSender;

    public LocationService() {

    }

    /**
     * Start the foreground service.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gnssViewModel = new GnssViewModel(getApplication());
        pressureViewModel = new PressureViewModel(getApplication());
        DatabaseViewModel databaseViewModel = new DatabaseViewModel(getApplication());
        readingListener = new ReadingListener(gnssViewModel, pressureViewModel, databaseViewModel);

        // Set ground pressure value
        if (intent != null) {
            float groundPressure = intent.getFloatExtra("groundPressure", NO_VALUE);
            if (groundPressure == NO_VALUE) {
                pressureViewModel.setGroundPressure();
            } else {
                pressureViewModel.setGroundPressure(groundPressure);
            }
        } else {
            pressureViewModel.setGroundPressure();
        }

        gnssViewModel.getGnssListener().startListening();
        pressureViewModel.getPressureListener().startListening();

        // Register broadcast p2pReceiver
        Peer2Peer p2p = new Peer2Peer(this);
        receiver = p2p.getReceiver();
        IntentFilter intentFilter = p2p.getIntentFilter();
        registerReceiver(receiver, intentFilter);
        subscribeToLocation();

        // Build notification for foreground service
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create and set required notification channel for Android O
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Add created channel to the system
            NotificationManager notificationManager = (NotificationManager)
                    getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }

            notification =
                    new Notification.Builder(this, CHANNEL_ID)
                            .setContentTitle("AccuDrop")
                            .setContentText("Logging Jump")
                            .build();
        } else {
            notification =
                    new Notification.Builder(this)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setContentTitle("AccuDrop")
                            .setContentText("Logging Jump")
                            .build();
        }

        startForeground(FOREGROUND_ID, notification);
        Log.i(TAG, "Location service started.");

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Stop logging, listeners, and service.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        readingListener.disableLogging();
        gnssViewModel.getGnssListener().stopListening();
        pressureViewModel.getPressureListener().stopListening();

        unregisterReceiver(receiver);

        stopForeground(true);
        Log.i(TAG, "Location service stopped.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setCoordSender(CoordSender coordSender) {
        this.coordSender = coordSender;
    }

    public void subscribeToLocation() {
        final Observer<Location> locationObserver = location -> {
            if (coordSender != null && location != null) {
                coordSender.write(location.toString().getBytes());
            }
        };
        gnssViewModel.getLastLocation().observeForever(locationObserver);
    }
}
