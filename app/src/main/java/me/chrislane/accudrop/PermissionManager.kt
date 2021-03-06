package me.chrislane.accudrop

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class PermissionManager(private val activity: Activity) : ActivityCompat.OnRequestPermissionsResultCallback {
    private val parentLayout: View = activity.findViewById(android.R.id.content)

    /**
     * Check if the app has location permissions.
     *
     * @return If the app has location permissions.
     */
    fun checkLocationPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "App has location permission: $hasPermission")
        }

        return hasPermission
    }

    /**
     * Request location permissions.
     *
     * @param reason The reason for requiring location permissions to show to the user.
     */
    fun requestLocationPermission(reason: String) {
        Snackbar.make(parentLayout, reason, Snackbar.LENGTH_INDEFINITE).setAction("OK") {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_LOCATION)
        }.show()
    }

    /**
     *
     * Handle the result from a permissions request.
     *
     * If the user hasn't chosen to not be asked again for permissions and the permission was denied,
     * the user will be prompted to grant the permission again.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            when (requestCode) {
                PERMISSIONS_REQUEST_LOCATION -> {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.d(TAG, "Requesting location permission.")
                        Snackbar.make(parentLayout, "The app requires location permissions",
                                Snackbar.LENGTH_INDEFINITE).setAction("OK") {
                            ActivityCompat.requestPermissions(activity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    PERMISSIONS_REQUEST_LOCATION)
                        }.show()
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = PermissionManager::class.java.simpleName
        private const val PERMISSIONS_REQUEST_LOCATION = 0
    }
}
