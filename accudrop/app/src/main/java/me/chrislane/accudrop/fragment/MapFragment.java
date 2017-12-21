package me.chrislane.accudrop.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.PermissionManager;
import me.chrislane.accudrop.R;
import me.chrislane.accudrop.viewmodel.LocationViewModel;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    public static final String TAG = MapFragment.class.getSimpleName();
    private GoogleMap map;
    private LocationViewModel locationViewModel;
    private CameraPosition.Builder camPosBuilder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Add this as a location listener
        if (savedInstanceState == null) {
            MainActivity main = (MainActivity) getActivity();
            if (main != null) {
                locationViewModel = ViewModelProviders.of(main).get(LocationViewModel.class);
            }
        }

        camPosBuilder = new CameraPosition.Builder()
                .zoom(15.5f)
                .bearing(0)
                .tilt(0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapFragment", "Map ready");
        map = googleMap;

        setupMap();
    }

    /**
     * Set up the GoogleMap with initial settings and location.
     */
    private void setupMap() {
        Location loc = locationViewModel.getLastLocation().getValue();
        CameraPosition camPos = camPosBuilder.target(locationViewModel.getLatLng(loc)).build();

        MainActivity main = (MainActivity) getActivity();
        if (main != null) {
            PermissionManager permissionManager = main.getPermissionManager();

            // Initial map setup
            if (permissionManager.checkLocationPermission()) {
                map.setMyLocationEnabled(true);
            } else {
                permissionManager.requestLocationPermission("Location access is required to find your location.");
            }
        }
        map.setBuildingsEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));

        //TODO : Remove example line drawing
        Polyline downWind = map.addPolyline(new PolylineOptions()
                .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
                .width(5)
                .color(Color.RED));
    }
}
