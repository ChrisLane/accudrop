package me.chrislane.accudrop;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

public class ApiClient implements OnConnectionFailedListener, ConnectionCallbacks {
    private final GoogleApiClient googleApiClient;
    private MainActivity mainActivity;

    public ApiClient(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        googleApiClient = new GoogleApiClient.Builder(mainActivity)
                .enableAutoManage(mainActivity, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("ApiClient", "Connected");
        mainActivity.getLocationViewModel().startLocationUpdates(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("ApiClient", "Connection suspended");
        mainActivity.getLocationViewModel().stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }
}
