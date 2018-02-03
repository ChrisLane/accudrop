package me.chrislane.accudrop;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

public class PermissionManager {

    private static final String TAG = PermissionManager.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_LOCATION = 0;
    private final Activity activity;
    private final View parentLayout;

    public PermissionManager(Activity activity) {
        this.activity = activity;
        parentLayout = activity.findViewById(android.R.id.content);
    }

    /**
     * Check if the app has location permissions.
     *
     * @return If the app has location permissions.
     */
    public boolean checkLocationPermission() {
        Boolean hasPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "App has location permission: " + hasPermission.toString());
        return hasPermission;
    }

    /**
     * Request location permissions.
     *
     * @param reason The reason for requiring location permissions to show to the user.
     */
    public void requestLocationPermission(String reason) {
        Snackbar.make(parentLayout, reason, Snackbar.LENGTH_INDEFINITE).setAction("OK", view ->
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION)).show();
    }

    /**
     * <p>Handle the result from a permissions request.</p>
     * <p>If the user hasn't chosen to not be asked again for permissions and the permission was denied,
     * the user will be prompted to grant the permission again.</p>
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            switch (requestCode) {
                case PermissionManager.PERMISSIONS_REQUEST_LOCATION: {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d(TAG, "Requesting location permission.");
                        Snackbar.make(parentLayout, "The app requires location permissions",
                                Snackbar.LENGTH_INDEFINITE).setAction("OK", view ->
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PermissionManager.PERMISSIONS_REQUEST_LOCATION)).show();
                    }
                }
            }
        }
    }
}
