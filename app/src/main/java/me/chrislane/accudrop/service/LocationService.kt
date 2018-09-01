package me.chrislane.accudrop.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import me.chrislane.accudrop.R
import me.chrislane.accudrop.listener.ReadingListener
import me.chrislane.accudrop.network.BroadcastReceiver
import me.chrislane.accudrop.network.CoordSender
import me.chrislane.accudrop.network.Peer2Peer
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import me.chrislane.accudrop.viewmodel.GnssViewModel
import me.chrislane.accudrop.viewmodel.PressureViewModel

class LocationService : Service() {
    private lateinit var pressureViewModel: PressureViewModel
    private lateinit var gnssViewModel: GnssViewModel
    private lateinit var readingListener: ReadingListener
    private var receiver: BroadcastReceiver? = null
    private var p2p: Peer2Peer? = null
    private var isGuidanceEnabled: Boolean = false

    /**
     * Start the foreground service.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        gnssViewModel = GnssViewModel(application)
        pressureViewModel = PressureViewModel(application)
        val databaseViewModel = DatabaseViewModel(application)
        readingListener = ReadingListener(gnssViewModel, pressureViewModel, databaseViewModel)

        // Get guidance enabled preference
        val preferences = PreferenceManager
                .getDefaultSharedPreferences(applicationContext)
        isGuidanceEnabled = preferences.getBoolean("guidance_enabled", false)

        // Set ground pressure value
        if (intent != null) {
            val groundPressure = intent.getFloatExtra("groundPressure", NO_VALUE)
            if (groundPressure == NO_VALUE) {
                pressureViewModel.setGroundPressure()
            } else {
                pressureViewModel.setGroundPressure(groundPressure)
            }
        } else {
            pressureViewModel.setGroundPressure()
        }

        gnssViewModel.gnssListener.startListening()
        pressureViewModel.pressureListener.startListening()

        // Register broadcast p2pReceiver
        if (isGuidanceEnabled) {
            p2p = Peer2Peer(this)
            receiver = p2p?.receiver
            val intentFilter = p2p?.intentFilter
            registerReceiver(receiver, intentFilter)
        }

        // Build notification for foreground service
        val notification: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create and set required notification channel for Android O
            val name = getString(R.string.notification_channel_name)
            val description = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            // Add created channel to the system
            val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            notification = Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("AccuDrop")
                    .setContentText("Logging Jump")
                    .build()
        } else {
            notification = Notification.Builder(this)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentTitle("AccuDrop")
                    .setContentText("Logging Jump")
                    .build()
        }

        startForeground(FOREGROUND_ID, notification)
        Log.i(TAG, "Location service started.")

        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Stop logging, listeners, and service.
     */
    override fun onDestroy() {
        super.onDestroy()

        readingListener.disableLogging()
        gnssViewModel.gnssListener.stopListening()
        pressureViewModel.pressureListener.stopListening()

        if (receiver != null) {
            unregisterReceiver(receiver)
            p2p!!.endConnection()
        }


        stopForeground(true)
        Log.i(TAG, "Location service stopped.")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun setCoordSender(coordSender: CoordSender) {
        readingListener.setCoordSender(coordSender)
    }

    fun checkProximity(lat: Double?, lng: Double?, altitude: Float?) {
        // TODO: Move proximity checking code into its own class
        val them = Location("")
        them.latitude = lat!!
        them.longitude = lng!!
        them.altitude = altitude!!.toDouble()

        val us = gnssViewModel.getLastLocation().value
        val usAlti = pressureViewModel.getLastAltitude().value

        if (us != null && usAlti != null) {
            us.altitude = usAlti.toDouble()
            val distance = us.distanceTo(them)
            Log.v(TAG, "Proximity = $distance")
            if (distance < 15) {
                // TODO: Replace with a meaningful warning
                // TODO: Save the warning details
                Toast.makeText(this, "Proximity warning. Danger!.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private val TAG = LocationService::class.java.simpleName
        private val NO_VALUE = 1337f
        private val CHANNEL_ID = "AccuDrop"
        private val FOREGROUND_ID = 1237
    }
}
