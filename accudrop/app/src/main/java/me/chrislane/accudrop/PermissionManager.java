package me.chrislane.accudrop;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

public class PermissionManager {
    private static final int PERMISSIONS_REQUEST_LOCATION = 0;
    private final Activity activity;
    private final View parentLayout;

    public PermissionManager(Activity activity) {
        this.activity = activity;
        parentLayout = activity.findViewById(android.R.id.content);
    }

    public boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(String reason) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(parentLayout, reason,
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", view ->
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_LOCATION)).show();
        }
    }
}
