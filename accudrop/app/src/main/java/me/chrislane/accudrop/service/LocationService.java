package me.chrislane.accudrop.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import me.chrislane.accudrop.listener.ReadingListener;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.JumpViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;

public class LocationService extends Service {

    private static final String TAG = LocationService.class.getSimpleName();
    private static final float NO_VALUE = 1337;
    private PressureViewModel pressureViewModel;
    private GnssViewModel gnssViewModel;
    private ReadingListener readingListener;

    public LocationService() {

    }

    /**
     * Start the foreground service.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gnssViewModel = new GnssViewModel(getApplication());
        pressureViewModel = new PressureViewModel(getApplication());
        JumpViewModel jumpViewModel = new JumpViewModel(getApplication());
        readingListener = new ReadingListener(gnssViewModel, pressureViewModel, jumpViewModel);

        // Set ground pressure value
        float groundPressure = intent.getFloatExtra("groundPressure", NO_VALUE);
        if (groundPressure == NO_VALUE) {
            pressureViewModel.setGroundPressure();
        } else {
            pressureViewModel.setGroundPressure(groundPressure);
        }

        gnssViewModel.getGnssListener().startListening();
        pressureViewModel.getPressureListener().startListening();

        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle("TITLE")
                        .setContentText("MESSAGE")
                        .setTicker("TICKER")
                        .build();


        int FOREGROUND_ID = 1237;
        startForeground(FOREGROUND_ID, notification);
        Log.d(TAG, "Location service started.");

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

        stopForeground(true);
        Log.d(TAG, "Location service stopped.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
